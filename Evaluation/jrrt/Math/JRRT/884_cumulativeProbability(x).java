package org.apache.commons.math3.distribution;
import java.io.Serializable;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.util.FastMath;

abstract public class AbstractRealDistribution implements RealDistribution, Serializable  {
  final public static double SOLVER_DEFAULT_ABSOLUTE_ACCURACY = 1e-6D;
  final private static long serialVersionUID = -38038050983108802L;
  @Deprecated() protected RandomDataImpl randomData = new RandomDataImpl();
  final protected RandomGenerator random;
  private double solverAbsoluteAccuracy = SOLVER_DEFAULT_ABSOLUTE_ACCURACY;
  @Deprecated() protected AbstractRealDistribution() {
    super();
    random = null;
  }
  protected AbstractRealDistribution(RandomGenerator rng) {
    super();
    random = rng;
  }
  @Deprecated() public double cumulativeProbability(double x0, double x1) throws NumberIsTooLargeException {
    return probability(x0, x1);
  }
  protected double getSolverAbsoluteAccuracy() {
    return solverAbsoluteAccuracy;
  }
  public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
    if(p < 0.0D || p > 1.0D) {
      throw new OutOfRangeException(p, 0, 1);
    }
    double lowerBound = getSupportLowerBound();
    if(p == 0.0D) {
      return lowerBound;
    }
    double upperBound = getSupportUpperBound();
    if(p == 1.0D) {
      return upperBound;
    }
    final double mu = getNumericalMean();
    final double sig = FastMath.sqrt(getNumericalVariance());
    final boolean chebyshevApplies;
    chebyshevApplies = !(Double.isInfinite(mu) || Double.isNaN(mu) || Double.isInfinite(sig) || Double.isNaN(sig));
    if(lowerBound == Double.NEGATIVE_INFINITY) {
      if(chebyshevApplies) {
        lowerBound = mu - sig * FastMath.sqrt((1.D - p) / p);
      }
      else {
        lowerBound = -1.0D;
        while(cumulativeProbability(lowerBound) >= p){
          lowerBound *= 2.0D;
        }
      }
    }
    if(upperBound == Double.POSITIVE_INFINITY) {
      if(chebyshevApplies) {
        upperBound = mu + sig * FastMath.sqrt(p / (1.D - p));
      }
      else {
        upperBound = 1.0D;
        while(cumulativeProbability(upperBound) < p){
          upperBound *= 2.0D;
        }
      }
    }
    final UnivariateFunction toSolve = new UnivariateFunction() {
        public double value(final double x) {
          double var_884 = cumulativeProbability(x);
          return var_884 - p;
        }
    };
    double x = UnivariateSolverUtils.solve(toSolve, lowerBound, upperBound, getSolverAbsoluteAccuracy());
    if(!isSupportConnected()) {
      final double dx = getSolverAbsoluteAccuracy();
      if(x - dx >= getSupportLowerBound()) {
        double px = cumulativeProbability(x);
        if(cumulativeProbability(x - dx) == px) {
          upperBound = x;
          while(upperBound - lowerBound > dx){
            final double midPoint = 0.5D * (lowerBound + upperBound);
            if(cumulativeProbability(midPoint) < px) {
              lowerBound = midPoint;
            }
            else {
              upperBound = midPoint;
            }
          }
          return upperBound;
        }
      }
    }
    return x;
  }
  public double probability(double x) {
    return 0D;
  }
  public double probability(double x0, double x1) {
    if(x0 > x1) {
      throw new NumberIsTooLargeException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT, x0, x1, true);
    }
    return cumulativeProbability(x1) - cumulativeProbability(x0);
  }
  public double sample() {
    return inverseCumulativeProbability(random.nextDouble());
  }
  public double[] sample(int sampleSize) {
    if(sampleSize <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
    }
    double[] out = new double[sampleSize];
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