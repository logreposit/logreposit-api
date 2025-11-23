package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.exceptions.NotRetryableMessagingException;
import com.logreposit.logrepositapi.communication.messaging.handler.InfluxMessageHandler;
import com.logreposit.logrepositapi.communication.messaging.handler.MqttMessageHandler;
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

  private final MqttMessageHandler mqttMessageHandler;
  private final InfluxMessageHandler influxMessageHandler;

  public RabbitMessageListener(
      MqttMessageHandler mqttMessageHandler, InfluxMessageHandler influxMessageHandler) {
    this.mqttMessageHandler = mqttMessageHandler;
    this.influxMessageHandler = influxMessageHandler;
  }

  @RabbitListener(
      queuesToDeclare =
          @Queue(value = "${logreposit.queue-name:q.logreposit_api}", durable = "true"))
  public void listenMqtt(@Payload Message message) throws MessagingException {
    setCorrelationId(message);
    checkIfMessageIsValidOrThrowNotRetryableException(message);

    logger.info("Retrieved message: {} => {}", message.getType(), message.getMetaData());

    this.mqttMessageHandler.handle(message);
  }

  // TODO DoM: conditional on service mode
  @RabbitListener(queuesToDeclare = @Queue(value = "q.influxdb_service}", durable = "true"))
  public void listenInflux(@Payload Message message) throws MessagingException {
    setCorrelationId(message);
    checkIfMessageIsValidOrThrowNotRetryableException(message);

    logger.info(
        "Retrieved (InfluxDB related) message: {} => {}", message.getType(), message.getMetaData());

    this.influxMessageHandler.handle(message);
  }

  private static void setCorrelationId(Message message) {
    if (message.getMetaData() != null
        && StringUtils.isNotEmpty(message.getMetaData().getCorrelationId())) {
      RequestCorrelation.setCorrelationId(message.getMetaData().getCorrelationId());
    } else {
      RequestCorrelation.setCorrelationId(null);
    }
  }

  private static void checkIfMessageIsValidOrThrowNotRetryableException(Message message)
      throws NotRetryableMessagingException {
    if (message == null) {
      throw new NotRetryableMessagingException("Message received was null.");
    }

    if (StringUtils.isBlank(message.getType())) {
      throw new NotRetryableMessagingException("Message received has blank type string.");
    }
  }
}
