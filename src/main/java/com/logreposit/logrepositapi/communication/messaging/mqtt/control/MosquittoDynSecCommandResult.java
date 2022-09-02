package com.logreposit.logrepositapi.communication.messaging.mqtt.control;

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
