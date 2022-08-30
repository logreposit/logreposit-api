package com.logreposit.logrepositapi.communication.messaging.handler;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.processors.AbstractMessageProcessor;
import com.logreposit.logrepositapi.communication.messaging.processors.EventLogdataReceivedMessageProcessor;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageHandler {
  private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

  private final Map<MessageType, AbstractMessageProcessor<?>> messageProcessors;

  public MessageHandler(EventLogdataReceivedMessageProcessor eventLogdataReceivedMessageProcessor) {
    this.messageProcessors =
        Map.ofEntries(
            Map.entry(
                MessageType.EVENT_GENERIC_LOGDATA_RECEIVED, eventLogdataReceivedMessageProcessor));
  }

  public void handle(Message message) throws MessagingException {
    var type = getTypeOfMessage(message);
    var processor = this.getMessageProcessor(type);

    processor.processMessage(message);
  }

  private AbstractMessageProcessor<?> getMessageProcessor(MessageType messageType)
      throws MessagingException {
    var messageProcessor = this.messageProcessors.get(messageType);

    if (messageProcessor == null) {
      logger.error("Could not find MessageProcessor for Event of type '{}'", messageType);

      throw new MessagingException(
          String.format("Could not find MessageProcessor for Event of type '%s'", messageType));
    }

    return messageProcessor;
  }

  private static MessageType getTypeOfMessage(Message message) throws MessagingException {
    try {
      return MessageType.valueOf(message.getType());
    } catch (IllegalArgumentException exception) {
      logger.error(
          "Could not find appropriate MessageType instance for value '{}'", message.getType());

      throw new MessagingException(
          String.format(
              "Could not find appropriate MessageType instance for value '%s'", message.getType()),
          exception);
    }
  }
}
