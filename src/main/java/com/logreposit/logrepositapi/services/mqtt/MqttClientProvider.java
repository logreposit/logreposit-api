package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttClientProvider {
  private final MqttConfiguration mqttConfiguration;
  private final Map<String, IMqttClient> mqttClients;

  public MqttClientProvider(MqttConfiguration mqttConfiguration) {
    this.mqttConfiguration = mqttConfiguration;

    this.mqttClients = new HashMap<>();
  }

  public IMqttClient getMqttClient(String username, String password) throws MqttException {
    if (!mqttConfiguration.isEnabled()) {
      throw new MqttClientProviderException("MQTT support is not enabled!");
    }

    final var cachedClient = getCachedClient(username);

    if (cachedClient.isPresent()) {
      return cachedClient.get();
    }

    final var mqttClient = mqttClient(username, password);

    mqttClients.put(username, mqttClient);

    return mqttClient;
  }

  private Optional<IMqttClient> getCachedClient(String username) {
    final var mqttClient = this.mqttClients.get(username);

    if (mqttClient == null) {
      log.info("No MQTT client with username '{}' initialized yet.", username);

      return Optional.empty();
    }

    return Optional.of(mqttClient);
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
