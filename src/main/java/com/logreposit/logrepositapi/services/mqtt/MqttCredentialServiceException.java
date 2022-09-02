package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class MqttCredentialServiceException extends LogrepositRuntimeException {
  public MqttCredentialServiceException(String message) {
    super(message);
  }

  public MqttCredentialServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
