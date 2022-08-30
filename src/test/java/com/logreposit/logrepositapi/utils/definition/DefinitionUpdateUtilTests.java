package com.logreposit.logrepositapi.utils.definition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class DefinitionUpdateUtilTests {
  @Test
  public void testUpdateDefinition_givenCurrentDefinitionIsNull_expectNewDefinitionIsChosen() {
    final var newDefinition = getSampleDeviceDefinition();
    final var mergedDefinition = DefinitionUpdateUtil.updateDefinition(null, newDefinition);

    assertThat(mergedDefinition).isNotNull();
    assertThat(mergedDefinition).isEqualTo(newDefinition);
  }

  @Test
  public void testUpdateDefinition_givenDuplicateMeasurementNames_expectThrowsException() {
    final var newDefinition = getSampleDeviceDefinition();

    final var anotherMeasurement = new MeasurementDefinition();

    anotherMeasurement.setName(newDefinition.getMeasurements().get(0).getName());
    anotherMeasurement.setTags(Set.of("tag1", "tag2"));
    anotherMeasurement.setFields(Collections.emptySet());

    newDefinition.getMeasurements().add(anotherMeasurement);

    var e =
        assertThrows(
            DefinitionUpdateValidationException.class,
            () -> DefinitionUpdateUtil.updateDefinition(null, newDefinition));

    assertThat(e).hasMessage("Duplicated measurements with the same name are not allowed.");
  }

  @Test
  public void testUpdateDefinition_givenDuplicatedFieldNames_expectThrowsException() {
    final var newDefinition = getSampleDeviceDefinition();

    final var fieldDefinition = new FieldDefinition();

    fieldDefinition.setName(
        newDefinition.getMeasurements().get(0).getFields().iterator().next().getName());
    fieldDefinition.setDatatype(DataType.STRING);
    fieldDefinition.setDescription("some description");

    newDefinition.getMeasurements().get(0).getFields().add(fieldDefinition);

    var e =
        assertThrows(
            DefinitionUpdateValidationException.class,
            () -> DefinitionUpdateUtil.updateDefinition(null, newDefinition));

    assertThat(e)
        .hasMessage(
            "Duplicated fields with the same name inside a single measurement are not allowed.");
  }

  @Test
  public void testUpdateDefinition_givenChangedFieldType_expectThrowsException() {
    final var oldDefinition = getSampleDeviceDefinition();
    final var newDefinition = getSampleDeviceDefinition();

    newDefinition.getMeasurements().get(0).getFields().stream()
        .filter(f -> "temperature".equals(f.getName()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("should not happen"))
        .setDatatype(DataType.STRING);

    newDefinition
        .getMeasurements()
        .get(0)
        .getFields()
        .iterator()
        .next()
        .setDatatype(DataType.STRING);

    var e =
        assertThrows(
            DefinitionUpdateValidationException.class,
            () -> DefinitionUpdateUtil.updateDefinition(oldDefinition, newDefinition));

    assertThat(e)
        .hasMessage(
            "Datatype of field with name 'temperature' has changed from 'FLOAT' to 'STRING'. Datatype changes are not allowed!");
  }

  @Test
  public void testUpdateDefinition_givenNewMeasurement_expectMerged() {
    final var newField = new FieldDefinition();

    newField.setName("voltage");
    newField.setDescription("battery bank voltage in millivolts");
    newField.setDatatype(DataType.INTEGER);

    final var newMeasurementDefinition = new MeasurementDefinition();

    newMeasurementDefinition.setName("data_2");
    newMeasurementDefinition.setTags(Set.of("tag_0", "tag_1", "tag_2"));
    newMeasurementDefinition.setFields(Set.of(newField));

    final var oldDefinition = getSampleDeviceDefinition();

    final var newDefinition = new DeviceDefinition();

    newDefinition.setMeasurements(
        Arrays.asList(oldDefinition.getMeasurements().get(0), newMeasurementDefinition));

    final var mergedDefinition =
        DefinitionUpdateUtil.updateDefinition(oldDefinition, newDefinition);

    assertThat(mergedDefinition).isNotNull();
    assertThat(mergedDefinition.getMeasurements()).hasSize(2);
    assertThat(mergedDefinition.getMeasurements().get(0)).isEqualTo(newMeasurementDefinition);
    assertThat(mergedDefinition.getMeasurements().get(1))
        .isEqualTo(oldDefinition.getMeasurements().get(0));
  }

  @Test
  public void testUpdateDefinition_givenNewFieldsAndChangedTags_expectMerged() {
    final var temperatureField = new FieldDefinition();

    temperatureField.setName("temperature");
    temperatureField.setDescription("Temperature in Degrees Celsius");
    temperatureField.setDatatype(DataType.FLOAT);

    final var voltageField = new FieldDefinition();

    voltageField.setName("voltage");
    voltageField.setDescription("Battery bank voltage in millivolts (mV)");
    voltageField.setDatatype(DataType.INTEGER);

    final var oldMeasurementDefinition = new MeasurementDefinition();

    oldMeasurementDefinition.setName("data");
    oldMeasurementDefinition.setTags(Set.of("tag_1", "tag_2"));
    oldMeasurementDefinition.setFields(Set.of(temperatureField));

    final var newMeasurementDefinition = new MeasurementDefinition();

    newMeasurementDefinition.setName("data");
    newMeasurementDefinition.setTags(Set.of("tag_1", "tag_3"));
    newMeasurementDefinition.setFields(Set.of(temperatureField, voltageField));

    final var oldDefinition = new DeviceDefinition();

    oldDefinition.setMeasurements(Collections.singletonList(oldMeasurementDefinition));

    final var newDefinition = new DeviceDefinition();

    newDefinition.setMeasurements(Collections.singletonList(newMeasurementDefinition));

    final var mergedDefinition =
        DefinitionUpdateUtil.updateDefinition(oldDefinition, newDefinition);

    assertThat(mergedDefinition).isNotNull();
    assertThat(mergedDefinition.getMeasurements()).hasSize(1);

    final var mergedMeasurementDefinition = mergedDefinition.getMeasurements().get(0);

    assertThat(mergedMeasurementDefinition).isNotNull();
    assertThat(mergedMeasurementDefinition.getName()).isEqualTo("data");
    assertThat(mergedMeasurementDefinition.getTags()).hasSize(3);
    assertThat(mergedMeasurementDefinition.getTags()).isEqualTo(Set.of("tag_1", "tag_2", "tag_3"));
    assertThat(mergedMeasurementDefinition.getFields()).hasSize(2);

    final var updatedVoltageField =
        mergedMeasurementDefinition.getFields().stream()
            .filter(f -> "voltage".equals(f.getName()))
            .findFirst()
            .orElse(null);

    final var updatedTemperatureField =
        mergedMeasurementDefinition.getFields().stream()
            .filter(f -> "temperature".equals(f.getName()))
            .findFirst()
            .orElse(null);

    assertThat(updatedVoltageField).isNotNull();
    assertThat(updatedTemperatureField).isNotNull();

    assertThat(updatedVoltageField.getName()).isEqualTo("voltage");
    assertThat(updatedVoltageField.getDatatype()).isEqualTo(DataType.INTEGER);
    assertThat(updatedVoltageField.getDescription())
        .isEqualTo("Battery bank voltage in millivolts (mV)");

    assertThat(updatedTemperatureField.getName()).isEqualTo("temperature");
    assertThat(updatedTemperatureField.getDatatype()).isEqualTo(DataType.FLOAT);
    assertThat(updatedTemperatureField.getDescription())
        .isEqualTo("Temperature in Degrees Celsius");
  }

  @Test
  public void testUpdateDefinition_givenNewFieldDescription_expectMergedWithNewDescription() {
    final var temperatureField = new FieldDefinition();

    temperatureField.setName("temperature");
    temperatureField.setDescription("Temperature in Degrees Celsius");
    temperatureField.setDatatype(DataType.FLOAT);

    final var newTemperatureField = new FieldDefinition();

    newTemperatureField.setName("temperature");
    newTemperatureField.setDescription("Temperature in Degrees Fahrenheit");
    newTemperatureField.setDatatype(DataType.FLOAT);

    final var oldMeasurementDefinition = new MeasurementDefinition();

    oldMeasurementDefinition.setName("data");
    oldMeasurementDefinition.setTags(Set.of("tag_1", "tag_2"));
    oldMeasurementDefinition.setFields(Set.of(temperatureField));

    final var newMeasurementDefinition = new MeasurementDefinition();

    newMeasurementDefinition.setName("data");
    newMeasurementDefinition.setTags(Set.of("tag_1", "tag_2"));
    newMeasurementDefinition.setFields(Set.of(newTemperatureField));

    final var oldDefinition = new DeviceDefinition();

    oldDefinition.setMeasurements(Collections.singletonList(oldMeasurementDefinition));

    final var newDefinition = new DeviceDefinition();

    newDefinition.setMeasurements(Collections.singletonList(newMeasurementDefinition));

    final var mergedDefinition =
        DefinitionUpdateUtil.updateDefinition(oldDefinition, newDefinition);

    assertThat(mergedDefinition).isNotNull();
    assertThat(mergedDefinition.getMeasurements()).hasSize(1);

    final var mergedMeasurementDefinition = mergedDefinition.getMeasurements().get(0);

    assertThat(mergedMeasurementDefinition).isNotNull();
    assertThat(mergedMeasurementDefinition.getName()).isEqualTo("data");
    assertThat(mergedMeasurementDefinition.getTags()).hasSize(2);
    assertThat(mergedMeasurementDefinition.getTags()).isEqualTo(Set.of("tag_1", "tag_2"));
    assertThat(mergedMeasurementDefinition.getFields()).hasSize(1);

    final var field1 = mergedMeasurementDefinition.getFields().iterator().next();

    assertThat(field1).isNotNull();

    assertThat(field1.getName()).isEqualTo("temperature");
    assertThat(field1.getDatatype()).isEqualTo(DataType.FLOAT);
    assertThat(field1.getDescription()).isEqualTo("Temperature in Degrees Fahrenheit");
  }

  private static DeviceDefinition getSampleDeviceDefinition() {
    final var temperatureField = new FieldDefinition();

    temperatureField.setName("temperature");
    temperatureField.setDatatype(DataType.FLOAT);
    temperatureField.setDescription("Temperature in degrees celsius");

    final var humidityField = new FieldDefinition();

    humidityField.setName("humidity");
    humidityField.setDatatype(DataType.INTEGER);
    humidityField.setDescription("Humidity in percent");

    final var measurementDefinition = new MeasurementDefinition();

    measurementDefinition.setName("measurement_1");
    measurementDefinition.setTags(Set.of("location", "identifier"));
    measurementDefinition.setFields(Set.of(temperatureField, humidityField));

    final var deviceDefinition = new DeviceDefinition();

    deviceDefinition.setMeasurements(List.of(measurementDefinition));

    return deviceDefinition;
  }
}
