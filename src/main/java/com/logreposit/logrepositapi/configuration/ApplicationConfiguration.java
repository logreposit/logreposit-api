package com.logreposit.logrepositapi.configuration;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(value = "logreposit")
@Getter
@Setter
public class ApplicationConfiguration {
  @NotNull private Integer messageSenderRetryCount;
  @NotNull private Long messageSenderRetryInitialBackOffInterval;
  @NotNull private Double messageSenderBackOffMultiplier;
  @NotBlank private String queueName;

  @NotNull
  @Size(min = 3, max = 3)
  private List<Integer> messageRetryIntervals;

  @NotNull private boolean messageListenerEnabled;
}
