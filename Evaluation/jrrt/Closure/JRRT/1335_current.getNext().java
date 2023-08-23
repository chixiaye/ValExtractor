package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

class NodeIterators  {
  private NodeIterators() {
    super();
  }
  
  static class FunctionlessLocalScope implements Iterator<Node>  {
    final private Stack<Node> ancestors = new Stack<Node>();
    FunctionlessLocalScope(Node ... ancestors) {
      super();
      Preconditions.checkArgument(ancestors.length > 0);
      for (Node n : ancestors) {
        if(n.isFunction()) {
          break ;
        }
        this.ancestors.add(0, n);
      }
    }
    List<Node> currentAncestors() {
      List<Node> list = Lists.newArrayList(ancestors);
      Collections.reverse(list);
      return list;
    }
    protected Node current() {
      return ancestors.peek();
    }
    protected Node currentParent() {
      return ancestors.size() >= 2 ? ancestors.get(ancestors.size() - 2) : null;
    }
    @Override() public Node next() {
      Node current = ancestors.pop();
      Node var_1335 = current.getNext();
      if(var_1335 == null) {
        current = ancestors.peek();
        if(current.isFunction()) {
          return next();
        }
      }
      else {
        current = current.getNext();
        ancestors.push(current);
        if(current.isFunction()) {
          return next();
        }
        while(current.hasChildren()){
          current = current.getFirstChild();
          ancestors.push(current);
          if(current.isFunction()) {
            return next();
          }
        }
      }
      return current;
    }
    @Override() public boolean hasNext() {
      return !(ancestors.size() == 1 && ancestors.peek().getNext() == null);
    }
    @Override() public void remove() {
      throw new UnsupportedOperationException("Not implemented");
    }
  }
  
  static class LocalVarMotion implements Iterator<Node>  {
    final private boolean valueHasSideEffects;
    final private FunctionlessLocalScope iterator;
    final private String varName;
    private Node lookAhead;
    private LocalVarMotion(Node nameNode, FunctionlessLocalScope iterator) {
      super();
      Preconditions.checkArgument(nameNode.isName());
      Node valueNode = NodeUtil.getAssignedValue(nameNode);
      this.varName = nameNode.getString();
      this.valueHasSideEffects = valueNode != null && NodeUtil.mayHaveSideEffects(valueNode);
      this.iterator = iterator;
      advanceLookAhead(true);
    }
    static LocalVarMotion forAssign(Node name, Node assign, Node expr, Node block) {
      Preconditions.checkArgument(assign.isAssign());
      Preconditions.checkArgument(expr.isExprResult());
      return new LocalVarMotion(name, new FunctionlessLocalScope(assign, expr, block));
    }
    static LocalVarMotion forVar(Node name, Node var, Node block) {
      Preconditions.checkArgument(var.isVar());
      Preconditions.checkArgument(NodeUtil.isStatement(var));
      return new LocalVarMotion(name, new FunctionlessLocalScope(name, var, block));
    }
    @Override() public Node next() {
      Node next = lookAhead;
      advanceLookAhead(false);
      return next;
    }
    @Override() public boolean hasNext() {
      return lookAhead != null;
    }
    private void advanceLookAhead(boolean atStart) {
      if(!atStart) {
        if(lookAhead == null) {
          return ;
        }
        Node curNode = iterator.current();
        if(curNode.isName() && varName.equals(curNode.getString())) {
          lookAhead = null;
          return ;
        }
      }
      if(!iterator.hasNext()) {
        lookAhead = null;
        return ;
      }
      Node nextNode = iterator.next();
      Node nextParent = iterator.currentParent();
      int type = nextNode.getType();
      if(valueHasSideEffects) {
        boolean readsState = false;
        if((nextNode.isName() && !varName.equals(nextNode.getString())) || (nextNode.isGetProp() || nextNode.isGetElem())) {
          if(nextParent == null || !NodeUtil.isVarOrSimpleAssignLhs(nextNode, nextParent)) {
            readsState = true;
          }
        }
        else 
          if(nextNode.isCall() || nextNode.isNew()) {
            readsState = true;
          }
        if(readsState) {
          lookAhead = null;
          return ;
        }
      }
      if(NodeUtil.nodeTypeMayHaveSideEffects(nextNode) && type != Token.NAME || type == Token.NAME && nextParent.isCatch()) {
        lookAhead = null;
        return ;
      }
      lookAhead = nextNode;
    }
    @Override() public void remove() {
      throw new UnsupportedOperationException("Not implemented");
    }
  }
}