package com.logreposit.logrepositapi.services.devicetoken;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import com.logreposit.logrepositapi.services.devices.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.devices.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
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
        this.deviceService.get(deviceId);

        List<DeviceToken> deviceTokens = this.deviceTokenRepository.findByDeviceId(deviceId);

        return deviceTokens;
    }

    @Override
    public DeviceToken create(String deviceId) throws DeviceNotFoundException
    {
        this.deviceService.get(deviceId);

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setCreatedAt(new Date());
        deviceToken.setDeviceId(deviceId);
        deviceToken.setToken(UUID.randomUUID().toString());

        DeviceToken createdDeviceToken = this.deviceTokenRepository.save(deviceToken);

        logger.info("Successfully created new device token: {}", createdDeviceToken);

        return createdDeviceToken;
    }
}
