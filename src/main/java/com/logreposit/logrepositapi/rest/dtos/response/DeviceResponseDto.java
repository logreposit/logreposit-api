package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.DeviceDefinitionDto;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class DeviceResponseDto implements ResponseDto
{
    private final String              id;
    private final String              name;
    private final DeviceDefinitionDto definition;
}
