package com.logreposit.logrepositapi.services.ingress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import com.logreposit.logrepositapi.utils.RetryTemplateFactory;
import com.logreposit.logrepositapi.utils.definition.DefinitionValidator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IngressService {
  private static final Logger logger = LoggerFactory.getLogger(IngressService.class);

  private final ApplicationConfiguration applicationConfiguration;
  private final RabbitMessageSender messageSender;
  private final MessageFactory messageFactory;

  public IngressService(
      ApplicationConfiguration applicationConfiguration,
      RabbitMessageSender messageSender,
      MessageFactory messageFactory) {
    this.applicationConfiguration = applicationConfiguration;
    this.messageSender = messageSender;
    this.messageFactory = messageFactory;
  }

  public void processData(Device device, List<ReadingDto> readings) throws IngressServiceException {
    DefinitionValidator.forDefinition(device.getDefinition()).validate(readings);

    final var message = this.buildMessage(device, readings);

    this.sendMessage(message);
  }

  private Message buildMessage(Device device, List<ReadingDto> readings)
      throws IngressServiceException {
    try {
      return this.messageFactory.buildEventGenericLogdataReceivedMessage(
          readings, device.getId(), device.getUserId());
    } catch (JsonProcessingException e) {
      logger.error(
          "Unable to create Log Data Received Message: {}", LoggingUtils.getLogForException(e));

      throw new IngressServiceException("Unable to create Log Data Received Message", e);
    }
  }

  private void sendMessage(Message message) throws IngressServiceException {
    final var maxAttempts = this.applicationConfiguration.getMessageSenderRetryCount();

    final var retryTemplate =
        RetryTemplateFactory.createWithExponentialBackOffForAllExceptions(
            maxAttempts,
            this.applicationConfiguration.getMessageSenderRetryInitialBackOffInterval(),
            this.applicationConfiguration.getMessageSenderBackOffMultiplier());

    try {
      retryTemplate.execute(
          retryContext -> {
            logger.info(
                "(Re-)try {}/{}: Sending message {}",
                retryContext.getRetryCount(),
                maxAttempts,
                message.getType());

            this.messageSender.send(message);

            return null;
          });
    } catch (MessageSenderException e) {
      logger.error(
          "Could not send Message of type {}: {}",
          message.getType(),
          LoggingUtils.getLogForException(e));

      throw new IngressServiceException("Could not send Message", e);
    }
  }
}
