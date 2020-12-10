package com.logreposit.logrepositapi.exceptions;

public abstract class LogrepositRuntimeException extends RuntimeException
{
    public LogrepositRuntimeException(String message)
    {
        super(message);
    }
}
