package com.logreposit.logrepositapi.services.devicetoken;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DeviceTokenServiceImpl implements DeviceTokenService {
  private static final Logger logger = LoggerFactory.getLogger(DeviceTokenServiceImpl.class);

  private final DeviceTokenRepository deviceTokenRepository;
  private final DeviceService deviceService;

  public DeviceTokenServiceImpl(
      DeviceTokenRepository deviceTokenRepository, DeviceService deviceService) {
    this.deviceTokenRepository = deviceTokenRepository;
    this.deviceService = deviceService;
  }

  @Override
  public List<DeviceToken> list(String deviceId) throws DeviceNotFoundException {
    this.deviceService.checkIfExistent(deviceId);

    return this.deviceTokenRepository.findByDeviceId(deviceId);
  }

  @Override
  public DeviceToken create(String deviceId) throws DeviceNotFoundException {
    this.deviceService.checkIfExistent(deviceId);

    final var deviceToken = new DeviceToken();

    deviceToken.setCreatedAt(new Date());
    deviceToken.setDeviceId(deviceId);
    deviceToken.setToken(UUID.randomUUID().toString());

    final var createdDeviceToken = this.deviceTokenRepository.save(deviceToken);

    logger.info("Successfully created new device token: {}", createdDeviceToken);

    return createdDeviceToken;
  }

  @Override
  public DeviceToken create(String deviceId, String userId) throws DeviceNotFoundException {
    this.deviceService.checkIfExistent(deviceId, userId);

    final var deviceToken = new DeviceToken();

    deviceToken.setToken(UUID.randomUUID().toString());
    deviceToken.setDeviceId(deviceId);
    deviceToken.setCreatedAt(new Date());

    DeviceToken createdDeviceToken = this.deviceTokenRepository.save(deviceToken);

    logger.info("Successfully created new device-token: {}", deviceToken);

    return createdDeviceToken;
  }

  @Override
  public Page<DeviceToken> list(String deviceId, String userId, Integer page, Integer size)
      throws DeviceNotFoundException {
    this.deviceService.checkIfExistent(deviceId, userId);

    final var pageRequest = PageRequest.of(page, size);

    return this.deviceTokenRepository.findByDeviceId(deviceId, pageRequest);
  }

  @Override
  public DeviceToken get(String deviceTokenId, String deviceId, String userId)
      throws DeviceNotFoundException, DeviceTokenNotFoundException {
    this.deviceService.checkIfExistent(deviceId, userId);

    final var deviceToken = this.deviceTokenRepository.findByIdAndDeviceId(deviceTokenId, deviceId);

    if (deviceToken.isEmpty()) {
      logger.error("Could not find device-token with id {}", deviceTokenId);

      throw new DeviceTokenNotFoundException("could not find device-token with id", deviceTokenId);
    }

    return deviceToken.get();
  }

  @Override
  public DeviceToken delete(String deviceTokenId, String deviceId, String userId)
      throws DeviceNotFoundException, DeviceTokenNotFoundException {
    final var deviceToken = this.get(deviceTokenId, deviceId, userId);

    this.deviceTokenRepository.delete(deviceToken);

    return deviceToken;
  }
}
