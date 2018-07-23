package com.logreposit.logrepositapi.utils.duration;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DurationCalculatorImpl implements DurationCalculator
{
    @Override
    public long getDuration(Date start, Date end) throws DurationCalculatorException
    {
        if (start == null)
        {
            throw new DurationCalculatorException("start parameter isn't allowed to be null.");
        }

        if (end == null)
        {
            throw new DurationCalculatorException("end parameter isn't allowed to be null.");
        }

        long duration = end.getTime() - start.getTime();

        return duration;
    }
}
