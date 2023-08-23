package org.apache.commons.math3.geometry.euclidean.threed;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.partitioning.AbstractSubHyperplane;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.Side;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;

public class SubPlane extends AbstractSubHyperplane<Euclidean3D, Euclidean2D>  {
  public SubPlane(final Hyperplane<Euclidean3D> hyperplane, final Region<Euclidean2D> remainingRegion) {
    super(hyperplane, remainingRegion);
  }
  @Override() protected AbstractSubHyperplane<Euclidean3D, Euclidean2D> buildNew(final Hyperplane<Euclidean3D> hyperplane, final Region<Euclidean2D> remainingRegion) {
    return new SubPlane(hyperplane, remainingRegion);
  }
  @Override() public Side side(Hyperplane<Euclidean3D> hyperplane) {
    final Plane otherPlane = (Plane)hyperplane;
    final Plane thisPlane = (Plane)getHyperplane();
    final Line inter = otherPlane.intersection(thisPlane);
    if(inter == null) {
      final double global = otherPlane.getOffset(thisPlane);
      return (global < -1.0e-10D) ? Side.MINUS : ((global > 1.0e-10D) ? Side.PLUS : Side.HYPER);
    }
    Vector2D p = thisPlane.toSubSpace(inter.toSpace(Vector1D.ZERO));
    Vector2D q = thisPlane.toSubSpace(inter.toSpace(Vector1D.ONE));
    Vector3D crossP = Vector3D.crossProduct(inter.getDirection(), thisPlane.getNormal());
    if(crossP.dotProduct(otherPlane.getNormal()) < 0) {
      final Vector2D tmp = p;
      p = q;
      q = tmp;
    }
    final org.apache.commons.math3.geometry.euclidean.twod.Line line2D = new org.apache.commons.math3.geometry.euclidean.twod.Line(p, q);
    return getRemainingRegion().side(line2D);
  }
  @Override() public SplitSubHyperplane<Euclidean3D> split(Hyperplane<Euclidean3D> hyperplane) {
    final Plane otherPlane = (Plane)hyperplane;
    final Plane thisPlane = (Plane)getHyperplane();
    final Line inter = otherPlane.intersection(thisPlane);
    if(inter == null) {
      final double global = otherPlane.getOffset(thisPlane);
      return (global < -1.0e-10D) ? new SplitSubHyperplane<Euclidean3D>(null, this) : new SplitSubHyperplane<Euclidean3D>(this, null);
    }
    Vector2D p = thisPlane.toSubSpace(inter.toSpace(Vector1D.ZERO));
    Vector2D q = thisPlane.toSubSpace(inter.toSpace(Vector1D.ONE));
    Vector3D crossP = Vector3D.crossProduct(inter.getDirection(), thisPlane.getNormal());
    if(crossP.dotProduct(otherPlane.getNormal()) < 0) {
      final Vector2D tmp = p;
      p = q;
      q = tmp;
    }
    final SubHyperplane<Euclidean2D> l2DMinus = new org.apache.commons.math3.geometry.euclidean.twod.Line(p, q).wholeHyperplane();
    final SubHyperplane<Euclidean2D> l2DPlus = new org.apache.commons.math3.geometry.euclidean.twod.Line(q, p).wholeHyperplane();
    final BSPTree<Euclidean2D> splitTree = getRemainingRegion().getTree(false).split(l2DMinus);
    final BSPTree<Euclidean2D> plusTree = getRemainingRegion().isEmpty(splitTree.getPlus()) ? new BSPTree<Euclidean2D>(Boolean.FALSE) : new BSPTree<Euclidean2D>(l2DPlus, new BSPTree<Euclidean2D>(Boolean.FALSE), splitTree.getPlus(), null);
    final BSPTree<Euclidean2D> minusTree = getRemainingRegion().isEmpty(splitTree.getMinus()) ? new BSPTree<Euclidean2D>(Boolean.FALSE) : new BSPTree<Euclidean2D>(l2DMinus, new BSPTree<Euclidean2D>(Boolean.FALSE), splitTree.getMinus(), null);
    Plane var_1474 = thisPlane.copySelf();
    return new SplitSubHyperplane<Euclidean3D>(new SubPlane(var_1474, new PolygonsSet(plusTree)), new SubPlane(thisPlane.copySelf(), new PolygonsSet(minusTree)));
  }
}