package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

class PrepareAst implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private boolean checkOnly;
  PrepareAst(AbstractCompiler compiler) {
    this(compiler, false);
  }
  PrepareAst(AbstractCompiler compiler, boolean checkOnly) {
    super();
    this.compiler = compiler;
    this.checkOnly = checkOnly;
  }
  private void normalizeBlocks(Node n) {
    if(NodeUtil.isControlStructure(n) && !n.isLabel() && !n.isSwitch()) {
      for(com.google.javascript.rhino.Node c = n.getFirstChild(); c != null; c = c.getNext()) {
        if(NodeUtil.isControlStructureCodeBlock(n, c) && !c.isBlock()) {
          Node newBlock = IR.block().srcref(n);
          n.replaceChild(c, newBlock);
          if(!c.isEmpty()) {
            newBlock.addChildrenToFront(c);
          }
          else {
            newBlock.setWasEmptyNode(true);
          }
          c = newBlock;
          reportChange();
        }
      }
    }
  }
  private void normalizeNodeTypes(Node n) {
    normalizeBlocks(n);
    for(com.google.javascript.rhino.Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Preconditions.checkState(child.getParent() == n);
      normalizeNodeTypes(child);
    }
  }
  @Override() public void process(Node externs, Node root) {
    if(checkOnly) {
      normalizeNodeTypes(root);
    }
    else {
      if(externs != null) {
        NodeTraversal.traverse(compiler, externs, new PrepareAnnotations());
      }
      if(root != null) {
        NodeTraversal.traverse(compiler, root, new PrepareAnnotations());
      }
    }
  }
  private void reportChange() {
    if(checkOnly) {
      Preconditions.checkState(false, "normalizeNodeType constraints violated");
    }
  }
  
  static class PrepareAnnotations implements NodeTraversal.Callback  {
    PrepareAnnotations() {
      super();
    }
    @Override() public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      if(n.isObjectLit()) {
        normalizeObjectLiteralAnnotations(n);
      }
      return true;
    }
    private void annotateCalls(Node n) {
      Preconditions.checkState(n.isCall());
      Node first = n.getFirstChild();
      if(!NodeUtil.isGet(first)) {
        n.putBooleanProp(Node.FREE_CALL, true);
      }
      if(first.isName() && "eval".equals(first.getString())) {
        first.putBooleanProp(Node.DIRECT_EVAL, true);
      }
    }
    private void annotateDispatchers(Node n, Node parent) {
      Preconditions.checkState(n.isFunction());
      if(parent.getJSDocInfo() != null && parent.getJSDocInfo().isJavaDispatch()) {
        if(parent.isAssign()) {
          Preconditions.checkState(parent.getLastChild() == n);
          n.putBooleanProp(Node.IS_DISPATCHER, true);
        }
      }
    }
    private void normalizeObjectLiteralAnnotations(Node objlit) {
      Preconditions.checkState(objlit.isObjectLit());
      for(com.google.javascript.rhino.Node key = objlit.getFirstChild(); key != null; key = key.getNext()) {
        Node value = key.getFirstChild();
        normalizeObjectLiteralKeyAnnotations(objlit, key, value);
      }
    }
    private void normalizeObjectLiteralKeyAnnotations(Node objlit, Node key, Node value) {
      Preconditions.checkState(objlit.isObjectLit());
      com.google.javascript.rhino.JSDocInfo var_1640 = key.getJSDocInfo();
      if(var_1640 != null && value.isFunction()) {
        value.setJSDocInfo(key.getJSDocInfo());
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.CALL:
        annotateCalls(n);
        break ;
        case Token.FUNCTION:
        annotateDispatchers(n, parent);
        break ;
      }
    }
  }
}