package com.logreposit.logrepositapi.utils.definition;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class DefinitionValidationException extends LogrepositRuntimeException {
  public DefinitionValidationException(String message) {
    super(message);
  }
}
