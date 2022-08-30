package com.logreposit.logrepositapi.services.apikey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
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
public class ApiKeyServiceImplTests {
  @MockBean private ApiKeyRepository apiKeyRepository;

  @Captor private ArgumentCaptor<ApiKey> apiKeyArgumentCaptor;

  @Captor private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

  private ApiKeyServiceImpl apiKeyService;

  @BeforeEach
  public void setUp() {
    this.apiKeyService = new ApiKeyServiceImpl(this.apiKeyRepository);
  }

  @Test
  public void testList() {
    final var existentUser = new User();

    existentUser.setId(UUID.randomUUID().toString());
    existentUser.setRoles(Collections.singletonList(UserRoles.ADMIN));
    existentUser.setEmail("user@localhost");

    final var apiKey = new ApiKey();

    apiKey.setUserId(existentUser.getId());
    apiKey.setId(UUID.randomUUID().toString());
    apiKey.setCreatedAt(new Date());
    apiKey.setKey(UUID.randomUUID().toString());

    final var existentApiKeys = new PageImpl<>(Collections.singletonList(apiKey));

    Mockito.when(
            this.apiKeyRepository.findByUserId(
                Mockito.eq(existentUser.getId()), Mockito.any(PageRequest.class)))
        .thenReturn(existentApiKeys);

    final int page = 1;
    final int size = 15;

    final var apiKeys = this.apiKeyService.list(existentUser.getId(), page, size);

    assertThat(apiKeys).isNotNull();

    Mockito.verify(this.apiKeyRepository, Mockito.times(1))
        .findByUserId(Mockito.eq(existentUser.getId()), this.pageRequestArgumentCaptor.capture());

    final var capturedPageRequest = this.pageRequestArgumentCaptor.getValue();

    assertThat(capturedPageRequest).isNotNull();
    assertThat(capturedPageRequest.getPageNumber()).isEqualTo(page);
    assertThat(capturedPageRequest.getPageSize()).isEqualTo(size);
    assertThat(apiKeys).isSameAs(existentApiKeys);
  }

  @Test
  public void testCreate() {
    final var user = new User();

    user.setRoles(Collections.singletonList(UserRoles.ADMIN));
    user.setEmail("user@localhost");

    Mockito.when(this.apiKeyRepository.save(Mockito.any()))
        .thenAnswer(
            i -> {
              ApiKey firstArgument = (ApiKey) i.getArguments()[0];

              firstArgument.setId(UUID.randomUUID().toString());

              return firstArgument;
            });

    final var createdKey = this.apiKeyService.create(user.getId());

    assertThat(createdKey).isNotNull();
    assertThat(createdKey.getId()).isNotNull();
    assertThat(createdKey.getUserId()).isEqualTo(user.getId());

    Mockito.verify(this.apiKeyRepository, Mockito.times(1))
        .save(this.apiKeyArgumentCaptor.capture());

    final var capturedApiKey = this.apiKeyArgumentCaptor.getValue();

    assertThat(capturedApiKey).isNotNull();
    assertThat(capturedApiKey.getUserId()).isEqualTo(user.getId());
    assertThat(capturedApiKey.getKey()).isNotNull();
    assertThat(capturedApiKey.getCreatedAt()).isNotNull();
  }

  @Test
  public void testGet() throws ApiKeyNotFoundException {
    final var apiKeyId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    final var existentUser = new User();

    existentUser.setId(userId);
    existentUser.setEmail(UUID.randomUUID() + "@local");
    existentUser.setRoles(Collections.singletonList("ADMIN"));

    final var existentApiKey = new ApiKey();

    existentApiKey.setId(apiKeyId);
    existentApiKey.setKey(UUID.randomUUID().toString());
    existentApiKey.setUserId(userId);
    existentApiKey.setCreatedAt(new Date());

    Mockito.when(this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId))
        .thenReturn(Optional.of(existentApiKey));

    final var apiKey = this.apiKeyService.get(apiKeyId, userId);

    assertThat(apiKey).isNotNull();

    Mockito.verify(this.apiKeyRepository, Mockito.times(1))
        .findByIdAndUserId(Mockito.eq(apiKeyId), Mockito.eq(userId));

    assertThat(apiKey).isSameAs(existentApiKey);
  }

  @Test
  public void testGet_noSuchApiKey() {
    final var apiKeyId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    final var existentUser = new User();

    existentUser.setId(userId);
    existentUser.setEmail(UUID.randomUUID() + "@local");
    existentUser.setRoles(Collections.singletonList("ADMIN"));

    Mockito.when(this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId))
        .thenReturn(Optional.empty());

    assertThrows(ApiKeyNotFoundException.class, () -> this.apiKeyService.get(apiKeyId, userId));
  }

  @Test
  public void testDelete() throws ApiKeyNotFoundException {
    final var apiKeyId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    final var existentUser = new User();

    existentUser.setId(userId);
    existentUser.setEmail(UUID.randomUUID() + "@local");
    existentUser.setRoles(Collections.singletonList("ADMIN"));

    final var existentApiKey = new ApiKey();

    existentApiKey.setId(apiKeyId);
    existentApiKey.setKey(UUID.randomUUID().toString());
    existentApiKey.setUserId(userId);
    existentApiKey.setCreatedAt(new Date());

    Mockito.when(this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId))
        .thenReturn(Optional.of(existentApiKey));

    final var apiKey = this.apiKeyService.delete(apiKeyId, userId);

    assertThat(apiKey).isNotNull();

    Mockito.verify(this.apiKeyRepository, Mockito.times(1))
        .findByIdAndUserId(Mockito.eq(apiKeyId), Mockito.eq(userId));
    Mockito.verify(this.apiKeyRepository, Mockito.times(1)).delete(Mockito.eq(existentApiKey));

    assertThat(apiKey).isSameAs(existentApiKey);
  }

  @Test
  public void testDelete_noSuchApiKey() {
    final var apiKeyId = UUID.randomUUID().toString();
    final var userId = UUID.randomUUID().toString();

    final var existentUser = new User();

    existentUser.setId(userId);
    existentUser.setEmail(UUID.randomUUID() + "@local");
    existentUser.setRoles(Collections.singletonList("ADMIN"));

    Mockito.when(this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId))
        .thenReturn(Optional.empty());

    assertThrows(ApiKeyNotFoundException.class, () -> this.apiKeyService.delete(apiKeyId, userId));
  }
}
