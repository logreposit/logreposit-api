package com.logreposit.logrepositapi.communication.messaging.handler;

import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.processors.mqtt.MqttEventLogdataReceivedMessageProcessor;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.configuration.conditional.ConditionalOnEnabledApplicationMode;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnEnabledApplicationMode(mode = ApplicationConfiguration.ApplicationMode.PROCESSOR_MQTT)
public class MqttMessageHandler extends AbstractMessageHandler {
  public MqttMessageHandler(
      MqttEventLogdataReceivedMessageProcessor mqttEventLogdataReceivedMessageProcessor) {
    super(
        Map.of(
            MessageType.EVENT_GENERIC_LOGDATA_RECEIVED, mqttEventLogdataReceivedMessageProcessor));
  }
}
