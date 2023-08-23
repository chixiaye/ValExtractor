package org.apache.commons.math3.analysis.function;
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

public class Logistic implements UnivariateDifferentiableFunction, DifferentiableUnivariateFunction  {
  final private double a;
  final private double k;
  final private double b;
  final private double oneOverN;
  final private double q;
  final private double m;
  public Logistic(double k, double m, double b, double q, double a, double n) throws NotStrictlyPositiveException {
    super();
    if(n <= 0) {
      throw new NotStrictlyPositiveException(n);
    }
    this.k = k;
    this.m = m;
    this.b = b;
    this.q = q;
    this.a = a;
    oneOverN = 1 / n;
  }
  public DerivativeStructure value(final DerivativeStructure t) {
    return t.negate().add(m).multiply(b).exp().multiply(q).add(1).pow(oneOverN).reciprocal().multiply(k - a).add(a);
  }
  @Deprecated() public UnivariateFunction derivative() {
    return FunctionUtils.toDifferentiableUnivariateFunction(this).derivative();
  }
  public double value(double x) {
    return value(m - x, k, b, q, a, oneOverN);
  }
  private static double value(double mMinusX, double k, double b, double q, double a, double oneOverN) {
    return a + (k - a) / FastMath.pow(1 + q * FastMath.exp(b * mMinusX), oneOverN);
  }
  
  public static class Parametric implements ParametricUnivariateFunction  {
    public double value(double x, double ... param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
      validateParameters(param);
      return Logistic.value(param[1] - x, param[0], param[2], param[3], param[4], 1 / param[5]);
    }
    public double[] gradient(double x, double ... param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
      validateParameters(param);
      final double b = param[2];
      final double q = param[3];
      final double mMinusX = param[1] - x;
      final double oneOverN = 1 / param[5];
      final double exp = FastMath.exp(b * mMinusX);
      final double qExp = q * exp;
      final double qExp1 = qExp + 1;
      final double factor1 = (param[0] - param[4]) * oneOverN / FastMath.pow(qExp1, oneOverN);
      final double factor2 = -factor1 / qExp1;
      final double gk = Logistic.value(mMinusX, 1, b, q, 0, oneOverN);
      final double gm = factor2 * b * qExp;
      final double gb = factor2 * mMinusX * qExp;
      final double gq = factor2 * exp;
      final double ga = Logistic.value(mMinusX, 0, b, q, 1, oneOverN);
      final double gn = factor1 * Math.log(qExp1) * oneOverN;
      return new double[]{ gk, gm, gb, gq, ga, gn } ;
    }
    private void validateParameters(double[] param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
      if(param == null) {
        throw new NullArgumentException();
      }
      if(param.length != 6) {
        throw new DimensionMismatchException(param.length, 6);
      }
      double var_171 = param[5];
      if(var_171 <= 0) {
        throw new NotStrictlyPositiveException(param[5]);
      }
    }
  }
}