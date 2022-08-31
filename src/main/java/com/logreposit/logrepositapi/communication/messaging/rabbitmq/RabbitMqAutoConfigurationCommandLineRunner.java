package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqAutoConfigurationCommandLineRunner implements CommandLineRunner {
  private static final Logger logger =
      LoggerFactory.getLogger(RabbitMqAutoConfigurationCommandLineRunner.class);

  private static final List<MessageType> SUBSCRIBED_MESSAGE_TYPES =
      List.of(MessageType.EVENT_GENERIC_LOGDATA_RECEIVED);

  private final ApplicationConfiguration applicationConfiguration;
  private final AmqpAdmin amqpAdmin;

  public RabbitMqAutoConfigurationCommandLineRunner(
      ApplicationConfiguration applicationConfiguration, AmqpAdmin amqpAdmin) {
    this.applicationConfiguration = applicationConfiguration;
    this.amqpAdmin = amqpAdmin;
  }

  @Override
  public void run(String... args) {
    this.configureRabbit();
  }

  private void configureRabbit() {
    this.declareQueue();

    this.declareErrorExchangeAndQueueAndBinding();
    this.declareRetryExchangesQueuesAndBindings();

    this.declareExchanges();
    this.declareBindings();
  }

  private void declareErrorExchangeAndQueueAndBinding() {
    Exchange errorExchange =
        ExchangeBuilder.directExchange(RabbitRetryStrategy.ERROR_EXCHANGE_NAME)
            .durable(true)
            .build();

    this.amqpAdmin.declareExchange(errorExchange);

    String queueName = this.applicationConfiguration.getQueueName();
    String errorQueueName = "error." + queueName;

    Queue errorQueue = QueueBuilder.durable(errorQueueName).build();

    this.amqpAdmin.declareQueue(errorQueue);

    this.declareBinding(errorQueueName, RabbitRetryStrategy.ERROR_EXCHANGE_NAME, queueName);
  }

  private void declareRetryExchangesQueuesAndBindings() {
    for (Integer retryInterval : this.applicationConfiguration.getMessageRetryIntervals()) {
      String retryExchangeName = RabbitRetryStrategy.getExchangeNameForRetryInterval(retryInterval);
      Exchange retryExchange = this.declareFanoutExchange(retryExchangeName);
      String retryQueueName = RabbitRetryStrategy.getRetryQueueName(retryInterval);
      Queue retryQueue =
          QueueBuilder.durable(retryQueueName)
              .withArgument("x-dead-letter-exchange", "")
              .withArgument("x-message-ttl", retryInterval)
              .build();

      this.amqpAdmin.declareQueue(retryQueue);

      Binding binding = BindingBuilder.bind(retryQueue).to(retryExchange).with("").noargs();

      this.amqpAdmin.declareBinding(binding);
    }
  }

  private void declareExchanges() {
    for (MessageType messageType : MessageType.values()) {
      String exchangeName = String.format("x.%s", messageType.toString().toLowerCase());

      this.declareFanoutExchange(exchangeName);
    }
  }

  private Exchange declareFanoutExchange(String exchangeName) {
    Exchange exchange = ExchangeBuilder.fanoutExchange(exchangeName).durable(true).build();

    logger.warn("Declaring exchange '{}' ...", exchangeName);

    this.amqpAdmin.declareExchange(exchange);

    logger.warn("Declared exchange '{}'.", exchangeName);

    return exchange;
  }

  private void declareQueue() {
    String queueName = this.applicationConfiguration.getQueueName();
    Queue queue = new Queue(queueName, true);

    logger.warn("declaring queue '{}' ...", queueName);

    this.amqpAdmin.declareQueue(queue);

    logger.warn("declared queue '{}'.", queueName);
  }

  private void declareBindings() {
    final var queueName = this.applicationConfiguration.getQueueName();

    SUBSCRIBED_MESSAGE_TYPES.stream()
        .map(t -> String.format("x.%s", t.toString().toLowerCase()))
        .forEach(x -> declareBinding(queueName, x, ""));
  }

  private void declareBinding(String queueName, String exchangeName, String routingKey) {
    Binding binding =
        new Binding(
            queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, new HashMap<>());

    logger.info("Declaring binding {} == ({}) ==> {} ...", exchangeName, routingKey, queueName);

    this.amqpAdmin.declareBinding(binding);

    logger.info("Declared binding {} => {}.", exchangeName, queueName);
  }
}
