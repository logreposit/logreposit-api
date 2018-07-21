package com.logreposit.logrepositapi.services.user;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.persistence.repositories.UserRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class UserServiceImplTests
{
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @Captor
    private ArgumentCaptor<ApiKey> apiKeyArgumentCaptor;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

    private UserServiceImpl userService;

    @Before
    public void setUp()
    {
        this.userService = new UserServiceImpl(this.userRepository, this.apiKeyRepository);
    }

    @Test
    public void testCreate() throws UserAlreadyExistentException
    {
        String       email = UUID.randomUUID().toString() + "@local.local";
        List<String> roles = Arrays.asList("ROLE1", "ROLE2");

        User user = new User();
        user.setEmail(email);
        user.setRoles(roles);

        User createdUser = new User();
        createdUser.setId(UUID.randomUUID().toString());
        createdUser.setEmail(email);
        createdUser.setRoles(roles);

        Mockito.when(this.userRepository.save(Mockito.eq(user))).thenReturn(createdUser);

        Mockito.when(this.apiKeyRepository.save(Mockito.any())).thenAnswer(i -> {
            ApiKey firstArgument = (ApiKey) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        User result = this.userService.create(user);

        Mockito.verify(this.userRepository, Mockito.times(1)).countByEmail(Mockito.eq(email));
        Mockito.verify(this.userRepository, Mockito.times(1)).save(Mockito.eq(user));
        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).save(this.apiKeyArgumentCaptor.capture());

        ApiKey capturedApiKey = this.apiKeyArgumentCaptor.getValue();

        Assert.assertNotNull(capturedApiKey);
        Assert.assertNotNull(capturedApiKey.getId());
        Assert.assertEquals(createdUser.getId(), capturedApiKey.getUserId());

        Assert.assertSame(result, createdUser);
    }

    @Test(expected = UserAlreadyExistentException.class)
    public void testCreate_emailAlreadyExistent() throws UserAlreadyExistentException
    {
        String       email = UUID.randomUUID().toString() + "@local.local";
        List<String> roles = Arrays.asList("ROLE1", "ROLE2");

        User user = new User();
        user.setEmail(email);
        user.setRoles(roles);

        Mockito.when(this.userRepository.countByEmail(Mockito.eq(email))).thenReturn(1L);

        this.userService.create(user);
    }

    @Test
    public void testList()
    {
        User user1 = new User();
        user1.setId(UUID.randomUUID().toString());
        user1.setEmail("email1@local");
        user1.setRoles(Arrays.asList("USER", "ADMIN"));

        User user2 = new User();
        user2.setId(UUID.randomUUID().toString());
        user2.setEmail("email2@local");
        user2.setRoles(Arrays.asList("USER", "SOMEOTHERROLE"));

        List<User> users    = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users);

        int page = 2;
        int size = 15;

        Mockito.when(this.userRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(userPage);

        Page<User> result = this.userService.list(page, size);

        Assert.assertNotNull(result);

        Mockito.verify(this.userRepository, Mockito.times(1)).findAll(this.pageRequestArgumentCaptor.capture());

        PageRequest capturedPageRequest = this.pageRequestArgumentCaptor.getValue();

        Assert.assertNotNull(capturedPageRequest);
        Assert.assertEquals(page, capturedPageRequest.getPageNumber());
        Assert.assertEquals(size, capturedPageRequest.getPageSize());

        Assert.assertSame(result, userPage);
    }

    @Test
    public void testGetByApiKey() throws UserNotFoundException, ApiKeyNotFoundException
    {
        String apiKey = UUID.randomUUID().toString();

        User existentUser = new User();
        existentUser.setId(UUID.randomUUID().toString());
        existentUser.setRoles(Arrays.asList("ROLE0", "ROLE1"));
        existentUser.setEmail("existent@local");

        ApiKey existentApiKey = new ApiKey();
        existentApiKey.setId(UUID.randomUUID().toString());
        existentApiKey.setKey(apiKey);
        existentApiKey.setUserId(existentUser.getId());
        existentApiKey.setCreatedAt(new Date());

        Mockito.when(this.apiKeyRepository.findByKey(Mockito.eq(apiKey))).thenReturn(Optional.of(existentApiKey));
        Mockito.when(this.userRepository.findById(Mockito.eq(existentApiKey.getUserId()))).thenReturn(Optional.of(existentUser));

        User user = this.userService.getByApiKey(apiKey);

        Assert.assertNotNull(user);

        Mockito.verify(this.apiKeyRepository, Mockito.times(1)).findByKey(Mockito.eq(apiKey));
        Mockito.verify(this.userRepository, Mockito.times(1)).findById(Mockito.eq(existentApiKey.getUserId()));

        Assert.assertSame(existentUser, user);
    }

    @Test(expected = ApiKeyNotFoundException.class)
    public void testGetByApiKey_noSuchKey() throws UserNotFoundException, ApiKeyNotFoundException
    {
        String apiKey = UUID.randomUUID().toString();

        Mockito.when(this.apiKeyRepository.findByKey(Mockito.eq(apiKey))).thenReturn(Optional.empty());

        this.userService.getByApiKey(apiKey);
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetByApiKey_noSuchUser() throws UserNotFoundException, ApiKeyNotFoundException
    {
        String apiKey = UUID.randomUUID().toString();

        ApiKey existentApiKey = new ApiKey();
        existentApiKey.setId(UUID.randomUUID().toString());
        existentApiKey.setKey(apiKey);
        existentApiKey.setUserId(UUID.randomUUID().toString());
        existentApiKey.setCreatedAt(new Date());

        Mockito.when(this.apiKeyRepository.findByKey(Mockito.eq(apiKey))).thenReturn(Optional.of(existentApiKey));
        Mockito.when(this.userRepository.findById(Mockito.eq(existentApiKey.getUserId()))).thenReturn(Optional.empty());

        this.userService.getByApiKey(apiKey);
    }

    @Test
    public void testGetFirstAdmin() throws UserNotFoundException
    {
        User user = new User();
        user.setEmail("admin@localhost");
        user.setRoles(Collections.singletonList(UserRoles.ADMIN));
        user.setId(UUID.randomUUID().toString());

        Mockito.when(this.userRepository.findFirstByRolesContaining(Mockito.eq(UserRoles.ADMIN))).thenReturn(Optional.of(user));

        User result = this.userService.getFirstAdmin();

        Assert.assertNotNull(result);

        Mockito.verify(this.userRepository, Mockito.times(1)).findFirstByRolesContaining(Mockito.eq(UserRoles.ADMIN));

        Assert.assertSame(result, user);
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetFirstAdmin_noSuchUser() throws UserNotFoundException
    {
        Mockito.when(this.userRepository.findFirstByRolesContaining(Mockito.eq(UserRoles.ADMIN))).thenReturn(Optional.empty());

        this.userService.getFirstAdmin();
    }
}
