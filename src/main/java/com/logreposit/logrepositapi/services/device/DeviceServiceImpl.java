package com.logreposit.logrepositapi.services.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.dtos.DeviceCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.repositories.DeviceRepository;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import com.logreposit.logrepositapi.utils.definition.DefinitionUpdateUtil;
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
    private final MessageFactory        messageFactory;
    private final RabbitMessageSender   messageSender;

    public DeviceServiceImpl(DeviceRepository deviceRepository,
                             DeviceTokenRepository deviceTokenRepository,
                             MessageFactory messageFactory,
                             RabbitMessageSender messageSender)
    {
        this.deviceRepository      = deviceRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.messageFactory        = messageFactory;
        this.messageSender         = messageSender;
    }

    @Override
    public Device create(Device device, String userEmail) throws DeviceServiceException
    {
        Device      createdDevice      = this.deviceRepository.save(device);
        DeviceToken deviceToken        = buildDeviceToken(createdDevice.getId());
        DeviceToken createdDeviceToken = this.deviceTokenRepository.save(deviceToken);

        this.publishDeviceCreated(createdDevice, userEmail);

        logger.info("Successfully created device: {} with device token: {}", createdDevice, createdDeviceToken);

        return createdDevice;
    }

    @Override
    public Page<Device> list(String userId, Integer page, Integer size)
    {
        PageRequest  pageRequest = PageRequest.of(page, size);
        Page<Device> devices     = this.deviceRepository.findByUserId(userId, pageRequest);

        return devices;
    }

    @Override
    public Device get(String deviceId) throws DeviceNotFoundException
    {
        Optional<Device> device = this.deviceRepository.findById(deviceId);

        if (device.isEmpty())
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

        if (device.isEmpty())
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

        if (deviceToken.isEmpty())
        {
            logger.error("device token {} not found in database.", token);
            throw new DeviceTokenNotFoundException("device token not found.", token);
        }

        Optional<Device> device = this.deviceRepository.findById(deviceToken.get().getDeviceId());

        if (device.isEmpty())
        {
            logger.error("could not find Device that belongs to device token {}.", token);
            throw new DeviceNotFoundException("Device for given device token not found.");
        }

        return device.get();
    }

    @Override
    public void checkIfExistent(String deviceId) throws DeviceNotFoundException
    {
        long count = this.deviceRepository.countById(deviceId);

        if (count < 1)
        {
            logger.error("could not find device with id {}.", deviceId);
            throw new DeviceNotFoundException("could not find device with id");
        }
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

    @Override
    public DeviceDefinition updateDefinition(String deviceId, DeviceDefinition definition) throws DeviceServiceException
    {
        Device device = this.get(deviceId);

        if (definition.equals(device.getDefinition()))
        {
            logger.info("New definition for Device with ID '{}' is equal to the already existing one, skipping update.", deviceId);

            return definition;
        }

        DeviceDefinition updatedDefinition = DefinitionUpdateUtil.updateDefinition(device.getDefinition(), definition);

        device.setDefinition(updatedDefinition);

        Device savedDevice = this.deviceRepository.save(device);

        return savedDevice.getDefinition();
    }

    private static DeviceToken buildDeviceToken(String deviceId)
    {
        DeviceToken deviceToken = new DeviceToken();

        deviceToken.setToken(UUID.randomUUID().toString());
        deviceToken.setDeviceId(deviceId);
        deviceToken.setCreatedAt(new Date());

        return deviceToken;
    }

    private void publishDeviceCreated(Device device, String userEmail) throws DeviceServiceException
    {
        try
        {
            DeviceCreatedMessageDto deviceCreatedMessageDto = createDeviceCreatedMessageDto(device);
            Message                 deviceCreatedMessage    = this.messageFactory.buildEventDeviceCreatedMessage(deviceCreatedMessageDto, device.getUserId(), userEmail);

            this.messageSender.send(deviceCreatedMessage);
        }
        catch (JsonProcessingException e)
        {
            logger.error("Unable to create deviceCreatedMessage: {}", LoggingUtils.getLogForException(e));
            throw new DeviceServiceException("Unable to create deviceCreatedMessage", e);
        }
        catch (MessageSenderException e)
        {
            logger.error("Unable to send deviceCreatedMessage: {}", LoggingUtils.getLogForException(e));
            throw new DeviceServiceException("Unable to send deviceCreatedMessage", e);
        }
    }

    private static DeviceCreatedMessageDto createDeviceCreatedMessageDto(Device device)
    {
        DeviceCreatedMessageDto deviceCreatedMessageDto = new DeviceCreatedMessageDto();

        deviceCreatedMessageDto.setId(device.getId());
        deviceCreatedMessageDto.setName(device.getName());

        return deviceCreatedMessageDto;
    }
}
