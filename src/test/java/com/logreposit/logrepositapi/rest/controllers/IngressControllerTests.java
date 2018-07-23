package com.logreposit.logrepositapi.rest.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.dtos.DeviceType;

import com.logreposit.logrepositapi.rest.dtos.request.IngressRequestDto;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.ingress.IngressService;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private MockMvc controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws DeviceTokenNotFoundException, DeviceNotFoundException
    {
        ControllerTestUtils.prepareDefaultDevice(this.deviceService);
    }

    @Test
    public void testIngress_unauthenticated() throws Exception
    {
        Object            sampleData        = getSampleData();
        IngressRequestDto ingressRequestDto = new IngressRequestDto();

        ingressRequestDto.setDeviceType(DeviceType.TECHNISCHE_ALTERNATIVE_CMI);
        ingressRequestDto.setData(sampleData);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/ingress")
                                                                      .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                      .content(this.objectMapper.writeValueAsString(ingressRequestDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isUnauthorized())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(70003))
                       .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    @Test
    public void testIngress()
    {

    }

    @Test
    public void testIngress_invalidToken()
    {
    }

    private static Object getSampleData()
    {
        Map<String, Object> data = new HashMap<>();

        data.put("date", new Date());

        return data;
    }
}
