package com.logreposit.logrepositapi.services.ingress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import com.logreposit.logrepositapi.rest.dtos.DeviceType;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FloatFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.TagDto;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class IngressServiceImplTests {
  private static final int MESSAGE_SENDER_RETRY_COUNT = 3;
  private static final long MESSAGE_SENDER_INITIAL_BACKOFF_INTERVAL = 10;
  private static final double MESSAGE_SENDER_BACKOFF_MULTIPLIER = 1.1;

  @MockBean private ApplicationConfiguration applicationConfiguration;

  @MockBean private RabbitMessageSender messageSender;

  @MockBean private MessageFactory messageFactory;

  @Captor private ArgumentCaptor<List<ReadingDto>> readingsArgumentCaptor;

  private IngressServiceImpl ingressService;

  @BeforeEach
  public void setUp() {
    this.ingressService =
        new IngressServiceImpl(
            this.applicationConfiguration, this.messageSender, this.messageFactory);

    Mockito.when(this.applicationConfiguration.getMessageSenderRetryCount())
        .thenReturn(MESSAGE_SENDER_RETRY_COUNT);
    Mockito.when(this.applicationConfiguration.getMessageSenderRetryInitialBackOffInterval())
        .thenReturn(MESSAGE_SENDER_INITIAL_BACKOFF_INTERVAL);
    Mockito.when(this.applicationConfiguration.getMessageSenderBackOffMultiplier())
        .thenReturn(MESSAGE_SENDER_BACKOFF_MULTIPLIER);
  }

  @Test
  public void testProcessData()
      throws JsonProcessingException, IngressServiceException, MessageSenderException {
    final var device = getTestDevice();
    final var deviceType = DeviceType.TECHNISCHE_ALTERNATIVE_CMI;
    final var data = getTestData();
    final var message = getTestMessage();

    Mockito.when(
            this.messageFactory.buildEventSolarLogLogdataReceivedMessage(
                Mockito.any(Object.class),
                Mockito.eq(device.getId()),
                Mockito.eq(device.getUserId())))
        .thenReturn(message);

    this.ingressService.processData(device, deviceType, data);

    final var dataArgumentCaptor = ArgumentCaptor.forClass(Object.class);

    Mockito.verify(this.messageFactory, Mockito.times(1))
        .buildEventSolarLogLogdataReceivedMessage(
            dataArgumentCaptor.capture(),
            Mockito.eq(device.getId()),
            Mockito.eq(device.getUserId()));

    Mockito.verify(this.messageSender, Mockito.times(1)).send(Mockito.same(message));
  }

  @Test
  public void testProcessData_givenGenericData_expectSuccess()
      throws JsonProcessingException, IngressServiceException, MessageSenderException {
    final var device = getTestDevice();
    final var readings = sampleReadings();
    final var message = getTestMessage();

    device.setDefinition(sampleDeviceDefinition());

    Mockito.when(
            this.messageFactory.buildEventGenericLogdataReceivedMessage(
                Mockito.any(), Mockito.eq(device.getId()), Mockito.eq(device.getUserId())))
        .thenReturn(message);

    this.ingressService.processData(device, readings);

    Mockito.verify(this.messageFactory, Mockito.times(1))
        .buildEventGenericLogdataReceivedMessage(
            this.readingsArgumentCaptor.capture(),
            Mockito.eq(device.getId()),
            Mockito.eq(device.getUserId()));

    Mockito.verify(this.messageSender, Mockito.times(1)).send(Mockito.same(message));

    final var capturedReadings = this.readingsArgumentCaptor.getValue();

    assertThat(capturedReadings).isEqualTo(readings);
  }

  @Test
  public void testProcessData_unknownDeviceType() {
    final var device = getTestDevice();
    final var deviceType = DeviceType.UNKNOWN;
    final var data = getTestData();

    assertThrows(
        UnsupportedDeviceTypeException.class,
        () -> this.ingressService.processData(device, deviceType, data));
  }

  @Test
  public void testProcessData_throwsJsonProcessingException() throws JsonProcessingException {
    final var device = getTestDevice();
    final var deviceType = DeviceType.TECHNISCHE_ALTERNATIVE_CMI;
    final var data = getTestData();

    Mockito.when(
            this.messageFactory.buildEventSolarLogLogdataReceivedMessage(
                Mockito.any(Object.class),
                Mockito.eq(device.getId()),
                Mockito.eq(device.getUserId())))
        .thenThrow(new TestJsonProcessingException(""));

    var e =
        assertThrows(
            IngressServiceException.class,
            () -> this.ingressService.processData(device, deviceType, data));

    assertThat(e).hasMessage("Unable to create Log Data Received Message");
  }

  @Test
  public void testProcessData_givenGeneric_throwsJsonProcessingException()
      throws JsonProcessingException {
    final var device = getTestDevice();
    final var readings = sampleReadings();

    device.setDefinition(sampleDeviceDefinition());

    Mockito.when(
            this.messageFactory.buildEventGenericLogdataReceivedMessage(
                Mockito.any(), Mockito.eq(device.getId()), Mockito.eq(device.getUserId())))
        .thenThrow(new TestJsonProcessingException(""));

    var e =
        assertThrows(
            IngressServiceException.class, () -> this.ingressService.processData(device, readings));

    assertThat(e).hasMessage("Unable to create Log Data Received Message");
  }

  @Test
  public void testProcessData_sendMessageRetriesExceeded()
      throws JsonProcessingException, MessageSenderException {
    final var device = getTestDevice();
    final var deviceType = DeviceType.TECHNISCHE_ALTERNATIVE_CMI;
    final var data = getTestData();
    final var message = getTestMessage();

    Mockito.when(
            this.messageFactory.buildEventSolarLogLogdataReceivedMessage(
                Mockito.any(Object.class),
                Mockito.eq(device.getId()),
                Mockito.eq(device.getUserId())))
        .thenReturn(message);

    Mockito.doThrow(new MessageSenderException("some error occurred", new RuntimeException()))
        .when(this.messageSender)
        .send(Mockito.eq(message));

    var e =
        assertThrows(
            IngressServiceException.class,
            () -> this.ingressService.processData(device, deviceType, data));

    assertThat(e).hasMessage("Could not send Message");

    Mockito.verify(this.messageFactory, Mockito.times(1))
        .buildEventSolarLogLogdataReceivedMessage(
            Mockito.any(), Mockito.eq(device.getId()), Mockito.eq(device.getUserId()));

    Mockito.verify(this.messageSender, Mockito.times(MESSAGE_SENDER_RETRY_COUNT))
        .send(Mockito.eq(message));
  }

  @Test
  public void testProcessData_givenGeneric_sendMessageRetriesExceeded()
      throws JsonProcessingException, MessageSenderException {
    final var device = getTestDevice();
    final var readings = sampleReadings();
    final var message = getTestMessage();

    device.setDefinition(sampleDeviceDefinition());

    Mockito.when(
            this.messageFactory.buildEventGenericLogdataReceivedMessage(
                Mockito.any(), Mockito.eq(device.getId()), Mockito.eq(device.getUserId())))
        .thenReturn(message);

    Mockito.doThrow(new MessageSenderException("some error occurred", new RuntimeException()))
        .when(this.messageSender)
        .send(Mockito.eq(message));

    var e =
        assertThrows(
            IngressServiceException.class, () -> this.ingressService.processData(device, readings));

    assertThat(e).hasMessage("Could not send Message");

    Mockito.verify(this.messageFactory, Mockito.times(1))
        .buildEventGenericLogdataReceivedMessage(
            Mockito.any(), Mockito.eq(device.getId()), Mockito.eq(device.getUserId()));

    Mockito.verify(this.messageSender, Mockito.times(MESSAGE_SENDER_RETRY_COUNT))
        .send(Mockito.eq(message));
  }

  private static Device getTestDevice() {
    final var device = new Device();

    device.setId(UUID.randomUUID().toString());
    device.setUserId(UUID.randomUUID().toString());
    device.setName(UUID.randomUUID().toString());

    return device;
  }

  private static Object getTestData() {
    final var dataMap = new HashMap<>();

    dataMap.put("date", new Date());

    return dataMap;
  }

  private static DeviceDefinition sampleDeviceDefinition() {
    final var temperatureField = new FieldDefinition();

    temperatureField.setName("temperature");
    temperatureField.setDatatype(DataType.FLOAT);

    final var measurementDefinition = new MeasurementDefinition();

    measurementDefinition.setName("data");
    measurementDefinition.setTags(Set.of("location", "sensor_id"));
    measurementDefinition.setFields(Collections.singleton(temperatureField));

    final var deviceDefinition = new DeviceDefinition();

    deviceDefinition.setMeasurements(Collections.singletonList(measurementDefinition));

    return deviceDefinition;
  }

  private static List<ReadingDto> sampleReadings() {
    final var temperatureField = new FloatFieldDto();

    temperatureField.setName("temperature");
    temperatureField.setValue(19.74);

    final var locationTag = new TagDto();

    locationTag.setName("location");
    locationTag.setValue("b112_312b");

    final var sensorIdTag = new TagDto();

    sensorIdTag.setName("sensor_id");
    sensorIdTag.setValue("0x14402");

    final var tags = Arrays.asList(locationTag, sensorIdTag);

    final var readingDto = new ReadingDto();

    readingDto.setDate(Instant.now());
    readingDto.setMeasurement("data");
    readingDto.setTags(tags);
    readingDto.setFields(Collections.singletonList(temperatureField));

    return Collections.singletonList(readingDto);
  }

  private static Message getTestMessage() {
    final var message = new Message();

    message.setId(UUID.randomUUID().toString());
    message.setDate(new Date());
    message.setType("type");
    message.setMetaData(new MessageMetaData());
    message.setPayload("");

    return message;
  }
}
