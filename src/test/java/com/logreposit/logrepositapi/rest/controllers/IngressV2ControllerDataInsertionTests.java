package com.logreposit.logrepositapi.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FloatFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.IngressV2RequestDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.IntegerFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.StringFieldDto;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.ingress.IngressService;
import com.logreposit.logrepositapi.services.user.UserService;
import com.logreposit.logrepositapi.utils.definition.DefinitionValidationException;
import com.logreposit.logrepositapi.utils.duration.DurationCalculator;
import com.logreposit.logrepositapi.utils.duration.DurationCalculatorException;
import org.junit.Before;
import org.junit.Ignore;
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

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {IngressV2Controller.class})
public class IngressV2ControllerDataInsertionTests
{
    @MockBean
    private DeviceService deviceService;

    @MockBean
    private DurationCalculator durationCalculator;

    @MockBean
    private UserService userService;

    @MockBean
    private IngressService ingressService;

    @Autowired
    private MockMvc controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<List<ReadingDto>> readingsArgumentCaptor;

    private final Pattern successfulInsertPattern = Pattern.compile("^Data was accepted for processing in [0-9]+ milliseconds\\.$");

    @Before
    public void setUp() throws DeviceTokenNotFoundException, DeviceNotFoundException, DurationCalculatorException
    {
        ControllerTestUtils.prepareDefaultDevice(this.deviceService);

        Mockito.when(this.durationCalculator.getDuration(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(0L);
    }

    @Test
    public void testDefinitionUpdate_givenUnauthenticatedRequest_expectError() throws Exception
    {
        IngressV2RequestDto ingressDto = new IngressV2RequestDto();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

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
    public void testIngressData_withEmptyReadings_shouldSucceed() throws Exception
    {
        IngressV2RequestDto ingressDto = new IngressV2RequestDto();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isAccepted())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.message").value(matchesPattern(this.successfulInsertPattern)));

        Mockito.verify(this.ingressService, Mockito.times(1)).processData(Mockito.eq(ControllerTestUtils.sampleDevice()), this.readingsArgumentCaptor.capture());

        List<ReadingDto> capturedReadingDtos = this.readingsArgumentCaptor.getValue();

        assertThat(capturedReadingDtos).isNotNull();
        assertThat(capturedReadingDtos).isEqualTo(ingressDto.getReadings());
    }

    @Test
    public void testIngressData_withReadingDtoMissingDate_expectError() throws Exception
    {
        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).setDate(null);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].date -> must not be null (actual value: null) => Please check your input."));
    }

    @Test
    public void testIngressData_withReadingDtoInvalidDate_expectError() throws Exception
    {
        String ingressJsonWithInvalidDate = "{\"readings\":[{\"date\":\"2020 Jul 08 13:46:00\",\"measurement\":\"data\",\"tags\":{\"sensor_id\":\"0x003A02\",\"location\":\"operation_room_32\"},\"fields\":[{\"name\":\"humidity\",\"datatype\":\"INTEGER\",\"value\":62}]}]}";

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressJsonWithInvalidDate));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80004))
                       .andExpect(jsonPath("$.message").value("Request could not be processed. Please check if the JSON syntax is valid."));
    }

    // TODO: all dates should be valid... 2020-01-01T10:12:13Z, 2020-01-01T10:12:13+00:00, ....
    @Ignore(value = "TODO: FIX IT! Maybe with a custom deserializer.. Should handle all common date formats...")
    @Test // TODO: fix ...     // testIngressData_withISO8601DateInJson_expectParsedCorrectly
    public void testIngressData_withReadingDtoIso8601DateWithoutMilliseconds_shouldSucceed() throws Exception
    {
        String ingressJsonWithInvalidDate = "{\"readings\":[{\"date\":\"2020-08-05T13:22:25+00:00\",\"measurement\":\"data\",\"tags\":{\"sensor_id\":\"0x003A02\",\"location\":\"operation_room_32\"},\"fields\":[{\"name\":\"humidity\",\"datatype\":\"INTEGER\",\"value\":62}]}]}";

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressJsonWithInvalidDate));

        // TODO ...

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isAccepted())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       // TODO: Assertions and so on...
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80004))
                       .andExpect(jsonPath("$.message").value("Request could not be processed. Please check if the JSON syntax is valid."));

        // TODO ...
    }

    @Test
    public void testIngressData_withReadingDtoMissingMeasurementName_expectError() throws Exception
    {
        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).setMeasurement(null);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].measurement -> must not be blank (actual value: null) => Please check your input."));
    }

    @Test
    public void testIngressData_withReadingDtoContainingInvalidTag_expectError() throws Exception {
        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).getTags().put("time", "some_value");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].tags[time] -> must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\" (actual value: some_value) => Please check your input."));
    }

    @Test
    public void testIngressData_withReadingDtoMissingFields_expectError() throws Exception {
        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).setFields(Collections.emptyList());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].fields -> must not be empty (actual value: []) => Please check your input."));
    }

    @Test
    public void testIngressData_withInvalidFieldName_expectError() throws Exception {
        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).getFields().get(0).setName("_invalid");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].fields[0].name -> must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\" (actual value: _invalid) => Please check your input."));
    }

    @Test
    public void testIngressData_withMissingFieldDatatype_expectError() throws Exception {
        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).getFields().get(0).setDatatype(null);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80004))
                       .andExpect(jsonPath("$.message").value("Request could not be processed. Please check if the JSON syntax is valid."));
    }

    @Test
    public void testIngressData_withBlankStringFieldValue_expectError() throws Exception {
        StringFieldDto stringFieldDto = new StringFieldDto();

        stringFieldDto.setName("field_name");
        stringFieldDto.setValue("");

        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).setFields(Collections.singletonList(stringFieldDto));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].fields[0].value -> must not be blank (actual value: ) => Please check your input."));
    }

    @Test
    public void testIngressData_withMissingStringFieldValue_expectError() throws Exception {
        StringFieldDto stringFieldDto = new StringFieldDto();

        stringFieldDto.setName("field_name");
        stringFieldDto.setValue(null);

        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).setFields(Collections.singletonList(stringFieldDto));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].fields[0].value -> must not be blank (actual value: null) => Please check your input."));
    }

    @Test
    public void testIngressData_withMissingIntegerFieldValue_expectError() throws Exception {
        IntegerFieldDto integerFieldDto = new IntegerFieldDto();

        integerFieldDto.setName("field_name");
        integerFieldDto.setValue(null);

        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).setFields(Collections.singletonList(integerFieldDto));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].fields[0].value -> must not be null (actual value: null) => Please check your input."));
    }

    @Test
    public void testIngressData_withMissingFloatFieldValue_expectError() throws Exception {
        FloatFieldDto floatFieldDto = new FloatFieldDto();

        floatFieldDto.setName("field_name");
        floatFieldDto.setValue(null);

        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).setFields(Collections.singletonList(floatFieldDto));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: readings[0].fields[0].value -> must not be null (actual value: null) => Please check your input."));
    }

    @Test
    public void testIngressData_withIncorrectFieldValue_expectError() throws Exception {
        StringFieldDto stringFieldDto = new StringFieldDto();

        stringFieldDto.setDatatype(DataType.FLOAT);
        stringFieldDto.setName("field_name");
        stringFieldDto.setValue("some_string_value");

        IngressV2RequestDto ingressDto = sampleIngressDto();

        ingressDto.getReadings().get(0).setFields(Collections.singletonList(stringFieldDto));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80004))
                       .andExpect(jsonPath("$.message").value("Request could not be processed. Please check if the JSON syntax is valid."));
    }

    @Test
    public void testIngressData_ingressServiceThrowsDefinitionValidationException_expectError() throws Exception {
        IngressV2RequestDto ingressDto = sampleIngressDto();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/v2/ingress/data")
                                                                      .header(LogrepositWebMvcConfiguration.DEVICE_TOKEN_HEADER_NAME, ControllerTestUtils.VALID_DEVICE_TOKEN)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(this.objectMapper.writeValueAsString(ingressDto));

        Mockito.doThrow(new DefinitionValidationException("custom error message")).when(this.ingressService).processData(Mockito.any(), Mockito.any());

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(51002))
                       .andExpect(jsonPath("$.message").value("custom error message"));
    }

    private static IngressV2RequestDto sampleIngressDto() {

        IntegerFieldDto integerFieldDto = new IntegerFieldDto();

        integerFieldDto.setName("humidity");
        integerFieldDto.setValue(62);

        Map<String, String> tags = new HashMap<>();

        tags.put("location", "operation_room_32");
        tags.put("sensor_id", "0x003A02");

        ReadingDto readingDto = new ReadingDto();

        readingDto.setMeasurement("data");
        readingDto.setDate(Instant.now());
        readingDto.setTags(tags);
        readingDto.setFields(Collections.singletonList(integerFieldDto));

        IngressV2RequestDto ingressDto = new IngressV2RequestDto();

        ingressDto.setReadings(Collections.singletonList(readingDto));

        return ingressDto;
    }
}
