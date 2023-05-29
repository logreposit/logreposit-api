package com.logreposit.logrepositapi.services.mqtt.emqx;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmqxApiClient {
  private final MqttConfiguration mqttConfiguration;

  public EmqxApiClient(MqttConfiguration mqttConfiguration) {
    this.mqttConfiguration = mqttConfiguration;
  }

  // TODO: write implementation!

  public void dummyMethod() {
    log.info("TODO: Dummy :) MQTT Configuration: {}", mqttConfiguration);
  }
}
