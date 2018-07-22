package com.logreposit.logrepositapi.services.devicetoken;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceTokenServiceImpl implements DeviceTokenService
{
    private static final Logger logger = LoggerFactory.getLogger(DeviceTokenServiceImpl.class);

    private final DeviceTokenRepository deviceTokenRepository;
    private final DeviceService         deviceService;

    public DeviceTokenServiceImpl(DeviceTokenRepository deviceTokenRepository, DeviceService deviceService)
    {
        this.deviceTokenRepository = deviceTokenRepository;
        this.deviceService         = deviceService;
    }

    @Override
    public List<DeviceToken> list(String deviceId) throws DeviceNotFoundException
    {
        this.deviceService.checkIfExistent(deviceId);

        List<DeviceToken> deviceTokens = this.deviceTokenRepository.findByDeviceId(deviceId);

        return deviceTokens;
    }

    @Override
    public DeviceToken create(String deviceId) throws DeviceNotFoundException
    {
        this.deviceService.checkIfExistent(deviceId);

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setCreatedAt(new Date());
        deviceToken.setDeviceId(deviceId);
        deviceToken.setToken(UUID.randomUUID().toString());

        DeviceToken createdDeviceToken = this.deviceTokenRepository.save(deviceToken);

        logger.info("Successfully created new device token: {}", createdDeviceToken);

        return createdDeviceToken;
    }

    @Override
    public DeviceToken create(String deviceId, String userId) throws DeviceNotFoundException
    {
        this.deviceService.checkIfExistent(deviceId, userId);

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setToken(UUID.randomUUID().toString());
        deviceToken.setDeviceId(deviceId);
        deviceToken.setCreatedAt(new Date());

        DeviceToken createdDeviceToken = this.deviceTokenRepository.save(deviceToken);

        logger.info("Successfully created new device-token: {}", deviceToken);

        return createdDeviceToken;
    }

    @Override
    public Page<DeviceToken> list(String deviceId, String userId, Integer page, Integer size) throws DeviceNotFoundException
    {
        this.deviceService.checkIfExistent(deviceId, userId);

        PageRequest       pageRequest  = PageRequest.of(page, size);
        Page<DeviceToken> deviceTokens = this.deviceTokenRepository.findByDeviceId(deviceId, pageRequest);

        return deviceTokens;
    }

    @Override
    public DeviceToken get(String deviceTokenId, String deviceId, String userId) throws DeviceNotFoundException, DeviceTokenNotFoundException
    {
        this.deviceService.checkIfExistent(deviceId, userId);

        Optional<DeviceToken> deviceToken = this.deviceTokenRepository.findByIdAndDeviceId(deviceTokenId, deviceId);

        if (!deviceToken.isPresent())
        {
            logger.error("Could not find device-token with id {}", deviceTokenId);
            throw new DeviceTokenNotFoundException("could not find device-token with id", deviceTokenId);
        }

        return deviceToken.get();
    }

    @Override
    public DeviceToken delete(String deviceTokenId, String deviceId, String userId) throws DeviceNotFoundException, DeviceTokenNotFoundException
    {
        DeviceToken deviceToken = this.get(deviceTokenId, deviceId, userId);

        this.deviceTokenRepository.delete(deviceToken);

        return deviceToken;
    }
}
