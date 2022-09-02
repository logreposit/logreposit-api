package com.logreposit.logrepositapi.communication.messaging.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.mqtt.MqttMessageSender;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventLogdataReceivedMessageProcessor
    extends AbstractMessageProcessor<List<ReadingDto>> {
  private static final Logger logger =
      LoggerFactory.getLogger(EventLogdataReceivedMessageProcessor.class);

  private final MqttMessageSender mqttMessageSender;

  public EventLogdataReceivedMessageProcessor(
      ObjectMapper objectMapper, MqttMessageSender mqttMessageSender) {
    super(objectMapper);

    this.mqttMessageSender = mqttMessageSender;
  }

  @Override
  public void processMessage(Message message) throws MessagingException {
    String userId = message.getMetaData().getUserId();
    String deviceId = message.getMetaData().getDeviceId();
    List<ReadingDto> logData = this.getMessagePayload(message, new TypeReference<>() {});

    logger.info(
        "Currently NoOp because of no further implementation: Retrieved List<ReadingDto> for Device id='{}' of User id='{}': {}",
        deviceId,
        userId,
        logData);

    throw new RuntimeException("Not yet implemented");
  }
}
