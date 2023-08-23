package com.google.javascript.jscomp;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

class InlineSimpleMethods extends MethodCompilerPass  {
  final private static Logger logger = Logger.getLogger(InlineSimpleMethods.class.getName());
  final static MethodCompilerPass.SignatureStore DUMMY_SIGNATURE_STORE = new MethodCompilerPass.SignatureStore() {
      @Override() public void addSignature(String functionName, Node functionNode, String sourceFile) {
      }
      @Override() public void removeSignature(String functionName) {
      }
      @Override() public void reset() {
      }
  };
  InlineSimpleMethods(AbstractCompiler compiler) {
    super(compiler);
  }
  @Override() Callback getActingCallback() {
    return new InlineTrivialAccessors();
  }
  private static Node getMethodBlock(Node fn) {
    if(fn.getChildCount() != 3) {
      return null;
    }
    Node expectedBlock = fn.getLastChild();
    return expectedBlock.isBlock() ? expectedBlock : null;
  }
  private static Node returnedExpression(Node fn) {
    Node expectedBlock = getMethodBlock(fn);
    if(!expectedBlock.hasOneChild()) {
      return null;
    }
    Node expectedReturn = expectedBlock.getFirstChild();
    if(!expectedReturn.isReturn()) {
      return null;
    }
    if(!expectedReturn.hasOneChild()) {
      return null;
    }
    return expectedReturn.getLastChild();
  }
  @Override() SignatureStore getSignatureStore() {
    return DUMMY_SIGNATURE_STORE;
  }
  private boolean allDefinitionsEquivalent(Collection<Node> definitions) {
    List<Node> list = Lists.newArrayList();
    list.addAll(definitions);
    Node node0 = list.get(0);
    for(int i = 1; i < list.size(); i++) {
      if(!compiler.areNodesEqualForInlining(list.get(i), node0)) {
        return false;
      }
    }
    return true;
  }
  private boolean argsMayHaveSideEffects(Node call) {
    for(com.google.javascript.rhino.Node currentChild = call.getFirstChild().getNext(); currentChild != null; currentChild = currentChild.getNext()) {
      if(NodeUtil.mayHaveSideEffects(currentChild, compiler)) {
        return true;
      }
    }
    return false;
  }
  private static boolean isEmptyMethod(Node fn) {
    Node expectedBlock = getMethodBlock(fn);
    return expectedBlock == null ? false : NodeUtil.isEmptyBlock(expectedBlock);
  }
  private static boolean isPropertyTree(Node expectedGetprop) {
    if(!expectedGetprop.isGetProp()) {
      return false;
    }
    Node leftChild = expectedGetprop.getFirstChild();
    if(!leftChild.isThis() && !isPropertyTree(leftChild)) {
      return false;
    }
    Node retVal = leftChild.getNext();
    if(NodeUtil.getStringValue(retVal) == null) {
      return false;
    }
    return true;
  }
  private void inlineConstReturn(Node parent, Node call, Node returnedValue) {
    Node retValue = returnedValue.cloneTree();
    parent.replaceChild(call, retValue);
    compiler.reportCodeChange();
  }
  private void inlineEmptyMethod(NodeTraversal t, Node parent, Node call) {
    if(NodeUtil.isExprCall(parent)) {
      parent.getParent().replaceChild(parent, IR.empty());
    }
    else {
      Node srcLocation = call;
      parent.replaceChild(call, NodeUtil.newUndefinedNode(srcLocation));
    }
    compiler.reportCodeChange();
  }
  private void inlinePropertyReturn(Node parent, Node call, Node returnedValue) {
    Node getProp = returnedValue.cloneTree();
    replaceThis(getProp, call.getFirstChild().removeFirstChild());
    parent.replaceChild(call, getProp);
    compiler.reportCodeChange();
  }
  private static void replaceThis(Node expectedGetprop, Node replacement) {
    Node leftChild = expectedGetprop.getFirstChild();
    if(leftChild.isThis()) {
      expectedGetprop.replaceChild(leftChild, replacement);
    }
    else {
      replaceThis(leftChild, replacement);
    }
  }
  
  private class InlineTrivialAccessors extends InvocationsCallback  {
    @Override() void visit(NodeTraversal t, Node callNode, Node parent, String callName) {
      if(externMethods.contains(callName) || nonMethodProperties.contains(callName)) {
        return ;
      }
      Collection<Node> definitions = methodDefinitions.get(callName);
      int var_1629 = definitions.size();
      if(definitions == null || var_1629 == 0) {
        return ;
      }
      Node firstDefinition = definitions.iterator().next();
      if(definitions.size() == 1 || allDefinitionsEquivalent(definitions)) {
        if(!argsMayHaveSideEffects(callNode)) {
          Node returned = returnedExpression(firstDefinition);
          if(returned != null) {
            if(isPropertyTree(returned)) {
              logger.fine("Inlining property accessor: " + callName);
              inlinePropertyReturn(parent, callNode, returned);
            }
            else 
              if(NodeUtil.isLiteralValue(returned, false) && !NodeUtil.mayHaveSideEffects(callNode.getFirstChild(), compiler)) {
                logger.fine("Inlining constant accessor: " + callName);
                inlineConstReturn(parent, callNode, returned);
              }
          }
          else 
            if(isEmptyMethod(firstDefinition) && !NodeUtil.mayHaveSideEffects(callNode.getFirstChild(), compiler)) {
              logger.fine("Inlining empty method: " + callName);
              inlineEmptyMethod(t, parent, callNode);
            }
        }
      }
      else {
        logger.fine("Method \'" + callName + "\' has conflicting definitions.");
      }
    }
  }
}