package com.logreposit.logrepositapi.configuration;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(value = "mqtt")
@Getter
@Setter
public class MqttConfiguration {
  @NotNull private boolean enabled;
  @NotBlank private String host;
  @NotNull private Integer port;
  @NotBlank private String username;
  @NotBlank private String password;

  @Bean
  @ConditionalOnProperty(value = "mqtt.enabled", havingValue = "true")
  public IMqttClient mqttClient() throws MqttException {
    final var options = new MqttConnectionOptions();

    options.setUserName(username);
    options.setPassword(password.getBytes(StandardCharsets.UTF_8));
    options.setConnectionTimeout(10);
    options.setAutomaticReconnect(true);

    final var endpoint = String.format("tcp://%s:%d", host, port);
    final var clientId = String.format("%s_%s", username, UUID.randomUUID());
    final var mqttClient = new MqttClient(endpoint, clientId, new MemoryPersistence());

    if (!mqttClient.isConnected()) {
      mqttClient.connect(options);
    }

    return mqttClient;
  }
}
