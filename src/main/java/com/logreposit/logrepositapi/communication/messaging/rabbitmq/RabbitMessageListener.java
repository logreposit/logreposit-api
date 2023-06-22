package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.handler.MessageHandler;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class RabbitMessageListener {
  private static final Logger logger = LoggerFactory.getLogger(RabbitMessageListener.class);

  private final MessageHandler messageHandler;

  public RabbitMessageListener(MessageHandler messageHandler) {
    this.messageHandler = messageHandler;
  }

  @RabbitListener(
      queuesToDeclare =
          @Queue(value = "${logreposit.queue-name:q.logreposit_api}", durable = "true"))
  public void listen(@Payload Message message) throws MessagingException {
    setCorrelationId(message);

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
}
