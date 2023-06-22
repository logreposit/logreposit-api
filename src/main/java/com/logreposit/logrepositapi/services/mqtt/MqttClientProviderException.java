package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class MqttClientProviderException extends LogrepositRuntimeException {
  public MqttClientProviderException(String message) {
    super(message);
  }
}
