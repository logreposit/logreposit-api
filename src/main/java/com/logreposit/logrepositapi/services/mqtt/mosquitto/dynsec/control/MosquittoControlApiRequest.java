package com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.MosquittoControlApiCommand;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MosquittoControlApiRequest {
  private List<? extends MosquittoControlApiCommand> commands;
}
