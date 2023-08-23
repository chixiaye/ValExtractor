package org.apache.commons.math3.complex;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class Complex implements FieldElement<Complex>, Serializable  {
  final public static Complex I = new Complex(0.0D, 1.0D);
  final public static Complex NaN = new Complex(Double.NaN, Double.NaN);
  final public static Complex INF = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
  final public static Complex ONE = new Complex(1.0D, 0.0D);
  final public static Complex ZERO = new Complex(0.0D, 0.0D);
  final private static long serialVersionUID = -6195664516687396620L;
  final private double imaginary;
  final private double real;
  final private transient boolean isNaN;
  final private transient boolean isInfinite;
  public Complex(double real) {
    this(real, 0.0D);
  }
  public Complex(double real, double imaginary) {
    super();
    this.real = real;
    this.imaginary = imaginary;
    isNaN = Double.isNaN(real) || Double.isNaN(imaginary);
    isInfinite = !isNaN && (Double.isInfinite(real) || Double.isInfinite(imaginary));
  }
  public Complex acos() {
    if(isNaN) {
      return NaN;
    }
    return this.add(this.sqrt1z().multiply(I)).log().multiply(I.negate());
  }
  public Complex add(double addend) {
    if(isNaN || Double.isNaN(addend)) {
      return NaN;
    }
    return createComplex(real + addend, imaginary);
  }
  public Complex add(Complex addend) throws NullArgumentException {
    MathUtils.checkNotNull(addend);
    if(isNaN || addend.isNaN) {
      return NaN;
    }
    return createComplex(real + addend.getReal(), imaginary + addend.getImaginary());
  }
  public Complex asin() {
    if(isNaN) {
      return NaN;
    }
    return sqrt1z().add(this.multiply(I)).log().multiply(I.negate());
  }
  public Complex atan() {
    if(isNaN) {
      return NaN;
    }
    return this.add(I).divide(I.subtract(this)).log().multiply(I.divide(createComplex(2.0D, 0.0D)));
  }
  public Complex conjugate() {
    if(isNaN) {
      return NaN;
    }
    return createComplex(real, -imaginary);
  }
  public Complex cos() {
    if(isNaN) {
      return NaN;
    }
    return createComplex(FastMath.cos(real) * FastMath.cosh(imaginary), -FastMath.sin(real) * FastMath.sinh(imaginary));
  }
  public Complex cosh() {
    if(isNaN) {
      return NaN;
    }
    return createComplex(FastMath.cosh(real) * FastMath.cos(imaginary), FastMath.sinh(real) * FastMath.sin(imaginary));
  }
  protected Complex createComplex(double realPart, double imaginaryPart) {
    return new Complex(realPart, imaginaryPart);
  }
  public Complex divide(double divisor) {
    if(isNaN || Double.isNaN(divisor)) {
      return NaN;
    }
    if(divisor == 0D) {
      return NaN;
    }
    if(Double.isInfinite(divisor)) {
      return !isInfinite() ? ZERO : NaN;
    }
    return createComplex(real / divisor, imaginary / divisor);
  }
  public Complex divide(Complex divisor) throws NullArgumentException {
    MathUtils.checkNotNull(divisor);
    if(isNaN || divisor.isNaN) {
      return NaN;
    }
    final double c = divisor.getReal();
    final double d = divisor.getImaginary();
    if(c == 0.0D && d == 0.0D) {
      return NaN;
    }
    if(divisor.isInfinite() && !isInfinite()) {
      return ZERO;
    }
    if(FastMath.abs(c) < FastMath.abs(d)) {
      double q = c / d;
      double denominator = c * q + d;
      return createComplex((real * q + imaginary) / denominator, (imaginary * q - real) / denominator);
    }
    else {
      double q = d / c;
      double denominator = d * q + c;
      return createComplex((imaginary * q + real) / denominator, (imaginary - real * q) / denominator);
    }
  }
  public Complex exp() {
    if(isNaN) {
      return NaN;
    }
    double expReal = FastMath.exp(real);
    return createComplex(expReal * FastMath.cos(imaginary), expReal * FastMath.sin(imaginary));
  }
  public Complex log() {
    if(isNaN) {
      return NaN;
    }
    return createComplex(FastMath.log(abs()), FastMath.atan2(imaginary, real));
  }
  public Complex multiply(double factor) {
    if(isNaN || Double.isNaN(factor)) {
      return NaN;
    }
    if(Double.isInfinite(real) || Double.isInfinite(imaginary) || Double.isInfinite(factor)) {
      return INF;
    }
    return createComplex(real * factor, imaginary * factor);
  }
  public Complex multiply(final int factor) {
    if(isNaN) {
      return NaN;
    }
    if(Double.isInfinite(real) || Double.isInfinite(imaginary)) {
      return INF;
    }
    return createComplex(real * factor, imaginary * factor);
  }
  public Complex multiply(Complex factor) throws NullArgumentException {
    MathUtils.checkNotNull(factor);
    if(isNaN || factor.isNaN) {
      return NaN;
    }
    if(Double.isInfinite(real) || Double.isInfinite(imaginary) || Double.isInfinite(factor.real) || Double.isInfinite(factor.imaginary)) {
      return INF;
    }
    double var_527 = factor.imaginary;
    return createComplex(real * factor.real - imaginary * factor.imaginary, real * var_527 + imaginary * factor.real);
  }
  public Complex negate() {
    if(isNaN) {
      return NaN;
    }
    return createComplex(-real, -imaginary);
  }
  public Complex pow(double x) {
    return this.log().multiply(x).exp();
  }
  public Complex pow(Complex x) throws NullArgumentException {
    MathUtils.checkNotNull(x);
    return this.log().multiply(x).exp();
  }
  public Complex reciprocal() {
    if(isNaN) {
      return NaN;
    }
    if(real == 0.0D && imaginary == 0.0D) {
      return INF;
    }
    if(isInfinite) {
      return ZERO;
    }
    if(FastMath.abs(real) < FastMath.abs(imaginary)) {
      double q = real / imaginary;
      double scale = 1.D / (real * q + imaginary);
      return createComplex(scale * q, -scale);
    }
    else {
      double q = imaginary / real;
      double scale = 1.D / (imaginary * q + real);
      return createComplex(scale, -scale * q);
    }
  }
  public Complex sin() {
    if(isNaN) {
      return NaN;
    }
    return createComplex(FastMath.sin(real) * FastMath.cosh(imaginary), FastMath.cos(real) * FastMath.sinh(imaginary));
  }
  public Complex sinh() {
    if(isNaN) {
      return NaN;
    }
    return createComplex(FastMath.sinh(real) * FastMath.cos(imaginary), FastMath.cosh(real) * FastMath.sin(imaginary));
  }
  public Complex sqrt() {
    if(isNaN) {
      return NaN;
    }
    if(real == 0.0D && imaginary == 0.0D) {
      return createComplex(0.0D, 0.0D);
    }
    double t = FastMath.sqrt((FastMath.abs(real) + abs()) / 2.0D);
    if(real >= 0.0D) {
      return createComplex(t, imaginary / (2.0D * t));
    }
    else {
      return createComplex(FastMath.abs(imaginary) / (2.0D * t), FastMath.copySign(1D, imaginary) * t);
    }
  }
  public Complex sqrt1z() {
    return createComplex(1.0D, 0.0D).subtract(this.multiply(this)).sqrt();
  }
  public Complex subtract(double subtrahend) {
    if(isNaN || Double.isNaN(subtrahend)) {
      return NaN;
    }
    return createComplex(real - subtrahend, imaginary);
  }
  public Complex subtract(Complex subtrahend) throws NullArgumentException {
    MathUtils.checkNotNull(subtrahend);
    if(isNaN || subtrahend.isNaN) {
      return NaN;
    }
    return createComplex(real - subtrahend.getReal(), imaginary - subtrahend.getImaginary());
  }
  public Complex tan() {
    if(isNaN || Double.isInfinite(real)) {
      return NaN;
    }
    if(imaginary > 20.0D) {
      return createComplex(0.0D, 1.0D);
    }
    if(imaginary < -20.0D) {
      return createComplex(0.0D, -1.0D);
    }
    double real2 = 2.0D * real;
    double imaginary2 = 2.0D * imaginary;
    double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);
    return createComplex(FastMath.sin(real2) / d, FastMath.sinh(imaginary2) / d);
  }
  public Complex tanh() {
    if(isNaN || Double.isInfinite(imaginary)) {
      return NaN;
    }
    if(real > 20.0D) {
      return createComplex(1.0D, 0.0D);
    }
    if(real < -20.0D) {
      return createComplex(-1.0D, 0.0D);
    }
    double real2 = 2.0D * real;
    double imaginary2 = 2.0D * imaginary;
    double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);
    return createComplex(FastMath.sinh(real2) / d, FastMath.sin(imaginary2) / d);
  }
  public static Complex valueOf(double realPart) {
    if(Double.isNaN(realPart)) {
      return NaN;
    }
    return new Complex(realPart);
  }
  public static Complex valueOf(double realPart, double imaginaryPart) {
    if(Double.isNaN(realPart) || Double.isNaN(imaginaryPart)) {
      return NaN;
    }
    return new Complex(realPart, imaginaryPart);
  }
  public ComplexField getField() {
    return ComplexField.getInstance();
  }
  public List<Complex> nthRoot(int n) throws NotPositiveException {
    if(n <= 0) {
      throw new NotPositiveException(LocalizedFormats.CANNOT_COMPUTE_NTH_ROOT_FOR_NEGATIVE_N, n);
    }
    final List<Complex> result = new ArrayList<Complex>();
    if(isNaN) {
      result.add(NaN);
      return result;
    }
    if(isInfinite()) {
      result.add(INF);
      return result;
    }
    final double nthRootOfAbs = FastMath.pow(abs(), 1.0D / n);
    final double nthPhi = getArgument() / n;
    final double slice = 2 * FastMath.PI / n;
    double innerPart = nthPhi;
    for(int k = 0; k < n; k++) {
      final double realPart = nthRootOfAbs * FastMath.cos(innerPart);
      final double imaginaryPart = nthRootOfAbs * FastMath.sin(innerPart);
      result.add(createComplex(realPart, imaginaryPart));
      innerPart += slice;
    }
    return result;
  }
  final protected Object readResolve() {
    return createComplex(real, imaginary);
  }
  @Override() public String toString() {
    return "(" + real + ", " + imaginary + ")";
  }
  @Override() public boolean equals(Object other) {
    if(this == other) {
      return true;
    }
    if(other instanceof Complex) {
      Complex c = (Complex)other;
      if(c.isNaN) {
        return isNaN;
      }
      else {
        return (real == c.real) && (imaginary == c.imaginary);
      }
    }
    return false;
  }
  public boolean isInfinite() {
    return isInfinite;
  }
  public boolean isNaN() {
    return isNaN;
  }
  public double abs() {
    if(isNaN) {
      return Double.NaN;
    }
    if(isInfinite()) {
      return Double.POSITIVE_INFINITY;
    }
    if(FastMath.abs(real) < FastMath.abs(imaginary)) {
      if(imaginary == 0.0D) {
        return FastMath.abs(real);
      }
      double q = real / imaginary;
      return FastMath.abs(imaginary) * FastMath.sqrt(1 + q * q);
    }
    else {
      if(real == 0.0D) {
        return FastMath.abs(imaginary);
      }
      double q = imaginary / real;
      return FastMath.abs(real) * FastMath.sqrt(1 + q * q);
    }
  }
  public double getArgument() {
    return FastMath.atan2(getImaginary(), getReal());
  }
  public double getImaginary() {
    return imaginary;
  }
  public double getReal() {
    return real;
  }
  @Override() public int hashCode() {
    if(isNaN) {
      return 7;
    }
    return 37 * (17 * MathUtils.hash(imaginary) + MathUtils.hash(real));
  }
}