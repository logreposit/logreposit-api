package com.logreposit.logrepositapi.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfiguration {
  private final BuildProperties buildProperties;

  public MonitoringConfiguration(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  @Bean
  public MeterRegistryCustomizer<MeterRegistry> registerCommonTags() {
    return registry ->
        registry.config().commonTags("application", this.buildProperties.getArtifact());
  }
}
