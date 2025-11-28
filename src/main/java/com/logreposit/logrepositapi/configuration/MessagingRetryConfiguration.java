package com.logreposit.logrepositapi.configuration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(value = "messaging.retry")
@Getter
@Setter
public class MessagingRetryConfiguration {
  @NotNull private Integer messageSenderRetryCount;
  @NotNull private Long messageSenderRetryInitialBackOffInterval;
  @NotNull private Double messageSenderBackOffMultiplier;

  @NotNull
  @Size(min = 3, max = 3)
  private List<Integer> messageRetryIntervals;
}
