package com.logreposit.logrepositapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMqMessageRecoverer;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitRetryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.NeverRetryPolicy;

@EnableRabbit
@Configuration
public class RabbitConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(RabbitConfiguration.class);

  @Bean
  public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public RabbitRetryTemplateCustomizer rabbitRetryTemplateCustomizer() {
    return (target, retryTemplate) -> retryTemplate.setRetryPolicy(new NeverRetryPolicy());
  }

  @Bean
  public MessageRecoverer messageRecoverer(
      RabbitTemplate rabbitTemplate, RabbitRetryStrategy rabbitRetryStrategy) {
    return new RabbitMqMessageRecoverer(rabbitTemplate, rabbitRetryStrategy);
  }

  @Bean(
      RabbitListenerAnnotationBeanPostProcessor.DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      SimpleRabbitListenerContainerFactoryConfigurer configurer,
      ConnectionFactory connectionFactory) {
    var factory = new SimpleRabbitListenerContainerFactory();

    configurer.configure(factory, connectionFactory);

    factory.setAfterReceivePostProcessors(
        message -> {
          logger.debug("Received RabbitMQ message: {}", message);

          return message;
        });

    return factory;
  }
}
