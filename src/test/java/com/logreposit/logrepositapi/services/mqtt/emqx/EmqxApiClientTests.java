package com.logreposit.logrepositapi.services.mqtt.emqx;

import static com.logreposit.logrepositapi.services.mqtt.emqx.EmqxApiClient.EMQX_API_ERROR_CODE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthAction;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthPermission;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxApiError;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthRule;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthUser;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxUserAuthRules;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.LoginResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.ObjectMapper;

@RestClientTest(EmqxApiClient.class)
@Import(MqttConfiguration.class)
@ActiveProfiles("emqx-test")
public class EmqxApiClientTests {
  @Autowired private EmqxApiClient client;

  @Autowired private MockRestServiceServer server;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void testRetrieveEmqxAuthUser_whenLoginReturnsEmptyToken_expectException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse(""));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> client.retrieveEmqxAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to retrieve EMQX Auth User")
        .hasCauseInstanceOf(EmqxApiClientException.class)
        .hasRootCauseMessage("Unable to retrieve authentication token");
  }

  @Test
  public void testRetrieveEmqxAuthUser_whenEmqxSuccess_expectCorrectResponse() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response =
        objectMapper.writeValueAsString(
            EmqxAuthUser.builder().userId(username).superuser(false).build());

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authentication/password_based:built_in_database/users/"
                    + username))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    final var retrievedUserOptional = client.retrieveEmqxAuthUser(username);

    assertThat(retrievedUserOptional).isPresent();

    final var retrievedUser = retrievedUserOptional.get();

    assertThat(retrievedUser.getUserId()).isEqualTo(username);
    assertThat(retrievedUser.getPassword()).isNull();
    assertThat(retrievedUser.getSuperuser()).isFalse();

    server.verify();
  }

  @Test
  public void testRetrieveEmqxAuthUser_whenEmqxNotFound_expectEmptyOptional() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response =
        objectMapper.writeValueAsString(emqxApiError(EMQX_API_ERROR_CODE_NOT_FOUND, null));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authentication/password_based:built_in_database/users/"
                    + username))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withResourceNotFound().contentType(MediaType.APPLICATION_JSON).body(response));

    final var retrievedUserOptional = client.retrieveEmqxAuthUser(username);

    assertThat(retrievedUserOptional).isEmpty();

    server.verify();
  }

  @Test
  public void testRetrieveEmqxAuthUser_whenEmqxNotFoundWithUnexpectedErrorCode_expectException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response = objectMapper.writeValueAsString(emqxApiError("INVALID", null));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authentication/password_based:built_in_database/users/"
                    + username))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withResourceNotFound().contentType(MediaType.APPLICATION_JSON).body(response));

    assertThatThrownBy(() -> client.retrieveEmqxAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to retrieve EMQX Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.NotFound.class);

    server.verify();
  }

  @Test
  public void testRetrieveEmqxAuthUser_whenEmqxNotFoundWithInvalidResponse_expectException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response = "Not found"; // Invalid JSON - will result in parsing error

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authentication/password_based:built_in_database/users/"
                    + username))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withResourceNotFound().contentType(MediaType.APPLICATION_JSON).body(response));

    assertThatThrownBy(() -> client.retrieveEmqxAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to parse EMQX Api Error response")
        .hasCauseInstanceOf(StreamReadException.class);

    server.verify();
  }

  @Test
  public void testRetrieveEmqxAuthUser_whenLoginError_expectException() {
    final var username = "myTestUser1";

    final var loginResponse =
        objectMapper.writeValueAsString(emqxApiError("BAD_USERNAME_OR_PWD", "Auth failed"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(
            withUnauthorizedRequest().contentType(MediaType.APPLICATION_JSON).body(loginResponse));

    assertThatThrownBy(() -> client.retrieveEmqxAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to retrieve EMQX Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.Unauthorized.class)
        .hasRootCauseMessage(
            "401 Unauthorized: \"{\"code\":\"BAD_USERNAME_OR_PWD\",\"message\":\"Auth failed\"}\"");

    server.verify();
  }

  @Test
  public void testCreateEmqxAuthUser_whenEmqxSuccess_expectCorrectResponse() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response =
        objectMapper.writeValueAsString(
            EmqxAuthUser.builder().userId(username).superuser(false).build());

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authentication/password_based:built_in_database/users"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    final var createdUser = client.createEmqxAuthUser(username, "myPassword");

    assertThat(createdUser.getUserId()).isEqualTo(username);
    assertThat(createdUser.getPassword()).isNull();
    assertThat(createdUser.getSuperuser()).isFalse();

    server.verify();
  }

  @Test
  public void testCreateEmqxAuthUser_whenLoginError_expectException() {
    final var username = "myTestUser1";

    final var loginResponse =
        objectMapper.writeValueAsString(emqxApiError("BAD_USERNAME_OR_PWD", "Auth failed"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(
            withUnauthorizedRequest().contentType(MediaType.APPLICATION_JSON).body(loginResponse));

    assertThatThrownBy(() -> client.createEmqxAuthUser(username, "myPassword"))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to create EMQX Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.Unauthorized.class)
        .hasRootCauseMessage(
            "401 Unauthorized: \"{\"code\":\"BAD_USERNAME_OR_PWD\",\"message\":\"Auth failed\"}\"");

    server.verify();
  }

  @Test
  public void testDeleteEmqxAuthUser_whenSuccess_expectNoException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authentication/password_based:built_in_database/users/"
                    + username))
        .andExpect(method(HttpMethod.DELETE))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withNoContent());

    assertDoesNotThrow(() -> client.deleteEmqxAuthUser(username));

    server.verify();
  }

  @Test
  public void testDeleteEmqxAuthUser_whenLoginError_expectException() {
    final var username = "myTestUser1";

    final var loginResponse =
        objectMapper.writeValueAsString(emqxApiError("BAD_USERNAME_OR_PWD", "Auth failed"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(
            withUnauthorizedRequest().contentType(MediaType.APPLICATION_JSON).body(loginResponse));

    assertThatThrownBy(() -> client.deleteEmqxAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to delete EMQX Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.Unauthorized.class)
        .hasRootCauseMessage(
            "401 Unauthorized: \"{\"code\":\"BAD_USERNAME_OR_PWD\",\"message\":\"Auth failed\"}\"");

    server.verify();
  }

  @Test
  public void testCreateEmqxAuthUserRules_whenSuccess_expectNoException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authorization/sources/built_in_database/rules/users"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withNoContent());

    final var rules =
        List.of(
            EmqxAuthRule.builder()
                .topic("myTopic")
                .permission(AuthPermission.ALLOW)
                .action(AuthAction.SUBSCRIBE)
                .build());

    assertDoesNotThrow(() -> client.createRulesForAuthUser(username, rules));

    server.verify();
  }

  @Test
  public void testCreateEmqxAuthUserRules_whenLoginError_expectException() {
    final var username = "myTestUser1";

    final var loginResponse =
        objectMapper.writeValueAsString(emqxApiError("BAD_USERNAME_OR_PWD", "Auth failed"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(
            withUnauthorizedRequest().contentType(MediaType.APPLICATION_JSON).body(loginResponse));

    final var rules =
        List.of(
            EmqxAuthRule.builder()
                .topic("myTopic")
                .permission(AuthPermission.ALLOW)
                .action(AuthAction.SUBSCRIBE)
                .build());

    assertThatThrownBy(() -> client.createRulesForAuthUser(username, rules))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to create rules for EMQX Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.Unauthorized.class)
        .hasRootCauseMessage(
            "401 Unauthorized: \"{\"code\":\"BAD_USERNAME_OR_PWD\",\"message\":\"Auth failed\"}\"");

    server.verify();
  }

  @Test
  public void testListEmqxAuthUserRules_whenEmqxSuccess_expectCorrectResponse() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response =
        objectMapper.writeValueAsString(
            EmqxUserAuthRules.builder()
                .username(username)
                .rules(
                    List.of(
                        EmqxAuthRule.builder()
                            .topic("t0")
                            .permission(AuthPermission.ALLOW)
                            .action(AuthAction.SUBSCRIBE)
                            .build(),
                        EmqxAuthRule.builder()
                            .topic("t1")
                            .permission(AuthPermission.ALLOW)
                            .action(AuthAction.PUBLISH)
                            .build()))
                .build());

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authorization/sources/built_in_database/rules/users/myTestUser1"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    final var authUserRules = client.listRulesOfAuthUser(username);

    assertThat(authUserRules).hasSize(2);

    assertThat(authUserRules.get(0).getTopic()).isEqualTo("t0");
    assertThat(authUserRules.get(0).getPermission()).isEqualTo(AuthPermission.ALLOW);
    assertThat(authUserRules.get(0).getAction()).isEqualTo(AuthAction.SUBSCRIBE);
    assertThat(authUserRules.get(1).getTopic()).isEqualTo("t1");
    assertThat(authUserRules.get(1).getPermission()).isEqualTo(AuthPermission.ALLOW);
    assertThat(authUserRules.get(1).getAction()).isEqualTo(AuthAction.PUBLISH);

    server.verify();
  }

  @Test
  public void testListEmqxAuthUserRules_whenEmqxNotFound_expectCorrectResponse() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response =
        objectMapper.writeValueAsString(emqxApiError(EMQX_API_ERROR_CODE_NOT_FOUND, null));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authorization/sources/built_in_database/rules/users/myTestUser1"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withResourceNotFound().contentType(MediaType.APPLICATION_JSON).body(response));

    final var authUserRules = client.listRulesOfAuthUser(username);

    assertThat(authUserRules).isNotNull().hasSize(0);

    server.verify();
  }

  @Test
  public void testListEmqxAuthUserRules_whenEmqxNotFoundAndUnexpectedErrorCode_expectException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response = objectMapper.writeValueAsString(emqxApiError("INVALID", null));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authorization/sources/built_in_database/rules/users/myTestUser1"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withResourceNotFound().contentType(MediaType.APPLICATION_JSON).body(response));

    assertThatThrownBy(() -> client.listRulesOfAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to list rules of Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.NotFound.class);

    server.verify();
  }

  @Test
  public void testListEmqxAuthUserRules_whenLoginError_expectException() {
    final var username = "myTestUser1";

    final var loginResponse =
        objectMapper.writeValueAsString(emqxApiError("BAD_USERNAME_OR_PWD", "Auth failed"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(
            withUnauthorizedRequest().contentType(MediaType.APPLICATION_JSON).body(loginResponse));

    assertThatThrownBy(() -> client.listRulesOfAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to list rules of Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.Unauthorized.class)
        .hasRootCauseMessage(
            "401 Unauthorized: \"{\"code\":\"BAD_USERNAME_OR_PWD\",\"message\":\"Auth failed\"}\"");

    server.verify();
  }

  @Test
  public void testDeleteEmqxAuthUserRules_whenEmqxSuccess_expectNoException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authorization/sources/built_in_database/rules/users/myTestUser1"))
        .andExpect(method(HttpMethod.DELETE))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withNoContent());

    assertDoesNotThrow(() -> client.deleteRulesOfAuthUser(username));

    server.verify();
  }

  @Test
  public void testDeleteEmqxAuthUserRules_whenEmqxNotFound_expectNoException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response =
        objectMapper.writeValueAsString(emqxApiError(EMQX_API_ERROR_CODE_NOT_FOUND, null));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authorization/sources/built_in_database/rules/users/myTestUser1"))
        .andExpect(method(HttpMethod.DELETE))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withResourceNotFound().contentType(MediaType.APPLICATION_JSON).body(response));

    assertDoesNotThrow(() -> client.deleteRulesOfAuthUser(username));

    server.verify();
  }

  @Test
  public void testDeleteEmqxAuthUserRules_whenEmqxNotFoundAndUnexpectedErrorCode_expectException() {
    final var username = "myTestUser1";

    final var loginResponse = objectMapper.writeValueAsString(loginResponse("myToken"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON));

    final var response = objectMapper.writeValueAsString(emqxApiError("INVALID", null));

    server
        .expect(
            requestTo(
                "http://myEmqx:18083/api/v5/authorization/sources/built_in_database/rules/users/myTestUser1"))
        .andExpect(method(HttpMethod.DELETE))
        .andExpect(header("Authorization", "Bearer myToken"))
        .andRespond(withResourceNotFound().contentType(MediaType.APPLICATION_JSON).body(response));

    assertThatThrownBy(() -> client.deleteRulesOfAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to delete rules of Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.NotFound.class);

    server.verify();
  }

  @Test
  public void testDeleteEmqxAuthUserRules_whenLoginError_expectException() {
    final var username = "myTestUser1";

    final var loginResponse =
        objectMapper.writeValueAsString(emqxApiError("BAD_USERNAME_OR_PWD", "Auth failed"));

    server
        .expect(requestTo("http://myEmqx:18083/api/v5/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"username\":\"adminUser\",\"password\":\"adminPassword\"}"))
        .andRespond(
            withUnauthorizedRequest().contentType(MediaType.APPLICATION_JSON).body(loginResponse));

    assertThatThrownBy(() -> client.deleteRulesOfAuthUser(username))
        .isExactlyInstanceOf(EmqxApiClientException.class)
        .hasMessage("Unable to delete rules of Auth User")
        .hasCauseInstanceOf(HttpClientErrorException.Unauthorized.class)
        .hasRootCauseMessage(
            "401 Unauthorized: \"{\"code\":\"BAD_USERNAME_OR_PWD\",\"message\":\"Auth failed\"}\"");

    server.verify();
  }

  private LoginResponse loginResponse(String token) {
    final var loginResponse = new LoginResponse();

    loginResponse.setToken(token);

    return loginResponse;
  }

  private EmqxApiError emqxApiError(String code, String message) {
    final var emqxApiError = new EmqxApiError();

    emqxApiError.setCode(code);
    emqxApiError.setMessage(message);

    return emqxApiError;
  }
}
