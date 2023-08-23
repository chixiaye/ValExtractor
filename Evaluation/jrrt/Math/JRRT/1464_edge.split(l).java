package org.apache.commons.math3.geometry.euclidean.threed;
import java.util.ArrayList;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.AbstractSubHyperplane;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;
import org.apache.commons.math3.geometry.partitioning.BoundaryAttribute;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.apache.commons.math3.util.FastMath;

public class OutlineExtractor  {
  private Vector3D u;
  private Vector3D v;
  private Vector3D w;
  public OutlineExtractor(final Vector3D u, final Vector3D v) {
    super();
    this.u = u;
    this.v = v;
    w = Vector3D.crossProduct(u, v);
  }
  public Vector2D[][] getOutline(final PolyhedronsSet polyhedronsSet) {
    final BoundaryProjector projector = new BoundaryProjector();
    polyhedronsSet.getTree(true).visit(projector);
    final PolygonsSet projected = projector.getProjected();
    final Vector2D[][] outline = projected.getVertices();
    for(int i = 0; i < outline.length; ++i) {
      final Vector2D[] rawLoop = outline[i];
      int end = rawLoop.length;
      int j = 0;
      while(j < end){
        if(pointIsBetween(rawLoop, end, j)) {
          for(int k = j; k < (end - 1); ++k) {
            rawLoop[k] = rawLoop[k + 1];
          }
          --end;
        }
        else {
          ++j;
        }
      }
      if(end != rawLoop.length) {
        outline[i] = new Vector2D[end];
        System.arraycopy(rawLoop, 0, outline[i], 0, end);
      }
    }
    return outline;
  }
  private boolean pointIsBetween(final Vector2D[] loop, final int n, final int i) {
    final Vector2D previous = loop[(i + n - 1) % n];
    final Vector2D current = loop[i];
    final Vector2D next = loop[(i + 1) % n];
    final double dx1 = current.getX() - previous.getX();
    final double dy1 = current.getY() - previous.getY();
    final double dx2 = next.getX() - current.getX();
    final double dy2 = next.getY() - current.getY();
    final double cross = dx1 * dy2 - dx2 * dy1;
    final double dot = dx1 * dx2 + dy1 * dy2;
    final double d1d2 = FastMath.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2));
    return (FastMath.abs(cross) <= (1.0e-6D * d1d2)) && (dot >= 0.0D);
  }
  
  private class BoundaryProjector implements BSPTreeVisitor<Euclidean3D>  {
    private PolygonsSet projected;
    public BoundaryProjector() {
      super();
      projected = new PolygonsSet(new BSPTree<Euclidean2D>(Boolean.FALSE));
    }
    public Order visitOrder(final BSPTree<Euclidean3D> node) {
      return Order.MINUS_SUB_PLUS;
    }
    public PolygonsSet getProjected() {
      return projected;
    }
    private void addContribution(final SubHyperplane<Euclidean3D> facet, final boolean reversed) {
      @SuppressWarnings(value = {"unchecked", }) final AbstractSubHyperplane<Euclidean3D, Euclidean2D> absFacet = (AbstractSubHyperplane<Euclidean3D, Euclidean2D>)facet;
      final Plane plane = (Plane)facet.getHyperplane();
      final double scal = plane.getNormal().dotProduct(w);
      if(FastMath.abs(scal) > 1.0e-3D) {
        Vector2D[][] vertices = ((PolygonsSet)absFacet.getRemainingRegion()).getVertices();
        if((scal < 0) ^ reversed) {
          final Vector2D[][] newVertices = new Vector2D[vertices.length][];
          for(int i = 0; i < vertices.length; ++i) {
            final Vector2D[] loop = vertices[i];
            final Vector2D[] newLoop = new Vector2D[loop.length];
            if(loop[0] == null) {
              newLoop[0] = null;
              for(int j = 1; j < loop.length; ++j) {
                newLoop[j] = loop[loop.length - j];
              }
            }
            else {
              for(int j = 0; j < loop.length; ++j) {
                newLoop[j] = loop[loop.length - (j + 1)];
              }
            }
            newVertices[i] = newLoop;
          }
          vertices = newVertices;
        }
        final ArrayList<SubHyperplane<Euclidean2D>> edges = new ArrayList<SubHyperplane<Euclidean2D>>();
        for (Vector2D[] loop : vertices) {
          final boolean closed = loop[0] != null;
          int previous = closed ? (loop.length - 1) : 1;
          Vector3D previous3D = plane.toSpace(loop[previous]);
          int current = (previous + 1) % loop.length;
          Vector2D pPoint = new Vector2D(previous3D.dotProduct(u), previous3D.dotProduct(v));
          while(current < loop.length){
            final Vector3D current3D = plane.toSpace(loop[current]);
            final Vector2D cPoint = new Vector2D(current3D.dotProduct(u), current3D.dotProduct(v));
            final org.apache.commons.math3.geometry.euclidean.twod.Line line = new org.apache.commons.math3.geometry.euclidean.twod.Line(pPoint, cPoint);
            SubHyperplane<Euclidean2D> edge = line.wholeHyperplane();
            if(closed || (previous != 1)) {
              final double angle = line.getAngle() + 0.5D * FastMath.PI;
              final org.apache.commons.math3.geometry.euclidean.twod.Line l = new org.apache.commons.math3.geometry.euclidean.twod.Line(pPoint, angle);
              SubHyperplane.SplitSubHyperplane<Euclidean2D> var_1464 = edge.split(l);
              edge = var_1464.getPlus();
            }
            if(closed || (current != (loop.length - 1))) {
              final double angle = line.getAngle() + 0.5D * FastMath.PI;
              final org.apache.commons.math3.geometry.euclidean.twod.Line l = new org.apache.commons.math3.geometry.euclidean.twod.Line(cPoint, angle);
              edge = edge.split(l).getMinus();
            }
            edges.add(edge);
            previous = current++;
            previous3D = current3D;
            pPoint = cPoint;
          }
        }
        final PolygonsSet projectedFacet = new PolygonsSet(edges);
        projected = (PolygonsSet)new RegionFactory<Euclidean2D>().union(projected, projectedFacet);
      }
    }
    public void visitInternalNode(final BSPTree<Euclidean3D> node) {
      @SuppressWarnings(value = {"unchecked", }) final BoundaryAttribute<Euclidean3D> attribute = (BoundaryAttribute<Euclidean3D>)node.getAttribute();
      if(attribute.getPlusOutside() != null) {
        addContribution(attribute.getPlusOutside(), false);
      }
      if(attribute.getPlusInside() != null) {
        addContribution(attribute.getPlusInside(), true);
      }
    }
    public void visitLeafNode(final BSPTree<Euclidean3D> node) {
    }
  }
}