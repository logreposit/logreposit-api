package com.logreposit.logrepositapi.rest.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserCreationRequestDto
{
    @NotNull
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^[^\\\\]+$")
    private String password;
}
