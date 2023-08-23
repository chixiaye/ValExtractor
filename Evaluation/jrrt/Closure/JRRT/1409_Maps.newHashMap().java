package com.google.javascript.jscomp;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class ExternExportsPass extends NodeTraversal.AbstractPostOrderCallback implements CompilerPass  {
  final static DiagnosticType EXPORTED_FUNCTION_UNKNOWN_PARAMETER_TYPE = DiagnosticType.warning("JSC_EXPORTED_FUNCTION_UNKNOWN_PARAMETER_TYPE", "Unable to determine type of parameter {0} for exported function {1}");
  final static DiagnosticType EXPORTED_FUNCTION_UNKNOWN_RETURN_TYPE = DiagnosticType.warning("JSC_EXPORTED_FUNCTION_UNKNOWN_RETURN_TYPE", "Unable to determine return type for exported function {0}");
  final private List<Export> exports;
  final private Map<String, Node> definitionMap;
  final private AbstractCompiler compiler;
  final private Node externsRoot;
  final private Map<String, String> mappedPaths;
  final private Set<String> alreadyExportedPaths;
  private List<String> exportSymbolFunctionNames;
  private List<String> exportPropertyFunctionNames;
  ExternExportsPass(AbstractCompiler compiler) {
    super();
    this.exports = Lists.newArrayList();
    this.compiler = compiler;
    java.util.HashMap<String, Node> var_1409 = Maps.newHashMap();
    this.definitionMap = var_1409;
    this.externsRoot = IR.block();
    this.externsRoot.setIsSyntheticBlock(true);
    this.alreadyExportedPaths = Sets.newHashSet();
    this.mappedPaths = Maps.newHashMap();
    initExportMethods();
  }
  public String getGeneratedExterns() {
    CodePrinter.Builder builder = new CodePrinter.Builder(externsRoot).setPrettyPrint(true).setOutputTypes(true);
    return builder.build();
  }
  private void handlePropertyExport(Node parent) {
    if(parent.getChildCount() != 4) {
      return ;
    }
    Node thisNode = parent.getFirstChild();
    Node objectArg = thisNode.getNext();
    Node nameArg = objectArg.getNext();
    Node valueArg = nameArg.getNext();
    if(!objectArg.isQualifiedName()) {
      return ;
    }
    if(!nameArg.isString()) {
      return ;
    }
    this.exports.add(new PropertyExport(objectArg.getQualifiedName(), nameArg.getString(), valueArg));
  }
  private void handleSymbolExport(Node parent) {
    if(parent.getChildCount() != 3) {
      return ;
    }
    Node thisNode = parent.getFirstChild();
    Node nameArg = thisNode.getNext();
    Node valueArg = nameArg.getNext();
    if(!nameArg.isString()) {
      return ;
    }
    this.exports.add(new SymbolExport(nameArg.getString(), valueArg));
  }
  private void initExportMethods() {
    exportSymbolFunctionNames = Lists.newArrayList();
    exportPropertyFunctionNames = Lists.newArrayList();
    CodingConvention convention = compiler.getCodingConvention();
    exportSymbolFunctionNames.add(convention.getExportSymbolFunction());
    exportPropertyFunctionNames.add(convention.getExportPropertyFunction());
    exportSymbolFunctionNames.add("google_exportSymbol");
    exportPropertyFunctionNames.add("google_exportProperty");
  }
  @Override() public void process(Node externs, Node root) {
    new NodeTraversal(compiler, this).traverse(root);
    Set<Export> sorted = new TreeSet<Export>(new Comparator<Export>() {
        @Override() public int compare(Export e1, Export e2) {
          return e1.getExportedPath().compareTo(e2.getExportedPath());
        }
    });
    sorted.addAll(exports);
    for (Export export : sorted) {
      export.generateExterns();
    }
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()){
      case Token.NAME:
      case Token.GETPROP:
      String name = n.getQualifiedName();
      if(name == null) {
        return ;
      }
      if(parent.isAssign() || parent.isVar()) {
        definitionMap.put(name, parent);
      }
      if(!parent.isCall()) {
        return ;
      }
      if(exportPropertyFunctionNames.contains(name)) {
        handlePropertyExport(parent);
      }
      if(exportSymbolFunctionNames.contains(name)) {
        handleSymbolExport(parent);
      }
    }
  }
  
  abstract private class Export  {
    final protected String symbolName;
    final protected Node value;
    Export(String symbolName, Node value) {
      super();
      this.symbolName = symbolName;
      this.value = value;
    }
    private List<String> computePathPrefixes(String path) {
      List<String> pieces = Lists.newArrayList(path.split("\\."));
      List<String> pathPrefixes = Lists.newArrayList();
      for(int i = 0; i < pieces.size(); i++) {
        pathPrefixes.add(Joiner.on(".").join(Iterables.limit(pieces, i + 1)));
      }
      return pathPrefixes;
    }
    private Node createExternFunction(Node exportedFunction) {
      Node paramList = NodeUtil.getFunctionParameters(exportedFunction).cloneTree();
      Node externFunction = IR.function(IR.name(""), paramList, IR.block());
      checkForFunctionsWithUnknownTypes(exportedFunction);
      externFunction.setJSType(exportedFunction.getJSType());
      return externFunction;
    }
    private Node createExternObjectLit(Node exportedObjectLit) {
      Node lit = IR.objectlit();
      lit.setJSType(exportedObjectLit.getJSType());
      lit.setJSDocInfo(new JSDocInfo());
      int index = 1;
      for(com.google.javascript.rhino.Node child = exportedObjectLit.getFirstChild(); child != null; child = child.getNext()) {
        if(child.isStringKey()) {
          lit.addChildToBack(IR.propdef(IR.stringKey(child.getString()), IR.number(index++)));
        }
      }
      return lit;
    }
    protected Node getValue(Node qualifiedNameNode) {
      String qualifiedName = value.getQualifiedName();
      if(qualifiedName == null) {
        return null;
      }
      Node definitionParent = definitionMap.get(qualifiedName);
      if(definitionParent == null) {
        return null;
      }
      Node definition;
      switch (definitionParent.getType()){
        case Token.ASSIGN:
        definition = definitionParent.getLastChild();
        break ;
        case Token.VAR:
        definition = definitionParent.getLastChild().getLastChild();
        break ;
        default:
        return null;
      }
      if(!definition.isFunction() && !definition.isObjectLit()) {
        return null;
      }
      return definition;
    }
    abstract String getExportedPath();
    void appendExtern(String path, Node valueToExport) {
      List<String> pathPrefixes = computePathPrefixes(path);
      for(int i = 0; i < pathPrefixes.size(); ++i) {
        String pathPrefix = pathPrefixes.get(i);
        boolean isCompletePathPrefix = (i == pathPrefixes.size() - 1);
        boolean skipPathPrefix = pathPrefix.endsWith(".prototype") || (alreadyExportedPaths.contains(pathPrefix) && !isCompletePathPrefix);
        if(!skipPathPrefix) {
          Node initializer;
          if(isCompletePathPrefix && valueToExport != null) {
            if(valueToExport.isFunction()) {
              initializer = createExternFunction(valueToExport);
            }
            else {
              Preconditions.checkState(valueToExport.isObjectLit());
              initializer = createExternObjectLit(valueToExport);
            }
          }
          else {
            initializer = IR.empty();
          }
          appendPathDefinition(pathPrefix, initializer);
        }
      }
    }
    private void appendPathDefinition(String path, Node initializer) {
      Node pathDefinition;
      if(!path.contains(".")) {
        if(initializer.isEmpty()) {
          pathDefinition = IR.var(IR.name(path));
        }
        else {
          pathDefinition = NodeUtil.newVarNode(path, initializer);
        }
      }
      else {
        Node qualifiedPath = NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), path);
        if(initializer.isEmpty()) {
          pathDefinition = NodeUtil.newExpr(qualifiedPath);
        }
        else {
          pathDefinition = NodeUtil.newExpr(IR.assign(qualifiedPath, initializer));
        }
      }
      externsRoot.addChildToBack(pathDefinition);
      alreadyExportedPaths.add(path);
    }
    private void checkForFunctionsWithUnknownTypes(Node function) {
      Preconditions.checkArgument(function.isFunction());
      FunctionType functionType = JSType.toMaybeFunctionType(function.getJSType());
      if(functionType == null) {
        return ;
      }
      JSDocInfo functionJSDocInfo = functionType.getJSDocInfo();
      JSType returnType = functionType.getReturnType();
      if(!functionType.isConstructor() && (returnType == null || returnType.isUnknownType())) {
        reportUnknownReturnType(function);
      }
      Node astParameterIterator = NodeUtil.getFunctionParameters(function).getFirstChild();
      Node typeParameterIterator = functionType.getParametersNode().getFirstChild();
      while(astParameterIterator != null){
        JSType parameterType = typeParameterIterator.getJSType();
        if(parameterType == null || parameterType.isUnknownType()) {
          reportUnknownParameterType(function, astParameterIterator);
        }
        astParameterIterator = astParameterIterator.getNext();
        typeParameterIterator = typeParameterIterator.getNext();
      }
    }
    void generateExterns() {
      appendExtern(getExportedPath(), getValue(value));
    }
    private void reportUnknownParameterType(Node function, Node parameter) {
      compiler.report(JSError.make(NodeUtil.getSourceName(function), parameter, CheckLevel.WARNING, EXPORTED_FUNCTION_UNKNOWN_PARAMETER_TYPE, NodeUtil.getFunctionName(function), parameter.getString()));
    }
    private void reportUnknownReturnType(Node function) {
      compiler.report(JSError.make(NodeUtil.getSourceName(function), function, CheckLevel.WARNING, EXPORTED_FUNCTION_UNKNOWN_RETURN_TYPE, NodeUtil.getFunctionName(function)));
    }
  }
  
  private class PropertyExport extends Export  {
    final private String exportPath;
    public PropertyExport(String exportPath, String symbolName, Node value) {
      super(symbolName, value);
      this.exportPath = exportPath;
    }
    @Override() String getExportedPath() {
      List<String> pieces = Lists.newArrayList(exportPath.split("\\."));
      for(int i = pieces.size(); i > 0; i--) {
        String cPath = Joiner.on(".").join(Iterables.limit(pieces, i));
        if(mappedPaths.containsKey(cPath)) {
          String newPath = mappedPaths.get(cPath);
          if(i < pieces.size()) {
            newPath += "." + Joiner.on(".").join(Iterables.skip(pieces, i));
          }
          return newPath + "." + symbolName;
        }
      }
      return exportPath + "." + symbolName;
    }
  }
  
  private class SymbolExport extends Export  {
    public SymbolExport(String symbolName, Node value) {
      super(symbolName, value);
      String qualifiedName = value.getQualifiedName();
      if(qualifiedName != null) {
        mappedPaths.put(qualifiedName, symbolName);
      }
    }
    @Override() String getExportedPath() {
      return symbolName;
    }
  }
}