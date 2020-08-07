package com.logreposit.logrepositapi.utils.definition;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.TagDto;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefinitionValidator
{
    private static final Logger logger = LoggerFactory.getLogger(DefinitionValidator.class);

    private final DeviceDefinition deviceDefinition;

    private DefinitionValidator(DeviceDefinition deviceDefinition)
    {
        this.deviceDefinition = deviceDefinition;
    }

    public static DefinitionValidator forDefinition(DeviceDefinition deviceDefinition) {
        return new DefinitionValidator(deviceDefinition);
    }

    public void validate(List<ReadingDto> readings)
    {
        if (this.deviceDefinition == null || CollectionUtils.isEmpty(this.deviceDefinition.getMeasurements())) {
            logger.info("Device definition has not been set yet. Cannot perform definition check.");

            throw new DefinitionValidationException("Device definition has not been set yet. Cannot perform definition check.");
        }

        if (CollectionUtils.isEmpty(readings))
        {
            logger.info("Readings are null or empty, so nothing to validate.");

            return;
        }

        readings.forEach(this::validateReading);
    }

    private void validateReading(ReadingDto readingDto)
    {
        final String                measurementName       = readingDto.getMeasurement();
        final Map<String, String>   tags                  = readingDto.getTags()
                                                                      .stream()
                                                                      .collect(Collectors.toMap(TagDto::getName, TagDto::getValue));
        final List<FieldDto>        fields                = readingDto.getFields();
        final MeasurementDefinition measurementDefinition = this.getMeasurementDefinition(measurementName);
        final Set<String>           invalidTags           = new HashSet<>(CollectionUtils.subtract(tags.keySet(), measurementDefinition.getTags()));

        if (CollectionUtils.isNotEmpty(invalidTags))
        {
            throw new DefinitionValidationException(
                    String.format(
                            "Measurement with name '%s' does not have the following tags defined: %s",
                            measurementName,
                            invalidTags.stream().sorted().collect(Collectors.toList())
                    )
            );
        }

        fields.forEach(f -> {
            final FieldDefinition fieldDefinition = this.getFieldDefinition(measurementDefinition, f.getName());

            if (!fieldDefinition.getDatatype().equals(f.getDatatype()))
            {
                throw new DefinitionValidationException(
                        String.format(
                                "Field with name '%s' within Measurement with name '%s' does not have the correct dataType defined. Given: %s / Expected: %s",
                                f.getName(),
                                measurementName,
                                f.getDatatype(),
                                fieldDefinition.getDatatype()
                        )
                );
            }
        });
    }

    private MeasurementDefinition getMeasurementDefinition(String name)
    {
        return this.deviceDefinition.getMeasurements()
                                    .stream()
                                    .filter(m -> name.equals(m.getName()))
                                    .findFirst()
                                    .orElseThrow(
                                            () -> new DefinitionValidationException(
                                                    String.format(
                                                            "Measurement with name '%s' does not exist for the given device.",
                                                            name
                                                    )
                                            )
                                    );
    }

    private FieldDefinition getFieldDefinition(MeasurementDefinition measurementDefinition, String fieldName)
    {
        return measurementDefinition.getFields()
                                    .stream()
                                    .filter(f -> fieldName.equals(f.getName()))
                                    .findFirst()
                                    .orElseThrow(
                                            () -> new DefinitionValidationException(
                                                    String.format(
                                                            "Field with name '%s' does not exist within Measurement with name '%s' for the given device.",
                                                            fieldName,
                                                            measurementDefinition.getName()
                                                    )
                                            )
                                    );
    }
}
