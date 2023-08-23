package com.google.javascript.jscomp;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Map;

class CheckProvides implements HotSwapCompilerPass  {
  final private AbstractCompiler compiler;
  final private CheckLevel checkLevel;
  final private CodingConvention codingConvention;
  final static DiagnosticType MISSING_PROVIDE_WARNING = DiagnosticType.disabled("JSC_MISSING_PROVIDE", "missing goog.provide(\'\'{0}\'\')");
  CheckProvides(AbstractCompiler compiler, CheckLevel checkLevel) {
    super();
    this.compiler = compiler;
    this.checkLevel = checkLevel;
    this.codingConvention = compiler.getCodingConvention();
  }
  @Override() public void hotSwapScript(Node scriptRoot, Node originalRoot) {
    CheckProvidesCallback callback = new CheckProvidesCallback(codingConvention);
    new NodeTraversal(compiler, callback).traverse(scriptRoot);
  }
  @Override() public void process(Node externs, Node root) {
    hotSwapScript(root, null);
  }
  
  private class CheckProvidesCallback extends AbstractShallowCallback  {
    final private Map<String, Node> provides = Maps.newHashMap();
    final private Map<String, Node> ctors = Maps.newHashMap();
    final private CodingConvention convention;
    CheckProvidesCallback(CodingConvention convention) {
      super();
      this.convention = convention;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.CALL:
        String providedClassName = codingConvention.extractClassNameIfProvide(n, parent);
        if(providedClassName != null) {
          provides.put(providedClassName, n);
        }
        break ;
        case Token.FUNCTION:
        visitFunctionNode(n, parent);
        break ;
        case Token.SCRIPT:
        visitScriptNode(t, n);
      }
    }
    private void visitFunctionNode(Node n, Node parent) {
      Node name = null;
      JSDocInfo info = parent.getJSDocInfo();
      if(info != null && info.isConstructor()) {
        name = parent.getFirstChild();
      }
      else {
        info = n.getJSDocInfo();
        if(info != null && info.isConstructor()) {
          name = n.getFirstChild();
        }
      }
      if(name != null && name.isQualifiedName()) {
        String qualifiedName = name.getQualifiedName();
        if(!this.convention.isPrivate(qualifiedName)) {
          Visibility visibility = info.getVisibility();
          if(!visibility.equals(JSDocInfo.Visibility.PRIVATE)) {
            ctors.put(qualifiedName, name);
          }
        }
      }
    }
    private void visitScriptNode(NodeTraversal t, Node n) {
      for (Map.Entry<String, Node> ctorEntry : ctors.entrySet()) {
        String var_2116 = ctorEntry.getKey();
        String ctor = var_2116;
        int index = -1;
        boolean found = false;
        do {
          index = ctor.indexOf('.', index + 1);
          String provideKey = index == -1 ? ctor : ctor.substring(0, index);
          if(provides.containsKey(provideKey)) {
            found = true;
            break ;
          }
        }while(index != -1);
        if(!found) {
          compiler.report(t.makeError(ctorEntry.getValue(), checkLevel, MISSING_PROVIDE_WARNING, ctorEntry.getKey()));
        }
      }
      provides.clear();
      ctors.clear();
    }
  }
}