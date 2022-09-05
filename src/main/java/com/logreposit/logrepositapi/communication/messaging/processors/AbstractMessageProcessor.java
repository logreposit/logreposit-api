package com.logreposit.logrepositapi.communication.messaging.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      T payload = this.objectMapper.readValue(message.getPayload(), typeReference);

      // TODO DoM: maybe get rid of those logs?!
      logger.info(
          "Successfully deserialized Message Payload into {} instance: {}",
          typeReference.toString(),
          LoggingUtils.serialize(payload));

      return payload;
    } catch (IOException exception) {
      logger.error(
          "Unable to deserialize Message payload to instance of '{}'.", typeReference.toString());
      throw new MessagingException(
          String.format("Unable to deserialize Message payload to instance of '%s'", typeReference),
          exception);
    }
  }
}
