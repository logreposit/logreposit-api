package com.logreposit.logrepositapi.communication.messaging.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.mqtt.MqttMessageSender;
import com.logreposit.logrepositapi.communication.messaging.mqtt.dtos.IngressV2MqttDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EventLogdataReceivedMessageProcessor
    extends AbstractMessageProcessor<List<ReadingDto>> {
  private final MqttMessageSender mqttMessageSender;

  public EventLogdataReceivedMessageProcessor(
      ObjectMapper objectMapper, MqttMessageSender mqttMessageSender) {
    super(objectMapper);

    this.mqttMessageSender = mqttMessageSender;
  }

  @Override
  public void processMessage(Message message) throws MessagingException {
    List<ReadingDto> logData = this.getMessagePayload(message, new TypeReference<>() {});

    sendLogdataReceivedMqttMessage(message.getMetaData(), logData);
  }

  private void sendLogdataReceivedMqttMessage(
      MessageMetaData messageMetaData, List<ReadingDto> readings) {
    final var userId = messageMetaData.getUserId();
    final var deviceId = messageMetaData.getDeviceId();

    if (userId == null || deviceId == null) {
      throw new IllegalArgumentException("userId and deviceId has to be set!");
    }

    final var topic = String.format("logreposit/users/%s/devices/%s/ingress", userId, deviceId);

    final var payload =
        IngressV2MqttDto.builder()
            .correlationId(messageMetaData.getCorrelationId())
            .readings(readings)
            .build();

    mqttMessageSender.send(topic, payload);
  }
}
