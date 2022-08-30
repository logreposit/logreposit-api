package com.logreposit.logrepositapi.rest.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.ResponseDto;
import java.util.Date;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class DeviceTokenResponseDto implements ResponseDto {
  private final String id;
  private final String token;
  private final Date createdAt;

  public DeviceTokenResponseDto(String id, String token, Date createdAt) {
    this.id = id;
    this.token = token;
    this.createdAt = createdAt;
  }
}
