package com.logreposit.logrepositapi.services.devicetoken;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.services.devices.DeviceNotFoundException;

import java.util.List;

public interface DeviceTokenService
{
    DeviceToken       create (String deviceId) throws DeviceNotFoundException;
    List<DeviceToken> list   (String deviceId) throws DeviceNotFoundException;
}
