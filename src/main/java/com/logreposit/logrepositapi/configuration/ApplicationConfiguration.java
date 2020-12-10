package com.logreposit.logrepositapi.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Validated
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

    @NotNull
    @Size(min = 3, max = 3)
    private List<Integer> messageRetryIntervals;
}
