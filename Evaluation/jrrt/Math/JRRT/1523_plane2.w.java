package org.apache.commons.math3.geometry.euclidean.threed;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.Embedding;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.util.FastMath;

public class Plane implements Hyperplane<Euclidean3D>, Embedding<Euclidean3D, Euclidean2D>  {
  private double originOffset;
  private Vector3D origin;
  private Vector3D u;
  private Vector3D v;
  private Vector3D w;
  public Plane(final Plane plane) {
    super();
    originOffset = plane.originOffset;
    origin = plane.origin;
    u = plane.u;
    v = plane.v;
    w = plane.w;
  }
  public Plane(final Vector3D normal) throws MathArithmeticException {
    super();
    setNormal(normal);
    originOffset = 0;
    setFrame();
  }
  public Plane(final Vector3D p, final Vector3D normal) throws MathArithmeticException {
    super();
    setNormal(normal);
    originOffset = -p.dotProduct(w);
    setFrame();
  }
  public Plane(final Vector3D p1, final Vector3D p2, final Vector3D p3) throws MathArithmeticException {
    this(p1, p2.subtract(p1).crossProduct(p3.subtract(p1)));
  }
  public Line intersection(final Plane other) {
    final Vector3D direction = Vector3D.crossProduct(w, other.w);
    if(direction.getNorm() < 1.0e-10D) {
      return null;
    }
    final Vector3D point = intersection(this, other, new Plane(direction));
    return new Line(point, point.add(direction));
  }
  public Plane copySelf() {
    return new Plane(this);
  }
  public Plane rotate(final Vector3D center, final Rotation rotation) {
    final Vector3D delta = origin.subtract(center);
    final Plane plane = new Plane(center.add(rotation.applyTo(delta)), rotation.applyTo(w));
    plane.u = rotation.applyTo(u);
    plane.v = rotation.applyTo(v);
    return plane;
  }
  public Plane translate(final Vector3D translation) {
    final Plane plane = new Plane(origin.add(translation), w);
    plane.u = u;
    plane.v = v;
    return plane;
  }
  public PolyhedronsSet wholeSpace() {
    return new PolyhedronsSet();
  }
  public SubPlane wholeHyperplane() {
    return new SubPlane(this, new PolygonsSet());
  }
  public Vector2D toSubSpace(final Vector<Euclidean3D> point) {
    return new Vector2D(point.dotProduct(u), point.dotProduct(v));
  }
  public Vector3D getNormal() {
    return w;
  }
  public Vector3D getOrigin() {
    return origin;
  }
  public Vector3D getPointAt(final Vector2D inPlane, final double offset) {
    return new Vector3D(inPlane.getX(), u, inPlane.getY(), v, offset - originOffset, w);
  }
  public Vector3D getU() {
    return u;
  }
  public Vector3D getV() {
    return v;
  }
  public Vector3D intersection(final Line line) {
    final Vector3D direction = line.getDirection();
    final double dot = w.dotProduct(direction);
    if(FastMath.abs(dot) < 1.0e-10D) {
      return null;
    }
    final Vector3D point = line.toSpace(Vector1D.ZERO);
    final double k = -(originOffset + w.dotProduct(point)) / dot;
    return new Vector3D(1.0D, point, k, direction);
  }
  public static Vector3D intersection(final Plane plane1, final Plane plane2, final Plane plane3) {
    final double a1 = plane1.w.getX();
    final double b1 = plane1.w.getY();
    final double c1 = plane1.w.getZ();
    final double d1 = plane1.originOffset;
    Vector3D var_1523 = plane2.w;
    final double a2 = var_1523.getX();
    final double b2 = plane2.w.getY();
    final double c2 = plane2.w.getZ();
    final double d2 = plane2.originOffset;
    final double a3 = plane3.w.getX();
    final double b3 = plane3.w.getY();
    final double c3 = plane3.w.getZ();
    final double d3 = plane3.originOffset;
    final double a23 = b2 * c3 - b3 * c2;
    final double b23 = c2 * a3 - c3 * a2;
    final double c23 = a2 * b3 - a3 * b2;
    final double determinant = a1 * a23 + b1 * b23 + c1 * c23;
    if(FastMath.abs(determinant) < 1.0e-10D) {
      return null;
    }
    final double r = 1.0D / determinant;
    return new Vector3D((-a23 * d1 - (c1 * b3 - c3 * b1) * d2 - (c2 * b1 - c1 * b2) * d3) * r, (-b23 * d1 - (c3 * a1 - c1 * a3) * d2 - (c1 * a2 - c2 * a1) * d3) * r, (-c23 * d1 - (b1 * a3 - b3 * a1) * d2 - (b2 * a1 - b1 * a2) * d3) * r);
  }
  public Vector3D toSpace(final Vector<Euclidean2D> point) {
    final Vector2D p2D = (Vector2D)point;
    return new Vector3D(p2D.getX(), u, p2D.getY(), v, -originOffset, w);
  }
  public boolean contains(final Vector3D p) {
    return FastMath.abs(getOffset(p)) < 1.0e-10D;
  }
  public boolean isSimilarTo(final Plane plane) {
    final double angle = Vector3D.angle(w, plane.w);
    return ((angle < 1.0e-10D) && (FastMath.abs(originOffset - plane.originOffset) < 1.0e-10D)) || ((angle > (FastMath.PI - 1.0e-10D)) && (FastMath.abs(originOffset + plane.originOffset) < 1.0e-10D));
  }
  public boolean sameOrientationAs(final Hyperplane<Euclidean3D> other) {
    return (((Plane)other).w).dotProduct(w) > 0.0D;
  }
  public double getOffset(final Vector<Euclidean3D> point) {
    return point.dotProduct(w) + originOffset;
  }
  public double getOffset(final Plane plane) {
    return originOffset + (sameOrientationAs(plane) ? -plane.originOffset : plane.originOffset);
  }
  public void reset(final Plane original) {
    originOffset = original.originOffset;
    origin = original.origin;
    u = original.u;
    v = original.v;
    w = original.w;
  }
  public void reset(final Vector3D p, final Vector3D normal) throws MathArithmeticException {
    setNormal(normal);
    originOffset = -p.dotProduct(w);
    setFrame();
  }
  public void revertSelf() {
    final Vector3D tmp = u;
    u = v;
    v = tmp;
    w = w.negate();
    originOffset = -originOffset;
  }
  private void setFrame() {
    origin = new Vector3D(-originOffset, w);
    u = w.orthogonal();
    v = Vector3D.crossProduct(w, u);
  }
  private void setNormal(final Vector3D normal) throws MathArithmeticException {
    final double norm = normal.getNorm();
    if(norm < 1.0e-10D) {
      throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
    }
    w = new Vector3D(1.0D / norm, normal);
  }
}