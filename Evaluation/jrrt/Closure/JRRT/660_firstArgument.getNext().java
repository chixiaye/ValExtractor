package com.google.javascript.jscomp;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class ObjectPropertyStringPreprocess implements CompilerPass  {
  final static String OBJECT_PROPERTY_STRING = "goog.testing.ObjectPropertyString";
  final public static String EXTERN_OBJECT_PROPERTY_STRING = "JSCompiler_ObjectPropertyString";
  final static DiagnosticType INVALID_NUM_ARGUMENTS_ERROR = DiagnosticType.error("JSC_OBJECT_PROPERTY_STRING_NUM_ARGS", "goog.testing.ObjectPropertyString instantiated with \"{0}\" " + "arguments, expected 2.");
  final static DiagnosticType QUALIFIED_NAME_EXPECTED_ERROR = DiagnosticType.error("JSC_OBJECT_PROPERTY_STRING_QUALIFIED_NAME_EXPECTED", "goog.testing.ObjectPropertyString instantiated with invalid " + "argument, qualified name expected. Was \"{0}\".");
  final static DiagnosticType STRING_LITERAL_EXPECTED_ERROR = DiagnosticType.error("JSC_OBJECT_PROPERTY_STRING_STRING_LITERAL_EXPECTED", "goog.testing.ObjectPropertyString instantiated with invalid " + "argument, string literal expected. Was \"{0}\".");
  final private AbstractCompiler compiler;
  ObjectPropertyStringPreprocess(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
  }
  private void addExternDeclaration(Node externs, Node declarationStmt) {
    Node script = externs.getLastChild();
    if(script == null || !script.isScript()) {
      script = IR.script();
      externs.addChildToBack(script);
    }
    script.addChildToBack(declarationStmt);
  }
  @Override() public void process(Node externs, Node root) {
    addExternDeclaration(externs, IR.var(IR.name(EXTERN_OBJECT_PROPERTY_STRING)));
    NodeTraversal.traverse(compiler, root, new Callback());
  }
  
  private class Callback extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(OBJECT_PROPERTY_STRING.equals(n.getQualifiedName())) {
        Node newName = IR.name(EXTERN_OBJECT_PROPERTY_STRING);
        newName.copyInformationFrom(n);
        parent.replaceChild(n, newName);
        compiler.reportCodeChange();
        return ;
      }
      if(!n.isNew()) {
        return ;
      }
      Node objectName = n.getFirstChild();
      if(!EXTERN_OBJECT_PROPERTY_STRING.equals(objectName.getQualifiedName())) {
        return ;
      }
      if(n.getChildCount() != 3) {
        compiler.report(t.makeError(n, INVALID_NUM_ARGUMENTS_ERROR, "" + n.getChildCount()));
        return ;
      }
      Node firstArgument = objectName.getNext();
      if(!firstArgument.isQualifiedName()) {
        compiler.report(t.makeError(firstArgument, QUALIFIED_NAME_EXPECTED_ERROR, Token.name(firstArgument.getType())));
        return ;
      }
      Node var_660 = firstArgument.getNext();
      Node secondArgument = var_660;
      if(!secondArgument.isString()) {
        compiler.report(t.makeError(secondArgument, STRING_LITERAL_EXPECTED_ERROR, Token.name(secondArgument.getType())));
        return ;
      }
      Node newFirstArgument = NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), compiler.getCodingConvention().getGlobalObject()).srcrefTree(firstArgument);
      Node newSecondArgument = NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), firstArgument.getQualifiedName() + "." + firstArgument.getNext().getString()).srcrefTree(secondArgument);
      n.replaceChild(firstArgument, newFirstArgument);
      n.replaceChild(secondArgument, newSecondArgument);
      compiler.reportCodeChange();
    }
  }
}