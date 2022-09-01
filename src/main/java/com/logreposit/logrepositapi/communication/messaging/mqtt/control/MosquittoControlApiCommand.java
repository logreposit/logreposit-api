package com.logreposit.logrepositapi.communication.messaging.mqtt.control;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class MosquittoControlApiCommand {
  private String command;
  private String correlationData;

  public MosquittoControlApiCommand(String command) {
    this.command = command;
    this.correlationData = UUID.randomUUID().toString();
  }
}
