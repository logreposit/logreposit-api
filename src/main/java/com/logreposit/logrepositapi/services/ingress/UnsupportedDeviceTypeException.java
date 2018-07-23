package com.logreposit.logrepositapi.services.ingress;

import com.logreposit.logrepositapi.rest.dtos.DeviceType;

public class UnsupportedDeviceTypeException extends IngressServiceException
{
    private final DeviceType deviceType;

    public UnsupportedDeviceTypeException(DeviceType deviceType)
    {
        this.deviceType = deviceType;
    }

    public UnsupportedDeviceTypeException(String message, DeviceType deviceType)
    {
        super(message);
        this.deviceType = deviceType;
    }

    public DeviceType getDeviceType()
    {
        return this.deviceType;
    }
}
