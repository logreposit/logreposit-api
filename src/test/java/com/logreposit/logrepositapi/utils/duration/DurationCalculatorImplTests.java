package com.logreposit.logrepositapi.utils.duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DurationCalculatorImplTests
{
    private DurationCalculatorImpl durationCalculator;

    @BeforeEach
    public void setUp()
    {
        this.durationCalculator = new DurationCalculatorImpl();
    }

    @Test
    public void testGetDuration() throws DurationCalculatorException
    {
        long expectedDelta = 3500;

        Date now      = new Date();
        long nowTime  = now.getTime();
        long thenTime = nowTime + expectedDelta;
        Date then     = new Date(thenTime);

        long delta = this.durationCalculator.getDuration(now, then);

        assertThat(delta).isEqualTo(expectedDelta);
    }

    @Test
    public void testGetDuration_startNull()
    {
        var exception = assertThrows(
                DurationCalculatorException.class,
                () -> this.durationCalculator.getDuration(null, new Date())
        );

        assertThat(exception).hasMessage("start parameter isn't allowed to be null.");
    }

    @Test
    public void testGetDuration_endNull()
    {
        var exception = assertThrows(
                DurationCalculatorException.class,
                () -> this.durationCalculator.getDuration(new Date(), null)
        );

        assertThat(exception).hasMessage("end parameter isn't allowed to be null.");
    }
}
