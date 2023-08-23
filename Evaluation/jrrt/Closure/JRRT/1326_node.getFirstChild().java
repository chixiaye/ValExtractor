package com.google.javascript.jscomp;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.TokenStream;

class NodeNameExtractor  {
  final private char delimiter;
  private int nextUniqueInt = 0;
  NodeNameExtractor(char delimiter) {
    super();
    this.delimiter = delimiter;
  }
  String getName(Node node) {
    switch (node.getType()){
      case Token.FUNCTION:
      Node functionNameNode = node.getFirstChild();
      return functionNameNode.getString();
      case Token.GETPROP:
      Node var_1326 = node.getFirstChild();
      Node lhsOfDot = var_1326;
      Node rhsOfDot = lhsOfDot.getNext();
      String lhsOfDotName = getName(lhsOfDot);
      String rhsOfDotName = getName(rhsOfDot);
      if("prototype".equals(rhsOfDotName)) {
        return lhsOfDotName + delimiter;
      }
      else {
        return lhsOfDotName + delimiter + rhsOfDotName;
      }
      case Token.GETELEM:
      Node outsideBrackets = node.getFirstChild();
      Node insideBrackets = outsideBrackets.getNext();
      String nameOutsideBrackets = getName(outsideBrackets);
      String nameInsideBrackets = getName(insideBrackets);
      if("prototype".equals(nameInsideBrackets)) {
        return nameOutsideBrackets + delimiter;
      }
      else {
        return nameOutsideBrackets + delimiter + nameInsideBrackets;
      }
      case Token.NAME:
      return node.getString();
      case Token.STRING:
      case Token.STRING_KEY:
      return TokenStream.isJSIdentifier(node.getString()) ? node.getString() : ("__" + nextUniqueInt++);
      case Token.NUMBER:
      return NodeUtil.getStringValue(node);
      case Token.THIS:
      return "this";
      case Token.CALL:
      return getName(node.getFirstChild());
      default:
      StringBuilder sb = new StringBuilder();
      for(com.google.javascript.rhino.Node child = node.getFirstChild(); child != null; child = child.getNext()) {
        if(sb.length() > 0) {
          sb.append(delimiter);
        }
        sb.append(getName(child));
      }
      return sb.toString();
    }
  }
}