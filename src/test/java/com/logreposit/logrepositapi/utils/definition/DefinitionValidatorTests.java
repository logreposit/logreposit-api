package com.logreposit.logrepositapi.utils.definition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FloatFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.IntegerFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.StringFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.TagDto;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class DefinitionValidatorTests {
  @Test
  public void testValidate_givenNoErrors_expectSucceeds() {
    final var deviceDefinition = sampleDeviceDefinition();
    final var definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
    final var readingDto = sampleReadingDto();

    definitionValidator.validate(Collections.singletonList(readingDto));
  }

  @Test
  public void testValidate_givenDefinitionNull_expectError() {
    final var definitionValidator = DefinitionValidator.forDefinition(null);
    final var readingDto = sampleReadingDto();

    final var e =
        assertThrows(
            DefinitionValidationException.class,
            () -> definitionValidator.validate(Collections.singletonList(readingDto)));

    assertThat(e)
        .hasMessage("Device definition has not been set yet. Cannot perform definition check.");
  }

  @Test
  public void testValidate_givenDefinitionEmpty_expectError() {
    final var definitionValidator = DefinitionValidator.forDefinition(new DeviceDefinition());

    final var readingDto = sampleReadingDto();

    final var e =
        assertThrows(
            DefinitionValidationException.class,
            () -> definitionValidator.validate(Collections.singletonList(readingDto)));

    assertThat(e)
        .hasMessage("Device definition has not been set yet. Cannot perform definition check.");
  }

  @Test
  public void testValidate_givenMeasurementDoesNotExist_expectError() {
    final var deviceDefinition = sampleDeviceDefinition();
    final var definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
    final var readingDto = sampleReadingDto();

    readingDto.setMeasurement("invalid");

    final var e =
        assertThrows(
            DefinitionValidationException.class,
            () -> definitionValidator.validate(Collections.singletonList(readingDto)));

    assertThat(e)
        .hasMessage("Measurement with name 'invalid' does not exist for the given device.");
  }

  @Test
  public void testValidate_givenFieldDoesNotExist_expectError() {
    final var deviceDefinition = sampleDeviceDefinition();
    final var definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
    final var readingDto = sampleReadingDto();

    readingDto.getFields().iterator().next().setName("invalid");

    final var e =
        assertThrows(
            DefinitionValidationException.class,
            () -> definitionValidator.validate(Collections.singletonList(readingDto)));

    assertThat(e)
        .hasMessage(
            "Field with name 'invalid' does not exist within Measurement with name 'data' for the given device.");
  }

  @Test
  public void testValidate_givenFieldHasIncorrectDatatype_expectError() {
    final var deviceDefinition = sampleDeviceDefinition();
    final var definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
    final var readingDto = sampleReadingDto();

    final var floatFieldDto = new FloatFieldDto();

    floatFieldDto.setName("humidity");
    floatFieldDto.setValue(44.12);

    readingDto.setFields(Collections.singletonList(floatFieldDto));

    final var e =
        assertThrows(
            DefinitionValidationException.class,
            () -> definitionValidator.validate(Collections.singletonList(readingDto)));

    assertThat(e)
        .hasMessage(
            "Field with name 'humidity' within Measurement with name 'data' does not have the correct dataType defined. Given: FLOAT / Expected: INTEGER");
  }

  @Test
  public void testValidate_givenInvalidTags_expectError() {
    final var deviceDefinition = sampleDeviceDefinition();
    final var definitionValidator = DefinitionValidator.forDefinition(deviceDefinition);
    final var readingDto = sampleReadingDto();

    final var deviceNameTag = new TagDto();

    deviceNameTag.setName("device_name");
    deviceNameTag.setValue("zxatnkt-003");

    final var networkTag = new TagDto();

    networkTag.setName("network");
    networkTag.setValue("xzatnkt-n01");

    readingDto.getTags().add(deviceNameTag);
    readingDto.getTags().add(networkTag);

    final var e =
        assertThrows(
            DefinitionValidationException.class,
            () -> definitionValidator.validate(Collections.singletonList(readingDto)));

    assertThat(e)
        .hasMessage(
            "Measurement with name 'data' does not have the following tags defined: [device_name, network]");
  }

  private static ReadingDto sampleReadingDto() {
    final var temperatureField = new FloatFieldDto();

    temperatureField.setName("temperature");
    temperatureField.setValue(19.242);

    final var humidityField = new IntegerFieldDto();

    humidityField.setName("humidity");
    humidityField.setValue(48L);

    final var stateField = new StringFieldDto();

    stateField.setName("state");
    stateField.setValue("HUMIDITY_DECREASE");

    final var locationTag = new TagDto();

    locationTag.setName("location");
    locationTag.setValue("b210_320");

    final var sensorIdTag = new TagDto();

    sensorIdTag.setName("sensor_id");
    sensorIdTag.setValue("0x005C1");

    final var readingDto = new ReadingDto();

    readingDto.setMeasurement("data");
    readingDto.setDate(Instant.now());
    readingDto.getTags().addAll(List.of(locationTag, sensorIdTag));
    readingDto.getFields().addAll(List.of(temperatureField, humidityField, stateField));

    return readingDto;
  }

  private static DeviceDefinition sampleDeviceDefinition() {
    final var temperatureFieldDefinition = new FieldDefinition();

    temperatureFieldDefinition.setName("temperature");
    temperatureFieldDefinition.setDatatype(DataType.FLOAT);
    temperatureFieldDefinition.setDescription("Temperature in degrees celsius");

    final var humidityFieldDefinition = new FieldDefinition();

    humidityFieldDefinition.setName("humidity");
    humidityFieldDefinition.setDatatype(DataType.INTEGER);
    humidityFieldDefinition.setDescription("Humidity in percent");

    final var stateFieldDefinition = new FieldDefinition();

    stateFieldDefinition.setName("state");
    stateFieldDefinition.setDatatype(DataType.STRING);
    stateFieldDefinition.setDescription("Current state");

    final var measurementDefinition = new MeasurementDefinition();

    measurementDefinition.setName("data");
    measurementDefinition.setTags(Set.of("location", "sensor_id"));
    measurementDefinition.setFields(
        Set.of(temperatureFieldDefinition, humidityFieldDefinition, stateFieldDefinition));

    final var deviceDefinition = new DeviceDefinition();

    deviceDefinition.setMeasurements(Collections.singletonList(measurementDefinition));

    return deviceDefinition;
  }
}
