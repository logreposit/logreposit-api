package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {AccountController.class})
public class AccountControllerTests
{
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
    public void testGet() throws Exception
    {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/account")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        User regularUser = ControllerTestUtils.getRegularUser();

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.email").value(regularUser.getEmail()));
    }
}
