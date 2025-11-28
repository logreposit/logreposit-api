package com.logreposit.logrepositapi.services.influxdb;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class InfluxDBServiceException extends LogrepositException {
  public InfluxDBServiceException(String message) {
    super(message);
  }
}
