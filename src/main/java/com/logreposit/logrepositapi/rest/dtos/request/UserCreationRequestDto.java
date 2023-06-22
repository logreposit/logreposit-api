package com.logreposit.logrepositapi.rest.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserCreationRequestDto {
  @NotNull @Email private String email;

  @NotBlank
  @Pattern(regexp = "^[^\\\\]+$")
  private String password;
}
