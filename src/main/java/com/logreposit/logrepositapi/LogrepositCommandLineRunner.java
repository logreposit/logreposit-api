package com.logreposit.logrepositapi;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.user.UserAlreadyExistentException;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

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

        logger.info("Administrator Details => user: {}, apiKey: {}", adminUser, apiKey);
    }

    private User retrieveOrCreateAdminUser() throws UserAlreadyExistentException
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

            User createdUser = this.userService.create(user);

            return createdUser;
        }
    }

    private ApiKey retrieveOrCreateApiKeyForUser(String userId) throws UserNotFoundException
    {
        List<ApiKey> apiKeys = this.apiKeyService.list(userId);

        if (!CollectionUtils.isEmpty(apiKeys))
        {
            return apiKeys.get(0);
        }

        logger.info("Could not find api key for admin user with id {}. Creating new one.", userId);

        ApiKey apiKey = this.apiKeyService.create(userId);

        return apiKey;
    }
}
