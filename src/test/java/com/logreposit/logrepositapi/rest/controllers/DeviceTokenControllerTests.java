package com.logreposit.logrepositapi.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.devicetoken.DeviceTokenService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    @Test
    public void testList() throws Exception
    {
        String deviceId = UUID.randomUUID().toString();

        int defaultPageNumber = 0;
        int defaultPageSize   = 10;

        User regularUser = ControllerTestUtils.getRegularUser();

        DeviceToken deviceToken1 = sampleDeviceToken(deviceId);
        DeviceToken deviceToken2 = sampleDeviceToken(deviceId);

        List<DeviceToken> deviceTokens    = Arrays.asList(deviceToken1, deviceToken2);
        Page<DeviceToken> deviceTokenPage = new PageImpl<>(deviceTokens);

        Mockito.when(this.deviceTokenService.list(Mockito.eq(deviceId), Mockito.eq(regularUser.getId()), Mockito.anyInt(), Mockito.anyInt())).thenReturn(deviceTokenPage);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/devices/" + deviceId + "/tokens")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.totalElements").value(2))
                       .andExpect(jsonPath("$.data.totalPages").value(1))
                       .andExpect(jsonPath("$.data.items").isArray())
                       .andExpect(jsonPath("$.data.items.length()").value(2))
                       .andExpect(jsonPath("$.data.items[0].id").value(deviceToken1.getId()))
                       .andExpect(jsonPath("$.data.items[0].token").value(deviceToken1.getToken()))
                       .andExpect(jsonPath("$.data.items[0].createdAt").exists())
                       .andExpect(jsonPath("$.data.items[1].id").value(deviceToken2.getId()))
                       .andExpect(jsonPath("$.data.items[1].token").value(deviceToken2.getToken()))
                       .andExpect(jsonPath("$.data.items[1].createdAt").exists());

        ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeArgumentCaptor   = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(this.deviceTokenService, Mockito.times(1)).list(Mockito.eq(deviceId), Mockito.eq(regularUser.getId()), pageNumberArgumentCaptor.capture(), pageSizeArgumentCaptor.capture());

        Integer pageNumber = pageNumberArgumentCaptor.getValue();
        Integer pageSize   = pageSizeArgumentCaptor.getValue();

        Assert.assertNotNull(pageNumber);
        Assert.assertNotNull(pageSize);
        Assert.assertEquals(defaultPageNumber, pageNumber.intValue());
        Assert.assertEquals(defaultPageSize, pageSize.intValue());
    }

    @Test
    public void testList_customPaginationSettings() throws Exception
    {
        String deviceId = UUID.randomUUID().toString();

        int pageNumber = 1;
        int pageSize   = 8;

        User regularUser = ControllerTestUtils.getRegularUser();

        DeviceToken deviceToken1 = sampleDeviceToken(deviceId);
        DeviceToken deviceToken2 = sampleDeviceToken(deviceId);

        List<DeviceToken> deviceTokens    = Arrays.asList(deviceToken1, deviceToken2);
        Page<DeviceToken> deviceTokenPage = new PageImpl<>(deviceTokens);

        Mockito.when(this.deviceTokenService.list(Mockito.eq(deviceId), Mockito.eq(regularUser.getId()), Mockito.anyInt(), Mockito.anyInt())).thenReturn(deviceTokenPage);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/devices/" + deviceId + "/tokens?page=" + pageNumber + "&size=" + pageSize)
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.totalElements").value(2))
                       .andExpect(jsonPath("$.data.totalPages").value(1))
                       .andExpect(jsonPath("$.data.items").isArray())
                       .andExpect(jsonPath("$.data.items.length()").value(2))
                       .andExpect(jsonPath("$.data.items[0].id").value(deviceToken1.getId()))
                       .andExpect(jsonPath("$.data.items[0].token").value(deviceToken1.getToken()))
                       .andExpect(jsonPath("$.data.items[0].createdAt").exists())
                       .andExpect(jsonPath("$.data.items[1].id").value(deviceToken2.getId()))
                       .andExpect(jsonPath("$.data.items[1].token").value(deviceToken2.getToken()))
                       .andExpect(jsonPath("$.data.items[1].createdAt").exists());

        ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeArgumentCaptor   = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(this.deviceTokenService, Mockito.times(1)).list(Mockito.eq(deviceId), Mockito.eq(regularUser.getId()), pageNumberArgumentCaptor.capture(), pageSizeArgumentCaptor.capture());

        Integer capturedPageNumber = pageNumberArgumentCaptor.getValue();
        Integer capturedPageSize   = pageSizeArgumentCaptor.getValue();

        Assert.assertNotNull(capturedPageNumber);
        Assert.assertNotNull(capturedPageSize);
        Assert.assertEquals(pageNumber, capturedPageNumber.intValue());
        Assert.assertEquals(pageSize, capturedPageSize.intValue());
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
