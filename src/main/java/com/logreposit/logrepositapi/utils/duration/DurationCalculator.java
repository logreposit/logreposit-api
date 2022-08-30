package com.logreposit.logrepositapi.utils.duration;

import java.util.Date;

public interface DurationCalculator {
  long getDuration(Date start, Date end) throws DurationCalculatorException;
}
