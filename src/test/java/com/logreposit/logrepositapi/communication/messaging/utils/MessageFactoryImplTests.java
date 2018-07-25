package com.logreposit.logrepositapi.communication.messaging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.dtos.DeviceCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.dtos.UserCreatedMessageDto;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ObjectMapper.class})
public class MessageFactoryImplTests
{
    @Autowired
    private ObjectMapper objectMapper;

    private MessageFactoryImpl messageFactory;

    @Before
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

        Assert.assertNotNull(message);
        Assert.assertEquals(MessageType.EVENT_CMI_LOGDATA_RECEIVED.toString(), message.getType());
        Assert.assertNotNull(message.getId());
        Assert.assertNotNull(message.getDate());
        Assert.assertNotNull(message.getMetaData());
        Assert.assertNotNull(message.getPayload());

        Assert.assertEquals(correlationId, message.getMetaData().getCorrelationId());
        Assert.assertEquals(deviceId, message.getMetaData().getDeviceId());
        Assert.assertEquals(userId, message.getMetaData().getUserId());
        Assert.assertEquals(this.objectMapper.writeValueAsString(sampleObject), message.getPayload());
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

        Assert.assertNotNull(message);
        Assert.assertNotNull(message.getId());
        Assert.assertNotNull(message.getDate());
        Assert.assertNotNull(message.getType());
        Assert.assertNotNull(message.getMetaData());
        Assert.assertNotNull(message.getPayload());

        Assert.assertEquals(MessageType.EVENT_USER_CREATED.toString(), message.getType());
        Assert.assertEquals(correlationId, message.getMetaData().getCorrelationId());
        Assert.assertEquals(this.objectMapper.writeValueAsString(userCreatedMessageDto), message.getPayload());
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

        Assert.assertNotNull(message);
        Assert.assertNotNull(message.getId());
        Assert.assertNotNull(message.getDate());
        Assert.assertNotNull(message.getType());
        Assert.assertNotNull(message.getMetaData());
        Assert.assertNotNull(message.getPayload());

        Assert.assertEquals(MessageType.EVENT_DEVICE_CREATED.toString(), message.getType());
        Assert.assertEquals(userId, message.getMetaData().getUserId());
        Assert.assertEquals(userEmail, message.getMetaData().getUserEmail());
        Assert.assertEquals(deviceCreatedMessageDto.getId(), message.getMetaData().getDeviceId());
        Assert.assertEquals(correlationId, message.getMetaData().getCorrelationId());
        Assert.assertEquals(this.objectMapper.writeValueAsString(deviceCreatedMessageDto), message.getPayload());
    }

    private static Object sampleObject()
    {
        Map<String, Object> hashMap = new HashMap<>();

        hashMap.put("date", new Date());

        return hashMap;
    }
}
