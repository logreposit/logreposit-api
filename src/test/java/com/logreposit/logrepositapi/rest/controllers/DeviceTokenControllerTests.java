package com.logreposit.logrepositapi.rest.controllers;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.devicetoken.DeviceTokenService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {DeviceTokenController.class})
public class DeviceTokenControllerTests
{
    @MockBean
    private UserService userService;

    @MockBean
    private DeviceService deviceService;

    @MockBean
    private DeviceTokenService deviceTokenService;

    @Autowired
    private MockMvc controller;

    @Captor
    private ArgumentCaptor<DeviceToken> deviceTokenArgumentCaptor;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws UserNotFoundException, ApiKeyNotFoundException
    {
        ControllerTestUtils.prepareDefaultUsers(this.userService);
    }

    @Test
    public void testCreate() throws Exception
    {
        String deviceId = UUID.randomUUID().toString();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/devices/" + deviceId + "/tokens")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        User        regularUser = ControllerTestUtils.getRegularUser();
        DeviceToken deviceToken = sampleDeviceToken(deviceId);

        Mockito.when(this.deviceTokenService.create(Mockito.eq(deviceId), Mockito.eq(regularUser.getId()))).thenReturn(deviceToken);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isCreated())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.id").value(deviceToken.getId()))
                       .andExpect(jsonPath("$.data.token").value(deviceToken.getToken()))
                       .andExpect(jsonPath("$.data.createdAt").exists());

        Mockito.verify(this.deviceTokenService, Mockito.times(1)).create(Mockito.eq(deviceId), Mockito.eq(regularUser.getId()));
    }

    private static DeviceToken sampleDeviceToken(String deviceId)
    {
        DeviceToken deviceToken = new DeviceToken();

        deviceToken.setId(UUID.randomUUID().toString());
        deviceToken.setToken(UUID.randomUUID().toString());
        deviceToken.setDeviceId(deviceId);
        deviceToken.setCreatedAt(new Date());

        return deviceToken;
    }
}
