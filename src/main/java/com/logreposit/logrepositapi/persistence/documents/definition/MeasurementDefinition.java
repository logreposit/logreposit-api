package com.logreposit.logrepositapi.persistence.documents.definition;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class MeasurementDefinition
{
    private String               name;
    private Set<String>          tags;
    private Set<FieldDefinition> fields;

    public MeasurementDefinition()
    {
        this.fields = new HashSet<>();
    }
}
