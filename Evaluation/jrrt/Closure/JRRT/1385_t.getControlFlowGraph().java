package com.google.javascript.jscomp;
import com.google.common.base.Predicate;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.NodeTraversal.ScopedCallback;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.jscomp.graph.GraphReachability;
import com.google.javascript.jscomp.graph.GraphReachability.EdgeTuple;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.TernaryValue;

class CheckUnreachableCode implements ScopedCallback  {
  final static DiagnosticType UNREACHABLE_CODE = DiagnosticType.error("JSC_UNREACHABLE_CODE", "unreachable code");
  final private AbstractCompiler compiler;
  final private CheckLevel level;
  CheckUnreachableCode(AbstractCompiler compiler, CheckLevel level) {
    super();
    this.compiler = compiler;
    this.level = level;
  }
  @Override() public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
    ControlFlowGraph<Node> var_1385 = t.getControlFlowGraph();
    GraphNode<Node, Branch> gNode = var_1385.getNode(n);
    if(gNode != null && gNode.getAnnotation() != GraphReachability.REACHABLE) {
      if(n.getLineno() != -1 && !n.isEmpty() && !n.isBreak()) {
        compiler.report(t.makeError(n, level, UNREACHABLE_CODE));
        new GraphReachability<Node, ControlFlowGraph.Branch>(t.getControlFlowGraph()).recompute(n);
        return false;
      }
    }
    return true;
  }
  @Override() public void enterScope(NodeTraversal t) {
    initScope(t.getControlFlowGraph());
  }
  @Override() public void exitScope(NodeTraversal t) {
  }
  private void initScope(ControlFlowGraph<Node> controlFlowGraph) {
    new GraphReachability<Node, ControlFlowGraph.Branch>(controlFlowGraph, new ReachablePredicate()).compute(controlFlowGraph.getEntry().getValue());
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
  }
  
  final private class ReachablePredicate implements Predicate<EdgeTuple<Node, ControlFlowGraph.Branch>>  {
    @Override() public boolean apply(EdgeTuple<Node, Branch> input) {
      Branch branch = input.edge;
      if(!branch.isConditional()) {
        return true;
      }
      Node predecessor = input.sourceNode;
      Node condition = NodeUtil.getConditionExpression(predecessor);
      if(condition != null) {
        TernaryValue val = NodeUtil.getImpureBooleanValue(condition);
        if(val != TernaryValue.UNKNOWN) {
          return val.toBoolean(true) == (branch == Branch.ON_TRUE);
        }
      }
      return true;
    }
  }
}