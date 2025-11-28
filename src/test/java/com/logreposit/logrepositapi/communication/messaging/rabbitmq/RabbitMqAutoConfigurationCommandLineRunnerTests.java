package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.configuration.MessagingRetryConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;

@ExtendWith(MockitoExtension.class)
public class RabbitMqAutoConfigurationCommandLineRunnerTests {

  @Mock private MessagingRetryConfiguration messagingRetryConfiguration;

  @Mock private AmqpAdmin amqpAdmin;

  @Test
  public void testRun() {}
}
