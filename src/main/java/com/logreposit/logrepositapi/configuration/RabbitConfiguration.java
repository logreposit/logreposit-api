package com.logreposit.logrepositapi.configuration;

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
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@EnableRabbit
@Configuration
public class RabbitConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(RabbitConfiguration.class);

  /*
      Spring AMQP Retry Features

      Spring AMQP has moved its retry capabilities from Spring Retry to Spring Framework.
      Spring Boot offers a customization hook-point for retry features used by the RetryTemplate and message listeners.
      To make it more explicit, two dedicated customizers have been introduced:
      - RabbitTemplateRetrySettingsCustomizer
      - RabbitListenerRetrySettingsCustomizer.

      If you were using RabbitRetryTemplateCustomizer to customize the retry settings according to a target,
      you will need to migrate to either of those interfaces.
  */

  @Bean
  public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
    return new JacksonJsonMessageConverter(jsonMapper);
  }

  //  @Bean
  //  public RabbitRetryTemplateCustomizer rabbitRetryTemplateCustomizer() {
  //    return (target, retryTemplate) -> retryTemplate.setRetryPolicy(new NeverRetryPolicy());
  //  }

  // TODO DoM: is this needed? (Config before see above)
  @Bean
  public RabbitListenerRetrySettingsCustomizer rabbitListenerRetrySettingsCustomizer() {
    return retrySettings -> {
      retrySettings.setMaxRetries(0L);
    };
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
