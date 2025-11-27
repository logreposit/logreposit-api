package com.logreposit.logrepositapi.utils;

import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.backoff.ExponentialBackOff;

public class RetryTemplateFactory {
  private RetryTemplateFactory() {}

  public static RetryTemplate createWithExponentialBackOffForAllExceptions(
      int maxAttempts, long initialBackOffInterval, double backOffMultiplier) {

    final var exponentialBackOff = new ExponentialBackOff();

    exponentialBackOff.setInitialInterval(initialBackOffInterval);
    exponentialBackOff.setMultiplier(backOffMultiplier);
    exponentialBackOff.setMaxAttempts(maxAttempts);

    final var retryPolicy = RetryPolicy.builder().backOff(exponentialBackOff).build();

    return new RetryTemplate(retryPolicy);
  }
}
