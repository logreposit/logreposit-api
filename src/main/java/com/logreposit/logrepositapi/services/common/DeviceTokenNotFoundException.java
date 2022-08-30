package com.logreposit.logrepositapi.services.common;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class DeviceTokenNotFoundException extends LogrepositException {
  private final String deviceToken;

  public DeviceTokenNotFoundException(String message, String deviceToken) {
    super(message);

    this.deviceToken = deviceToken;
  }

  public String getDeviceToken() {
    return this.deviceToken;
  }
}
