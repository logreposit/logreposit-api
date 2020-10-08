package com.logreposit.logrepositapi.rest.dtos.request.ingress;

import com.logreposit.logrepositapi.rest.dtos.validation.ValidKeyName;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TagDto
{
    @ValidKeyName
    private String name;

    @NotBlank
    private String value;
}
