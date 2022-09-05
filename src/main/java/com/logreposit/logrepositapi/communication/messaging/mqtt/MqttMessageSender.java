package com.logreposit.logrepositapi.communication.messaging.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.services.mqtt.MqttClientProvider;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttMessageSender {
  private final ObjectMapper objectMapper;
  private final MqttConfiguration mqttConfiguration;
  private final MqttClientProvider mqttClientProvider;
  private final MqttCredentialService mqttCredentialService;

  private IMqttClient mqttClient;

  public MqttMessageSender(
      ObjectMapper objectMapper,
      MqttConfiguration mqttConfiguration,
      MqttClientProvider mqttClientProvider,
      MqttCredentialService mqttCredentialService) {
    this.objectMapper = objectMapper;
    this.mqttConfiguration = mqttConfiguration;
    this.mqttClientProvider = mqttClientProvider;
    this.mqttCredentialService = mqttCredentialService;
  }

  public <T> void send(String topic, T message) {
    if (!mqttConfiguration.isEnabled()) {
      log.info(
          "MQTT support is not enabled. Not sending MQTT Message => topic: '{}', message: '{}'",
          topic,
          message);

      return;
    }

    log.info("Sending MQTT Message => topic: '{}', message: '{}'", topic, message);

    final var mqttMessage = mqttMessage(message);

    try {
      // TODO DoM: does try with resources make sense here with the auto-closable mqttClient?
      mqttClient().publish(topic, mqttMessage);
    } catch (MqttException e) {
      throw new MqttMessageSenderException("Unable to publish MQTT message", e);
    }
  }

  private IMqttClient mqttClient() throws MqttException {
    if (mqttClient != null) {
      return mqttClient;
    }

    final var mqttCredential = mqttCredentialService.getGlobalDeviceDataWriteCredential();
    final var globalDeviceDataWriteMqttClient =
        mqttClientProvider.getMqttClient(
            mqttCredential.getUsername(), mqttCredential.getPassword());

    if (globalDeviceDataWriteMqttClient == null) {
      throw new IllegalStateException("mqttClient should have been initialized before!");
    }

    this.mqttClient = globalDeviceDataWriteMqttClient;

    return globalDeviceDataWriteMqttClient;
  }

  private <T> MqttMessage mqttMessage(T payload) {
    try {
      final var serializedPayload = objectMapper.writeValueAsBytes(payload);
      final var message = new MqttMessage();

      message.setQos(1);
      message.setPayload(serializedPayload);

      return message;
    } catch (JsonProcessingException e) {
      throw new MqttMessageSenderException("Unable to serialize MqttMessage payload", e);
    }
  }
}
