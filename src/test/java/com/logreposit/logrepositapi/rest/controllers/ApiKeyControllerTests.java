package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {ApiKeyController.class})
public class ApiKeyControllerTests
{
    @MockBean
    private ApiKeyService apiKeyService;

    @MockBean
    private UserService userService;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private MockMvc controller;

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
