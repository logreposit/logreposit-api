package com.logreposit.logrepositapi.communication.messaging.rabbitmq.sender;

import com.logreposit.logrepositapi.communication.messaging.sender.MessageSender;
import org.springframework.amqp.core.Message;

import java.util.Map;

public interface RabbitMessageSender extends MessageSender
{
    void send(String exchange, String routingKey, Message message, Map<String, Object> headers);
}
