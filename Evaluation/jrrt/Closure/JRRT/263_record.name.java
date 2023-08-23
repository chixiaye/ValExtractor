package com.google.javascript.jscomp;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.Node;
import java.io.Serializable;
import java.util.*;

class FunctionNames implements CompilerPass, Serializable  {
  final private static long serialVersionUID = 1L;
  final private transient AbstractCompiler compiler;
  final private Map<Node, FunctionRecord> functionMap = Maps.newLinkedHashMap();
  final private transient FunctionListExtractor functionListExtractor;
  FunctionNames(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
    this.functionListExtractor = new FunctionListExtractor(functionMap);
  }
  public Iterable<Node> getFunctionNodeList() {
    return functionMap.keySet();
  }
  public String getFunctionName(Node f) {
    FunctionRecord record = functionMap.get(f);
    if(record == null) {
      return null;
    }
    String str = record.name;
    if(str.isEmpty()) {
      str = "<anonymous>";
    }
    Node parent = record.parent;
    if(parent != null) {
      str = getFunctionName(parent) + "::" + str;
    }
    str = str.replaceAll("::this\\.", ".");
    str = str.replaceAll("\\.\\.", ".");
    str = str.replaceFirst("^(<anonymous>::)*", "");
    return str;
  }
  public int getFunctionId(Node f) {
    FunctionRecord record = functionMap.get(f);
    if(record != null) {
      return record.id;
    }
    else {
      return -1;
    }
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, functionListExtractor);
    FunctionExpressionNamer namer = new FunctionExpressionNamer(functionMap);
    AnonymousFunctionNamingCallback namingCallback = new AnonymousFunctionNamingCallback(namer);
    NodeTraversal.traverse(compiler, root, namingCallback);
  }
  
  private static class FunctionExpressionNamer implements AnonymousFunctionNamingCallback.FunctionNamer  {
    final private static char DELIMITER = '.';
    final private static NodeNameExtractor extractor = new NodeNameExtractor(DELIMITER);
    final private Map<Node, FunctionRecord> functionMap;
    FunctionExpressionNamer(Map<Node, FunctionRecord> functionMap) {
      super();
      this.functionMap = functionMap;
    }
    @Override() final public String getCombinedName(String lhs, String rhs) {
      return lhs + DELIMITER + rhs;
    }
    @Override() final public String getName(Node node) {
      return extractor.getName(node);
    }
    @Override() final public void setFunctionName(String name, Node fnNode) {
      FunctionRecord record = functionMap.get(fnNode);
      assert (record != null);
      String var_263 = record.name;
      assert (var_263.isEmpty());
      record.name = name;
    }
  }
  
  private static class FunctionListExtractor extends AbstractPostOrderCallback  {
    final private Map<Node, FunctionRecord> functionMap;
    private int nextId = 0;
    FunctionListExtractor(Map<Node, FunctionRecord> functionMap) {
      super();
      this.functionMap = functionMap;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isFunction()) {
        Node functionNameNode = n.getFirstChild();
        String functionName = functionNameNode.getString();
        Node enclosingFunction = t.getEnclosingFunction();
        functionMap.put(n, new FunctionRecord(nextId, enclosingFunction, functionName));
        nextId++;
      }
    }
  }
  
  private static class FunctionRecord implements Serializable  {
    final private static long serialVersionUID = 1L;
    final public int id;
    final public Node parent;
    public String name;
    FunctionRecord(int id, Node parent, String name) {
      super();
      this.id = id;
      this.parent = parent;
      this.name = name;
    }
  }
}