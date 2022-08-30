package com.logreposit.logrepositapi.services.ingress;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TestJsonProcessingException extends JsonProcessingException {
  public TestJsonProcessingException(String msg) {
    super(msg);
  }
}
