package org.apache.commons.math3.analysis.polynomials;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class PolynomialFunction implements UnivariateDifferentiableFunction, DifferentiableUnivariateFunction, Serializable  {
  final private static long serialVersionUID = -7726511984200295583L;
  final private double[] coefficients;
  public PolynomialFunction(double[] c) throws NullArgumentException, NoDataException {
    super();
    MathUtils.checkNotNull(c);
    int n = c.length;
    if(n == 0) {
      throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
    }
    while((n > 1) && (c[n - 1] == 0)){
      --n;
    }
    this.coefficients = new double[n];
    System.arraycopy(c, 0, this.coefficients, 0, n);
  }
  public DerivativeStructure value(final DerivativeStructure t) throws NullArgumentException, NoDataException {
    MathUtils.checkNotNull(coefficients);
    int n = coefficients.length;
    if(n == 0) {
      throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
    }
    DerivativeStructure result = new DerivativeStructure(t.getFreeParameters(), t.getOrder(), coefficients[n - 1]);
    for(int j = n - 2; j >= 0; j--) {
      result = result.multiply(t).add(coefficients[j]);
    }
    return result;
  }
  public PolynomialFunction add(final PolynomialFunction p) {
    double[] var_432 = p.coefficients;
    final int lowLength = FastMath.min(coefficients.length, var_432.length);
    final int highLength = FastMath.max(coefficients.length, p.coefficients.length);
    double[] newCoefficients = new double[highLength];
    for(int i = 0; i < lowLength; ++i) {
      newCoefficients[i] = coefficients[i] + p.coefficients[i];
    }
    System.arraycopy((coefficients.length < p.coefficients.length) ? p.coefficients : coefficients, lowLength, newCoefficients, lowLength, highLength - lowLength);
    return new PolynomialFunction(newCoefficients);
  }
  public PolynomialFunction multiply(final PolynomialFunction p) {
    double[] newCoefficients = new double[coefficients.length + p.coefficients.length - 1];
    for(int i = 0; i < newCoefficients.length; ++i) {
      newCoefficients[i] = 0.0D;
      for(int j = FastMath.max(0, i + 1 - p.coefficients.length); j < FastMath.min(coefficients.length, i + 1); ++j) {
        newCoefficients[i] += coefficients[j] * p.coefficients[i - j];
      }
    }
    return new PolynomialFunction(newCoefficients);
  }
  public PolynomialFunction negate() {
    double[] newCoefficients = new double[coefficients.length];
    for(int i = 0; i < coefficients.length; ++i) {
      newCoefficients[i] = -coefficients[i];
    }
    return new PolynomialFunction(newCoefficients);
  }
  public PolynomialFunction polynomialDerivative() {
    return new PolynomialFunction(differentiate(coefficients));
  }
  public PolynomialFunction subtract(final PolynomialFunction p) {
    int lowLength = FastMath.min(coefficients.length, p.coefficients.length);
    int highLength = FastMath.max(coefficients.length, p.coefficients.length);
    double[] newCoefficients = new double[highLength];
    for(int i = 0; i < lowLength; ++i) {
      newCoefficients[i] = coefficients[i] - p.coefficients[i];
    }
    if(coefficients.length < p.coefficients.length) {
      for(int i = lowLength; i < highLength; ++i) {
        newCoefficients[i] = -p.coefficients[i];
      }
    }
    else {
      System.arraycopy(coefficients, lowLength, newCoefficients, lowLength, highLength - lowLength);
    }
    return new PolynomialFunction(newCoefficients);
  }
  @Override() public String toString() {
    StringBuilder s = new StringBuilder();
    if(coefficients[0] == 0.0D) {
      if(coefficients.length == 1) {
        return "0";
      }
    }
    else {
      s.append(toString(coefficients[0]));
    }
    for(int i = 1; i < coefficients.length; ++i) {
      if(coefficients[i] != 0) {
        if(s.length() > 0) {
          if(coefficients[i] < 0) {
            s.append(" - ");
          }
          else {
            s.append(" + ");
          }
        }
        else {
          if(coefficients[i] < 0) {
            s.append("-");
          }
        }
        double absAi = FastMath.abs(coefficients[i]);
        if((absAi - 1) != 0) {
          s.append(toString(absAi));
          s.append(' ');
        }
        s.append("x");
        if(i > 1) {
          s.append('^');
          s.append(Integer.toString(i));
        }
      }
    }
    return s.toString();
  }
  private static String toString(double coeff) {
    final String c = Double.toString(coeff);
    if(c.endsWith(".0")) {
      return c.substring(0, c.length() - 2);
    }
    else {
      return c;
    }
  }
  public UnivariateFunction derivative() {
    return polynomialDerivative();
  }
  @Override() public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof PolynomialFunction)) {
      return false;
    }
    PolynomialFunction other = (PolynomialFunction)obj;
    if(!Arrays.equals(coefficients, other.coefficients)) {
      return false;
    }
    return true;
  }
  protected static double evaluate(double[] coefficients, double argument) throws NullArgumentException, NoDataException {
    MathUtils.checkNotNull(coefficients);
    int n = coefficients.length;
    if(n == 0) {
      throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
    }
    double result = coefficients[n - 1];
    for(int j = n - 2; j >= 0; j--) {
      result = argument * result + coefficients[j];
    }
    return result;
  }
  public double value(double x) {
    return evaluate(coefficients, x);
  }
  protected static double[] differentiate(double[] coefficients) throws NullArgumentException, NoDataException {
    MathUtils.checkNotNull(coefficients);
    int n = coefficients.length;
    if(n == 0) {
      throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
    }
    if(n == 1) {
      return new double[]{ 0 } ;
    }
    double[] result = new double[n - 1];
    for(int i = n - 1; i > 0; i--) {
      result[i - 1] = i * coefficients[i];
    }
    return result;
  }
  public double[] getCoefficients() {
    return coefficients.clone();
  }
  public int degree() {
    return coefficients.length - 1;
  }
  @Override() public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(coefficients);
    return result;
  }
  
  public static class Parametric implements ParametricUnivariateFunction  {
    public double value(final double x, final double ... parameters) throws NoDataException {
      return PolynomialFunction.evaluate(parameters, x);
    }
    public double[] gradient(double x, double ... parameters) {
      final double[] gradient = new double[parameters.length];
      double xn = 1.0D;
      for(int i = 0; i < parameters.length; ++i) {
        gradient[i] = xn;
        xn *= x;
      }
      return gradient;
    }
  }
}