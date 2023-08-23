package org.apache.commons.math3.analysis.integration;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.util.FastMath;

public class SimpsonIntegrator extends BaseAbstractUnivariateIntegrator  {
  final public static int SIMPSON_MAX_ITERATIONS_COUNT = 64;
  public SimpsonIntegrator() {
    super(DEFAULT_MIN_ITERATIONS_COUNT, SIMPSON_MAX_ITERATIONS_COUNT);
  }
  public SimpsonIntegrator(final double relativeAccuracy, final double absoluteAccuracy, final int minimalIterationCount, final int maximalIterationCount) throws NotStrictlyPositiveException, NumberIsTooSmallException, NumberIsTooLargeException {
    super(relativeAccuracy, absoluteAccuracy, minimalIterationCount, maximalIterationCount);
    if(maximalIterationCount > SIMPSON_MAX_ITERATIONS_COUNT) {
      throw new NumberIsTooLargeException(maximalIterationCount, SIMPSON_MAX_ITERATIONS_COUNT, false);
    }
  }
  public SimpsonIntegrator(final int minimalIterationCount, final int maximalIterationCount) throws NotStrictlyPositiveException, NumberIsTooSmallException, NumberIsTooLargeException {
    super(minimalIterationCount, maximalIterationCount);
    if(maximalIterationCount > SIMPSON_MAX_ITERATIONS_COUNT) {
      throw new NumberIsTooLargeException(maximalIterationCount, SIMPSON_MAX_ITERATIONS_COUNT, false);
    }
  }
  @Override() protected double doIntegrate() throws TooManyEvaluationsException, MaxCountExceededException {
    TrapezoidIntegrator qtrap = new TrapezoidIntegrator();
    if(getMinimalIterationCount() == 1) {
      return (4 * qtrap.stage(this, 1) - qtrap.stage(this, 0)) / 3.0D;
    }
    double olds = 0;
    double oldt = qtrap.stage(this, 0);
    while(true){
      int var_194 = iterations.getCount();
      final double t = qtrap.stage(this, var_194);
      iterations.incrementCount();
      final double s = (4 * t - oldt) / 3.0D;
      if(iterations.getCount() >= getMinimalIterationCount()) {
        final double delta = FastMath.abs(s - olds);
        final double rLimit = getRelativeAccuracy() * (FastMath.abs(olds) + FastMath.abs(s)) * 0.5D;
        if((delta <= rLimit) || (delta <= getAbsoluteAccuracy())) {
          return s;
        }
      }
      olds = s;
      oldt = t;
    }
  }
}