package com.logreposit.logrepositapi.rest.mappers;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.DeviceDefinitionDto;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.FieldDefinitionDto;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.MeasurementDefinitionDto;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeviceDefinitionMapper {
  private DeviceDefinitionMapper() {}

  public static DeviceDefinition toEntity(DeviceDefinitionDto dto) {
    return createDeviceDefinition(
        dto.getMeasurements().stream()
            .map(
                m ->
                    createMeasurementDefinition(
                        m.getName(),
                        m.getTags(),
                        m.getFields().stream()
                            .map(
                                f ->
                                    createFieldDefinition(
                                        f.getName(), f.getDescription(), f.getDatatype()))
                            .collect(Collectors.toSet())))
            .collect(Collectors.toList()));
  }

  public static DeviceDefinitionDto toDto(DeviceDefinition entity) {
    return createDeviceDefinitionDto(
        entity.getMeasurements().stream()
            .map(
                m ->
                    createMeasurementDefinitionDto(
                        m.getName(),
                        m.getTags(),
                        m.getFields().stream()
                            .map(
                                f ->
                                    createFieldDefinitionDto(
                                        f.getName(), f.getDescription(), f.getDatatype()))
                            .collect(Collectors.toList())))
            .collect(Collectors.toList()));
  }

  private static DeviceDefinition createDeviceDefinition(List<MeasurementDefinition> measurements) {
    final var deviceDefinition = new DeviceDefinition();

    deviceDefinition.setMeasurements(measurements);

    return deviceDefinition;
  }

  private static MeasurementDefinition createMeasurementDefinition(
      String name, Set<String> tags, Set<FieldDefinition> fields) {
    final var measurementDefinition = new MeasurementDefinition();

    measurementDefinition.setName(name);
    measurementDefinition.setTags(tags);
    measurementDefinition.setFields(fields);

    return measurementDefinition;
  }

  private static FieldDefinition createFieldDefinition(
      String name, String description, DataType datatype) {
    final var fieldDefinition = new FieldDefinition();

    fieldDefinition.setName(name);
    fieldDefinition.setDescription(description);
    fieldDefinition.setDatatype(datatype);

    return fieldDefinition;
  }

  private static DeviceDefinitionDto createDeviceDefinitionDto(
      List<MeasurementDefinitionDto> measurements) {
    final var deviceDefinitionDto = new DeviceDefinitionDto();

    deviceDefinitionDto.setMeasurements(measurements);

    return deviceDefinitionDto;
  }

  private static MeasurementDefinitionDto createMeasurementDefinitionDto(
      String name, Set<String> tags, List<FieldDefinitionDto> fields) {
    final var measurementDefinitionDto = new MeasurementDefinitionDto();

    measurementDefinitionDto.setName(name);
    measurementDefinitionDto.setTags(tags);
    measurementDefinitionDto.setFields(fields);

    return measurementDefinitionDto;
  }

  private static FieldDefinitionDto createFieldDefinitionDto(
      String name, String description, DataType datatype) {
    final var fieldDefinitionDto = new FieldDefinitionDto();

    fieldDefinitionDto.setName(name);
    fieldDefinitionDto.setDescription(description);
    fieldDefinitionDto.setDatatype(datatype);

    return fieldDefinitionDto;
  }
}
