package com.logreposit.logrepositapi.services.devicetoken;

public class DeviceTokenNotFoundException extends DeviceTokenServiceException
{
    public DeviceTokenNotFoundException()
    {
    }

    public DeviceTokenNotFoundException(String message)
    {
        super(message);
    }

    public DeviceTokenNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DeviceTokenNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public DeviceTokenNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
