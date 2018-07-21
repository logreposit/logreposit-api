package com.logreposit.logrepositapi.communication.messaging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class MessageFactoryImpl implements MessageFactory
{
    private final ObjectMapper objectMapper;

    public MessageFactoryImpl(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    @Override
    public Message buildEventCmiLogdataReceivedMessage(Object cmiLogData, String deviceId, String userId) throws JsonProcessingException
    {
        MessageMetaData messageMetaData = new MessageMetaData();
        messageMetaData.setDeviceId(deviceId);
        messageMetaData.setUserId(userId);

        Message message = new Message();
        message.setDate(new Date());
        message.setId(UUID.randomUUID().toString());
        message.setType(MessageType.EVENT_CMI_LOGDATA_RECEIVED.toString());
        message.setMetaData(messageMetaData);
        message.setPayload(this.objectMapper.writeValueAsString(cmiLogData));

        return message;
    }
}
