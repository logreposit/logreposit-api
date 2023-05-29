package com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class MosquittoDynSecClientException extends LogrepositRuntimeException {
  public MosquittoDynSecClientException(String message, Throwable cause) {
    super(message, cause);
  }
}