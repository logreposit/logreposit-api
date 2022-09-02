package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class MqttClientProvider {
  private final MqttConfiguration mqttConfiguration;
  private final UserService userService;
  private final MqttCredentialService mqttCredentialService;

  private IMqttClient dynSecMqttClient;
  private IMqttClient logrepositMqttClient;

  public MqttClientProvider(
      MqttConfiguration mqttConfiguration,
      UserService userService,
      MqttCredentialService mqttCredentialService) {
    this.mqttConfiguration = mqttConfiguration;
    this.userService = userService;
    this.mqttCredentialService = mqttCredentialService;
  }

  public IMqttClient getDynSecMqttClient() throws MqttException {
    if (!mqttConfiguration.isEnabled()) {
      throw new RuntimeException("MQTT config is not enabled!");
    }

    if (this.dynSecMqttClient != null) {
      return this.dynSecMqttClient;
    }

    this.dynSecMqttClient =
        mqttClient(mqttConfiguration.getUsername(), mqttConfiguration.getPassword());

    return this.dynSecMqttClient;
  }

  public IMqttClient getLogrepositMqttClient() throws UserNotFoundException, MqttException {
    if (!mqttConfiguration.isEnabled()) {
      throw new RuntimeException("MQTT config is not enabled!");
    }

    if (this.logrepositMqttClient != null) {
      return this.logrepositMqttClient;
    }

    final var adminUser = userService.getFirstAdmin();
    final var mqttCredentials = mqttCredentialService.list(adminUser.getId(), 0, 1).getContent();

    if (CollectionUtils.isEmpty(mqttCredentials)) {
      throw new MqttCredentialNotFoundException(
          String.format("No MQTT credentials found for admin user '%s'!", adminUser.getId()));
    }

    final var mqttCredential = mqttCredentials.get(0);

    this.logrepositMqttClient =
        mqttClient(mqttCredential.getUsername(), mqttCredential.getPassword());

    return this.logrepositMqttClient;
  }

  private IMqttClient mqttClient(String username, String password) throws MqttException {
    final var options = new MqttConnectOptions();

    options.setUserName(username);
    options.setPassword(password.toCharArray());
    options.setConnectionTimeout(10);
    options.setAutomaticReconnect(true);

    final var endpoint =
        String.format("tcp://%s:%d", mqttConfiguration.getHost(), mqttConfiguration.getPort());
    // TODO DoM: check if this clientId is ok
    final var clientId = String.format("%s_%s", username, UUID.randomUUID());
    final var mqttClient = new MqttClient(endpoint, clientId, new MemoryPersistence());
    // TODO DoM: check if MemoryPersistence is ok for this use-case

    if (!mqttClient.isConnected()) {
      mqttClient.connect(options);
    }

    return mqttClient;
  }
}
