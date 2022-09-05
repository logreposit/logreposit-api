package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.handler.MessageHandler;
import com.logreposit.logrepositapi.configuration.RabbitConfiguration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"logreposit.messageRetryIntervals=100,200,300"})
@Import(RabbitConfiguration.class)
public class MessagingRetryIntegrationTests {
  private static final String MESSAGE_ERROR_COUNT_HEADER_KEY = "x-error-count";

  @Autowired private AmqpAdmin amqpAdmin;

  @Autowired private RabbitMessageSender rabbitMessageSender;

  @SpyBean private RabbitTemplate rabbitTemplate;

  @MockBean private MessageHandler messageHandler;

  @Captor private ArgumentCaptor<org.springframework.amqp.core.Message> messageCaptor;

  @BeforeEach
  public void setUp() {
    this.amqpAdmin.declareExchange(
        ExchangeBuilder.fanoutExchange("x.event_integration_test").build());

    this.amqpAdmin.declareBinding(
        new Binding(
            "q.logreposit_api",
            Binding.DestinationType.QUEUE,
            "x.event_integration_test",
            "",
            new HashMap<>()));

    assertThat(this.amqpAdmin.getQueueProperties("retry.q.100")).isNotNull();
    assertThat(this.amqpAdmin.getQueueProperties("retry.q.200")).isNotNull();
    assertThat(this.amqpAdmin.getQueueProperties("retry.q.300")).isNotNull();
  }

  @Test
  public void testRetry_givenMessageWithUnknownType_expectGetsRetried15TimesAndEndsUpInErrorQueue()
      throws MessagingException {
    doThrow(new MessagingException("oops")).when(this.messageHandler).handle(any());

    final var message = new Message();

    message.setPayload("{}");
    message.setType("EVENT_INTEGRATION_TEST");

    this.rabbitMessageSender.send(message);

    await()
        .atMost(15, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              verify(this.rabbitTemplate, times(1))
                  .convertAndSend(
                      eq("x.event_integration_test"),
                      anyString(),
                      any(org.springframework.amqp.core.Message.class));
              verify(this.rabbitTemplate, times(5))
                  .convertAndSend(
                      eq("retry.x.100"),
                      eq("q.logreposit_api"),
                      any(org.springframework.amqp.core.Message.class));
              verify(this.rabbitTemplate, times(5))
                  .convertAndSend(
                      eq("retry.x.200"),
                      eq("q.logreposit_api"),
                      any(org.springframework.amqp.core.Message.class));
              verify(this.rabbitTemplate, times(5))
                  .convertAndSend(
                      eq("retry.x.300"),
                      eq("q.logreposit_api"),
                      any(org.springframework.amqp.core.Message.class));
              verify(this.rabbitTemplate, times(1))
                  .convertAndSend(
                      eq("error.x"),
                      eq("q.logreposit_api"),
                      any(org.springframework.amqp.core.Message.class));
            });

    verify(this.rabbitTemplate, times(1))
        .convertAndSend(eq("x.event_integration_test"), anyString(), this.messageCaptor.capture());
    verify(this.rabbitTemplate, times(5))
        .convertAndSend(eq("retry.x.100"), eq("q.logreposit_api"), this.messageCaptor.capture());
    verify(this.rabbitTemplate, times(5))
        .convertAndSend(eq("retry.x.200"), eq("q.logreposit_api"), this.messageCaptor.capture());
    verify(this.rabbitTemplate, times(5))
        .convertAndSend(eq("retry.x.300"), eq("q.logreposit_api"), this.messageCaptor.capture());
    verify(this.rabbitTemplate, times(1))
        .convertAndSend(eq("error.x"), eq("q.logreposit_api"), this.messageCaptor.capture());

    final var capturedMessages = this.messageCaptor.getAllValues();

    assertThat(capturedMessages).hasSize(17);
    assertThat(
            (Object)
                capturedMessages
                    .get(0)
                    .getMessageProperties()
                    .getHeader(MESSAGE_ERROR_COUNT_HEADER_KEY))
        .isNull();

    for (int i = 1; i < 17; i++) {
      assertErrorCountIs(capturedMessages.get(i), (long) i);
    }
  }

  private static void assertErrorCountIs(
      org.springframework.amqp.core.Message message, Long expectedCount) {
    final var errorCountHeader =
        message.getMessageProperties().getHeader(MESSAGE_ERROR_COUNT_HEADER_KEY);

    assertThat(errorCountHeader).isNotNull();
    assertThat(errorCountHeader).isInstanceOf(Long.class);
    assertThat(errorCountHeader).isEqualTo(expectedCount);
  }
}
