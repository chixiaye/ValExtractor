package org.apache.commons.math3.distribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

public class GammaDistribution extends AbstractRealDistribution  {
  final public static double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9D;
  final private static long serialVersionUID = 20120524L;
  final private double shape;
  final private double scale;
  final private double shiftedShape;
  final private double densityPrefactor1;
  final private double densityPrefactor2;
  final private double minY;
  final private double maxLogY;
  final private double solverAbsoluteAccuracy;
  public GammaDistribution(RandomGenerator rng, double shape, double scale, double inverseCumAccuracy) throws NotStrictlyPositiveException {
    super(rng);
    if(shape <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.SHAPE, shape);
    }
    if(scale <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.SCALE, scale);
    }
    this.shape = shape;
    this.scale = scale;
    this.solverAbsoluteAccuracy = inverseCumAccuracy;
    this.shiftedShape = shape + Gamma.LANCZOS_G + 0.5D;
    final double aux = FastMath.E / (2.0D * FastMath.PI * shiftedShape);
    this.densityPrefactor2 = shape * FastMath.sqrt(aux) / Gamma.lanczos(shape);
    this.densityPrefactor1 = this.densityPrefactor2 / scale * FastMath.pow(shiftedShape, -shape) * FastMath.exp(shape + Gamma.LANCZOS_G);
    this.minY = shape + Gamma.LANCZOS_G - FastMath.log(Double.MAX_VALUE);
    this.maxLogY = FastMath.log(Double.MAX_VALUE) / (shape - 1.0D);
  }
  public GammaDistribution(double shape, double scale) throws NotStrictlyPositiveException {
    this(shape, scale, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
  }
  public GammaDistribution(double shape, double scale, double inverseCumAccuracy) throws NotStrictlyPositiveException {
    this(new Well19937c(), shape, scale, inverseCumAccuracy);
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
    if(x <= 0) {
      ret = 0;
    }
    else {
      ret = Gamma.regularizedGammaP(shape, x / scale);
    }
    return ret;
  }
  public double density(double x) {
    if(x < 0) {
      return 0;
    }
    final double y = x / scale;
    if((y <= minY) || (FastMath.log(y) >= maxLogY)) {
      final double aux1 = (y - shiftedShape) / shiftedShape;
      final double aux2 = shape * (FastMath.log1p(aux1) - aux1);
      final double aux3 = -y * (Gamma.LANCZOS_G + 0.5D) / shiftedShape + Gamma.LANCZOS_G + aux2;
      return densityPrefactor2 / x * FastMath.exp(aux3);
    }
    return densityPrefactor1 * FastMath.exp(-y) * FastMath.pow(y, shape - 1);
  }
  @Deprecated() public double getAlpha() {
    return shape;
  }
  @Deprecated() public double getBeta() {
    return scale;
  }
  public double getNumericalMean() {
    return shape * scale;
  }
  public double getNumericalVariance() {
    return shape * scale * scale;
  }
  public double getScale() {
    return scale;
  }
  public double getShape() {
    return shape;
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
  @Override() public double sample() {
    if(shape < 1) {
      while(true){
        final double u = random.nextDouble();
        final double bGS = 1 + shape / FastMath.E;
        final double p = bGS * u;
        if(p <= 1) {
          final double x = FastMath.pow(p, 1 / shape);
          final double u2 = random.nextDouble();
          if(u2 > FastMath.exp(-x)) {
            continue ;
          }
          else {
            return scale * x;
          }
        }
        else {
          final double x = -1 * FastMath.log((bGS - p) / shape);
          final double u2 = random.nextDouble();
          if(u2 > FastMath.pow(x, shape - 1)) {
            continue ;
          }
          else {
            return scale * x;
          }
        }
      }
    }
    final double d = shape - 0.333333333333333333D;
    final double c = 1 / (3 * FastMath.sqrt(d));
    while(true){
      final double x = random.nextGaussian();
      final double v = (1 + c * x) * (1 + c * x) * (1 + c * x);
      if(v <= 0) {
        continue ;
      }
      final double x2 = x * x;
      double var_895 = random.nextDouble();
      final double u = var_895;
      if(u < 1 - 0.0331D * x2 * x2) {
        return scale * d * v;
      }
      if(FastMath.log(u) < 0.5D * x2 + d * (1 - v + FastMath.log(v))) {
        return scale * d * v;
      }
    }
  }
}