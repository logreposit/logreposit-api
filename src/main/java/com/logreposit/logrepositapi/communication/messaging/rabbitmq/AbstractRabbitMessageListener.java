package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.handler.AbstractMessageHandler;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRabbitMessageListener<T extends AbstractMessageHandler> {
  private final Logger logger;
  private final T messageHandler;

  public AbstractRabbitMessageListener(T messageHandler) {
    this.messageHandler = messageHandler;
    this.logger = LoggerFactory.getLogger(getClass());
  }

  void handleMessage(Message message) throws MessagingException {
    setCorrelationId(message);
    checkIfMessageIsValidOrThrowNotRetryableException(message);

    logger.info("Retrieved message: {} => {}", message.getType(), message.getMetaData());

    this.messageHandler.handle(message);
  }

  private static void setCorrelationId(Message message) {
    if (message.getMetaData() != null
        && StringUtils.isNotEmpty(message.getMetaData().getCorrelationId())) {
      RequestCorrelation.setCorrelationId(message.getMetaData().getCorrelationId());
    } else {
      RequestCorrelation.setCorrelationId(null);
    }
  }

  private static void checkIfMessageIsValidOrThrowNotRetryableException(Message message)
      throws MessagingException {
    if (message == null) {
      throw new MessagingException("Message received was null.");
    }

    if (StringUtils.isBlank(message.getType())) {
      throw new MessagingException("Message received has blank type string.");
    }
  }
}
