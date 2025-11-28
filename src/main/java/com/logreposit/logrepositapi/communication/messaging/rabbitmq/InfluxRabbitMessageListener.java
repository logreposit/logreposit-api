package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.handler.InfluxMessageHandler;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.configuration.conditional.ConditionalOnEnabledApplicationMode;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnEnabledApplicationMode(
    mode = ApplicationConfiguration.ApplicationMode.PROCESSOR_INFLUX)
public class InfluxRabbitMessageListener
    extends AbstractRabbitMessageListener<InfluxMessageHandler> {
  public InfluxRabbitMessageListener(InfluxMessageHandler messageHandler) {
    super(messageHandler);
  }

  @RabbitListener(queuesToDeclare = @Queue(value = "q.influxdb_service", durable = "true"))
  public void listenInflux(@Payload Message message) throws MessagingException {
    handleMessage(message);
  }
}
