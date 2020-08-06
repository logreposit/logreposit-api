package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import com.logreposit.logrepositapi.rest.dtos.validation.ValidKeyName;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class MeasurementDefinitionDto
{
    @ValidKeyName
    private String name;

    private Set<@ValidKeyName String> tags;

    @NotEmpty
    private List<@Valid FieldDefinitionDto> fields;

    public MeasurementDefinitionDto() {
        this.tags = new HashSet<>();
        this.fields = new ArrayList<>();
    }
}
