package com.google.javascript.jscomp;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.NameInfo;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.Property;
import com.google.javascript.jscomp.AnalyzePrototypeProperties.Symbol;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

class CrossModuleMethodMotion implements CompilerPass  {
  final static DiagnosticType NULL_COMMON_MODULE_ERROR = DiagnosticType.error("JSC_INTERNAL_ERROR_MODULE_DEPEND", "null deepest common module");
  final private AbstractCompiler compiler;
  final private IdGenerator idGenerator;
  final private AnalyzePrototypeProperties analyzer;
  final private JSModuleGraph moduleGraph;
  final static String STUB_METHOD_NAME = "JSCompiler_stubMethod";
  final static String UNSTUB_METHOD_NAME = "JSCompiler_unstubMethod";
  final static String STUB_DECLARATIONS = "var JSCompiler_stubMap = [];" + "function JSCompiler_stubMethod(JSCompiler_stubMethod_id) {" + "  return function() {" + "    return JSCompiler_stubMap[JSCompiler_stubMethod_id].apply(" + "        this, arguments);" + "  };" + "}" + "function JSCompiler_unstubMethod(" + "    JSCompiler_unstubMethod_id, JSCompiler_unstubMethod_body) {" + "  return JSCompiler_stubMap[JSCompiler_unstubMethod_id] = " + "      JSCompiler_unstubMethod_body;" + "}";
  CrossModuleMethodMotion(AbstractCompiler compiler, IdGenerator idGenerator, boolean canModifyExterns) {
    super();
    this.compiler = compiler;
    this.idGenerator = idGenerator;
    this.moduleGraph = compiler.getModuleGraph();
    this.analyzer = new AnalyzePrototypeProperties(compiler, moduleGraph, canModifyExterns, false);
  }
  private void moveMethods(Collection<NameInfo> allNameInfo) {
    boolean var_1862 = idGenerator.hasGeneratedAnyIds();
    boolean hasStubDeclaration = var_1862;
    for (NameInfo nameInfo : allNameInfo) {
      if(!nameInfo.isReferenced()) {
        continue ;
      }
      if(nameInfo.readsClosureVariables()) {
        continue ;
      }
      JSModule deepestCommonModuleRef = nameInfo.getDeepestCommonModuleRef();
      if(deepestCommonModuleRef == null) {
        compiler.report(JSError.make(NULL_COMMON_MODULE_ERROR));
        continue ;
      }
      Iterator<Symbol> declarations = nameInfo.getDeclarations().descendingIterator();
      while(declarations.hasNext()){
        Symbol symbol = declarations.next();
        if(!(symbol instanceof Property)) {
          continue ;
        }
        Property prop = (Property)symbol;
        if(prop.getRootVar() == null || !prop.getRootVar().isGlobal()) {
          continue ;
        }
        Node value = prop.getValue();
        if(moduleGraph.dependsOn(deepestCommonModuleRef, prop.getModule()) && value.isFunction()) {
          Node valueParent = value.getParent();
          if(valueParent.isGetterDef() || valueParent.isSetterDef()) {
            continue ;
          }
          Node proto = prop.getPrototype();
          int stubId = idGenerator.newId();
          Node stubCall = IR.call(IR.name(STUB_METHOD_NAME), IR.number(stubId)).copyInformationFromForTree(value);
          stubCall.putBooleanProp(Node.FREE_CALL, true);
          valueParent.replaceChild(value, stubCall);
          Node unstubParent = compiler.getNodeForCodeInsertion(deepestCommonModuleRef);
          Node unstubCall = IR.call(IR.name(UNSTUB_METHOD_NAME), IR.number(stubId), value);
          unstubCall.putBooleanProp(Node.FREE_CALL, true);
          unstubParent.addChildToFront(IR.exprResult(IR.assign(IR.getprop(proto.cloneTree(), IR.string(nameInfo.name)), unstubCall)).copyInformationFromForTree(value));
          compiler.reportCodeChange();
        }
      }
    }
    if(!hasStubDeclaration && idGenerator.hasGeneratedAnyIds()) {
      Node declarations = compiler.parseSyntheticCode(STUB_DECLARATIONS);
      compiler.getNodeForCodeInsertion(null).addChildrenToFront(declarations.removeChildren());
    }
  }
  @Override() public void process(Node externRoot, Node root) {
    if(moduleGraph != null && moduleGraph.getModuleCount() > 1) {
      analyzer.process(externRoot, root);
      moveMethods(analyzer.getAllNameInfo());
    }
  }
  
  static class IdGenerator implements Serializable  {
    final private static long serialVersionUID = 0L;
    private int currentId = 0;
    boolean hasGeneratedAnyIds() {
      return currentId != 0;
    }
    int newId() {
      return currentId++;
    }
  }
}