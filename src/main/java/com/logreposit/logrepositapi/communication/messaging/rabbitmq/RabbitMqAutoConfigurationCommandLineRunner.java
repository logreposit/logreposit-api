package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
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
        Queue queue = new Queue(this.applicationConfiguration.getQueueName(), true);

        logger.warn("declaring queue '{}' ...", this.applicationConfiguration.getQueueName());

        String result = this.amqpAdmin.declareQueue(queue);

        logger.warn("declared queue '{}': {}", this.applicationConfiguration.getQueueName(), result);
    }
}
