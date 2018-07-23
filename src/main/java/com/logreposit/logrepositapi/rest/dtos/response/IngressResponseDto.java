package com.logreposit.logrepositapi.rest.dtos.response;

import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class IngressResponseDto extends ResponseDto
{
    private String message;
}
