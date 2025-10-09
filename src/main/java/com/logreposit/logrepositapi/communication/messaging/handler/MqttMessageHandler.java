package com.logreposit.logrepositapi.communication.messaging.handler;

import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.processors.mqtt.MqttEventLogdataReceivedMessageProcessor;
import java.util.Map;
import org.springframework.stereotype.Service;

// TODO DoM: Make application-mode conditional?
@Service
public class MqttMessageHandler extends AbstractMessageHandler {
  public MqttMessageHandler(
      MqttEventLogdataReceivedMessageProcessor mqttEventLogdataReceivedMessageProcessor) {
    super(
        Map.of(
            MessageType.EVENT_GENERIC_LOGDATA_RECEIVED, mqttEventLogdataReceivedMessageProcessor));
  }
}
