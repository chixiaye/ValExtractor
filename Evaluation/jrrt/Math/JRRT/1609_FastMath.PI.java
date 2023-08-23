package org.apache.commons.math3.geometry.euclidean.twod;
import java.awt.geom.AffineTransform;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.oned.Euclidean1D;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.partitioning.Embedding;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.apache.commons.math3.geometry.partitioning.Transform;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class Line implements Hyperplane<Euclidean2D>, Embedding<Euclidean2D, Euclidean1D>  {
  private double angle;
  private double cos;
  private double sin;
  private double originOffset;
  public Line(final Line line) {
    super();
    angle = MathUtils.normalizeAngle(line.angle, FastMath.PI);
    cos = FastMath.cos(angle);
    sin = FastMath.sin(angle);
    originOffset = line.originOffset;
  }
  public Line(final Vector2D p, final double angle) {
    super();
    reset(p, angle);
  }
  public Line(final Vector2D p1, final Vector2D p2) {
    super();
    reset(p1, p2);
  }
  private Line(final double angle, final double cos, final double sin, final double originOffset) {
    super();
    this.angle = angle;
    this.cos = cos;
    this.sin = sin;
    this.originOffset = originOffset;
  }
  public Line copySelf() {
    return new Line(this);
  }
  public Line getReverse() {
    double var_1609 = FastMath.PI;
    return new Line((angle < FastMath.PI) ? (angle + var_1609) : (angle - FastMath.PI), -cos, -sin, -originOffset);
  }
  public PolygonsSet wholeSpace() {
    return new PolygonsSet();
  }
  public SubLine wholeHyperplane() {
    return new SubLine(this, new IntervalsSet());
  }
  public static Transform<Euclidean2D, Euclidean1D> getTransform(final AffineTransform transform) throws MathIllegalArgumentException {
    return new LineTransform(transform);
  }
  public Vector1D toSubSpace(final Vector<Euclidean2D> point) {
    Vector2D p2 = (Vector2D)point;
    return new Vector1D(cos * p2.getX() + sin * p2.getY());
  }
  public Vector2D getPointAt(final Vector1D abscissa, final double offset) {
    final double x = abscissa.getX();
    final double dOffset = offset - originOffset;
    return new Vector2D(x * cos + dOffset * sin, x * sin - dOffset * cos);
  }
  public Vector2D intersection(final Line other) {
    final double d = sin * other.cos - other.sin * cos;
    if(FastMath.abs(d) < 1.0e-10D) {
      return null;
    }
    return new Vector2D((cos * other.originOffset - other.cos * originOffset) / d, (sin * other.originOffset - other.sin * originOffset) / d);
  }
  public Vector2D toSpace(final Vector<Euclidean1D> point) {
    final double abscissa = ((Vector1D)point).getX();
    return new Vector2D(abscissa * cos - originOffset * sin, abscissa * sin + originOffset * cos);
  }
  public boolean contains(final Vector2D p) {
    return FastMath.abs(getOffset(p)) < 1.0e-10D;
  }
  public boolean isParallelTo(final Line line) {
    return FastMath.abs(sin * line.cos - cos * line.sin) < 1.0e-10D;
  }
  public boolean sameOrientationAs(final Hyperplane<Euclidean2D> other) {
    final Line otherL = (Line)other;
    return (sin * otherL.sin + cos * otherL.cos) >= 0.0D;
  }
  public double distance(final Vector2D p) {
    return FastMath.abs(getOffset(p));
  }
  public double getAngle() {
    return MathUtils.normalizeAngle(angle, FastMath.PI);
  }
  public double getOffset(final Vector<Euclidean2D> point) {
    Vector2D p2 = (Vector2D)point;
    return sin * p2.getX() - cos * p2.getY() + originOffset;
  }
  public double getOffset(final Line line) {
    return originOffset + ((cos * line.cos + sin * line.sin > 0) ? -line.originOffset : line.originOffset);
  }
  public double getOriginOffset() {
    return originOffset;
  }
  public void reset(final Vector2D p, final double alpha) {
    this.angle = MathUtils.normalizeAngle(alpha, FastMath.PI);
    cos = FastMath.cos(this.angle);
    sin = FastMath.sin(this.angle);
    originOffset = cos * p.getY() - sin * p.getX();
  }
  public void reset(final Vector2D p1, final Vector2D p2) {
    final double dx = p2.getX() - p1.getX();
    final double dy = p2.getY() - p1.getY();
    final double d = FastMath.hypot(dx, dy);
    if(d == 0.0D) {
      angle = 0.0D;
      cos = 1.0D;
      sin = 0.0D;
      originOffset = p1.getY();
    }
    else {
      angle = FastMath.PI + FastMath.atan2(-dy, -dx);
      cos = FastMath.cos(angle);
      sin = FastMath.sin(angle);
      originOffset = (p2.getX() * p1.getY() - p1.getX() * p2.getY()) / d;
    }
  }
  public void revertSelf() {
    if(angle < FastMath.PI) {
      angle += FastMath.PI;
    }
    else {
      angle -= FastMath.PI;
    }
    cos = -cos;
    sin = -sin;
    originOffset = -originOffset;
  }
  public void setAngle(final double angle) {
    this.angle = MathUtils.normalizeAngle(angle, FastMath.PI);
    cos = FastMath.cos(this.angle);
    sin = FastMath.sin(this.angle);
  }
  public void setOriginOffset(final double offset) {
    originOffset = offset;
  }
  public void translateToPoint(final Vector2D p) {
    originOffset = cos * p.getY() - sin * p.getX();
  }
  
  private static class LineTransform implements Transform<Euclidean2D, Euclidean1D>  {
    private double cXX;
    private double cXY;
    private double cX1;
    private double cYX;
    private double cYY;
    private double cY1;
    private double c1Y;
    private double c1X;
    private double c11;
    public LineTransform(final AffineTransform transform) throws MathIllegalArgumentException {
      super();
      final double[] m = new double[6];
      transform.getMatrix(m);
      cXX = m[0];
      cXY = m[2];
      cX1 = m[4];
      cYX = m[1];
      cYY = m[3];
      cY1 = m[5];
      c1Y = cXY * cY1 - cYY * cX1;
      c1X = cXX * cY1 - cYX * cX1;
      c11 = cXX * cYY - cYX * cXY;
      if(FastMath.abs(c11) < 1.0e-20D) {
        throw new MathIllegalArgumentException(LocalizedFormats.NON_INVERTIBLE_TRANSFORM);
      }
    }
    public Line apply(final Hyperplane<Euclidean2D> hyperplane) {
      final Line line = (Line)hyperplane;
      final double rOffset = c1X * line.cos + c1Y * line.sin + c11 * line.originOffset;
      final double rCos = cXX * line.cos + cXY * line.sin;
      final double rSin = cYX * line.cos + cYY * line.sin;
      final double inv = 1.0D / FastMath.sqrt(rSin * rSin + rCos * rCos);
      return new Line(FastMath.PI + FastMath.atan2(-rSin, -rCos), inv * rCos, inv * rSin, inv * rOffset);
    }
    public SubHyperplane<Euclidean1D> apply(final SubHyperplane<Euclidean1D> sub, final Hyperplane<Euclidean2D> original, final Hyperplane<Euclidean2D> transformed) {
      final OrientedPoint op = (OrientedPoint)sub.getHyperplane();
      final Line originalLine = (Line)original;
      final Line transformedLine = (Line)transformed;
      final Vector1D newLoc = transformedLine.toSubSpace(apply(originalLine.toSpace(op.getLocation())));
      return new OrientedPoint(newLoc, op.isDirect()).wholeHyperplane();
    }
    public Vector2D apply(final Vector<Euclidean2D> point) {
      final Vector2D p2D = (Vector2D)point;
      final double x = p2D.getX();
      final double y = p2D.getY();
      return new Vector2D(cXX * x + cXY * y + cX1, cYX * x + cYY * y + cY1);
    }
  }
}