package com.logreposit.logrepositapi.services.devicetoken;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class DeviceTokenServiceException extends LogrepositException
{
    public DeviceTokenServiceException()
    {
    }

    public DeviceTokenServiceException(String message)
    {
        super(message);
    }

    public DeviceTokenServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DeviceTokenServiceException(Throwable cause)
    {
        super(cause);
    }

    public DeviceTokenServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
