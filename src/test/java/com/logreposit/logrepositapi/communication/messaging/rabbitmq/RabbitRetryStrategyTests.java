package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;

import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RabbitRetryStrategyTests {
  private RabbitRetryStrategy rabbitRetryStrategy;

  @BeforeEach
  public void setUp() {
    ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    applicationConfiguration.setMessageRetryIntervals(List.of(1000, 2000, 3000));

    this.rabbitRetryStrategy = new RabbitRetryStrategy(applicationConfiguration);
  }

  @Test
  public void testGetExchangeNameForRetryInterval_given100_expectCorrectExchangeName() {
    assertThat(RabbitRetryStrategy.getExchangeNameForRetryInterval(100)).isEqualTo("retry.x.100");
  }

  @Test
  public void testGetQueueNameForRetryInterval_given100_expectCorrectQueueName() {
    assertThat(RabbitRetryStrategy.getRetryQueueName(100)).isEqualTo("retry.q.100");
  }

  @Test
  public void testGetExchange_givenNegativeErrorCount_expectCorrectExchangeName() {
    assertThat(this.rabbitRetryStrategy.getExchange(-1)).isEqualTo("retry.x.1000");
  }

  @Test
  public void testGetExchange_givenErrorCount0_expectCorrectExchangeName() {
    assertThat(this.rabbitRetryStrategy.getExchange(0)).isEqualTo("retry.x.1000");
  }

  @Test
  public void testGetExchange_givenErrorCount5_expectCorrectExchangeName() {
    assertThat(this.rabbitRetryStrategy.getExchange(5)).isEqualTo("retry.x.1000");
  }

  @Test
  public void testGetExchange_givenErrorCount6_expectCorrectExchangeName() {
    assertThat(this.rabbitRetryStrategy.getExchange(6)).isEqualTo("retry.x.2000");
  }

  @Test
  public void testGetExchange_givenErrorCount10_expectCorrectExchangeName() {
    assertThat(this.rabbitRetryStrategy.getExchange(10)).isEqualTo("retry.x.2000");
  }

  @Test
  public void testGetExchange_givenErrorCount11_expectCorrectExchangeName() {
    assertThat(this.rabbitRetryStrategy.getExchange(11)).isEqualTo("retry.x.3000");
  }

  @Test
  public void testGetExchange_givenErrorCount15_expectCorrectExchangeName() {
    assertThat(this.rabbitRetryStrategy.getExchange(15)).isEqualTo("retry.x.3000");
  }

  @Test
  public void testGetExchange_givenErrorCount16_expectCorrectExchangeName() {
    assertThat(this.rabbitRetryStrategy.getExchange(16)).isEqualTo("error.x");
  }
}
