package com.logreposit.logrepositapi.rest.dtos.shared.definition;

import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class DeviceDefinitionDto implements ResponseDto
{
    @NotEmpty
    private List<@Valid MeasurementDefinitionDto> measurements;

    public DeviceDefinitionDto()
    {
        this.measurements = new ArrayList<>();
    }
}
