package com.logreposit.logrepositapi;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.user.CreatedUser;
import com.logreposit.logrepositapi.services.user.UserAlreadyExistentException;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import com.logreposit.logrepositapi.services.user.UserServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;

@Component
public class LogrepositCommandLineRunner implements CommandLineRunner
{
    private static final Logger logger = LoggerFactory.getLogger(LogrepositCommandLineRunner.class);

    private final UserService   userService;
    private final ApiKeyService apiKeyService;

    public LogrepositCommandLineRunner(UserService userService, ApiKeyService apiKeyService)
    {
        this.userService   = userService;
        this.apiKeyService = apiKeyService;
    }

    @Override
    public void run(String... args) throws Exception
    {
        User   adminUser = this.retrieveOrCreateAdminUser();
        ApiKey apiKey    = this.retrieveOrCreateApiKeyForUser(adminUser.getId());

        logger.warn("Administrator Details => email: {} apiKey: {}", adminUser.getEmail(), apiKey.getKey());
    }

    private User retrieveOrCreateAdminUser() throws UserServiceException
    {
        try
        {
            User user = this.userService.getFirstAdmin();

            return user;
        }
        catch (UserNotFoundException e)
        {
            logger.warn("Caught UserNotFoundException. Creating new one...");

            User user = new User();
            user.setRoles(Collections.singletonList(UserRoles.ADMIN));
            user.setEmail("admin@localhost");

            CreatedUser createdUser = this.userService.create(user);

            return createdUser.getUser();
        }
    }

    private ApiKey retrieveOrCreateApiKeyForUser(String userId)
    {
        Page<ApiKey> apiKeys = this.apiKeyService.list(userId, 0, 1);

        if (!CollectionUtils.isEmpty(apiKeys.getContent()))
        {
            return apiKeys.getContent().get(0);
        }

        logger.info("Could not find api key for admin user with id {}. Creating new one.", userId);

        ApiKey apiKey = this.apiKeyService.create(userId);

        return apiKey;
    }
}
