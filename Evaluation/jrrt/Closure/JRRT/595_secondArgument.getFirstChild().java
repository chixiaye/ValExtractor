package com.google.javascript.jscomp;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

class ObjectPropertyStringPostprocess implements CompilerPass  {
  final private AbstractCompiler compiler;
  public ObjectPropertyStringPostprocess(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, new Callback());
  }
  
  private class Callback extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isNew()) {
        return ;
      }
      Node objectName = n.getFirstChild();
      if(!ObjectPropertyStringPreprocess.EXTERN_OBJECT_PROPERTY_STRING.equals(objectName.getQualifiedName())) {
        return ;
      }
      Node firstArgument = objectName.getNext();
      Node secondArgument = firstArgument.getNext();
      int secondArgumentType = secondArgument.getType();
      if(secondArgumentType == Token.GETPROP) {
        Node newChild = secondArgument.getFirstChild();
        secondArgument.removeChild(newChild);
        n.replaceChild(firstArgument, newChild);
        Node var_595 = secondArgument.getFirstChild();
        n.replaceChild(secondArgument, IR.string(var_595.getString()));
      }
      else 
        if(secondArgumentType == Token.GETELEM) {
          Node newFirstArgument = secondArgument.getFirstChild();
          secondArgument.removeChild(newFirstArgument);
          Node newSecondArgument = secondArgument.getLastChild();
          secondArgument.removeChild(newSecondArgument);
          n.replaceChild(firstArgument, newFirstArgument);
          n.replaceChild(secondArgument, newSecondArgument);
        }
        else {
          n.replaceChild(secondArgument, IR.string(secondArgument.getString()));
        }
      compiler.reportCodeChange();
    }
  }
}