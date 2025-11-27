package com.logreposit.logrepositapi.services.ingress;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.configuration.MessagingRetryConfiguration;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.utils.RetryTemplateFactory;
import com.logreposit.logrepositapi.utils.definition.DefinitionValidator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryException;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;

@Service
public class IngressService {
  private static final Logger logger = LoggerFactory.getLogger(IngressService.class);

  private final MessagingRetryConfiguration messagingRetryConfiguration;
  private final RabbitMessageSender messageSender;
  private final MessageFactory messageFactory;

  public IngressService(
      MessagingRetryConfiguration messagingRetryConfiguration,
      RabbitMessageSender messageSender,
      MessageFactory messageFactory) {
    this.messagingRetryConfiguration = messagingRetryConfiguration;
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
    } catch (JacksonException e) {
      logger.error("Unable to create Log Data Received Message", e);

      throw new IngressServiceException("Unable to create Log Data Received Message", e);
    }
  }

  private void sendMessage(Message message) throws IngressServiceException {
    final var maxAttempts = this.messagingRetryConfiguration.getMessageSenderRetryCount();

    final var retryTemplate =
        RetryTemplateFactory.createWithExponentialBackOffForAllExceptions(
            maxAttempts,
            this.messagingRetryConfiguration.getMessageSenderRetryInitialBackOffInterval(),
            this.messagingRetryConfiguration.getMessageSenderBackOffMultiplier());

    final var retryCounter = new AtomicInteger(0);

    try {
      retryTemplate.execute(
          () -> {
            logger.info(
                "Try {}/{}: Sending message {}",
                retryCounter.getAndIncrement() + 1,
                maxAttempts,
                message.getType());

            this.messageSender.send(message);

            return null;
          });
    } catch (RetryException e) {
      logger.error(
          "Could not send Message of type {} because of {} (retry count {})",
          message.getType(),
          e.getCause().getClass().getSimpleName(),
          e.getRetryCount(),
          e);

      throw new IngressServiceException("Could not send Message", e);
    }
  }
}
