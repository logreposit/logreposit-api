package com.logreposit.logrepositapi.services.devicetoken;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import java.util.List;
import org.springframework.data.domain.Page;

public interface DeviceTokenService {
  DeviceToken create(String deviceId) throws DeviceNotFoundException;

  List<DeviceToken> list(String deviceId) throws DeviceNotFoundException;

  DeviceToken create(String deviceId, String userId) throws DeviceNotFoundException;

  Page<DeviceToken> list(String deviceId, String userId, Integer page, Integer size)
      throws DeviceNotFoundException;

  DeviceToken get(String deviceTokenId, String deviceId, String userId)
      throws DeviceNotFoundException, DeviceTokenNotFoundException;

  DeviceToken delete(String deviceTokenId, String deviceId, String userId)
      throws DeviceNotFoundException, DeviceTokenNotFoundException;
}
