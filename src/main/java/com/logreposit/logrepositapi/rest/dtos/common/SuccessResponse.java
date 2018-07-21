package com.logreposit.logrepositapi.rest.dtos.common;

import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SuccessResponse<T extends ResponseDto> extends Response
{
    private T data;

    @Builder
    public SuccessResponse(T data)
    {
        super(ResponseStatus.SUCCESS);

        this.data = data;
    }
}
