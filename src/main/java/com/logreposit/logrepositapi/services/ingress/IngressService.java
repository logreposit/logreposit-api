package com.logreposit.logrepositapi.services.ingress;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.DeviceType;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import java.util.List;

public interface IngressService {
  void processData(Device device, DeviceType deviceType, Object data)
      throws IngressServiceException;

  void processData(Device device, List<ReadingDto> readings) throws IngressServiceException;
}
