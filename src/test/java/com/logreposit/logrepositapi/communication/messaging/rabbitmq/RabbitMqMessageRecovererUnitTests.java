package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
public class RabbitMqMessageRecovererUnitTests {
  private static final String MESSAGE_ERROR_COUNT_HEADER_KEY = "x-error-count";
  private static final String CONSUMER_QUEUE = "q.consumer-queue";

  @Mock private RabbitTemplate rabbitTemplate;

  @Mock private RabbitRetryStrategy rabbitRetryStrategy;

  private RabbitMqMessageRecoverer messageRecoverer;

  @BeforeEach
  public void setUp() {
    doNothing()
        .when(this.rabbitTemplate)
        .convertAndSend(anyString(), anyString(), any(Message.class));

    this.messageRecoverer =
        new RabbitMqMessageRecoverer(this.rabbitTemplate, this.rabbitRetryStrategy);
  }

  @Test
  public void
      testRecover_givenErrorCountNull_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount() {
    this
        .testRecover_givenErrorCount_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount(
            null);
  }

  @Test
  public void
      testRecover_givenErrorCount0_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount() {
    this
        .testRecover_givenErrorCount_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount(
            null);
  }

  @Test
  public void
      testRecover_givenErrorCount42_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount() {
    this
        .testRecover_givenErrorCount_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount(
            42L);
  }

  @Test
  public void
      testRecover_givenErrorCountString_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount() {
    this
        .testRecover_givenErrorCount_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount(
            "someString");
  }

  private void
      testRecover_givenErrorCount_expectGetsExchangeNameFromRetryStrategyAndRepublishesAgainWithUpdatedErrorCount(
          Object messageErrorCount) {
    long expectedErrorCount = 1;

    if (messageErrorCount instanceof Long) {
      expectedErrorCount = (Long) messageErrorCount + 1;
    }

    when(this.rabbitRetryStrategy.getExchange(eq(expectedErrorCount))).thenReturn("x.something");

    var message = givenMessageWithErrorCount(messageErrorCount);

    this.messageRecoverer.recover(message, new Throwable());

    var messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

    verify(this.rabbitRetryStrategy, times(1)).getExchange(eq(expectedErrorCount));
    verify(this.rabbitTemplate, times(1))
        .convertAndSend(eq("x.something"), eq(CONSUMER_QUEUE), messageArgumentCaptor.capture());

    Message capturedMessage = messageArgumentCaptor.getValue();

    assertThat(capturedMessage).isNotNull();

    assertThat(
            (Long) capturedMessage.getMessageProperties().getHeader(MESSAGE_ERROR_COUNT_HEADER_KEY))
        .isEqualTo(expectedErrorCount);
    assertThat(capturedMessage.getMessageProperties().getDeliveryMode())
        .isEqualTo(MessageDeliveryMode.PERSISTENT);
  }

  private static Message givenMessageWithErrorCount(Object errorCount) {
    var messageProperties = new MessageProperties();

    messageProperties.getHeaders().put(MESSAGE_ERROR_COUNT_HEADER_KEY, errorCount);
    messageProperties.setConsumerQueue(CONSUMER_QUEUE);

    byte[] body = {};

    return new Message(body, messageProperties);
  }
}
