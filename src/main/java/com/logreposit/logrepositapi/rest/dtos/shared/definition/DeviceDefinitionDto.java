package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class DeviceDefinitionDto
{
    @NotEmpty
    private List<MeasurementDefinitionDto> measurements;
}
