package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import com.logreposit.logrepositapi.rest.dtos.validation.ValidKeyName;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

@Data
public class MeasurementDefinitionDto
{
    @ValidKeyName
    private String name;

    private Set<@ValidKeyName String> tags;

    @NotEmpty
    private List<FieldDefinitionDto> fields;
}
