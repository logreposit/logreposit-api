package com.logreposit.logrepositapi.utils.definition;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FloatFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.IntegerFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.StringFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.TagDto;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class DefinitionValidatorTests
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testValidate_givenNoErrors_expectSucceeds()
    {
        DeviceDefinition    deviceDefinition    = sampleDeviceDefinition();
        DefinitionValidator definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
        ReadingDto          readingDto          = sampleReadingDto();

        definitionValidator.validate(Collections.singletonList(readingDto));
    }

    @Test
    public void testValidate_givenDefinitionNull_expectError()
    {
        this.expectedException.expect(DefinitionValidationException.class);
        this.expectedException.expectMessage("Device definition has not been set yet. Cannot perform definition check.");

        DefinitionValidator definitionValidator = DefinitionValidator.forDefinition(null);
        ReadingDto          readingDto          = sampleReadingDto();

        definitionValidator.validate(Collections.singletonList(readingDto));
    }

    @Test
    public void testValidate_givenDefinitionEmpty_expectError()
    {
        this.expectedException.expect(DefinitionValidationException.class);
        this.expectedException.expectMessage("Device definition has not been set yet. Cannot perform definition check.");

        DefinitionValidator definitionValidator = DefinitionValidator.forDefinition(new DeviceDefinition());
        ReadingDto          readingDto          = sampleReadingDto();

        definitionValidator.validate(Collections.singletonList(readingDto));
    }

    @Test
    public void testValidate_givenMeasurementDoesNotExist_expectError()
    {
        this.expectedException.expect(DefinitionValidationException.class);
        this.expectedException.expectMessage("Measurement with name 'invalid' does not exist for the given device.");

        DeviceDefinition    deviceDefinition    = sampleDeviceDefinition();
        DefinitionValidator definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
        ReadingDto          readingDto          = sampleReadingDto();

        readingDto.setMeasurement("invalid");

        definitionValidator.validate(Collections.singletonList(readingDto));
    }

    @Test
    public void testValidate_givenFieldDoesNotExist_expectError()
    {
        this.expectedException.expect(DefinitionValidationException.class);
        this.expectedException.expectMessage("Field with name 'invalid' does not exist within Measurement with name 'data' for the given device.");

        DeviceDefinition    deviceDefinition    = sampleDeviceDefinition();
        DefinitionValidator definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
        ReadingDto          readingDto          = sampleReadingDto();

        readingDto.getFields().iterator().next().setName("invalid");

        definitionValidator.validate(Collections.singletonList(readingDto));
    }

    @Test
    public void testValidate_givenFieldHasIncorrectDatatype_expectError()
    {
        this.expectedException.expect(DefinitionValidationException.class);
        this.expectedException.expectMessage("Field with name 'humidity' within Measurement with name 'data' does not have the correct dataType defined. Given: FLOAT / Expected: INTEGER");

        DeviceDefinition    deviceDefinition    = sampleDeviceDefinition();
        DefinitionValidator definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
        ReadingDto          readingDto          = sampleReadingDto();

        FloatFieldDto floatFieldDto = new FloatFieldDto();

        floatFieldDto.setName("humidity");
        floatFieldDto.setValue(44.12);

        readingDto.setFields(Collections.singletonList(floatFieldDto));

        definitionValidator.validate(Collections.singletonList(readingDto));
    }

    @Test
    public void testValidate_givenInvalidTags_expectError()
    {
        this.expectedException.expect(DefinitionValidationException.class);
        this.expectedException.expectMessage("Measurement with name 'data' does not have the following tags defined: [device_name, network]");

        DeviceDefinition    deviceDefinition    = sampleDeviceDefinition();
        DefinitionValidator definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
        ReadingDto          readingDto          = sampleReadingDto();

        TagDto deviceNameTag = new TagDto();

        deviceNameTag.setName("device_name");
        deviceNameTag.setValue("zxatnkt-003");

        TagDto networkTag = new TagDto();

        networkTag.setName("network");
        networkTag.setValue("xzatnkt-n01");

        readingDto.getTags().add(deviceNameTag);
        readingDto.getTags().add(networkTag);

        definitionValidator.validate(Collections.singletonList(readingDto));
    }

    private static ReadingDto sampleReadingDto()
    {
        FloatFieldDto temperatureField = new FloatFieldDto();

        temperatureField.setName("temperature");
        temperatureField.setValue(19.242);

        IntegerFieldDto humidityField = new IntegerFieldDto();

        humidityField.setName("humidity");
        humidityField.setValue(48L);

        StringFieldDto stateField = new StringFieldDto();

        stateField.setName("state");
        stateField.setValue("HUMIDITY_DECREASE");

        List<FieldDto> fields = Arrays.asList(temperatureField, humidityField, stateField);

        TagDto locationTag = new TagDto();

        locationTag.setName("location");
        locationTag.setValue("b210_320");

        TagDto sensorIdTag = new TagDto();

        sensorIdTag.setName("sensor_id");
        sensorIdTag.setValue("0x005C1");

        List<TagDto> tags = Arrays.asList(locationTag, sensorIdTag);

        ReadingDto readingDto = new ReadingDto();

        readingDto.setMeasurement("data");
        readingDto.setDate(Instant.now());

        readingDto.getTags().addAll(tags);
        readingDto.getFields().addAll(fields);

        return readingDto;
    }

    private static DeviceDefinition sampleDeviceDefinition()
    {
        FieldDefinition temperatureFieldDefinition = new FieldDefinition();

        temperatureFieldDefinition.setName("temperature");
        temperatureFieldDefinition.setDatatype(DataType.FLOAT);
        temperatureFieldDefinition.setDescription("Temperature in degrees celsius");

        FieldDefinition humidityFieldDefinition = new FieldDefinition();

        humidityFieldDefinition.setName("humidity");
        humidityFieldDefinition.setDatatype(DataType.INTEGER);
        humidityFieldDefinition.setDescription("Humidity in percent");

        FieldDefinition stateFieldDefinition = new FieldDefinition();

        stateFieldDefinition.setName("state");
        stateFieldDefinition.setDatatype(DataType.STRING);
        stateFieldDefinition.setDescription("Current state");

        MeasurementDefinition measurementDefinition = new MeasurementDefinition();

        measurementDefinition.setName("data");
        measurementDefinition.setTags(new HashSet<>(Arrays.asList("location", "sensor_id")));
        measurementDefinition.setFields(new HashSet<>(Arrays.asList(temperatureFieldDefinition, humidityFieldDefinition, stateFieldDefinition)));

        DeviceDefinition deviceDefinition = new DeviceDefinition();

        deviceDefinition.setMeasurements(Collections.singletonList(measurementDefinition));

        return deviceDefinition;
    }
}
