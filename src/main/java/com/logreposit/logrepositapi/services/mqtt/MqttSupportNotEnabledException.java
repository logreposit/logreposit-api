package com.logreposit.logrepositapi.services.mqtt;

public class MqttSupportNotEnabledException extends MqttServiceException {
  public MqttSupportNotEnabledException() {
    super("MQTT Support is not enabled!");
  }
}
