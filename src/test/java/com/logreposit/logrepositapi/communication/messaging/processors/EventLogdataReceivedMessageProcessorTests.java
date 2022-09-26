package com.logreposit.logrepositapi.communication.messaging.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.communication.messaging.mqtt.MqttMessageSender;
import com.logreposit.logrepositapi.communication.messaging.mqtt.dtos.IngressV2MqttDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FloatFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.TagDto;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventLogdataReceivedMessageProcessorTests {
  private static final String TEST_CORRELATION_ID = "effcc656-59c9-4064-933a-e434a751eca8";
  private static final String TEST_USER_ID = "f8e9550b-6ca8-4da1-86e5-79df1defd7a1";
  private static final String TEST_DEVICE_ID = "6313e4fd-a056-4dad-8636-9399470f3087";

  private EventLogdataReceivedMessageProcessor eventLogdataReceivedMessageProcessor;

  @Mock private MqttMessageSender mqttMessageSender;

  @Captor private ArgumentCaptor<String> topicArgumentCaptor;
  @Captor private ArgumentCaptor<IngressV2MqttDto> ingressV2MqttDtoArgumentCaptor;

  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());

    this.eventLogdataReceivedMessageProcessor =
        new EventLogdataReceivedMessageProcessor(this.objectMapper, mqttMessageSender);
  }

  @Test
  public void testProcessMessage_givenValidMessage_expectRuntimeException()
      throws MessagingException, JsonProcessingException {
    final var tag = new TagDto();

    tag.setName("location");
    tag.setValue("B112");

    final var field = new FloatFieldDto();

    field.setName("temperature");
    field.setValue(20.003);

    final var reading = new ReadingDto();

    reading.setDate(Instant.ofEpochMilli(1662389834325L));
    reading.setMeasurement("measurementName");
    reading.setTags(List.of(tag));
    reading.setFields(List.of(field));

    final var message = getSampleMessage();

    message.setPayload(objectMapper.writeValueAsString(List.of(reading)));

    this.eventLogdataReceivedMessageProcessor.processMessage(message);

    verify(mqttMessageSender)
        .send(topicArgumentCaptor.capture(), ingressV2MqttDtoArgumentCaptor.capture());

    final var capturedTopic = topicArgumentCaptor.getValue();

    assertThat(capturedTopic)
        .isEqualTo(
            "logreposit/users/f8e9550b-6ca8-4da1-86e5-79df1defd7a1/devices/6313e4fd-a056-4dad-8636-9399470f3087/ingress");

    final var capturedPayload = ingressV2MqttDtoArgumentCaptor.getValue();

    assertThat(capturedPayload).isNotNull();

    assertSoftly(
        softly -> {
          softly.assertThat(capturedPayload.getCorrelationId()).isEqualTo(TEST_CORRELATION_ID);
          softly.assertThat(capturedPayload.getReadings()).hasSize(1);

          final var capturedReading = capturedPayload.getReadings().get(0);

          softly.assertThat(capturedReading).isEqualTo(reading);
        });
  }

  @Test
  public void testProcessMessage_givenWrongPayload_expectRuntimeException() {
    final var message = getSampleMessage();

    message.setPayload("{\"a\": \"b\"}");

    var e =
        assertThrows(
            MessagingException.class,
            () -> this.eventLogdataReceivedMessageProcessor.processMessage(message));

    assertThat(e).hasMessageStartingWith("Unable to deserialize Message payload to instance of");
    assertThat(e).hasCauseInstanceOf(MismatchedInputException.class);
    assertThat(e)
        .hasRootCauseMessage(
            "Cannot deserialize value of type `java.util.ArrayList<com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto>` from Object value (token `JsonToken.START_OBJECT`)\n"
                + " at [Source: (String)\"{\"a\": \"b\"}\"; line: 1, column: 1]");
  }

  @Test
  public void testProcessMessage_givenMissingUserId_expectRuntimeException() throws JsonProcessingException {
    final var tag = new TagDto();

    tag.setName("location");
    tag.setValue("B112");

    final var field = new FloatFieldDto();

    field.setName("temperature");
    field.setValue(20.003);

    final var reading = new ReadingDto();

    reading.setDate(Instant.ofEpochMilli(1662389834325L));
    reading.setMeasurement("measurementName");
    reading.setTags(List.of(tag));
    reading.setFields(List.of(field));

    final var message = getSampleMessage();

    message.setPayload(objectMapper.writeValueAsString(List.of(reading)));
    message.getMetaData().setUserId(null);

    var e =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> this.eventLogdataReceivedMessageProcessor.processMessage(message));

    assertThat(e).hasMessage("userId and deviceId has to be set!");
    assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
  }

  private static Message getSampleMessage() {
    final var messageMetaData = new MessageMetaData();

    messageMetaData.setCorrelationId(TEST_CORRELATION_ID);
    messageMetaData.setUserId(TEST_USER_ID);
    messageMetaData.setDeviceId(TEST_DEVICE_ID);

    final var message = new Message();

    message.setMetaData(messageMetaData);

    return message;
  }
}
