package com.logreposit.logrepositapi.services.apikey;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class ApiKeyServiceException extends LogrepositException
{
    public ApiKeyServiceException()
    {
    }

    public ApiKeyServiceException(String message)
    {
        super(message);
    }

    public ApiKeyServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ApiKeyServiceException(Throwable cause)
    {
        super(cause);
    }

    public ApiKeyServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
