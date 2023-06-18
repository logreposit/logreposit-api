package com.logreposit.logrepositapi.services.mqtt.emqx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthAction;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthPermission;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxApiError;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthRule;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthUser;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxUserAuthRules;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.LoginRequest;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.LoginResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class EmqxApiClient {
  private final MqttConfiguration mqttConfiguration;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public EmqxApiClient(
      MqttConfiguration mqttConfiguration,
      RestTemplateBuilder restTemplateBuilder,
      ObjectMapper objectMapper) {
    this.mqttConfiguration = mqttConfiguration;
    this.objectMapper = objectMapper;

    this.restTemplate =
        restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(10)) // TODO: find good value
            .setReadTimeout(Duration.ofSeconds(10)) // TODO: find good value
            .build();
  }

  public EmqxAuthUser createEmqxAuthUser(String username, String password) {
    final var emqxAuthUser =
        EmqxAuthUser.builder().userId(username).password(password).superuser(false).build();

    final var response =
        this.restTemplate.postForEntity(
            createUri("api/v5/authentication/password_based:built_in_database/users"),
            authenticateAndCreateHttpEntity(emqxAuthUser),
            EmqxAuthUser.class);

    return response.getBody();
  }

  public void deleteEmqxAuthUser(String username) {
    this.restTemplate.exchange(
        createUri("api/v5/authentication/password_based:built_in_database/users/" + username),
        HttpMethod.DELETE,
        authenticateAndCreateHttpEntity(),
        Void.class);
  }

  // Response: 204 NO CONTENT
  public void createRuleForAuthUser(
      String username, String topic, AuthPermission permission, AuthAction action) {
    final var userPermissions =
        EmqxUserAuthRules.builder()
            .username(username)
            .rules(
                List.of(
                    EmqxAuthRule.builder()
                        .topic(topic)
                        .permission(permission)
                        .action(action)
                        .build()))
            .build();

    this.restTemplate.postForEntity(
        createUri("api/v5/authorization/sources/built_in_database/rules/users"),
        authenticateAndCreateHttpEntity(List.of(userPermissions)),
        Void.class);
  }

  public void deleteRulesOfAuthUser(String username) {
    // Could result in 404 if nothing exists :)

    try {
      this.restTemplate.exchange(
          createUri("api/v5/authorization/sources/built_in_database/rules/users/" + username),
          HttpMethod.DELETE,
          authenticateAndCreateHttpEntity(),
          Void.class);
    } catch (HttpClientErrorException.NotFound e) {
      final var apiErrorOptional = parseApiError(e.getResponseBodyAsString());

      if (apiErrorOptional.isPresent() && "NOT_FOUND".equals(apiErrorOptional.get().getCode())) {
        log.info(
            "There were no authorization rules configured for client with username '{}'.",
            username);

        return;
      }

      throw new EmqxApiClientException("Unable to delete rules of Auth User", e);
    }
  }

  private HttpEntity<Void> authenticateAndCreateHttpEntity() {
    return authenticateAndCreateHttpEntity(null);
  }

  private <T> HttpEntity<T> authenticateAndCreateHttpEntity(T body) {
    final var headersIncludingAuthToken =
        createAuthenticationHeaders(retrieveAuthenticationToken());

    return new HttpEntity<>(body, headersIncludingAuthToken);
  }

  private MultiValueMap<String, String> createAuthenticationHeaders(String authToken) {
    final var httpHeaders = new HttpHeaders();

    httpHeaders.setBearerAuth(authToken);

    return httpHeaders;
  }

  private String retrieveAuthenticationToken() {
    final var loginUri = createUri("api/v5/login");
    final var loginRequest =
        new LoginRequest(mqttConfiguration.getUsername(), mqttConfiguration.getPassword());
    final var response =
        this.restTemplate.postForEntity(loginUri, loginRequest, LoginResponse.class);
    final var loginResponse = response.getBody();

    if (loginResponse == null || StringUtils.isBlank(loginResponse.getToken())) {
      throw new RuntimeException("TODO!"); // TODO
    }

    return loginResponse.getToken();
  }

  private URI createUri(String path) {
    try {
      final var url = new URL(new URL(mqttConfiguration.getEmqx().getManagementEndpoint()), path);

      return url.toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new EmqxApiClientException("Unable to create URI", e);
    }
  }

  private Optional<EmqxApiError> parseApiError(String body) {
    try {
      final var apiError = this.objectMapper.readValue(body, EmqxApiError.class);

      return Optional.of(apiError);
    } catch (JsonProcessingException e) {
      log.error(
          "Unable to parse EMQX Api Error response. Does the response really come from EMQX? Response Body: {}",
          body);

      throw new RuntimeException("TODO", e); // TODO
    }
  }

  /*
  EMQX Environment Variables:
  - "EMQX_NODE_NAME=emqx@node1.emqx.io"
  - "EMQX_LOG__CONSOLE_HANDLER__LEVEL=debug"
  - "EMQX_DASHBOARD__DEFAULT_USERNAME=administrator"
  - "EMQX_DASHBOARD__DEFAULT_PASSWORD=administrator1"
  - "EMQX_AUTHENTICATION__1__ENABLE=true"
  - "EMQX_AUTHENTICATION__1__BACKEND=built_in_database"
  - "EMQX_AUTHENTICATION__1__MECHANISM=password_based"
  - "EMQX_AUTHORIZATION__NO_MATCH=deny"
  - "EMQX_AUTHORIZATION__SOURCES__1__TYPE=file"
  - "EMQX_AUTHORIZATION__SOURCES__1__ENABLE=false"
  - "EMQX_AUTHORIZATION__SOURCES__1__PATH=etc/acl.conf"
  - "EMQX_AUTHORIZATION__SOURCES__2__ENABLE=true"
  - "EMQX_AUTHORIZATION__SOURCES__2__TYPE=built_in_database"
   */

  /*
  1. => Login =>              curl -vv -X POST http://127.0.0.1:18083/api/v5/login -H 'Content-Type: application/json' -d '{"username": "administrator","password": "administrator1"}'
  2. => List MQTT Users =>    curl -vv -X GET http://127.0.0.1:18083/api/v5/authentication/password_based:built_in_database/users -H 'Accept: application/json' -H 'Authorization: Bearer <TOKEN>'
  3. => Retrieve MQTT user => curl -vv -X GET http://127.0.0.1:18083/api/v5/authentication/password_based:built_in_database/users/myclient1 -H 'Accept: application/json' -H 'Authorization: Bearer <TOKEN>'
  4. => Create MQTT user =>   curl -vv -X POST http://127.0.0.1:18083/api/v5/authentication/password_based:built_in_database/users -H 'Content-Type: application/json' -H 'Authorization: Bearer <TOKEN>' -d '{"user_id": "myclient1", "password": "mypassword1"}'
   */
}
