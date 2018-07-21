package com.logreposit.logrepositapi.services.common;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class ApiKeyNotFoundException extends LogrepositException
{
    public ApiKeyNotFoundException(String message)
    {
        super(message);
    }
}
