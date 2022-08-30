package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import java.util.List;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class UserCreatedResponseDto implements ResponseDto {
  private final String id;
  private final String email;
  private final List<String> roles;
  private final String apiKey;

  public UserCreatedResponseDto(String id, String email, List<String> roles, String apiKey) {
    this.id = id;
    this.email = email;
    this.roles = roles;
    this.apiKey = apiKey;
  }
}
