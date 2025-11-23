package com.logreposit.logrepositapi.configuration;

import lombok.Getter;
import lombok.Setter;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@Getter
@Setter
@ConfigurationProperties(value = "influxdbservice.communication.influx")
public class InfluxDbConfiguration {
  private String url;

  // TODO DoM: Check if we should use username/password or token auth
  private String username;
  private String password;

  @Bean
  public InfluxDB influxDb() {
    return InfluxDBFactory.connect(this.url, this.username, this.password);
  }
}
