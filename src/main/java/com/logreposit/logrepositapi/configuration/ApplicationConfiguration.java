package com.logreposit.logrepositapi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(value = "logreposit")
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

    public Integer getMessageSenderRetryCount()
    {
        return this.messageSenderRetryCount;
    }

    public void setMessageSenderRetryCount(Integer messageSenderRetryCount)
    {
        this.messageSenderRetryCount = messageSenderRetryCount;
    }

    public Long getMessageSenderRetryInitialBackOffInterval()
    {
        return this.messageSenderRetryInitialBackOffInterval;
    }

    public void setMessageSenderRetryInitialBackOffInterval(Long messageSenderRetryInitialBackOffInterval)
    {
        this.messageSenderRetryInitialBackOffInterval = messageSenderRetryInitialBackOffInterval;
    }

    public Double getMessageSenderBackOffMultiplier()
    {
        return this.messageSenderBackOffMultiplier;
    }

    public void setMessageSenderBackOffMultiplier(Double messageSenderBackOffMultiplier)
    {
        this.messageSenderBackOffMultiplier = messageSenderBackOffMultiplier;
    }

    public String getQueueName()
    {
        return this.queueName;
    }

    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }
}
