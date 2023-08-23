package org.apache.commons.math3.analysis.function;
import java.util.Arrays;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

public class Gaussian implements UnivariateDifferentiableFunction, DifferentiableUnivariateFunction  {
  final private double mean;
  final private double is;
  final private double i2s2;
  final private double norm;
  public Gaussian() {
    this(0, 1);
  }
  public Gaussian(double mean, double sigma) throws NotStrictlyPositiveException {
    this(1 / (sigma * FastMath.sqrt(2 * Math.PI)), mean, sigma);
  }
  public Gaussian(double norm, double mean, double sigma) throws NotStrictlyPositiveException {
    super();
    if(sigma <= 0) {
      throw new NotStrictlyPositiveException(sigma);
    }
    this.norm = norm;
    this.mean = mean;
    this.is = 1 / sigma;
    this.i2s2 = 0.5D * is * is;
  }
  public DerivativeStructure value(final DerivativeStructure t) throws DimensionMismatchException {
    final double u = is * (t.getValue() - mean);
    double[] f = new double[t.getOrder() + 1];
    final double[] p = new double[f.length];
    p[0] = 1;
    final double u2 = u * u;
    double coeff = norm * FastMath.exp(-0.5D * u2);
    if(coeff <= Precision.SAFE_MIN) {
      Arrays.fill(f, 0.0D);
    }
    else {
      f[0] = coeff;
      for(int n = 1; n < f.length; ++n) {
        double v = 0;
        p[n] = -p[n - 1];
        for(int k = n; k >= 0; k -= 2) {
          v = v * u2 + p[k];
          if(k > 2) {
            p[k - 2] = (k - 1) * p[k - 1] - p[k - 3];
          }
          else 
            if(k == 2) {
              p[0] = p[1];
            }
        }
        if((n & 0x1) == 1) {
          v *= u;
        }
        coeff *= is;
        f[n] = coeff * v;
      }
    }
    return t.compose(f);
  }
  @Deprecated() public UnivariateFunction derivative() {
    return FunctionUtils.toDifferentiableUnivariateFunction(this).derivative();
  }
  public double value(double x) {
    return value(x - mean, norm, i2s2);
  }
  private static double value(double xMinusMean, double norm, double i2s2) {
    return norm * FastMath.exp(-xMinusMean * xMinusMean * i2s2);
  }
  
  public static class Parametric implements ParametricUnivariateFunction  {
    public double value(double x, double ... param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
      validateParameters(param);
      final double diff = x - param[1];
      double var_180 = param[2];
      final double i2s2 = 1 / (2 * param[2] * var_180);
      return Gaussian.value(diff, param[0], i2s2);
    }
    public double[] gradient(double x, double ... param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
      validateParameters(param);
      final double norm = param[0];
      final double diff = x - param[1];
      final double sigma = param[2];
      final double i2s2 = 1 / (2 * sigma * sigma);
      final double n = Gaussian.value(diff, 1, i2s2);
      final double m = norm * n * 2 * i2s2 * diff;
      final double s = m * diff / sigma;
      return new double[]{ n, m, s } ;
    }
    private void validateParameters(double[] param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
      if(param == null) {
        throw new NullArgumentException();
      }
      if(param.length != 3) {
        throw new DimensionMismatchException(param.length, 3);
      }
      if(param[2] <= 0) {
        throw new NotStrictlyPositiveException(param[2]);
      }
    }
  }
}