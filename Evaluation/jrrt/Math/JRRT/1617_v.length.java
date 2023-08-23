package org.apache.commons.math3.geometry.euclidean.twod;
import java.text.NumberFormat;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.geometry.Space;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class Vector2D implements Vector<Euclidean2D>  {
  final public static Vector2D ZERO = new Vector2D(0, 0);
  final public static Vector2D NaN = new Vector2D(Double.NaN, Double.NaN);
  final public static Vector2D POSITIVE_INFINITY = new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
  final public static Vector2D NEGATIVE_INFINITY = new Vector2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
  final private static long serialVersionUID = 266938651998679754L;
  final private double x;
  final private double y;
  public Vector2D(double a, Vector2D u) {
    super();
    this.x = a * u.x;
    this.y = a * u.y;
  }
  public Vector2D(double a1, Vector2D u1, double a2, Vector2D u2) {
    super();
    this.x = a1 * u1.x + a2 * u2.x;
    this.y = a1 * u1.y + a2 * u2.y;
  }
  public Vector2D(double a1, Vector2D u1, double a2, Vector2D u2, double a3, Vector2D u3) {
    super();
    this.x = a1 * u1.x + a2 * u2.x + a3 * u3.x;
    this.y = a1 * u1.y + a2 * u2.y + a3 * u3.y;
  }
  public Vector2D(double a1, Vector2D u1, double a2, Vector2D u2, double a3, Vector2D u3, double a4, Vector2D u4) {
    super();
    this.x = a1 * u1.x + a2 * u2.x + a3 * u3.x + a4 * u4.x;
    this.y = a1 * u1.y + a2 * u2.y + a3 * u3.y + a4 * u4.y;
  }
  public Vector2D(double x, double y) {
    super();
    this.x = x;
    this.y = y;
  }
  public Vector2D(double[] v) throws DimensionMismatchException {
    super();
    if(v.length != 2) {
      int var_1617 = v.length;
      throw new DimensionMismatchException(var_1617, 2);
    }
    this.x = v[0];
    this.y = v[1];
  }
  public Space getSpace() {
    return Euclidean2D.getInstance();
  }
  @Override() public String toString() {
    return Vector2DFormat.getInstance().format(this);
  }
  public String toString(final NumberFormat format) {
    return new Vector2DFormat(format).format(this);
  }
  public Vector2D add(double factor, Vector<Euclidean2D> v) {
    Vector2D v2 = (Vector2D)v;
    return new Vector2D(x + factor * v2.getX(), y + factor * v2.getY());
  }
  public Vector2D add(Vector<Euclidean2D> v) {
    Vector2D v2 = (Vector2D)v;
    return new Vector2D(x + v2.getX(), y + v2.getY());
  }
  public Vector2D getZero() {
    return ZERO;
  }
  public Vector2D negate() {
    return new Vector2D(-x, -y);
  }
  public Vector2D normalize() throws MathArithmeticException {
    double s = getNorm();
    if(s == 0) {
      throw new MathArithmeticException(LocalizedFormats.CANNOT_NORMALIZE_A_ZERO_NORM_VECTOR);
    }
    return scalarMultiply(1 / s);
  }
  public Vector2D scalarMultiply(double a) {
    return new Vector2D(a * x, a * y);
  }
  public Vector2D subtract(double factor, Vector<Euclidean2D> v) {
    Vector2D v2 = (Vector2D)v;
    return new Vector2D(x - factor * v2.getX(), y - factor * v2.getY());
  }
  public Vector2D subtract(Vector<Euclidean2D> p) {
    Vector2D p3 = (Vector2D)p;
    return new Vector2D(x - p3.x, y - p3.y);
  }
  @Override() public boolean equals(Object other) {
    if(this == other) {
      return true;
    }
    if(other instanceof Vector2D) {
      final Vector2D rhs = (Vector2D)other;
      if(rhs.isNaN()) {
        return this.isNaN();
      }
      return (x == rhs.x) && (y == rhs.y);
    }
    return false;
  }
  public boolean isInfinite() {
    return !isNaN() && (Double.isInfinite(x) || Double.isInfinite(y));
  }
  public boolean isNaN() {
    return Double.isNaN(x) || Double.isNaN(y);
  }
  public double distance(Vector<Euclidean2D> p) {
    Vector2D p3 = (Vector2D)p;
    final double dx = p3.x - x;
    final double dy = p3.y - y;
    return FastMath.sqrt(dx * dx + dy * dy);
  }
  public static double distance(Vector2D p1, Vector2D p2) {
    return p1.distance(p2);
  }
  public double distance1(Vector<Euclidean2D> p) {
    Vector2D p3 = (Vector2D)p;
    final double dx = FastMath.abs(p3.x - x);
    final double dy = FastMath.abs(p3.y - y);
    return dx + dy;
  }
  public double distanceInf(Vector<Euclidean2D> p) {
    Vector2D p3 = (Vector2D)p;
    final double dx = FastMath.abs(p3.x - x);
    final double dy = FastMath.abs(p3.y - y);
    return FastMath.max(dx, dy);
  }
  public static double distanceInf(Vector2D p1, Vector2D p2) {
    return p1.distanceInf(p2);
  }
  public double distanceSq(Vector<Euclidean2D> p) {
    Vector2D p3 = (Vector2D)p;
    final double dx = p3.x - x;
    final double dy = p3.y - y;
    return dx * dx + dy * dy;
  }
  public static double distanceSq(Vector2D p1, Vector2D p2) {
    return p1.distanceSq(p2);
  }
  public double dotProduct(final Vector<Euclidean2D> v) {
    final Vector2D v2 = (Vector2D)v;
    return x * v2.x + y * v2.y;
  }
  public double getNorm() {
    return FastMath.sqrt(x * x + y * y);
  }
  public double getNorm1() {
    return FastMath.abs(x) + FastMath.abs(y);
  }
  public double getNormInf() {
    return FastMath.max(FastMath.abs(x), FastMath.abs(y));
  }
  public double getNormSq() {
    return x * x + y * y;
  }
  public double getX() {
    return x;
  }
  public double getY() {
    return y;
  }
  public double[] toArray() {
    return new double[]{ x, y } ;
  }
  @Override() public int hashCode() {
    if(isNaN()) {
      return 542;
    }
    return 122 * (76 * MathUtils.hash(x) + MathUtils.hash(y));
  }
}