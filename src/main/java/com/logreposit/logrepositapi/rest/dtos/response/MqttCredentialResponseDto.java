package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import java.util.Date;
import java.util.List;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class MqttCredentialResponseDto implements ResponseDto {
  private final String id;
  private final String username;
  private final String password;
  private final String description;
  private final List<String> roles;
  private final Date createdAt;

  public MqttCredentialResponseDto(
      String id,
      String username,
      String password,
      String description,
      List<String> roles,
      Date createdAt) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.description = description;
    this.roles = roles;
    this.createdAt = createdAt;
  }
}
