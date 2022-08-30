package com.logreposit.logrepositapi.utils.duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DurationCalculatorImplTests {
  private DurationCalculatorImpl durationCalculator;

  @BeforeEach
  public void setUp() {
    this.durationCalculator = new DurationCalculatorImpl();
  }

  @Test
  public void testGetDuration() throws DurationCalculatorException {
    final long expectedDelta = 3500;

    final var now = new Date();
    final long nowTime = now.getTime();
    final long thenTime = nowTime + expectedDelta;
    final var then = new Date(thenTime);

    final long delta = this.durationCalculator.getDuration(now, then);

    assertThat(delta).isEqualTo(expectedDelta);
  }

  @Test
  public void testGetDuration_startNull() {
    final var exception =
        assertThrows(
            DurationCalculatorException.class,
            () -> this.durationCalculator.getDuration(null, new Date()));

    assertThat(exception).hasMessage("start parameter isn't allowed to be null.");
  }

  @Test
  public void testGetDuration_endNull() {
    final var exception =
        assertThrows(
            DurationCalculatorException.class,
            () -> this.durationCalculator.getDuration(new Date(), null));

    assertThat(exception).hasMessage("end parameter isn't allowed to be null.");
  }
}
