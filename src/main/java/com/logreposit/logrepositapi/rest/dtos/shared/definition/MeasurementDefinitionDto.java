package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

@Data
public class MeasurementDefinitionDto
{
    @NotBlank
    private String name; // TODO: must match influx db measurement name convention / OR [a-z0-9_]

    private Set<String> tags;

    @NotEmpty
    private List<FieldDefinitionDto> fields;
}
