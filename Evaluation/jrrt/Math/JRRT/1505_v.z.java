package org.apache.commons.math3.geometry.euclidean.threed;
import java.io.Serializable;
import java.text.NumberFormat;
import org.apache.commons.math3.RealFieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
public class FieldVector3D<T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.FieldVector3D@T>> implements Serializable  {
  final private static long serialVersionUID = 20130224L;
  final private T x;
  final private T y;
  final private T z;
  public FieldVector3D(final T a, final FieldVector3D<T> u) {
    super();
    this.x = a.multiply(u.x);
    this.y = a.multiply(u.y);
    this.z = a.multiply(u.z);
  }
  public FieldVector3D(final T a, final Vector3D u) {
    super();
    this.x = a.multiply(u.getX());
    this.y = a.multiply(u.getY());
    this.z = a.multiply(u.getZ());
  }
  public FieldVector3D(final T a1, final FieldVector3D<T> u1, final T a2, final FieldVector3D<T> u2) {
    super();
    final T prototype = a1;
    this.x = prototype.linearCombination(a1, u1.getX(), a2, u2.getX());
    this.y = prototype.linearCombination(a1, u1.getY(), a2, u2.getY());
    this.z = prototype.linearCombination(a1, u1.getZ(), a2, u2.getZ());
  }
  public FieldVector3D(final T a1, final FieldVector3D<T> u1, final T a2, final FieldVector3D<T> u2, final T a3, final FieldVector3D<T> u3) {
    super();
    final T prototype = a1;
    this.x = prototype.linearCombination(a1, u1.getX(), a2, u2.getX(), a3, u3.getX());
    this.y = prototype.linearCombination(a1, u1.getY(), a2, u2.getY(), a3, u3.getY());
    this.z = prototype.linearCombination(a1, u1.getZ(), a2, u2.getZ(), a3, u3.getZ());
  }
  public FieldVector3D(final T a1, final FieldVector3D<T> u1, final T a2, final FieldVector3D<T> u2, final T a3, final FieldVector3D<T> u3, final T a4, final FieldVector3D<T> u4) {
    super();
    final T prototype = a1;
    this.x = prototype.linearCombination(a1, u1.getX(), a2, u2.getX(), a3, u3.getX(), a4, u4.getX());
    this.y = prototype.linearCombination(a1, u1.getY(), a2, u2.getY(), a3, u3.getY(), a4, u4.getY());
    this.z = prototype.linearCombination(a1, u1.getZ(), a2, u2.getZ(), a3, u3.getZ(), a4, u4.getZ());
  }
  public FieldVector3D(final T a1, final Vector3D u1, final T a2, final Vector3D u2) {
    super();
    final T prototype = a1;
    this.x = prototype.linearCombination(u1.getX(), a1, u2.getX(), a2);
    this.y = prototype.linearCombination(u1.getY(), a1, u2.getY(), a2);
    this.z = prototype.linearCombination(u1.getZ(), a1, u2.getZ(), a2);
  }
  public FieldVector3D(final T a1, final Vector3D u1, final T a2, final Vector3D u2, final T a3, final Vector3D u3) {
    super();
    final T prototype = a1;
    this.x = prototype.linearCombination(u1.getX(), a1, u2.getX(), a2, u3.getX(), a3);
    this.y = prototype.linearCombination(u1.getY(), a1, u2.getY(), a2, u3.getY(), a3);
    this.z = prototype.linearCombination(u1.getZ(), a1, u2.getZ(), a2, u3.getZ(), a3);
  }
  public FieldVector3D(final T a1, final Vector3D u1, final T a2, final Vector3D u2, final T a3, final Vector3D u3, final T a4, final Vector3D u4) {
    super();
    final T prototype = a1;
    this.x = prototype.linearCombination(u1.getX(), a1, u2.getX(), a2, u3.getX(), a3, u4.getX(), a4);
    this.y = prototype.linearCombination(u1.getY(), a1, u2.getY(), a2, u3.getY(), a3, u4.getY(), a4);
    this.z = prototype.linearCombination(u1.getZ(), a1, u2.getZ(), a2, u3.getZ(), a3, u4.getZ(), a4);
  }
  public FieldVector3D(final T alpha, final T delta) {
    super();
    T cosDelta = delta.cos();
    this.x = alpha.cos().multiply(cosDelta);
    this.y = alpha.sin().multiply(cosDelta);
    this.z = delta.sin();
  }
  public FieldVector3D(final T x, final T y, final T z) {
    super();
    this.x = x;
    this.y = y;
    this.z = z;
  }
  public FieldVector3D(final T[] v) throws DimensionMismatchException {
    super();
    if(v.length != 3) {
      throw new DimensionMismatchException(v.length, 3);
    }
    this.x = v[0];
    this.y = v[1];
    this.z = v[2];
  }
  public FieldVector3D(final double a, final FieldVector3D<T> u) {
    super();
    this.x = u.x.multiply(a);
    this.y = u.y.multiply(a);
    this.z = u.z.multiply(a);
  }
  public FieldVector3D(final double a1, final FieldVector3D<T> u1, final double a2, final FieldVector3D<T> u2) {
    super();
    final T prototype = u1.getX();
    this.x = prototype.linearCombination(a1, u1.getX(), a2, u2.getX());
    this.y = prototype.linearCombination(a1, u1.getY(), a2, u2.getY());
    this.z = prototype.linearCombination(a1, u1.getZ(), a2, u2.getZ());
  }
  public FieldVector3D(final double a1, final FieldVector3D<T> u1, final double a2, final FieldVector3D<T> u2, final double a3, final FieldVector3D<T> u3) {
    super();
    final T prototype = u1.getX();
    this.x = prototype.linearCombination(a1, u1.getX(), a2, u2.getX(), a3, u3.getX());
    this.y = prototype.linearCombination(a1, u1.getY(), a2, u2.getY(), a3, u3.getY());
    this.z = prototype.linearCombination(a1, u1.getZ(), a2, u2.getZ(), a3, u3.getZ());
  }
  public FieldVector3D(final double a1, final FieldVector3D<T> u1, final double a2, final FieldVector3D<T> u2, final double a3, final FieldVector3D<T> u3, final double a4, final FieldVector3D<T> u4) {
    super();
    final T prototype = u1.getX();
    this.x = prototype.linearCombination(a1, u1.getX(), a2, u2.getX(), a3, u3.getX(), a4, u4.getX());
    this.y = prototype.linearCombination(a1, u1.getY(), a2, u2.getY(), a3, u3.getY(), a4, u4.getY());
    this.z = prototype.linearCombination(a1, u1.getZ(), a2, u2.getZ(), a3, u3.getZ(), a4, u4.getZ());
  }
  public FieldVector3D<T> add(final double factor, final FieldVector3D<T> v) {
    return new FieldVector3D<T>(1.0D, this, factor, v);
  }
  public FieldVector3D<T> add(final double factor, final Vector3D v) {
    return new FieldVector3D<T>(x.add(factor * v.getX()), y.add(factor * v.getY()), z.add(factor * v.getZ()));
  }
  public FieldVector3D<T> add(final T factor, final FieldVector3D<T> v) {
    return new FieldVector3D<T>(x.getField().getOne(), this, factor, v);
  }
  public FieldVector3D<T> add(final T factor, final Vector3D v) {
    return new FieldVector3D<T>(x.add(factor.multiply(v.getX())), y.add(factor.multiply(v.getY())), z.add(factor.multiply(v.getZ())));
  }
  public FieldVector3D<T> add(final FieldVector3D<T> v) {
    return new FieldVector3D<T>(x.add(v.x), y.add(v.y), z.add(v.z));
  }
  public FieldVector3D<T> add(final Vector3D v) {
    return new FieldVector3D<T>(x.add(v.getX()), y.add(v.getY()), z.add(v.getZ()));
  }
  public FieldVector3D<T> crossProduct(final FieldVector3D<T> v) {
    T var_1505 = v.z;
    return new FieldVector3D<T>(x.linearCombination(y, var_1505, z.negate(), v.y), y.linearCombination(z, v.x, x.negate(), v.z), z.linearCombination(x, v.y, y.negate(), v.x));
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> FieldVector3D<T> crossProduct(final FieldVector3D<T> v1, final FieldVector3D<T> v2) {
    return v1.crossProduct(v2);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> FieldVector3D<T> crossProduct(final FieldVector3D<T> v1, final Vector3D v2) {
    return v1.crossProduct(v2);
  }
  public FieldVector3D<T> crossProduct(final Vector3D v) {
    return new FieldVector3D<T>(x.linearCombination(v.getZ(), y, -v.getY(), z), y.linearCombination(v.getX(), z, -v.getZ(), x), z.linearCombination(v.getY(), x, -v.getX(), y));
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> FieldVector3D<T> crossProduct(final Vector3D v1, final FieldVector3D<T> v2) {
    return new FieldVector3D<T>(v2.x.linearCombination(v1.getY(), v2.z, -v1.getZ(), v2.y), v2.y.linearCombination(v1.getZ(), v2.x, -v1.getX(), v2.z), v2.z.linearCombination(v1.getX(), v2.y, -v1.getY(), v2.x));
  }
  public FieldVector3D<T> negate() {
    return new FieldVector3D<T>(x.negate(), y.negate(), z.negate());
  }
  public FieldVector3D<T> normalize() throws MathArithmeticException {
    final T s = getNorm();
    if(s.getReal() == 0) {
      throw new MathArithmeticException(LocalizedFormats.CANNOT_NORMALIZE_A_ZERO_NORM_VECTOR);
    }
    return scalarMultiply(s.reciprocal());
  }
  public FieldVector3D<T> orthogonal() throws MathArithmeticException {
    final double threshold = 0.6D * getNorm().getReal();
    if(threshold == 0) {
      throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
    }
    if(FastMath.abs(x.getReal()) <= threshold) {
      final T inverse = y.multiply(y).add(z.multiply(z)).sqrt().reciprocal();
      return new FieldVector3D<T>(inverse.getField().getZero(), inverse.multiply(z), inverse.multiply(y).negate());
    }
    else 
      if(FastMath.abs(y.getReal()) <= threshold) {
        final T inverse = x.multiply(x).add(z.multiply(z)).sqrt().reciprocal();
        return new FieldVector3D<T>(inverse.multiply(z).negate(), inverse.getField().getZero(), inverse.multiply(x));
      }
      else {
        final T inverse = x.multiply(x).add(y.multiply(y)).sqrt().reciprocal();
        return new FieldVector3D<T>(inverse.multiply(y), inverse.multiply(x).negate(), inverse.getField().getZero());
      }
  }
  public FieldVector3D<T> scalarMultiply(final double a) {
    return new FieldVector3D<T>(x.multiply(a), y.multiply(a), z.multiply(a));
  }
  public FieldVector3D<T> scalarMultiply(final T a) {
    return new FieldVector3D<T>(x.multiply(a), y.multiply(a), z.multiply(a));
  }
  public FieldVector3D<T> subtract(final double factor, final FieldVector3D<T> v) {
    return new FieldVector3D<T>(1.0D, this, -factor, v);
  }
  public FieldVector3D<T> subtract(final double factor, final Vector3D v) {
    return new FieldVector3D<T>(x.subtract(factor * v.getX()), y.subtract(factor * v.getY()), z.subtract(factor * v.getZ()));
  }
  public FieldVector3D<T> subtract(final T factor, final FieldVector3D<T> v) {
    return new FieldVector3D<T>(x.getField().getOne(), this, factor.negate(), v);
  }
  public FieldVector3D<T> subtract(final T factor, final Vector3D v) {
    return new FieldVector3D<T>(x.subtract(factor.multiply(v.getX())), y.subtract(factor.multiply(v.getY())), z.subtract(factor.multiply(v.getZ())));
  }
  public FieldVector3D<T> subtract(final FieldVector3D<T> v) {
    return new FieldVector3D<T>(x.subtract(v.x), y.subtract(v.y), z.subtract(v.z));
  }
  public FieldVector3D<T> subtract(final Vector3D v) {
    return new FieldVector3D<T>(x.subtract(v.getX()), y.subtract(v.getY()), z.subtract(v.getZ()));
  }
  @Override() public String toString() {
    return Vector3DFormat.getInstance().format(toVector3D());
  }
  public String toString(final NumberFormat format) {
    return new Vector3DFormat(format).format(toVector3D());
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T angle(final FieldVector3D<T> v1, final FieldVector3D<T> v2) throws MathArithmeticException {
    final T normProduct = v1.getNorm().multiply(v2.getNorm());
    if(normProduct.getReal() == 0) {
      throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
    }
    final T dot = dotProduct(v1, v2);
    final double threshold = normProduct.getReal() * 0.9999D;
    if((dot.getReal() < -threshold) || (dot.getReal() > threshold)) {
      FieldVector3D<T> v3 = crossProduct(v1, v2);
      if(dot.getReal() >= 0) {
        return v3.getNorm().divide(normProduct).asin();
      }
      return v3.getNorm().divide(normProduct).asin().subtract(FastMath.PI).negate();
    }
    return dot.divide(normProduct).acos();
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T angle(final FieldVector3D<T> v1, final Vector3D v2) throws MathArithmeticException {
    final T normProduct = v1.getNorm().multiply(v2.getNorm());
    if(normProduct.getReal() == 0) {
      throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
    }
    final T dot = dotProduct(v1, v2);
    final double threshold = normProduct.getReal() * 0.9999D;
    if((dot.getReal() < -threshold) || (dot.getReal() > threshold)) {
      FieldVector3D<T> v3 = crossProduct(v1, v2);
      if(dot.getReal() >= 0) {
        return v3.getNorm().divide(normProduct).asin();
      }
      return v3.getNorm().divide(normProduct).asin().subtract(FastMath.PI).negate();
    }
    return dot.divide(normProduct).acos();
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T angle(final Vector3D v1, final FieldVector3D<T> v2) throws MathArithmeticException {
    return angle(v2, v1);
  }
  public T distance(final FieldVector3D<T> v) {
    final T dx = v.x.subtract(x);
    final T dy = v.y.subtract(y);
    final T dz = v.z.subtract(z);
    return dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz)).sqrt();
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distance(final FieldVector3D<T> v1, final FieldVector3D<T> v2) {
    return v1.distance(v2);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distance(final FieldVector3D<T> v1, final Vector3D v2) {
    return v1.distance(v2);
  }
  public T distance(final Vector3D v) {
    final T dx = x.subtract(v.getX());
    final T dy = y.subtract(v.getY());
    final T dz = z.subtract(v.getZ());
    return dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz)).sqrt();
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distance(final Vector3D v1, final FieldVector3D<T> v2) {
    return v2.distance(v1);
  }
  public T distance1(final FieldVector3D<T> v) {
    final T dx = v.x.subtract(x).abs();
    final T dy = v.y.subtract(y).abs();
    final T dz = v.z.subtract(z).abs();
    return dx.add(dy).add(dz);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distance1(final FieldVector3D<T> v1, final FieldVector3D<T> v2) {
    return v1.distance1(v2);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distance1(final FieldVector3D<T> v1, final Vector3D v2) {
    return v1.distance1(v2);
  }
  public T distance1(final Vector3D v) {
    final T dx = x.subtract(v.getX()).abs();
    final T dy = y.subtract(v.getY()).abs();
    final T dz = z.subtract(v.getZ()).abs();
    return dx.add(dy).add(dz);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distance1(final Vector3D v1, final FieldVector3D<T> v2) {
    return v2.distance1(v1);
  }
  public T distanceInf(final FieldVector3D<T> v) {
    final T dx = v.x.subtract(x).abs();
    final T dy = v.y.subtract(y).abs();
    final T dz = v.z.subtract(z).abs();
    if(dx.getReal() <= dy.getReal()) {
      if(dy.getReal() <= dz.getReal()) {
        return dz;
      }
      else {
        return dy;
      }
    }
    else {
      if(dx.getReal() <= dz.getReal()) {
        return dz;
      }
      else {
        return dx;
      }
    }
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distanceInf(final FieldVector3D<T> v1, final FieldVector3D<T> v2) {
    return v1.distanceInf(v2);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distanceInf(final FieldVector3D<T> v1, final Vector3D v2) {
    return v1.distanceInf(v2);
  }
  public T distanceInf(final Vector3D v) {
    final T dx = x.subtract(v.getX()).abs();
    final T dy = y.subtract(v.getY()).abs();
    final T dz = z.subtract(v.getZ()).abs();
    if(dx.getReal() <= dy.getReal()) {
      if(dy.getReal() <= dz.getReal()) {
        return dz;
      }
      else {
        return dy;
      }
    }
    else {
      if(dx.getReal() <= dz.getReal()) {
        return dz;
      }
      else {
        return dx;
      }
    }
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distanceInf(final Vector3D v1, final FieldVector3D<T> v2) {
    return v2.distanceInf(v1);
  }
  public T distanceSq(final FieldVector3D<T> v) {
    final T dx = v.x.subtract(x);
    final T dy = v.y.subtract(y);
    final T dz = v.z.subtract(z);
    return dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz));
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distanceSq(final FieldVector3D<T> v1, final FieldVector3D<T> v2) {
    return v1.distanceSq(v2);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distanceSq(final FieldVector3D<T> v1, final Vector3D v2) {
    return v1.distanceSq(v2);
  }
  public T distanceSq(final Vector3D v) {
    final T dx = x.subtract(v.getX());
    final T dy = y.subtract(v.getY());
    final T dz = z.subtract(v.getZ());
    return dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz));
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distanceSq(final Vector3D v1, final FieldVector3D<T> v2) {
    return v2.distanceSq(v1);
  }
  public T dotProduct(final FieldVector3D<T> v) {
    return x.linearCombination(x, v.x, y, v.y, z, v.z);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T dotProduct(final FieldVector3D<T> v1, final FieldVector3D<T> v2) {
    return v1.dotProduct(v2);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T dotProduct(final FieldVector3D<T> v1, final Vector3D v2) {
    return v1.dotProduct(v2);
  }
  public T dotProduct(final Vector3D v) {
    return x.linearCombination(v.getX(), x, v.getY(), y, v.getZ(), z);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T dotProduct(final Vector3D v1, final FieldVector3D<T> v2) {
    return v2.dotProduct(v1);
  }
  public T getAlpha() {
    return y.atan2(x);
  }
  public T getDelta() {
    return z.divide(getNorm()).asin();
  }
  public T getNorm() {
    return x.multiply(x).add(y.multiply(y)).add(z.multiply(z)).sqrt();
  }
  public T getNorm1() {
    return x.abs().add(y.abs()).add(z.abs());
  }
  public T getNormInf() {
    final T xAbs = x.abs();
    final T yAbs = y.abs();
    final T zAbs = z.abs();
    if(xAbs.getReal() <= yAbs.getReal()) {
      if(yAbs.getReal() <= zAbs.getReal()) {
        return zAbs;
      }
      else {
        return yAbs;
      }
    }
    else {
      if(xAbs.getReal() <= zAbs.getReal()) {
        return zAbs;
      }
      else {
        return xAbs;
      }
    }
  }
  public T getNormSq() {
    return x.multiply(x).add(y.multiply(y)).add(z.multiply(z));
  }
  public T getX() {
    return x;
  }
  public T getY() {
    return y;
  }
  public T getZ() {
    return z;
  }
  public T[] toArray() {
    final T[] array = MathArrays.buildArray(x.getField(), 3);
    array[0] = x;
    array[1] = y;
    array[2] = z;
    return array;
  }
  public Vector3D toVector3D() {
    return new Vector3D(x.getReal(), y.getReal(), z.getReal());
  }
  @Override() public boolean equals(Object other) {
    if(this == other) {
      return true;
    }
    if(other instanceof FieldVector3D) {
      @SuppressWarnings(value = {"unchecked", }) final FieldVector3D<T> rhs = (FieldVector3D<T>)other;
      if(rhs.isNaN()) {
        return this.isNaN();
      }
      return x.equals(rhs.x) && y.equals(rhs.y) && z.equals(rhs.z);
    }
    return false;
  }
  public boolean isInfinite() {
    return !isNaN() && (Double.isInfinite(x.getReal()) || Double.isInfinite(y.getReal()) || Double.isInfinite(z.getReal()));
  }
  public boolean isNaN() {
    return Double.isNaN(x.getReal()) || Double.isNaN(y.getReal()) || Double.isNaN(z.getReal());
  }
  @Override() public int hashCode() {
    if(isNaN()) {
      return 409;
    }
    return 311 * (107 * x.hashCode() + 83 * y.hashCode() + z.hashCode());
  }
}