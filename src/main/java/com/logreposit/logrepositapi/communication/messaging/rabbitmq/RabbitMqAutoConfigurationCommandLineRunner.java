package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.configuration.MessagingRetryConfiguration;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RabbitMqAutoConfigurationCommandLineRunner implements CommandLineRunner {
  private static final Logger logger =
      LoggerFactory.getLogger(RabbitMqAutoConfigurationCommandLineRunner.class);

  private final MessagingRetryConfiguration messagingRetryConfiguration;
  private final AmqpAdmin amqpAdmin;

  public RabbitMqAutoConfigurationCommandLineRunner(
      MessagingRetryConfiguration messagingRetryConfiguration, AmqpAdmin amqpAdmin) {
    this.messagingRetryConfiguration = messagingRetryConfiguration;
    this.amqpAdmin = amqpAdmin;
  }

  @Override
  public void run(String... args) {
    logger.info("Initializing RabbitMQ configuration (Queues, Exchanges, Retry Logic, ...) ...");

    this.configureRabbit();
  }

  private void configureRabbit() {
    // TODO: Rethink Queue / Exchange design.
    // TODO: Keep it like that for now because of backwards compatibility reasons.
    final var queues = List.of("q.logreposit_api", "q.influxdb_service");

    this.declareQueues(queues);

    this.declareErrorExchange();

    this.declareErrorQueuesAndErrorQueueBindings(queues);

    this.declareRetryExchangesQueuesAndBindings();

    this.declareExchanges();

    this.declareBindings();
  }

  private void declareErrorExchange() {
    Exchange errorExchange =
        ExchangeBuilder.directExchange(RabbitRetryStrategy.ERROR_EXCHANGE_NAME)
            .durable(true)
            .build();

    this.amqpAdmin.declareExchange(errorExchange);
  }

  private void declareErrorQueuesAndErrorQueueBindings(List<String> queues) {
    queues.forEach(this::declareErrorQueueAndBinding);
  }

  private void declareErrorQueueAndBinding(String queueName) {
    Queue errorQueue = QueueBuilder.durable(errorQueueName(queueName)).build();

    this.amqpAdmin.declareQueue(errorQueue);

    this.declareErrorExchangeBinding(queueName);
  }

  private void declareErrorExchangeBinding(String queueName) {
    this.declareBinding(
        errorQueueName(queueName), RabbitRetryStrategy.ERROR_EXCHANGE_NAME, queueName);
  }

  private static String errorQueueName(String queueName) {
    return String.format("error.%s", queueName);
  }

  private void declareRetryExchangesQueuesAndBindings() {
    for (Integer retryInterval : this.messagingRetryConfiguration.getMessageRetryIntervals()) {
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

  private void declareQueues(List<String> queueNames) {
    for (String queueName : queueNames) {
      declareQueue(queueName);
    }
  }

  private void declareQueue(String queueName) {
    Queue queue = new Queue(queueName, true);

    logger.warn("declaring queue '{}' ...", queueName);

    this.amqpAdmin.declareQueue(queue);

    logger.warn("declared queue '{}'.", queueName);
  }

  private void declareBindings() {
    // TODO: Rethink Queue / Exchange design.
    // TODO: Currently, this is a hardcoded list (mostly because of backwards compatibility reasons)

    final var routingKey = "";

    declareBinding(
        "q.logreposit_api",
        exchangeNameFor(MessageType.EVENT_GENERIC_LOGDATA_RECEIVED),
        routingKey);

    declareBinding(
        "q.influxdb_service",
        exchangeNameFor(MessageType.EVENT_GENERIC_LOGDATA_RECEIVED),
        routingKey);

    declareBinding(
        "q.influxdb_service", exchangeNameFor(MessageType.EVENT_USER_CREATED), routingKey);

    declareBinding(
        "q.influxdb_service", exchangeNameFor(MessageType.EVENT_DEVICE_CREATED), routingKey);
  }

  private String exchangeNameFor(MessageType messageType) {
    return String.format("x.%s", messageType.toString().toLowerCase());
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
