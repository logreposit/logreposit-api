package com.logreposit.logrepositapi.communication.messaging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
    }

    private static Object sampleObject()
    {
        Map<String, Object> hashMap = new HashMap<>();

        hashMap.put("date", new Date());

        return hashMap;
    }
}
