package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

class SyntacticScopeCreator implements ScopeCreator  {
  final private AbstractCompiler compiler;
  private Scope scope;
  private InputId inputId;
  final private RedeclarationHandler redeclarationHandler;
  final private static String ARGUMENTS = "arguments";
  final public static DiagnosticType VAR_MULTIPLY_DECLARED_ERROR = DiagnosticType.error("JSC_VAR_MULTIPLY_DECLARED_ERROR", "Variable {0} first declared in {1}");
  final public static DiagnosticType VAR_ARGUMENTS_SHADOWED_ERROR = DiagnosticType.error("JSC_VAR_ARGUMENTS_SHADOWED_ERROR", "Shadowing \"arguments\" is not allowed");
  SyntacticScopeCreator(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
    this.redeclarationHandler = new DefaultRedeclarationHandler();
  }
  SyntacticScopeCreator(AbstractCompiler compiler, RedeclarationHandler redeclarationHandler) {
    super();
    this.compiler = compiler;
    this.redeclarationHandler = redeclarationHandler;
  }
  @Override() public Scope createScope(Node n, Scope parent) {
    inputId = null;
    if(parent == null) {
      scope = Scope.createGlobalScope(n);
    }
    else {
      scope = new Scope(parent, n);
    }
    scanRoot(n, parent);
    inputId = null;
    Scope returnedScope = scope;
    scope = null;
    return returnedScope;
  }
  static Scope generateUntypedTopScope(AbstractCompiler compiler) {
    return new SyntacticScopeCreator(compiler).createScope(compiler.getRoot(), null);
  }
  static boolean hasDuplicateDeclarationSuppression(Node n, Scope.Var origVar) {
    Preconditions.checkState(n.isName());
    Node parent = n.getParent();
    Node origParent = origVar.getParentNode();
    JSDocInfo info = n.getJSDocInfo();
    if(info == null) {
      info = parent.getJSDocInfo();
    }
    if(info != null && info.getSuppressions().contains("duplicate")) {
      return true;
    }
    info = origVar.nameNode.getJSDocInfo();
    if(info == null) {
      info = origParent.getJSDocInfo();
    }
    return (info != null && info.getSuppressions().contains("duplicate"));
  }
  private void declareVar(Node n) {
    Preconditions.checkState(n.isName());
    CompilerInput input = compiler.getInput(inputId);
    String name = n.getString();
    if(scope.isDeclared(name, false) || (scope.isLocal() && name.equals(ARGUMENTS))) {
      redeclarationHandler.onRedeclaration(scope, name, n, input);
    }
    else {
      scope.declare(name, n, null, input);
    }
  }
  private void scanRoot(Node n, Scope parent) {
    if(n.isFunction()) {
      if(inputId == null) {
        inputId = NodeUtil.getInputId(n);
      }
      final Node fnNameNode = n.getFirstChild();
      final Node args = fnNameNode.getNext();
      final Node body = args.getNext();
      String fnName = fnNameNode.getString();
      if(!fnName.isEmpty() && NodeUtil.isFunctionExpression(n)) {
        declareVar(fnNameNode);
      }
      Preconditions.checkState(args.isParamList());
      for(com.google.javascript.rhino.Node a = args.getFirstChild(); a != null; a = a.getNext()) {
        Preconditions.checkState(a.isName());
        declareVar(a);
      }
      scanVars(body, n);
    }
    else {
      Preconditions.checkState(scope.getParent() == null);
      scanVars(n, null);
    }
  }
  private void scanVars(Node n, Node parent) {
    switch (n.getType()){
      case Token.VAR:
      for(com.google.javascript.rhino.Node child = n.getFirstChild(); child != null; ) {
        Node next = child.getNext();
        declareVar(child);
        child = next;
      }
      return ;
      case Token.FUNCTION:
      if(NodeUtil.isFunctionExpression(n)) {
        return ;
      }
      Node var_197 = n.getFirstChild();
      String fnName = var_197.getString();
      if(fnName.isEmpty()) {
        return ;
      }
      declareVar(n.getFirstChild());
      return ;
      case Token.CATCH:
      Preconditions.checkState(n.getChildCount() == 2);
      Preconditions.checkState(n.getFirstChild().isName());
      final Node var = n.getFirstChild();
      final Node block = var.getNext();
      declareVar(var);
      scanVars(block, n);
      return ;
      case Token.SCRIPT:
      inputId = n.getInputId();
      Preconditions.checkNotNull(inputId);
      break ;
    }
    if(NodeUtil.isControlStructure(n) || NodeUtil.isStatementBlock(n)) {
      for(com.google.javascript.rhino.Node child = n.getFirstChild(); child != null; ) {
        Node next = child.getNext();
        scanVars(child, n);
        child = next;
      }
    }
  }
  
  private class DefaultRedeclarationHandler implements RedeclarationHandler  {
    @Override() public void onRedeclaration(Scope s, String name, Node n, CompilerInput input) {
      Node parent = n.getParent();
      if(scope.isGlobal()) {
        Scope.Var origVar = scope.getVar(name);
        Node origParent = origVar.getParentNode();
        if(origParent.isCatch() && parent.isCatch()) {
          return ;
        }
        boolean allowDupe = hasDuplicateDeclarationSuppression(n, origVar);
        if(!allowDupe) {
          compiler.report(JSError.make(NodeUtil.getSourceName(n), n, VAR_MULTIPLY_DECLARED_ERROR, name, (origVar.input != null ? origVar.input.getName() : "??")));
        }
      }
      else 
        if(name.equals(ARGUMENTS) && !NodeUtil.isVarDeclaration(n)) {
          compiler.report(JSError.make(NodeUtil.getSourceName(n), n, VAR_ARGUMENTS_SHADOWED_ERROR));
        }
    }
  }
  
  interface RedeclarationHandler  {
    void onRedeclaration(Scope s, String name, Node n, CompilerInput input);
  }
}