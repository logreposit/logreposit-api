package com.logreposit.logrepositapi.persistence.documents.definition;

import lombok.Data;

import java.util.List;

@Data
public class DeviceDefinition
{
    private List<MeasurementDefinition> measurements;
}
