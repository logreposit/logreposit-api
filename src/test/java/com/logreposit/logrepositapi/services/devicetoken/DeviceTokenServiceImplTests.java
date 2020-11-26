package com.logreposit.logrepositapi.services.devicetoken;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
public class DeviceTokenServiceImplTests
{
    @MockBean
    private DeviceTokenRepository deviceTokenRepository;

    @MockBean
    private DeviceService deviceService;

    @Captor
    private ArgumentCaptor<DeviceToken> deviceTokenArgumentCaptor;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

    private DeviceTokenServiceImpl deviceTokenService;

    @BeforeEach
    public void setUp()
    {
        this.deviceTokenService = new DeviceTokenServiceImpl(this.deviceTokenRepository, this.deviceService);
    }

    @Test
    public void testList() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setId(UUID.randomUUID().toString());
        deviceToken.setDeviceId(deviceId);
        deviceToken.setToken(UUID.randomUUID().toString());
        deviceToken.setCreatedAt(new Date());

        List<DeviceToken> deviceTokens = Collections.singletonList(deviceToken);

        Mockito.when(this.deviceTokenRepository.findByDeviceId(Mockito.eq(deviceId))).thenReturn(deviceTokens);

        List<DeviceToken> result = this.deviceTokenService.list(deviceId);

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(deviceTokens);

        Mockito.verify(this.deviceService, Mockito.times(1)).checkIfExistent(Mockito.eq(deviceId));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).findByDeviceId(Mockito.eq(deviceId));
    }

    @Test
    public void testList_noSuchDevice() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();

        Mockito.doThrow(new DeviceNotFoundException("")).when(this.deviceService).checkIfExistent(Mockito.eq(deviceId));

        assertThrows(DeviceNotFoundException.class, () -> this.deviceTokenService.list(deviceId));
    }

    @Test
    public void testCreate() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();

        Mockito.when(this.deviceTokenRepository.save(Mockito.any())).thenAnswer(i -> {
            DeviceToken firstArgument = (DeviceToken) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        DeviceToken createdDeviceToken = this.deviceTokenService.create(deviceId);

        assertThat(createdDeviceToken).isNotNull();
        assertThat(createdDeviceToken.getId()).isNotNull();

        Mockito.verify(this.deviceService, Mockito.times(1)).checkIfExistent(Mockito.eq(deviceId));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).save(this.deviceTokenArgumentCaptor.capture());

        DeviceToken capturedDeviceToken = this.deviceTokenArgumentCaptor.getValue();

        assertThat(capturedDeviceToken).isNotNull();
        assertThat(capturedDeviceToken.getDeviceId()).isEqualTo(deviceId);
        assertThat(capturedDeviceToken.getToken()).isNotNull();
        assertThat(capturedDeviceToken.getCreatedAt()).isNotNull();
        assertThat(capturedDeviceToken.getId()).isNotNull();
    }

    @Test
    public void testCreate_noSuchDevice() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();

        Mockito.doThrow(new DeviceNotFoundException("")).when(this.deviceService).checkIfExistent(Mockito.eq(deviceId));

        assertThrows(DeviceNotFoundException.class, () -> this.deviceTokenService.create(deviceId));
    }

    @Test
    public void testCreate_withDeviceId() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        Mockito.when(this.deviceTokenRepository.save(Mockito.any())).thenAnswer(i -> {
            DeviceToken firstArgument = (DeviceToken) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        DeviceToken createdDeviceToken = this.deviceTokenService.create(deviceId, userId);

        assertThat(createdDeviceToken).isNotNull();
        assertThat(createdDeviceToken.getId()).isNotNull();

        Mockito.verify(this.deviceService, Mockito.times(1)).checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).save(this.deviceTokenArgumentCaptor.capture());

        DeviceToken capturedDeviceToken = this.deviceTokenArgumentCaptor.getValue();

        assertThat(capturedDeviceToken).isNotNull();
        assertThat(capturedDeviceToken.getDeviceId()).isEqualTo(deviceId);
        assertThat(capturedDeviceToken.getToken()).isNotNull();
        assertThat(capturedDeviceToken.getCreatedAt()).isNotNull();
        assertThat(capturedDeviceToken.getId()).isNotNull();
    }

    @Test
    public void testCreate_withDeviceId_noSuchDevice() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        Mockito.doThrow(new DeviceNotFoundException("")).when(this.deviceService).checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));

        assertThrows(DeviceNotFoundException.class, () -> this.deviceTokenService.create(deviceId, userId));
    }

    @Test
    public void testList_withDeviceId() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setId(UUID.randomUUID().toString());
        deviceToken.setDeviceId(deviceId);
        deviceToken.setToken(UUID.randomUUID().toString());
        deviceToken.setCreatedAt(new Date());

        Page<DeviceToken> deviceTokens = new PageImpl<>(Collections.singletonList(deviceToken));

        Mockito.when(this.deviceTokenRepository.findByDeviceId(Mockito.eq(deviceId), Mockito.any(PageRequest.class))).thenReturn(deviceTokens);

        int page = 2;
        int size = 12;

        Page<DeviceToken> result = this.deviceTokenService.list(deviceId, userId, page, size);

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(deviceTokens);

        Mockito.verify(this.deviceService, Mockito.times(1)).checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).findByDeviceId(Mockito.eq(deviceId), this.pageRequestArgumentCaptor.capture());

        PageRequest capturedPageRequest = this.pageRequestArgumentCaptor.getValue();

        assertThat(capturedPageRequest).isNotNull();
        assertThat(capturedPageRequest.getPageNumber()).isEqualTo(page);
        assertThat(capturedPageRequest.getPageSize()).isEqualTo(size);
    }

    @Test
    public void testList_withDeviceId_noSuchDevice() throws DeviceNotFoundException
    {
        String deviceId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        Mockito.doThrow(new DeviceNotFoundException("")).when(this.deviceService).checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));

        assertThrows(DeviceNotFoundException.class, () -> this.deviceTokenService.list(deviceId, userId, 2, 3));
    }

    @Test
    public void testGet() throws DeviceNotFoundException, DeviceTokenNotFoundException
    {
        String deviceTokenId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();

        DeviceToken existentDeviceToken = new DeviceToken();
        existentDeviceToken.setId(deviceTokenId);
        existentDeviceToken.setToken(UUID.randomUUID().toString());
        existentDeviceToken.setDeviceId(deviceId);
        existentDeviceToken.setCreatedAt(new Date());

        Mockito.when(this.deviceTokenRepository.findByIdAndDeviceId(Mockito.eq(deviceTokenId), Mockito.eq(deviceId))).thenReturn(Optional.of(existentDeviceToken));

        DeviceToken result = this.deviceTokenService.get(deviceTokenId, deviceId, userId);

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(existentDeviceToken);

        Mockito.verify(this.deviceService, Mockito.times(1)).checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).findByIdAndDeviceId(Mockito.eq(deviceTokenId), Mockito.eq(deviceId));
    }

    @Test
    public void testGet_noSuchDevice() throws DeviceNotFoundException
    {
        String deviceTokenId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();

        Mockito.doThrow(new DeviceNotFoundException("")).when(this.deviceService).checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));

        assertThrows(DeviceNotFoundException.class, () -> this.deviceTokenService.get(deviceTokenId, deviceId, userId));
    }

    @Test
    public void testGet_noSuchToken()
    {
        String deviceTokenId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();

        Mockito.when(this.deviceTokenRepository.findByIdAndDeviceId(Mockito.eq(deviceTokenId), Mockito.eq(deviceId))).thenReturn(Optional.empty());

        assertThrows(DeviceTokenNotFoundException.class, () -> this.deviceTokenService.get(deviceTokenId, deviceId, userId));
    }

    @Test
    public void testDelete() throws DeviceNotFoundException, DeviceTokenNotFoundException
    {
        String deviceTokenId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();

        DeviceToken existentDeviceToken = new DeviceToken();
        existentDeviceToken.setId(deviceTokenId);
        existentDeviceToken.setToken(UUID.randomUUID().toString());
        existentDeviceToken.setDeviceId(deviceId);
        existentDeviceToken.setCreatedAt(new Date());

        Mockito.when(this.deviceTokenRepository.findByIdAndDeviceId(Mockito.eq(deviceTokenId), Mockito.eq(deviceId))).thenReturn(Optional.of(existentDeviceToken));

        DeviceToken result = this.deviceTokenService.delete(deviceTokenId, deviceId, userId);

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(existentDeviceToken);

        Mockito.verify(this.deviceService, Mockito.times(1)).checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).findByIdAndDeviceId(Mockito.eq(deviceTokenId), Mockito.eq(deviceId));
        Mockito.verify(this.deviceTokenRepository, Mockito.times(1)).delete(Mockito.same(existentDeviceToken));
    }

    @Test
    public void testDelete_noSuchDevice() throws DeviceNotFoundException
    {
        String deviceTokenId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();

        Mockito.doThrow(new DeviceNotFoundException("")).when(this.deviceService).checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));

        assertThrows(DeviceNotFoundException.class, () -> this.deviceTokenService.delete(deviceTokenId, deviceId, userId));
    }

    @Test
    public void testDelete_noSuchToken()
    {
        String deviceTokenId = UUID.randomUUID().toString();
        String deviceId      = UUID.randomUUID().toString();
        String userId        = UUID.randomUUID().toString();

        Mockito.when(this.deviceTokenRepository.findByIdAndDeviceId(Mockito.eq(deviceTokenId), Mockito.eq(deviceId))).thenReturn(Optional.empty());

        assertThrows(DeviceTokenNotFoundException.class, () -> this.deviceTokenService.delete(deviceTokenId, deviceId, userId));
    }
}
