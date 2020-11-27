package com.logreposit.logrepositapi.communication.messaging.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessagingException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractMessageProcessor<T>
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageProcessor.class);

    private final ObjectMapper objectMapper;

    public AbstractMessageProcessor(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public abstract void processMessage(Message message) throws MessagingException;

    protected T getMessagePayload(Message message, TypeReference<T> typeReference) throws MessagingException
    {
        try
        {
            T payload = this.objectMapper.readValue(message.getPayload(), typeReference);

            logger.info("Successfully deserialized Message Payload into {} instance: {}", typeReference.toString(), LoggingUtils.serialize(payload));

            return payload;
        }
        catch (IOException exception)
        {
            logger.error("Unable to deserialize Message payload to instance of '{}'.", typeReference.toString());
            throw new MessagingException(String.format("Unable to deserialize Message payload to instance of '%s'", typeReference.toString()), exception);
        }
    }

    protected T getMessagePayload(Message message, Class<T> clazz) throws MessagingException
    {
        try
        {
            T payload = this.objectMapper.reader().forType(clazz).readValue(message.getPayload());

            logger.info("Successfully deserialized Message Payload into {} instance: {}", clazz.toString(), LoggingUtils.serialize(payload));

            return payload;
        }
        catch (IOException exception)
        {
            logger.error("Unable to deserialize Message payload to instance of '{}'.", clazz.toString());
            throw new MessagingException(String.format("Unable to deserialize Message payload to instance of '%s'", clazz.toString()), exception);
        }
    }
}
