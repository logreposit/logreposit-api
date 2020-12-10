package com.logreposit.logrepositapi.services.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.dtos.DeviceCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.documents.definition.DataType;
import com.logreposit.logrepositapi.persistence.documents.definition.DeviceDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.FieldDefinition;
import com.logreposit.logrepositapi.persistence.documents.definition.MeasurementDefinition;
import com.logreposit.logrepositapi.persistence.repositories.DeviceRepository;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
public class DeviceServiceImplTests
{
    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private DeviceTokenRepository deviceTokenRepository;

    @MockBean
    private MessageFactory messageFactory;

    @MockBean
    private RabbitMessageSender messageSender;

    @Captor
    private ArgumentCaptor<DeviceToken> deviceTokenArgumentCaptor;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

    private DeviceServiceImpl deviceService;

    @BeforeEach
    public void setUp()
    {
        this.deviceService = new DeviceServiceImpl(this.deviceRepository, this.deviceTokenRepository, this.messageFactory, this.messageSender);
    }

    @Test
    public void testCreate() throws DeviceServiceException, JsonProcessingException, MessageSenderException
    {
        String email = "admin@localhost";

        Device device = new Device();
        device.setUserId(UUID.randomUUID().toString());
        device.setName("some_name_1");

        Device createdDevice = new Device();
        createdDevice.setId(UUID.randomUUID().toString());
        createdDevice.setUserId(device.getUserId());
        createdDevice.setName(device.getName());

        Mockito.when(this.deviceRepository.save(Mockito.same(device))).thenReturn(createdDevice);

        Mockito.when(this.deviceTokenRepository.save(Mockito.any())).thenAnswer(i -> {
            DeviceToken firstArgument = (DeviceToken) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        Message deviceCreatedMessage = new Message();

        Mockito.when(this.messageFactory.buildEventDeviceCreatedMessage(Mockito.any(), Mockito.eq(device.getUserId()), Mockito.eq(email))).thenReturn(deviceCreatedMessage);

        Device result = this.deviceService.create(device, email);

        Mockito.verify(this.deviceRepository, Mockito.times(1)).save(Mockito.same(device));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).save(this.deviceTokenArgumentCaptor.capture());

        DeviceToken capturedDeviceToken = this.deviceTokenArgumentCaptor.getValue();

        assertThat(capturedDeviceToken).isNotNull();
        assertThat(capturedDeviceToken.getDeviceId()).isEqualTo(createdDevice.getId());
        assertThat(result).isSameAs(createdDevice);

        ArgumentCaptor<DeviceCreatedMessageDto> deviceCreatedMessageDtoArgumentCaptor = ArgumentCaptor.forClass(DeviceCreatedMessageDto.class);

        Mockito.verify(this.messageFactory, Mockito.times(1))
               .buildEventDeviceCreatedMessage(deviceCreatedMessageDtoArgumentCaptor.capture(), Mockito.eq(device.getUserId()), Mockito.eq(email));

        Mockito.verify(this.messageSender, Mockito.times(1)).send(Mockito.same(deviceCreatedMessage));
    }

    @Test
    public void testGet() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();

        Device existentDevice = new Device();
        existentDevice.setId(deviceId);
        existentDevice.setUserId(UUID.randomUUID().toString());
        existentDevice.setName("some_name_9");

        Mockito.when(this.deviceRepository.findById(Mockito.eq(deviceId))).thenReturn(Optional.of(existentDevice));

        Device result = this.deviceService.get(deviceId);

        assertThat(result).isNotNull();

        Mockito.verify(this.deviceRepository, Mockito.times(1)).findById(Mockito.eq(deviceId));

        assertThat(result).isSameAs(existentDevice);
    }

    @Test
    public void testGet_noSuchDevice()
    {
        String deviceId = UUID.randomUUID().toString();

        Mockito.when(this.deviceRepository.findById(Mockito.eq(deviceId))).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> this.deviceService.get(deviceId));
    }

    @Test
    public void testGetByDeviceToken() throws DeviceTokenNotFoundException, DeviceNotFoundException
    {
        String deviceToken = UUID.randomUUID().toString();

        Device existentDevice = new Device();
        existentDevice.setId(UUID.randomUUID().toString());
        existentDevice.setUserId(UUID.randomUUID().toString());
        existentDevice.setName("some_device_91");

        DeviceToken existentDeviceToken = new DeviceToken();
        existentDeviceToken.setId(UUID.randomUUID().toString());
        existentDeviceToken.setToken(deviceToken);
        existentDeviceToken.setDeviceId(existentDevice.getId());
        existentDeviceToken.setCreatedAt(new Date());

        Mockito.when(this.deviceTokenRepository.findByToken(Mockito.eq(deviceToken))).thenReturn(Optional.of(existentDeviceToken));
        Mockito.when(this.deviceRepository.findById(Mockito.eq(existentDevice.getId()))).thenReturn(Optional.of(existentDevice));

        Device result = this.deviceService.getByDeviceToken(deviceToken);

        assertThat(result).isNotNull();

        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).findByToken(Mockito.eq(deviceToken));
        Mockito.verify(this.deviceRepository, Mockito.times(1)).findById(Mockito.eq(existentDevice.getId()));

        assertThat(result).isSameAs(existentDevice);
    }

    @Test
    public void testGetByDeviceToken_noSuchToken()
    {
        String deviceToken = UUID.randomUUID().toString();

        Mockito.when(this.deviceTokenRepository.findByToken(Mockito.eq(deviceToken))).thenReturn(Optional.empty());

        assertThrows(DeviceTokenNotFoundException.class, () -> this.deviceService.getByDeviceToken(deviceToken));
    }

    @Test
    public void testGetByDeviceToken_noSuchDevice()
    {
        String deviceToken = UUID.randomUUID().toString();

        DeviceToken existentDeviceToken = new DeviceToken();
        existentDeviceToken.setId(UUID.randomUUID().toString());
        existentDeviceToken.setToken(deviceToken);
        existentDeviceToken.setDeviceId(UUID.randomUUID().toString());
        existentDeviceToken.setCreatedAt(new Date());

        Mockito.when(this.deviceTokenRepository.findByToken(Mockito.eq(deviceToken))).thenReturn(Optional.of(existentDeviceToken));
        Mockito.when(this.deviceRepository.findById(Mockito.eq(existentDeviceToken.getDeviceId()))).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> this.deviceService.getByDeviceToken(deviceToken));
    }

    @Test
    public void testGet_withUserId() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        Device existentDevice = new Device();
        existentDevice.setId(deviceId);
        existentDevice.setUserId(UUID.randomUUID().toString());
        existentDevice.setName("some_name_9");

        Mockito.when(this.deviceRepository.findByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId))).thenReturn(Optional.of(existentDevice));

        Device result = this.deviceService.get(deviceId, userId);

        assertThat(result).isNotNull();

        Mockito.verify(this.deviceRepository, Mockito.times(1)).findByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId));

        assertThat(result).isSameAs(existentDevice);
    }

    @Test
    public void testGet_withUserId_noSuchDevice()
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        Device existentDevice = new Device();
        existentDevice.setId(deviceId);
        existentDevice.setUserId(UUID.randomUUID().toString());
        existentDevice.setName("some_name_9");

        Mockito.when(this.deviceRepository.findByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId))).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> this.deviceService.get(deviceId, userId));
    }

    @Test
    public void testDelete_withUserId() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        Device existentDevice = new Device();
        existentDevice.setId(deviceId);
        existentDevice.setUserId(UUID.randomUUID().toString());
        existentDevice.setName("some_name_9");

        Mockito.when(this.deviceRepository.findByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId))).thenReturn(Optional.of(existentDevice));

        Device result = this.deviceService.delete(deviceId, userId);

        assertThat(result).isNotNull();

        Mockito.verify(this.deviceRepository, Mockito.times(1)).findByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).deleteByDeviceId(Mockito.eq(deviceId));
        Mockito.verify(this.deviceRepository, Mockito.times(1)).delete(Mockito.same(existentDevice));

        assertThat(result).isSameAs(existentDevice);
    }

    @Test
    public void testDelete_withUserId_noSuchDevice()
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        Device existentDevice = new Device();
        existentDevice.setId(deviceId);
        existentDevice.setUserId(UUID.randomUUID().toString());
        existentDevice.setName("some_name_9");

        Mockito.when(this.deviceRepository.findByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId))).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> this.deviceService.delete(deviceId, userId));
    }

    @Test
    public void testList_withUserId()
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        int page = 2;
        int size = 15;

        Device device1 = new Device();
        device1.setId(deviceId);
        device1.setUserId(userId);
        device1.setName("some_name_9");

        Device device2 = new Device();
        device2.setId(deviceId);
        device2.setUserId(userId);
        device2.setName("some_name_8");

        List<Device> devices    = Arrays.asList(device1, device2);
        Page<Device> devicePage = new PageImpl<>(devices);

        Mockito.when(this.deviceRepository.findByUserId(Mockito.eq(userId), Mockito.any(PageRequest.class))).thenReturn(devicePage);

        Page<Device> result = this.deviceService.list(userId, page, size);

        assertThat(result).isNotNull();

        Mockito.verify(this.deviceRepository, Mockito.times(1)).findByUserId(Mockito.eq(userId), this.pageRequestArgumentCaptor.capture());

        PageRequest capturedPageRequest = this.pageRequestArgumentCaptor.getValue();

        assertThat(capturedPageRequest).isNotNull();
        assertThat(capturedPageRequest.getPageNumber()).isEqualTo(page);
        assertThat(capturedPageRequest.getPageSize()).isEqualTo(size);

        assertThat(result).isSameAs(devicePage);
    }

    @Test
    public void testCheckIfExistent() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();

        Mockito.when(this.deviceRepository.countById(Mockito.eq(deviceId))).thenReturn(1L);

        this.deviceService.checkIfExistent(deviceId);

        Mockito.verify(this.deviceRepository, Mockito.times(1)).countById(Mockito.eq(deviceId));
    }

    @Test
    public void testCheckIfExistent_noSuchDevice()
    {
        String deviceId = UUID.randomUUID().toString();

        Mockito.when(this.deviceRepository.countById(Mockito.eq(deviceId))).thenReturn(0L);

        assertThrows(DeviceNotFoundException.class, () -> this.deviceService.checkIfExistent(deviceId));
    }

    @Test
    public void testCheckIfExistent_withUserId() throws DeviceNotFoundException
    {
        String userId   = UUID.randomUUID().toString();
        String deviceId = UUID.randomUUID().toString();

        Mockito.when(this.deviceRepository.countByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId))).thenReturn(1L);

        this.deviceService.checkIfExistent(deviceId, userId);

        Mockito.verify(this.deviceRepository, Mockito.times(1)).countByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId));
    }

    @Test
    public void testCheckIfExistent_withUserId_noSuchDevice()
    {
        String userId   = UUID.randomUUID().toString();
        String deviceId = UUID.randomUUID().toString();

        Mockito.when(this.deviceRepository.countByIdAndUserId(Mockito.eq(deviceId), Mockito.eq(userId))).thenReturn(0L);

        assertThrows(DeviceNotFoundException.class, () -> this.deviceService.checkIfExistent(deviceId, userId));
    }

    @Test
    public void testUpdateDefinition_withAddedFields_expectSuccess() throws DeviceServiceException
    {
        FieldDefinition humidityField = new FieldDefinition();

        humidityField.setName("humididy");
        humidityField.setDatatype(DataType.INTEGER);

        DeviceDefinition newDeviceDefinition = sampleDeviceDefinition();

        newDeviceDefinition.getMeasurements().get(0).getTags().add("sensor_id");
        newDeviceDefinition.getMeasurements().get(0).getFields().add(humidityField);

        String deviceId   = UUID.randomUUID().toString();
        String userId     = UUID.randomUUID().toString();
        String deviceName = "some_name_9";

        Device existentDevice = new Device();

        existentDevice.setId(deviceId);
        existentDevice.setUserId(userId);
        existentDevice.setName(deviceName);
        existentDevice.setDefinition(sampleDeviceDefinition());

        Device savedDevice = new Device();

        savedDevice.setId(deviceId);
        savedDevice.setUserId(userId);
        savedDevice.setName(deviceName);
        savedDevice.setDefinition(newDeviceDefinition);

        Mockito.when(this.deviceRepository.findById(Mockito.eq(deviceId))).thenReturn(Optional.of(existentDevice));
        Mockito.when(this.deviceRepository.save(Mockito.any(Device.class))).thenReturn(savedDevice);

        DeviceDefinition updatedDefinition = this.deviceService.updateDefinition(deviceId, newDeviceDefinition);

        assertThat(updatedDefinition).isNotNull();
        assertThat(updatedDefinition).isEqualTo(newDeviceDefinition);

        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);

        Mockito.verify(this.deviceRepository, Mockito.times(1)).findById(Mockito.eq(deviceId));
        Mockito.verify(this.deviceRepository, Mockito.times(1)).save(deviceArgumentCaptor.capture());

        Device capturedDevice = deviceArgumentCaptor.getValue();

        assertThat(capturedDevice).isNotNull();
        assertThat(capturedDevice.getDefinition()).isEqualTo(newDeviceDefinition);
    }

    @Test
    public void testUpdateDefinition_withSameDefinition_expectSuccessWithoutSave() throws DeviceServiceException
    {
        String deviceId = UUID.randomUUID().toString();

        Device existentDevice = new Device();

        existentDevice.setId(deviceId);
        existentDevice.setUserId(UUID.randomUUID().toString());
        existentDevice.setName("some_name_9");
        existentDevice.setDefinition(sampleDeviceDefinition());

        Mockito.when(this.deviceRepository.findById(Mockito.eq(deviceId))).thenReturn(Optional.of(existentDevice));

        DeviceDefinition newDeviceDefinition = sampleDeviceDefinition();
        DeviceDefinition updatedDefinition   = this.deviceService.updateDefinition(deviceId, newDeviceDefinition);

        Mockito.verify(this.deviceRepository, Mockito.times(1)).findById(Mockito.eq(deviceId));
        Mockito.verify(this.deviceRepository, Mockito.never()).save(Mockito.any(Device.class));

        assertThat(updatedDefinition).isEqualTo(existentDevice.getDefinition());
    }

    private static DeviceDefinition sampleDeviceDefinition()
    {
        FieldDefinition tempField = new FieldDefinition();

        tempField.setName("temperature");
        tempField.setDatatype(DataType.FLOAT);

        MeasurementDefinition measurementDefinition = new MeasurementDefinition();

        measurementDefinition.setName("data");
        measurementDefinition.setTags(new HashSet<>(Collections.singletonList("location")));
        measurementDefinition.setFields(new HashSet<>(Collections.singletonList(tempField)));

        DeviceDefinition deviceDefinition = new DeviceDefinition();

        deviceDefinition.setMeasurements(Collections.singletonList(measurementDefinition));

        return deviceDefinition;
    }
}
