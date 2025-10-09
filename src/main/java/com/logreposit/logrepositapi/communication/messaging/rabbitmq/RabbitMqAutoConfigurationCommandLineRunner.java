package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.configuration.MessagingRetryConfiguration;
import java.util.ArrayList;
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
@Order(2)
public class RabbitMqAutoConfigurationCommandLineRunner implements CommandLineRunner {
  private static final Logger logger =
      LoggerFactory.getLogger(RabbitMqAutoConfigurationCommandLineRunner.class);

  private static final List<MessageType> SUBSCRIBED_MESSAGE_TYPES =
      List.of(MessageType.EVENT_GENERIC_LOGDATA_RECEIVED);

  private final ApplicationConfiguration applicationConfiguration;
  private final MessagingRetryConfiguration messagingRetryConfiguration;
  private final AmqpAdmin amqpAdmin;

  public RabbitMqAutoConfigurationCommandLineRunner(
      ApplicationConfiguration applicationConfiguration,
      MessagingRetryConfiguration messagingRetryConfiguration,
      AmqpAdmin amqpAdmin) {
    this.applicationConfiguration = applicationConfiguration;
    this.messagingRetryConfiguration = messagingRetryConfiguration;
    this.amqpAdmin = amqpAdmin;
  }

  @Override
  public void run(String... args) {
    logger.info("Initializing RabbitMQ configuration (Queues, Exchanges, Retry Logic, ...) ...");

    this.configureRabbit();
  }

  private void configureRabbit() {
    final var queues = resolveQueuesToConfigure();

    this.declareQueues(queues);

    this.declareErrorExchange();

    this.declareErrorQueuesAndErrorQueueBindings(queues);

    this.declareRetryExchangesQueuesAndBindings();

    this.declareExchanges();

    this.declareBindings(queues);
  }

  private List<String> resolveQueuesToConfigure() {
    final var applicationModes = applicationConfiguration.getModes().getEnabled();

    // TODO DoM: beautify later :)
    final var queues = new ArrayList<String>();

    if (applicationModes.contains(ApplicationConfiguration.ApplicationMode.PROCESSOR_INFLUX)) {
      queues.add("q.logreposit_api_influx");
    }

    if (applicationModes.contains(ApplicationConfiguration.ApplicationMode.PROCESSOR_MQTT)) {
      queues.add("q.logreposit_api_mqtt");
    }

    return queues;
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

  private void declareBindings(List<String> queues) {
    // TODO DoM: For now there is only one message in this hardcoded list,
    // TODO DoM: Change that to be more dynamic in the future

    queues.forEach(this::declareBinding);
  }

  private void declareBinding(String queueName) {
    // TODO DoM: For now there is only one message in this hardcoded list,
    // TODO DoM: Change that to be more dynamic in the future

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

  private static String errorQueueName(String queueName) {
    return String.format("error.%s", queueName);
  }
}
