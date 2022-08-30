package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RabbitRetryStrategy {
  private final List<Integer> retryIntervals;

  public RabbitRetryStrategy(ApplicationConfiguration applicationConfiguration) {
    this.retryIntervals = applicationConfiguration.getMessageRetryIntervals();
  }

  public static final String ERROR_EXCHANGE_NAME = "error.x";

  public String getExchange(long errorCount) {
    if (errorCount <= 5) return getExchangeNameForRetryInterval(this.retryIntervals.get(0));

    if (errorCount <= 10) return getExchangeNameForRetryInterval(this.retryIntervals.get(1));

    if (errorCount <= 15) return getExchangeNameForRetryInterval(this.retryIntervals.get(2));

    return ERROR_EXCHANGE_NAME;
  }

  public static String getExchangeNameForRetryInterval(int retryInterval) {
    return "retry.x." + retryInterval;
  }

  public static String getRetryQueueName(int retryInterval) {
    return "retry.q." + retryInterval;
  }
}
