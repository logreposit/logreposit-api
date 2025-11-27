package com.logreposit.logrepositapi.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfiguration {
  @Bean
  public MeterRegistryCustomizer<MeterRegistry> registerCommonTags() {
    // TODO: Maybe the enabled app modes should be also added as tags
    return registry -> registry.config().commonTags("application", "logreposit-api");
  }
}
