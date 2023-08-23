package org.apache.commons.math3.geometry.partitioning;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.geometry.Space;
import org.apache.commons.math3.geometry.Vector;
abstract public class AbstractRegion<S extends org.apache.commons.math3.geometry.Space, T extends org.apache.commons.math3.geometry.Space> implements Region<S>  {
  private BSPTree<S> tree;
  private double size;
  private Vector<S> barycenter;
  protected AbstractRegion() {
    super();
    tree = new BSPTree<S>(Boolean.TRUE);
  }
  protected AbstractRegion(final BSPTree<S> tree) {
    super();
    this.tree = tree;
  }
  protected AbstractRegion(final Collection<SubHyperplane<S>> boundary) {
    super();
    if(boundary.size() == 0) {
      tree = new BSPTree<S>(Boolean.TRUE);
    }
    else {
      final TreeSet<SubHyperplane<S>> ordered = new TreeSet<SubHyperplane<S>>(new Comparator<SubHyperplane<S>>() {
          public int compare(final SubHyperplane<S> o1, final SubHyperplane<S> o2) {
            final double size1 = o1.getSize();
            final double size2 = o2.getSize();
            return (size2 < size1) ? -1 : ((o1 == o2) ? 0 : +1);
          }
      });
      ordered.addAll(boundary);
      tree = new BSPTree<S>();
      insertCuts(tree, ordered);
      tree.visit(new BSPTreeVisitor<S>() {
          public Order visitOrder(final BSPTree<S> node) {
            return Order.PLUS_SUB_MINUS;
          }
          public void visitInternalNode(final BSPTree<S> node) {
          }
          public void visitLeafNode(final BSPTree<S> node) {
            node.setAttribute((node == node.getParent().getPlus()) ? Boolean.FALSE : Boolean.TRUE);
          }
      });
    }
  }
  public AbstractRegion(final Hyperplane<S>[] hyperplanes) {
    super();
    if((hyperplanes == null) || (hyperplanes.length == 0)) {
      tree = new BSPTree<S>(Boolean.FALSE);
    }
    else {
      tree = hyperplanes[0].wholeSpace().getTree(false);
      BSPTree<S> node = tree;
      node.setAttribute(Boolean.TRUE);
      for (final Hyperplane<S> hyperplane : hyperplanes) {
        if(node.insertCut(hyperplane)) {
          node.setAttribute(null);
          node.getPlus().setAttribute(Boolean.FALSE);
          node = node.getMinus();
          node.setAttribute(Boolean.TRUE);
        }
      }
    }
  }
  public AbstractRegion<S, T> applyTransform(final Transform<S, T> transform) {
    return buildNew(recurseTransform(getTree(false), transform));
  }
  abstract public AbstractRegion<S, T> buildNew(BSPTree<S> newTree);
  public AbstractRegion<S, T> copySelf() {
    return buildNew(tree.copySelf());
  }
  public BSPTree<S> getTree(final boolean includeBoundaryAttributes) {
    if(includeBoundaryAttributes && (tree.getCut() != null) && (tree.getAttribute() == null)) {
      tree.visit(new BoundaryBuilder<S>());
    }
    return tree;
  }
  @SuppressWarnings(value = {"unchecked", }) private BSPTree<S> recurseTransform(final BSPTree<S> node, final Transform<S, T> transform) {
    if(node.getCut() == null) {
      return new BSPTree<S>(node.getAttribute());
    }
    final SubHyperplane<S> sub = node.getCut();
    final SubHyperplane<S> tSub = ((AbstractSubHyperplane<S, T>)sub).applyTransform(transform);
    BoundaryAttribute<S> attribute = (BoundaryAttribute<S>)node.getAttribute();
    if(attribute != null) {
      final SubHyperplane<S> tPO = (attribute.getPlusOutside() == null) ? null : ((AbstractSubHyperplane<S, T>)attribute.getPlusOutside()).applyTransform(transform);
      final SubHyperplane<S> tPI = (attribute.getPlusInside() == null) ? null : ((AbstractSubHyperplane<S, T>)attribute.getPlusInside()).applyTransform(transform);
      attribute = new BoundaryAttribute<S>(tPO, tPI);
    }
    return new BSPTree<S>(tSub, recurseTransform(node.getPlus(), transform), recurseTransform(node.getMinus(), transform), attribute);
  }
  public Location checkPoint(final Vector<S> point) {
    return checkPoint(tree, point);
  }
  protected Location checkPoint(final BSPTree<S> node, final Vector<S> point) {
    final BSPTree<S> cell = node.getCell(point);
    if(cell.getCut() == null) {
      return ((Boolean)cell.getAttribute()) ? Location.INSIDE : Location.OUTSIDE;
    }
    final Location minusCode = checkPoint(cell.getMinus(), point);
    final Location plusCode = checkPoint(cell.getPlus(), point);
    return (minusCode == plusCode) ? minusCode : Location.BOUNDARY;
  }
  public Side side(final Hyperplane<S> hyperplane) {
    final Sides sides = new Sides();
    recurseSides(tree, hyperplane.wholeHyperplane(), sides);
    return sides.plusFound() ? (sides.minusFound() ? Side.BOTH : Side.PLUS) : (sides.minusFound() ? Side.MINUS : Side.HYPER);
  }
  public SubHyperplane<S> intersection(final SubHyperplane<S> sub) {
    return recurseIntersection(tree, sub);
  }
  private SubHyperplane<S> recurseIntersection(final BSPTree<S> node, final SubHyperplane<S> sub) {
    if(node.getCut() == null) {
      return (Boolean)node.getAttribute() ? sub.copySelf() : null;
    }
    SubHyperplane<S> var_1696 = node.getCut();
    final Hyperplane<S> hyperplane = var_1696.getHyperplane();
    switch (sub.side(hyperplane)){
      case PLUS:
      return recurseIntersection(node.getPlus(), sub);
      case MINUS:
      return recurseIntersection(node.getMinus(), sub);
      case BOTH:
      final SubHyperplane.SplitSubHyperplane<S> split = sub.split(hyperplane);
      final SubHyperplane<S> plus = recurseIntersection(node.getPlus(), split.getPlus());
      final SubHyperplane<S> minus = recurseIntersection(node.getMinus(), split.getMinus());
      if(plus == null) {
        return minus;
      }
      else 
        if(minus == null) {
          return plus;
        }
        else {
          return plus.reunite(minus);
        }
      default:
      return recurseIntersection(node.getPlus(), recurseIntersection(node.getMinus(), sub));
    }
  }
  public Vector<S> getBarycenter() {
    if(barycenter == null) {
      computeGeometricalProperties();
    }
    return barycenter;
  }
  public boolean contains(final Region<S> region) {
    return new RegionFactory<S>().difference(region, this).isEmpty();
  }
  public boolean isEmpty() {
    return isEmpty(tree);
  }
  public boolean isEmpty(final BSPTree<S> node) {
    if(node.getCut() == null) {
      return !((Boolean)node.getAttribute());
    }
    return isEmpty(node.getMinus()) && isEmpty(node.getPlus());
  }
  public double getBoundarySize() {
    final BoundarySizeVisitor<S> visitor = new BoundarySizeVisitor<S>();
    getTree(true).visit(visitor);
    return visitor.getSize();
  }
  public double getSize() {
    if(barycenter == null) {
      computeGeometricalProperties();
    }
    return size;
  }
  abstract protected void computeGeometricalProperties();
  private void insertCuts(final BSPTree<S> node, final Collection<SubHyperplane<S>> boundary) {
    final Iterator<SubHyperplane<S>> iterator = boundary.iterator();
    Hyperplane<S> inserted = null;
    while((inserted == null) && iterator.hasNext()){
      inserted = iterator.next().getHyperplane();
      if(!node.insertCut(inserted.copySelf())) {
        inserted = null;
      }
    }
    if(!iterator.hasNext()) {
      return ;
    }
    final ArrayList<SubHyperplane<S>> plusList = new ArrayList<SubHyperplane<S>>();
    final ArrayList<SubHyperplane<S>> minusList = new ArrayList<SubHyperplane<S>>();
    while(iterator.hasNext()){
      final SubHyperplane<S> other = iterator.next();
      switch (other.side(inserted)){
        case PLUS:
        plusList.add(other);
        break ;
        case MINUS:
        minusList.add(other);
        break ;
        case BOTH:
        final SubHyperplane.SplitSubHyperplane<S> split = other.split(inserted);
        plusList.add(split.getPlus());
        minusList.add(split.getMinus());
        break ;
        default:
      }
    }
    insertCuts(node.getPlus(), plusList);
    insertCuts(node.getMinus(), minusList);
  }
  private void recurseSides(final BSPTree<S> node, final SubHyperplane<S> sub, final Sides sides) {
    if(node.getCut() == null) {
      if((Boolean)node.getAttribute()) {
        sides.rememberPlusFound();
        sides.rememberMinusFound();
      }
      return ;
    }
    final Hyperplane<S> hyperplane = node.getCut().getHyperplane();
    switch (sub.side(hyperplane)){
      case PLUS:
      if(node.getCut().side(sub.getHyperplane()) == Side.PLUS) {
        if(!isEmpty(node.getMinus())) {
          sides.rememberPlusFound();
        }
      }
      else {
        if(!isEmpty(node.getMinus())) {
          sides.rememberMinusFound();
        }
      }
      if(!(sides.plusFound() && sides.minusFound())) {
        recurseSides(node.getPlus(), sub, sides);
      }
      break ;
      case MINUS:
      if(node.getCut().side(sub.getHyperplane()) == Side.PLUS) {
        if(!isEmpty(node.getPlus())) {
          sides.rememberPlusFound();
        }
      }
      else {
        if(!isEmpty(node.getPlus())) {
          sides.rememberMinusFound();
        }
      }
      if(!(sides.plusFound() && sides.minusFound())) {
        recurseSides(node.getMinus(), sub, sides);
      }
      break ;
      case BOTH:
      final SubHyperplane.SplitSubHyperplane<S> split = sub.split(hyperplane);
      recurseSides(node.getPlus(), split.getPlus(), sides);
      if(!(sides.plusFound() && sides.minusFound())) {
        recurseSides(node.getMinus(), split.getMinus(), sides);
      }
      break ;
      default:
      if(node.getCut().getHyperplane().sameOrientationAs(sub.getHyperplane())) {
        if((node.getPlus().getCut() != null) || ((Boolean)node.getPlus().getAttribute())) {
          sides.rememberPlusFound();
        }
        if((node.getMinus().getCut() != null) || ((Boolean)node.getMinus().getAttribute())) {
          sides.rememberMinusFound();
        }
      }
      else {
        if((node.getPlus().getCut() != null) || ((Boolean)node.getPlus().getAttribute())) {
          sides.rememberMinusFound();
        }
        if((node.getMinus().getCut() != null) || ((Boolean)node.getMinus().getAttribute())) {
          sides.rememberPlusFound();
        }
      }
    }
  }
  protected void setBarycenter(final Vector<S> barycenter) {
    this.barycenter = barycenter;
  }
  protected void setSize(final double size) {
    this.size = size;
  }
  private static class BoundaryBuilder<S extends org.apache.commons.math3.geometry.Space> implements BSPTreeVisitor<S>  {
    public Order visitOrder(BSPTree<S> node) {
      return Order.PLUS_MINUS_SUB;
    }
    private void characterize(final BSPTree<S> node, final SubHyperplane<S> sub, final SubHyperplane<S>[] characterization) {
      if(node.getCut() == null) {
        final boolean inside = (Boolean)node.getAttribute();
        if(inside) {
          if(characterization[1] == null) {
            characterization[1] = sub;
          }
          else {
            characterization[1] = characterization[1].reunite(sub);
          }
        }
        else {
          if(characterization[0] == null) {
            characterization[0] = sub;
          }
          else {
            characterization[0] = characterization[0].reunite(sub);
          }
        }
      }
      else {
        final Hyperplane<S> hyperplane = node.getCut().getHyperplane();
        switch (sub.side(hyperplane)){
          case PLUS:
          characterize(node.getPlus(), sub, characterization);
          break ;
          case MINUS:
          characterize(node.getMinus(), sub, characterization);
          break ;
          case BOTH:
          final SubHyperplane.SplitSubHyperplane<S> split = sub.split(hyperplane);
          characterize(node.getPlus(), split.getPlus(), characterization);
          characterize(node.getMinus(), split.getMinus(), characterization);
          break ;
          default:
          throw new MathInternalError();
        }
      }
    }
    public void visitInternalNode(BSPTree<S> node) {
      SubHyperplane<S> plusOutside = null;
      SubHyperplane<S> plusInside = null;
      @SuppressWarnings(value = {"unchecked", }) final SubHyperplane<S>[] plusChar = (SubHyperplane<S>[])Array.newInstance(SubHyperplane.class, 2);
      characterize(node.getPlus(), node.getCut().copySelf(), plusChar);
      if(plusChar[0] != null && !plusChar[0].isEmpty()) {
        @SuppressWarnings(value = {"unchecked", }) final SubHyperplane<S>[] minusChar = (SubHyperplane<S>[])Array.newInstance(SubHyperplane.class, 2);
        characterize(node.getMinus(), plusChar[0], minusChar);
        if(minusChar[1] != null && !minusChar[1].isEmpty()) {
          plusOutside = minusChar[1];
        }
      }
      if(plusChar[1] != null && !plusChar[1].isEmpty()) {
        @SuppressWarnings(value = {"unchecked", }) final SubHyperplane<S>[] minusChar = (SubHyperplane<S>[])Array.newInstance(SubHyperplane.class, 2);
        characterize(node.getMinus(), plusChar[1], minusChar);
        if(minusChar[0] != null && !minusChar[0].isEmpty()) {
          plusInside = minusChar[0];
        }
      }
      node.setAttribute(new BoundaryAttribute<S>(plusOutside, plusInside));
    }
    public void visitLeafNode(BSPTree<S> node) {
    }
  }
  
  final private static class Sides  {
    private boolean plusFound;
    private boolean minusFound;
    public Sides() {
      super();
      plusFound = false;
      minusFound = false;
    }
    public boolean minusFound() {
      return minusFound;
    }
    public boolean plusFound() {
      return plusFound;
    }
    public void rememberMinusFound() {
      minusFound = true;
    }
    public void rememberPlusFound() {
      plusFound = true;
    }
  }
}