package com.logreposit.logrepositapi.services.mqtt;

public class MqttSupportNotEnabledException extends MqttCredentialServiceException {
  public MqttSupportNotEnabledException() {
    super("MQTT Support is not enabled!");
  }
}
