package com.logreposit.logrepositapi.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class LoggingUtils {
  private static final String DEFAULT = "<NOT_SERIALIZABLE>";

  private LoggingUtils() {}

  public static String serialize(Object object) {
    final var objectMapper = createObjectMapper();

    try {
      return objectMapper.writeValueAsString(object);
    } catch (JacksonException e) {
      return DEFAULT;
    }
  }

  private static ObjectMapper createObjectMapper() {
    return new ObjectMapper();
  }
}
