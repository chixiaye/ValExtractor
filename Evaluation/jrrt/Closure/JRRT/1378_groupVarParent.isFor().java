package com.google.javascript.jscomp;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.ScopedCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.util.Iterator;
import java.util.Set;

class GroupVariableDeclarations implements CompilerPass, ScopedCallback  {
  final private AbstractCompiler compiler;
  GroupVariableDeclarations(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
  }
  @Override() public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
    return true;
  }
  private void applyGroupingToVar(Node firstVar, Node groupVar) {
    Node child = groupVar.getFirstChild();
    Node initializedName = null;
    while(child != null){
      if(child.hasChildren()) {
        if(initializedName != null) {
          return ;
        }
        initializedName = child;
      }
      child = child.getNext();
    }
    Node groupVarParent = groupVar.getParent();
    if(initializedName != null) {
      if(NodeUtil.isForIn(groupVarParent)) {
        return ;
      }
      Node clone = initializedName.cloneNode();
      groupVar.replaceChild(initializedName, clone);
      Node initializedVal = initializedName.removeFirstChild();
      Node assignmentNode = IR.assign(initializedName, initializedVal);
      boolean var_1378 = groupVarParent.isFor();
      if(var_1378) {
        groupVarParent.replaceChild(groupVar, assignmentNode);
      }
      else {
        Node exprNode = NodeUtil.newExpr(assignmentNode);
        groupVarParent.replaceChild(groupVar, exprNode);
      }
    }
    else {
      if(groupVarParent.isFor()) {
        if(NodeUtil.isForIn(groupVarParent)) {
          Node nameNodeClone = groupVar.getFirstChild().cloneNode();
          groupVarParent.replaceChild(groupVar, nameNodeClone);
        }
        else {
          Node emptyNode = IR.empty();
          groupVarParent.replaceChild(groupVar, emptyNode);
        }
      }
      else {
        groupVarParent.removeChild(groupVar);
      }
    }
    Node children = groupVar.removeChildren();
    firstVar.addChildrenToBack(children);
    compiler.reportCodeChange();
  }
  @Override() public void enterScope(NodeTraversal t) {
    Set<Node> varNodes = Sets.newLinkedHashSet();
    Iterator<Var> scopeVarIter = t.getScope().getVars();
    while(scopeVarIter.hasNext()){
      Node parentNode = scopeVarIter.next().getParentNode();
      if(parentNode.isVar()) {
        varNodes.add(parentNode);
      }
    }
    if(varNodes.size() <= 1) {
      return ;
    }
    Iterator<Node> varNodeIter = varNodes.iterator();
    Node firstVarNode = varNodeIter.next();
    while(varNodeIter.hasNext()){
      Node varNode = varNodeIter.next();
      applyGroupingToVar(firstVarNode, varNode);
    }
  }
  @Override() public void exitScope(NodeTraversal t) {
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, this);
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
  }
}