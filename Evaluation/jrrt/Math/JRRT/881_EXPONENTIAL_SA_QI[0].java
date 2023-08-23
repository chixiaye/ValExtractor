package org.apache.commons.math3.distribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

public class ExponentialDistribution extends AbstractRealDistribution  {
  final public static double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9D;
  final private static long serialVersionUID = 2401296428283614780L;
  final private static double[] EXPONENTIAL_SA_QI;
  final private double mean;
  final private double solverAbsoluteAccuracy;
  static {
    final double LN2 = FastMath.log(2);
    double qi = 0;
    int i = 1;
    final ResizableDoubleArray ra = new ResizableDoubleArray(20);
    while(qi < 1){
      qi += FastMath.pow(LN2, i) / CombinatoricsUtils.factorial(i);
      ra.addElement(qi);
      ++i;
    }
    EXPONENTIAL_SA_QI = ra.getElements();
  }
  public ExponentialDistribution(RandomGenerator rng, double mean, double inverseCumAccuracy) throws NotStrictlyPositiveException {
    super(rng);
    if(mean <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.MEAN, mean);
    }
    this.mean = mean;
    solverAbsoluteAccuracy = inverseCumAccuracy;
  }
  public ExponentialDistribution(double mean) {
    this(mean, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
  }
  public ExponentialDistribution(double mean, double inverseCumAccuracy) {
    this(new Well19937c(), mean, inverseCumAccuracy);
  }
  public boolean isSupportConnected() {
    return true;
  }
  public boolean isSupportLowerBoundInclusive() {
    return true;
  }
  public boolean isSupportUpperBoundInclusive() {
    return false;
  }
  public double cumulativeProbability(double x) {
    double ret;
    if(x <= 0.0D) {
      ret = 0.0D;
    }
    else {
      ret = 1.0D - FastMath.exp(-x / mean);
    }
    return ret;
  }
  public double density(double x) {
    if(x < 0) {
      return 0;
    }
    return FastMath.exp(-x / mean) / mean;
  }
  public double getMean() {
    return mean;
  }
  public double getNumericalMean() {
    return getMean();
  }
  public double getNumericalVariance() {
    final double m = getMean();
    return m * m;
  }
  @Override() protected double getSolverAbsoluteAccuracy() {
    return solverAbsoluteAccuracy;
  }
  public double getSupportLowerBound() {
    return 0;
  }
  public double getSupportUpperBound() {
    return Double.POSITIVE_INFINITY;
  }
  @Override() public double inverseCumulativeProbability(double p) throws OutOfRangeException {
    double ret;
    if(p < 0.0D || p > 1.0D) {
      throw new OutOfRangeException(p, 0.0D, 1.0D);
    }
    else 
      if(p == 1.0D) {
        ret = Double.POSITIVE_INFINITY;
      }
      else {
        ret = -mean * FastMath.log(1.0D - p);
      }
    return ret;
  }
  @Override() public double sample() {
    double a = 0;
    double u = random.nextDouble();
    while(u < 0.5D){
      a += EXPONENTIAL_SA_QI[0];
      u *= 2;
    }
    u += u - 1;
    if(u <= EXPONENTIAL_SA_QI[0]) {
      return mean * (a + u);
    }
    int i = 0;
    double u2 = random.nextDouble();
    double umin = u2;
    do {
      ++i;
      u2 = random.nextDouble();
      if(u2 < umin) {
        umin = u2;
      }
    }while(u > EXPONENTIAL_SA_QI[i]);
    double var_881 = EXPONENTIAL_SA_QI[0];
    return mean * (a + umin * var_881);
  }
}