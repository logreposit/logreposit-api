package com.logreposit.logrepositapi.rest.dtos.common;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ErrorResponse extends Response
{
    private Integer code;
    private String  message;

    @Builder
    public ErrorResponse(Integer code, String message)
    {
        super(ResponseStatus.ERROR);

        this.code = code;
        this.message = message;
    }
}
