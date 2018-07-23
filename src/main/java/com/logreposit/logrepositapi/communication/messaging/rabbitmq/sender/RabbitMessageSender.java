package com.logreposit.logrepositapi.communication.messaging.rabbitmq.sender;

import com.logreposit.logrepositapi.communication.messaging.sender.MessageSender;

import java.util.Map;

public interface RabbitMessageSender extends MessageSender
{
    void send(String exchange, String routingKey, org.springframework.amqp.core.Message message, Map<String, Object> headers);
}
