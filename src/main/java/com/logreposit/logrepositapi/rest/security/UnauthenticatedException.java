package com.logreposit.logrepositapi.rest.security;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class UnauthenticatedException extends LogrepositException
{
    public UnauthenticatedException()
    {
    }

    public UnauthenticatedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
