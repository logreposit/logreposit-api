package com.logreposit.logrepositapi.services.apikey;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class ApiKeyServiceImplTests
{
    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @Captor
    private ArgumentCaptor<ApiKey> apiKeyArgumentCaptor;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

    private ApiKeyServiceImpl apiKeyService;

    @Before
    public void setUp()
    {
        this.apiKeyService = new ApiKeyServiceImpl(this.apiKeyRepository);
    }

    @Test
    public void testList()
    {
        User existentUser = new User();
        existentUser.setId(UUID.randomUUID().toString());
        existentUser.setRoles(Collections.singletonList(UserRoles.ADMIN));
        existentUser.setEmail("user@localhost");

        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(existentUser.getId());
        apiKey.setId(UUID.randomUUID().toString());
        apiKey.setCreatedAt(new Date());
        apiKey.setKey(UUID.randomUUID().toString());

        Page<ApiKey> existentApiKeys = new PageImpl<>(Collections.singletonList(apiKey));

        Mockito.when(this.apiKeyRepository.findByUserId(Mockito.eq(existentUser.getId()), Mockito.any(PageRequest.class))).thenReturn(existentApiKeys);

        int page = 1;
        int size = 15;

        Page<ApiKey> apiKeys = this.apiKeyService.list(existentUser.getId(), page, size);

        Assert.assertNotNull(apiKeys);

        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).findByUserId(Mockito.eq(existentUser.getId()), this.pageRequestArgumentCaptor.capture());

        PageRequest capturedPageRequest = this.pageRequestArgumentCaptor.getValue();

        Assert.assertNotNull(capturedPageRequest);
        Assert.assertEquals(page, capturedPageRequest.getPageNumber());
        Assert.assertEquals(size, capturedPageRequest.getPageSize());

        Assert.assertSame(existentApiKeys, apiKeys);
    }

    @Test
    public void testCreate()
    {
        User user = new User();
        user.setRoles(Collections.singletonList(UserRoles.ADMIN));
        user.setEmail("user@localhost");

        Mockito.when(this.apiKeyRepository.save(Mockito.any())).thenAnswer(i -> {
            ApiKey firstArgument = (ApiKey) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        ApiKey createdKey = this.apiKeyService.create(user.getId());

        Assert.assertNotNull(createdKey);
        Assert.assertNotNull(createdKey.getId());
        Assert.assertEquals(user.getId(), createdKey.getUserId());

        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).save(this.apiKeyArgumentCaptor.capture());

        ApiKey capturedApiKey = this.apiKeyArgumentCaptor.getValue();

        Assert.assertNotNull(capturedApiKey);
        Assert.assertEquals(user.getId(), capturedApiKey.getUserId());
        Assert.assertNotNull(capturedApiKey.getKey());
        Assert.assertNotNull(capturedApiKey.getCreatedAt());
    }

    @Test
    public void testGet() throws ApiKeyNotFoundException
    {
        String apiKeyId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        User existentUser = new User();
        existentUser.setId(userId);
        existentUser.setEmail(UUID.randomUUID().toString() + "@local");
        existentUser.setRoles(Collections.singletonList("ADMIN"));

        ApiKey existentApiKey = new ApiKey();
        existentApiKey.setId(apiKeyId);
        existentApiKey.setKey(UUID.randomUUID().toString());
        existentApiKey.setUserId(userId);
        existentApiKey.setCreatedAt(new Date());

        Mockito.when(this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId)).thenReturn(Optional.of(existentApiKey));

        ApiKey apiKey = this.apiKeyService.get(apiKeyId, userId);

        Assert.assertNotNull(apiKey);

        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).findByIdAndUserId(Mockito.eq(apiKeyId), Mockito.eq(userId));

        Assert.assertSame(apiKey, existentApiKey);
    }

    @Test(expected = ApiKeyNotFoundException.class)
    public void testGet_noSuchApiKey() throws ApiKeyNotFoundException
    {
        String apiKeyId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        User existentUser = new User();
        existentUser.setId(userId);
        existentUser.setEmail(UUID.randomUUID().toString() + "@local");
        existentUser.setRoles(Collections.singletonList("ADMIN"));

        Mockito.when(this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId)).thenReturn(Optional.empty());

        this.apiKeyService.get(apiKeyId, userId);
    }

    @Test
    public void testDelete() throws ApiKeyNotFoundException
    {
        String apiKeyId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        User existentUser = new User();
        existentUser.setId(userId);
        existentUser.setEmail(UUID.randomUUID().toString() + "@local");
        existentUser.setRoles(Collections.singletonList("ADMIN"));

        ApiKey existentApiKey = new ApiKey();
        existentApiKey.setId(apiKeyId);
        existentApiKey.setKey(UUID.randomUUID().toString());
        existentApiKey.setUserId(userId);
        existentApiKey.setCreatedAt(new Date());

        Mockito.when(this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId)).thenReturn(Optional.of(existentApiKey));

        ApiKey apiKey = this.apiKeyService.delete(apiKeyId, userId);

        Assert.assertNotNull(apiKey);

        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).findByIdAndUserId(Mockito.eq(apiKeyId), Mockito.eq(userId));
        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).delete(Mockito.eq(existentApiKey));

        Assert.assertSame(apiKey, existentApiKey);
    }

    @Test(expected = ApiKeyNotFoundException.class)
    public void testDelete_noSuchApiKey() throws ApiKeyNotFoundException
    {
        String apiKeyId = UUID.randomUUID().toString();
        String userId   = UUID.randomUUID().toString();

        User existentUser = new User();
        existentUser.setId(userId);
        existentUser.setEmail(UUID.randomUUID().toString() + "@local");
        existentUser.setRoles(Collections.singletonList("ADMIN"));

        Mockito.when(this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId)).thenReturn(Optional.empty());

        this.apiKeyService.delete(apiKeyId, userId);
    }
}
