package org.apache.commons.math3.geometry.euclidean.oned;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.geometry.partitioning.AbstractRegion;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;
import org.apache.commons.math3.util.Precision;

public class IntervalsSet extends AbstractRegion<Euclidean1D, Euclidean1D>  {
  public IntervalsSet() {
    super();
  }
  public IntervalsSet(final BSPTree<Euclidean1D> tree) {
    super(tree);
  }
  public IntervalsSet(final Collection<SubHyperplane<Euclidean1D>> boundary) {
    super(boundary);
  }
  public IntervalsSet(final double lower, final double upper) {
    super(buildTree(lower, upper));
  }
  private static BSPTree<Euclidean1D> buildTree(final double lower, final double upper) {
    if(Double.isInfinite(lower) && (lower < 0)) {
      if(Double.isInfinite(upper) && (upper > 0)) {
        return new BSPTree<Euclidean1D>(Boolean.TRUE);
      }
      final SubHyperplane<Euclidean1D> upperCut = new OrientedPoint(new Vector1D(upper), true).wholeHyperplane();
      return new BSPTree<Euclidean1D>(upperCut, new BSPTree<Euclidean1D>(Boolean.FALSE), new BSPTree<Euclidean1D>(Boolean.TRUE), null);
    }
    final SubHyperplane<Euclidean1D> lowerCut = new OrientedPoint(new Vector1D(lower), false).wholeHyperplane();
    if(Double.isInfinite(upper) && (upper > 0)) {
      return new BSPTree<Euclidean1D>(lowerCut, new BSPTree<Euclidean1D>(Boolean.FALSE), new BSPTree<Euclidean1D>(Boolean.TRUE), null);
    }
    final SubHyperplane<Euclidean1D> upperCut = new OrientedPoint(new Vector1D(upper), true).wholeHyperplane();
    return new BSPTree<Euclidean1D>(lowerCut, new BSPTree<Euclidean1D>(Boolean.FALSE), new BSPTree<Euclidean1D>(upperCut, new BSPTree<Euclidean1D>(Boolean.FALSE), new BSPTree<Euclidean1D>(Boolean.TRUE), null), null);
  }
  @Override() public IntervalsSet buildNew(final BSPTree<Euclidean1D> tree) {
    return new IntervalsSet(tree);
  }
  public List<Interval> asList() {
    final List<Interval> list = new ArrayList<Interval>();
    recurseList(getTree(false), list, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    return list;
  }
  public double getInf() {
    BSPTree<Euclidean1D> node = getTree(false);
    double inf = Double.POSITIVE_INFINITY;
    while(node.getCut() != null){
      final OrientedPoint op = (OrientedPoint)node.getCut().getHyperplane();
      inf = op.getLocation().getX();
      node = op.isDirect() ? node.getMinus() : node.getPlus();
    }
    return ((Boolean)node.getAttribute()) ? Double.NEGATIVE_INFINITY : inf;
  }
  public double getSup() {
    BSPTree<Euclidean1D> node = getTree(false);
    double sup = Double.NEGATIVE_INFINITY;
    while(node.getCut() != null){
      final OrientedPoint op = (OrientedPoint)node.getCut().getHyperplane();
      sup = op.getLocation().getX();
      node = op.isDirect() ? node.getPlus() : node.getMinus();
    }
    return ((Boolean)node.getAttribute()) ? Double.POSITIVE_INFINITY : sup;
  }
  @Override() protected void computeGeometricalProperties() {
    if(getTree(false).getCut() == null) {
      setBarycenter(Vector1D.NaN);
      setSize(((Boolean)getTree(false).getAttribute()) ? Double.POSITIVE_INFINITY : 0);
    }
    else {
      double size = 0.0D;
      double sum = 0.0D;
      for (final Interval interval : asList()) {
        size += interval.getSize();
        sum += interval.getSize() * interval.getBarycenter();
      }
      setSize(size);
      if(Double.isInfinite(size)) {
        setBarycenter(Vector1D.NaN);
      }
      else 
        if(size >= Precision.SAFE_MIN) {
          setBarycenter(new Vector1D(sum / size));
        }
        else {
          SubHyperplane<Euclidean1D> var_1148 = getTree(false).getCut();
          setBarycenter(((OrientedPoint)var_1148.getHyperplane()).getLocation());
        }
    }
  }
  private void recurseList(final BSPTree<Euclidean1D> node, final List<Interval> list, final double lower, final double upper) {
    if(node.getCut() == null) {
      if((Boolean)node.getAttribute()) {
        list.add(new Interval(lower, upper));
      }
    }
    else {
      final OrientedPoint op = (OrientedPoint)node.getCut().getHyperplane();
      final Vector1D loc = op.getLocation();
      double x = loc.getX();
      final BSPTree<Euclidean1D> low = op.isDirect() ? node.getMinus() : node.getPlus();
      final BSPTree<Euclidean1D> high = op.isDirect() ? node.getPlus() : node.getMinus();
      recurseList(low, list, lower, x);
      if((checkPoint(low, loc) == Location.INSIDE) && (checkPoint(high, loc) == Location.INSIDE)) {
        x = list.remove(list.size() - 1).getInf();
      }
      recurseList(high, list, x, upper);
    }
  }
}