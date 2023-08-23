package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.Node;

class CollapseAnonymousFunctions implements CompilerPass  {
  final private AbstractCompiler compiler;
  public CollapseAnonymousFunctions(AbstractCompiler compiler) {
    super();
    Preconditions.checkArgument(compiler.getLifeCycleStage().isNormalized());
    this.compiler = compiler;
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, new Callback());
  }
  
  private class Callback extends AbstractPostOrderCallback  {
    private boolean containsName(Node n, String name) {
      if(n.isName() && n.getString().equals(name)) {
        return true;
      }
      for (Node child : n.children()) {
        if(containsName(child, name)) {
          return true;
        }
      }
      return false;
    }
    private boolean isRecursiveFunction(Node function) {
      Node name = function.getFirstChild();
      String var_934 = name.getString();
      if(var_934.isEmpty()) {
        return false;
      }
      Node args = name.getNext();
      Node body = args.getNext();
      return containsName(body, name.getString());
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isVar()) {
        return ;
      }
      Node grandparent = parent.getParent();
      if(!(parent.isScript() || grandparent != null && grandparent.isFunction() && parent.isBlock())) {
        return ;
      }
      Preconditions.checkState(n.hasOneChild());
      Node name = n.getFirstChild();
      Node value = name.getFirstChild();
      if(value != null && value.isFunction() && !isRecursiveFunction(value)) {
        Node fnName = value.getFirstChild();
        fnName.setString(name.getString());
        NodeUtil.copyNameAnnotations(name, fnName);
        name.removeChild(value);
        parent.replaceChild(n, value);
        if(!t.inGlobalScope() && NodeUtil.isHoistedFunctionDeclaration(value)) {
          parent.addChildToFront(value.detachFromParent());
        }
        compiler.reportCodeChange();
      }
    }
  }
}