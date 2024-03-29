package com.logreposit.logrepositapi.utils.duration;

import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class DurationCalculatorImpl implements DurationCalculator {
  @Override
  public long getDuration(Date start, Date end) throws DurationCalculatorException {
    if (start == null) {
      throw new DurationCalculatorException("start parameter isn't allowed to be null.");
    }

    if (end == null) {
      throw new DurationCalculatorException("end parameter isn't allowed to be null.");
    }

    return end.getTime() - start.getTime();
  }
}
