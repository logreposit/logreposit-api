package com.logreposit.logrepositapi.services.ingress;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class IngressServiceException extends LogrepositException
{
    public IngressServiceException()
    {
    }

    public IngressServiceException(String message)
    {
        super(message);
    }

    public IngressServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IngressServiceException(Throwable cause)
    {
        super(cause);
    }

    public IngressServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
