package com.logreposit.logrepositapi.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.dtos.request.DeviceCreationRequestDto;
import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {DeviceController.class})
public class DeviceControllerTests
{
    @MockBean
    private UserService userService;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private MockMvc controller;

    @Captor
    private ArgumentCaptor<Device> deviceArgumentCaptor;

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
        User                     regularUser              = ControllerTestUtils.getRegularUser();
        DeviceCreationRequestDto deviceCreationRequestDto = sampleDeviceCreationRequestDto();
        Device                   device                   = sampleDevice(deviceCreationRequestDto.getName(), regularUser.getId());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/devices")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY)
                                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                      .content(this.objectMapper.writeValueAsString(deviceCreationRequestDto));

        Mockito.when(this.deviceService.create(Mockito.any(Device.class))).thenReturn(device);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isCreated())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.id").value(device.getId()))
                       .andExpect(jsonPath("$.data.name").value(device.getName()));

        Mockito.verify(this.deviceService, Mockito.times(1)).create(this.deviceArgumentCaptor.capture());

        Device capturedDevice = this.deviceArgumentCaptor.getValue();

        Assert.assertNotNull(capturedDevice);
        Assert.assertEquals(deviceCreationRequestDto.getName(), capturedDevice.getName());
        Assert.assertEquals(regularUser.getId(), capturedDevice.getUserId());
    }

    @Test
    public void testList() throws Exception
    {
        int defaultPageNumber = 0;
        int defaultPageSize   = 10;

        User regularUser = ControllerTestUtils.getRegularUser();

        Device device1 = sampleDevice(UUID.randomUUID().toString(), regularUser.getId());
        Device device2 = sampleDevice(UUID.randomUUID().toString(), regularUser.getId());

        List<Device> devices    = Arrays.asList(device1, device2);
        Page<Device> devicePage = new PageImpl<>(devices);

        Mockito.when(this.deviceService.list(Mockito.eq(regularUser.getId()), Mockito.anyInt(), Mockito.anyInt())).thenReturn(devicePage);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/devices")
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
                       .andExpect(jsonPath("$.data.items[0].id").value(device1.getId()))
                       .andExpect(jsonPath("$.data.items[0].name").value(device1.getName()))
                       .andExpect(jsonPath("$.data.items[1].id").value(device2.getId()))
                       .andExpect(jsonPath("$.data.items[1].name").value(device2.getName()));

        ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeArgumentCaptor   = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(this.deviceService, Mockito.times(1)).list(Mockito.eq(regularUser.getId()), pageNumberArgumentCaptor.capture(), pageSizeArgumentCaptor.capture());

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
        int pageNumber = 1;
        int pageSize   = 8;

        User regularUser = ControllerTestUtils.getRegularUser();

        Device device1 = sampleDevice(UUID.randomUUID().toString(), regularUser.getId());
        Device device2 = sampleDevice(UUID.randomUUID().toString(), regularUser.getId());

        List<Device> devices    = Arrays.asList(device1, device2);
        Page<Device> devicePage = new PageImpl<>(devices);

        Mockito.when(this.deviceService.list(Mockito.eq(regularUser.getId()), Mockito.anyInt(), Mockito.anyInt())).thenReturn(devicePage);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/devices?page=" + pageNumber + "&size=" + pageSize)
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
                       .andExpect(jsonPath("$.data.items[0].id").value(device1.getId()))
                       .andExpect(jsonPath("$.data.items[0].name").value(device1.getName()))
                       .andExpect(jsonPath("$.data.items[1].id").value(device2.getId()))
                       .andExpect(jsonPath("$.data.items[1].name").value(device2.getName()));

        ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeArgumentCaptor   = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(this.deviceService, Mockito.times(1)).list(Mockito.eq(regularUser.getId()), pageNumberArgumentCaptor.capture(), pageSizeArgumentCaptor.capture());

        Integer capturedPageNumber = pageNumberArgumentCaptor.getValue();
        Integer capturedPageSize   = pageSizeArgumentCaptor.getValue();

        Assert.assertNotNull(capturedPageNumber);
        Assert.assertNotNull(capturedPageSize);
        Assert.assertEquals(pageNumber, capturedPageNumber.intValue());
        Assert.assertEquals(pageSize, capturedPageSize.intValue());
    }

    @Test
    public void testList_customPaginationSettings_exceedsLimits() throws Exception
    {
        int pageNumber = -1;
        int pageSize   = 40;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/devices?page=" + pageNumber + "&size=" + pageSize)
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80016))
                       .andExpect(jsonPath("$.message").value(containsString("list.size: size must be less or equal than 25")))
                       .andExpect(jsonPath("$.message").value(containsString("list.page: page must be greater than or equal to 0")));
    }

    @Test
    public void testGet() throws Exception
    {
        User   regularUser = ControllerTestUtils.getRegularUser();
        Device device      = sampleDevice(UUID.randomUUID().toString(), regularUser.getId());

        Mockito.when(this.deviceService.get(Mockito.eq(device.getId()), Mockito.eq(regularUser.getId()))).thenReturn(device);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/devices/" + device.getId())
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.id").value(device.getId()))
                       .andExpect(jsonPath("$.data.name").value(device.getName()));

        Mockito.verify(this.deviceService, Mockito.times(1)).get(Mockito.eq(device.getId()), Mockito.eq(regularUser.getId()));
    }

    @Test
    public void testGet_noSuchKey() throws Exception
    {
        User   regularUser = ControllerTestUtils.getRegularUser();
        Device device      = sampleDevice(UUID.randomUUID().toString(), regularUser.getId());

        Mockito.when(this.deviceService.get(Mockito.eq(device.getId()), Mockito.eq(regularUser.getId()))).thenThrow(new DeviceNotFoundException(""));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/devices/" + device.getId())
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isNotFound())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(30001))
                       .andExpect(jsonPath("$.message").value("Given device resource not found."));
    }

    @Test
    public void testDelete() throws Exception
    {
        User   regularUser = ControllerTestUtils.getRegularUser();
        Device device      = sampleDevice(UUID.randomUUID().toString(), regularUser.getId());

        Mockito.when(this.deviceService.delete(Mockito.eq(device.getId()), Mockito.eq(regularUser.getId()))).thenReturn(device);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/devices/" + device.getId())
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.id").value(device.getId()))
                       .andExpect(jsonPath("$.data.name").value(device.getName()));

        Mockito.verify(this.deviceService, Mockito.times(1)).delete(Mockito.eq(device.getId()), Mockito.eq(regularUser.getId()));
    }

    @Test
    public void testDelete_noSuchKey() throws Exception
    {
        User   regularUser = ControllerTestUtils.getRegularUser();
        Device device      = sampleDevice(UUID.randomUUID().toString(), regularUser.getId());

        Mockito.when(this.deviceService.delete(Mockito.eq(device.getId()), Mockito.eq(regularUser.getId()))).thenThrow(new DeviceNotFoundException(""));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/devices/" + device.getId())
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isNotFound())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(30001))
                       .andExpect(jsonPath("$.message").value("Given device resource not found."));
    }

    private static DeviceCreationRequestDto sampleDeviceCreationRequestDto()
    {
        DeviceCreationRequestDto device = new DeviceCreationRequestDto();
        device.setName(UUID.randomUUID().toString());

        return device;
    }

    private static Device sampleDevice(String name, String userId)
    {
        Device device = new Device();

        device.setId(UUID.randomUUID().toString());
        device.setUserId(userId);
        device.setName(name);

        return device;
    }
}
