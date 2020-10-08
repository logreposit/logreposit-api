package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.rest.dtos.validation.ValidKeyName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FieldDefinitionDto
{
    @NotBlank
    @ValidKeyName
    private String name;

    private String description;

    @NotNull
    private DataType datatype;
}
