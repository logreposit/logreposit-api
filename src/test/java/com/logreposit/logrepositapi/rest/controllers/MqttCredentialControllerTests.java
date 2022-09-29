package com.logreposit.logrepositapi.rest.controllers;

import static org.mockito.ArgumentMatchers.eq;
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
  private static final List<MqttRole> READ_ONLY_ROLES = List.of(MqttRole.ACCOUNT_DEVICE_DATA_READ);
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

    Mockito.when(
            this.mqttCredentialService.create(
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
        .andExpect(jsonPath("$.data.roles[0]").value("ACCOUNT_DEVICE_DATA_READ"))
        .andExpect(jsonPath("$.data.createdAt").isString());

    Mockito.verify(this.mqttCredentialService, Mockito.times(1))
        .create(eq(REGULAR_USER_ID), eq(MQTT_CREDENTIAL_SAMPLE_DESCRIPTION), eq(READ_ONLY_ROLES));
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
