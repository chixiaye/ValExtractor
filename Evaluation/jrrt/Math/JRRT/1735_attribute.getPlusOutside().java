package org.apache.commons.math3.geometry.partitioning;
import org.apache.commons.math3.geometry.Space;
class BoundarySizeVisitor<S extends org.apache.commons.math3.geometry.Space> implements BSPTreeVisitor<S>  {
  private double boundarySize;
  public BoundarySizeVisitor() {
    super();
    boundarySize = 0;
  }
  public Order visitOrder(final BSPTree<S> node) {
    return Order.MINUS_SUB_PLUS;
  }
  public double getSize() {
    return boundarySize;
  }
  public void visitInternalNode(final BSPTree<S> node) {
    @SuppressWarnings(value = {"unchecked", }) final BoundaryAttribute<S> attribute = (BoundaryAttribute<S>)node.getAttribute();
    SubHyperplane<S> var_1735 = attribute.getPlusOutside();
    if(var_1735 != null) {
      boundarySize += attribute.getPlusOutside().getSize();
    }
    if(attribute.getPlusInside() != null) {
      boundarySize += attribute.getPlusInside().getSize();
    }
  }
  public void visitLeafNode(final BSPTree<S> node) {
  }
}