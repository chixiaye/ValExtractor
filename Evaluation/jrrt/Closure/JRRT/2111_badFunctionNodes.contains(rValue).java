package com.google.javascript.jscomp;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.DefinitionsRemover.Definition;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.NodeTraversal.ScopedCallback;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.rhino.Node;
import java.util.Collection;
import java.util.List;
import java.util.Set;

class ChainCalls implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private Set<Node> badFunctionNodes = Sets.newHashSet();
  final private Set<Node> goodFunctionNodes = Sets.newHashSet();
  final private List<CallSite> callSites = Lists.newArrayList();
  private SimpleDefinitionFinder defFinder;
  private GatherFunctions gatherFunctions = new GatherFunctions();
  ChainCalls(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
  }
  @Override() public void process(Node externs, Node root) {
    defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);
    NodeTraversal.traverse(compiler, root, new GatherCallSites());
    for (CallSite callSite : callSites) {
      callSite.parent.removeChild(callSite.n);
      callSite.n.removeChild(callSite.callNode);
      callSite.nextGetPropNode.replaceChild(callSite.nextGetPropFirstChildNode, callSite.callNode);
      compiler.reportCodeChange();
    }
  }
  
  private static class CallSite  {
    final Node parent;
    final Node n;
    final Node callNode;
    final Node nextGetPropNode;
    final Node nextGetPropFirstChildNode;
    CallSite(Node parent, Node n, Node callNode, Node nextGetPropNode, Node nextGetPropFirstChildNode) {
      super();
      this.parent = parent;
      this.n = n;
      this.callNode = callNode;
      this.nextGetPropNode = nextGetPropNode;
      this.nextGetPropFirstChildNode = nextGetPropFirstChildNode;
    }
  }
  
  private class GatherCallSites extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isExprResult()) {
        return ;
      }
      Node callNode = n.getFirstChild();
      if(!callNode.isCall()) {
        return ;
      }
      Node getPropNode = callNode.getFirstChild();
      if(!getPropNode.isGetProp()) {
        return ;
      }
      Node getPropFirstChildNode = getPropNode.getFirstChild();
      Collection<Definition> definitions = defFinder.getDefinitionsReferencedAt(getPropNode);
      if(definitions == null) {
        return ;
      }
      for (Definition definition : definitions) {
        Node rValue = definition.getRValue();
        if(rValue == null) {
          return ;
        }
        boolean var_2111 = badFunctionNodes.contains(rValue);
        if(var_2111) {
          return ;
        }
        if(!goodFunctionNodes.contains(rValue)) {
          NodeTraversal.traverse(compiler, rValue, gatherFunctions);
          if(badFunctionNodes.contains(rValue)) {
            return ;
          }
        }
      }
      Node nextNode = n.getNext();
      if(nextNode == null || !nextNode.isExprResult()) {
        return ;
      }
      Node nextCallNode = nextNode.getFirstChild();
      if(!nextCallNode.isCall()) {
        return ;
      }
      Node nextGetPropNode = nextCallNode.getFirstChild();
      if(!nextGetPropNode.isGetProp()) {
        return ;
      }
      Node nextGetPropFirstChildNode = nextGetPropNode.getFirstChild();
      if(!compiler.areNodesEqualForInlining(nextGetPropFirstChildNode, getPropFirstChildNode)) {
        return ;
      }
      if(NodeUtil.mayEffectMutableState(getPropFirstChildNode)) {
        return ;
      }
      callSites.add(new CallSite(parent, n, callNode, nextGetPropNode, nextGetPropFirstChildNode));
    }
  }
  
  private class GatherFunctions implements ScopedCallback  {
    @Override() public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
      return true;
    }
    @Override() public void enterScope(NodeTraversal t) {
      ControlFlowGraph<Node> cfg = t.getControlFlowGraph();
      for (DiGraphEdge<Node, Branch> s : cfg.getImplicitReturn().getInEdges()) {
        Node exitNode = s.getSource().getValue();
        if(!exitNode.isReturn() || exitNode.getFirstChild() == null || !exitNode.getFirstChild().isThis()) {
          badFunctionNodes.add(t.getScopeRoot());
          return ;
        }
      }
      goodFunctionNodes.add(t.getScopeRoot());
    }
    @Override() public void exitScope(NodeTraversal t) {
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    }
  }
}