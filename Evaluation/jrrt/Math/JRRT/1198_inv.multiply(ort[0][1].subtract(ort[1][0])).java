package org.apache.commons.math3.geometry.euclidean.threed;
import java.io.Serializable;
import org.apache.commons.math3.RealFieldElement;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
public class FieldRotation<T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.FieldRotation@T>> implements Serializable  {
  final private static long serialVersionUID = 20130224L;
  final private T q0;
  final private T q1;
  final private T q2;
  final private T q3;
  public FieldRotation(FieldVector3D<T> u1, FieldVector3D<T> u2, FieldVector3D<T> v1, FieldVector3D<T> v2) throws MathArithmeticException {
    super();
    final FieldVector3D<T> u3 = FieldVector3D.crossProduct(u1, u2).normalize();
    u2 = FieldVector3D.crossProduct(u3, u1).normalize();
    u1 = u1.normalize();
    final FieldVector3D<T> v3 = FieldVector3D.crossProduct(v1, v2).normalize();
    v2 = FieldVector3D.crossProduct(v3, v1).normalize();
    v1 = v1.normalize();
    final T[][] array = MathArrays.buildArray(u1.getX().getField(), 3, 3);
    array[0][0] = u1.getX().multiply(v1.getX()).add(u2.getX().multiply(v2.getX())).add(u3.getX().multiply(v3.getX()));
    array[0][1] = u1.getY().multiply(v1.getX()).add(u2.getY().multiply(v2.getX())).add(u3.getY().multiply(v3.getX()));
    array[0][2] = u1.getZ().multiply(v1.getX()).add(u2.getZ().multiply(v2.getX())).add(u3.getZ().multiply(v3.getX()));
    array[1][0] = u1.getX().multiply(v1.getY()).add(u2.getX().multiply(v2.getY())).add(u3.getX().multiply(v3.getY()));
    array[1][1] = u1.getY().multiply(v1.getY()).add(u2.getY().multiply(v2.getY())).add(u3.getY().multiply(v3.getY()));
    array[1][2] = u1.getZ().multiply(v1.getY()).add(u2.getZ().multiply(v2.getY())).add(u3.getZ().multiply(v3.getY()));
    array[2][0] = u1.getX().multiply(v1.getZ()).add(u2.getX().multiply(v2.getZ())).add(u3.getX().multiply(v3.getZ()));
    array[2][1] = u1.getY().multiply(v1.getZ()).add(u2.getY().multiply(v2.getZ())).add(u3.getY().multiply(v3.getZ()));
    array[2][2] = u1.getZ().multiply(v1.getZ()).add(u2.getZ().multiply(v2.getZ())).add(u3.getZ().multiply(v3.getZ()));
    T[] quat = mat2quat(array);
    q0 = quat[0];
    q1 = quat[1];
    q2 = quat[2];
    q3 = quat[3];
  }
  public FieldRotation(final FieldVector3D<T> axis, final T angle) throws MathIllegalArgumentException {
    super();
    final T norm = axis.getNorm();
    if(norm.getReal() == 0) {
      throw new MathIllegalArgumentException(LocalizedFormats.ZERO_NORM_FOR_ROTATION_AXIS);
    }
    final T halfAngle = angle.multiply(-0.5D);
    final T coeff = halfAngle.sin().divide(norm);
    q0 = halfAngle.cos();
    q1 = coeff.multiply(axis.getX());
    q2 = coeff.multiply(axis.getY());
    q3 = coeff.multiply(axis.getZ());
  }
  public FieldRotation(final FieldVector3D<T> u, final FieldVector3D<T> v) throws MathArithmeticException {
    super();
    final T normProduct = u.getNorm().multiply(v.getNorm());
    if(normProduct.getReal() == 0) {
      throw new MathArithmeticException(LocalizedFormats.ZERO_NORM_FOR_ROTATION_DEFINING_VECTOR);
    }
    final T dot = FieldVector3D.dotProduct(u, v);
    if(dot.getReal() < ((2.0e-15D - 1.0D) * normProduct.getReal())) {
      final FieldVector3D<T> w = u.orthogonal();
      q0 = normProduct.getField().getZero();
      q1 = w.getX().negate();
      q2 = w.getY().negate();
      q3 = w.getZ().negate();
    }
    else {
      q0 = dot.divide(normProduct).add(1.0D).multiply(0.5D).sqrt();
      final T coeff = q0.multiply(normProduct).multiply(2.0D).reciprocal();
      final FieldVector3D<T> q = FieldVector3D.crossProduct(v, u);
      q1 = coeff.multiply(q.getX());
      q2 = coeff.multiply(q.getY());
      q3 = coeff.multiply(q.getZ());
    }
  }
  public FieldRotation(final RotationOrder order, final T alpha1, final T alpha2, final T alpha3) {
    super();
    final T one = alpha1.getField().getOne();
    final FieldRotation<T> r1 = new FieldRotation<T>(new FieldVector3D<T>(one, order.getA1()), alpha1);
    final FieldRotation<T> r2 = new FieldRotation<T>(new FieldVector3D<T>(one, order.getA2()), alpha2);
    final FieldRotation<T> r3 = new FieldRotation<T>(new FieldVector3D<T>(one, order.getA3()), alpha3);
    final FieldRotation<T> composed = r1.applyTo(r2.applyTo(r3));
    q0 = composed.q0;
    q1 = composed.q1;
    q2 = composed.q2;
    q3 = composed.q3;
  }
  public FieldRotation(final T q0, final T q1, final T q2, final T q3, final boolean needsNormalization) {
    super();
    if(needsNormalization) {
      final T inv = q0.multiply(q0).add(q1.multiply(q1)).add(q2.multiply(q2)).add(q3.multiply(q3)).sqrt().reciprocal();
      this.q0 = inv.multiply(q0);
      this.q1 = inv.multiply(q1);
      this.q2 = inv.multiply(q2);
      this.q3 = inv.multiply(q3);
    }
    else {
      this.q0 = q0;
      this.q1 = q1;
      this.q2 = q2;
      this.q3 = q3;
    }
  }
  public FieldRotation(final T[][] m, final double threshold) throws NotARotationMatrixException {
    super();
    if((m.length != 3) || (m[0].length != 3) || (m[1].length != 3) || (m[2].length != 3)) {
      throw new NotARotationMatrixException(LocalizedFormats.ROTATION_MATRIX_DIMENSIONS, m.length, m[0].length);
    }
    final T[][] ort = orthogonalizeMatrix(m, threshold);
    final T d0 = ort[1][1].multiply(ort[2][2]).subtract(ort[2][1].multiply(ort[1][2]));
    final T d1 = ort[0][1].multiply(ort[2][2]).subtract(ort[2][1].multiply(ort[0][2]));
    final T d2 = ort[0][1].multiply(ort[1][2]).subtract(ort[1][1].multiply(ort[0][2]));
    final T det = ort[0][0].multiply(d0).subtract(ort[1][0].multiply(d1)).add(ort[2][0].multiply(d2));
    if(det.getReal() < 0.0D) {
      throw new NotARotationMatrixException(LocalizedFormats.CLOSEST_ORTHOGONAL_MATRIX_HAS_NEGATIVE_DETERMINANT, det);
    }
    final T[] quat = mat2quat(ort);
    q0 = quat[0];
    q1 = quat[1];
    q2 = quat[2];
    q3 = quat[3];
  }
  public FieldRotation<T> applyInverseTo(final FieldRotation<T> r) {
    return new FieldRotation<T>(r.q0.multiply(q0).add(r.q1.multiply(q1).add(r.q2.multiply(q2)).add(r.q3.multiply(q3))).negate(), r.q0.multiply(q1).add(r.q2.multiply(q3).subtract(r.q3.multiply(q2))).subtract(r.q1.multiply(q0)), r.q0.multiply(q2).add(r.q3.multiply(q1).subtract(r.q1.multiply(q3))).subtract(r.q2.multiply(q0)), r.q0.multiply(q3).add(r.q1.multiply(q2).subtract(r.q2.multiply(q1))).subtract(r.q3.multiply(q0)), false);
  }
  public FieldRotation<T> applyInverseTo(final Rotation r) {
    return new FieldRotation<T>(q0.multiply(r.getQ0()).add(q1.multiply(r.getQ1()).add(q2.multiply(r.getQ2())).add(q3.multiply(r.getQ3()))).negate(), q1.multiply(r.getQ0()).add(q3.multiply(r.getQ2()).subtract(q2.multiply(r.getQ3()))).subtract(q0.multiply(r.getQ1())), q2.multiply(r.getQ0()).add(q1.multiply(r.getQ3()).subtract(q3.multiply(r.getQ1()))).subtract(q0.multiply(r.getQ2())), q3.multiply(r.getQ0()).add(q2.multiply(r.getQ1()).subtract(q1.multiply(r.getQ2()))).subtract(q0.multiply(r.getQ3())), false);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> FieldRotation<T> applyInverseTo(final Rotation rOuter, final FieldRotation<T> rInner) {
    return new FieldRotation<T>(rInner.q0.multiply(rOuter.getQ0()).add(rInner.q1.multiply(rOuter.getQ1()).add(rInner.q2.multiply(rOuter.getQ2())).add(rInner.q3.multiply(rOuter.getQ3()))).negate(), rInner.q0.multiply(rOuter.getQ1()).add(rInner.q2.multiply(rOuter.getQ3()).subtract(rInner.q3.multiply(rOuter.getQ2()))).subtract(rInner.q1.multiply(rOuter.getQ0())), rInner.q0.multiply(rOuter.getQ2()).add(rInner.q3.multiply(rOuter.getQ1()).subtract(rInner.q1.multiply(rOuter.getQ3()))).subtract(rInner.q2.multiply(rOuter.getQ0())), rInner.q0.multiply(rOuter.getQ3()).add(rInner.q1.multiply(rOuter.getQ2()).subtract(rInner.q2.multiply(rOuter.getQ1()))).subtract(rInner.q3.multiply(rOuter.getQ0())), false);
  }
  public FieldRotation<T> applyTo(final FieldRotation<T> r) {
    return new FieldRotation<T>(r.q0.multiply(q0).subtract(r.q1.multiply(q1).add(r.q2.multiply(q2)).add(r.q3.multiply(q3))), r.q1.multiply(q0).add(r.q0.multiply(q1)).add(r.q2.multiply(q3).subtract(r.q3.multiply(q2))), r.q2.multiply(q0).add(r.q0.multiply(q2)).add(r.q3.multiply(q1).subtract(r.q1.multiply(q3))), r.q3.multiply(q0).add(r.q0.multiply(q3)).add(r.q1.multiply(q2).subtract(r.q2.multiply(q1))), false);
  }
  public FieldRotation<T> applyTo(final Rotation r) {
    return new FieldRotation<T>(q0.multiply(r.getQ0()).subtract(q1.multiply(r.getQ1()).add(q2.multiply(r.getQ2())).add(q3.multiply(r.getQ3()))), q0.multiply(r.getQ1()).add(q1.multiply(r.getQ0())).add(q3.multiply(r.getQ2()).subtract(q2.multiply(r.getQ3()))), q0.multiply(r.getQ2()).add(q2.multiply(r.getQ0())).add(q1.multiply(r.getQ3()).subtract(q3.multiply(r.getQ1()))), q0.multiply(r.getQ3()).add(q3.multiply(r.getQ0())).add(q2.multiply(r.getQ1()).subtract(q1.multiply(r.getQ2()))), false);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> FieldRotation<T> applyTo(final Rotation r1, final FieldRotation<T> rInner) {
    return new FieldRotation<T>(rInner.q0.multiply(r1.getQ0()).subtract(rInner.q1.multiply(r1.getQ1()).add(rInner.q2.multiply(r1.getQ2())).add(rInner.q3.multiply(r1.getQ3()))), rInner.q1.multiply(r1.getQ0()).add(rInner.q0.multiply(r1.getQ1())).add(rInner.q2.multiply(r1.getQ3()).subtract(rInner.q3.multiply(r1.getQ2()))), rInner.q2.multiply(r1.getQ0()).add(rInner.q0.multiply(r1.getQ2())).add(rInner.q3.multiply(r1.getQ1()).subtract(rInner.q1.multiply(r1.getQ3()))), rInner.q3.multiply(r1.getQ0()).add(rInner.q0.multiply(r1.getQ3())).add(rInner.q1.multiply(r1.getQ2()).subtract(rInner.q2.multiply(r1.getQ1()))), false);
  }
  public FieldRotation<T> revert() {
    return new FieldRotation<T>(q0.negate(), q1, q2, q3, false);
  }
  public FieldVector3D<T> applyInverseTo(final FieldVector3D<T> u) {
    final T x = u.getX();
    final T y = u.getY();
    final T z = u.getZ();
    final T s = q1.multiply(x).add(q2.multiply(y)).add(q3.multiply(z));
    final T m0 = q0.negate();
    return new FieldVector3D<T>(m0.multiply(x.multiply(m0).subtract(q2.multiply(z).subtract(q3.multiply(y)))).add(s.multiply(q1)).multiply(2).subtract(x), m0.multiply(y.multiply(m0).subtract(q3.multiply(x).subtract(q1.multiply(z)))).add(s.multiply(q2)).multiply(2).subtract(y), m0.multiply(z.multiply(m0).subtract(q1.multiply(y).subtract(q2.multiply(x)))).add(s.multiply(q3)).multiply(2).subtract(z));
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> FieldVector3D<T> applyInverseTo(final Rotation r, final FieldVector3D<T> u) {
    final T x = u.getX();
    final T y = u.getY();
    final T z = u.getZ();
    final T s = x.multiply(r.getQ1()).add(y.multiply(r.getQ2())).add(z.multiply(r.getQ3()));
    final double m0 = -r.getQ0();
    return new FieldVector3D<T>(x.multiply(m0).subtract(z.multiply(r.getQ2()).subtract(y.multiply(r.getQ3()))).multiply(m0).add(s.multiply(r.getQ1())).multiply(2).subtract(x), y.multiply(m0).subtract(x.multiply(r.getQ3()).subtract(z.multiply(r.getQ1()))).multiply(m0).add(s.multiply(r.getQ2())).multiply(2).subtract(y), z.multiply(m0).subtract(y.multiply(r.getQ1()).subtract(x.multiply(r.getQ2()))).multiply(m0).add(s.multiply(r.getQ3())).multiply(2).subtract(z));
  }
  public FieldVector3D<T> applyInverseTo(final Vector3D u) {
    final double x = u.getX();
    final double y = u.getY();
    final double z = u.getZ();
    final T s = q1.multiply(x).add(q2.multiply(y)).add(q3.multiply(z));
    final T m0 = q0.negate();
    return new FieldVector3D<T>(m0.multiply(m0.multiply(x).subtract(q2.multiply(z).subtract(q3.multiply(y)))).add(s.multiply(q1)).multiply(2).subtract(x), m0.multiply(m0.multiply(y).subtract(q3.multiply(x).subtract(q1.multiply(z)))).add(s.multiply(q2)).multiply(2).subtract(y), m0.multiply(m0.multiply(z).subtract(q1.multiply(y).subtract(q2.multiply(x)))).add(s.multiply(q3)).multiply(2).subtract(z));
  }
  public FieldVector3D<T> applyTo(final FieldVector3D<T> u) {
    final T x = u.getX();
    final T y = u.getY();
    final T z = u.getZ();
    final T s = q1.multiply(x).add(q2.multiply(y)).add(q3.multiply(z));
    return new FieldVector3D<T>(q0.multiply(x.multiply(q0).subtract(q2.multiply(z).subtract(q3.multiply(y)))).add(s.multiply(q1)).multiply(2).subtract(x), q0.multiply(y.multiply(q0).subtract(q3.multiply(x).subtract(q1.multiply(z)))).add(s.multiply(q2)).multiply(2).subtract(y), q0.multiply(z.multiply(q0).subtract(q1.multiply(y).subtract(q2.multiply(x)))).add(s.multiply(q3)).multiply(2).subtract(z));
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> FieldVector3D<T> applyTo(final Rotation r, final FieldVector3D<T> u) {
    final T x = u.getX();
    final T y = u.getY();
    final T z = u.getZ();
    final T s = x.multiply(r.getQ1()).add(y.multiply(r.getQ2())).add(z.multiply(r.getQ3()));
    return new FieldVector3D<T>(x.multiply(r.getQ0()).subtract(z.multiply(r.getQ2()).subtract(y.multiply(r.getQ3()))).multiply(r.getQ0()).add(s.multiply(r.getQ1())).multiply(2).subtract(x), y.multiply(r.getQ0()).subtract(x.multiply(r.getQ3()).subtract(z.multiply(r.getQ1()))).multiply(r.getQ0()).add(s.multiply(r.getQ2())).multiply(2).subtract(y), z.multiply(r.getQ0()).subtract(y.multiply(r.getQ1()).subtract(x.multiply(r.getQ2()))).multiply(r.getQ0()).add(s.multiply(r.getQ3())).multiply(2).subtract(z));
  }
  public FieldVector3D<T> applyTo(final Vector3D u) {
    final double x = u.getX();
    final double y = u.getY();
    final double z = u.getZ();
    final T s = q1.multiply(x).add(q2.multiply(y)).add(q3.multiply(z));
    return new FieldVector3D<T>(q0.multiply(q0.multiply(x).subtract(q2.multiply(z).subtract(q3.multiply(y)))).add(s.multiply(q1)).multiply(2).subtract(x), q0.multiply(q0.multiply(y).subtract(q3.multiply(x).subtract(q1.multiply(z)))).add(s.multiply(q2)).multiply(2).subtract(y), q0.multiply(q0.multiply(z).subtract(q1.multiply(y).subtract(q2.multiply(x)))).add(s.multiply(q3)).multiply(2).subtract(z));
  }
  public FieldVector3D<T> getAxis() {
    final T squaredSine = q1.multiply(q1).add(q2.multiply(q2)).add(q3.multiply(q3));
    if(squaredSine.getReal() == 0) {
      final Field<T> field = squaredSine.getField();
      return new FieldVector3D<T>(field.getOne(), field.getZero(), field.getZero());
    }
    else 
      if(q0.getReal() < 0) {
        T inverse = squaredSine.sqrt().reciprocal();
        return new FieldVector3D<T>(q1.multiply(inverse), q2.multiply(inverse), q3.multiply(inverse));
      }
    final T inverse = squaredSine.sqrt().reciprocal().negate();
    return new FieldVector3D<T>(q1.multiply(inverse), q2.multiply(inverse), q3.multiply(inverse));
  }
  private FieldVector3D<T> vector(final double x, final double y, final double z) {
    final T zero = q0.getField().getZero();
    return new FieldVector3D<T>(zero.add(x), zero.add(y), zero.add(z));
  }
  public Rotation toRotation() {
    return new Rotation(q0.getReal(), q1.getReal(), q2.getReal(), q3.getReal(), false);
  }
  public static  <T extends org.apache.commons.math3.RealFieldElement<org.apache.commons.math3.geometry.euclidean.threed.T>> T distance(final FieldRotation<T> r1, final FieldRotation<T> r2) {
    return r1.applyInverseTo(r2).getAngle();
  }
  public T getAngle() {
    if((q0.getReal() < -0.1D) || (q0.getReal() > 0.1D)) {
      return q1.multiply(q1).add(q2.multiply(q2)).add(q3.multiply(q3)).sqrt().asin().multiply(2);
    }
    else 
      if(q0.getReal() < 0) {
        return q0.negate().acos().multiply(2);
      }
    return q0.acos().multiply(2);
  }
  public T getQ0() {
    return q0;
  }
  public T getQ1() {
    return q1;
  }
  public T getQ2() {
    return q2;
  }
  public T getQ3() {
    return q3;
  }
  private T[] buildArray(final T a0, final T a1, final T a2) {
    final T[] array = MathArrays.buildArray(a0.getField(), 3);
    array[0] = a0;
    array[1] = a1;
    array[2] = a2;
    return array;
  }
  public T[] getAngles(final RotationOrder order) throws CardanEulerSingularityException {
    if(order == RotationOrder.XYZ) {
      final FieldVector3D<T> v1 = applyTo(vector(0, 0, 1));
      final FieldVector3D<T> v2 = applyInverseTo(vector(1, 0, 0));
      if((v2.getZ().getReal() < -0.9999999999D) || (v2.getZ().getReal() > 0.9999999999D)) {
        throw new CardanEulerSingularityException(true);
      }
      return buildArray(v1.getY().negate().atan2(v1.getZ()), v2.getZ().asin(), v2.getY().negate().atan2(v2.getX()));
    }
    else 
      if(order == RotationOrder.XZY) {
        final FieldVector3D<T> v1 = applyTo(vector(0, 1, 0));
        final FieldVector3D<T> v2 = applyInverseTo(vector(1, 0, 0));
        if((v2.getY().getReal() < -0.9999999999D) || (v2.getY().getReal() > 0.9999999999D)) {
          throw new CardanEulerSingularityException(true);
        }
        return buildArray(v1.getZ().atan2(v1.getY()), v2.getY().asin().negate(), v2.getZ().atan2(v2.getX()));
      }
      else 
        if(order == RotationOrder.YXZ) {
          final FieldVector3D<T> v1 = applyTo(vector(0, 0, 1));
          final FieldVector3D<T> v2 = applyInverseTo(vector(0, 1, 0));
          if((v2.getZ().getReal() < -0.9999999999D) || (v2.getZ().getReal() > 0.9999999999D)) {
            throw new CardanEulerSingularityException(true);
          }
          return buildArray(v1.getX().atan2(v1.getZ()), v2.getZ().asin().negate(), v2.getX().atan2(v2.getY()));
        }
        else 
          if(order == RotationOrder.YZX) {
            final FieldVector3D<T> v1 = applyTo(vector(1, 0, 0));
            final FieldVector3D<T> v2 = applyInverseTo(vector(0, 1, 0));
            if((v2.getX().getReal() < -0.9999999999D) || (v2.getX().getReal() > 0.9999999999D)) {
              throw new CardanEulerSingularityException(true);
            }
            return buildArray(v1.getZ().negate().atan2(v1.getX()), v2.getX().asin(), v2.getZ().negate().atan2(v2.getY()));
          }
          else 
            if(order == RotationOrder.ZXY) {
              final FieldVector3D<T> v1 = applyTo(vector(0, 1, 0));
              final FieldVector3D<T> v2 = applyInverseTo(vector(0, 0, 1));
              if((v2.getY().getReal() < -0.9999999999D) || (v2.getY().getReal() > 0.9999999999D)) {
                throw new CardanEulerSingularityException(true);
              }
              return buildArray(v1.getX().negate().atan2(v1.getY()), v2.getY().asin(), v2.getX().negate().atan2(v2.getZ()));
            }
            else 
              if(order == RotationOrder.ZYX) {
                final FieldVector3D<T> v1 = applyTo(vector(1, 0, 0));
                final FieldVector3D<T> v2 = applyInverseTo(vector(0, 0, 1));
                if((v2.getX().getReal() < -0.9999999999D) || (v2.getX().getReal() > 0.9999999999D)) {
                  throw new CardanEulerSingularityException(true);
                }
                return buildArray(v1.getY().atan2(v1.getX()), v2.getX().asin().negate(), v2.getY().atan2(v2.getZ()));
              }
              else 
                if(order == RotationOrder.XYX) {
                  final FieldVector3D<T> v1 = applyTo(vector(1, 0, 0));
                  final FieldVector3D<T> v2 = applyInverseTo(vector(1, 0, 0));
                  if((v2.getX().getReal() < -0.9999999999D) || (v2.getX().getReal() > 0.9999999999D)) {
                    throw new CardanEulerSingularityException(false);
                  }
                  return buildArray(v1.getY().atan2(v1.getZ().negate()), v2.getX().acos(), v2.getY().atan2(v2.getZ()));
                }
                else 
                  if(order == RotationOrder.XZX) {
                    final FieldVector3D<T> v1 = applyTo(vector(1, 0, 0));
                    final FieldVector3D<T> v2 = applyInverseTo(vector(1, 0, 0));
                    if((v2.getX().getReal() < -0.9999999999D) || (v2.getX().getReal() > 0.9999999999D)) {
                      throw new CardanEulerSingularityException(false);
                    }
                    return buildArray(v1.getZ().atan2(v1.getY()), v2.getX().acos(), v2.getZ().atan2(v2.getY().negate()));
                  }
                  else 
                    if(order == RotationOrder.YXY) {
                      final FieldVector3D<T> v1 = applyTo(vector(0, 1, 0));
                      final FieldVector3D<T> v2 = applyInverseTo(vector(0, 1, 0));
                      if((v2.getY().getReal() < -0.9999999999D) || (v2.getY().getReal() > 0.9999999999D)) {
                        throw new CardanEulerSingularityException(false);
                      }
                      return buildArray(v1.getX().atan2(v1.getZ()), v2.getY().acos(), v2.getX().atan2(v2.getZ().negate()));
                    }
                    else 
                      if(order == RotationOrder.YZY) {
                        final FieldVector3D<T> v1 = applyTo(vector(0, 1, 0));
                        final FieldVector3D<T> v2 = applyInverseTo(vector(0, 1, 0));
                        if((v2.getY().getReal() < -0.9999999999D) || (v2.getY().getReal() > 0.9999999999D)) {
                          throw new CardanEulerSingularityException(false);
                        }
                        return buildArray(v1.getZ().atan2(v1.getX().negate()), v2.getY().acos(), v2.getZ().atan2(v2.getX()));
                      }
                      else 
                        if(order == RotationOrder.ZXZ) {
                          final FieldVector3D<T> v1 = applyTo(vector(0, 0, 1));
                          final FieldVector3D<T> v2 = applyInverseTo(vector(0, 0, 1));
                          if((v2.getZ().getReal() < -0.9999999999D) || (v2.getZ().getReal() > 0.9999999999D)) {
                            throw new CardanEulerSingularityException(false);
                          }
                          return buildArray(v1.getX().atan2(v1.getY().negate()), v2.getZ().acos(), v2.getX().atan2(v2.getY()));
                        }
                        else {
                          final FieldVector3D<T> v1 = applyTo(vector(0, 0, 1));
                          final FieldVector3D<T> v2 = applyInverseTo(vector(0, 0, 1));
                          if((v2.getZ().getReal() < -0.9999999999D) || (v2.getZ().getReal() > 0.9999999999D)) {
                            throw new CardanEulerSingularityException(false);
                          }
                          return buildArray(v1.getY().atan2(v1.getX()), v2.getZ().acos(), v2.getY().atan2(v2.getX().negate()));
                        }
  }
  private T[] mat2quat(final T[][] ort) {
    final T[] quat = MathArrays.buildArray(ort[0][0].getField(), 4);
    T s = ort[0][0].add(ort[1][1]).add(ort[2][2]);
    if(s.getReal() > -0.19D) {
      quat[0] = s.add(1.0D).sqrt().multiply(0.5D);
      T inv = quat[0].reciprocal().multiply(0.25D);
      quat[1] = inv.multiply(ort[1][2].subtract(ort[2][1]));
      quat[2] = inv.multiply(ort[2][0].subtract(ort[0][2]));
      quat[3] = inv.multiply(ort[0][1].subtract(ort[1][0]));
    }
    else {
      s = ort[0][0].subtract(ort[1][1]).subtract(ort[2][2]);
      if(s.getReal() > -0.19D) {
        quat[1] = s.add(1.0D).sqrt().multiply(0.5D);
        T inv = quat[1].reciprocal().multiply(0.25D);
        quat[0] = inv.multiply(ort[1][2].subtract(ort[2][1]));
        quat[2] = inv.multiply(ort[0][1].add(ort[1][0]));
        quat[3] = inv.multiply(ort[0][2].add(ort[2][0]));
      }
      else {
        s = ort[1][1].subtract(ort[0][0]).subtract(ort[2][2]);
        if(s.getReal() > -0.19D) {
          quat[2] = s.add(1.0D).sqrt().multiply(0.5D);
          T inv = quat[2].reciprocal().multiply(0.25D);
          quat[0] = inv.multiply(ort[2][0].subtract(ort[0][2]));
          quat[1] = inv.multiply(ort[0][1].add(ort[1][0]));
          quat[3] = inv.multiply(ort[2][1].add(ort[1][2]));
        }
        else {
          s = ort[2][2].subtract(ort[0][0]).subtract(ort[1][1]);
          quat[3] = s.add(1.0D).sqrt().multiply(0.5D);
          T inv = quat[3].reciprocal().multiply(0.25D);
          T var_1198 = inv.multiply(ort[0][1].subtract(ort[1][0]));
          quat[0] = var_1198;
          quat[1] = inv.multiply(ort[0][2].add(ort[2][0]));
          quat[2] = inv.multiply(ort[2][1].add(ort[1][2]));
        }
      }
    }
    return quat;
  }
  public T[][] getMatrix() {
    final T q0q0 = q0.multiply(q0);
    final T q0q1 = q0.multiply(q1);
    final T q0q2 = q0.multiply(q2);
    final T q0q3 = q0.multiply(q3);
    final T q1q1 = q1.multiply(q1);
    final T q1q2 = q1.multiply(q2);
    final T q1q3 = q1.multiply(q3);
    final T q2q2 = q2.multiply(q2);
    final T q2q3 = q2.multiply(q3);
    final T q3q3 = q3.multiply(q3);
    final T[][] m = MathArrays.buildArray(q0.getField(), 3, 3);
    m[0][0] = q0q0.add(q1q1).multiply(2).subtract(1);
    m[1][0] = q1q2.subtract(q0q3).multiply(2);
    m[2][0] = q1q3.add(q0q2).multiply(2);
    m[0][1] = q1q2.add(q0q3).multiply(2);
    m[1][1] = q0q0.add(q2q2).multiply(2).subtract(1);
    m[2][1] = q2q3.subtract(q0q1).multiply(2);
    m[0][2] = q1q3.subtract(q0q2).multiply(2);
    m[1][2] = q2q3.add(q0q1).multiply(2);
    m[2][2] = q0q0.add(q3q3).multiply(2).subtract(1);
    return m;
  }
  private T[][] orthogonalizeMatrix(final T[][] m, final double threshold) throws NotARotationMatrixException {
    T x00 = m[0][0];
    T x01 = m[0][1];
    T x02 = m[0][2];
    T x10 = m[1][0];
    T x11 = m[1][1];
    T x12 = m[1][2];
    T x20 = m[2][0];
    T x21 = m[2][1];
    T x22 = m[2][2];
    double fn = 0;
    double fn1;
    final T[][] o = MathArrays.buildArray(m[0][0].getField(), 3, 3);
    int i = 0;
    while(++i < 11){
      final T mx00 = m[0][0].multiply(x00).add(m[1][0].multiply(x10)).add(m[2][0].multiply(x20));
      final T mx10 = m[0][1].multiply(x00).add(m[1][1].multiply(x10)).add(m[2][1].multiply(x20));
      final T mx20 = m[0][2].multiply(x00).add(m[1][2].multiply(x10)).add(m[2][2].multiply(x20));
      final T mx01 = m[0][0].multiply(x01).add(m[1][0].multiply(x11)).add(m[2][0].multiply(x21));
      final T mx11 = m[0][1].multiply(x01).add(m[1][1].multiply(x11)).add(m[2][1].multiply(x21));
      final T mx21 = m[0][2].multiply(x01).add(m[1][2].multiply(x11)).add(m[2][2].multiply(x21));
      final T mx02 = m[0][0].multiply(x02).add(m[1][0].multiply(x12)).add(m[2][0].multiply(x22));
      final T mx12 = m[0][1].multiply(x02).add(m[1][1].multiply(x12)).add(m[2][1].multiply(x22));
      final T mx22 = m[0][2].multiply(x02).add(m[1][2].multiply(x12)).add(m[2][2].multiply(x22));
      o[0][0] = x00.subtract(x00.multiply(mx00).add(x01.multiply(mx10)).add(x02.multiply(mx20)).subtract(m[0][0]).multiply(0.5D));
      o[0][1] = x01.subtract(x00.multiply(mx01).add(x01.multiply(mx11)).add(x02.multiply(mx21)).subtract(m[0][1]).multiply(0.5D));
      o[0][2] = x02.subtract(x00.multiply(mx02).add(x01.multiply(mx12)).add(x02.multiply(mx22)).subtract(m[0][2]).multiply(0.5D));
      o[1][0] = x10.subtract(x10.multiply(mx00).add(x11.multiply(mx10)).add(x12.multiply(mx20)).subtract(m[1][0]).multiply(0.5D));
      o[1][1] = x11.subtract(x10.multiply(mx01).add(x11.multiply(mx11)).add(x12.multiply(mx21)).subtract(m[1][1]).multiply(0.5D));
      o[1][2] = x12.subtract(x10.multiply(mx02).add(x11.multiply(mx12)).add(x12.multiply(mx22)).subtract(m[1][2]).multiply(0.5D));
      o[2][0] = x20.subtract(x20.multiply(mx00).add(x21.multiply(mx10)).add(x22.multiply(mx20)).subtract(m[2][0]).multiply(0.5D));
      o[2][1] = x21.subtract(x20.multiply(mx01).add(x21.multiply(mx11)).add(x22.multiply(mx21)).subtract(m[2][1]).multiply(0.5D));
      o[2][2] = x22.subtract(x20.multiply(mx02).add(x21.multiply(mx12)).add(x22.multiply(mx22)).subtract(m[2][2]).multiply(0.5D));
      final double corr00 = o[0][0].getReal() - m[0][0].getReal();
      final double corr01 = o[0][1].getReal() - m[0][1].getReal();
      final double corr02 = o[0][2].getReal() - m[0][2].getReal();
      final double corr10 = o[1][0].getReal() - m[1][0].getReal();
      final double corr11 = o[1][1].getReal() - m[1][1].getReal();
      final double corr12 = o[1][2].getReal() - m[1][2].getReal();
      final double corr20 = o[2][0].getReal() - m[2][0].getReal();
      final double corr21 = o[2][1].getReal() - m[2][1].getReal();
      final double corr22 = o[2][2].getReal() - m[2][2].getReal();
      fn1 = corr00 * corr00 + corr01 * corr01 + corr02 * corr02 + corr10 * corr10 + corr11 * corr11 + corr12 * corr12 + corr20 * corr20 + corr21 * corr21 + corr22 * corr22;
      if(FastMath.abs(fn1 - fn) <= threshold) {
        return o;
      }
      x00 = o[0][0];
      x01 = o[0][1];
      x02 = o[0][2];
      x10 = o[1][0];
      x11 = o[1][1];
      x12 = o[1][2];
      x20 = o[2][0];
      x21 = o[2][1];
      x22 = o[2][2];
      fn = fn1;
    }
    throw new NotARotationMatrixException(LocalizedFormats.UNABLE_TO_ORTHOGONOLIZE_MATRIX, i - 1);
  }
  public void applyInverseTo(final double[] in, final T[] out) {
    final double x = in[0];
    final double y = in[1];
    final double z = in[2];
    final T s = q1.multiply(x).add(q2.multiply(y)).add(q3.multiply(z));
    final T m0 = q0.negate();
    out[0] = m0.multiply(m0.multiply(x).subtract(q2.multiply(z).subtract(q3.multiply(y)))).add(s.multiply(q1)).multiply(2).subtract(x);
    out[1] = m0.multiply(m0.multiply(y).subtract(q3.multiply(x).subtract(q1.multiply(z)))).add(s.multiply(q2)).multiply(2).subtract(y);
    out[2] = m0.multiply(m0.multiply(z).subtract(q1.multiply(y).subtract(q2.multiply(x)))).add(s.multiply(q3)).multiply(2).subtract(z);
  }
  public void applyInverseTo(final T[] in, final T[] out) {
    final T x = in[0];
    final T y = in[1];
    final T z = in[2];
    final T s = q1.multiply(x).add(q2.multiply(y)).add(q3.multiply(z));
    final T m0 = q0.negate();
    out[0] = m0.multiply(x.multiply(m0).subtract(q2.multiply(z).subtract(q3.multiply(y)))).add(s.multiply(q1)).multiply(2).subtract(x);
    out[1] = m0.multiply(y.multiply(m0).subtract(q3.multiply(x).subtract(q1.multiply(z)))).add(s.multiply(q2)).multiply(2).subtract(y);
    out[2] = m0.multiply(z.multiply(m0).subtract(q1.multiply(y).subtract(q2.multiply(x)))).add(s.multiply(q3)).multiply(2).subtract(z);
  }
  public void applyTo(final double[] in, final T[] out) {
    final double x = in[0];
    final double y = in[1];
    final double z = in[2];
    final T s = q1.multiply(x).add(q2.multiply(y)).add(q3.multiply(z));
    out[0] = q0.multiply(q0.multiply(x).subtract(q2.multiply(z).subtract(q3.multiply(y)))).add(s.multiply(q1)).multiply(2).subtract(x);
    out[1] = q0.multiply(q0.multiply(y).subtract(q3.multiply(x).subtract(q1.multiply(z)))).add(s.multiply(q2)).multiply(2).subtract(y);
    out[2] = q0.multiply(q0.multiply(z).subtract(q1.multiply(y).subtract(q2.multiply(x)))).add(s.multiply(q3)).multiply(2).subtract(z);
  }
  public void applyTo(final T[] in, final T[] out) {
    final T x = in[0];
    final T y = in[1];
    final T z = in[2];
    final T s = q1.multiply(x).add(q2.multiply(y)).add(q3.multiply(z));
    out[0] = q0.multiply(x.multiply(q0).subtract(q2.multiply(z).subtract(q3.multiply(y)))).add(s.multiply(q1)).multiply(2).subtract(x);
    out[1] = q0.multiply(y.multiply(q0).subtract(q3.multiply(x).subtract(q1.multiply(z)))).add(s.multiply(q2)).multiply(2).subtract(y);
    out[2] = q0.multiply(z.multiply(q0).subtract(q1.multiply(y).subtract(q2.multiply(x)))).add(s.multiply(q3)).multiply(2).subtract(z);
  }
}