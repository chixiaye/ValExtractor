package org.apache.commons.math3.util;
import org.apache.commons.math3.RealFieldElement;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.exception.DimensionMismatchException;

public class Decimal64 extends Number implements RealFieldElement<Decimal64>, Comparable<Decimal64>  {
  final public static Decimal64 ZERO;
  final public static Decimal64 ONE;
  final public static Decimal64 NEGATIVE_INFINITY;
  final public static Decimal64 POSITIVE_INFINITY;
  final public static Decimal64 NAN;
  final private static long serialVersionUID = 20120227L;
  static {
    ZERO = new Decimal64(0D);
    ONE = new Decimal64(1D);
    NEGATIVE_INFINITY = new Decimal64(Double.NEGATIVE_INFINITY);
    POSITIVE_INFINITY = new Decimal64(Double.POSITIVE_INFINITY);
    NAN = new Decimal64(Double.NaN);
  }
  final private double value;
  public Decimal64(final double x) {
    super();
    this.value = x;
  }
  public Decimal64 abs() {
    return new Decimal64(FastMath.abs(value));
  }
  public Decimal64 acos() {
    return new Decimal64(FastMath.acos(value));
  }
  public Decimal64 acosh() {
    return new Decimal64(FastMath.acosh(value));
  }
  public Decimal64 add(final double a) {
    return new Decimal64(value + a);
  }
  public Decimal64 add(final Decimal64 a) {
    return new Decimal64(this.value + a.value);
  }
  public Decimal64 asin() {
    return new Decimal64(FastMath.asin(value));
  }
  public Decimal64 asinh() {
    return new Decimal64(FastMath.asinh(value));
  }
  public Decimal64 atan() {
    return new Decimal64(FastMath.atan(value));
  }
  public Decimal64 atan2(final Decimal64 x) {
    return new Decimal64(FastMath.atan2(value, x.value));
  }
  public Decimal64 atanh() {
    return new Decimal64(FastMath.atanh(value));
  }
  public Decimal64 cbrt() {
    return new Decimal64(FastMath.cbrt(value));
  }
  public Decimal64 ceil() {
    return new Decimal64(FastMath.ceil(value));
  }
  public Decimal64 copySign(final double sign) {
    return new Decimal64(FastMath.copySign(value, sign));
  }
  public Decimal64 copySign(final Decimal64 sign) {
    return new Decimal64(FastMath.copySign(value, sign.value));
  }
  public Decimal64 cos() {
    return new Decimal64(FastMath.cos(value));
  }
  public Decimal64 cosh() {
    return new Decimal64(FastMath.cosh(value));
  }
  public Decimal64 divide(final double a) {
    return new Decimal64(value / a);
  }
  public Decimal64 divide(final Decimal64 a) {
    return new Decimal64(this.value / a.value);
  }
  public Decimal64 exp() {
    return new Decimal64(FastMath.exp(value));
  }
  public Decimal64 expm1() {
    return new Decimal64(FastMath.expm1(value));
  }
  public Decimal64 floor() {
    return new Decimal64(FastMath.floor(value));
  }
  public Decimal64 hypot(final Decimal64 y) {
    return new Decimal64(FastMath.hypot(value, y.value));
  }
  public Decimal64 linearCombination(final double a1, final Decimal64 b1, final double a2, final Decimal64 b2) {
    return new Decimal64(MathArrays.linearCombination(a1, b1.value, a2, b2.value));
  }
  public Decimal64 linearCombination(final double a1, final Decimal64 b1, final double a2, final Decimal64 b2, final double a3, final Decimal64 b3) {
    return new Decimal64(MathArrays.linearCombination(a1, b1.value, a2, b2.value, a3, b3.value));
  }
  public Decimal64 linearCombination(final double a1, final Decimal64 b1, final double a2, final Decimal64 b2, final double a3, final Decimal64 b3, final double a4, final Decimal64 b4) {
    return new Decimal64(MathArrays.linearCombination(a1, b1.value, a2, b2.value, a3, b3.value, a4, b4.value));
  }
  public Decimal64 linearCombination(final double[] a, final Decimal64[] b) throws DimensionMismatchException {
    if(a.length != b.length) {
      throw new DimensionMismatchException(a.length, b.length);
    }
    final double[] bDouble = new double[b.length];
    for(int i = 0; i < a.length; ++i) {
      bDouble[i] = b[i].value;
    }
    return new Decimal64(MathArrays.linearCombination(a, bDouble));
  }
  public Decimal64 linearCombination(final Decimal64 a1, final Decimal64 b1, final Decimal64 a2, final Decimal64 b2) {
    return new Decimal64(MathArrays.linearCombination(a1.value, b1.value, a2.value, b2.value));
  }
  public Decimal64 linearCombination(final Decimal64 a1, final Decimal64 b1, final Decimal64 a2, final Decimal64 b2, final Decimal64 a3, final Decimal64 b3) {
    return new Decimal64(MathArrays.linearCombination(a1.value, b1.value, a2.value, b2.value, a3.value, b3.value));
  }
  public Decimal64 linearCombination(final Decimal64 a1, final Decimal64 b1, final Decimal64 a2, final Decimal64 b2, final Decimal64 a3, final Decimal64 b3, final Decimal64 a4, final Decimal64 b4) {
    return new Decimal64(MathArrays.linearCombination(a1.value, b1.value, a2.value, b2.value, a3.value, b3.value, a4.value, b4.value));
  }
  public Decimal64 linearCombination(final Decimal64[] a, final Decimal64[] b) throws DimensionMismatchException {
    if(a.length != b.length) {
      throw new DimensionMismatchException(a.length, b.length);
    }
    final double[] aDouble = new double[a.length];
    int var_4214 = b.length;
    final double[] bDouble = new double[var_4214];
    for(int i = 0; i < a.length; ++i) {
      aDouble[i] = a[i].value;
      bDouble[i] = b[i].value;
    }
    return new Decimal64(MathArrays.linearCombination(aDouble, bDouble));
  }
  public Decimal64 log() {
    return new Decimal64(FastMath.log(value));
  }
  public Decimal64 log10() {
    return new Decimal64(FastMath.log10(value));
  }
  public Decimal64 log1p() {
    return new Decimal64(FastMath.log1p(value));
  }
  public Decimal64 multiply(final double a) {
    return new Decimal64(value * a);
  }
  public Decimal64 multiply(final int n) {
    return new Decimal64(n * this.value);
  }
  public Decimal64 multiply(final Decimal64 a) {
    return new Decimal64(this.value * a.value);
  }
  public Decimal64 negate() {
    return new Decimal64(-this.value);
  }
  public Decimal64 pow(final double p) {
    return new Decimal64(FastMath.pow(value, p));
  }
  public Decimal64 pow(final int n) {
    return new Decimal64(FastMath.pow(value, n));
  }
  public Decimal64 pow(final Decimal64 e) {
    return new Decimal64(FastMath.pow(value, e.value));
  }
  public Decimal64 reciprocal() {
    return new Decimal64(1.0D / this.value);
  }
  public Decimal64 remainder(final double a) {
    return new Decimal64(FastMath.IEEEremainder(value, a));
  }
  public Decimal64 remainder(final Decimal64 a) {
    return new Decimal64(FastMath.IEEEremainder(value, a.value));
  }
  public Decimal64 rint() {
    return new Decimal64(FastMath.rint(value));
  }
  public Decimal64 rootN(final int n) {
    if(value < 0) {
      return new Decimal64(-FastMath.pow(-value, 1.0D / n));
    }
    else {
      return new Decimal64(FastMath.pow(value, 1.0D / n));
    }
  }
  public Decimal64 scalb(final int n) {
    return new Decimal64(FastMath.scalb(value, n));
  }
  public Decimal64 signum() {
    return new Decimal64(FastMath.signum(value));
  }
  public Decimal64 sin() {
    return new Decimal64(FastMath.sin(value));
  }
  public Decimal64 sinh() {
    return new Decimal64(FastMath.sinh(value));
  }
  public Decimal64 sqrt() {
    return new Decimal64(FastMath.sqrt(value));
  }
  public Decimal64 subtract(final double a) {
    return new Decimal64(value - a);
  }
  public Decimal64 subtract(final Decimal64 a) {
    return new Decimal64(this.value - a.value);
  }
  public Decimal64 tan() {
    return new Decimal64(FastMath.tan(value));
  }
  public Decimal64 tanh() {
    return new Decimal64(FastMath.tanh(value));
  }
  public Field<Decimal64> getField() {
    return Decimal64Field.getInstance();
  }
  @Override() public String toString() {
    return Double.toString(value);
  }
  @Override() public boolean equals(final Object obj) {
    if(obj instanceof Decimal64) {
      final Decimal64 that = (Decimal64)obj;
      return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(that.value);
    }
    return false;
  }
  public boolean isInfinite() {
    return Double.isInfinite(value);
  }
  public boolean isNaN() {
    return Double.isNaN(value);
  }
  @Override() public byte byteValue() {
    return (byte)value;
  }
  @Override() public double doubleValue() {
    return value;
  }
  public double getReal() {
    return value;
  }
  @Override() public float floatValue() {
    return (float)value;
  }
  public int compareTo(final Decimal64 o) {
    return Double.compare(this.value, o.value);
  }
  @Override() public int hashCode() {
    long v = Double.doubleToLongBits(value);
    return (int)(v ^ (v >>> 32));
  }
  @Override() public int intValue() {
    return (int)value;
  }
  @Override() public long longValue() {
    return (long)value;
  }
  public long round() {
    return FastMath.round(value);
  }
  @Override() public short shortValue() {
    return (short)value;
  }
}