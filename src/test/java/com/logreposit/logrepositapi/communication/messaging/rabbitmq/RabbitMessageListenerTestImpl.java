package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

public class RabbitMessageListenerTestImpl
    extends AbstractRabbitMessageListener<MessageHandlerTestImpl> {
  public RabbitMessageListenerTestImpl(MessageHandlerTestImpl messageHandler) {
    super(messageHandler);
  }
}
