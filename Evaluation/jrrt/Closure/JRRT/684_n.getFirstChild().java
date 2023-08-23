package com.google.javascript.jscomp;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.protobuf.TextFormat;
import java.io.IOException;
import java.util.List;

class InstrumentFunctions implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private FunctionNames functionNames;
  final private String templateFilename;
  final private String appNameStr;
  final private String initCodeSource;
  final private String definedFunctionName;
  final private String reportFunctionName;
  final private String reportFunctionExitName;
  final private String appNameSetter;
  final private List<String> declarationsToRemove;
  InstrumentFunctions(AbstractCompiler compiler, FunctionNames functionNames, String templateFilename, String appNameStr, Readable readable) {
    super();
    this.compiler = compiler;
    this.functionNames = functionNames;
    this.templateFilename = templateFilename;
    this.appNameStr = appNameStr;
    Instrumentation.Builder builder = Instrumentation.newBuilder();
    try {
      TextFormat.merge(readable, builder);
    }
    catch (IOException e) {
      compiler.report(JSError.make(RhinoErrorReporter.PARSE_ERROR, "Error reading instrumentation template protobuf at " + templateFilename));
      this.initCodeSource = "";
      this.definedFunctionName = "";
      this.reportFunctionName = "";
      this.reportFunctionExitName = "";
      this.appNameSetter = "";
      this.declarationsToRemove = Lists.newArrayList();
      return ;
    }
    Instrumentation template = builder.build();
    StringBuilder initCodeSourceBuilder = new StringBuilder();
    for (String line : template.getInitList()) {
      initCodeSourceBuilder.append(line).append("\n");
    }
    this.initCodeSource = initCodeSourceBuilder.toString();
    this.definedFunctionName = template.getReportDefined();
    this.reportFunctionName = template.getReportCall();
    this.reportFunctionExitName = template.getReportExit();
    this.appNameSetter = template.getAppNameSetter();
    this.declarationsToRemove = ImmutableList.copyOf(template.getDeclarationToRemoveList());
  }
  @Override() public void process(Node externs, Node root) {
    Node initCode = null;
    if(!initCodeSource.isEmpty()) {
      Node initCodeRoot = compiler.parseSyntheticCode(templateFilename + ":init", initCodeSource);
      if(initCodeRoot != null && initCodeRoot.getFirstChild() != null) {
        initCode = initCodeRoot.removeChildren();
      }
      else {
        return ;
      }
    }
    NodeTraversal.traverse(compiler, root, new RemoveCallback(declarationsToRemove));
    NodeTraversal.traverse(compiler, root, new InstrumentCallback());
    if(!appNameSetter.isEmpty()) {
      Node call = IR.call(IR.name(appNameSetter), IR.string(appNameStr));
      call.putBooleanProp(Node.FREE_CALL, true);
      Node expr = IR.exprResult(call);
      Node addingRoot = compiler.getNodeForCodeInsertion(null);
      addingRoot.addChildrenToFront(expr);
      compiler.reportCodeChange();
    }
    if(initCode != null) {
      Node addingRoot = compiler.getNodeForCodeInsertion(null);
      addingRoot.addChildrenToFront(initCode);
      compiler.reportCodeChange();
    }
  }
  
  private class InstrumentCallback extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isFunction()) {
        return ;
      }
      int id = functionNames.getFunctionId(n);
      if(id < 0) {
        return ;
      }
      if(!reportFunctionName.isEmpty()) {
        Node var_684 = n.getFirstChild();
        Node body = var_684.getNext().getNext();
        Node call = IR.call(IR.name(reportFunctionName), IR.number(id));
        call.putBooleanProp(Node.FREE_CALL, true);
        Node expr = IR.exprResult(call);
        body.addChildToFront(expr);
        compiler.reportCodeChange();
      }
      if(!reportFunctionExitName.isEmpty()) {
        Node body = n.getFirstChild().getNext().getNext();
        (new InstrumentReturns(id)).process(body);
      }
      if(!definedFunctionName.isEmpty()) {
        Node call = IR.call(IR.name(definedFunctionName), IR.number(id));
        call.putBooleanProp(Node.FREE_CALL, true);
        Node expr = NodeUtil.newExpr(call);
        Node addingRoot = null;
        if(NodeUtil.isFunctionDeclaration(n)) {
          JSModule module = t.getModule();
          addingRoot = compiler.getNodeForCodeInsertion(module);
          addingRoot.addChildToFront(expr);
        }
        else {
          Node beforeChild = n;
          for (Node ancestor : n.getAncestors()) {
            int type = ancestor.getType();
            if(type == Token.BLOCK || type == Token.SCRIPT) {
              addingRoot = ancestor;
              break ;
            }
            beforeChild = ancestor;
          }
          addingRoot.addChildBefore(expr, beforeChild);
        }
        compiler.reportCodeChange();
      }
    }
  }
  
  private class InstrumentReturns implements NodeTraversal.Callback  {
    final private int functionId;
    InstrumentReturns(int functionId) {
      super();
      this.functionId = functionId;
    }
    private Node newReportFunctionExitNode() {
      Node call = IR.call(IR.name(reportFunctionExitName), IR.number(functionId));
      call.putBooleanProp(Node.FREE_CALL, true);
      return call;
    }
    private boolean allPathsReturn(Node block) {
      ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
      cfa.process(null, block);
      ControlFlowGraph<Node> cfg = cfa.getCfg();
      Node returnPathsParent = cfg.getImplicitReturn().getValue();
      for (DiGraphNode<Node, Branch> pred : cfg.getDirectedPredNodes(returnPathsParent)) {
        Node n = pred.getValue();
        if(!n.isReturn()) {
          return false;
        }
      }
      return true;
    }
    @Override() public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      return !n.isFunction();
    }
    void process(Node body) {
      NodeTraversal.traverse(compiler, body, this);
      if(!allPathsReturn(body)) {
        Node call = newReportFunctionExitNode();
        Node expr = IR.exprResult(call);
        body.addChildToBack(expr);
        compiler.reportCodeChange();
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isReturn()) {
        return ;
      }
      Node call = newReportFunctionExitNode();
      Node returnRhs = n.removeFirstChild();
      if(returnRhs != null) {
        call.addChildToBack(returnRhs);
      }
      n.addChildToFront(call);
      compiler.reportCodeChange();
    }
  }
  
  private static class RemoveCallback extends AbstractPostOrderCallback  {
    final private List<String> removable;
    RemoveCallback(List<String> removable) {
      super();
      this.removable = removable;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(NodeUtil.isVarDeclaration(n)) {
        if(removable.contains(n.getString())) {
          parent.removeChild(n);
          if(!parent.hasChildren()) {
            parent.getParent().removeChild(parent);
          }
        }
      }
    }
  }
}