package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Set;

class VarCheck extends AbstractPostOrderCallback implements HotSwapCompilerPass  {
  final static DiagnosticType UNDEFINED_VAR_ERROR = DiagnosticType.error("JSC_UNDEFINED_VARIABLE", "variable {0} is undeclared");
  final static DiagnosticType VIOLATED_MODULE_DEP_ERROR = DiagnosticType.error("JSC_VIOLATED_MODULE_DEPENDENCY", "module {0} cannot reference {2}, defined in " + "module {1}, since {1} loads after {0}");
  final static DiagnosticType MISSING_MODULE_DEP_ERROR = DiagnosticType.warning("JSC_MISSING_MODULE_DEPENDENCY", "missing module dependency; module {0} should depend " + "on module {1} because it references {2}");
  final static DiagnosticType STRICT_MODULE_DEP_ERROR = DiagnosticType.disabled("JSC_STRICT_MODULE_DEPENDENCY", "module {0} cannot reference {2}, defined in " + "module {1}");
  final static DiagnosticType NAME_REFERENCE_IN_EXTERNS_ERROR = DiagnosticType.warning("JSC_NAME_REFERENCE_IN_EXTERNS", "accessing name {0} in externs has no effect");
  final static DiagnosticType UNDEFINED_EXTERN_VAR_ERROR = DiagnosticType.warning("JSC_UNDEFINED_EXTERN_VAR_ERROR", "name {0} is not undefined in the externs.");
  private Node synthesizedExternsRoot = null;
  final private Set<String> varsToDeclareInExterns = Sets.newHashSet();
  final private AbstractCompiler compiler;
  final private boolean sanityCheck;
  final private boolean strictExternCheck;
  VarCheck(AbstractCompiler compiler) {
    this(compiler, false);
  }
  VarCheck(AbstractCompiler compiler, boolean sanityCheck) {
    super();
    this.compiler = compiler;
    this.strictExternCheck = compiler.getErrorLevel(JSError.make("", 0, 0, UNDEFINED_EXTERN_VAR_ERROR)) == CheckLevel.ERROR;
    this.sanityCheck = sanityCheck;
  }
  private CompilerInput getSynthesizedExternsInput() {
    return compiler.getSynthesizedExternsInput();
  }
  private Node getSynthesizedExternsRoot() {
    if(synthesizedExternsRoot == null) {
      CompilerInput synthesizedExterns = getSynthesizedExternsInput();
      synthesizedExternsRoot = synthesizedExterns.getAstRoot(compiler);
    }
    return synthesizedExternsRoot;
  }
  private void createSynthesizedExternVar(String varName) {
    Node nameNode = IR.name(varName);
    if(compiler.getCodingConvention().isConstant(varName)) {
      nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    }
    getSynthesizedExternsRoot().addChildToBack(IR.var(nameNode));
    varsToDeclareInExterns.remove(varName);
    compiler.reportCodeChange();
  }
  @Override() public void hotSwapScript(Node scriptRoot, Node originalRoot) {
    Preconditions.checkState(scriptRoot.isScript());
    NodeTraversal t = new NodeTraversal(compiler, this);
    t.traverseWithScope(scriptRoot, SyntacticScopeCreator.generateUntypedTopScope(compiler));
  }
  @Override() public void process(Node externs, Node root) {
    if(!sanityCheck) {
      NodeTraversal.traverse(compiler, externs, new NameRefInExternsCheck());
    }
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(externs, root), this);
    for (String varName : varsToDeclareInExterns) {
      createSynthesizedExternVar(varName);
    }
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    if(!n.isName()) {
      return ;
    }
    String varName = n.getString();
    if(varName.isEmpty()) {
      Preconditions.checkState(parent.isFunction());
      boolean var_905 = NodeUtil.isFunctionExpression(parent);
      Preconditions.checkState(var_905);
      return ;
    }
    if((parent.isVar() || NodeUtil.isFunctionDeclaration(parent)) && varsToDeclareInExterns.contains(varName)) {
      createSynthesizedExternVar(varName);
      n.addSuppression("duplicate");
    }
    Scope scope = t.getScope();
    Scope.Var var = scope.getVar(varName);
    if(var == null) {
      if(NodeUtil.isFunctionExpression(parent)) {
      }
      else {
        if(!strictExternCheck || !t.getInput().isExtern()) {
          t.report(n, UNDEFINED_VAR_ERROR, varName);
        }
        if(sanityCheck) {
          throw new IllegalStateException("Unexpected variable " + varName);
        }
        else {
          createSynthesizedExternVar(varName);
          scope.getGlobalScope().declare(varName, n, null, getSynthesizedExternsInput());
        }
      }
      return ;
    }
    CompilerInput currInput = t.getInput();
    CompilerInput varInput = var.input;
    if(currInput == varInput || currInput == null || varInput == null) {
      return ;
    }
    JSModule currModule = currInput.getModule();
    JSModule varModule = varInput.getModule();
    JSModuleGraph moduleGraph = compiler.getModuleGraph();
    if(!sanityCheck && varModule != currModule && varModule != null && currModule != null) {
      if(moduleGraph.dependsOn(currModule, varModule)) {
      }
      else {
        if(scope.isGlobal()) {
          if(moduleGraph.dependsOn(varModule, currModule)) {
            t.report(n, VIOLATED_MODULE_DEP_ERROR, currModule.getName(), varModule.getName(), varName);
          }
          else {
            t.report(n, MISSING_MODULE_DEP_ERROR, currModule.getName(), varModule.getName(), varName);
          }
        }
        else {
          t.report(n, STRICT_MODULE_DEP_ERROR, currModule.getName(), varModule.getName(), varName);
        }
      }
    }
  }
  
  private class NameRefInExternsCheck extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isName()) {
        switch (parent.getType()){
          case Token.VAR:
          case Token.FUNCTION:
          case Token.PARAM_LIST:
          break ;
          case Token.GETPROP:
          if(n == parent.getFirstChild()) {
            Scope scope = t.getScope();
            Scope.Var var = scope.getVar(n.getString());
            if(var == null) {
              t.report(n, UNDEFINED_EXTERN_VAR_ERROR, n.getString());
              varsToDeclareInExterns.add(n.getString());
            }
          }
          break ;
          default:
          t.report(n, NAME_REFERENCE_IN_EXTERNS_ERROR, n.getString());
          Scope scope = t.getScope();
          Scope.Var var = scope.getVar(n.getString());
          if(var == null) {
            varsToDeclareInExterns.add(n.getString());
          }
          break ;
        }
      }
    }
  }
}