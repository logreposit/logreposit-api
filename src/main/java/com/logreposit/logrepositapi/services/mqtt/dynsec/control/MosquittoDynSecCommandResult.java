package com.logreposit.logrepositapi.services.mqtt.dynsec.control;

import com.logreposit.logrepositapi.services.mqtt.dynsec.control.commands.MosquittoControlApiCommand;
import lombok.Data;

@Data
public class MosquittoDynSecCommandResult {
  private MosquittoControlApiCommand command;
  private MosquittoControlApiResponse response;

  public MosquittoDynSecCommandResult(
      MosquittoControlApiCommand command, MosquittoControlApiResponse response) {
    this.command = command;
    this.response = response;
  }
}
