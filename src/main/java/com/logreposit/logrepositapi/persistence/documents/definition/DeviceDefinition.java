package com.logreposit.logrepositapi.persistence.documents.definition;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeviceDefinition
{
    private List<MeasurementDefinition> measurements;

    public DeviceDefinition() {
        this.measurements = new ArrayList<>();
    }
}
