package com.logreposit.logrepositapi.rest.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logreposit.logrepositapi.rest.dtos.shared.definition.DeviceDefinitionDto;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DeviceCreationRequestDto
{
    @NotBlank
    private String name;

    private DeviceDefinitionDto definition;
}
