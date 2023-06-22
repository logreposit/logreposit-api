package com.logreposit.logrepositapi;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import com.logreposit.logrepositapi.services.user.UserServiceException;
import java.util.Collections;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Order(1)
public class LogrepositCommandLineRunner implements CommandLineRunner {
  private static final Logger logger = LoggerFactory.getLogger(LogrepositCommandLineRunner.class);

  private final UserService userService;
  private final ApiKeyService apiKeyService;

  public LogrepositCommandLineRunner(UserService userService, ApiKeyService apiKeyService) {
    this.userService = userService;
    this.apiKeyService = apiKeyService;
  }

  @Override
  public void run(String... args) throws Exception {
    logger.info("Initializing Admin API User & Admin API Key ...");

    final var adminUser = this.retrieveOrCreateAdminUser();
    final var apiKey = this.retrieveOrCreateApiKeyForUser(adminUser.getId());

    logger.warn(
        "Administrator Details => email: {} apiKey: {}", adminUser.getEmail(), apiKey.getKey());
  }

  private User retrieveOrCreateAdminUser() throws UserServiceException {
    try {
      return this.userService.getFirstAdmin();
    } catch (UserNotFoundException e) {
      logger.warn("Caught UserNotFoundException. Creating new one...");

      final var user = new User();

      user.setRoles(Collections.singletonList(UserRoles.ADMIN));
      user.setEmail("admin@localhost");
      user.setPassword(getRandomPassword());

      final var createdUser = this.userService.create(user);

      return createdUser.getUser();
    }
  }

  private ApiKey retrieveOrCreateApiKeyForUser(String userId) {
    final var apiKeys = this.apiKeyService.list(userId, 0, 1);

    if (!CollectionUtils.isEmpty(apiKeys.getContent())) {
      return apiKeys.getContent().get(0);
    }

    logger.info("Could not find api key for admin user with id {}. Creating new one.", userId);

    return this.apiKeyService.create(userId);
  }

  private static String getRandomPassword() {
    return (UUID.randomUUID() + "_" + UUID.randomUUID()).toUpperCase();
  }
}
