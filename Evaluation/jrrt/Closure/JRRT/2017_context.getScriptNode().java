package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.javascript.jscomp.FindExportableNodes.GenerateNodeContext;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.util.Map;

class GenerateExports implements CompilerPass  {
  final private static String PROTOTYPE_PROPERTY = "prototype";
  final private AbstractCompiler compiler;
  final private String exportSymbolFunction;
  final private String exportPropertyFunction;
  GenerateExports(AbstractCompiler compiler, String exportSymbolFunction, String exportPropertyFunction) {
    super();
    Preconditions.checkNotNull(compiler);
    Preconditions.checkNotNull(exportSymbolFunction);
    Preconditions.checkNotNull(exportPropertyFunction);
    this.compiler = compiler;
    this.exportSymbolFunction = exportSymbolFunction;
    this.exportPropertyFunction = exportPropertyFunction;
  }
  private String getPropertyName(Node node) {
    Preconditions.checkArgument(node.isGetProp());
    return node.getLastChild().getString();
  }
  private void annotate(Node node) {
    NodeTraversal.traverse(compiler, node, new PrepareAst.PrepareAnnotations());
  }
  @Override() public void process(Node externs, Node root) {
    FindExportableNodes findExportableNodes = new FindExportableNodes(compiler);
    NodeTraversal.traverse(compiler, root, findExportableNodes);
    Map<String, GenerateNodeContext> exports = findExportableNodes.getExports();
    CodingConvention convention = compiler.getCodingConvention();
    for (Map.Entry<String, GenerateNodeContext> entry : exports.entrySet()) {
      String export = entry.getKey();
      GenerateNodeContext context = entry.getValue();
      String parent = null;
      String grandparent = null;
      Node node = context.getNode().getFirstChild();
      if(node.isGetProp()) {
        parent = node.getFirstChild().getQualifiedName();
        if(node.getFirstChild().isGetProp() && getPropertyName(node.getFirstChild()).equals(PROTOTYPE_PROPERTY)) {
          grandparent = node.getFirstChild().getFirstChild().getQualifiedName();
        }
      }
      boolean useExportSymbol = true;
      if(grandparent != null && exports.containsKey(grandparent)) {
        useExportSymbol = false;
      }
      else 
        if(parent != null && exports.containsKey(parent)) {
          useExportSymbol = false;
        }
      Node call;
      if(useExportSymbol) {
        call = IR.call(NodeUtil.newQualifiedNameNode(convention, exportSymbolFunction, context.getNode(), export), IR.string(export), NodeUtil.newQualifiedNameNode(convention, export, context.getNode(), export));
      }
      else {
        String property = getPropertyName(node);
        call = IR.call(NodeUtil.newQualifiedNameNode(convention, exportPropertyFunction, context.getNode(), exportPropertyFunction), NodeUtil.newQualifiedNameNode(convention, parent, context.getNode(), exportPropertyFunction), IR.string(property), NodeUtil.newQualifiedNameNode(convention, export, context.getNode(), exportPropertyFunction));
      }
      Node expression = IR.exprResult(call);
      annotate(expression);
      Node insertionPoint = context.getContextNode().getNext();
      while(insertionPoint != null && NodeUtil.isExprCall(insertionPoint) && convention.getClassesDefinedByCall(insertionPoint.getFirstChild()) != null){
        insertionPoint = insertionPoint.getNext();
      }
      if(insertionPoint == null) {
        Node var_2017 = context.getScriptNode();
        var_2017.addChildToBack(expression);
      }
      else {
        context.getScriptNode().addChildBefore(expression, insertionPoint);
      }
      compiler.reportCodeChange();
    }
  }
}