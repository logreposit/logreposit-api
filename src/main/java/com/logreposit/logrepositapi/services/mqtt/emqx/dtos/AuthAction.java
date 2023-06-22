package com.logreposit.logrepositapi.services.mqtt.emqx.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AuthAction {
  @JsonProperty("publish")
  PUBLISH,
  @JsonProperty("subscribe")
  SUBSCRIBE,
  @JsonProperty("all")
  ALL
}
