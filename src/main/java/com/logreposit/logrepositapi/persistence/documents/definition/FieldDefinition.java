package com.logreposit.logrepositapi.persistence.documents.definition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FieldDefinition
{
    private String name;
    private String description;
    private DataType datatype;
}
