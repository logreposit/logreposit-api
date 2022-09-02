package com.logreposit.logrepositapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.services.mqtt.dynsec.MosquittoDynSecClient;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(value = "mqtt")
@ConditionalOnProperty(value = "mqtt.enabled", havingValue = "true")
@Getter
@Setter
public class MqttConfiguration {
  @NotNull private boolean enabled;
  @NotBlank private String host;
  @NotNull private Integer port;
  @NotBlank private String username;
  @NotBlank private String password;

  // repos to get admin credentials

  @Bean(name = "dynSecMqttClient")
  public IMqttClient dynSecMqttClient() throws MqttException {
    return mqttClient(username, password);
  }

  @Bean
  public MosquittoDynSecClient mosquittoDynSecClient(
      ObjectMapper objectMapper, @Qualifier("dynSecMqttClient") IMqttClient mqttClient)
      throws MqttException {
    return new MosquittoDynSecClient(objectMapper, mqttClient);
  }

  private IMqttClient mqttClient(String username, String password) throws MqttException {
    final var options = new MqttConnectOptions();

    options.setUserName(username);
    options.setPassword(password.toCharArray());
    options.setConnectionTimeout(10);
    options.setAutomaticReconnect(true);

    final var endpoint = String.format("tcp://%s:%d", host, port);
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
