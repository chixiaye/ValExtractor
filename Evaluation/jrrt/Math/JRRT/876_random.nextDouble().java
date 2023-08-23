package org.apache.commons.math3.distribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

public class PoissonDistribution extends AbstractIntegerDistribution  {
  final public static int DEFAULT_MAX_ITERATIONS = 10000000;
  final public static double DEFAULT_EPSILON = 1e-12D;
  final private static long serialVersionUID = -3349935121172596109L;
  final private NormalDistribution normal;
  final private ExponentialDistribution exponential;
  final private double mean;
  final private int maxIterations;
  final private double epsilon;
  public PoissonDistribution(RandomGenerator rng, double p, double epsilon, int maxIterations) throws NotStrictlyPositiveException {
    super(rng);
    if(p <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.MEAN, p);
    }
    mean = p;
    this.epsilon = epsilon;
    this.maxIterations = maxIterations;
    normal = new NormalDistribution(rng, p, FastMath.sqrt(p), NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    exponential = new ExponentialDistribution(rng, 1, ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
  }
  public PoissonDistribution(double p) throws NotStrictlyPositiveException {
    this(p, DEFAULT_EPSILON, DEFAULT_MAX_ITERATIONS);
  }
  public PoissonDistribution(double p, double epsilon) throws NotStrictlyPositiveException {
    this(p, epsilon, DEFAULT_MAX_ITERATIONS);
  }
  public PoissonDistribution(double p, double epsilon, int maxIterations) throws NotStrictlyPositiveException {
    this(new Well19937c(), p, epsilon, maxIterations);
  }
  public PoissonDistribution(double p, int maxIterations) {
    this(p, DEFAULT_EPSILON, maxIterations);
  }
  public boolean isSupportConnected() {
    return true;
  }
  public double cumulativeProbability(int x) {
    if(x < 0) {
      return 0;
    }
    if(x == Integer.MAX_VALUE) {
      return 1;
    }
    return Gamma.regularizedGammaQ((double)x + 1, mean, epsilon, maxIterations);
  }
  public double getMean() {
    return mean;
  }
  public double getNumericalMean() {
    return getMean();
  }
  public double getNumericalVariance() {
    return getMean();
  }
  public double normalApproximateProbability(int x) {
    return normal.cumulativeProbability(x + 0.5D);
  }
  public double probability(int x) {
    double ret;
    if(x < 0 || x == Integer.MAX_VALUE) {
      ret = 0.0D;
    }
    else 
      if(x == 0) {
        ret = FastMath.exp(-mean);
      }
      else {
        ret = FastMath.exp(-SaddlePointExpansion.getStirlingError(x) - SaddlePointExpansion.getDeviancePart(x, mean)) / FastMath.sqrt(MathUtils.TWO_PI * x);
      }
    return ret;
  }
  public int getSupportLowerBound() {
    return 0;
  }
  public int getSupportUpperBound() {
    return Integer.MAX_VALUE;
  }
  @Override() public int sample() {
    return (int)FastMath.min(nextPoisson(mean), Integer.MAX_VALUE);
  }
  private long nextPoisson(double meanPoisson) {
    final double pivot = 40.0D;
    if(meanPoisson < pivot) {
      double p = FastMath.exp(-meanPoisson);
      long n = 0;
      double r = 1.0D;
      double rnd = 1.0D;
      while(n < 1000 * meanPoisson){
        rnd = random.nextDouble();
        r = r * rnd;
        if(r >= p) {
          n++;
        }
        else {
          return n;
        }
      }
      return n;
    }
    else {
      final double lambda = FastMath.floor(meanPoisson);
      final double lambdaFractional = meanPoisson - lambda;
      final double logLambda = FastMath.log(lambda);
      final double logLambdaFactorial = CombinatoricsUtils.factorialLog((int)lambda);
      final long y2 = lambdaFractional < Double.MIN_VALUE ? 0 : nextPoisson(lambdaFractional);
      final double delta = FastMath.sqrt(lambda * FastMath.log(32 * lambda / FastMath.PI + 1));
      final double halfDelta = delta / 2;
      final double twolpd = 2 * lambda + delta;
      final double a1 = FastMath.sqrt(FastMath.PI * twolpd) * FastMath.exp(1 / 8 * lambda);
      final double a2 = (twolpd / delta) * FastMath.exp(-delta * (1 + delta) / twolpd);
      final double aSum = a1 + a2 + 1;
      final double p1 = a1 / aSum;
      final double p2 = a2 / aSum;
      final double c1 = 1 / (8 * lambda);
      double x = 0;
      double y = 0;
      double v = 0;
      int a = 0;
      double t = 0;
      double qr = 0;
      double qa = 0;
      for(; true; ) {
        double var_876 = random.nextDouble();
        final double u = var_876;
        if(u <= p1) {
          final double n = random.nextGaussian();
          x = n * FastMath.sqrt(lambda + halfDelta) - 0.5D;
          if(x > delta || x < -lambda) {
            continue ;
          }
          y = x < 0 ? FastMath.floor(x) : FastMath.ceil(x);
          final double e = exponential.sample();
          v = -e - (n * n / 2) + c1;
        }
        else {
          if(u > p1 + p2) {
            y = lambda;
            break ;
          }
          else {
            x = delta + (twolpd / delta) * exponential.sample();
            y = FastMath.ceil(x);
            v = -exponential.sample() - delta * (x + 1) / twolpd;
          }
        }
        a = x < 0 ? 1 : 0;
        t = y * (y + 1) / (2 * lambda);
        if(v < -t && a == 0) {
          y = lambda + y;
          break ;
        }
        qr = t * ((2 * y + 1) / (6 * lambda) - 1);
        qa = qr - (t * t) / (3 * (lambda + a * (y + 1)));
        if(v < qa) {
          y = lambda + y;
          break ;
        }
        if(v > qr) {
          continue ;
        }
        if(v < y * logLambda - CombinatoricsUtils.factorialLog((int)(y + lambda)) + logLambdaFactorial) {
          y = lambda + y;
          break ;
        }
      }
      return y2 + (long)y;
    }
  }
}