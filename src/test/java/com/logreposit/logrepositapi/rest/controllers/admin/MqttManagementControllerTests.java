package com.logreposit.logrepositapi.rest.controllers.admin;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.controllers.ControllerTestUtils;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@WebMvcTest(controllers = {MqttManagementController.class})
public class MqttManagementControllerTests {
  private static final MediaType EXPECTED_CONTENT_TYPE = MediaType.APPLICATION_JSON;

  @Autowired private MockMvc controller;

  @MockBean private UserService userService;

  @MockBean private DeviceService deviceService;

  @MockBean private MqttCredentialService mqttCredentialService;

  @BeforeEach
  public void setUp() throws UserNotFoundException, ApiKeyNotFoundException {
    ControllerTestUtils.prepareDefaultUsers(this.userService);
  }

  @Test
  public void testSync() throws Exception {
    final var request =
        MockMvcRequestBuilders.post("/v1/admin/mqtt-credentials/actions/sync-all")
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.ADMIN_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"));

    verify(mqttCredentialService).syncAll();
  }
}
