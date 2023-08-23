package org.apache.commons.math3.complex;
import java.io.Serializable;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

final public class Quaternion implements Serializable  {
  final public static Quaternion IDENTITY = new Quaternion(1, 0, 0, 0);
  final public static Quaternion ZERO = new Quaternion(0, 0, 0, 0);
  final public static Quaternion I = new Quaternion(0, 1, 0, 0);
  final public static Quaternion J = new Quaternion(0, 0, 1, 0);
  final public static Quaternion K = new Quaternion(0, 0, 0, 1);
  final private static long serialVersionUID = 20092012L;
  final private double q0;
  final private double q1;
  final private double q2;
  final private double q3;
  public Quaternion(final double a, final double b, final double c, final double d) {
    super();
    this.q0 = a;
    this.q1 = b;
    this.q2 = c;
    this.q3 = d;
  }
  public Quaternion(final double scalar, final double[] v) throws DimensionMismatchException {
    super();
    int var_528 = v.length;
    if(var_528 != 3) {
      throw new DimensionMismatchException(v.length, 3);
    }
    this.q0 = scalar;
    this.q1 = v[0];
    this.q2 = v[1];
    this.q3 = v[2];
  }
  public Quaternion(final double[] v) {
    this(0, v);
  }
  public Quaternion add(final Quaternion q) {
    return add(this, q);
  }
  public static Quaternion add(final Quaternion q1, final Quaternion q2) {
    return new Quaternion(q1.getQ0() + q2.getQ0(), q1.getQ1() + q2.getQ1(), q1.getQ2() + q2.getQ2(), q1.getQ3() + q2.getQ3());
  }
  public Quaternion getConjugate() {
    return new Quaternion(q0, -q1, -q2, -q3);
  }
  public Quaternion getInverse() {
    final double squareNorm = q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3;
    if(squareNorm < Precision.SAFE_MIN) {
      throw new ZeroException(LocalizedFormats.NORM, squareNorm);
    }
    return new Quaternion(q0 / squareNorm, -q1 / squareNorm, -q2 / squareNorm, -q3 / squareNorm);
  }
  public Quaternion getPositivePolarForm() {
    if(getQ0() < 0) {
      final Quaternion unitQ = normalize();
      return new Quaternion(-unitQ.getQ0(), -unitQ.getQ1(), -unitQ.getQ2(), -unitQ.getQ3());
    }
    else {
      return this.normalize();
    }
  }
  public Quaternion multiply(final double alpha) {
    return new Quaternion(alpha * q0, alpha * q1, alpha * q2, alpha * q3);
  }
  public Quaternion multiply(final Quaternion q) {
    return multiply(this, q);
  }
  public static Quaternion multiply(final Quaternion q1, final Quaternion q2) {
    final double q1a = q1.getQ0();
    final double q1b = q1.getQ1();
    final double q1c = q1.getQ2();
    final double q1d = q1.getQ3();
    final double q2a = q2.getQ0();
    final double q2b = q2.getQ1();
    final double q2c = q2.getQ2();
    final double q2d = q2.getQ3();
    final double w = q1a * q2a - q1b * q2b - q1c * q2c - q1d * q2d;
    final double x = q1a * q2b + q1b * q2a + q1c * q2d - q1d * q2c;
    final double y = q1a * q2c - q1b * q2d + q1c * q2a + q1d * q2b;
    final double z = q1a * q2d + q1b * q2c - q1c * q2b + q1d * q2a;
    return new Quaternion(w, x, y, z);
  }
  public Quaternion normalize() {
    final double norm = getNorm();
    if(norm < Precision.SAFE_MIN) {
      throw new ZeroException(LocalizedFormats.NORM, norm);
    }
    return new Quaternion(q0 / norm, q1 / norm, q2 / norm, q3 / norm);
  }
  public Quaternion subtract(final Quaternion q) {
    return subtract(this, q);
  }
  public static Quaternion subtract(final Quaternion q1, final Quaternion q2) {
    return new Quaternion(q1.getQ0() - q2.getQ0(), q1.getQ1() - q2.getQ1(), q1.getQ2() - q2.getQ2(), q1.getQ3() - q2.getQ3());
  }
  @Override() public String toString() {
    final String sp = " ";
    final StringBuilder s = new StringBuilder();
    s.append("[").append(q0).append(sp).append(q1).append(sp).append(q2).append(sp).append(q3).append("]");
    return s.toString();
  }
  @Override() public boolean equals(Object other) {
    if(this == other) {
      return true;
    }
    if(other instanceof Quaternion) {
      final Quaternion q = (Quaternion)other;
      return q0 == q.getQ0() && q1 == q.getQ1() && q2 == q.getQ2() && q3 == q.getQ3();
    }
    return false;
  }
  public boolean equals(final Quaternion q, final double eps) {
    return Precision.equals(q0, q.getQ0(), eps) && Precision.equals(q1, q.getQ1(), eps) && Precision.equals(q2, q.getQ2(), eps) && Precision.equals(q3, q.getQ3(), eps);
  }
  public boolean isPureQuaternion(double eps) {
    return FastMath.abs(getQ0()) <= eps;
  }
  public boolean isUnitQuaternion(double eps) {
    return Precision.equals(getNorm(), 1D, eps);
  }
  public double dotProduct(final Quaternion q) {
    return dotProduct(this, q);
  }
  public static double dotProduct(final Quaternion q1, final Quaternion q2) {
    return q1.getQ0() * q2.getQ0() + q1.getQ1() * q2.getQ1() + q1.getQ2() * q2.getQ2() + q1.getQ3() * q2.getQ3();
  }
  public double getNorm() {
    return FastMath.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
  }
  public double getQ0() {
    return q0;
  }
  public double getQ1() {
    return q1;
  }
  public double getQ2() {
    return q2;
  }
  public double getQ3() {
    return q3;
  }
  public double getScalarPart() {
    return getQ0();
  }
  public double[] getVectorPart() {
    return new double[]{ getQ1(), getQ2(), getQ3() } ;
  }
  @Override() public int hashCode() {
    int result = 17;
    for (double comp : new double[]{ q0, q1, q2, q3 } ) {
      final int c = MathUtils.hash(comp);
      result = 31 * result + c;
    }
    return result;
  }
}