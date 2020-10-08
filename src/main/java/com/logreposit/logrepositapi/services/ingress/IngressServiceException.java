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
}
