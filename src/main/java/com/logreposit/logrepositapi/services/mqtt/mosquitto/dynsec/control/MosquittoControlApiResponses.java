package com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MosquittoControlApiResponses {
  private List<MosquittoControlApiResponse> responses;
}
