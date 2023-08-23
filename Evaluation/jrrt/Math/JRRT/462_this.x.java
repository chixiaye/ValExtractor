package org.apache.commons.math3.analysis.polynomials;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class PolynomialFunctionLagrangeForm implements UnivariateFunction  {
  private double[] coefficients;
  final private double[] x;
  final private double[] y;
  private boolean coefficientsComputed;
  public PolynomialFunctionLagrangeForm(double[] x, double[] y) throws DimensionMismatchException, NumberIsTooSmallException, NonMonotonicSequenceException {
    super();
    this.x = new double[x.length];
    this.y = new double[y.length];
    System.arraycopy(x, 0, this.x, 0, x.length);
    System.arraycopy(y, 0, this.y, 0, y.length);
    coefficientsComputed = false;
    if(!verifyInterpolationArray(x, y, false)) {
      double[] var_462 = this.x;
      MathArrays.sortInPlace(var_462, this.y);
      verifyInterpolationArray(this.x, this.y, true);
    }
  }
  public static boolean verifyInterpolationArray(double[] x, double[] y, boolean abort) throws DimensionMismatchException, NumberIsTooSmallException, NonMonotonicSequenceException {
    if(x.length != y.length) {
      throw new DimensionMismatchException(x.length, y.length);
    }
    if(x.length < 2) {
      throw new NumberIsTooSmallException(LocalizedFormats.WRONG_NUMBER_OF_POINTS, 2, x.length, true);
    }
    return MathArrays.checkOrder(x, MathArrays.OrderDirection.INCREASING, true, abort);
  }
  public static double evaluate(double[] x, double[] y, double z) throws DimensionMismatchException, NumberIsTooSmallException, NonMonotonicSequenceException {
    if(verifyInterpolationArray(x, y, false)) {
      return evaluateInternal(x, y, z);
    }
    final double[] xNew = new double[x.length];
    final double[] yNew = new double[y.length];
    System.arraycopy(x, 0, xNew, 0, x.length);
    System.arraycopy(y, 0, yNew, 0, y.length);
    MathArrays.sortInPlace(xNew, yNew);
    verifyInterpolationArray(xNew, yNew, true);
    return evaluateInternal(xNew, yNew, z);
  }
  private static double evaluateInternal(double[] x, double[] y, double z) {
    int nearest = 0;
    final int n = x.length;
    final double[] c = new double[n];
    final double[] d = new double[n];
    double min_dist = Double.POSITIVE_INFINITY;
    for(int i = 0; i < n; i++) {
      c[i] = y[i];
      d[i] = y[i];
      final double dist = FastMath.abs(z - x[i]);
      if(dist < min_dist) {
        nearest = i;
        min_dist = dist;
      }
    }
    double value = y[nearest];
    for(int i = 1; i < n; i++) {
      for(int j = 0; j < n - i; j++) {
        final double tc = x[j] - z;
        final double td = x[i + j] - z;
        final double divider = x[j] - x[i + j];
        final double w = (c[j + 1] - d[j]) / divider;
        c[j] = tc * w;
        d[j] = td * w;
      }
      if(nearest < 0.5D * (n - i + 1)) {
        value += c[nearest];
      }
      else {
        nearest--;
        value += d[nearest];
      }
    }
    return value;
  }
  public double value(double z) {
    return evaluateInternal(x, y, z);
  }
  public double[] getCoefficients() {
    if(!coefficientsComputed) {
      computeCoefficients();
    }
    double[] out = new double[coefficients.length];
    System.arraycopy(coefficients, 0, out, 0, coefficients.length);
    return out;
  }
  public double[] getInterpolatingPoints() {
    double[] out = new double[x.length];
    System.arraycopy(x, 0, out, 0, x.length);
    return out;
  }
  public double[] getInterpolatingValues() {
    double[] out = new double[y.length];
    System.arraycopy(y, 0, out, 0, y.length);
    return out;
  }
  public int degree() {
    return x.length - 1;
  }
  protected void computeCoefficients() {
    final int n = degree() + 1;
    coefficients = new double[n];
    for(int i = 0; i < n; i++) {
      coefficients[i] = 0.0D;
    }
    final double[] c = new double[n + 1];
    c[0] = 1.0D;
    for(int i = 0; i < n; i++) {
      for(int j = i; j > 0; j--) {
        c[j] = c[j - 1] - c[j] * x[i];
      }
      c[0] *= -x[i];
      c[i + 1] = 1;
    }
    final double[] tc = new double[n];
    for(int i = 0; i < n; i++) {
      double d = 1;
      for(int j = 0; j < n; j++) {
        if(i != j) {
          d *= x[i] - x[j];
        }
      }
      final double t = y[i] / d;
      tc[n - 1] = c[n];
      coefficients[n - 1] += t * tc[n - 1];
      for(int j = n - 2; j >= 0; j--) {
        tc[j] = c[j + 1] + tc[j + 1] * x[i];
        coefficients[j] += t * tc[j];
      }
    }
    coefficientsComputed = true;
  }
}