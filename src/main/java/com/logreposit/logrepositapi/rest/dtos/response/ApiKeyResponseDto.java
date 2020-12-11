package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.Data;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class ApiKeyResponseDto implements ResponseDto
{
    private final String id;
    private final String key;
    private final Date   createdAt;

    public ApiKeyResponseDto(String id, String key, Date createdAt)
    {
        this.id        = id;
        this.key       = key;
        this.createdAt = createdAt;
    }
}
