package com.logreposit.logrepositapi.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(value = "mqtt")
@Getter
@Setter
public class MqttConfiguration {
  @NotNull private MqttBrokerType brokerType;
  @NotNull private boolean enabled;
  @NotBlank private String host;
  @NotNull private Integer port;
  @NotBlank private String username;
  @NotBlank private String password;
}
