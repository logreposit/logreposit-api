package com.logreposit.logrepositapi.configuration;

public enum MqttBrokerType {
  MOSQUITTO(Constants.MOSQUITTO_VALUE),
  EMQX(Constants.EMQX_VALUE);

  MqttBrokerType(String value) {}

  public static class Constants {
    public static final String MOSQUITTO_VALUE = "MOSQUITTO";
    public static final String EMQX_VALUE = "EMQX";
  }
}
