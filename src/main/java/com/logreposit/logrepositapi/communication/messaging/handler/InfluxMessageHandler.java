package com.logreposit.logrepositapi.communication.messaging.handler;

import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.processors.influx.InfluxEventDeviceCreatedMessageProcessor;
import com.logreposit.logrepositapi.communication.messaging.processors.influx.InfluxEventGenericLogdataReceivedMessageProcessor;
import com.logreposit.logrepositapi.communication.messaging.processors.influx.InfluxEventUserCreatedMessageProcessor;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.configuration.conditional.ConditionalOnEnabledApplicationMode;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnEnabledApplicationMode(
    mode = ApplicationConfiguration.ApplicationMode.PROCESSOR_INFLUX)
public class InfluxMessageHandler extends AbstractMessageHandler {
  public InfluxMessageHandler(
      InfluxEventUserCreatedMessageProcessor influxEventUserCreatedMessageProcessor,
      InfluxEventDeviceCreatedMessageProcessor influxEventDeviceCreatedMessageProcessor,
      InfluxEventGenericLogdataReceivedMessageProcessor
          influxEventGenericLogdataReceivedMessageProcessor) {
    super(
        Map.ofEntries(
            Map.entry(MessageType.EVENT_USER_CREATED, influxEventUserCreatedMessageProcessor),
            Map.entry(MessageType.EVENT_DEVICE_CREATED, influxEventDeviceCreatedMessageProcessor),
            Map.entry(
                MessageType.EVENT_GENERIC_LOGDATA_RECEIVED,
                influxEventGenericLogdataReceivedMessageProcessor)));
  }
}
