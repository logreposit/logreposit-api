package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class MqttServiceException extends LogrepositException {
  public MqttServiceException(String message) {
    super(message);
  }

  public MqttServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
