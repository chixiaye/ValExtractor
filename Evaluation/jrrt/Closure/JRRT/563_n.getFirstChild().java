package com.google.javascript.jscomp;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

class AnonymousFunctionNamingCallback extends AbstractPostOrderCallback  {
  final private FunctionNamer namer;
  AnonymousFunctionNamingCallback(FunctionNamer namer) {
    super();
    this.namer = namer;
  }
  private void nameObjectLiteralMethods(Node objectLiteral, String context) {
    for(com.google.javascript.rhino.Node keyNode = objectLiteral.getFirstChild(); keyNode != null; keyNode = keyNode.getNext()) {
      Node valueNode = keyNode.getFirstChild();
      if(keyNode.isStringKey()) {
        String name = namer.getCombinedName(context, namer.getName(keyNode));
        int type = valueNode.getType();
        if(type == Token.FUNCTION) {
          Node functionNameNode = valueNode.getFirstChild();
          String functionName = functionNameNode.getString();
          if(functionName.isEmpty()) {
            namer.setFunctionName(name, valueNode);
          }
        }
        else 
          if(type == Token.OBJECTLIT) {
            nameObjectLiteralMethods(valueNode, name);
          }
      }
    }
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()){
      case Token.FUNCTION:
      Node var_563 = n.getFirstChild();
      Node functionNameNode = var_563;
      String functionName = functionNameNode.getString();
      if(functionName.length() == 0) {
        if(parent.isAssign()) {
          Node lhs = parent.getFirstChild();
          String name = namer.getName(lhs);
          namer.setFunctionName(name, n);
        }
        else 
          if(parent.isName()) {
            String name = namer.getName(parent);
            namer.setFunctionName(name, n);
          }
      }
      break ;
      case Token.ASSIGN:
      Node lhs = n.getFirstChild();
      Node rhs = lhs.getNext();
      if(rhs.isObjectLit()) {
        nameObjectLiteralMethods(rhs, namer.getName(lhs));
      }
    }
  }
  
  interface FunctionNamer  {
    String getCombinedName(String lhs, String rhs);
    String getName(Node node);
    void setFunctionName(String name, Node fnNode);
  }
}