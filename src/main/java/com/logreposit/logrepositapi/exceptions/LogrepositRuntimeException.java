package com.logreposit.logrepositapi.exceptions;

public abstract class LogrepositRuntimeException extends RuntimeException {
  public LogrepositRuntimeException(String message) {
    super(message);
  }

  public LogrepositRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
