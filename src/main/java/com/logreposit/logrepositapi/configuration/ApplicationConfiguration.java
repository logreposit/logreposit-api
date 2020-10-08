package com.logreposit.logrepositapi.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(value = "logreposit")
@Getter
@Setter
public class ApplicationConfiguration
{
    @NotNull
    private Integer messageSenderRetryCount;

    @NotNull
    private Long messageSenderRetryInitialBackOffInterval;

    @NotNull
    private Double messageSenderBackOffMultiplier;

    @NotBlank
    private String queueName;
}
