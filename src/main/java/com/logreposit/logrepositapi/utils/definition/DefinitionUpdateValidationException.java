package com.logreposit.logrepositapi.utils.definition;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class DefinitionUpdateValidationException extends LogrepositRuntimeException {
  public DefinitionUpdateValidationException(String message) {
    super(message);
  }
}
