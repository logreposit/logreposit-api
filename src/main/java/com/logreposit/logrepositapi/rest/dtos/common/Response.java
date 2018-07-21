package com.logreposit.logrepositapi.rest.dtos.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
abstract class Response
{
    @JsonProperty(value = "status")
    private ResponseStatus status;
}
