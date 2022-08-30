package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMessageSender {
  private static final Logger logger = LoggerFactory.getLogger(RabbitMessageSender.class);

  private final ObjectMapper objectMapper;
  private final RabbitTemplate rabbitTemplate;

  public RabbitMessageSender(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
    this.objectMapper = objectMapper;
    this.rabbitTemplate = rabbitTemplate;
  }

  public void send(Message message) throws MessageSenderException {
    String exchange = String.format("x.%s", message.getType().toLowerCase());
    String routingKey = UUID.randomUUID().toString();
    String payload = this.serializeMessage(message);

    var amqpMessage =
        MessageBuilder.withBody(payload.getBytes())
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .build();

    logger.info(
        "Sending message with type '{}' to exchange '{}' with routing key '{}'",
        message.getType(),
        exchange,
        routingKey);

    this.rabbitTemplate.convertAndSend(exchange, routingKey, amqpMessage);
  }

  private String serializeMessage(Message message) throws MessageSenderException {
    try {
      String serialized = this.objectMapper.writeValueAsString(message);

      return serialized;
    } catch (JsonProcessingException exception) {
      logger.error("Unable to serialize Message: {}", LoggingUtils.getLogForException(exception));
      throw new MessageSenderException("Unable to serialize Message", exception);
    }
  }
}
