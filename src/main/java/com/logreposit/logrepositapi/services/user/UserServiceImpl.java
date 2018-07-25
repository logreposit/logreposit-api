package com.logreposit.logrepositapi.services.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.dtos.UserCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.sender.MessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.persistence.repositories.UserRepository;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService
{
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository   userRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final MessageFactory   messageFactory;
    private final MessageSender    messageSender;

    public UserServiceImpl(UserRepository userRepository,
                           ApiKeyRepository apiKeyRepository,
                           MessageFactory messageFactory,
                           MessageSender messageSender)
    {
        this.userRepository   = userRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.messageFactory   = messageFactory;
        this.messageSender    = messageSender;
    }

    @Override
    public CreatedUser create(User user) throws UserServiceException
    {
        logger.info("Creating user: {}", user);

        String plainTextPassword = user.getPassword();

        this.checkIfUserAlreadyExistent(user.getEmail());
        this.hashUserPassword(user);

        User   createdUser   = this.userRepository.save(user);
        ApiKey apiKey        = buildApiKey(createdUser.getId());
        ApiKey createdApiKey = this.apiKeyRepository.save(apiKey);

        logger.info("Created user: {} with api key: {}", user, createdApiKey);

        this.publishUserCreated(createdUser, plainTextPassword);

        CreatedUser result = new CreatedUser(createdUser, createdApiKey);

        return result;
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

    private void hashUserPassword(User user)
    {
        String plainTextPassword = user.getPassword();
        String passwordHash      = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());

        user.setPassword(passwordHash);
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

    private void publishUserCreated(User user, String plainTextPassword) throws UserServiceException
    {
        try
        {
            UserCreatedMessageDto userCreatedMessageDto = createUserCreatedMessageDto(user, plainTextPassword);
            Message               userCreatedMessage    = this.messageFactory.buildEventUserCreatedMessage(userCreatedMessageDto);

            this.messageSender.send(userCreatedMessage);
        }
        catch (JsonProcessingException e)
        {
            logger.error("Unable to create userCreatedMessage: {}", LoggingUtils.getLogForException(e));
            throw new UserServiceException("Unable to create userCreatedMessage", e);
        }
        catch (MessageSenderException e)
        {
            logger.error("Unable to send userCreatedMessage: {}", LoggingUtils.getLogForException(e));
            throw new UserServiceException("Unable to send userCreatedMessage", e);
        }
    }

    private static UserCreatedMessageDto createUserCreatedMessageDto(User user, String plainTextPassword)
    {
        UserCreatedMessageDto userCreatedMessageDto = new UserCreatedMessageDto();

        userCreatedMessageDto.setId(user.getId());
        userCreatedMessageDto.setEmail(user.getEmail());
        userCreatedMessageDto.setRoles(user.getRoles());
        userCreatedMessageDto.setPassword(plainTextPassword);

        return userCreatedMessageDto;
    }
}
