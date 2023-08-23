package org.apache.commons.math3.geometry.partitioning;
import org.apache.commons.math3.geometry.Space;
abstract public class AbstractSubHyperplane<S extends org.apache.commons.math3.geometry.Space, T extends org.apache.commons.math3.geometry.Space> implements SubHyperplane<S>  {
  final private Hyperplane<S> hyperplane;
  final private Region<T> remainingRegion;
  protected AbstractSubHyperplane(final Hyperplane<S> hyperplane, final Region<T> remainingRegion) {
    super();
    this.hyperplane = hyperplane;
    this.remainingRegion = remainingRegion;
  }
  public AbstractSubHyperplane<S, T> applyTransform(final Transform<S, T> transform) {
    final Hyperplane<S> tHyperplane = transform.apply(hyperplane);
    final BSPTree<T> tTree = recurseTransform(remainingRegion.getTree(false), tHyperplane, transform);
    return buildNew(tHyperplane, remainingRegion.buildNew(tTree));
  }
  abstract protected AbstractSubHyperplane<S, T> buildNew(final Hyperplane<S> hyper, final Region<T> remaining);
  public AbstractSubHyperplane<S, T> copySelf() {
    return buildNew(hyperplane, remainingRegion);
  }
  public AbstractSubHyperplane<S, T> reunite(final SubHyperplane<S> other) {
    @SuppressWarnings(value = {"unchecked", }) AbstractSubHyperplane<S, T> o = (AbstractSubHyperplane<S, T>)other;
    return buildNew(hyperplane, new RegionFactory<T>().union(remainingRegion, o.remainingRegion));
  }
  private BSPTree<T> recurseTransform(final BSPTree<T> node, final Hyperplane<S> transformed, final Transform<S, T> transform) {
    if(node.getCut() == null) {
      Object var_1704 = node.getAttribute();
      return new BSPTree<T>(var_1704);
    }
    @SuppressWarnings(value = {"unchecked", }) BoundaryAttribute<T> attribute = (BoundaryAttribute<T>)node.getAttribute();
    if(attribute != null) {
      final SubHyperplane<T> tPO = (attribute.getPlusOutside() == null) ? null : transform.apply(attribute.getPlusOutside(), hyperplane, transformed);
      final SubHyperplane<T> tPI = (attribute.getPlusInside() == null) ? null : transform.apply(attribute.getPlusInside(), hyperplane, transformed);
      attribute = new BoundaryAttribute<T>(tPO, tPI);
    }
    return new BSPTree<T>(transform.apply(node.getCut(), hyperplane, transformed), recurseTransform(node.getPlus(), transformed, transform), recurseTransform(node.getMinus(), transformed, transform), attribute);
  }
  public Hyperplane<S> getHyperplane() {
    return hyperplane;
  }
  public Region<T> getRemainingRegion() {
    return remainingRegion;
  }
  abstract public Side side(Hyperplane<S> hyper);
  abstract public SplitSubHyperplane<S> split(Hyperplane<S> hyper);
  public boolean isEmpty() {
    return remainingRegion.isEmpty();
  }
  public double getSize() {
    return remainingRegion.getSize();
  }
}