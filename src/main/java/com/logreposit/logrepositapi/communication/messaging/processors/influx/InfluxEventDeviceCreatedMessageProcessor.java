package com.logreposit.logrepositapi.communication.messaging.processors.influx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.dtos.DeviceCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.processors.AbstractMessageProcessor;
import com.logreposit.logrepositapi.services.influxdb.InfluxDBService;
import com.logreposit.logrepositapi.services.influxdb.InfluxDBServiceException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InfluxEventDeviceCreatedMessageProcessor
    extends AbstractMessageProcessor<DeviceCreatedMessageDto> {
  private static final Logger logger =
      LoggerFactory.getLogger(InfluxEventDeviceCreatedMessageProcessor.class);

  private final InfluxDBService influxDBService;

  @Autowired
  public InfluxEventDeviceCreatedMessageProcessor(
      ObjectMapper objectMapper, InfluxDBService influxDBService) {
    super(objectMapper);

    this.influxDBService = influxDBService;
  }

  @Override
  public void processMessage(Message message) throws MessagingException {
    this.validateMessage(message);

    final var device = this.getMessagePayload(message, new TypeReference<>() {});

    logger.info("Retrieved created Device: {}", LoggingUtils.serialize(device));

    try {
      this.influxDBService.createDatabase(device.getId(), message.getMetaData().getUserEmail());

      logger.info("Successfully created database and configured permissions.");
    } catch (InfluxDBServiceException exception) {
      logger.error("Caught InfluxDBServiceException while creating database", exception);

      throw new MessagingException(
          "Caught InfluxDBServiceException while creating database", exception);
    }
  }

  private void validateMessage(Message message) throws MessagingException {
    if (message == null || message.getMetaData() == null) {
      logger.error("Message MetaData missing.");
      throw new MessagingException("Message MetaData missing.");
    }

    final var messageMetaData = message.getMetaData();

    if (StringUtils.isEmpty(messageMetaData.getUserEmail())) {
      logger.error("metaData.userEmail missing");
      throw new MessagingException("metaData.userEmail is missing.");
    }
  }
}
