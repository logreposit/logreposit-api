package com.logreposit.logrepositapi.communication.messaging.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class EventLogdataReceivedMessageProcessorTests
{
    private EventLogdataReceivedMessageProcessor eventLogdataReceivedMessageProcessor;

    @BeforeEach
    public void setUp() {
        this.eventLogdataReceivedMessageProcessor = new EventLogdataReceivedMessageProcessor(new ObjectMapper());
    }

    @Test
    public void testProcessMessage_givenValidMessage_expectRuntimeException() {

        Message message = getSampleMessage();

        message.setPayload("[]");

        var e = assertThrows(RuntimeException.class, () -> this.eventLogdataReceivedMessageProcessor.processMessage(message));

        assertThat(e).hasMessage("Not yet implemented");
    }

    @Test
    public void testProcessMessage_givenWrongPayload_expectRuntimeException() {

        Message message = getSampleMessage();

        message.setPayload("{\"a\": \"b\"}");

        var e = assertThrows(MessagingException.class, () -> this.eventLogdataReceivedMessageProcessor.processMessage(message));

        assertThat(e).hasMessageStartingWith("Unable to deserialize Message payload to instance of");
        assertThat(e).hasCauseInstanceOf(MismatchedInputException.class);
        assertThat(e).hasRootCauseMessage("Cannot deserialize instance of `java.util.ArrayList<com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto>` out of START_OBJECT token\n at [Source: (String)\"{\"a\": \"b\"}\"; line: 1, column: 1]");
    }

    private static Message getSampleMessage() {
        MessageMetaData messageMetaData = new MessageMetaData();

        messageMetaData.setUserId(UUID.randomUUID().toString());
        messageMetaData.setDeviceId(UUID.randomUUID().toString());

        Message message = new Message();

        message.setMetaData(messageMetaData);

        return message;
    }
}
