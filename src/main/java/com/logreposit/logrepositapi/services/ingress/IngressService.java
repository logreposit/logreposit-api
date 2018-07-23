package com.logreposit.logrepositapi.services.ingress;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.DeviceType;

public interface IngressService
{
    void processData(Device device, DeviceType deviceType, Object data) throws IngressServiceException;
}
