package com.logreposit.logrepositapi.communication.messaging.mqtt;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class MosquittoDynSecClientException extends LogrepositRuntimeException {
  public MosquittoDynSecClientException(String message) {
    super(message);
  }
}