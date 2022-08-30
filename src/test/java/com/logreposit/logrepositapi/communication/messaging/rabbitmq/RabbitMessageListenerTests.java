package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.handler.MessageHandler;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
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

  @Mock private MessageHandler messageHandler;

  @Captor private ArgumentCaptor<Message> messageCaptor;

  private RabbitMessageListener rabbitMessageListener;

  @BeforeEach
  public void setUp() {
    this.rabbitMessageListener = new RabbitMessageListener(this.messageHandler);
  }

  @Test
  public void testListen() throws MessagingException {
    Message message = givenMessage();

    this.rabbitMessageListener.listen(message);

    assertThat(RequestCorrelation.getCorrelationId()).isEqualTo(CORRELATION_ID);
    verify(this.messageHandler, times(1)).handle(this.messageCaptor.capture());

    Message capturedMessage = this.messageCaptor.getValue();

    assertThat(capturedMessage).isNotNull();
    assertThat(capturedMessage).isSameAs(message);
  }

  private static Message givenMessage() {
    MessageMetaData messageMetaData = new MessageMetaData();

    messageMetaData.setCorrelationId(CORRELATION_ID);

    Message message = new Message();

    message.setMetaData(messageMetaData);

    return message;
  }
}
