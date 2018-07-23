package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponseDto extends ResponseDto
{
    private String       id;
    private String       email;
    private List<String> roles;
}
