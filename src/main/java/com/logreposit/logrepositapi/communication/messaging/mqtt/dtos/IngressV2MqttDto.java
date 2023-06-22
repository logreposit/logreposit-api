package com.logreposit.logrepositapi.communication.messaging.mqtt.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class IngressV2MqttDto {
  private String correlationId;
  private List<ReadingDto> readings;
}
