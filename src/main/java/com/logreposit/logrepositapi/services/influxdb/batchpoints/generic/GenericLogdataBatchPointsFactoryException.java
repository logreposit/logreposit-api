package com.logreposit.logrepositapi.services.influxdb.batchpoints.generic;

import com.logreposit.logrepositapi.services.influxdb.batchpoints.BatchPointsFactoryException;

public class GenericLogdataBatchPointsFactoryException extends BatchPointsFactoryException {
  public GenericLogdataBatchPointsFactoryException(String message) {
    super(message);
  }
}
