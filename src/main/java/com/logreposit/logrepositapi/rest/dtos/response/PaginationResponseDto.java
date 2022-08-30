package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
@Data
public class PaginationResponseDto<T extends ResponseDto> implements ResponseDto {
  private final long totalElements;
  private final long totalPages;
  private final List<T> items;
}
