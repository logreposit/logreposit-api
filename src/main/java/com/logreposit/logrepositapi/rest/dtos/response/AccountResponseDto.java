package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.controllers.AccountController;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class AccountResponseDto implements ResponseDto {
  private final String id;
  private final String email;

  public AccountResponseDto(String id, String email) {
    this.id = id;
    this.email = emaill;
  }
}
