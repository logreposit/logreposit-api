package com.logreposit.logrepositapi.communication.messaging.processors.influx;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.dtos.UserCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.processors.AbstractMessageProcessor;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.configuration.conditional.ConditionalOnEnabledApplicationMode;
import com.logreposit.logrepositapi.services.influxdb.InfluxDBService;
import com.logreposit.logrepositapi.services.influxdb.InfluxDBServiceException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnEnabledApplicationMode(
    mode = ApplicationConfiguration.ApplicationMode.PROCESSOR_INFLUX)
public class InfluxEventUserCreatedMessageProcessor
    extends AbstractMessageProcessor<UserCreatedMessageDto> {
  private static final Logger logger =
      LoggerFactory.getLogger(InfluxEventUserCreatedMessageProcessor.class);

  private final InfluxDBService influxDBService;

  @Autowired
  public InfluxEventUserCreatedMessageProcessor(
      ObjectMapper objectMapper, InfluxDBService influxDBService) {
    super(objectMapper);

    this.influxDBService = influxDBService;
  }

  @Override
  public void processMessage(Message message) throws MessagingException {
    final var user = this.getMessagePayload(message, new TypeReference<>() {});

    logger.info("Retrieved created User: {}", LoggingUtils.serialize(user));

    try {
      this.influxDBService.createUser(user.getEmail(), user.getPassword());

      logger.info("Successfully created user.");
    } catch (InfluxDBServiceException exception) {
      logger.error("Caught InfluxDBServiceException while creating user", exception);
      throw new MessagingException(
          "Caught InfluxDBServiceException while creating user", exception);
    }
  }
}
