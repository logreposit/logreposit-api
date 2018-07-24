package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqAutoConfigurationCommandLineRunner implements CommandLineRunner
{
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqAutoConfigurationCommandLineRunner.class);

    private final ApplicationConfiguration applicationConfiguration;
    private final AmqpAdmin                amqpAdmin;

    public RabbitMqAutoConfigurationCommandLineRunner(ApplicationConfiguration applicationConfiguration, AmqpAdmin amqpAdmin)
    {
        this.applicationConfiguration = applicationConfiguration;
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public void run(String... args)
    {
        this.configureRabbit();
    }

    private void configureRabbit()
    {
        this.declareExchanges();
        this.declareQueue();
        this.declareBindings();
    }

    private void declareExchanges()
    {
        for (MessageType messageType : MessageType.values())
        {
            String   exchangeName = String.format("x.%s", messageType.toString().toLowerCase());
            Exchange exchange     = ExchangeBuilder.fanoutExchange(exchangeName).durable(true).build();

            logger.warn("Declaring exchange '{}' for MessageType '{}' ...", exchangeName, messageType);

            this.amqpAdmin.declareExchange(exchange);

            logger.warn("Declared exchange '{}'.", exchangeName);
        }
    }

    private void declareQueue()
    {
        Queue  queue     = new Queue(this.applicationConfiguration.getQueueName(), true);
        String queueName = this.applicationConfiguration.getQueueName();

        logger.warn("declaring queue '{}' ...", queueName);

        this.amqpAdmin.declareQueue(queue);

        logger.warn("declared queue '{}'.", queueName);
    }

    private void declareBindings()
    {

    }
}
