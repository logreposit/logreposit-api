package com.logreposit.logrepositapi.rest.dtos.response;

import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.Data;

@Data
public class IngressResponseDto implements ResponseDto
{
    private final String message;
}
