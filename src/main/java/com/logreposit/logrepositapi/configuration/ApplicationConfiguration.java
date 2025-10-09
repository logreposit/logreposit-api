package com.logreposit.logrepositapi.configuration;

import java.util.ArrayList;
import java.util.Arrays;
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

  // TODO DoM: Try out if we can easily override this with application.yml / environment variable
  // TODO DoM: configuration
  @Getter
  public static class ApplicationModeConfiguration {
    List<ApplicationMode> enabled =
        new ArrayList<>(Arrays.stream(ApplicationMode.values()).toList());
  }

  public enum ApplicationMode {
    INGRESS,
    PROCESSOR_MQTT,
    PROCESSOR_INFLUX
  }
}
