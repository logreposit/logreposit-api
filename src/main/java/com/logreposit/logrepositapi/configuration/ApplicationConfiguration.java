package com.logreposit.logrepositapi.configuration;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(value = "app")
@Getter
@Setter
public class ApplicationConfiguration {
  private ApplicationModeConfiguration modes = new ApplicationModeConfiguration();

  @Getter
  @Setter
  public static class ApplicationModeConfiguration {
    List<ApplicationMode> enabled = List.of();
  }

  public enum ApplicationMode {
    INGRESS,
    PROCESSOR_MQTT,
    PROCESSOR_INFLUX
  }
}
