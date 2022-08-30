package com.logreposit.logrepositapi.services.device;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class DeviceServiceException extends LogrepositException {
  public DeviceServiceException(String message) {
    super(message);
  }

  public DeviceServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
