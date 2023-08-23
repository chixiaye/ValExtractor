package com.google.javascript.jscomp;
import com.google.javascript.rhino.Node;

class ReorderConstantExpression extends AbstractPeepholeOptimization  {
  @Override() Node optimizeSubtree(Node subtree) {
    if(NodeUtil.isSymmetricOperation(subtree) || NodeUtil.isRelationalOperation(subtree)) {
      Node var_1643 = subtree.getLastChild();
      if(NodeUtil.isImmutableValue(var_1643) && !NodeUtil.isImmutableValue(subtree.getFirstChild())) {
        if(NodeUtil.isRelationalOperation(subtree)) {
          int inverseOperator = NodeUtil.getInverseOperator(subtree.getType());
          subtree.setType(inverseOperator);
        }
        Node firstNode = subtree.getFirstChild().detachFromParent();
        Node lastNode = subtree.getLastChild().detachFromParent();
        subtree.addChildrenToFront(lastNode);
        subtree.addChildrenToBack(firstNode);
        reportCodeChange();
      }
    }
    return subtree;
  }
}