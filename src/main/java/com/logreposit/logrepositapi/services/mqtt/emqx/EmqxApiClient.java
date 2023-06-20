package com.logreposit.logrepositapi.services.mqtt.emqx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.configuration.MqttConfiguration;
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
  private static final String EMQX_API_ERROR_CODE_NOT_FOUND = "NOT_FOUND";

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
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
  }

  public Optional<EmqxAuthUser> retrieveEmqxAuthUser(String username) {
    try {
      final var response =
          this.restTemplate.exchange(
              createUri("api/v5/authentication/password_based:built_in_database/users/" + username),
              HttpMethod.GET,
              authenticateAndCreateHttpEntity(),
              EmqxAuthUser.class);

      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      final var apiError = parseApiError(e.getResponseBodyAsString());

      if (EMQX_API_ERROR_CODE_NOT_FOUND.equals(apiError.getCode())) {
        log.info("There is no EMQX Auth User client with username '{}' existent yet.", username);

        return Optional.empty();
      }

      throw new EmqxApiClientException("Unable to retrieve EMQX Auth User", e);
    }
  }

  public EmqxAuthUser createEmqxAuthUser(String username, String password) {
    final var emqxAuthUser =
        EmqxAuthUser.builder().userId(username).password(password).superuser(false).build();

    final var response =
        this.restTemplate.postForEntity(
            createUri("api/v5/authentication/password_based:built_in_database/users"),
            authenticateAndCreateHttpEntity(emqxAuthUser),
            EmqxAuthUser.class);

    final var createdAuthUser = response.getBody();

    log.info("Successfully created new EMQX AuthUser: {}", createdAuthUser);

    return createdAuthUser;
  }

  public void deleteEmqxAuthUser(String username) {
    this.restTemplate.exchange(
        createUri("api/v5/authentication/password_based:built_in_database/users/" + username),
        HttpMethod.DELETE,
        authenticateAndCreateHttpEntity(),
        Void.class);
  }

  // Response: 204 NO CONTENT
  public void createRulesForAuthUser(String username, List<EmqxAuthRule> rules) {
    final var userPermissions = EmqxUserAuthRules.builder().username(username).rules(rules).build();

    log.info("Creating auth rules for '{}': {}", username, rules);

    this.restTemplate.postForEntity(
        createUri("api/v5/authorization/sources/built_in_database/rules/users"),
        authenticateAndCreateHttpEntity(List.of(userPermissions)),
        Void.class);
  }

  public List<EmqxAuthRule> listRulesOfAuthUser(String username) {
    try {
      final var rules =
          this.restTemplate.exchange(
              createUri("api/v5/authorization/sources/built_in_database/rules/users/" + username),
              HttpMethod.GET,
              authenticateAndCreateHttpEntity(),
              EmqxUserAuthRules.class);

      if (rules.getBody() == null || rules.getBody().getRules() == null) {
        throw new EmqxApiClientException("Unable to list rules of Auth User, result is null.");
      }

      return rules.getBody().getRules();
    } catch (HttpClientErrorException.NotFound e) {
      final var apiError = parseApiError(e.getResponseBodyAsString());

      if (EMQX_API_ERROR_CODE_NOT_FOUND.equals(apiError.getCode())) {
        log.info(
            "There are no authorization rules configured for client with username '{}' yet.",
            username);

        return List.of();
      }

      throw new EmqxApiClientException("Unable to list rules of Auth User", e);
    }
  }

  public void deleteRulesOfAuthUser(String username) {
    try {
      this.restTemplate.exchange(
          createUri("api/v5/authorization/sources/built_in_database/rules/users/" + username),
          HttpMethod.DELETE,
          authenticateAndCreateHttpEntity(),
          Void.class);
    } catch (HttpClientErrorException.NotFound e) {
      final var apiError = parseApiError(e.getResponseBodyAsString());

      if (EMQX_API_ERROR_CODE_NOT_FOUND.equals(apiError.getCode())) {
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
    final var loginRequest =
        new LoginRequest(mqttConfiguration.getUsername(), mqttConfiguration.getPassword());

    final var response =
        this.restTemplate.postForEntity(createUri("api/v5/login"), loginRequest, LoginResponse.class);

    final var loginResponse = response.getBody();

    if (loginResponse == null || StringUtils.isBlank(loginResponse.getToken())) {
      throw new EmqxApiClientException("Unable to retrieve authentication token");
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

  private EmqxApiError parseApiError(String body) {
    try {
      return this.objectMapper.readValue(body, EmqxApiError.class);
    } catch (JsonProcessingException e) {
      log.error(
          "Unable to parse EMQX Api Error response. Response Body: {}", body);

      throw new EmqxApiClientException("Unable to parse EMQX Api Error response", e);
    }
  }
}
