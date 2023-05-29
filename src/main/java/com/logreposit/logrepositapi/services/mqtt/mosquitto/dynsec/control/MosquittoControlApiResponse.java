package com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MosquittoControlApiResponse {
  private String correlationData;
  private String command;
  private String error;
}
