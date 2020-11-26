package com.logreposit.logrepositapi.communication.messaging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.dtos.DeviceCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.dtos.UserCreatedMessageDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.FloatFieldDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.TagDto;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ObjectMapper.class})
public class MessageFactoryImplTests
{
    @Autowired
    private ObjectMapper objectMapper;

    private MessageFactoryImpl messageFactory;

    @BeforeEach
    public void setUp()
    {
        this.messageFactory = new MessageFactoryImpl(this.objectMapper);
    }

    @Test
    public void testBuildEventCmiLogdataReceivedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        Object sampleObject  = sampleObject();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventCmiLogdataReceivedMessage(sampleObject, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_CMI_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleObject));
    }

    @Test
    public void testBuildEventBMV600LogdataReceivedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        Object sampleObject  = sampleObject();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventBMV600LogdataReceivedMessage(sampleObject, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_BMV_600_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleObject));
    }

    @Test
    public void testBuildEventLacrosseTXLogdataReceivedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        Object sampleObject  = sampleObject();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventLacrosseTXLogdataReceivedMessage(sampleObject, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_LACROSSE_TX_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleObject));
    }

    @Test
    public void testBuildEventSolarLogLogdataReceivedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        Object sampleObject  = sampleObject();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventSolarLogLogdataReceivedMessage(sampleObject, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_SOLARLOG_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleObject));
    }

    @Test
    public void testBuildEventFroelingS3200LogdataReceivedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        Object sampleObject  = sampleObject();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventFroelingS3200LogdataReceivedMessage(sampleObject, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_FROELING_LAMBDATRONIC_S3200_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleObject));
    }

    @Test
    public void testBuildEventCotekSPSeriesLogdataReceivedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        Object sampleObject  = sampleObject();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventCotekSPSeriesLogdataReceivedMessage(sampleObject, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_COTEK_SP_SERIES_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleObject));
    }

    @Test
    public void testBuildEventCCS811LogdataReceivedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        Object sampleObject  = sampleObject();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventCCS811LogdataReceivedMessage(sampleObject, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_CCS811_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleObject));
    }

    @Test
    public void testBuildEventDHTLogdataReceivedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        Object sampleObject  = sampleObject();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventDHTLogdataReceivedMessage(sampleObject, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_DHT_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleObject));
    }

    @Test
    public void testBuildEventGenericLogdataReceivedMessage() throws JsonProcessingException
    {
        String           correlationId  = UUID.randomUUID().toString();
        String           deviceId       = UUID.randomUUID().toString();
        String           userId         = UUID.randomUUID().toString();
        List<ReadingDto> sampleReadings = sampleReadings();

        RequestCorrelation.setCorrelationId(correlationId);

        Message message = this.messageFactory.buildEventGenericLogdataReceivedMessage(sampleReadings, deviceId, userId);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_GENERIC_LOGDATA_RECEIVED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(sampleReadings));
    }

    @Test
    public void testBuildEventUserCreatedMessage() throws JsonProcessingException
    {
        String                correlationId         = UUID.randomUUID().toString();
        UserCreatedMessageDto userCreatedMessageDto = new UserCreatedMessageDto();

        RequestCorrelation.setCorrelationId(correlationId);

        userCreatedMessageDto.setId(UUID.randomUUID().toString());
        userCreatedMessageDto.setEmail("some.name@localhost");
        userCreatedMessageDto.setPassword("awesomePassword123");
        userCreatedMessageDto.setRoles(Collections.singletonList(UserRoles.USER));

        Message message = this.messageFactory.buildEventUserCreatedMessage(userCreatedMessageDto);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_USER_CREATED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(userCreatedMessageDto));
    }

    @Test
    public void testBuildEventDeviceCreatedMessage() throws JsonProcessingException
    {
        String correlationId = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();
        String userEmail     = UUID.randomUUID().toString();

        RequestCorrelation.setCorrelationId(correlationId);

        DeviceCreatedMessageDto deviceCreatedMessageDto = new DeviceCreatedMessageDto();

        deviceCreatedMessageDto.setId(UUID.randomUUID().toString());
        deviceCreatedMessageDto.setName(UUID.randomUUID().toString());

        Message message = this.messageFactory.buildEventDeviceCreatedMessage(deviceCreatedMessageDto, userId, userEmail);

        assertThat(message).isNotNull();
        assertThat(message.getType()).isEqualTo(MessageType.EVENT_DEVICE_CREATED.toString());
        assertThat(message.getId()).isNotBlank();
        assertThat(message.getMetaData()).isNotNull();
        assertThat(message.getPayload()).isNotBlank();

        assertThat(message.getMetaData().getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMetaData().getUserId()).isEqualTo(userId);
        assertThat(message.getMetaData().getUserEmail()).isEqualTo(userEmail);
        assertThat(message.getMetaData().getDeviceId()).isEqualTo(deviceCreatedMessageDto.getId());
        assertThat(message.getPayload()).isEqualTo(this.objectMapper.writeValueAsString(deviceCreatedMessageDto));
    }

    private static List<ReadingDto> sampleReadings()
    {
        FloatFieldDto temperatureField = new FloatFieldDto();

        temperatureField.setName("temperature");
        temperatureField.setValue(19.74);

        TagDto locationTag = new TagDto();

        locationTag.setName("location");
        locationTag.setValue("b112_312b");

        TagDto sensorIdTag = new TagDto();

        sensorIdTag.setName("sensor_id");
        sensorIdTag.setValue("0x14402");

        List<TagDto> tags = Arrays.asList(locationTag, sensorIdTag);

        ReadingDto readingDto = new ReadingDto();

        readingDto.setDate(Instant.now());
        readingDto.setMeasurement("data");
        readingDto.setTags(tags);
        readingDto.setFields(Collections.singletonList(temperatureField));

        return Collections.singletonList(readingDto);
    }

    private static Object sampleObject()
    {
        Map<String, Object> hashMap = new HashMap<>();

        hashMap.put("date", new Date());

        return hashMap;
    }
}
