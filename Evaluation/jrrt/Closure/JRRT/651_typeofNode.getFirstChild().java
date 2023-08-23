package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;

class PeepholeFoldWithTypes extends AbstractPeepholeOptimization  {
  @Override() Node optimizeSubtree(Node subtree) {
    switch (subtree.getType()){
      case Token.TYPEOF:
      return tryFoldTypeof(subtree);
      default:
      return subtree;
    }
  }
  private Node tryFoldTypeof(Node typeofNode) {
    Preconditions.checkArgument(typeofNode.isTypeOf());
    Node var_651 = typeofNode.getFirstChild();
    Preconditions.checkArgument(var_651 != null);
    Node argumentNode = typeofNode.getFirstChild();
    if(!NodeUtil.isLiteralValue(argumentNode, true) && !mayHaveSideEffects(argumentNode)) {
      JSType argumentType = argumentNode.getJSType();
      String typeName = null;
      if(argumentType != null) {
        if(argumentType.isObject() || argumentType.isNullType()) {
          typeName = "object";
        }
        else 
          if(argumentType.isStringValueType()) {
            typeName = "string";
          }
          else 
            if(argumentType.isNumberValueType()) {
              typeName = "number";
            }
            else 
              if(argumentType.isBooleanValueType()) {
                typeName = "boolean";
              }
              else 
                if(argumentType.isVoidType()) {
                  typeName = "undefined";
                }
                else 
                  if(argumentType.isUnionType()) {
                    typeName = null;
                  }
        if(typeName != null) {
          Node newNode = IR.string(typeName);
          typeofNode.getParent().replaceChild(typeofNode, newNode);
          reportCodeChange();
          return newNode;
        }
      }
    }
    return typeofNode;
  }
}