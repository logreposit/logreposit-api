package com.logreposit.logrepositapi.exceptions;

public class LogrepositException extends Exception
{
    public LogrepositException()
    {
    }

    public LogrepositException(String message)
    {
        super(message);
    }

    public LogrepositException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
