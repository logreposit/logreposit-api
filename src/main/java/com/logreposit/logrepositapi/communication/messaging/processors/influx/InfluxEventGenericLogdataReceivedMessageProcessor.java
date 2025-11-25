package com.logreposit.logrepositapi.communication.messaging.processors.influx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.processors.AbstractMessageProcessor;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.services.influxdb.InfluxDBService;
import com.logreposit.logrepositapi.services.influxdb.batchpoints.generic.GenericLogdataBatchPointsFactory;
import com.logreposit.logrepositapi.services.influxdb.batchpoints.generic.GenericLogdataBatchPointsFactoryException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InfluxEventGenericLogdataReceivedMessageProcessor
    extends AbstractMessageProcessor<List<ReadingDto>> {
  private static final Logger logger =
      LoggerFactory.getLogger(InfluxEventGenericLogdataReceivedMessageProcessor.class);

  private final InfluxDBService influxDBService;
  private final GenericLogdataBatchPointsFactory genericLogdataBatchPointsFactory;

  @Autowired
  public InfluxEventGenericLogdataReceivedMessageProcessor(
      ObjectMapper objectMapper,
      InfluxDBService influxDBService,
      GenericLogdataBatchPointsFactory genericLogdataBatchPointsFactory) {
    super(objectMapper);

    this.influxDBService = influxDBService;
    this.genericLogdataBatchPointsFactory = genericLogdataBatchPointsFactory;
  }

  @Override
  public void processMessage(Message message) throws MessagingException {
    final var userId = message.getMetaData().getUserId();
    final var deviceId = message.getMetaData().getDeviceId();
    final var logData = this.getMessagePayload(message, new TypeReference<>() {});

    logger.info(
        "Retrieved List<ReadingDto> for Device '{}' of User '{}': {}",
        deviceId,
        userId,
        LoggingUtils.serialize(logData));

    try {
      final var batchPoints =
          this.genericLogdataBatchPointsFactory.createBatchPoints(deviceId, logData);

      this.influxDBService.insert(batchPoints);

      logger.info("Successfully processed Payload.");
    } catch (GenericLogdataBatchPointsFactoryException exception) {
      logger.error(
          "Caught GenericLogdataBatchPointsFactoryException while preparing data for insertion into DB",
          exception);
      throw new MessagingException(
          "Caught GenericLogdataBatchPointsFactoryException while preparing data for insertion into DB",
          exception);
    }
  }
}
