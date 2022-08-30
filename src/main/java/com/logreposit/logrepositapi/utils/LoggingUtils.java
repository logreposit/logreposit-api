package com.logreposit.logrepositapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class LoggingUtils {
  private static final String DEFAULT = "<NOT_SERIALIZABLE>";

  private LoggingUtils() {}

  public static String serialize(Object object) {
    final var objectMapper = createObjectMapper();

    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      return DEFAULT;
    }
  }

  public static String getLogForException(Exception exception) {
    final var cls = exception.getClass().getName();
    final var message = exception.getMessage();
    final var stackTrace = ExceptionUtils.getStackTrace(exception);

    return String.format("[%s] %s%n%s", cls, message, stackTrace);
  }

  private static ObjectMapper createObjectMapper() {
    final var objectMapper = new ObjectMapper();

    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    return objectMapper;
  }
}
