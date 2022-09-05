package com.logreposit.logrepositapi.communication.messaging.mqtt;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class MqttMessageSenderException extends LogrepositRuntimeException {
  public MqttMessageSenderException(String message, Throwable cause) {
    super(message, cause);
  }
}
