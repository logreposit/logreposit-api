package com.logreposit.logrepositapi.utils.definition;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class DefinitionUpdateUtilTests
{
    @Test
    public void testUpdateDefinition_givenCurrentDefinitionIsNull_expectNewDefinitionIsChosen() {
        DeviceDefinition newDefinition = getSampleDeviceDefinition();
        DeviceDefinition mergedDefinition = DefinitionUpdateUtil.updateDefinition(null, newDefinition);

        assertThat(mergedDefinition).isNotNull();
        assertThat(mergedDefinition).isEqualTo(newDefinition);
    }

    private static DeviceDefinition getSampleDeviceDefinition() {

        FieldDefinition temperatureField = new FieldDefinition();

        temperatureField.setName("temperature");
        temperatureField.setDatatype(DataType.FLOAT);
        temperatureField.setDescription("Temperature in degrees celsius");

        FieldDefinition humidityField = new FieldDefinition();

        humidityField.setName("humidity");
        humidityField.setDatatype(DataType.INTEGER);
        humidityField.setDescription("Humidity in percent");

        MeasurementDefinition measurementDefinition = new MeasurementDefinition();

        measurementDefinition.setName("measurement_1");
        measurementDefinition.setTags(new HashSet<>(Arrays.asList("location", "identifier")));
        measurementDefinition.setFields(new HashSet<>(Arrays.asList(temperatureField, humidityField)));

        DeviceDefinition deviceDefinition = new DeviceDefinition();

        deviceDefinition.setMeasurements(Collections.singletonList(measurementDefinition));

        return deviceDefinition;
    }
}
