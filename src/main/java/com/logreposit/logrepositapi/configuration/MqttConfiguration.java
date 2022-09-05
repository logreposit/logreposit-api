package com.logreposit.logrepositapi.configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(value = "mqtt")
@ConditionalOnProperty(value = "mqtt.enabled", havingValue = "true")
@Getter
@Setter
public class MqttConfiguration {
  @NotNull private boolean enabled;
  @NotBlank private String host;
  @NotNull private Integer port;
  @NotBlank private String username;
  @NotBlank private String password;
}
