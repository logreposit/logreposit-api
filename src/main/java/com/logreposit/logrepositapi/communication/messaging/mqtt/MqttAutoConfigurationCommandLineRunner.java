package com.logreposit.logrepositapi.communication.messaging.mqtt;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import com.logreposit.logrepositapi.services.user.UserService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@Order(3)
public class MqttAutoConfigurationCommandLineRunner implements CommandLineRunner {

  private final MqttConfiguration mqttConfiguration;
  private final MqttCredentialService mqttCredentialService;
  private final UserService userService;

  public MqttAutoConfigurationCommandLineRunner(
      MqttConfiguration mqttConfiguration,
      MqttCredentialService mqttCredentialService,
      UserService userService) {
    this.mqttConfiguration = mqttConfiguration;
    this.mqttCredentialService = mqttCredentialService;
    this.userService = userService;
  }

  @Override
  public void run(String... args) throws Exception {
    log.info("Initializing MQTT configuration (MQTT credentials with write permission) ...");

    final var adminUser = this.userService.getFirstAdmin();

    log.info("Found first Admin User, creating  : {}", userService);

    this.retrieveOrCreateMqttCredentialForUser(adminUser.getId());
  }

  private void retrieveOrCreateMqttCredentialForUser(String userId) {
    final var mqttCredentials = this.mqttCredentialService.list(userId, 0, 1);

    if (!CollectionUtils.isEmpty(mqttCredentials.getContent())) {
      final var credential = mqttCredentials.getContent().get(0);

      log.info("Logreposit API MQTT client details => {}", credential);

      return;
    }

    if (!this.mqttConfiguration.isEnabled()) {
      log.info(
          "Could not find mqtt client credential for user with id {}. NOT creating a new one because MQTT support is not enabled.",
          userId);

      return;
    }

    log.info(
        "Could not find mqtt client credential for user with id {}. Creating new one...", userId);

    try {
      final var createdMqttCredential =
          this.mqttCredentialService.create(
              userId,
              "Logreposit API MQTT client credential used to publish device updates",
              List.of(MqttRole.GLOBAL_DEVICE_DATA_WRITE));

      log.info("Created MQTT credentials for user with ID '{}': {}", userId, createdMqttCredential);
    } catch (Exception e) {
      log.error(
          "Error creating Logreposit API MQTT client credentials for publishing device updates. Will continue regularly though to not interrupt accepting device data.",
          e);
    }
  }
}
