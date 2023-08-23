package org.apache.commons.math3.analysis.integration;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.util.FastMath;

public class TrapezoidIntegrator extends BaseAbstractUnivariateIntegrator  {
  final public static int TRAPEZOID_MAX_ITERATIONS_COUNT = 64;
  private double s;
  public TrapezoidIntegrator() {
    super(DEFAULT_MIN_ITERATIONS_COUNT, TRAPEZOID_MAX_ITERATIONS_COUNT);
  }
  public TrapezoidIntegrator(final double relativeAccuracy, final double absoluteAccuracy, final int minimalIterationCount, final int maximalIterationCount) throws NotStrictlyPositiveException, NumberIsTooSmallException, NumberIsTooLargeException {
    super(relativeAccuracy, absoluteAccuracy, minimalIterationCount, maximalIterationCount);
    if(maximalIterationCount > TRAPEZOID_MAX_ITERATIONS_COUNT) {
      throw new NumberIsTooLargeException(maximalIterationCount, TRAPEZOID_MAX_ITERATIONS_COUNT, false);
    }
  }
  public TrapezoidIntegrator(final int minimalIterationCount, final int maximalIterationCount) throws NotStrictlyPositiveException, NumberIsTooSmallException, NumberIsTooLargeException {
    super(minimalIterationCount, maximalIterationCount);
    if(maximalIterationCount > TRAPEZOID_MAX_ITERATIONS_COUNT) {
      throw new NumberIsTooLargeException(maximalIterationCount, TRAPEZOID_MAX_ITERATIONS_COUNT, false);
    }
  }
  @Override() protected double doIntegrate() throws MathIllegalArgumentException, TooManyEvaluationsException, MaxCountExceededException {
    double oldt = stage(this, 0);
    iterations.incrementCount();
    while(true){
      final int i = iterations.getCount();
      final double t = stage(this, i);
      if(i >= getMinimalIterationCount()) {
        final double delta = FastMath.abs(t - oldt);
        final double rLimit = getRelativeAccuracy() * (FastMath.abs(oldt) + FastMath.abs(t)) * 0.5D;
        if((delta <= rLimit) || (delta <= getAbsoluteAccuracy())) {
          return t;
        }
      }
      oldt = t;
      iterations.incrementCount();
    }
  }
  double stage(final BaseAbstractUnivariateIntegrator baseIntegrator, final int n) throws TooManyEvaluationsException {
    if(n == 0) {
      double var_191 = baseIntegrator.getMax();
      final double max = var_191;
      final double min = baseIntegrator.getMin();
      s = 0.5D * (max - min) * (baseIntegrator.computeObjectiveValue(min) + baseIntegrator.computeObjectiveValue(max));
      return s;
    }
    else {
      final long np = 1L << (n - 1);
      double sum = 0;
      final double max = baseIntegrator.getMax();
      final double min = baseIntegrator.getMin();
      final double spacing = (max - min) / np;
      double x = min + 0.5D * spacing;
      for(long i = 0; i < np; i++) {
        sum += baseIntegrator.computeObjectiveValue(x);
        x += spacing;
      }
      s = 0.5D * (s + sum * spacing);
      return s;
    }
  }
}