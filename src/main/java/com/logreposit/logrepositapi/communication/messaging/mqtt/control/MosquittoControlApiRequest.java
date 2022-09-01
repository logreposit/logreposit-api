package com.logreposit.logrepositapi.communication.messaging.mqtt.control;

import com.fasterxml.jackson.annotation.JsonInclude;
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
