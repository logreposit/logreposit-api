package com.logreposit.logrepositapi.utils.definition;

import com.logreposit.logrepositapi.exceptions.LogrepositRuntimeException;

public class DefinitionValidationException extends LogrepositRuntimeException
{
    public DefinitionValidationException()
    {
    }

    public DefinitionValidationException(String message)
    {
        super(message);
    }

    public DefinitionValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DefinitionValidationException(Throwable cause)
    {
        super(cause);
    }

    public DefinitionValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
