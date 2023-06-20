package com.logreposit.logrepositapi.services.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.springframework.stereotype.Service;

@Service
public class MqttClientCache {
  private final Map<String, IMqttClient> mqttClients;

  public MqttClientCache() {
    this.mqttClients = new HashMap<>();
  }

  public Optional<IMqttClient> get(String username) {
    return Optional.ofNullable(mqttClients.get(username));
  }

  public void put(String username, IMqttClient client) {
    this.mqttClients.put(username, client);
  }
}
