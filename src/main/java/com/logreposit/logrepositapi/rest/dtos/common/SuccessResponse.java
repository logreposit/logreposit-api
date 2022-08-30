package com.logreposit.logrepositapi.rest.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(callSuper = true)
@Data
public class SuccessResponse<T extends ResponseDto> extends Response {
  private T data;

  @Builder
  public SuccessResponse(T data) {
    super(ResponseStatus.SUCCESS, RequestCorrelation.getCorrelationId());

    this.data = data;
  }
}
