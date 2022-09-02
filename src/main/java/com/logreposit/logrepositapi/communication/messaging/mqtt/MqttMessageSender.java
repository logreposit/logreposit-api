package com.logreposit.logrepositapi.communication.messaging.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttMessageSender {
  private final ObjectMapper objectMapper;
  private final IMqttClient mqttClient;

  public MqttMessageSender(ObjectMapper objectMapper, Optional<IMqttClient> mqttClient) {
    this.objectMapper = objectMapper;

    if (mqttClient.isPresent()) {
      this.mqttClient = mqttClient.get();

      log.info("MQTT Client has been configured.");
    } else {
      this.mqttClient = null;

      log.info("MQTT Client has not been configured.");
    }
  }

  public <T> void send(String topic, T message) throws MqttException, JsonProcessingException {
    if (mqttClient == null) {
      log.info(
          "MQTT client has not been configured. Not sending Message => topic: '{}', message: '{}'",
          topic,
          message);

      return;
    }

    log.info("Sending MQTT Message => topic: '{}', message: '{}'", topic, message);

    final var mqttMessage = mqttMessage(message);

    mqttClient.publish(topic, mqttMessage);
  }

  private <T> MqttMessage mqttMessage(T payload) throws JsonProcessingException {
    final var serializedPayload = objectMapper.writeValueAsBytes(payload);
    final var message = new MqttMessage();

    message.setQos(1); // TODO DoM: think about which QoS level to choose for downlink messages
    message.setPayload(serializedPayload);

    return message;
  }
}
