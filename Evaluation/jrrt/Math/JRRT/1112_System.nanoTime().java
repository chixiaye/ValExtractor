package org.apache.commons.math3.genetics;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.exception.NumberIsTooSmallException;

public class FixedElapsedTime implements StoppingCondition  {
  final private long maxTimePeriod;
  private long endTime = -1;
  public FixedElapsedTime(final long maxTime) throws NumberIsTooSmallException {
    this(maxTime, TimeUnit.SECONDS);
  }
  public FixedElapsedTime(final long maxTime, final TimeUnit unit) throws NumberIsTooSmallException {
    super();
    if(maxTime < 0) {
      throw new NumberIsTooSmallException(maxTime, 0, true);
    }
    maxTimePeriod = unit.toNanos(maxTime);
  }
  public boolean isSatisfied(final Population population) {
    if(endTime < 0) {
      long var_1112 = System.nanoTime();
      endTime = var_1112 + maxTimePeriod;
    }
    return System.nanoTime() >= endTime;
  }
}