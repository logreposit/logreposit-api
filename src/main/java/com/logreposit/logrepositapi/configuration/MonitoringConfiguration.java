package com.logreposit.logrepositapi.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfiguration {
  @Bean
  public MeterRegistryCustomizer<MeterRegistry> registerCommonTags() {
    return registry -> registry.config().commonTags("application", "logreposit-api");
  }
}
