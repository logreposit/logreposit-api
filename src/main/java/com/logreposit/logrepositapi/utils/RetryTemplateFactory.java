package com.logreposit.logrepositapi.utils;

import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

public class RetryTemplateFactory {
  private RetryTemplateFactory() {}

  public static RetryTemplate createWithExponentialBackOffForAllExceptions(
      int maxAttempts, long initialBackOffInterval, double backOffMultiplier) {
    final var simpleRetryPolicy = new SimpleRetryPolicy();

    simpleRetryPolicy.setMaxAttempts(maxAttempts);

    final var exponentialBackOffPolicy = new ExponentialBackOffPolicy();

    exponentialBackOffPolicy.setInitialInterval(initialBackOffInterval);
    exponentialBackOffPolicy.setMultiplier(backOffMultiplier);

    final var retryTemplate = new RetryTemplate();

    retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);
    retryTemplate.setRetryPolicy(simpleRetryPolicy);

    return retryTemplate;
  }
}
