package com.google.javascript.jscomp;
import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.NodeTraversal.AbstractShallowStatementCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class RescopeGlobalSymbols implements CompilerPass  {
  final private static String DISAMBIGUATION_SUFFIX = "$";
  final private static String WINDOW = "window";
  final private static Set<String> SPECIAL_EXTERNS = ImmutableSet.of(WINDOW, "eval", "arguments");
  final private AbstractCompiler compiler;
  final private String globalSymbolNamespace;
  final private boolean addExtern;
  RescopeGlobalSymbols(AbstractCompiler compiler, String globalSymbolNamespace) {
    this(compiler, globalSymbolNamespace, true);
  }
  RescopeGlobalSymbols(AbstractCompiler compiler, String globalSymbolNamespace, boolean addExtern) {
    super();
    this.compiler = compiler;
    this.globalSymbolNamespace = globalSymbolNamespace;
    this.addExtern = addExtern;
  }
  private void addExternForGlobalSymbolNamespace() {
    Node varNode = IR.var(IR.name(globalSymbolNamespace));
    CompilerInput input = compiler.newExternInput("{RescopeGlobalSymbolsNamespaceVar}");
    input.getAstRoot(compiler).addChildrenToBack(varNode);
    compiler.reportCodeChange();
  }
  @Override() public void process(Node externs, Node root) {
    if(addExtern) {
      addExternForGlobalSymbolNamespace();
    }
    NodeTraversal.traverse(compiler, root, new RewriteGlobalFunctionStatementsToVarAssignmentsCallback());
    NodeTraversal.traverse(compiler, root, new RewriteScopeCallback());
    NodeTraversal.traverse(compiler, root, new RemoveGlobalVarCallback());
    NodeTraversal.traverse(compiler, root, new MakeExternsReferenceWindowExplicitly());
  }
  
  private class MakeExternsReferenceWindowExplicitly extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isName()) {
        return ;
      }
      String name = n.getString();
      Scope.Var var = t.getScope().getVar(name);
      if(name.length() > 0 && (var == null || var.isExtern()) && !globalSymbolNamespace.equals(name) && !SPECIAL_EXTERNS.contains(name)) {
        parent.replaceChild(n, IR.getprop(IR.name(WINDOW), IR.string(name)).srcrefTree(n));
        compiler.reportCodeChange();
      }
    }
  }
  
  private class RemoveGlobalVarCallback extends AbstractShallowStatementCallback  {
    private Node joinOnComma(List<Node> commas, Node source) {
      Node comma = commas.get(0);
      for(int i = 1; i < commas.size(); i++) {
        Node nextComma = IR.comma(comma, commas.get(i));
        nextComma.copyInformationFrom(source);
        comma = nextComma;
      }
      return comma;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isVar()) {
        return ;
      }
      List<Node> commas = new ArrayList<Node>();
      List<Node> interestingChildren = new ArrayList<Node>();
      for (Node c : n.children()) {
        if(c.isAssign() || parent.isFor()) {
          interestingChildren.add(c);
        }
      }
      for (Node c : interestingChildren) {
        if(parent.isFor() && parent.getFirstChild() == n) {
          Node var_828 = c.cloneTree();
          commas.add(var_828);
        }
        else {
          Node expr = IR.exprResult(c.cloneTree()).srcref(c);
          parent.addChildBefore(expr, n);
        }
      }
      if(commas.size() > 0) {
        Node comma = joinOnComma(commas, n);
        parent.addChildBefore(comma, n);
      }
      parent.removeChild(n);
      compiler.reportCodeChange();
    }
  }
  
  private class RewriteGlobalFunctionStatementsToVarAssignmentsCallback extends AbstractShallowStatementCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(NodeUtil.isFunctionDeclaration(n)) {
        String name = NodeUtil.getFunctionName(n);
        n.getFirstChild().setString("");
        Node prev = parent.getChildBefore(n);
        n.detachFromParent();
        Node var = NodeUtil.newVarNode(name, n);
        if(prev == null) {
          parent.addChildToFront(var);
        }
        else {
          parent.addChildAfter(var, prev);
        }
        compiler.reportCodeChange();
      }
    }
  }
  
  private class RewriteScopeCallback extends AbstractPostOrderCallback  {
    private void replaceSymbol(Node node, String name) {
      Node parent = node.getParent();
      Node replacement = IR.getprop(IR.name(globalSymbolNamespace).srcref(node), IR.string(name).srcref(node));
      replacement.srcref(node);
      if(node.hasChildren()) {
        Node assign = IR.assign(replacement, node.removeFirstChild());
        parent.replaceChild(node, assign);
      }
      else {
        parent.replaceChild(node, replacement);
      }
      compiler.reportCodeChange();
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isName()) {
        return ;
      }
      String name = n.getString();
      if(parent.isFunction() && name.length() == 0) {
        return ;
      }
      Scope.Var var = t.getScope().getVar(name);
      if(var == null) {
        return ;
      }
      if(var.isExtern()) {
        return ;
      }
      if(!var.isExtern() && (name.equals(globalSymbolNamespace) || name.indexOf(globalSymbolNamespace + DISAMBIGUATION_SUFFIX) == 0)) {
        n.setString(name + DISAMBIGUATION_SUFFIX);
        compiler.reportCodeChange();
      }
      if(!var.isGlobal()) {
        return ;
      }
      Node nameNode = var.getNameNode();
      if(nameNode != null && nameNode.getParent() != null && nameNode.getParent().isCatch()) {
        return ;
      }
      replaceSymbol(n, name);
    }
  }
}