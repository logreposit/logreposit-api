package com.logreposit.logrepositapi.communication.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
public class UserCreatedMessageDto {
  private String id;
  private String email;
  private String password;
  private List<String> roles;
}
