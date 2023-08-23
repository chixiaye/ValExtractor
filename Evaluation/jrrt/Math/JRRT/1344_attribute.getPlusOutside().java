package org.apache.commons.math3.geometry.euclidean.threed;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.oned.Euclidean1D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.SubLine;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.AbstractRegion;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;
import org.apache.commons.math3.geometry.partitioning.BoundaryAttribute;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.apache.commons.math3.geometry.partitioning.Transform;
import org.apache.commons.math3.util.FastMath;

public class PolyhedronsSet extends AbstractRegion<Euclidean3D, Euclidean2D>  {
  public PolyhedronsSet() {
    super();
  }
  public PolyhedronsSet(final BSPTree<Euclidean3D> tree) {
    super(tree);
  }
  public PolyhedronsSet(final Collection<SubHyperplane<Euclidean3D>> boundary) {
    super(boundary);
  }
  public PolyhedronsSet(final double xMin, final double xMax, final double yMin, final double yMax, final double zMin, final double zMax) {
    super(buildBoundary(xMin, xMax, yMin, yMax, zMin, zMax));
  }
  private static BSPTree<Euclidean3D> buildBoundary(final double xMin, final double xMax, final double yMin, final double yMax, final double zMin, final double zMax) {
    final Plane pxMin = new Plane(new Vector3D(xMin, 0, 0), Vector3D.MINUS_I);
    final Plane pxMax = new Plane(new Vector3D(xMax, 0, 0), Vector3D.PLUS_I);
    final Plane pyMin = new Plane(new Vector3D(0, yMin, 0), Vector3D.MINUS_J);
    final Plane pyMax = new Plane(new Vector3D(0, yMax, 0), Vector3D.PLUS_J);
    final Plane pzMin = new Plane(new Vector3D(0, 0, zMin), Vector3D.MINUS_K);
    final Plane pzMax = new Plane(new Vector3D(0, 0, zMax), Vector3D.PLUS_K);
    @SuppressWarnings(value = {"unchecked", }) final Region<Euclidean3D> boundary = new RegionFactory<Euclidean3D>().buildConvex(pxMin, pxMax, pyMin, pyMax, pzMin, pzMax);
    return boundary.getTree(false);
  }
  @Override() public PolyhedronsSet buildNew(final BSPTree<Euclidean3D> tree) {
    return new PolyhedronsSet(tree);
  }
  public PolyhedronsSet rotate(final Vector3D center, final Rotation rotation) {
    return (PolyhedronsSet)applyTransform(new RotationTransform(center, rotation));
  }
  public PolyhedronsSet translate(final Vector3D translation) {
    return (PolyhedronsSet)applyTransform(new TranslationTransform(translation));
  }
  private SubHyperplane<Euclidean3D> boundaryFacet(final Vector3D point, final BSPTree<Euclidean3D> node) {
    final Vector2D point2D = ((Plane)node.getCut().getHyperplane()).toSubSpace(point);
    @SuppressWarnings(value = {"unchecked", }) final BoundaryAttribute<Euclidean3D> attribute = (BoundaryAttribute<Euclidean3D>)node.getAttribute();
    if((attribute.getPlusOutside() != null) && (((SubPlane)attribute.getPlusOutside()).getRemainingRegion().checkPoint(point2D) == Location.INSIDE)) {
      return attribute.getPlusOutside();
    }
    if((attribute.getPlusInside() != null) && (((SubPlane)attribute.getPlusInside()).getRemainingRegion().checkPoint(point2D) == Location.INSIDE)) {
      return attribute.getPlusInside();
    }
    return null;
  }
  public SubHyperplane<Euclidean3D> firstIntersection(final Vector3D point, final Line line) {
    return recurseFirstIntersection(getTree(true), point, line);
  }
  private SubHyperplane<Euclidean3D> recurseFirstIntersection(final BSPTree<Euclidean3D> node, final Vector3D point, final Line line) {
    final SubHyperplane<Euclidean3D> cut = node.getCut();
    if(cut == null) {
      return null;
    }
    final BSPTree<Euclidean3D> minus = node.getMinus();
    final BSPTree<Euclidean3D> plus = node.getPlus();
    final Plane plane = (Plane)cut.getHyperplane();
    final double offset = plane.getOffset(point);
    final boolean in = FastMath.abs(offset) < 1.0e-10D;
    final BSPTree<Euclidean3D> near;
    final BSPTree<Euclidean3D> far;
    if(offset < 0) {
      near = minus;
      far = plus;
    }
    else {
      near = plus;
      far = minus;
    }
    if(in) {
      final SubHyperplane<Euclidean3D> facet = boundaryFacet(point, node);
      if(facet != null) {
        return facet;
      }
    }
    final SubHyperplane<Euclidean3D> crossed = recurseFirstIntersection(near, point, line);
    if(crossed != null) {
      return crossed;
    }
    if(!in) {
      final Vector3D hit3D = plane.intersection(line);
      if(hit3D != null) {
        final SubHyperplane<Euclidean3D> facet = boundaryFacet(hit3D, node);
        if(facet != null) {
          return facet;
        }
      }
    }
    return recurseFirstIntersection(far, point, line);
  }
  @Override() protected void computeGeometricalProperties() {
    getTree(true).visit(new FacetsContributionVisitor());
    if(getSize() < 0) {
      setSize(Double.POSITIVE_INFINITY);
      setBarycenter(Vector3D.NaN);
    }
    else {
      setSize(getSize() / 3.0D);
      setBarycenter(new Vector3D(1.0D / (4 * getSize()), (Vector3D)getBarycenter()));
    }
  }
  
  private class FacetsContributionVisitor implements BSPTreeVisitor<Euclidean3D>  {
    public FacetsContributionVisitor() {
      super();
      setSize(0);
      setBarycenter(new Vector3D(0, 0, 0));
    }
    public Order visitOrder(final BSPTree<Euclidean3D> node) {
      return Order.MINUS_SUB_PLUS;
    }
    private void addContribution(final SubHyperplane<Euclidean3D> facet, final boolean reversed) {
      final Region<Euclidean2D> polygon = ((SubPlane)facet).getRemainingRegion();
      final double area = polygon.getSize();
      if(Double.isInfinite(area)) {
        setSize(Double.POSITIVE_INFINITY);
        setBarycenter(Vector3D.NaN);
      }
      else {
        final Plane plane = (Plane)facet.getHyperplane();
        final Vector3D facetB = plane.toSpace(polygon.getBarycenter());
        double scaled = area * facetB.dotProduct(plane.getNormal());
        if(reversed) {
          scaled = -scaled;
        }
        setSize(getSize() + scaled);
        setBarycenter(new Vector3D(1.0D, (Vector3D)getBarycenter(), scaled, facetB));
      }
    }
    public void visitInternalNode(final BSPTree<Euclidean3D> node) {
      @SuppressWarnings(value = {"unchecked", }) final BoundaryAttribute<Euclidean3D> attribute = (BoundaryAttribute<Euclidean3D>)node.getAttribute();
      SubHyperplane<Euclidean3D> var_1344 = attribute.getPlusOutside();
      if(var_1344 != null) {
        addContribution(attribute.getPlusOutside(), false);
      }
      if(attribute.getPlusInside() != null) {
        addContribution(attribute.getPlusInside(), true);
      }
    }
    public void visitLeafNode(final BSPTree<Euclidean3D> node) {
    }
  }
  
  private static class RotationTransform implements Transform<Euclidean3D, Euclidean2D>  {
    private Vector3D center;
    private Rotation rotation;
    private Plane cachedOriginal;
    private Transform<Euclidean2D, Euclidean1D> cachedTransform;
    public RotationTransform(final Vector3D center, final Rotation rotation) {
      super();
      this.center = center;
      this.rotation = rotation;
    }
    public Plane apply(final Hyperplane<Euclidean3D> hyperplane) {
      return ((Plane)hyperplane).rotate(center, rotation);
    }
    public SubHyperplane<Euclidean2D> apply(final SubHyperplane<Euclidean2D> sub, final Hyperplane<Euclidean3D> original, final Hyperplane<Euclidean3D> transformed) {
      if(original != cachedOriginal) {
        final Plane oPlane = (Plane)original;
        final Plane tPlane = (Plane)transformed;
        final Vector3D p00 = oPlane.getOrigin();
        final Vector3D p10 = oPlane.toSpace(new Vector2D(1.0D, 0.0D));
        final Vector3D p01 = oPlane.toSpace(new Vector2D(0.0D, 1.0D));
        final Vector2D tP00 = tPlane.toSubSpace(apply(p00));
        final Vector2D tP10 = tPlane.toSubSpace(apply(p10));
        final Vector2D tP01 = tPlane.toSubSpace(apply(p01));
        final AffineTransform at = new AffineTransform(tP10.getX() - tP00.getX(), tP10.getY() - tP00.getY(), tP01.getX() - tP00.getX(), tP01.getY() - tP00.getY(), tP00.getX(), tP00.getY());
        cachedOriginal = (Plane)original;
        cachedTransform = org.apache.commons.math3.geometry.euclidean.twod.Line.getTransform(at);
      }
      return ((SubLine)sub).applyTransform(cachedTransform);
    }
    public Vector3D apply(final Vector<Euclidean3D> point) {
      final Vector3D delta = ((Vector3D)point).subtract(center);
      return new Vector3D(1.0D, center, 1.0D, rotation.applyTo(delta));
    }
  }
  
  private static class TranslationTransform implements Transform<Euclidean3D, Euclidean2D>  {
    private Vector3D translation;
    private Plane cachedOriginal;
    private Transform<Euclidean2D, Euclidean1D> cachedTransform;
    public TranslationTransform(final Vector3D translation) {
      super();
      this.translation = translation;
    }
    public Plane apply(final Hyperplane<Euclidean3D> hyperplane) {
      return ((Plane)hyperplane).translate(translation);
    }
    public SubHyperplane<Euclidean2D> apply(final SubHyperplane<Euclidean2D> sub, final Hyperplane<Euclidean3D> original, final Hyperplane<Euclidean3D> transformed) {
      if(original != cachedOriginal) {
        final Plane oPlane = (Plane)original;
        final Plane tPlane = (Plane)transformed;
        final Vector2D shift = tPlane.toSubSpace(apply(oPlane.getOrigin()));
        final AffineTransform at = AffineTransform.getTranslateInstance(shift.getX(), shift.getY());
        cachedOriginal = (Plane)original;
        cachedTransform = org.apache.commons.math3.geometry.euclidean.twod.Line.getTransform(at);
      }
      return ((SubLine)sub).applyTransform(cachedTransform);
    }
    public Vector3D apply(final Vector<Euclidean3D> point) {
      return new Vector3D(1.0D, (Vector3D)point, 1.0D, translation);
    }
  }
}