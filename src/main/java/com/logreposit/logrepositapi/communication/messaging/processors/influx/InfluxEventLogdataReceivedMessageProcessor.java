package com.logreposit.logrepositapi.communication.messaging.processors.influx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.processors.AbstractMessageProcessor;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InfluxEventLogdataReceivedMessageProcessor
    extends AbstractMessageProcessor<List<ReadingDto>> {
  private static final Logger logger =
      LoggerFactory.getLogger(InfluxEventLogdataReceivedMessageProcessor.class);

  public InfluxEventLogdataReceivedMessageProcessor(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public void processMessage(Message message) throws MessagingException {
    List<ReadingDto> logData = this.getMessagePayload(message, new TypeReference<>() {});

    logger.info("TODO DoM: Received LogData with size {}", logData.size());
  }
}
