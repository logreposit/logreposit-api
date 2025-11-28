package com.logreposit.logrepositapi.communication.messaging.processors;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

public abstract class AbstractMessageProcessor<T> {
  private static final Logger logger = LoggerFactory.getLogger(AbstractMessageProcessor.class);

  private final ObjectMapper objectMapper;

  public AbstractMessageProcessor(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public abstract void processMessage(Message message) throws MessagingException;

  protected T getMessagePayload(Message message, TypeReference<T> typeReference)
      throws MessagingException {
    try {
      return this.objectMapper.readValue(message.getPayload(), typeReference);
    } catch (JacksonException exception) {
      logger.error(
          "Unable to deserialize Message payload to instance of '{}'.", typeReference.toString());
      throw new MessagingException(
          String.format("Unable to deserialize Message payload to instance of '%s'", typeReference),
          exception);
    }
  }
}
