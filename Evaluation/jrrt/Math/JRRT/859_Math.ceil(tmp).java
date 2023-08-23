package org.apache.commons.math3.distribution;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.util.FastMath;

abstract public class AbstractIntegerDistribution implements IntegerDistribution, Serializable  {
  final private static long serialVersionUID = -1146319659338487221L;
  @Deprecated() final protected RandomDataImpl randomData = new RandomDataImpl();
  final protected RandomGenerator random;
  @Deprecated() protected AbstractIntegerDistribution() {
    super();
    random = null;
  }
  protected AbstractIntegerDistribution(RandomGenerator rng) {
    super();
    random = rng;
  }
  private double checkedCumulativeProbability(int argument) throws MathInternalError {
    double result = Double.NaN;
    result = cumulativeProbability(argument);
    if(Double.isNaN(result)) {
      throw new MathInternalError(LocalizedFormats.DISCRETE_CUMULATIVE_PROBABILITY_RETURNED_NAN, argument);
    }
    return result;
  }
  public double cumulativeProbability(int x0, int x1) throws NumberIsTooLargeException {
    if(x1 < x0) {
      throw new NumberIsTooLargeException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT, x0, x1, true);
    }
    return cumulativeProbability(x1) - cumulativeProbability(x0);
  }
  public int inverseCumulativeProbability(final double p) throws OutOfRangeException {
    if(p < 0.0D || p > 1.0D) {
      throw new OutOfRangeException(p, 0, 1);
    }
    int lower = getSupportLowerBound();
    if(p == 0.0D) {
      return lower;
    }
    if(lower == Integer.MIN_VALUE) {
      if(checkedCumulativeProbability(lower) >= p) {
        return lower;
      }
    }
    else {
      lower -= 1;
    }
    int upper = getSupportUpperBound();
    if(p == 1.0D) {
      return upper;
    }
    final double mu = getNumericalMean();
    final double sigma = FastMath.sqrt(getNumericalVariance());
    final boolean chebyshevApplies = !(Double.isInfinite(mu) || Double.isNaN(mu) || Double.isInfinite(sigma) || Double.isNaN(sigma) || sigma == 0.0D);
    if(chebyshevApplies) {
      double k = FastMath.sqrt((1.0D - p) / p);
      double tmp = mu - k * sigma;
      if(tmp > lower) {
        lower = ((int)Math.ceil(tmp)) - 1;
      }
      k = 1.0D / k;
      tmp = mu + k * sigma;
      if(tmp < upper) {
        double var_859 = Math.ceil(tmp);
        upper = ((int)var_859) - 1;
      }
    }
    return solveInverseCumulativeProbability(p, lower, upper);
  }
  public int sample() {
    return inverseCumulativeProbability(random.nextDouble());
  }
  protected int solveInverseCumulativeProbability(final double p, int lower, int upper) {
    while(lower + 1 < upper){
      int xm = (lower + upper) / 2;
      if(xm < lower || xm > upper) {
        xm = lower + (upper - lower) / 2;
      }
      double pm = checkedCumulativeProbability(xm);
      if(pm >= p) {
        upper = xm;
      }
      else {
        lower = xm;
      }
    }
    return upper;
  }
  public int[] sample(int sampleSize) {
    if(sampleSize <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
    }
    int[] out = new int[sampleSize];
    for(int i = 0; i < sampleSize; i++) {
      out[i] = sample();
    }
    return out;
  }
  public void reseedRandomGenerator(long seed) {
    random.setSeed(seed);
    randomData.reSeed(seed);
  }
}