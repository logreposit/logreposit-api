package com.logreposit.logrepositapi.services.influxdb.batchpoints;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class BatchPointsFactoryException extends LogrepositException {
  public BatchPointsFactoryException(String message) {
    super(message);
  }
}
