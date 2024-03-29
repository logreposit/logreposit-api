package com.logreposit.logrepositapi.services.devicetoken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.repositories.DeviceTokenRepository;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class DeviceTokenServiceImplTests {
  @MockBean private DeviceTokenRepository deviceTokenRepository;

  @MockBean private DeviceService deviceService;

  @Captor private ArgumentCaptor<DeviceToken> deviceTokenArgumentCaptor;

  @Captor private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

  private DeviceTokenServiceImpl deviceTokenService;

  @BeforeEach
  public void setUp() {
    this.deviceTokenService =
        new DeviceTokenServiceImpl(this.deviceTokenRepository, this.deviceService);
  }

  @Test
  public void testList() throws DeviceNotFoundException {
    final var deviceId = UUID.randomUUID().toString();

    final var deviceToken = new DeviceToken();

    deviceToken.setId(UUID.randomUUID().toString());
    deviceToken.setDeviceId(deviceId);
    deviceToken.setToken(UUID.randomUUID().toString());
    deviceToken.setCreatedAt(new Date());

    final var deviceTokens = Collections.singletonList(deviceToken);

    Mockito.when(this.deviceTokenRepository.findByDeviceId(Mockito.eq(deviceId)))
        .thenReturn(deviceTokens);

    final var result = this.deviceTokenService.list(deviceId);

    assertThat(result).isNotNull();
    assertThat(result).isSameAs(deviceTokens);

    Mockito.verify(this.deviceService, Mockito.times(1)).checkIfExistent(Mockito.eq(deviceId));
    Mockito.verify(this.deviceTokenRepository, Mockito.times(1))
        .findByDeviceId(Mockito.eq(deviceId));
  }

  @Test
  public void testList_noSuchDevice() throws DeviceNotFoundException {
    final var deviceId = UUID.randomUUID().toString();

    Mockito.doThrow(new DeviceNotFoundException(""))
        .when(this.deviceService)
        .checkIfExistent(Mockito.eq(deviceId));

    assertThrows(DeviceNotFoundException.class, () -> this.deviceTokenService.list(deviceId));
  }

  @Test
  public void testCreate() throws DeviceNotFoundException {
    final var deviceId = UUID.randomUUID().toString();

    Mockito.when(this.deviceTokenRepository.save(Mockito.any()))
        .thenAnswer(
            i -> {
              DeviceToken firstArgument = (DeviceToken) i.getArguments()[0];

              firstArgument.setId(UUID.randomUUID().toString());

              return firstArgument;
            });

    final var createdDeviceToken = this.deviceTokenService.create(deviceId);

    assertThat(createdDeviceToken).isNotNull();
    assertThat(createdDeviceToken.getId()).isNotNull();

    Mockito.verify(this.deviceService, Mockito.times(1)).checkIfExistent(Mockito.eq(deviceId));
    Mockito.verify(this.deviceTokenRepository, Mockito.times(1))
        .save(this.deviceTokenArgumentCaptor.capture());

    final var capturedDeviceToken = this.deviceTokenArgumentCaptor.getValue();

    assertThat(capturedDeviceToken).isNotNull();
    assertThat(capturedDeviceToken.getDeviceId()).isEqualTo(deviceId);
    assertThat(capturedDeviceToken.getToken()).isNotNull();
    assertThat(capturedDeviceToken.getCreatedAt()).isNotNull();
    assertThat(capturedDeviceToken.getId()).isNotNull();
  }

  @Test
  public void testCreate_noSuchDevice() throws DeviceNotFoundException {
    final var deviceId = UUID.randomUUID().toString();

    Mockito.doThrow(new DeviceNotFoundException(""))
        .when(this.deviceService)
        .checkIfExistent(Mockito.eq(deviceId));

    assertThrows(DeviceNotFoundException.class, () -> this.deviceTokenService.create(deviceId));
  }

  @Test
  public void testCreate_withDeviceId() throws DeviceNotFoundException {
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    Mockito.when(this.deviceTokenRepository.save(Mockito.any()))
        .thenAnswer(
            i -> {
              DeviceToken firstArgument = (DeviceToken) i.getArguments()[0];

              firstArgument.setId(UUID.randomUUID().toString());

              return firstArgument;
            });

    final var createdDeviceToken = this.deviceTokenService.create(deviceId, userId);

    assertThat(createdDeviceToken).isNotNull();
    assertThat(createdDeviceToken.getId()).isNotNull();

    Mockito.verify(this.deviceService, Mockito.times(1))
        .checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));
    Mockito.verify(this.deviceTokenRepository, Mockito.times(1))
        .save(this.deviceTokenArgumentCaptor.capture());

    final var capturedDeviceToken = this.deviceTokenArgumentCaptor.getValue();

    assertThat(capturedDeviceToken).isNotNull();
    assertThat(capturedDeviceToken.getDeviceId()).isEqualTo(deviceId);
    assertThat(capturedDeviceToken.getToken()).isNotNull();
    assertThat(capturedDeviceToken.getCreatedAt()).isNotNull();
    assertThat(capturedDeviceToken.getId()).isNotNull();
  }

  @Test
  public void testCreate_withDeviceId_noSuchDevice() throws DeviceNotFoundException {
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    Mockito.doThrow(new DeviceNotFoundException(""))
        .when(this.deviceService)
        .checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));

    assertThrows(
        DeviceNotFoundException.class, () -> this.deviceTokenService.create(deviceId, userId));
  }

  @Test
  public void testList_withDeviceId() throws DeviceNotFoundException {
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    final var deviceToken = new DeviceToken();

    deviceToken.setId(UUID.randomUUID().toString());
    deviceToken.setDeviceId(deviceId);
    deviceToken.setToken(UUID.randomUUID().toString());
    deviceToken.setCreatedAt(new Date());

    final var deviceTokens = new PageImpl<>(Collections.singletonList(deviceToken));

    Mockito.when(
            this.deviceTokenRepository.findByDeviceId(
                Mockito.eq(deviceId), Mockito.any(PageRequest.class)))
        .thenReturn(deviceTokens);

    final int page = 2;
    final int size = 12;

    final var result = this.deviceTokenService.list(deviceId, userId, page, size);

    assertThat(result).isNotNull();
    assertThat(result).isSameAs(deviceTokens);

    Mockito.verify(this.deviceService, Mockito.times(1))
        .checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));
    Mockito.verify(this.deviceTokenRepository, Mockito.times(1))
        .findByDeviceId(Mockito.eq(deviceId), this.pageRequestArgumentCaptor.capture());

    final var capturedPageRequest = this.pageRequestArgumentCaptor.getValue();

    assertThat(capturedPageRequest).isNotNull();
    assertThat(capturedPageRequest.getPageNumber()).isEqualTo(page);
    assertThat(capturedPageRequest.getPageSize()).isEqualTo(size);
  }

  @Test
  public void testList_withDeviceId_noSuchDevice() throws DeviceNotFoundException {
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    Mockito.doThrow(new DeviceNotFoundException(""))
        .when(this.deviceService)
        .checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));

    assertThrows(
        DeviceNotFoundException.class, () -> this.deviceTokenService.list(deviceId, userId, 2, 3));
  }

  @Test
  public void testGet() throws DeviceNotFoundException, DeviceTokenNotFoundException {
    final var deviceTokenId = UUID.randomUUID().toString();
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    final var existentDeviceToken = new DeviceToken();

    existentDeviceToken.setId(deviceTokenId);
    existentDeviceToken.setToken(UUID.randomUUID().toString());
    existentDeviceToken.setDeviceId(deviceId);
    existentDeviceToken.setCreatedAt(new Date());

    Mockito.when(
            this.deviceTokenRepository.findByIdAndDeviceId(
                Mockito.eq(deviceTokenId), Mockito.eq(deviceId)))
        .thenReturn(Optional.of(existentDeviceToken));

    final var result = this.deviceTokenService.get(deviceTokenId, deviceId, userId);

    assertThat(result).isNotNull();
    assertThat(result).isSameAs(existentDeviceToken);

    Mockito.verify(this.deviceService, Mockito.times(1))
        .checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));
    Mockito.verify(this.deviceTokenRepository, Mockito.times(1))
        .findByIdAndDeviceId(Mockito.eq(deviceTokenId), Mockito.eq(deviceId));
  }

  @Test
  public void testGet_noSuchDevice() throws DeviceNotFoundException {
    final var deviceTokenId = UUID.randomUUID().toString();
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    Mockito.doThrow(new DeviceNotFoundException(""))
        .when(this.deviceService)
        .checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));

    assertThrows(
        DeviceNotFoundException.class,
        () -> this.deviceTokenService.get(deviceTokenId, deviceId, userId));
  }

  @Test
  public void testGet_noSuchToken() {
    final var deviceTokenId = UUID.randomUUID().toString();
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    Mockito.when(
            this.deviceTokenRepository.findByIdAndDeviceId(
                Mockito.eq(deviceTokenId), Mockito.eq(deviceId)))
        .thenReturn(Optional.empty());

    assertThrows(
        DeviceTokenNotFoundException.class,
        () -> this.deviceTokenService.get(deviceTokenId, deviceId, userId));
  }

  @Test
  public void testDelete() throws DeviceNotFoundException, DeviceTokenNotFoundException {
    final var deviceTokenId = UUID.randomUUID().toString();
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    final var existentDeviceToken = new DeviceToken();

    existentDeviceToken.setId(deviceTokenId);
    existentDeviceToken.setToken(UUID.randomUUID().toString());
    existentDeviceToken.setDeviceId(deviceId);
    existentDeviceToken.setCreatedAt(new Date());

    Mockito.when(
            this.deviceTokenRepository.findByIdAndDeviceId(
                Mockito.eq(deviceTokenId), Mockito.eq(deviceId)))
        .thenReturn(Optional.of(existentDeviceToken));

    final var result = this.deviceTokenService.delete(deviceTokenId, deviceId, userId);

    assertThat(result).isNotNull();
    assertThat(result).isSameAs(existentDeviceToken);

    Mockito.verify(this.deviceService, Mockito.times(1))
        .checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));
    Mockito.verify(this.deviceTokenRepository, Mockito.times(1))
        .findByIdAndDeviceId(Mockito.eq(deviceTokenId), Mockito.eq(deviceId));
    Mockito.verify(this.deviceTokenRepository, Mockito.times(1))
        .delete(Mockito.same(existentDeviceToken));
  }

  @Test
  public void testDelete_noSuchDevice() throws DeviceNotFoundException {
    final var deviceTokenId = UUID.randomUUID().toString();
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    Mockito.doThrow(new DeviceNotFoundException(""))
        .when(this.deviceService)
        .checkIfExistent(Mockito.eq(deviceId), Mockito.eq(userId));

    assertThrows(
        DeviceNotFoundException.class,
        () -> this.deviceTokenService.delete(deviceTokenId, deviceId, userId));
  }

  @Test
  public void testDelete_noSuchToken() {
    final var deviceTokenId = UUID.randomUUID().toString();
    final var deviceId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    Mockito.when(
            this.deviceTokenRepository.findByIdAndDeviceId(
                Mockito.eq(deviceTokenId), Mockito.eq(deviceId)))
        .thenReturn(Optional.empty());

    assertThrows(
        DeviceTokenNotFoundException.class,
        () -> this.deviceTokenService.delete(deviceTokenId, deviceId, userId));
  }
}
