package com.logreposit.logrepositapi.services.device;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import org.springframework.data.domain.Page;

public interface DeviceService
{
    Device       create           (Device device);
    Page<Device> list             (String userId, Integer page, Integer size);

    Device       get              (String deviceId)                throws DeviceNotFoundException;
    Device       get              (String deviceId, String userId) throws DeviceNotFoundException;
    Device       delete           (String deviceId, String userId) throws DeviceNotFoundException;
    Device       getByDeviceToken (String apiKey)                  throws DeviceTokenNotFoundException, DeviceNotFoundException;

    void         checkIfExistent  (String deviceId)                throws DeviceNotFoundException;
    void         checkIfExistent  (String deviceId, String userId) throws DeviceNotFoundException;
}
