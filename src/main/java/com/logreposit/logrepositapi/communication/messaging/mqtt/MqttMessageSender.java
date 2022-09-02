package com.logreposit.logrepositapi.communication.messaging.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.services.mqtt.MqttClientProvider;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttMessageSender {
  private final ObjectMapper objectMapper;
  private final MqttClientProvider mqttClientProvider;

  public MqttMessageSender(ObjectMapper objectMapper, MqttClientProvider mqttClientProvider) {
    this.objectMapper = objectMapper;
    this.mqttClientProvider = mqttClientProvider;
  }

  public <T> void send(String topic, T message)
      throws MqttException, JsonProcessingException, UserNotFoundException {
    log.info("Sending MQTT Message => topic: '{}', message: '{}'", topic, message);

    final var mqttMessage = mqttMessage(message);
    final var mqttClient = mqttClientProvider.getLogrepositMqttClient();

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
