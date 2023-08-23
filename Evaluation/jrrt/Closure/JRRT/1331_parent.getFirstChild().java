package com.google.javascript.jscomp;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.jscomp.graph.LinkedDirectedGraph;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Comparator;
class ControlFlowGraph<N extends java.lang.Object> extends LinkedDirectedGraph<N, ControlFlowGraph.Branch>  {
  final private DiGraphNode<N, ControlFlowGraph.Branch> implicitReturn;
  final private DiGraphNode<N, ControlFlowGraph.Branch> entry;
  ControlFlowGraph(N entry, boolean nodeAnnotations, boolean edgeAnnotations) {
    super(nodeAnnotations, edgeAnnotations);
    implicitReturn = createDirectedGraphNode(null);
    this.entry = createDirectedGraphNode(entry);
  }
  public Comparator<DiGraphNode<N, Branch>> getOptionalNodeComparator(boolean isForward) {
    return null;
  }
  public DiGraphNode<N, ControlFlowGraph.Branch> getEntry() {
    return entry;
  }
  public DiGraphNode<N, ControlFlowGraph.Branch> getImplicitReturn() {
    return implicitReturn;
  }
  public static boolean isEnteringNewCfgNode(Node n) {
    Node parent = n.getParent();
    switch (parent.getType()){
      case Token.BLOCK:
      case Token.SCRIPT:
      case Token.TRY:
      return true;
      case Token.FUNCTION:
      Node var_1331 = parent.getFirstChild();
      return n != var_1331.getNext();
      case Token.WHILE:
      case Token.DO:
      case Token.IF:
      return NodeUtil.getConditionExpression(parent) != n;
      case Token.FOR:
      if(NodeUtil.isForIn(parent)) {
        return n != parent.getFirstChild();
      }
      else {
        return NodeUtil.getConditionExpression(parent) != n;
      }
      case Token.SWITCH:
      case Token.CASE:
      case Token.CATCH:
      case Token.WITH:
      return n != parent.getFirstChild();
      default:
      return false;
    }
  }
  public boolean isImplicitReturn(DiGraphNode<N, ControlFlowGraph.Branch> node) {
    return node == implicitReturn;
  }
  public void connectToImplicitReturn(N srcValue, Branch edgeValue) {
    super.connect(srcValue, edgeValue, null);
  }
  
  abstract public static class AbstractCfgNodeTraversalCallback implements Callback  {
    @Override() final public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
      if(parent == null) {
        return true;
      }
      return !isEnteringNewCfgNode(n);
    }
  }
  public static enum Branch {
    ON_TRUE(),

    ON_FALSE(),

    UNCOND(),

    ON_EX(),

    SYN_BLOCK(),

  ;
    public boolean isConditional() {
      return this == ON_TRUE || this == ON_FALSE;
    }
  private Branch() {
  }
  }
}