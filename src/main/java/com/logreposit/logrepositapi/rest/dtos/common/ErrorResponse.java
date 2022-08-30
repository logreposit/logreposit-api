package com.logreposit.logrepositapi.rest.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends Response {
  private Integer code;
  private String message;

  @Builder
  public ErrorResponse(Integer code, String message) {
    super(ResponseStatus.ERROR, RequestCorrelation.getCorrelationId());

    this.code = code;
    this.message = message;
  }
}
