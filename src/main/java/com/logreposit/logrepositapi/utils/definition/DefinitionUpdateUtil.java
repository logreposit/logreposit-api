package com.logreposit.logrepositapi.utils.definition;

import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefinitionUpdateUtil
{
    private static final Logger logger = LoggerFactory.getLogger(DefinitionUpdateUtil.class);

    private DefinitionUpdateUtil() {}

    public static DeviceDefinition updateDefinition(DeviceDefinition existingDefinition,
                                                    DeviceDefinition newDefinition)
    {
        validateNoDuplicatedMeasurementNames(newDefinition.getMeasurements());
        newDefinition.getMeasurements().forEach(m -> validateNoDuplicateFieldNamesInMeasurement(m.getFields()));

        DeviceDefinition currentDefinition = getCurrentDefinitionOrEmpty(existingDefinition);

        Set<String> measurementNamesInCurrentDefinition = getMeasurementNames(currentDefinition.getMeasurements());

        List<MeasurementDefinition> newMeasurements = newDefinition.getMeasurements()
                                                                   .stream()
                                                                   .filter(m -> !measurementNamesInCurrentDefinition.contains(m.getName()))
                                                                   .map(DefinitionUpdateUtil::copyMeasurement)
                                                                   .collect(Collectors.toList());

        Set<String> measurementNamesInNewDefinition = getMeasurementNames(newDefinition.getMeasurements());

        List<MeasurementDefinition> currentUntouchedMeasurements = currentDefinition.getMeasurements()
                                                                                    .stream()
                                                                                    .filter(m -> !measurementNamesInNewDefinition.contains(m.getName()))
                                                                                    .map(DefinitionUpdateUtil::copyMeasurement)
                                                                                    .collect(Collectors.toList());

        Set<String> measurementNamesToBeMerged = new HashSet<>(CollectionUtils.subtract(measurementNamesInNewDefinition, getMeasurementNames(newMeasurements)));

        List<MeasurementDefinition> mergedMeasurements          = mergeMeasurements(measurementNamesToBeMerged, currentDefinition.getMeasurements(), newDefinition.getMeasurements());
        List<MeasurementDefinition> finalMeasurementDefinitions = joinLists(List.of(newMeasurements, currentUntouchedMeasurements, mergedMeasurements));

        DeviceDefinition deviceDefinition = new DeviceDefinition();

        deviceDefinition.setMeasurements(finalMeasurementDefinitions);

        return deviceDefinition;
    }

    private static List<MeasurementDefinition> mergeMeasurements(Set<String> measurementNamesToBeMerged, List<MeasurementDefinition> existingMeasurements, List<MeasurementDefinition> newMeasurements)
    {
        List<MeasurementDefinition> mergedDefinitions = measurementNamesToBeMerged.stream()
                                                                                  .map(n -> mergeMeasurement(getMeasurement(existingMeasurements, n), getMeasurement(newMeasurements, n)))
                                                                                  .collect(Collectors.toList());

        return mergedDefinitions;
    }

    private static MeasurementDefinition getMeasurement(List<MeasurementDefinition> measurementDefinitions, String name)
    {
        Optional<MeasurementDefinition> measurement = measurementDefinitions.stream()
                                                                            .filter(m -> name.equals(m.getName()))
                                                                            .findFirst();

        if (measurement.isEmpty())
        {
            throw new RuntimeException("Measurement with given name not found although it should be there.");
        }

        return measurement.get();
    }

    private static MeasurementDefinition mergeMeasurement(MeasurementDefinition existingDefinition, MeasurementDefinition newDefinition)
    {
        Set<String>        newDefinitionNames      = getFieldNames(newDefinition.getFields());
        Set<String>        existingDefinitionNames = getFieldNames(existingDefinition.getFields());
        Collection<String> newFieldNames           = CollectionUtils.subtract(newDefinitionNames, existingDefinitionNames);

        Set<FieldDefinition> newFields = newDefinition.getFields().stream().filter(f -> newFieldNames.contains(f.getName())).collect(Collectors.toSet());

        Set<FieldDefinition> currentUntouchedFields = existingDefinition.getFields()
                                                                        .stream()
                                                                        .filter(m -> !newDefinitionNames.contains(m.getName()))
                                                                        .collect(Collectors.toSet());

        Set<String> fieldNamesToBeMerged = new HashSet<>(CollectionUtils.subtract(newDefinitionNames, getFieldNames(newFields)));

        Set<FieldDefinition> mergedFields          = mergeFields(fieldNamesToBeMerged, existingDefinition.getFields(), newDefinition.getFields());
        Set<FieldDefinition> finalFieldDefinitions = joinSets(List.of(newFields, currentUntouchedFields, mergedFields));

        MeasurementDefinition measurementDefinition = new MeasurementDefinition();

        measurementDefinition.setName(existingDefinition.getName());
        measurementDefinition.setTags(joinSets(List.of(existingDefinition.getTags(), newDefinition.getTags())));
        measurementDefinition.setFields(finalFieldDefinitions);

        return measurementDefinition;
    }

    private static void validateNoDuplicatedMeasurementNames(Collection<MeasurementDefinition> measurementDefinitions)
    {
        Map<String, List<MeasurementDefinition>> groupedByName = measurementDefinitions.stream()
                                                                                       .collect(Collectors.groupingBy(MeasurementDefinition::getName, Collectors.toList()));

        if (groupedByName.values().stream().anyMatch(m -> m.size() > 1))
        {
            throw new DefinitionUpdateValidationException("Duplicated measurements with the same name are not allowed.");
        }
    }

    private static void validateNoDuplicateFieldNamesInMeasurement(Collection<FieldDefinition> fieldDefinitions)
    {
        Map<String, List<FieldDefinition>> groupedByName = fieldDefinitions.stream()
                                                                           .collect(Collectors.groupingBy(FieldDefinition::getName, Collectors.toList()));

        if (groupedByName.values().stream().anyMatch(m -> m.size() > 1))
        {
            throw new DefinitionUpdateValidationException("Duplicated fields with the same name inside a single measurement are not allowed.");
        }
    }

    private static DeviceDefinition getCurrentDefinitionOrEmpty(DeviceDefinition currentDefinition)
    {
        if (currentDefinition == null)
        {
            logger.info("Current DeviceDefinition is null, returning new empty one.");

            DeviceDefinition deviceDefinition = new DeviceDefinition();

            deviceDefinition.setMeasurements(Collections.emptyList());

            return deviceDefinition;
        }

        return currentDefinition;
    }

    private static Set<FieldDefinition> mergeFields(Set<String> fieldNamesToBeMerged, Set<FieldDefinition> existingFields, Set<FieldDefinition> newFields)
    {
        Set<FieldDefinition> mergedDefinitions = fieldNamesToBeMerged.stream()
                                                                     .map(n -> mergeField(getField(existingFields, n), getField(newFields, n)))
                                                                     .collect(Collectors.toSet());

        return mergedDefinitions;
    }

    private static FieldDefinition getField(Set<FieldDefinition> fieldDefinitions, String name)
    {
        Optional<FieldDefinition> field = fieldDefinitions.stream()
                                                          .filter(m -> name.equals(m.getName()))
                                                          .findFirst();

        if (field.isEmpty())
        {
            throw new RuntimeException(String.format("Field with given name '%s' was not found although it should be there", name));
        }

        return field.get();
    }

    private static FieldDefinition mergeField(FieldDefinition existingDefinition, FieldDefinition newDefinition)
    {
        if (existingDefinition.getDatatype() != newDefinition.getDatatype())
        {
            throw new DefinitionUpdateValidationException(
                    String.format(
                            "Datatype of field with name '%s' has changed from '%s' to '%s'. Datatype changes are not allowed!",
                            existingDefinition.getName(),
                            existingDefinition.getDatatype(),
                            newDefinition.getDatatype()
                    )
            );
        }

        FieldDefinition fieldDefinition = new FieldDefinition();

        fieldDefinition.setName(existingDefinition.getName());
        fieldDefinition.setDatatype(existingDefinition.getDatatype());
        fieldDefinition.setDescription(newDefinition.getDescription());

        return fieldDefinition;
    }

    private static MeasurementDefinition copyMeasurement(MeasurementDefinition originalMeasurement)
    {
        MeasurementDefinition measurementDefinition = new MeasurementDefinition();

        measurementDefinition.setName(originalMeasurement.getName());
        measurementDefinition.setTags(new HashSet<>(originalMeasurement.getTags()));
        measurementDefinition.setFields(originalMeasurement.getFields()
                                                           .stream()
                                                           .map(DefinitionUpdateUtil::copyField)
                                                           .collect(Collectors.toSet()));

        return measurementDefinition;
    }

    private static FieldDefinition copyField(FieldDefinition originalField)
    {
        FieldDefinition fieldDefinition = new FieldDefinition();

        fieldDefinition.setName(originalField.getName());
        fieldDefinition.setDatatype(originalField.getDatatype());
        fieldDefinition.setDescription(originalField.getDescription());

        return fieldDefinition;
    }

    private static Set<String> getMeasurementNames(Collection<MeasurementDefinition> measurementDefinitions)
    {
        return measurementDefinitions.stream()
                                     .map(MeasurementDefinition::getName)
                                     .collect(Collectors.toSet());
    }

    private static Set<String> getFieldNames(Collection<FieldDefinition> fieldDefinitions)
    {
        return fieldDefinitions.stream()
                               .map(FieldDefinition::getName)
                               .collect(Collectors.toSet());
    }

    private static <T> List<T> joinLists(List<List<T>> lists)
    {
        return lists.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static <T> Set<T> joinSets(List<Set<T>> sets)
    {
        return sets.stream()
                   .flatMap(Set::stream)
                   .collect(Collectors.toSet());
    }
}
