package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeviceResponseDto extends ResponseDto
{
    private String id;
    private String name;
}
