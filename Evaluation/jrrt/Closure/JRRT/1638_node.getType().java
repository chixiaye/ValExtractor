package com.google.javascript.jscomp;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Iterator;
import javax.annotation.Nullable;

class ReplaceMessages extends JsMessageVisitor  {
  final private MessageBundle bundle;
  final private boolean strictReplacement;
  final static DiagnosticType BUNDLE_DOES_NOT_HAVE_THE_MESSAGE = DiagnosticType.error("JSC_BUNDLE_DOES_NOT_HAVE_THE_MESSAGE", "Message with id = {0} could not be found in replacement bundle");
  ReplaceMessages(AbstractCompiler compiler, MessageBundle bundle, boolean checkDuplicatedMessages, JsMessage.Style style, boolean strictReplacement) {
    super(compiler, checkDuplicatedMessages, style, bundle.idGenerator());
    this.bundle = bundle;
    this.strictReplacement = strictReplacement;
  }
  private Node constructAddOrStringNode(Iterator<CharSequence> partsIterator, Node argListNode) throws MalformedException {
    CharSequence part = partsIterator.next();
    Node partNode = null;
    if(part instanceof JsMessage.PlaceholderReference) {
      JsMessage.PlaceholderReference phRef = (JsMessage.PlaceholderReference)part;
      for (Node node : argListNode.children()) {
        if(node.isName()) {
          String arg = node.getString();
          if(arg.equalsIgnoreCase(phRef.getName())) {
            partNode = IR.name(arg);
          }
        }
      }
      if(partNode == null) {
        throw new MalformedException("Unrecognized message placeholder referenced: " + phRef.getName(), argListNode);
      }
    }
    else {
      partNode = IR.string(part.toString());
    }
    if(partsIterator.hasNext()) {
      return IR.add(partNode, constructAddOrStringNode(partsIterator, argListNode));
    }
    else {
      return partNode;
    }
  }
  private Node constructStringExprNode(Iterator<CharSequence> parts, Node objLitNode) throws MalformedException {
    CharSequence part = parts.next();
    Node partNode = null;
    if(part instanceof JsMessage.PlaceholderReference) {
      JsMessage.PlaceholderReference phRef = (JsMessage.PlaceholderReference)part;
      if(objLitNode == null) {
        throw new MalformedException("Empty placeholder value map " + "for a translated message with placeholders.", objLitNode);
      }
      for(com.google.javascript.rhino.Node key = objLitNode.getFirstChild(); key != null; key = key.getNext()) {
        if(key.getString().equals(phRef.getName())) {
          Node valueNode = key.getFirstChild();
          partNode = valueNode.cloneTree();
        }
      }
      if(partNode == null) {
        throw new MalformedException("Unrecognized message placeholder referenced: " + phRef.getName(), objLitNode);
      }
    }
    else {
      partNode = IR.string(part.toString());
    }
    if(parts.hasNext()) {
      return IR.add(partNode, constructStringExprNode(parts, objLitNode));
    }
    else {
      return partNode;
    }
  }
  private Node getNewValueNode(JsMessage message, Node origValueNode) throws MalformedException {
    switch (origValueNode.getType()){
      case Token.FUNCTION:
      updateFunctionNode(message, origValueNode);
      return origValueNode;
      case Token.STRING:
      String newString = message.toString();
      if(!origValueNode.getString().equals(newString)) {
        origValueNode.setString(newString);
        compiler.reportCodeChange();
      }
      return origValueNode;
      case Token.ADD:
      return IR.string(message.toString());
      case Token.CALL:
      return replaceCallNode(message, origValueNode);
      default:
      throw new MalformedException("Expected FUNCTION, STRING, or ADD node; found: " + origValueNode.getType(), origValueNode);
    }
  }
  private Node replaceCallNode(JsMessage message, Node callNode) throws MalformedException {
    checkNode(callNode, Token.CALL);
    Node getPropNode = callNode.getFirstChild();
    checkNode(getPropNode, Token.GETPROP);
    Node stringExprNode = getPropNode.getNext();
    checkStringExprNode(stringExprNode);
    Node objLitNode = stringExprNode.getNext();
    return constructStringExprNode(message.parts().iterator(), objLitNode);
  }
  private void checkStringExprNode(@Nullable() Node node) {
    if(node == null) {
      throw new IllegalArgumentException("Expected a string; found: null");
    }
    int var_1638 = node.getType();
    switch (var_1638){
      case Token.STRING:
      break ;
      case Token.ADD:
      Node c = node.getFirstChild();
      checkStringExprNode(c);
      checkStringExprNode(c.getNext());
      break ;
      default:
      throw new IllegalArgumentException("Expected a string; found: " + node.getType());
    }
  }
  @Override() void processJsMessage(JsMessage message, JsMessageDefinition definition) {
    JsMessage replacement = bundle.getMessage(message.getId());
    if(replacement == null) {
      if(strictReplacement) {
        compiler.report(JSError.make(message.getSourceName(), definition.getMessageNode(), BUNDLE_DOES_NOT_HAVE_THE_MESSAGE, message.getId()));
        return ;
      }
      else {
        replacement = message;
      }
    }
    Node newValue;
    Node msgNode = definition.getMessageNode();
    try {
      newValue = getNewValueNode(replacement, msgNode);
    }
    catch (MalformedException e) {
      compiler.report(JSError.make(message.getSourceName(), e.getNode(), MESSAGE_TREE_MALFORMED, e.getMessage()));
      newValue = msgNode;
    }
    if(newValue != msgNode) {
      newValue.copyInformationFromForTree(msgNode);
      definition.getMessageParentNode().replaceChild(msgNode, newValue);
      compiler.reportCodeChange();
    }
  }
  @Override() void processMessageFallback(Node callNode, JsMessage message1, JsMessage message2) {
    boolean isFirstMessageTranslated = (bundle.getMessage(message1.getId()) != null);
    boolean isSecondMessageTranslated = (bundle.getMessage(message2.getId()) != null);
    Node replacementNode = isSecondMessageTranslated && !isFirstMessageTranslated ? callNode.getChildAtIndex(2) : callNode.getChildAtIndex(1);
    callNode.getParent().replaceChild(callNode, replacementNode.detachFromParent());
  }
  private void updateFunctionNode(JsMessage message, Node functionNode) throws MalformedException {
    checkNode(functionNode, Token.FUNCTION);
    Node nameNode = functionNode.getFirstChild();
    checkNode(nameNode, Token.NAME);
    Node argListNode = nameNode.getNext();
    checkNode(argListNode, Token.PARAM_LIST);
    Node oldBlockNode = argListNode.getNext();
    checkNode(oldBlockNode, Token.BLOCK);
    Iterator<CharSequence> iterator = message.parts().iterator();
    Node valueNode = iterator.hasNext() ? constructAddOrStringNode(iterator, argListNode) : IR.string("");
    Node newBlockNode = IR.block(IR.returnNode(valueNode));
    if(newBlockNode.checkTreeEquals(oldBlockNode) != null) {
      newBlockNode.copyInformationFromForTree(oldBlockNode);
      functionNode.replaceChild(oldBlockNode, newBlockNode);
      compiler.reportCodeChange();
    }
  }
}