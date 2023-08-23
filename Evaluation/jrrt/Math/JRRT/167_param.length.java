package org.apache.commons.math3.analysis.function;
import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

public class Logit implements UnivariateDifferentiableFunction, DifferentiableUnivariateFunction  {
  final private double lo;
  final private double hi;
  public Logit() {
    this(0, 1);
  }
  public Logit(double lo, double hi) {
    super();
    this.lo = lo;
    this.hi = hi;
  }
  public DerivativeStructure value(final DerivativeStructure t) throws OutOfRangeException {
    final double x = t.getValue();
    if(x < lo || x > hi) {
      throw new OutOfRangeException(x, lo, hi);
    }
    double[] f = new double[t.getOrder() + 1];
    f[0] = FastMath.log((x - lo) / (hi - x));
    if(Double.isInfinite(f[0])) {
      if(f.length > 1) {
        f[1] = Double.POSITIVE_INFINITY;
      }
      for(int i = 2; i < f.length; ++i) {
        f[i] = f[i - 2];
      }
    }
    else {
      final double invL = 1.0D / (x - lo);
      double xL = invL;
      final double invH = 1.0D / (hi - x);
      double xH = invH;
      for(int i = 1; i < f.length; ++i) {
        f[i] = xL + xH;
        xL *= -i * invL;
        xH *= i * invH;
      }
    }
    return t.compose(f);
  }
  @Deprecated() public UnivariateFunction derivative() {
    return FunctionUtils.toDifferentiableUnivariateFunction(this).derivative();
  }
  public double value(double x) throws OutOfRangeException {
    return value(x, lo, hi);
  }
  private static double value(double x, double lo, double hi) throws OutOfRangeException {
    if(x < lo || x > hi) {
      throw new OutOfRangeException(x, lo, hi);
    }
    return FastMath.log((x - lo) / (hi - x));
  }
  
  public static class Parametric implements ParametricUnivariateFunction  {
    public double value(double x, double ... param) throws NullArgumentException, DimensionMismatchException {
      validateParameters(param);
      return Logit.value(x, param[0], param[1]);
    }
    public double[] gradient(double x, double ... param) throws NullArgumentException, DimensionMismatchException {
      validateParameters(param);
      final double lo = param[0];
      final double hi = param[1];
      return new double[]{ 1 / (lo - x), 1 / (hi - x) } ;
    }
    private void validateParameters(double[] param) throws NullArgumentException, DimensionMismatchException {
      if(param == null) {
        throw new NullArgumentException();
      }
      int var_167 = param.length;
      if(var_167 != 2) {
        throw new DimensionMismatchException(param.length, 2);
      }
    }
  }
}