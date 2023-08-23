package org.apache.commons.math3.distribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

public class FDistribution extends AbstractRealDistribution  {
  final public static double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9D;
  final private static long serialVersionUID = -8516354193418641566L;
  final private double numeratorDegreesOfFreedom;
  final private double denominatorDegreesOfFreedom;
  final private double solverAbsoluteAccuracy;
  private double numericalVariance = Double.NaN;
  private boolean numericalVarianceIsCalculated = false;
  public FDistribution(RandomGenerator rng, double numeratorDegreesOfFreedom, double denominatorDegreesOfFreedom, double inverseCumAccuracy) throws NotStrictlyPositiveException {
    super(rng);
    if(numeratorDegreesOfFreedom <= 0) {
      LocalizedFormats var_879 = LocalizedFormats.DEGREES_OF_FREEDOM;
      throw new NotStrictlyPositiveException(var_879, numeratorDegreesOfFreedom);
    }
    if(denominatorDegreesOfFreedom <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.DEGREES_OF_FREEDOM, denominatorDegreesOfFreedom);
    }
    this.numeratorDegreesOfFreedom = numeratorDegreesOfFreedom;
    this.denominatorDegreesOfFreedom = denominatorDegreesOfFreedom;
    solverAbsoluteAccuracy = inverseCumAccuracy;
  }
  public FDistribution(double numeratorDegreesOfFreedom, double denominatorDegreesOfFreedom) throws NotStrictlyPositiveException {
    this(numeratorDegreesOfFreedom, denominatorDegreesOfFreedom, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
  }
  public FDistribution(double numeratorDegreesOfFreedom, double denominatorDegreesOfFreedom, double inverseCumAccuracy) throws NotStrictlyPositiveException {
    this(new Well19937c(), numeratorDegreesOfFreedom, denominatorDegreesOfFreedom, inverseCumAccuracy);
  }
  public boolean isSupportConnected() {
    return true;
  }
  public boolean isSupportLowerBoundInclusive() {
    return false;
  }
  public boolean isSupportUpperBoundInclusive() {
    return false;
  }
  protected double calculateNumericalVariance() {
    final double denominatorDF = getDenominatorDegreesOfFreedom();
    if(denominatorDF > 4) {
      final double numeratorDF = getNumeratorDegreesOfFreedom();
      final double denomDFMinusTwo = denominatorDF - 2;
      return (2 * (denominatorDF * denominatorDF) * (numeratorDF + denominatorDF - 2)) / ((numeratorDF * (denomDFMinusTwo * denomDFMinusTwo) * (denominatorDF - 4)));
    }
    return Double.NaN;
  }
  public double cumulativeProbability(double x) {
    double ret;
    if(x <= 0) {
      ret = 0;
    }
    else {
      double n = numeratorDegreesOfFreedom;
      double m = denominatorDegreesOfFreedom;
      ret = Beta.regularizedBeta((n * x) / (m + n * x), 0.5D * n, 0.5D * m);
    }
    return ret;
  }
  public double density(double x) {
    final double nhalf = numeratorDegreesOfFreedom / 2;
    final double mhalf = denominatorDegreesOfFreedom / 2;
    final double logx = FastMath.log(x);
    final double logn = FastMath.log(numeratorDegreesOfFreedom);
    final double logm = FastMath.log(denominatorDegreesOfFreedom);
    final double lognxm = FastMath.log(numeratorDegreesOfFreedom * x + denominatorDegreesOfFreedom);
    return FastMath.exp(nhalf * logn + nhalf * logx - logx + mhalf * logm - nhalf * lognxm - mhalf * lognxm - Beta.logBeta(nhalf, mhalf));
  }
  public double getDenominatorDegreesOfFreedom() {
    return denominatorDegreesOfFreedom;
  }
  public double getNumeratorDegreesOfFreedom() {
    return numeratorDegreesOfFreedom;
  }
  public double getNumericalMean() {
    final double denominatorDF = getDenominatorDegreesOfFreedom();
    if(denominatorDF > 2) {
      return denominatorDF / (denominatorDF - 2);
    }
    return Double.NaN;
  }
  public double getNumericalVariance() {
    if(!numericalVarianceIsCalculated) {
      numericalVariance = calculateNumericalVariance();
      numericalVarianceIsCalculated = true;
    }
    return numericalVariance;
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
}