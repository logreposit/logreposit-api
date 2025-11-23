package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.handler.InfluxMessageHandler;
import com.logreposit.logrepositapi.communication.messaging.handler.MqttMessageHandler;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RabbitMessageListenerTests {
  private static final String CORRELATION_ID = "correlation-id";

  @Mock private MqttMessageHandler mqttMessageHandler;

  @Mock private InfluxMessageHandler influxMessageHandler;

  @Captor private ArgumentCaptor<Message> messageCaptor;

  private RabbitMessageListener rabbitMessageListener;

  @BeforeEach
  public void setUp() {
    this.rabbitMessageListener =
        new RabbitMessageListener(this.mqttMessageHandler, this.influxMessageHandler);
  }

  @Test
  public void testListen() throws MessagingException {
    final var message = givenMessage();

    this.rabbitMessageListener.listenMqtt(message);

    assertThat(RequestCorrelation.getCorrelationId()).isEqualTo(CORRELATION_ID);
    verify(this.mqttMessageHandler, times(1)).handle(this.messageCaptor.capture());

    final var capturedMessage = this.messageCaptor.getValue();

    assertThat(capturedMessage).isNotNull();
    assertThat(capturedMessage).isSameAs(message);
  }

  private static Message givenMessage() {
    final var messageMetaData = new MessageMetaData();

    messageMetaData.setCorrelationId(CORRELATION_ID);

    final var message = new Message();

    // Just take the first message type
    message.setType(Arrays.stream(MessageType.values()).findFirst().orElseThrow().toString());

    message.setMetaData(messageMetaData);

    return message;
  }
}
