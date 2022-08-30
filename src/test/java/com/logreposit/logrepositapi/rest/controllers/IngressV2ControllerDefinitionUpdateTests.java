package com.logreposit.logrepositapi.rest.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.DeviceDefinitionDto;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.FieldDefinitionDto;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.MeasurementDefinitionDto;
import com.logreposit.logrepositapi.rest.mappers.DeviceDefinitionMapper;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.ingress.IngressService;
import com.logreposit.logrepositapi.services.user.UserService;
import com.logreposit.logrepositapi.utils.definition.DefinitionUpdateValidationException;
import com.logreposit.logrepositapi.utils.duration.DurationCalculator;
import com.logreposit.logrepositapi.utils.duration.DurationCalculatorException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {IngressV2Controller.class})
public class IngressV2ControllerDefinitionUpdateTests {
  private static final MediaType EXPECTED_CONTENT_TYPE = MediaType.APPLICATION_JSON;

  @MockBean private DeviceService deviceService;

  @MockBean private DurationCalculator durationCalculator;

  @MockBean private UserService userService;

  @MockBean private IngressService ingressService;

  @Autowired private MockMvc controller;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp()
      throws DeviceTokenNotFoundException, DeviceNotFoundException, DurationCalculatorException {
    ControllerTestUtils.prepareDefaultDevice(this.deviceService);

    Mockito.when(
            this.durationCalculator.getDuration(Mockito.any(Date.class), Mockito.any(Date.class)))
        .thenReturn(0L);
  }

  @Test
  public void testDefinitionUpdate_givenUnauthenticatedRequest_expectError() throws Exception {
    DeviceDefinitionDto sampleDeviceDefinitionDto = getSampleDeviceDefinitionDto();

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(70003))
        .andExpect(jsonPath("$.message").value("Unauthenticated"));
  }

  @Test
  public void testDefinitionUpdate_givenValidDefinition_expectDeviceServiceCallAndSuccess()
      throws Exception {
    DeviceDefinitionDto sampleDeviceDefinitionDto = getSampleDeviceDefinitionDto();
    Device sampleDevice = ControllerTestUtils.sampleDevice();

    Mockito.when(
            this.deviceService.updateDefinition(
                Mockito.eq(sampleDevice.getId()), Mockito.any(DeviceDefinition.class)))
        .thenReturn(DeviceDefinitionMapper.toEntity(sampleDeviceDefinitionDto));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .header(
                LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME,
                ControllerTestUtils.VALID_DEVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.measurements").isArray())
        .andExpect(jsonPath("$.data.measurements.length()").value(1))
        .andExpect(jsonPath("$.data.measurements[0].name").value("data"))
        .andExpect(jsonPath("$.data.measurements[0].tags.length()").value(2))
        .andExpect(
            jsonPath("$.data.measurements[0].tags")
                .value(containsInAnyOrder("location", "sensor_id")))
        .andExpect(jsonPath("$.data.measurements[0].fields.length()").value(1))
        .andExpect(jsonPath("$.data.measurements[0].fields[0].name").value("temperature"))
        .andExpect(
            jsonPath("$.data.measurements[0].fields[0].description")
                .value("Temperature in degrees celsius"))
        .andExpect(jsonPath("$.data.measurements[0].fields[0].datatype").value("FLOAT"));

    ArgumentCaptor<String> deviceIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<DeviceDefinition> deviceDefinitionArgumentCaptor =
        ArgumentCaptor.forClass(DeviceDefinition.class);

    Mockito.verify(this.deviceService, Mockito.times(1))
        .updateDefinition(
            deviceIdArgumentCaptor.capture(), deviceDefinitionArgumentCaptor.capture());

    String capturedDeviceId = deviceIdArgumentCaptor.getValue();
    DeviceDefinition capturedDeviceDefinition = deviceDefinitionArgumentCaptor.getValue();

    assertThat(capturedDeviceId).isNotNull();
    assertThat(capturedDeviceDefinition).isNotNull();

    assertThat(capturedDeviceId).isEqualTo(sampleDevice.getId());
    assertThat(DeviceDefinitionMapper.toDto(capturedDeviceDefinition))
        .isEqualTo(sampleDeviceDefinitionDto);
  }

  @Test
  public void testDefinitionUpdate_givenInvalidDefinitionWithoutMeasurements_expectError()
      throws Exception {
    DeviceDefinitionDto sampleDeviceDefinitionDto = new DeviceDefinitionDto();

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .header(
                LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME,
                ControllerTestUtils.VALID_DEVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(80005))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Invalid input data. Field Errors: measurements -> must not be empty (actual value: []) => Please check your input."));
  }

  @Test
  public void testDefinitionUpdate_givenInvalidDefinitionWithoutFields_expectError()
      throws Exception {
    MeasurementDefinitionDto measurementDefinitionDto = new MeasurementDefinitionDto();

    measurementDefinitionDto.setName("my_measurement_name");

    DeviceDefinitionDto sampleDeviceDefinitionDto = new DeviceDefinitionDto();

    sampleDeviceDefinitionDto.setMeasurements(Collections.singletonList(measurementDefinitionDto));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .header(
                LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME,
                ControllerTestUtils.VALID_DEVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(80005))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Invalid input data. Field Errors: measurements[0].fields -> must not be empty (actual value: []) => Please check your input."));
  }

  @Test
  public void testDefinitionUpdate_givenDefinitionWithInvalidMeasurementName_expectError()
      throws Exception {
    FieldDefinitionDto fieldDefinitionDto = new FieldDefinitionDto();

    fieldDefinitionDto.setName("temperature");
    fieldDefinitionDto.setDatatype(DataType.FLOAT);

    MeasurementDefinitionDto measurementDefinitionDto = new MeasurementDefinitionDto();

    measurementDefinitionDto.setName("time");
    measurementDefinitionDto.setFields(Collections.singletonList(fieldDefinitionDto));

    DeviceDefinitionDto sampleDeviceDefinitionDto = new DeviceDefinitionDto();

    sampleDeviceDefinitionDto.setMeasurements(Collections.singletonList(measurementDefinitionDto));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .header(
                LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME,
                ControllerTestUtils.VALID_DEVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(80005))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Invalid input data. Field Errors: measurements[0].name -> must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\" (actual value: time) => Please check your input."));
  }

  @Test
  public void testDefinitionUpdate_givenDefinitionWithInvalidTagName_expectError()
      throws Exception {
    FieldDefinitionDto fieldDefinitionDto = new FieldDefinitionDto();

    fieldDefinitionDto.setName("temperature");
    fieldDefinitionDto.setDatatype(DataType.FLOAT);

    MeasurementDefinitionDto measurementDefinitionDto = new MeasurementDefinitionDto();

    measurementDefinitionDto.setName("data");
    measurementDefinitionDto.setTags(Set.of("time"));
    measurementDefinitionDto.setFields(Collections.singletonList(fieldDefinitionDto));

    DeviceDefinitionDto sampleDeviceDefinitionDto = new DeviceDefinitionDto();

    sampleDeviceDefinitionDto.setMeasurements(Collections.singletonList(measurementDefinitionDto));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .header(
                LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME,
                ControllerTestUtils.VALID_DEVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(80005))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Invalid input data. Field Errors: measurements[0].tags[] -> must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\" (actual value: time) => Please check your input."));
  }

  @Test
  public void testDefinitionUpdate_givenDefinitionWithInvalidFieldName_expectError()
      throws Exception {
    FieldDefinitionDto fieldDefinitionDto = new FieldDefinitionDto();

    fieldDefinitionDto.setName("time");
    fieldDefinitionDto.setDatatype(DataType.FLOAT);

    MeasurementDefinitionDto measurementDefinitionDto = new MeasurementDefinitionDto();

    measurementDefinitionDto.setName("data");
    measurementDefinitionDto.setFields(Collections.singletonList(fieldDefinitionDto));

    DeviceDefinitionDto sampleDeviceDefinitionDto = new DeviceDefinitionDto();

    sampleDeviceDefinitionDto.setMeasurements(Collections.singletonList(measurementDefinitionDto));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .header(
                LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME,
                ControllerTestUtils.VALID_DEVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(80005))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Invalid input data. Field Errors: measurements[0].fields[0].name -> must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\" (actual value: time) => Please check your input."));
  }

  @Test
  public void testDefinitionUpdate_givenDefinitionWithoutFieldDatatype_expectError()
      throws Exception {
    FieldDefinitionDto fieldDefinitionDto = new FieldDefinitionDto();

    fieldDefinitionDto.setName("temperature");
    fieldDefinitionDto.setDatatype(null);

    MeasurementDefinitionDto measurementDefinitionDto = new MeasurementDefinitionDto();

    measurementDefinitionDto.setName("data");
    measurementDefinitionDto.setFields(Collections.singletonList(fieldDefinitionDto));

    DeviceDefinitionDto sampleDeviceDefinitionDto = new DeviceDefinitionDto();

    sampleDeviceDefinitionDto.setMeasurements(Collections.singletonList(measurementDefinitionDto));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .header(
                LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME,
                ControllerTestUtils.VALID_DEVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(80005))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Invalid input data. Field Errors: measurements[0].fields[0].datatype -> must not be null (actual value: null) => Please check your input."));
  }

  @Test
  public void
      testDefinitionUpdate_givenDeviceServiceThrowsDefinitionUpdateValidationException_expectError()
          throws Exception {
    FieldDefinitionDto fieldDefinitionDto = new FieldDefinitionDto();

    fieldDefinitionDto.setName("temperature");
    fieldDefinitionDto.setDatatype(DataType.FLOAT);

    MeasurementDefinitionDto measurementDefinitionDto = new MeasurementDefinitionDto();

    measurementDefinitionDto.setName("data");
    measurementDefinitionDto.setFields(Collections.singletonList(fieldDefinitionDto));

    DeviceDefinitionDto sampleDeviceDefinitionDto = new DeviceDefinitionDto();

    sampleDeviceDefinitionDto.setMeasurements(Collections.singletonList(measurementDefinitionDto));

    Mockito.when(
            this.deviceService.updateDefinition(
                Mockito.eq(ControllerTestUtils.sampleDevice().getId()),
                Mockito.any(DeviceDefinition.class)))
        .thenThrow(new DefinitionUpdateValidationException("exception error message"));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.put("/v2/ingress/definition")
            .header(
                LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME,
                ControllerTestUtils.VALID_DEVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(sampleDeviceDefinitionDto));

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(51001))
        .andExpect(jsonPath("$.message").value("exception error message"));
  }

  private static DeviceDefinitionDto getSampleDeviceDefinitionDto() {
    FieldDefinitionDto fieldDefinitionDto = new FieldDefinitionDto();

    fieldDefinitionDto.setName("temperature");
    fieldDefinitionDto.setDatatype(DataType.FLOAT);
    fieldDefinitionDto.setDescription("Temperature in degrees celsius");

    MeasurementDefinitionDto measurementDefinitionDto = new MeasurementDefinitionDto();

    measurementDefinitionDto.setName("data");
    measurementDefinitionDto.setTags(Set.of("sensor_id", "location"));
    measurementDefinitionDto.setFields(Collections.singletonList(fieldDefinitionDto));

    DeviceDefinitionDto deviceDefinitionDto = new DeviceDefinitionDto();

    deviceDefinitionDto.setMeasurements(Collections.singletonList(measurementDefinitionDto));

    return deviceDefinitionDto;
  }
}
