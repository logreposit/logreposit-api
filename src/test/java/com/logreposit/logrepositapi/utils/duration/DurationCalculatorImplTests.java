package com.logreposit.logrepositapi.utils.duration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

@RunWith(JUnit4.class)
public class DurationCalculatorImplTests
{
    private DurationCalculatorImpl durationCalculator;

    @Before
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

        Assert.assertEquals(expectedDelta, delta);
    }

    @Test
    public void testGetDuration_startNull()
    {
        try
        {
            this.durationCalculator.getDuration(null, new Date());

            Assert.fail("Should not be here.");
        }
        catch (DurationCalculatorException e)
        {
            Assert.assertEquals("start parameter isn't allowed to be null.", e.getMessage());
        }
    }

    @Test
    public void testGetDuration_endNull()
    {
        try
        {
            this.durationCalculator.getDuration(new Date(), null);

            Assert.fail("Should not be here.");
        }
        catch (DurationCalculatorException e)
        {
            Assert.assertEquals("end parameter isn't allowed to be null.", e.getMessage());
        }
    }
}
