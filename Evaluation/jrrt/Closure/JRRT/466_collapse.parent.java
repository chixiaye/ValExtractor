package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.List;
import java.util.Set;

class CollapseVariableDeclarations implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private List<Collapse> collapses = Lists.newArrayList();
  final private Set<Node> nodesToCollapse = Sets.newHashSet();
  CollapseVariableDeclarations(AbstractCompiler compiler) {
    super();
    Preconditions.checkState(!compiler.getLifeCycleStage().isNormalized());
    this.compiler = compiler;
  }
  private boolean isNamedParameter(Var v) {
    return v.getParentNode().isParamList();
  }
  private void applyCollapses() {
    for (Collapse collapse : collapses) {
      Node var = new Node(Token.VAR);
      var.copyInformationFrom(collapse.startNode);
      Node var_466 = collapse.parent;
      var_466.addChildBefore(var, collapse.startNode);
      boolean redeclaration = false;
      for(com.google.javascript.rhino.Node n = collapse.startNode; n != collapse.endNode; ) {
        Node next = n.getNext();
        Preconditions.checkState(var.getNext() == n);
        collapse.parent.removeChildAfter(var);
        if(n.isVar()) {
          while(n.hasChildren()){
            var.addChildToBack(n.removeFirstChild());
          }
        }
        else {
          Node assign = n.getFirstChild();
          Node lhs = assign.getFirstChild();
          Preconditions.checkState(lhs.isName());
          Node rhs = assign.getLastChild();
          lhs.addChildToBack(rhs.detachFromParent());
          var.addChildToBack(lhs.detachFromParent());
          redeclaration = true;
        }
        n = next;
      }
      if(redeclaration) {
        JSDocInfo info = new JSDocInfo();
        info.addSuppression("duplicate");
        var.setJSDocInfo(info);
      }
    }
  }
  @Override() public void process(Node externs, Node root) {
    collapses.clear();
    nodesToCollapse.clear();
    NodeTraversal.traverse(compiler, root, new GatherCollapses());
    if(!collapses.isEmpty()) {
      applyCollapses();
      compiler.reportCodeChange();
    }
  }
  
  private static class Collapse  {
    final Node startNode;
    final Node endNode;
    final Node parent;
    Collapse(Node startNode, Node endNode, Node parent) {
      super();
      this.startNode = startNode;
      this.endNode = endNode;
      this.parent = parent;
    }
  }
  
  private class GatherCollapses extends AbstractPostOrderCallback  {
    final private Set<Var> blacklistedVars = Sets.newHashSet();
    private boolean canBeRedeclared(Node n, Scope s) {
      if(!NodeUtil.isExprAssign(n)) {
        return false;
      }
      Node assign = n.getFirstChild();
      Node lhs = assign.getFirstChild();
      if(!lhs.isName()) {
        return false;
      }
      Var var = s.getVar(lhs.getString());
      return var != null && var.getScope() == s && !isNamedParameter(var) && !blacklistedVars.contains(var);
    }
    private void blacklistStubVars(NodeTraversal t, Node varNode) {
      for(com.google.javascript.rhino.Node child = varNode.getFirstChild(); child != null; child = child.getNext()) {
        if(child.getFirstChild() == null) {
          blacklistedVars.add(t.getScope().getVar(child.getString()));
        }
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isVar()) {
        blacklistStubVars(t, n);
      }
      if(!n.isVar() && !canBeRedeclared(n, t.getScope())) 
        return ;
      if(nodesToCollapse.contains(n)) 
        return ;
      if(parent.isIf()) 
        return ;
      Node varNode = n;
      boolean hasVar = n.isVar();
      n = n.getNext();
      boolean hasNodesToCollapse = false;
      while(n != null && (n.isVar() || canBeRedeclared(n, t.getScope()))){
        if(n.isVar()) {
          blacklistStubVars(t, n);
          hasVar = true;
        }
        nodesToCollapse.add(n);
        hasNodesToCollapse = true;
        n = n.getNext();
      }
      if(hasNodesToCollapse && hasVar) {
        nodesToCollapse.add(varNode);
        collapses.add(new Collapse(varNode, n, parent));
      }
    }
  }
}