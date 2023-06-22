package com.logreposit.logrepositapi.services.mqtt.emqx;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class EmqxApiClientException extends LogrepositRuntimeException {
  public EmqxApiClientException(String message) {
    super(message);
  }

  public EmqxApiClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
