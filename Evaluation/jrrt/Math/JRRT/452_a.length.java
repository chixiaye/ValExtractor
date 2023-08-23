package org.apache.commons.math3.analysis.polynomials;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.MathUtils;

public class PolynomialFunctionNewtonForm implements UnivariateDifferentiableFunction  {
  private double[] coefficients;
  final private double[] c;
  final private double[] a;
  private boolean coefficientsComputed;
  public PolynomialFunctionNewtonForm(double[] a, double[] c) throws NullArgumentException, NoDataException, DimensionMismatchException {
    super();
    verifyInputArray(a, c);
    this.a = new double[a.length];
    this.c = new double[c.length];
    System.arraycopy(a, 0, this.a, 0, a.length);
    System.arraycopy(c, 0, this.c, 0, c.length);
    coefficientsComputed = false;
  }
  public DerivativeStructure value(final DerivativeStructure t) {
    verifyInputArray(a, c);
    final int n = c.length;
    DerivativeStructure value = new DerivativeStructure(t.getFreeParameters(), t.getOrder(), a[n]);
    for(int i = n - 1; i >= 0; i--) {
      value = t.subtract(c[i]).multiply(value).add(a[i]);
    }
    return value;
  }
  public static double evaluate(double[] a, double[] c, double z) throws NullArgumentException, DimensionMismatchException, NoDataException {
    verifyInputArray(a, c);
    final int n = c.length;
    double value = a[n];
    for(int i = n - 1; i >= 0; i--) {
      value = a[i] + (z - c[i]) * value;
    }
    return value;
  }
  public double value(double z) {
    return evaluate(a, c, z);
  }
  public double[] getCenters() {
    double[] out = new double[c.length];
    System.arraycopy(c, 0, out, 0, c.length);
    return out;
  }
  public double[] getCoefficients() {
    if(!coefficientsComputed) {
      computeCoefficients();
    }
    double[] out = new double[coefficients.length];
    System.arraycopy(coefficients, 0, out, 0, coefficients.length);
    return out;
  }
  public double[] getNewtonCoefficients() {
    int var_452 = a.length;
    double[] out = new double[var_452];
    System.arraycopy(a, 0, out, 0, a.length);
    return out;
  }
  public int degree() {
    return c.length;
  }
  protected void computeCoefficients() {
    final int n = degree();
    coefficients = new double[n + 1];
    for(int i = 0; i <= n; i++) {
      coefficients[i] = 0.0D;
    }
    coefficients[0] = a[n];
    for(int i = n - 1; i >= 0; i--) {
      for(int j = n - i; j > 0; j--) {
        coefficients[j] = coefficients[j - 1] - c[i] * coefficients[j];
      }
      coefficients[0] = a[i] - c[i] * coefficients[0];
    }
    coefficientsComputed = true;
  }
  protected static void verifyInputArray(double[] a, double[] c) throws NullArgumentException, NoDataException, DimensionMismatchException {
    MathUtils.checkNotNull(a);
    MathUtils.checkNotNull(c);
    if(a.length == 0 || c.length == 0) {
      throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
    }
    if(a.length != c.length + 1) {
      throw new DimensionMismatchException(LocalizedFormats.ARRAY_SIZES_SHOULD_HAVE_DIFFERENCE_1, a.length, c.length);
    }
  }
}