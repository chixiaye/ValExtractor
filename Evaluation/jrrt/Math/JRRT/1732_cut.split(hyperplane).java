package org.apache.commons.math3.geometry.partitioning;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.Space;
import org.apache.commons.math3.util.FastMath;
public class BSPTree<S extends org.apache.commons.math3.geometry.Space>  {
  private SubHyperplane<S> cut;
  private BSPTree<S> plus;
  private BSPTree<S> minus;
  private BSPTree<S> parent;
  private Object attribute;
  public BSPTree() {
    super();
    cut = null;
    plus = null;
    minus = null;
    parent = null;
    attribute = null;
  }
  public BSPTree(final Object attribute) {
    super();
    cut = null;
    plus = null;
    minus = null;
    parent = null;
    this.attribute = attribute;
  }
  public BSPTree(final SubHyperplane<S> cut, final BSPTree<S> plus, final BSPTree<S> minus, final Object attribute) {
    super();
    this.cut = cut;
    this.plus = plus;
    this.minus = minus;
    this.parent = null;
    this.attribute = attribute;
    plus.parent = this;
    minus.parent = this;
  }
  public BSPTree<S> copySelf() {
    if(cut == null) {
      return new BSPTree<S>(attribute);
    }
    return new BSPTree<S>(cut.copySelf(), plus.copySelf(), minus.copySelf(), attribute);
  }
  public BSPTree<S> getCell(final Vector<S> point) {
    if(cut == null) {
      return this;
    }
    final double offset = cut.getHyperplane().getOffset(point);
    if(FastMath.abs(offset) < 1.0e-10D) {
      return this;
    }
    else 
      if(offset <= 0) {
        return minus.getCell(point);
      }
      else {
        return plus.getCell(point);
      }
  }
  public BSPTree<S> getMinus() {
    return minus;
  }
  public BSPTree<S> getParent() {
    return parent;
  }
  public BSPTree<S> getPlus() {
    return plus;
  }
  public BSPTree<S> merge(final BSPTree<S> tree, final LeafMerger<S> leafMerger) {
    return merge(tree, leafMerger, null, false);
  }
  private BSPTree<S> merge(final BSPTree<S> tree, final LeafMerger<S> leafMerger, final BSPTree<S> parentTree, final boolean isPlusChild) {
    if(cut == null) {
      return leafMerger.merge(this, tree, parentTree, isPlusChild, true);
    }
    else 
      if(tree.cut == null) {
        return leafMerger.merge(tree, this, parentTree, isPlusChild, false);
      }
      else {
        final BSPTree<S> merged = tree.split(cut);
        if(parentTree != null) {
          merged.parent = parentTree;
          if(isPlusChild) {
            parentTree.plus = merged;
          }
          else {
            parentTree.minus = merged;
          }
        }
        plus.merge(merged.plus, leafMerger, merged, true);
        minus.merge(merged.minus, leafMerger, merged, false);
        merged.condense();
        if(merged.cut != null) {
          merged.cut = merged.fitToCell(merged.cut.getHyperplane().wholeHyperplane());
        }
        return merged;
      }
  }
  public BSPTree<S> split(final SubHyperplane<S> sub) {
    if(cut == null) {
      return new BSPTree<S>(sub, copySelf(), new BSPTree<S>(attribute), null);
    }
    final Hyperplane<S> cHyperplane = cut.getHyperplane();
    final Hyperplane<S> sHyperplane = sub.getHyperplane();
    switch (sub.side(cHyperplane)){
      case PLUS:
      {
        final BSPTree<S> split = plus.split(sub);
        if(cut.side(sHyperplane) == Side.PLUS) {
          split.plus = new BSPTree<S>(cut.copySelf(), split.plus, minus.copySelf(), attribute);
          split.plus.condense();
          split.plus.parent = split;
        }
        else {
          split.minus = new BSPTree<S>(cut.copySelf(), split.minus, minus.copySelf(), attribute);
          split.minus.condense();
          split.minus.parent = split;
        }
        return split;
      }
      case MINUS:
      {
        final BSPTree<S> split = minus.split(sub);
        if(cut.side(sHyperplane) == Side.PLUS) {
          split.plus = new BSPTree<S>(cut.copySelf(), plus.copySelf(), split.plus, attribute);
          split.plus.condense();
          split.plus.parent = split;
        }
        else {
          split.minus = new BSPTree<S>(cut.copySelf(), plus.copySelf(), split.minus, attribute);
          split.minus.condense();
          split.minus.parent = split;
        }
        return split;
      }
      case BOTH:
      {
        final SubHyperplane.SplitSubHyperplane<S> cutParts = cut.split(sHyperplane);
        final SubHyperplane.SplitSubHyperplane<S> subParts = sub.split(cHyperplane);
        final BSPTree<S> split = new BSPTree<S>(sub, plus.split(subParts.getPlus()), minus.split(subParts.getMinus()), null);
        split.plus.cut = cutParts.getPlus();
        split.minus.cut = cutParts.getMinus();
        final BSPTree<S> tmp = split.plus.minus;
        split.plus.minus = split.minus.plus;
        split.plus.minus.parent = split.plus;
        split.minus.plus = tmp;
        split.minus.plus.parent = split.minus;
        split.plus.condense();
        split.minus.condense();
        return split;
      }
      default:
      return cHyperplane.sameOrientationAs(sHyperplane) ? new BSPTree<S>(sub, plus.copySelf(), minus.copySelf(), attribute) : new BSPTree<S>(sub, minus.copySelf(), plus.copySelf(), attribute);
    }
  }
  public Object getAttribute() {
    return attribute;
  }
  private SubHyperplane<S> fitToCell(final SubHyperplane<S> sub) {
    SubHyperplane<S> s = sub;
    for(org.apache.commons.math3.geometry.partitioning.BSPTree<org.apache.commons.math3.geometry.partitioning.BSPTree@S> tree = this; tree.parent != null; tree = tree.parent) {
      if(tree == tree.parent.plus) {
        s = s.split(tree.parent.cut.getHyperplane()).getPlus();
      }
      else {
        s = s.split(tree.parent.cut.getHyperplane()).getMinus();
      }
    }
    return s;
  }
  public SubHyperplane<S> getCut() {
    return cut;
  }
  public boolean insertCut(final Hyperplane<S> hyperplane) {
    if(cut != null) {
      plus.parent = null;
      minus.parent = null;
    }
    final SubHyperplane<S> chopped = fitToCell(hyperplane.wholeHyperplane());
    if(chopped == null || chopped.isEmpty()) {
      cut = null;
      plus = null;
      minus = null;
      return false;
    }
    cut = chopped;
    plus = new BSPTree<S>();
    plus.parent = this;
    minus = new BSPTree<S>();
    minus.parent = this;
    return true;
  }
  private void chopOffMinus(final Hyperplane<S> hyperplane) {
    if(cut != null) {
      cut = cut.split(hyperplane).getPlus();
      plus.chopOffMinus(hyperplane);
      minus.chopOffMinus(hyperplane);
    }
  }
  private void chopOffPlus(final Hyperplane<S> hyperplane) {
    if(cut != null) {
      cut = cut.split(hyperplane).getMinus();
      plus.chopOffPlus(hyperplane);
      minus.chopOffPlus(hyperplane);
    }
  }
  private void condense() {
    if((cut != null) && (plus.cut == null) && (minus.cut == null) && (((plus.attribute == null) && (minus.attribute == null)) || ((plus.attribute != null) && plus.attribute.equals(minus.attribute)))) {
      attribute = (plus.attribute == null) ? minus.attribute : plus.attribute;
      cut = null;
      plus = null;
      minus = null;
    }
  }
  public void insertInTree(final BSPTree<S> parentTree, final boolean isPlusChild) {
    parent = parentTree;
    if(parentTree != null) {
      if(isPlusChild) {
        parentTree.plus = this;
      }
      else {
        parentTree.minus = this;
      }
    }
    if(cut != null) {
      for(org.apache.commons.math3.geometry.partitioning.BSPTree<org.apache.commons.math3.geometry.partitioning.BSPTree@S> tree = this; tree.parent != null; tree = tree.parent) {
        final Hyperplane<S> hyperplane = tree.parent.cut.getHyperplane();
        if(tree == tree.parent.plus) {
          SubHyperplane.SplitSubHyperplane<S> var_1732 = cut.split(hyperplane);
          cut = var_1732.getPlus();
          plus.chopOffMinus(hyperplane);
          minus.chopOffMinus(hyperplane);
        }
        else {
          cut = cut.split(hyperplane).getMinus();
          plus.chopOffPlus(hyperplane);
          minus.chopOffPlus(hyperplane);
        }
      }
      condense();
    }
  }
  public void setAttribute(final Object attribute) {
    this.attribute = attribute;
  }
  public void visit(final BSPTreeVisitor<S> visitor) {
    if(cut == null) {
      visitor.visitLeafNode(this);
    }
    else {
      switch (visitor.visitOrder(this)){
        case PLUS_MINUS_SUB:
        plus.visit(visitor);
        minus.visit(visitor);
        visitor.visitInternalNode(this);
        break ;
        case PLUS_SUB_MINUS:
        plus.visit(visitor);
        visitor.visitInternalNode(this);
        minus.visit(visitor);
        break ;
        case MINUS_PLUS_SUB:
        minus.visit(visitor);
        plus.visit(visitor);
        visitor.visitInternalNode(this);
        break ;
        case MINUS_SUB_PLUS:
        minus.visit(visitor);
        visitor.visitInternalNode(this);
        plus.visit(visitor);
        break ;
        case SUB_PLUS_MINUS:
        visitor.visitInternalNode(this);
        plus.visit(visitor);
        minus.visit(visitor);
        break ;
        case SUB_MINUS_PLUS:
        visitor.visitInternalNode(this);
        minus.visit(visitor);
        plus.visit(visitor);
        break ;
        default:
        throw new MathInternalError();
      }
    }
  }
  public interface LeafMerger<S extends org.apache.commons.math3.geometry.Space>  {
    BSPTree<S> merge(BSPTree<S> leaf, BSPTree<S> tree, BSPTree<S> parentTree, boolean isPlusChild, boolean leafFromInstance);
  }
}