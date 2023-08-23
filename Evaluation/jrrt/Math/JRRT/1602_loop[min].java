package org.apache.commons.math3.geometry.euclidean.twod;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;

class NestedLoops  {
  private Vector2D[] loop;
  private ArrayList<NestedLoops> surrounded;
  private Region<Euclidean2D> polygon;
  private boolean originalIsClockwise;
  public NestedLoops() {
    super();
    surrounded = new ArrayList<NestedLoops>();
  }
  private NestedLoops(final Vector2D[] loop) throws MathIllegalArgumentException {
    super();
    if(loop[0] == null) {
      throw new MathIllegalArgumentException(LocalizedFormats.OUTLINE_BOUNDARY_LOOP_OPEN);
    }
    this.loop = loop;
    surrounded = new ArrayList<NestedLoops>();
    final ArrayList<SubHyperplane<Euclidean2D>> edges = new ArrayList<SubHyperplane<Euclidean2D>>();
    Vector2D current = loop[loop.length - 1];
    for(int i = 0; i < loop.length; ++i) {
      final Vector2D previous = current;
      current = loop[i];
      final Line line = new Line(previous, current);
      final IntervalsSet region = new IntervalsSet(line.toSubSpace(previous).getX(), line.toSubSpace(current).getX());
      edges.add(new SubLine(line, region));
    }
    polygon = new PolygonsSet(edges);
    if(Double.isInfinite(polygon.getSize())) {
      polygon = new RegionFactory<Euclidean2D>().getComplement(polygon);
      originalIsClockwise = false;
    }
    else {
      originalIsClockwise = true;
    }
  }
  private void add(final NestedLoops node) throws MathIllegalArgumentException {
    for (final NestedLoops child : surrounded) {
      if(child.polygon.contains(node.polygon)) {
        child.add(node);
        return ;
      }
    }
    for(final java.util.Iterator<org.apache.commons.math3.geometry.euclidean.twod.NestedLoops> iterator = surrounded.iterator(); iterator.hasNext(); ) {
      final NestedLoops child = iterator.next();
      if(node.polygon.contains(child.polygon)) {
        node.surrounded.add(child);
        iterator.remove();
      }
    }
    RegionFactory<Euclidean2D> factory = new RegionFactory<Euclidean2D>();
    for (final NestedLoops child : surrounded) {
      if(!factory.intersection(node.polygon, child.polygon).isEmpty()) {
        throw new MathIllegalArgumentException(LocalizedFormats.CROSSING_BOUNDARY_LOOPS);
      }
    }
    surrounded.add(node);
  }
  public void add(final Vector2D[] bLoop) throws MathIllegalArgumentException {
    add(new NestedLoops(bLoop));
  }
  public void correctOrientation() {
    for (NestedLoops child : surrounded) {
      child.setClockWise(true);
    }
  }
  private void setClockWise(final boolean clockwise) {
    if(originalIsClockwise ^ clockwise) {
      int min = -1;
      int max = loop.length;
      while(++min < --max){
        Vector2D var_1602 = loop[min];
        final Vector2D tmp = var_1602;
        loop[min] = loop[max];
        loop[max] = tmp;
      }
    }
    for (final NestedLoops child : surrounded) {
      child.setClockWise(!clockwise);
    }
  }
}