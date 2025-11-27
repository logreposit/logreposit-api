package com.logreposit.logrepositapi.communication.messaging.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.processors.mqtt.MqttEventLogdataReceivedMessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MqttMessageHandlerTests {
  @Mock private MqttEventLogdataReceivedMessageProcessor mqttEventLogdataReceivedMessageProcessor;

  private MqttMessageHandler mqttMessageHandler;

  @BeforeEach
  public void setUp() {
    this.mqttMessageHandler = new MqttMessageHandler(this.mqttEventLogdataReceivedMessageProcessor);
  }

  @Test
  public void testHandle_givenValidMessage_expectProcessorIsCalled() throws MessagingException {
    final var message = new Message();

    message.setType("EVENT_GENERIC_LOGDATA_RECEIVED");

    this.mqttMessageHandler.handle(message);

    verify(this.mqttEventLogdataReceivedMessageProcessor, times(1)).processMessage(eq(message));
  }

  @Test
  public void testHandle_givenMessageWithUnknownMessageType_expectException() {
    final var message = new Message();

    message.setType("EVENT_UNKNOWN_TYPE");

    final var e =
        assertThrows(MessagingException.class, () -> this.mqttMessageHandler.handle(message));

    assertThat(e)
        .hasMessage(
            "Could not find appropriate MessageType instance for value 'EVENT_UNKNOWN_TYPE'");

    assertThat(e).hasCauseInstanceOf(IllegalArgumentException.class);
  }
}
