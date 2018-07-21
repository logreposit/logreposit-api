package com.logreposit.logrepositapi.services.apikey;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class ApiKeyServiceImplTests
{
    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private UserService userService;

    @Captor
    private ArgumentCaptor<ApiKey> apiKeyArgumentCaptor;

    private ApiKeyServiceImpl apiKeyService;

    @Before
    public void setUp()
    {
        this.apiKeyService = new ApiKeyServiceImpl(this.apiKeyRepository, this.userService);
    }

    @Test
    public void testList() throws UserNotFoundException
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

        List<ApiKey> existentApiKeys = Collections.singletonList(apiKey);

        Mockito.when(this.userService.get(Mockito.eq(existentUser.getId()))).thenReturn(existentUser);
        Mockito.when(this.apiKeyRepository.findByUserId(Mockito.eq(existentUser.getId()))).thenReturn(existentApiKeys);

        List<ApiKey> apiKeys = this.apiKeyService.list(existentUser.getId());

        Assert.assertNotNull(apiKeys);

        Mockito.verify(this.userService, Mockito.times(1)).get(Mockito.eq(existentUser.getId()));
        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).findByUserId(Mockito.eq(existentUser.getId()));

        Assert.assertSame(existentApiKeys, apiKeys);
    }

    @Test(expected = UserNotFoundException.class)
    public void testList_noSuchUser() throws UserNotFoundException
    {
        String userId = UUID.randomUUID().toString();

        Mockito.when(this.userService.get(Mockito.eq(userId))).thenThrow(new UserNotFoundException(""));

        this.apiKeyService.list(userId);
    }

    @Test
    public void testCreate() throws UserNotFoundException
    {
        User existentUser = new User();
        existentUser.setId(UUID.randomUUID().toString());
        existentUser.setRoles(Collections.singletonList(UserRoles.ADMIN));
        existentUser.setEmail("user@localhost");

        Mockito.when(this.userService.get(Mockito.eq(existentUser.getId()))).thenReturn(existentUser);

        Mockito.when(this.apiKeyRepository.save(Mockito.any())).thenAnswer(i -> {
            ApiKey firstArgument = (ApiKey) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        ApiKey createdKey = this.apiKeyService.create(existentUser.getId());

        Assert.assertNotNull(createdKey);
        Assert.assertNotNull(createdKey.getId());
        Assert.assertEquals(existentUser.getId(), createdKey.getUserId());

        Mockito.verify(this.userService, Mockito.times(1)).get(Mockito.eq(existentUser.getId()));
        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).save(this.apiKeyArgumentCaptor.capture());

        ApiKey capturedApiKey = this.apiKeyArgumentCaptor.getValue();

        Assert.assertNotNull(capturedApiKey);
        Assert.assertEquals(existentUser.getId(), capturedApiKey.getUserId());
        Assert.assertNotNull(capturedApiKey.getKey());
        Assert.assertNotNull(capturedApiKey.getCreatedAt());
    }

    @Test(expected = UserNotFoundException.class)
    public void testCreate_noSuchUser() throws UserNotFoundException
    {
        String userId = UUID.randomUUID().toString();

        Mockito.when(this.userService.get(Mockito.eq(userId))).thenThrow(new UserNotFoundException(""));

        this.apiKeyService.create(userId);
    }
}
