package com.logreposit.logrepositapi.communication.messaging.mqtt.control;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateClientCommand extends MosquittoControlApiCommand {
  public CreateClientCommand(String username, String password) {
    super("createClient");

    this.username = username;
    this.password = password;
  }

  private String username;
  private String password;
}
