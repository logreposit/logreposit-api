package com.logreposit.logrepositapi.persistence.documents.definition;

import lombok.Data;

import java.util.List;

@Data
public class MeasurementDefinition
{
    private String name;
    private List<String> tags;
    private List<FieldDefinition> fields;
}
