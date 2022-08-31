package com.logreposit.logrepositapi.communication.messaging.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttMessageSender {
  private final IMqttClient mqttClient;

  public MqttMessageSender(Optional<IMqttClient> mqttClient) {
    if (mqttClient.isPresent()) {
      this.mqttClient = mqttClient.get();

      log.info("MQTT Client has been configured.");
    } else {
      this.mqttClient = null;

      log.info("MQTT Client has not been configured.");
    }
  }

  public void send(String topic, String message) throws MqttException {
    if (mqttClient == null) {
      log.info(
          "MQTT client has not been configured. Not sending Message => topic: '{}', message: '{}'",
          topic,
          message);

      return;
    }

    log.info("Sending MQTT Message => topic: '{}', message: '{}'", topic, message);

    final var mqttMessage = new MqttMessage();

    mqttMessage.setQos(1);
    mqttMessage.setPayload(message.getBytes(StandardCharsets.UTF_8));

    // TODO DoM: do we need retries here?!
    mqttClient.publish(topic, mqttMessage);
  }
}
