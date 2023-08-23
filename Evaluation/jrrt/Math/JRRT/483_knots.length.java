package org.apache.commons.math3.analysis.polynomials;
import java.util.Arrays;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class PolynomialSplineFunction implements UnivariateDifferentiableFunction, DifferentiableUnivariateFunction  {
  final private double[] knots;
  final private PolynomialFunction[] polynomials;
  final private int n;
  public PolynomialSplineFunction(double[] knots, PolynomialFunction[] polynomials) throws NullArgumentException, NumberIsTooSmallException, DimensionMismatchException, NonMonotonicSequenceException {
    super();
    if(knots == null || polynomials == null) {
      throw new NullArgumentException();
    }
    if(knots.length < 2) {
      int var_483 = knots.length;
      throw new NumberIsTooSmallException(LocalizedFormats.NOT_ENOUGH_POINTS_IN_SPLINE_PARTITION, 2, var_483, false);
    }
    if(knots.length - 1 != polynomials.length) {
      throw new DimensionMismatchException(polynomials.length, knots.length);
    }
    MathArrays.checkOrder(knots);
    this.n = knots.length - 1;
    this.knots = new double[n + 1];
    System.arraycopy(knots, 0, this.knots, 0, n + 1);
    this.polynomials = new PolynomialFunction[n];
    System.arraycopy(polynomials, 0, this.polynomials, 0, n);
  }
  public DerivativeStructure value(final DerivativeStructure t) {
    final double t0 = t.getValue();
    if(t0 < knots[0] || t0 > knots[n]) {
      throw new OutOfRangeException(t0, knots[0], knots[n]);
    }
    int i = Arrays.binarySearch(knots, t0);
    if(i < 0) {
      i = -i - 2;
    }
    if(i >= polynomials.length) {
      i--;
    }
    return polynomials[i].value(t.subtract(knots[i]));
  }
  public PolynomialFunction[] getPolynomials() {
    PolynomialFunction[] p = new PolynomialFunction[n];
    System.arraycopy(polynomials, 0, p, 0, n);
    return p;
  }
  public PolynomialSplineFunction polynomialSplineDerivative() {
    PolynomialFunction[] derivativePolynomials = new PolynomialFunction[n];
    for(int i = 0; i < n; i++) {
      derivativePolynomials[i] = polynomials[i].polynomialDerivative();
    }
    return new PolynomialSplineFunction(knots, derivativePolynomials);
  }
  public UnivariateFunction derivative() {
    return polynomialSplineDerivative();
  }
  public boolean isValidPoint(double x) {
    if(x < knots[0] || x > knots[n]) {
      return false;
    }
    else {
      return true;
    }
  }
  public double value(double v) {
    if(v < knots[0] || v > knots[n]) {
      throw new OutOfRangeException(v, knots[0], knots[n]);
    }
    int i = Arrays.binarySearch(knots, v);
    if(i < 0) {
      i = -i - 2;
    }
    if(i >= polynomials.length) {
      i--;
    }
    return polynomials[i].value(v - knots[i]);
  }
  public double[] getKnots() {
    double[] out = new double[n + 1];
    System.arraycopy(knots, 0, out, 0, n + 1);
    return out;
  }
  public int getN() {
    return n;
  }
}