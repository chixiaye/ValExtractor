package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.util.regex.Pattern;

class ExportTestFunctions implements CompilerPass  {
  final private static Pattern TEST_FUNCTIONS_NAME_PATTERN = Pattern.compile("^(?:((\\w+\\.)+prototype\\.)*" + "(setUpPage|setUp|tearDown|tearDownPage|test\\w+))$");
  private AbstractCompiler compiler;
  final private String exportSymbolFunction;
  final private String exportPropertyFunction;
  ExportTestFunctions(AbstractCompiler compiler, String exportSymbolFunction, String exportPropertyFunction) {
    super();
    Preconditions.checkNotNull(compiler);
    this.compiler = compiler;
    this.exportSymbolFunction = exportSymbolFunction;
    this.exportPropertyFunction = exportPropertyFunction;
  }
  private boolean isTestFunction(Node n, String functionName) {
    return !(functionName == null || !TEST_FUNCTIONS_NAME_PATTERN.matcher(functionName).matches());
  }
  private void exportTestFunctionAsProperty(String fullyQualifiedFunctionName, Node parent, Node node, Node scriptNode) {
    String testFunctionName = NodeUtil.getPrototypePropertyName(node.getFirstChild());
    String objectName = fullyQualifiedFunctionName.substring(0, fullyQualifiedFunctionName.lastIndexOf('.'));
    String exportCallStr = String.format("%s(%s, \'%s\', %s);", exportPropertyFunction, objectName, testFunctionName, fullyQualifiedFunctionName);
    Node exportCall = this.compiler.parseSyntheticCode(exportCallStr).removeChildren();
    exportCall.useSourceInfoFromForTree(scriptNode);
    scriptNode.addChildAfter(exportCall, parent);
    compiler.reportCodeChange();
  }
  private void exportTestFunctionAsSymbol(String testFunctionName, Node node, Node scriptNode) {
    Node exportCallTarget = NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), exportSymbolFunction, node, testFunctionName);
    Node call = IR.call(exportCallTarget);
    if(exportCallTarget.isName()) {
      call.putBooleanProp(Node.FREE_CALL, true);
    }
    call.addChildToBack(IR.string(testFunctionName));
    call.addChildToBack(NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), testFunctionName, node, testFunctionName));
    Node expression = IR.exprResult(call);
    scriptNode.addChildAfter(expression, node);
    compiler.reportCodeChange();
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, new ExportTestFunctionsNodes());
  }
  
  private class ExportTestFunctionsNodes extends NodeTraversal.AbstractShallowCallback  {
    private boolean isVarDeclaredFunction(Node node) {
      if(!node.isVar()) {
        return false;
      }
      Node grandchild = node.getFirstChild().getFirstChild();
      return grandchild != null && grandchild.isFunction();
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(parent == null) {
        return ;
      }
      if(parent.isScript()) {
        if(NodeUtil.isFunctionDeclaration(n)) {
          String functionName = NodeUtil.getFunctionName(n);
          boolean var_1697 = isTestFunction(n, functionName);
          if(var_1697) {
            exportTestFunctionAsSymbol(functionName, n, parent);
          }
        }
        else 
          if(isVarDeclaredFunction(n)) {
            Node functionNode = n.getFirstChild().getFirstChild();
            String functionName = NodeUtil.getFunctionName(functionNode);
            if(isTestFunction(functionNode, functionName)) {
              exportTestFunctionAsSymbol(functionName, n, parent);
            }
          }
      }
      else 
        if(NodeUtil.isExprAssign(parent) && !n.getLastChild().isAssign()) {
          Node grandparent = parent.getParent();
          if(grandparent != null && grandparent.isScript()) {
            String functionName = n.getFirstChild().getQualifiedName();
            if(isTestFunction(n, functionName)) {
              exportTestFunctionAsProperty(functionName, parent, n, grandparent);
            }
          }
        }
    }
  }
}