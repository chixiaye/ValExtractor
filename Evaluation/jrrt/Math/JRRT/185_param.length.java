package org.apache.commons.math3.analysis.function;
import java.util.Arrays;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.FastMath;

public class Sigmoid implements UnivariateDifferentiableFunction, DifferentiableUnivariateFunction  {
  final private double lo;
  final private double hi;
  public Sigmoid() {
    this(0, 1);
  }
  public Sigmoid(double lo, double hi) {
    super();
    this.lo = lo;
    this.hi = hi;
  }
  public DerivativeStructure value(final DerivativeStructure t) throws DimensionMismatchException {
    double[] f = new double[t.getOrder() + 1];
    final double exp = FastMath.exp(-t.getValue());
    if(Double.isInfinite(exp)) {
      f[0] = lo;
      Arrays.fill(f, 1, f.length, 0.0D);
    }
    else {
      final double[] p = new double[f.length];
      final double inv = 1 / (1 + exp);
      double coeff = hi - lo;
      for(int n = 0; n < f.length; ++n) {
        double v = 0;
        p[n] = 1;
        for(int k = n; k >= 0; --k) {
          v = v * exp + p[k];
          if(k > 1) {
            p[k - 1] = (n - k + 2) * p[k - 2] - (k - 1) * p[k - 1];
          }
          else {
            p[0] = 0;
          }
        }
        coeff *= inv;
        f[n] = coeff * v;
      }
      f[0] += lo;
    }
    return t.compose(f);
  }
  @Deprecated() public UnivariateFunction derivative() {
    return FunctionUtils.toDifferentiableUnivariateFunction(this).derivative();
  }
  public double value(double x) {
    return value(x, lo, hi);
  }
  private static double value(double x, double lo, double hi) {
    return lo + (hi - lo) / (1 + FastMath.exp(-x));
  }
  
  public static class Parametric implements ParametricUnivariateFunction  {
    public double value(double x, double ... param) throws NullArgumentException, DimensionMismatchException {
      validateParameters(param);
      return Sigmoid.value(x, param[0], param[1]);
    }
    public double[] gradient(double x, double ... param) throws NullArgumentException, DimensionMismatchException {
      validateParameters(param);
      final double invExp1 = 1 / (1 + FastMath.exp(-x));
      return new double[]{ 1 - invExp1, invExp1 } ;
    }
    private void validateParameters(double[] param) throws NullArgumentException, DimensionMismatchException {
      if(param == null) {
        throw new NullArgumentException();
      }
      if(param.length != 2) {
        int var_185 = param.length;
        throw new DimensionMismatchException(var_185, 2);
      }
    }
  }
}