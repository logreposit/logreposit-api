package com.logreposit.logrepositapi.rest.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
@NoArgsConstructor
@Data
abstract class Response
{
    @JsonProperty(value = "status")
    private ResponseStatus status;

    @JsonProperty(value = "correlationId")
    private String correlationId;
}
