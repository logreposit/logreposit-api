package com.logreposit.logrepositapi.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.dtos.DeviceType;
import com.logreposit.logrepositapi.rest.dtos.request.IngressRequestDto;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.ingress.IngressService;
import com.logreposit.logrepositapi.services.ingress.UnsupportedDeviceTypeException;
import com.logreposit.logrepositapi.services.user.UserService;
import com.logreposit.logrepositapi.utils.duration.DurationCalculator;
import com.logreposit.logrepositapi.utils.duration.DurationCalculatorException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {IngressController.class})
public class IngressControllerTests
{
    @MockBean
    private DeviceService deviceService;

    @MockBean
    private UserService userService;

    @MockBean
    private IngressService ingressService;

    @MockBean
    private DurationCalculator durationCalculator;

    @Autowired
    private MockMvc controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws DeviceTokenNotFoundException, DeviceNotFoundException, DurationCalculatorException
    {
        ControllerTestUtils.prepareDefaultDevice(this.deviceService);

        Mockito.when(this.durationCalculator.getDuration(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(0L);
    }

    @Test
    public void testIngress_unauthenticated() throws Exception
    {
        Object            sampleData        = getSampleData();
        IngressRequestDto ingressRequestDto = new IngressRequestDto();

        ingressRequestDto.setDeviceType(DeviceType.TECHNISCHE_ALTERNATIVE_CMI);
        ingressRequestDto.setData(sampleData);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v1/ingress")
                                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                      .content(this.objectMapper.writeValueAsString(ingressRequestDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isUnauthorized())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(70003))
                       .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    @Test
    public void testIngress() throws Exception
    {
        Object            sampleData        = getSampleData();
        IngressRequestDto ingressRequestDto = new IngressRequestDto();

        ingressRequestDto.setDeviceType(DeviceType.TECHNISCHE_ALTERNATIVE_CMI);
        ingressRequestDto.setData(sampleData);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v1/ingress")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                      .content(this.objectMapper.writeValueAsString(ingressRequestDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isAccepted())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.message").value("Data was accepted for processing in 0 milliseconds."));

        ArgumentCaptor<Device>     deviceArgumentCaptor     = ArgumentCaptor.forClass(Device.class);
        ArgumentCaptor<DeviceType> deviceTypeArgumentCaptor = ArgumentCaptor.forClass(DeviceType.class);
        ArgumentCaptor<Object>     objectArgumentCaptor     = ArgumentCaptor.forClass(Object.class);

        Mockito.verify(
                this.ingressService,
                Mockito.times(1)).processData(deviceArgumentCaptor.capture(), deviceTypeArgumentCaptor.capture(), objectArgumentCaptor.capture()
        );

        Device     capturedDevice     = deviceArgumentCaptor.getValue();
        DeviceType capturedDeviceType = deviceTypeArgumentCaptor.getValue();
        Object     captuedData        = objectArgumentCaptor.getValue();

        Assert.assertNotNull(capturedDevice);
        Assert.assertNotNull(capturedDeviceType);
        Assert.assertNotNull(captuedData);

        Device sampleDevice = ControllerTestUtils.sampleDevice();

        Assert.assertEquals(capturedDevice.getId(), sampleDevice.getId());
        Assert.assertEquals(capturedDevice.getUserId(), sampleDevice.getUserId());
        Assert.assertEquals(capturedDevice.getName(), sampleDevice.getName());
        Assert.assertEquals(capturedDeviceType, ingressRequestDto.getDeviceType());
        Assert.assertEquals(this.objectMapper.writeValueAsString(captuedData), this.objectMapper.writeValueAsString(ingressRequestDto.getData()));
    }

    @Test
    public void testIngress_invalidToken() throws Exception
    {
        String deviceToken = UUID.randomUUID().toString();

        Object            sampleData        = getSampleData();
        IngressRequestDto ingressRequestDto = new IngressRequestDto();

        ingressRequestDto.setDeviceType(DeviceType.TECHNISCHE_ALTERNATIVE_CMI);
        ingressRequestDto.setData(sampleData);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v1/ingress")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, deviceToken)
                                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                      .content(this.objectMapper.writeValueAsString(ingressRequestDto));

        Mockito.when(this.deviceService.getByDeviceToken(Mockito.eq(deviceToken))).thenThrow(new DeviceTokenNotFoundException("", deviceToken));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isUnauthorized())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(70003))
                       .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    @Test
    public void testIngress_noDeviceForToken() throws Exception
    {
        String deviceToken = UUID.randomUUID().toString();

        Object            sampleData        = getSampleData();
        IngressRequestDto ingressRequestDto = new IngressRequestDto();

        ingressRequestDto.setDeviceType(DeviceType.TECHNISCHE_ALTERNATIVE_CMI);
        ingressRequestDto.setData(sampleData);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v1/ingress")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, deviceToken)
                                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                      .content(this.objectMapper.writeValueAsString(ingressRequestDto));

        Mockito.when(this.deviceService.getByDeviceToken(Mockito.eq(deviceToken))).thenThrow(new DeviceNotFoundException(""));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isUnauthorized())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(70003))
                       .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    @Test
    public void testIngress_invalidDeviceType() throws Exception
    {
        String deviceToken = UUID.randomUUID().toString();

        Object            sampleData        = getSampleData();
        IngressRequestDto ingressRequestDto = new IngressRequestDto();

        ingressRequestDto.setDeviceType(DeviceType.UNKNOWN);
        ingressRequestDto.setData(sampleData);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v1/ingress")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, deviceToken)
                                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                      .content(this.objectMapper.writeValueAsString(ingressRequestDto));

        Mockito.doThrow(new UnsupportedDeviceTypeException(DeviceType.UNKNOWN)).when(this.ingressService).processData(Mockito.any(), Mockito.eq(DeviceType.UNKNOWN), Mockito.any());

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(50002))
                       .andExpect(jsonPath("$.message").value("Error processing data: Unsupported device type: 'UNKNOWN'"));
    }

    private static Object getSampleData()
    {
        Map<String, Object> data = new HashMap<>();

        data.put("date", new Date());

        return data;
    }
}
