package com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeleteClientCommand extends MosquittoControlApiCommand {
  public DeleteClientCommand(String username) {
    super("deleteClient");

    this.username = username;
  }

  private String username;
}
