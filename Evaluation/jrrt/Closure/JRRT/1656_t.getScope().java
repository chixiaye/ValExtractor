package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.graph.FixedPointGraphTraversal;
import com.google.javascript.jscomp.graph.LinkedDirectedGraph;
import com.google.javascript.jscomp.graph.FixedPointGraphTraversal.EdgeCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

class AnalyzePrototypeProperties implements CompilerPass  {
  final private SymbolType PROPERTY = SymbolType.PROPERTY;
  final private SymbolType VAR = SymbolType.VAR;
  final private AbstractCompiler compiler;
  final private boolean canModifyExterns;
  final private boolean anchorUnusedVars;
  final private JSModuleGraph moduleGraph;
  final private JSModule firstModule;
  final private static Set<String> IMPLICITLY_USED_PROPERTIES = ImmutableSet.of("length", "toString", "valueOf");
  final private LinkedDirectedGraph<NameInfo, JSModule> symbolGraph = LinkedDirectedGraph.createWithoutAnnotations();
  final private NameInfo globalNode = new NameInfo("[global]");
  final private NameInfo externNode = new NameInfo("[extern]");
  final private NameInfo anonymousNode = new NameInfo("[anonymous]");
  final private Map<String, NameInfo> propertyNameInfo = Maps.newHashMap();
  final private Map<String, NameInfo> varNameInfo = Maps.newHashMap();
  AnalyzePrototypeProperties(AbstractCompiler compiler, JSModuleGraph moduleGraph, boolean canModifyExterns, boolean anchorUnusedVars) {
    super();
    this.compiler = compiler;
    this.moduleGraph = moduleGraph;
    this.canModifyExterns = canModifyExterns;
    this.anchorUnusedVars = anchorUnusedVars;
    if(moduleGraph != null) {
      firstModule = moduleGraph.getRootModule();
    }
    else {
      firstModule = null;
    }
    globalNode.markReference(null);
    externNode.markReference(null);
    symbolGraph.createNode(globalNode);
    symbolGraph.createNode(externNode);
    for (String property : IMPLICITLY_USED_PROPERTIES) {
      NameInfo nameInfo = getNameInfoForName(property, PROPERTY);
      if(moduleGraph == null) {
        symbolGraph.connect(externNode, null, nameInfo);
      }
      else {
        for (JSModule module : moduleGraph.getAllModules()) {
          symbolGraph.connect(externNode, module, nameInfo);
        }
      }
    }
  }
  public Collection<NameInfo> getAllNameInfo() {
    List<NameInfo> result = Lists.newArrayList(propertyNameInfo.values());
    result.addAll(varNameInfo.values());
    return result;
  }
  private NameInfo getNameInfoForName(String name, SymbolType type) {
    Map<String, NameInfo> map = type == PROPERTY ? propertyNameInfo : varNameInfo;
    if(map.containsKey(name)) {
      return map.get(name);
    }
    else {
      NameInfo nameInfo = new NameInfo(name);
      map.put(name, nameInfo);
      symbolGraph.createNode(nameInfo);
      return nameInfo;
    }
  }
  @Override() public void process(Node externRoot, Node root) {
    if(!canModifyExterns) {
      NodeTraversal.traverse(compiler, externRoot, new ProcessExternProperties());
    }
    NodeTraversal.traverse(compiler, root, new ProcessProperties());
    FixedPointGraphTraversal<NameInfo, JSModule> t = FixedPointGraphTraversal.newTraversal(new PropagateReferences());
    t.computeFixedPoint(symbolGraph, Sets.newHashSet(externNode, globalNode));
  }
  
  static class AssignmentProperty implements Property  {
    final private Node exprNode;
    final private Var rootVar;
    final private JSModule module;
    AssignmentProperty(Node node, Var rootVar, JSModule module) {
      super();
      this.exprNode = node;
      this.rootVar = rootVar;
      this.module = module;
    }
    @Override() public JSModule getModule() {
      return module;
    }
    private Node getAssignNode() {
      return exprNode.getFirstChild();
    }
    @Override() public Node getPrototype() {
      return getAssignNode().getFirstChild().getFirstChild();
    }
    @Override() public Node getValue() {
      return getAssignNode().getLastChild();
    }
    @Override() public Var getRootVar() {
      return rootVar;
    }
    @Override() public void remove() {
      NodeUtil.removeChild(exprNode.getParent(), exprNode);
    }
  }
  
  class GlobalFunction implements Symbol  {
    final private Node nameNode;
    final private Var var;
    final private JSModule module;
    GlobalFunction(Node nameNode, Var var, JSModule module) {
      super();
      Node parent = nameNode.getParent();
      Preconditions.checkState(parent.isVar() || NodeUtil.isFunctionDeclaration(parent));
      this.nameNode = nameNode;
      this.var = var;
      this.module = module;
    }
    @Override() public JSModule getModule() {
      return module;
    }
    public Node getFunctionNode() {
      Node parent = nameNode.getParent();
      if(parent.isFunction()) {
        return parent;
      }
      else {
        return nameNode.getChildAtIndex(1);
      }
    }
    @Override() public Var getRootVar() {
      return var;
    }
    @Override() public void remove() {
      Node parent = nameNode.getParent();
      if(parent.isFunction() || parent.hasOneChild()) {
        NodeUtil.removeChild(parent.getParent(), parent);
      }
      else {
        Preconditions.checkState(parent.isVar());
        parent.removeChild(nameNode);
      }
    }
  }
  
  static class LiteralProperty implements Property  {
    final private Node key;
    final private Node value;
    final private Node map;
    final private Node assign;
    final private Var rootVar;
    final private JSModule module;
    LiteralProperty(Node key, Node value, Node map, Node assign, Var rootVar, JSModule module) {
      super();
      this.key = key;
      this.value = value;
      this.map = map;
      this.assign = assign;
      this.rootVar = rootVar;
      this.module = module;
    }
    @Override() public JSModule getModule() {
      return module;
    }
    @Override() public Node getPrototype() {
      return assign.getFirstChild();
    }
    @Override() public Node getValue() {
      return value;
    }
    @Override() public Var getRootVar() {
      return rootVar;
    }
    @Override() public void remove() {
      map.removeChild(key);
    }
  }
  
  private class NameContext  {
    final NameInfo name;
    final Scope scope;
    NameContext(NameInfo name, Scope scope) {
      super();
      this.name = name;
      this.scope = scope;
    }
  }
  
  class NameInfo  {
    final String name;
    private boolean referenced = false;
    final private Deque<Symbol> declarations = new ArrayDeque<Symbol>();
    private JSModule deepestCommonModuleRef = null;
    private boolean readClosureVariables = false;
    NameInfo(String name) {
      super();
      this.name = name;
    }
    Deque<Symbol> getDeclarations() {
      return declarations;
    }
    JSModule getDeepestCommonModuleRef() {
      return deepestCommonModuleRef;
    }
    @Override() public String toString() {
      return name;
    }
    boolean isReferenced() {
      return referenced;
    }
    boolean markReference(JSModule module) {
      boolean hasChanged = false;
      if(!referenced) {
        referenced = true;
        hasChanged = true;
      }
      if(moduleGraph != null) {
        JSModule originalDeepestCommon = deepestCommonModuleRef;
        if(deepestCommonModuleRef == null) {
          deepestCommonModuleRef = module;
        }
        else {
          deepestCommonModuleRef = moduleGraph.getDeepestCommonDependencyInclusive(deepestCommonModuleRef, module);
        }
        if(originalDeepestCommon != deepestCommonModuleRef) {
          hasChanged = true;
        }
      }
      return hasChanged;
    }
    boolean readsClosureVariables() {
      return readClosureVariables;
    }
  }
  
  private class ProcessExternProperties extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isGetProp()) {
        symbolGraph.connect(externNode, firstModule, getNameInfoForName(n.getLastChild().getString(), PROPERTY));
      }
    }
  }
  
  private class ProcessProperties implements NodeTraversal.ScopedCallback  {
    private Stack<NameContext> symbolStack = new Stack<NameContext>();
    private String getPrototypePropertyNameFromRValue(Node rValue) {
      Node lValue = NodeUtil.getBestLValue(rValue);
      if(lValue == null || lValue.getParent() == null || lValue.getParent().getParent() == null || !(NodeUtil.isObjectLitKey(lValue, lValue.getParent()) || NodeUtil.isExprAssign(lValue.getParent().getParent()))) {
        return null;
      }
      String lValueName = NodeUtil.getBestLValueName(NodeUtil.getBestLValue(rValue));
      if(lValueName == null) {
        return null;
      }
      int lastDot = lValueName.lastIndexOf('.');
      if(lastDot == -1) {
        return null;
      }
      String firstPart = lValueName.substring(0, lastDot);
      if(!firstPart.endsWith(".prototype")) {
        return null;
      }
      return lValueName.substring(lastDot + 1);
    }
    private String processNonFunctionPrototypeAssign(Node n, Node parent) {
      if(isAssignRValue(n, parent) && !n.isFunction()) {
        return getPrototypePropertyNameFromRValue(n);
      }
      return null;
    }
    private Var maybeGetVar(NodeTraversal t, Node maybeName) {
      return maybeName.isName() ? t.getScope().getVar(maybeName.getString()) : null;
    }
    private boolean isAssignRValue(Node n, Node parent) {
      return parent != null && parent.isAssign() && parent.getFirstChild() != n;
    }
    private boolean isGlobalFunctionDeclaration(NodeTraversal t, Node n) {
      Scope s = t.getScope();
      if(!(s.isGlobal() || s.getDepth() == 1 && s.getRootNode() == n)) {
        return false;
      }
      return NodeUtil.isFunctionDeclaration(n) || n.isFunction() && n.getParent().isName();
    }
    private boolean processGlobalFunctionDeclaration(NodeTraversal t, Node nameNode, Var v) {
      Node firstChild = nameNode.getFirstChild();
      Node parent = nameNode.getParent();
      if(isGlobalFunctionDeclaration(t, parent) || firstChild != null && isGlobalFunctionDeclaration(t, firstChild)) {
        String name = nameNode.getString();
        getNameInfoForName(name, VAR).getDeclarations().add(new GlobalFunction(nameNode, v, t.getModule()));
        if(compiler.getCodingConvention().isExported(name) || anchorUnusedVars) {
          addGlobalUseOfSymbol(name, t.getModule(), VAR);
        }
        return true;
      }
      return false;
    }
    private boolean processPrototypeRef(NodeTraversal t, Node ref) {
      Node root = NodeUtil.getRootOfQualifiedName(ref);
      Node n = ref.getParent();
      switch (n.getType()){
        case Token.GETPROP:
        Node dest = n.getFirstChild().getNext();
        Node parent = n.getParent();
        Node grandParent = parent.getParent();
        if(dest.isString() && NodeUtil.isExprAssign(grandParent) && NodeUtil.isVarOrSimpleAssignLhs(n, parent)) {
          String name = dest.getString();
          Property prop = new AssignmentProperty(grandParent, maybeGetVar(t, root), t.getModule());
          getNameInfoForName(name, PROPERTY).getDeclarations().add(prop);
          return true;
        }
        break ;
        case Token.ASSIGN:
        Node map = n.getFirstChild().getNext();
        if(map.isObjectLit()) {
          for(com.google.javascript.rhino.Node key = map.getFirstChild(); key != null; key = key.getNext()) {
            String name = key.getString();
            Property prop = new LiteralProperty(key, key.getFirstChild(), map, n, maybeGetVar(t, root), t.getModule());
            getNameInfoForName(name, PROPERTY).getDeclarations().add(prop);
          }
          return true;
        }
        break ;
      }
      return false;
    }
    @Override() public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      String propName = processNonFunctionPrototypeAssign(n, parent);
      if(propName != null) {
        symbolStack.push(new NameContext(getNameInfoForName(propName, PROPERTY), null));
      }
      return true;
    }
    private void addGlobalUseOfSymbol(String name, JSModule module, SymbolType type) {
      symbolGraph.connect(globalNode, module, getNameInfoForName(name, type));
    }
    private void addSymbolUse(String name, JSModule module, SymbolType type) {
      NameInfo info = getNameInfoForName(name, type);
      NameInfo def = null;
      for(int i = symbolStack.size() - 1; i >= 0; i--) {
        def = symbolStack.get(i).name;
        if(def != anonymousNode) {
          break ;
        }
      }
      if(!def.equals(info)) {
        symbolGraph.connect(def, module, info);
      }
    }
    @Override() public void enterScope(NodeTraversal t) {
      Node n = t.getCurrentNode();
      if(n.isFunction()) {
        String propName = getPrototypePropertyNameFromRValue(n);
        if(propName != null) {
          symbolStack.push(new NameContext(getNameInfoForName(propName, PROPERTY), t.getScope()));
        }
        else 
          if(isGlobalFunctionDeclaration(t, n)) {
            Node parent = n.getParent();
            String name = parent.isName() ? parent.getString() : n.getFirstChild().getString();
            symbolStack.push(new NameContext(getNameInfoForName(name, VAR), t.getScope()));
          }
          else {
            symbolStack.push(new NameContext(anonymousNode, t.getScope()));
          }
      }
      else {
        Preconditions.checkState(t.inGlobalScope());
        symbolStack.push(new NameContext(globalNode, t.getScope()));
      }
    }
    @Override() public void exitScope(NodeTraversal t) {
      symbolStack.pop();
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isGetProp()) {
        String propName = n.getFirstChild().getNext().getString();
        if(n.isQualifiedName()) {
          if(propName.equals("prototype")) {
            if(processPrototypeRef(t, n)) {
              return ;
            }
          }
          else 
            if(compiler.getCodingConvention().isExported(propName)) {
              addGlobalUseOfSymbol(propName, t.getModule(), PROPERTY);
              return ;
            }
            else {
              if(n.getParent().isAssign() && n.getNext() != null) {
                String rValueName = getPrototypePropertyNameFromRValue(n);
                if(rValueName != null) {
                  return ;
                }
              }
            }
        }
        addSymbolUse(propName, t.getModule(), PROPERTY);
      }
      else 
        if(n.isObjectLit()) {
          String lValueName = NodeUtil.getBestLValueName(NodeUtil.getBestLValue(n));
          if(lValueName != null && lValueName.endsWith(".prototype")) {
            return ;
          }
          for(com.google.javascript.rhino.Node propNameNode = n.getFirstChild(); propNameNode != null; propNameNode = propNameNode.getNext()) {
            if(!propNameNode.isQuotedString()) {
              addSymbolUse(propNameNode.getString(), t.getModule(), PROPERTY);
            }
          }
        }
        else 
          if(n.isName()) {
            String name = n.getString();
            Scope var_1656 = t.getScope();
            Var var = var_1656.getVar(name);
            if(var != null) {
              if(var.isGlobal()) {
                if(var.getInitialValue() != null && var.getInitialValue().isFunction()) {
                  if(t.inGlobalScope()) {
                    if(!processGlobalFunctionDeclaration(t, n, var)) {
                      addGlobalUseOfSymbol(name, t.getModule(), VAR);
                    }
                  }
                  else {
                    addSymbolUse(name, t.getModule(), VAR);
                  }
                }
              }
              else 
                if(var.getScope() != t.getScope()) {
                  for(int i = symbolStack.size() - 1; i >= 0; i--) {
                    NameContext context = symbolStack.get(i);
                    if(context.scope == var.getScope()) {
                      break ;
                    }
                    context.name.readClosureVariables = true;
                  }
                }
            }
          }
      if(processNonFunctionPrototypeAssign(n, parent) != null) {
        symbolStack.pop();
      }
    }
  }
  
  private class PropagateReferences implements EdgeCallback<NameInfo, JSModule>  {
    @Override() public boolean traverseEdge(NameInfo start, JSModule edge, NameInfo dest) {
      if(start.isReferenced()) {
        JSModule startModule = start.getDeepestCommonModuleRef();
        if(startModule != null && moduleGraph.dependsOn(startModule, edge)) {
          return dest.markReference(startModule);
        }
        else {
          return dest.markReference(edge);
        }
      }
      return false;
    }
  }
  
  interface Property extends Symbol  {
    Node getPrototype();
    Node getValue();
  }
  
  interface Symbol  {
    JSModule getModule();
    Var getRootVar();
    void remove();
  }
  private enum SymbolType {
    PROPERTY(),

    VAR(),

  ;
  private SymbolType() {
  }
  }
}