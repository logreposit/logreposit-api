package com.logreposit.logrepositapi;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import com.logreposit.logrepositapi.services.user.UserServiceException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class LogrepositCommandLineRunner implements CommandLineRunner {
  private static final Logger logger = LoggerFactory.getLogger(LogrepositCommandLineRunner.class);

  private final UserService userService;
  private final ApiKeyService apiKeyService;
  private final MqttConfiguration mqttConfiguration;
  private final MqttCredentialService mqttCredentialService;

  public LogrepositCommandLineRunner(
      UserService userService,
      ApiKeyService apiKeyService,
      MqttConfiguration mqttConfiguration,
      MqttCredentialService mqttCredentialService) {
    this.userService = userService;
    this.apiKeyService = apiKeyService;
    this.mqttConfiguration = mqttConfiguration;
    this.mqttCredentialService = mqttCredentialService;
  }

  @Override
  public void run(String... args) throws Exception {
    final var adminUser = this.retrieveOrCreateAdminUser();
    final var apiKey = this.retrieveOrCreateApiKeyForUser(adminUser.getId());

    logger.warn(
        "Administrator Details => email: {} apiKey: {}", adminUser.getEmail(), apiKey.getKey());

    this.retrieveOrCreateMqttCredentialForUser(adminUser.getId());
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

  private void retrieveOrCreateMqttCredentialForUser(String userId) {
    final var mqttCredentials = this.mqttCredentialService.list(userId, 0, 1);

    if (!CollectionUtils.isEmpty(mqttCredentials.getContent())) {
      final var credential = mqttCredentials.getContent().get(0);

      logger.info("Logreposit API MQTT client details => {}", credential);

      return;
    }

    if (!this.mqttConfiguration.isEnabled()) {
      logger.info(
          "Could not find mqtt client credential for user with id {}. NOT creating a new one because MQTT support is not enabled.",
          userId);

      return;
    }

    logger.info(
        "Could not find mqtt client credential for user with id {}. Creating new one...", userId);

    try {
      final var createdMqttCredential =
          this.mqttCredentialService.create(
              userId,
              "Logreposit API MQTT client credential used to publish device updates",
              List.of(MqttRole.GLOBAL_DEVICE_DATA_WRITE));

      logger.info(
          "Created MQTT credentials for user with ID '{}': {}", userId, createdMqttCredential);
    } catch (Exception e) {
      logger.error(
          "Error creating Logreposit API MQTT client credentials for publishing device updates. Will continue regularly though to not interrupt accepting device data.",
          e);
    }
  }

  private static String getRandomPassword() {
    return (UUID.randomUUID() + "_" + UUID.randomUUID()).toUpperCase();
  }
}
