package com.logreposit.logrepositapi.services.devices;

public class DeviceNotFoundException extends DeviceServiceException
{
    public DeviceNotFoundException(String message)
    {
        super(message);
    }

    public DeviceNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
