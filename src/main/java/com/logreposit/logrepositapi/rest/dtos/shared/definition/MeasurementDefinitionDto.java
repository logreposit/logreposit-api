package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class MeasurementDefinitionDto
{
    @NotBlank
    private String name;

    private List<String> tags;

    @NotEmpty
    private List<FieldDefinitionDto> fields;
}
