package com.logreposit.logrepositapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class LoggingUtils
{
    private static final String DEFAULT = "<NOT_SERIALIZABLE>";

    private LoggingUtils()
    {
    }

    public static String serialize(Object object)
    {
        ObjectMapper objectMapper = createObjectMapper();

        try
        {
            return objectMapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e)
        {
            return DEFAULT;
        }
    }

    public static String getLogForException(Exception exception)
    {
        String cls = exception.getClass().getName();
        String message = exception.getMessage();
        String stackTrace = ExceptionUtils.getStackTrace(exception);

        String logline = String.format("[%s] %s%n%s", cls, message, stackTrace);
        return logline;
    }

    private static ObjectMapper createObjectMapper()
    {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper;
    }
}
