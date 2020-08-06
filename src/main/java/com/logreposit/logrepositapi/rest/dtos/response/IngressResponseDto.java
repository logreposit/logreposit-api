package com.logreposit.logrepositapi.rest.dtos.response;

import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
public class IngressResponseDto implements ResponseDto
{
    private String message;
}
