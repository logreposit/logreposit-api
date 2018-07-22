package com.logreposit.logrepositapi.services.user;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.persistence.repositories.UserRepository;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService
{
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ApiKeyRepository apiKeyRepository;

    public UserServiceImpl(UserRepository userRepository,
                           ApiKeyRepository apiKeyRepository)
    {
        this.userRepository   = userRepository;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public User create(User user) throws UserAlreadyExistentException
    {
        logger.info("Creating user: {}", user);

        this.checkIfUserAlreadyExistent(user.getEmail());

        User   createdUser   = this.userRepository.save(user);
        ApiKey apiKey        = buildApiKey(createdUser.getId());
        ApiKey createdApiKey = this.apiKeyRepository.save(apiKey);

        logger.info("Created user: {} with api key: {}", user, createdApiKey);

        return createdUser;
    }

    @Override
    public Page<User> list(Integer page, Integer size)
    {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User>  users       = this.userRepository.findAll(pageRequest);

        return users;
    }

    @Override
    public User getByApiKey(String key) throws ApiKeyNotFoundException, UserNotFoundException
    {
        Optional<ApiKey> apiKey = this.apiKeyRepository.findByKey(key);

        if (!apiKey.isPresent())
        {
            logger.error("api key {} not found in database.", key);
            throw new ApiKeyNotFoundException("Api Key not found.");
        }

        Optional<User> user = this.userRepository.findById(apiKey.get().getUserId());

        if (!user.isPresent())
        {
            logger.error("could not find user that belongs to api key {}.", key);
            throw new UserNotFoundException("User for given Api Key not found.");
        }

        return user.get();
    }

    @Override
    public User get(String userId) throws UserNotFoundException
    {
        Optional<User> user = this.userRepository.findById(userId);

        if (!user.isPresent())
        {
            logger.error("could not find user with id {}.", userId);
            throw new UserNotFoundException("could not find user with id");
        }

        return user.get();
    }

    @Override
    public User getFirstAdmin() throws UserNotFoundException
    {
        Optional<User> adminUser = this.userRepository.findFirstByRolesContaining(UserRoles.ADMIN);

        if (!adminUser.isPresent())
        {
            throw new UserNotFoundException("Admin user not found.");
        }

        return adminUser.get();
    }

    private void checkIfUserAlreadyExistent(String email) throws UserAlreadyExistentException
    {
        long count = this.userRepository.countByEmail(email);

        if (count > 0)
        {
            throw new UserAlreadyExistentException("User with the given email address is already existent", email);
        }
    }

    private static ApiKey buildApiKey(String userId)
    {
        ApiKey apiKey = new ApiKey();

        apiKey.setKey(UUID.randomUUID().toString());
        apiKey.setUserId(userId);
        apiKey.setCreatedAt(new Date());

        return apiKey;
    }
}
