package com.logreposit.logrepositapi.communication.messaging.handler;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.processors.EventLogdataReceivedMessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTests
{
    @Mock
    private EventLogdataReceivedMessageProcessor eventLogdataReceivedMessageProcessor;

    private MessageHandler messageHandler;

    @BeforeEach
    public void setUp() {
        this.messageHandler = new MessageHandler(this.eventLogdataReceivedMessageProcessor);
    }

    @Test
    public void testHandle_givenValidMessage_expectProcessorIsCalled() throws MessagingException
    {
        Message message = new Message();

        message.setType("EVENT_GENERIC_LOGDATA_RECEIVED");

        this.messageHandler.handle(message);

        verify(this.eventLogdataReceivedMessageProcessor, times(1)).processMessage(eq(message));
    }

    @Test
    public void testHandle_noMessageProcessorImplemented_expectException() {
        Message message = new Message();

        message.setType("EVENT_LACROSSE_TX_LOGDATA_RECEIVED");

        var e = assertThrows(MessagingException.class, () -> this.messageHandler.handle(message));

        assertThat(e).hasMessage("Could not find MessageProcessor for Event of type 'EVENT_LACROSSE_TX_LOGDATA_RECEIVED'");
        assertThat(e).hasNoCause();
    }

    @Test
    public void testHandle_givenMessageWithUnknownMessageType_expectException() {
        Message message = new Message();

        message.setType("EVENT_UNKNOWN_TYPE");

        var e = assertThrows(MessagingException.class, () -> this.messageHandler.handle(message));

        assertThat(e).hasMessage("Could not find appropriate MessageType instance for value 'EVENT_UNKNOWN_TYPE'");
        assertThat(e).hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
