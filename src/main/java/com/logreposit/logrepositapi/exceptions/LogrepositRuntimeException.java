package com.logreposit.logrepositapi.exceptions;

public class LogrepositRuntimeException extends RuntimeException
{
    public LogrepositRuntimeException()
    {
    }

    public LogrepositRuntimeException(String message)
    {
        super(message);
    }

    public LogrepositRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public LogrepositRuntimeException(Throwable cause)
    {
        super(cause);
    }

    public LogrepositRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
