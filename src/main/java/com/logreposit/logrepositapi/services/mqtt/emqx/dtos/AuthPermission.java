package com.logreposit.logrepositapi.services.mqtt.emqx.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AuthPermission {
  @JsonProperty("allow")
  ALLOW,
  @JsonProperty("deny")
  DENY,
}
