package com.google.javascript.jscomp;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.Node;
import java.util.List;
import java.util.Set;

final class ClosureCodeRemoval implements CompilerPass  {
  final private AbstractCompiler compiler;
  final static String ABSTRACT_METHOD_NAME = "goog.abstractMethod";
  final private boolean removeAbstractMethods;
  final private boolean removeAssertionCalls;
  final private List<RemovableAssignment> abstractMethodAssignmentNodes = Lists.newArrayList();
  final private List<Node> assertionCalls = Lists.newArrayList();
  ClosureCodeRemoval(AbstractCompiler compiler, boolean removeAbstractMethods, boolean removeAssertionCalls) {
    super();
    this.compiler = compiler;
    this.removeAbstractMethods = removeAbstractMethods;
    this.removeAssertionCalls = removeAssertionCalls;
  }
  @Override() public void process(Node externs, Node root) {
    List<Callback> passes = Lists.newArrayList();
    if(removeAbstractMethods) {
      passes.add(new FindAbstractMethods());
    }
    if(removeAssertionCalls) {
      passes.add(new FindAssertionCalls());
    }
    CombinedCompilerPass.traverse(compiler, root, passes);
    for (RemovableAssignment assignment : abstractMethodAssignmentNodes) {
      assignment.remove();
    }
    for (Node call : assertionCalls) {
      Node parent = call.getParent();
      if(parent.isExprResult()) {
        parent.getParent().removeChild(parent);
      }
      else {
        Node firstArg = call.getFirstChild().getNext();
        if(firstArg == null) {
          parent.replaceChild(call, NodeUtil.newUndefinedNode(call));
        }
        else {
          parent.replaceChild(call, firstArg.detachFromParent());
        }
      }
      compiler.reportCodeChange();
    }
  }
  
  private class FindAbstractMethods extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isAssign()) {
        Node var_1953 = n.getFirstChild();
        Node nameNode = var_1953;
        Node valueNode = n.getLastChild();
        if(nameNode.isQualifiedName() && valueNode.isQualifiedName() && ABSTRACT_METHOD_NAME.equals(valueNode.getQualifiedName())) {
          abstractMethodAssignmentNodes.add(new RemovableAssignment(n.getFirstChild(), n, t));
        }
      }
    }
  }
  
  private class FindAssertionCalls extends AbstractPostOrderCallback  {
    Set<String> assertionNames = Sets.newHashSet();
    FindAssertionCalls() {
      super();
      for (AssertionFunctionSpec spec : compiler.getCodingConvention().getAssertionFunctions()) {
        assertionNames.add(spec.getFunctionName());
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isCall()) {
        String fnName = n.getFirstChild().getQualifiedName();
        if(assertionNames.contains(fnName)) {
          assertionCalls.add(n);
        }
      }
    }
  }
  
  private class RemovableAssignment  {
    final Node node;
    final Node parent;
    final List<Node> assignAncestors = Lists.newArrayList();
    final Node lastAncestor;
    public RemovableAssignment(Node nameNode, Node assignNode, NodeTraversal traversal) {
      super();
      this.node = nameNode;
      this.parent = assignNode;
      Node ancestor = assignNode;
      do {
        ancestor = ancestor.getParent();
        assignAncestors.add(ancestor);
      }while(ancestor.isAssign() && ancestor.getFirstChild().isQualifiedName());
      lastAncestor = ancestor.getParent();
    }
    public void remove() {
      Node rhs = node.getNext();
      Node last = parent;
      for (Node ancestor : assignAncestors) {
        if(ancestor.isExprResult()) {
          lastAncestor.removeChild(ancestor);
        }
        else {
          rhs.detachFromParent();
          ancestor.replaceChild(last, rhs);
        }
        last = ancestor;
      }
      compiler.reportCodeChange();
    }
  }
}