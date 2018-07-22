package com.logreposit.logrepositapi.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

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
    public void testCreate()
    {
        
    }
}
