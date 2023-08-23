package com.google.javascript.jscomp;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.LinkedHashMap;

public class FindExportableNodes extends AbstractPostOrderCallback  {
  final static DiagnosticType NON_GLOBAL_ERROR = DiagnosticType.error("JSC_NON_GLOBAL_ERROR", "@export only applies to symbols/properties defined in the " + "global scope.");
  final private LinkedHashMap<String, GenerateNodeContext> exports;
  final private AbstractCompiler compiler;
  public FindExportableNodes(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
    this.exports = Maps.newLinkedHashMap();
  }
  public LinkedHashMap<String, GenerateNodeContext> getExports() {
    return exports;
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    JSDocInfo docInfo = n.getJSDocInfo();
    if(docInfo != null && docInfo.isExport()) {
      String export = null;
      GenerateNodeContext context = null;
      switch (n.getType()){
        case Token.FUNCTION:
        boolean var_1882 = parent.isScript();
        if(var_1882) {
          export = NodeUtil.getFunctionName(n);
          context = new GenerateNodeContext(n, parent, n);
        }
        break ;
        case Token.ASSIGN:
        Node grandparent = parent.getParent();
        if(grandparent != null && grandparent.isScript() && parent.isExprResult() && !n.getLastChild().isAssign()) {
          export = n.getFirstChild().getQualifiedName();
          context = new GenerateNodeContext(n, grandparent, parent);
        }
        break ;
        case Token.VAR:
        if(parent.isScript()) {
          if(n.getFirstChild().hasChildren() && !n.getFirstChild().getFirstChild().isAssign()) {
            export = n.getFirstChild().getString();
            context = new GenerateNodeContext(n, parent, n);
          }
        }
      }
      if(export != null) {
        exports.put(export, context);
      }
      else {
        compiler.report(t.makeError(n, NON_GLOBAL_ERROR));
      }
    }
  }
  
  public static class GenerateNodeContext  {
    final private Node scriptNode;
    final private Node contextNode;
    final private Node node;
    public GenerateNodeContext(Node node, Node scriptNode, Node contextNode) {
      super();
      this.node = node;
      this.scriptNode = scriptNode;
      this.contextNode = contextNode;
    }
    public Node getContextNode() {
      return contextNode;
    }
    public Node getNode() {
      return node;
    }
    public Node getScriptNode() {
      return scriptNode;
    }
  }
}