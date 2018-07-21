package com.logreposit.logrepositapi.services.device;
import java.util.Date;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.repositories.DeviceRepository;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class DeviceServiceImplTests
{
    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private DeviceTokenRepository deviceTokenRepository;

    @Captor
    private ArgumentCaptor<DeviceToken> deviceTokenArgumentCaptor;

    private DeviceServiceImpl deviceService;

    @Before
    public void setUp()
    {
        this.deviceService = new DeviceServiceImpl(this.deviceRepository, this.deviceTokenRepository);
    }

    @Test
    public void testCreate()
    {
        Device device = new Device();
        device.setUserId(UUID.randomUUID().toString());
        device.setName("some_name_1");

        Device createdDevice = new Device();
        device.setId(UUID.randomUUID().toString());
        device.setUserId(device.getUserId());
        device.setName(device.getName());

        Mockito.when(this.deviceRepository.save(Mockito.same(device))).thenReturn(createdDevice);

        Mockito.when(this.deviceTokenRepository.save(Mockito.any())).thenAnswer(i -> {
            DeviceToken firstArgument = (DeviceToken) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        Device result = this.deviceService.create(device);

        Mockito.verify(this.deviceRepository, Mockito.times(1)).save(Mockito.same(device));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).save(this.deviceTokenArgumentCaptor.capture());

        DeviceToken capturedDeviceToken = this.deviceTokenArgumentCaptor.getValue();

        Assert.assertNotNull(capturedDeviceToken);
        Assert.assertEquals(createdDevice.getId(), capturedDeviceToken.getDeviceId());

        Assert.assertSame(result, createdDevice);
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

        Assert.assertNotNull(result);

        Mockito.verify(this.deviceRepository, Mockito.times(1)).findById(Mockito.eq(deviceId));

        Assert.assertSame(existentDevice, result);
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGet_noSuchDevice() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();

        Mockito.when(this.deviceRepository.findById(Mockito.eq(deviceId))).thenReturn(Optional.empty());

        this.deviceService.get(deviceId);
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

        Assert.assertNotNull(result);

        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).findByToken(Mockito.eq(deviceToken));
        Mockito.verify(this.deviceRepository, Mockito.times(1)).findById(Mockito.eq(existentDevice.getId()));

        Assert.assertSame(existentDevice, result);
    }

    @Test(expected = DeviceTokenNotFoundException.class)
    public void testGetByDeviceToken_noSuchToken() throws DeviceTokenNotFoundException, DeviceNotFoundException
    {
        String deviceToken = UUID.randomUUID().toString();

        Mockito.when(this.deviceTokenRepository.findByToken(Mockito.eq(deviceToken))).thenReturn(Optional.empty());

        this.deviceService.getByDeviceToken(deviceToken);
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetByDeviceToken_noSuchDevice() throws DeviceTokenNotFoundException, DeviceNotFoundException
    {
        String deviceToken = UUID.randomUUID().toString();

        DeviceToken existentDeviceToken = new DeviceToken();
        existentDeviceToken.setId(UUID.randomUUID().toString());
        existentDeviceToken.setToken(deviceToken);
        existentDeviceToken.setDeviceId(UUID.randomUUID().toString());
        existentDeviceToken.setCreatedAt(new Date());

        Mockito.when(this.deviceTokenRepository.findByToken(Mockito.eq(deviceToken))).thenReturn(Optional.of(existentDeviceToken));
        Mockito.when(this.deviceRepository.findById(Mockito.eq(existentDeviceToken.getDeviceId()))).thenReturn(Optional.empty());

        this.deviceService.getByDeviceToken(deviceToken);
    }
}
