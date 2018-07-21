package com.logreposit.logrepositapi.services.device;

import com.logreposit.logrepositapi.persistence.documents.Device;

public interface DeviceService
{
    Device create           (Device device);
    Device get              (String deviceId) throws DeviceNotFoundException;
    Device getByDeviceToken (String apiKey)   throws DeviceTokenNotFoundException, DeviceNotFoundException;
}
