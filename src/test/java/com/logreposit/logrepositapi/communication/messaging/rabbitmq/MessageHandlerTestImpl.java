package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.handler.AbstractMessageHandler;
import com.logreposit.logrepositapi.communication.messaging.processors.AbstractMessageProcessor;
import java.util.Map;

public class MessageHandlerTestImpl extends AbstractMessageHandler {
  public MessageHandlerTestImpl(Map<MessageType, AbstractMessageProcessor<?>> messageProcessors) {
    super(messageProcessors);
  }
}
