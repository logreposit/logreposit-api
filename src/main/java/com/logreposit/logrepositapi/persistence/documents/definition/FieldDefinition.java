package com.logreposit.logrepositapi.persistence.documents.definition;

import lombok.Data;

@Data
public class FieldDefinition
{
    private String   name;
    private String   description;
    private DataType datatype;
}
