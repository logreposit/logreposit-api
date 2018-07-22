package com.logreposit.logrepositapi.rest.controllers;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.controllers.admin.UserManagementController;
import com.logreposit.logrepositapi.rest.dtos.request.UserCreationRequestDto;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserManagementController.class)
public class UserManagementControllerTests
{
    private static final String ADMIN_API_KEY = "ef05b0b1-89d0-446f-bfb2-81974143dc8a";

    @MockBean
    private UserService userService;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private MockMvc controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws UserNotFoundException, ApiKeyNotFoundException
    {
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setEmail("admin@localhost");
        adminUser.setRoles(Collections.singletonList(UserRoles.ADMIN));

        Mockito.when(this.userService.getByApiKey(Mockito.eq(ADMIN_API_KEY))).thenReturn(adminUser);
    }

    @Test
    public void testCreate() throws Exception
    {
        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(UUID.randomUUID().toString() + "@localhost");

        String userCreationRequestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ADMIN_API_KEY)
                                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                      .content(userCreationRequestDtoSerialized);

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenAnswer(i -> {
            User firstArgument = (User) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });


        this.controller.perform(request)
                       .andExpect(status().isCreated());
    }
}
