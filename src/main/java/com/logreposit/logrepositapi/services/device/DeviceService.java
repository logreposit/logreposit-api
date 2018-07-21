package com.logreposit.logrepositapi.services.device;

import com.logreposit.logrepositapi.persistence.documents.Device;
import org.springframework.data.domain.Page;

public interface DeviceService
{
    Device       create           (Device device);
    Page<Device> list             (String userId, int page, int size);

    Device       get              (String deviceId)                throws DeviceNotFoundException;
    Device       get              (String deviceId, String userId) throws DeviceNotFoundException;
    Device       delete           (String deviceId, String userId) throws DeviceNotFoundException;
    Device       getByDeviceToken (String apiKey)                  throws DeviceTokenNotFoundException, DeviceNotFoundException;
}
