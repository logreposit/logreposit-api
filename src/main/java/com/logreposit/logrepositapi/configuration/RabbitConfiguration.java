package com.logreposit.logrepositapi.configuration;

import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMqMessageRecoverer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;

@EnableRabbit
public class RabbitConfiguration
{
    private static final Logger logger = LoggerFactory.getLogger(RabbitConfiguration.class);

//    @Bean
//    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
//        return new Jackson2JsonMessageConverter(objectMapper);
//    }

//    @Bean
//    public RabbitRetryTemplateCustomizer rabbitRetryTemplateCustomizer() {
//        return (target, retryTemplate) -> {
//            retryTemplate.setRetryPolicy(new NeverRetryPolicy());
//        };
//    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RabbitMqMessageRecoverer(rabbitTemplate);
    }

    @Bean(RabbitListenerAnnotationBeanPostProcessor.DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        var factory = new SimpleRabbitListenerContainerFactory();

        configurer.configure(factory, connectionFactory);

        factory.setAfterReceivePostProcessors(message -> {
            logger.info("Received RabbitMQ message: {}", message);

            return message;
        });

        return factory;
    }
}
