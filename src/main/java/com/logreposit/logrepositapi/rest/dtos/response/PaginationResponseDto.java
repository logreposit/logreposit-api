package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
@EqualsAndHashCode(callSuper = true)
@Data
public class PaginationResponseDto<T extends ResponseDto> extends ResponseDto
{
    private long    totalElements;
    private long    totalPages;
    private List<T> items;
}
