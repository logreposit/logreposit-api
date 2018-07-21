package com.logreposit.logrepositapi.services.devices;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class DeviceServiceException extends LogrepositException
{
    public DeviceServiceException()
    {
    }

    public DeviceServiceException(String message)
    {
        super(message);
    }

    public DeviceServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DeviceServiceException(Throwable cause)
    {
        super(cause);
    }

    public DeviceServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
