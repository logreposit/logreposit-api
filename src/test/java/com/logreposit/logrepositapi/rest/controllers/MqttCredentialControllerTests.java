package com.logreposit.logrepositapi.rest.controllers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.dtos.request.MqttCredentialRequestDto;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialNotFoundException;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {MqttCredentialController.class})
public class MqttCredentialControllerTests {
  private static final MediaType EXPECTED_CONTENT_TYPE = MediaType.APPLICATION_JSON;

  private static final String REGULAR_USER_ID = ControllerTestUtils.getRegularUser().getId();
  private static final MqttRole READ_ONLY_ROLE = MqttRole.ACCOUNT_DEVICE_DATA_READ;
  private static final List<MqttRole> READ_ONLY_ROLES = List.of(READ_ONLY_ROLE);
  private static final String MQTT_CREDENTIAL_SAMPLE_DESCRIPTION =
      "some informative text where this credential is going to be used";

  @MockBean private UserService userService;

  @MockBean private DeviceService deviceService;

  @MockBean private MqttCredentialService mqttCredentialService;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private MockMvc controller;

  @BeforeEach
  public void setUp() throws UserNotFoundException, ApiKeyNotFoundException {
    ControllerTestUtils.prepareDefaultUsers(userService);
  }

  @Test
  public void testCreate() throws Exception {
    final var regularUser = ControllerTestUtils.getRegularUser();
    final var creationRequestDto = sampleMqttCredentialRequestDto();
    final var credential = sampleMqttCredential(regularUser.getId());

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.post("/v1/account/mqtt-credentials")
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(creationRequestDto));

    when(this.mqttCredentialService.create(
            eq(REGULAR_USER_ID), eq(creationRequestDto.getDescription()), eq(READ_ONLY_ROLES)))
        .thenReturn(credential);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isCreated())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.id").value(credential.getId()))
        .andExpect(jsonPath("$.data.username").value("mqtt-user"))
        .andExpect(jsonPath("$.data.password").value("mqtt-password"))
        .andExpect(jsonPath("$.data.password").value("mqtt-password"))
        .andExpect(jsonPath("$.data.description").value(MQTT_CREDENTIAL_SAMPLE_DESCRIPTION))
        .andExpect(jsonPath("$.data.roles.length()").value(1))
        .andExpect(jsonPath("$.data.roles[0]").value(READ_ONLY_ROLE.toString()))
        .andExpect(jsonPath("$.data.createdAt").isString());

    verify(this.mqttCredentialService, times(1))
        .create(eq(REGULAR_USER_ID), eq(MQTT_CREDENTIAL_SAMPLE_DESCRIPTION), eq(READ_ONLY_ROLES));
  }

  @Test
  public void testList() throws Exception {
    final int defaultPageNumber = 0;
    final int defaultPageSize = 10;

    final var regularUser = ControllerTestUtils.getRegularUser();

    final var credential1 = sampleMqttCredential(regularUser.getId());
    final var credential2 = sampleMqttCredential(regularUser.getId());

    final var credentials = List.of(credential1, credential2);

    when(this.mqttCredentialService.list(eq(regularUser.getId()), anyInt(), anyInt()))
        .thenReturn(new PageImpl<>(credentials));

    final var request =
        MockMvcRequestBuilders.get("/v1/account/mqtt-credentials")
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.totalElements").value(2))
        .andExpect(jsonPath("$.data.totalPages").value(1))
        .andExpect(jsonPath("$.data.items").isArray())
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].id").value(credential1.getId()))
        .andExpect(jsonPath("$.data.items[0].username").value(credential1.getUsername()))
        .andExpect(jsonPath("$.data.items[0].password").value(credential1.getPassword()))
        .andExpect(jsonPath("$.data.items[0].description").value(credential1.getDescription()))
        .andExpect(jsonPath("$.data.items[0].roles.length()").value(1))
        .andExpect(jsonPath("$.data.items[0].roles[0]").value(READ_ONLY_ROLE.toString()))
        .andExpect(jsonPath("$.data.items[0].createdAt").isString())
        .andExpect(jsonPath("$.data.items[1].id").value(credential2.getId()))
        .andExpect(jsonPath("$.data.items[1].username").value(credential2.getUsername()))
        .andExpect(jsonPath("$.data.items[1].password").value(credential2.getPassword()))
        .andExpect(jsonPath("$.data.items[1].description").value(credential2.getDescription()))
        .andExpect(jsonPath("$.data.items[1].roles.length()").value(1))
        .andExpect(jsonPath("$.data.items[1].roles[0]").value(READ_ONLY_ROLE.toString()))
        .andExpect(jsonPath("$.data.items[1].createdAt").isString());

    verify(this.mqttCredentialService, times(1))
        .list(eq(regularUser.getId()), eq(defaultPageNumber), eq(defaultPageSize));
  }

  @Test
  public void testList_customPaginationSettings() throws Exception {
    final int pageNumber = 1;
    final int pageSize = 8;

    final var regularUser = ControllerTestUtils.getRegularUser();

    final var credential1 = sampleMqttCredential(regularUser.getId());
    final var credential2 = sampleMqttCredential(regularUser.getId());

    final var credentials = List.of(credential1, credential2);

    when(this.mqttCredentialService.list(eq(regularUser.getId()), eq(pageNumber), eq(pageSize)))
        .thenReturn(new PageImpl<>(credentials));

    final var request =
        MockMvcRequestBuilders.get(
                "/v1/account/mqtt-credentials?page=" + pageNumber + "&size=" + pageSize)
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.totalElements").value(2))
        .andExpect(jsonPath("$.data.totalPages").value(1))
        .andExpect(jsonPath("$.data.items").isArray())
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].id").value(credential1.getId()))
        .andExpect(jsonPath("$.data.items[0].username").value(credential1.getUsername()))
        .andExpect(jsonPath("$.data.items[0].password").value(credential1.getPassword()))
        .andExpect(jsonPath("$.data.items[0].description").value(credential1.getDescription()))
        .andExpect(jsonPath("$.data.items[0].roles.length()").value(1))
        .andExpect(jsonPath("$.data.items[0].roles[0]").value(READ_ONLY_ROLE.toString()))
        .andExpect(jsonPath("$.data.items[0].createdAt").isString())
        .andExpect(jsonPath("$.data.items[1].id").value(credential2.getId()))
        .andExpect(jsonPath("$.data.items[1].username").value(credential2.getUsername()))
        .andExpect(jsonPath("$.data.items[1].password").value(credential2.getPassword()))
        .andExpect(jsonPath("$.data.items[1].description").value(credential2.getDescription()))
        .andExpect(jsonPath("$.data.items[1].roles.length()").value(1))
        .andExpect(jsonPath("$.data.items[1].roles[0]").value(READ_ONLY_ROLE.toString()))
        .andExpect(jsonPath("$.data.items[1].createdAt").isString());

    verify(this.mqttCredentialService, times(1)).list(eq(regularUser.getId()), eq(1), eq(8));
  }

  @Test
  public void testList_customPaginationSettings_exceedsLimits() throws Exception {
    final int pageNumber = -1;
    final int pageSize = 40;

    final var request =
        MockMvcRequestBuilders.get(
                "/v1/account/mqtt-credentials?page=" + pageNumber + "&size=" + pageSize)
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(80016))
        .andExpect(
            jsonPath("$.message")
                .value(containsString("list.size: size must be less or equal than 25")))
        .andExpect(
            jsonPath("$.message")
                .value(containsString("list.page: page must be greater than or equal to 0")));
  }

  @Test
  public void testGet() throws Exception {
    final var regularUser = ControllerTestUtils.getRegularUser();
    final var credential = sampleMqttCredential(regularUser.getId());

    when(this.mqttCredentialService.get(eq(credential.getId()), eq(regularUser.getId())))
        .thenReturn(credential);

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get("/v1/account/mqtt-credentials/" + credential.getId())
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.id").value(credential.getId()))
        .andExpect(jsonPath("$.data.username").value(credential.getUsername()))
        .andExpect(jsonPath("$.data.password").value(credential.getPassword()))
        .andExpect(jsonPath("$.data.description").value(credential.getDescription()))
        .andExpect(jsonPath("$.data.roles.length()").value(1))
        .andExpect(jsonPath("$.data.roles[0]").value(READ_ONLY_ROLE.toString()))
        .andExpect(jsonPath("$.data.createdAt").isString());

    verify(this.mqttCredentialService, times(1))
        .get(eq(credential.getId()), eq(regularUser.getId()));
  }

  @Test
  public void testGet_noSuchKey() throws Exception {
    final var regularUser = ControllerTestUtils.getRegularUser();
    final var credential = sampleMqttCredential(regularUser.getId());

    when(this.mqttCredentialService.get(Mockito.eq(credential.getId()), eq(regularUser.getId())))
        .thenThrow(new MqttCredentialNotFoundException(""));

    final var request =
        MockMvcRequestBuilders.get("/v1/account/mqtt-credentials/" + credential.getId())
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(21001))
        .andExpect(jsonPath("$.message").value("Given mqtt-credential resource not found."));
  }

  private static MqttCredentialRequestDto sampleMqttCredentialRequestDto() {
    final var credential = new MqttCredentialRequestDto();

    credential.setDescription(MQTT_CREDENTIAL_SAMPLE_DESCRIPTION);

    return credential;
  }

  private static MqttCredential sampleMqttCredential(String userId) {
    final var credential = new MqttCredential();

    credential.setId(UUID.randomUUID().toString());
    credential.setDescription(MQTT_CREDENTIAL_SAMPLE_DESCRIPTION);
    credential.setUsername("mqtt-user");
    credential.setPassword("mqtt-password");
    credential.setRoles(READ_ONLY_ROLES);
    credential.setUserId(userId);
    credential.setCreatedAt(new Date());

    return credential;
  }
}
