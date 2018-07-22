package com.logreposit.logrepositapi.services.device;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.repositories.DeviceRepository;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceServiceImpl implements DeviceService
{
    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);

    private final DeviceRepository      deviceRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository, DeviceTokenRepository deviceTokenRepository)
    {
        this.deviceRepository      = deviceRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public Device create(Device device)
    {
        Device      createdDevice      = this.deviceRepository.save(device);
        DeviceToken deviceToken        = buildDeviceToken(createdDevice.getId());
        DeviceToken createdDeviceToken = this.deviceTokenRepository.save(deviceToken);

        logger.info("Successfully created device: {} with device token: {}", createdDevice, createdDeviceToken);

        return createdDevice;
    }

    @Override
    public Page<Device> list(String userId, int page, int size)
    {
        PageRequest  pageRequest = PageRequest.of(page, size);
        Page<Device> devices     = this.deviceRepository.findByUserId(userId, pageRequest);

        return devices;
    }

    @Override
    public Device get(String deviceId) throws DeviceNotFoundException
    {
        Optional<Device> device = this.deviceRepository.findById(deviceId);

        if (!device.isPresent())
        {
            logger.error("could not find device with id {}.", deviceId);
            throw new DeviceNotFoundException("could not find device with id");
        }

        return device.get();
    }

    @Override
    public Device get(String deviceId, String userId) throws DeviceNotFoundException
    {
        Optional<Device> device = this.deviceRepository.findByIdAndUserId(deviceId, userId);

        if (!device.isPresent())
        {
            logger.error("could not find device with id {}.", deviceId);
            throw new DeviceNotFoundException("could not find device with id");
        }

        return device.get();
    }

    @Override
    public Device delete(String deviceId, String userId) throws DeviceNotFoundException
    {
        Device device = this.get(deviceId, userId);

        this.deviceTokenRepository.deleteByDeviceId(deviceId);
        this.deviceRepository.delete(device);

        return device;
    }

    @Override
    public Device getByDeviceToken(String token) throws DeviceTokenNotFoundException, DeviceNotFoundException
    {
        Optional<DeviceToken> deviceToken = this.deviceTokenRepository.findByToken(token);

        if (!deviceToken.isPresent())
        {
            logger.error("device token {} not found in database.", token);
            throw new DeviceTokenNotFoundException("device token not found.", token);
        }

        Optional<Device> device = this.deviceRepository.findById(deviceToken.get().getDeviceId());

        if (!device.isPresent())
        {
            logger.error("could not find Device that belongs to device token {}.", token);
            throw new DeviceNotFoundException("Device for given device token not found.");
        }

        return device.get();
    }

    @Override
    public void checkIfExistent(String deviceId, String userId) throws DeviceNotFoundException
    {
        long count = this.deviceRepository.countByIdAndUserId(deviceId, userId);

        if (count < 1)
        {
            logger.error("could not find device with id {}.", deviceId);
            throw new DeviceNotFoundException("could not find device with id");
        }
    }

    private static DeviceToken buildDeviceToken(String deviceId)
    {
        DeviceToken deviceToken = new DeviceToken();

        deviceToken.setToken(UUID.randomUUID().toString());
        deviceToken.setDeviceId(deviceId);
        deviceToken.setCreatedAt(new Date());

        return deviceToken;
    }
}
