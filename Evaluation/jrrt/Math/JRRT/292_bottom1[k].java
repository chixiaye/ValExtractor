package org.apache.commons.math3.analysis.interpolation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableVectorFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.CombinatoricsUtils;

public class HermiteInterpolator implements UnivariateDifferentiableVectorFunction  {
  final private List<Double> abscissae;
  final private List<double[]> topDiagonal;
  final private List<double[]> bottomDiagonal;
  public HermiteInterpolator() {
    super();
    this.abscissae = new ArrayList<Double>();
    this.topDiagonal = new ArrayList<double[]>();
    this.bottomDiagonal = new ArrayList<double[]>();
  }
  public DerivativeStructure[] value(final DerivativeStructure x) throws NoDataException {
    checkInterpolation();
    final DerivativeStructure[] value = new DerivativeStructure[topDiagonal.get(0).length];
    Arrays.fill(value, x.getField().getZero());
    DerivativeStructure valueCoeff = x.getField().getOne();
    for(int i = 0; i < topDiagonal.size(); ++i) {
      double[] dividedDifference = topDiagonal.get(i);
      for(int k = 0; k < value.length; ++k) {
        value[k] = value[k].add(valueCoeff.multiply(dividedDifference[k]));
      }
      final DerivativeStructure deltaX = x.subtract(abscissae.get(i));
      valueCoeff = valueCoeff.multiply(deltaX);
    }
    return value;
  }
  private PolynomialFunction polynomial(double ... c) {
    return new PolynomialFunction(c);
  }
  public PolynomialFunction[] getPolynomials() throws NoDataException {
    checkInterpolation();
    final PolynomialFunction zero = polynomial(0);
    PolynomialFunction[] polynomials = new PolynomialFunction[topDiagonal.get(0).length];
    for(int i = 0; i < polynomials.length; ++i) {
      polynomials[i] = zero;
    }
    PolynomialFunction coeff = polynomial(1);
    for(int i = 0; i < topDiagonal.size(); ++i) {
      double[] tdi = topDiagonal.get(i);
      for(int k = 0; k < polynomials.length; ++k) {
        polynomials[k] = polynomials[k].add(coeff.multiply(polynomial(tdi[k])));
      }
      coeff = coeff.multiply(polynomial(-abscissae.get(i), 1.0D));
    }
    return polynomials;
  }
  public double[] value(double x) throws NoDataException {
    checkInterpolation();
    final double[] value = new double[topDiagonal.get(0).length];
    double valueCoeff = 1;
    for(int i = 0; i < topDiagonal.size(); ++i) {
      double[] dividedDifference = topDiagonal.get(i);
      for(int k = 0; k < value.length; ++k) {
        value[k] += dividedDifference[k] * valueCoeff;
      }
      final double deltaX = x - abscissae.get(i);
      valueCoeff *= deltaX;
    }
    return value;
  }
  public void addSamplePoint(final double x, final double[] ... value) throws ZeroException, MathArithmeticException {
    for(int i = 0; i < value.length; ++i) {
      final double[] y = value[i].clone();
      if(i > 1) {
        double inv = 1.0D / CombinatoricsUtils.factorial(i);
        for(int j = 0; j < y.length; ++j) {
          y[j] *= inv;
        }
      }
      final int n = abscissae.size();
      bottomDiagonal.add(n - i, y);
      double[] bottom0 = y;
      for(int j = i; j < n; ++j) {
        final double[] bottom1 = bottomDiagonal.get(n - (j + 1));
        final double inv = 1.0D / (x - abscissae.get(n - (j + 1)));
        if(Double.isInfinite(inv)) {
          throw new ZeroException(LocalizedFormats.DUPLICATED_ABSCISSA_DIVISION_BY_ZERO, x);
        }
        for(int k = 0; k < y.length; ++k) {
          double var_292 = bottom1[k];
          bottom1[k] = inv * (bottom0[k] - var_292);
        }
        bottom0 = bottom1;
      }
      topDiagonal.add(bottom0.clone());
      abscissae.add(x);
    }
  }
  private void checkInterpolation() throws NoDataException {
    if(abscissae.isEmpty()) {
      throw new NoDataException(LocalizedFormats.EMPTY_INTERPOLATION_SAMPLE);
    }
  }
}