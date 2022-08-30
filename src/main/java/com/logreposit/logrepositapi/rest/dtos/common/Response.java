package com.logreposit.logrepositapi.rest.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
abstract class Response {
  @JsonProperty(value = "status")
  private ResponseStatus status;

  @JsonProperty(value = "correlationId")
  private String correlationId;

  public Response(ResponseStatus status, String correlationId) {
    this.status = status;
    this.correlationId = correlationId;
  }
}
