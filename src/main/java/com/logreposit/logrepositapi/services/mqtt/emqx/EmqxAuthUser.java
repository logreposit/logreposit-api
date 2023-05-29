package com.logreposit.logrepositapi.services.mqtt.emqx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmqxAuthUser {
  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("is_super_user")
  private Boolean superUser;

  public EmqxAuthUser(String userId, Boolean superUser) {
    this.userId = userId;
    this.superUser = superUser;
  }
}
