package com.logreposit.logrepositapi.services.mqtt.emqx;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxApiError;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthRule;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthUser;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxUserAuthRules;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.LoginRequest;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.LoginResponse;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class EmqxApiClient {
  static final String EMQX_API_ERROR_CODE_NOT_FOUND = "NOT_FOUND";

  private final MqttConfiguration mqttConfiguration;
  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public EmqxApiClient(
      MqttConfiguration mqttConfiguration,
      RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper) {
    this.mqttConfiguration = mqttConfiguration;
    this.objectMapper = objectMapper;

    this.restClient =
        restClientBuilder.baseUrl(mqttConfiguration.getEmqx().getManagementEndpoint()).build();
  }

  public Optional<EmqxAuthUser> retrieveEmqxAuthUser(String username) {
    try {
      final var response =
          this.restClient
              .get()
              .uri("api/v5/authentication/password_based:built_in_database/users/" + username)
              .headers(this::authenticateAndAddBearerAuthHeader)
              .retrieve()
              .toEntity(EmqxAuthUser.class);

      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      final var apiError = parseApiError(e.getResponseBodyAsString());

      if (EMQX_API_ERROR_CODE_NOT_FOUND.equals(apiError.getCode())) {
        log.info("There is no EMQX Auth User client with username '{}' existent yet.", username);

        return Optional.empty();
      }

      throw new EmqxApiClientException("Unable to retrieve EMQX Auth User", e);
    } catch (Exception e) {
      throw new EmqxApiClientException("Unable to retrieve EMQX Auth User", e);
    }
  }

  public EmqxAuthUser createEmqxAuthUser(String username, String password) {
    final var emqxAuthUser =
        EmqxAuthUser.builder().userId(username).password(password).superuser(false).build();

    try {
      final var response =
          this.restClient
              .post()
              .uri("api/v5/authentication/password_based:built_in_database/users")
              .headers(this::authenticateAndAddBearerAuthHeader)
              .body(emqxAuthUser)
              .retrieve()
              .toEntity(EmqxAuthUser.class);

      final var createdAuthUser = response.getBody();

      log.info("Successfully created new EMQX AuthUser: {}", createdAuthUser);

      return createdAuthUser;
    } catch (Exception e) {
      throw new EmqxApiClientException("Unable to create EMQX Auth User", e);
    }
  }

  public void deleteEmqxAuthUser(String username) {
    try {
      this.restClient
          .delete()
          .uri("api/v5/authentication/password_based:built_in_database/users/" + username)
          .headers(this::authenticateAndAddBearerAuthHeader)
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      throw new EmqxApiClientException("Unable to delete EMQX Auth User", e);
    }
  }

  public void createRulesForAuthUser(String username, List<EmqxAuthRule> rules) {
    final var userPermissions = EmqxUserAuthRules.builder().username(username).rules(rules).build();

    log.info("Creating auth rules for '{}': {}", username, rules);

    try {
      this.restClient
          .post()
          .uri("api/v5/authorization/sources/built_in_database/rules/users")
          .headers(this::authenticateAndAddBearerAuthHeader)
          .body(List.of(userPermissions))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      throw new EmqxApiClientException("Unable to create rules for EMQX Auth User", e);
    }
  }

  public List<EmqxAuthRule> listRulesOfAuthUser(String username) {
    try {
      final var rules =
          this.restClient
              .get()
              .uri("api/v5/authorization/sources/built_in_database/rules/users/" + username)
              .headers(this::authenticateAndAddBearerAuthHeader)
              .retrieve()
              .toEntity(EmqxUserAuthRules.class);

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
    } catch (Exception e) {
      throw new EmqxApiClientException("Unable to list rules of Auth User", e);
    }
  }

  public void deleteRulesOfAuthUser(String username) {
    try {
      this.restClient
          .delete()
          .uri("api/v5/authorization/sources/built_in_database/rules/users/" + username)
          .headers(this::authenticateAndAddBearerAuthHeader)
          .retrieve()
          .toBodilessEntity();
    } catch (HttpClientErrorException.NotFound e) {
      final var apiError = parseApiError(e.getResponseBodyAsString());

      if (EMQX_API_ERROR_CODE_NOT_FOUND.equals(apiError.getCode())) {
        log.info(
            "There were no authorization rules configured for client with username '{}'.",
            username);

        return;
      }

      throw new EmqxApiClientException("Unable to delete rules of Auth User", e);
    } catch (Exception e) {
      throw new EmqxApiClientException("Unable to delete rules of Auth User", e);
    }
  }

  private void authenticateAndAddBearerAuthHeader(HttpHeaders headers) {
    final var authToken = retrieveAuthenticationToken();
    headers.setBearerAuth(authToken);
  }

  private String retrieveAuthenticationToken() {
    final var loginRequest =
        new LoginRequest(mqttConfiguration.getUsername(), mqttConfiguration.getPassword());

    final var response =
        this.restClient
            .post()
            .uri("api/v5/login")
            .body(loginRequest)
            .retrieve()
            .toEntity(LoginResponse.class);

    final var loginResponse = response.getBody();

    if (loginResponse == null || StringUtils.isBlank(loginResponse.getToken())) {
      throw new EmqxApiClientException("Unable to retrieve authentication token");
    }

    return loginResponse.getToken();
  }

  private EmqxApiError parseApiError(String body) {
    try {
      return this.objectMapper.readValue(body, EmqxApiError.class);
    } catch (JacksonException e) {
      log.error("Unable to parse EMQX Api Error response. Response Body: {}", body);

      throw new EmqxApiClientException("Unable to parse EMQX Api Error response", e);
    }
  }
}
