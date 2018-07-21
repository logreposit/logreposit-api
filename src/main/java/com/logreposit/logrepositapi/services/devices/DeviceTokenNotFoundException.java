package com.logreposit.logrepositapi.services.devices;

public class DeviceTokenNotFoundException extends DeviceServiceException
{
    private final String deviceToken;

    public DeviceTokenNotFoundException(String message, String deviceToken)
    {
        super(message);

        this.deviceToken = deviceToken;
    }

    public String getDeviceToken()
    {
        return this.deviceToken;
    }
}
