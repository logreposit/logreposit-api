package com.logreposit.logrepositapi.communication.messaging.rabbitmq.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class RabbitMessageSenderImpl implements RabbitMessageSender
{
    private static final Logger logger = LoggerFactory.getLogger(RabbitMessageSenderImpl.class);

    private final ObjectMapper   objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMessageSenderImpl(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate)
    {
        this.objectMapper   = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(Message message) throws MessageSenderException
    {
        String exchange   = String.format("x.%s", message.getType().toLowerCase());
        String routingKey = UUID.randomUUID().toString();
        String payload    = this.serializeMessage(message);

        logger.info("Sending message with type '{}' to exchange '{}' with routing key '{}'", message.getType(), exchange, routingKey);

        this.rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }

    @Override
    public void send(String exchange, String routingKey, org.springframework.amqp.core.Message message, Map<String, Object> headers)
    {
        this.rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
            m.getMessageProperties().getHeaders().putAll(headers);
            return m;
        });
    }

    private String serializeMessage(Message message) throws MessageSenderException
    {
        try
        {
            String serialized = this.objectMapper.writeValueAsString(message);

            return serialized;
        }
        catch (JsonProcessingException exception)
        {
            logger.error("Unable to serialize Message instance: {}", LoggingUtils.getLogForException(exception));
            throw new MessageSenderException("Unable to serialize Message instance", exception);
        }
    }
}
