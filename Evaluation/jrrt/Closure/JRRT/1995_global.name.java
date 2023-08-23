package com.google.javascript.jscomp;
import javax.annotation.Nullable;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AliasExternals implements CompilerPass  {
  final private static int DEFAULT_REQUIRED_USAGE = 4;
  private int requiredUsage = DEFAULT_REQUIRED_USAGE;
  final private static int MIN_PROP_SIZE = 4;
  final static String PROTOTYPE_PROPERTY_NAME = getArrayNotationNameFor("prototype");
  final private Map<String, Symbol> props = Maps.newHashMap();
  final private List<Node> accessors = Lists.newArrayList();
  final private List<Node> mutators = Lists.newArrayList();
  final private Map<Node, Node> replacementMap = new IdentityHashMap<Node, Node>();
  final private Map<String, Symbol> globals = Maps.newHashMap();
  final private AbstractCompiler compiler;
  final private JSModuleGraph moduleGraph;
  private Node defaultRoot;
  private Map<JSModule, Node> moduleRoots;
  final private Set<String> unaliasableGlobals = Sets.newHashSet("arguments", "eval", "NodeFilter", "JSCompiler_renameProperty");
  final private Set<String> aliasableGlobals = Sets.newHashSet();
  AliasExternals(AbstractCompiler compiler, JSModuleGraph moduleGraph) {
    this(compiler, moduleGraph, null, null);
  }
  AliasExternals(AbstractCompiler compiler, JSModuleGraph moduleGraph, @Nullable() String unaliasableGlobals, @Nullable() String aliasableGlobals) {
    super();
    this.compiler = compiler;
    this.moduleGraph = moduleGraph;
    if(!Strings.isNullOrEmpty(unaliasableGlobals) && !Strings.isNullOrEmpty(aliasableGlobals)) {
      throw new IllegalArgumentException("Cannot pass in both unaliasable and aliasable globals; you must " + "choose one or the other.");
    }
    if(!Strings.isNullOrEmpty(unaliasableGlobals)) {
      this.unaliasableGlobals.addAll(Arrays.asList(unaliasableGlobals.split(",")));
    }
    if(!Strings.isNullOrEmpty(aliasableGlobals)) {
      this.aliasableGlobals.addAll(Arrays.asList(aliasableGlobals.split(",")));
    }
    if(moduleGraph != null) {
      moduleRoots = Maps.newHashMap();
    }
  }
  private Node getAddingRoot(JSModule m) {
    if(m != null) {
      Node root = moduleRoots.get(m);
      if(root != null) {
        return root;
      }
      root = compiler.getNodeForCodeInsertion(m);
      if(root != null) {
        moduleRoots.put(m, root);
        return root;
      }
    }
    return defaultRoot;
  }
  private static String getArrayNotationNameFor(String prop) {
    return "$$PROP_" + prop;
  }
  private static String getMutatorFor(String prop) {
    return "SETPROP_" + prop;
  }
  private Symbol newSymbolForGlobalVar(Node name) {
    return new Symbol(name.getString(), name.getBooleanProp(Node.IS_CONSTANT_NAME));
  }
  private Symbol newSymbolForProperty(String name) {
    return new Symbol(name, false);
  }
  private void addAccessorPropName(String propName, Node root) {
    Node propValue = IR.string(propName);
    Node propNameNode = IR.name(getArrayNotationNameFor(propName));
    propNameNode.addChildToFront(propValue);
    Node var = IR.var(propNameNode);
    root.addChildToFront(var);
    compiler.reportCodeChange();
  }
  private void addGlobalAliasNode(Symbol global, Node root) {
    String globalName = global.name;
    Node globalValue = IR.name(global.name);
    globalValue.putBooleanProp(Node.IS_CONSTANT_NAME, global.isConstant);
    Node globalNameNode = IR.name("GLOBAL_" + globalName);
    globalNameNode.addChildToFront(globalValue);
    Node var = IR.var(globalNameNode);
    root.addChildToFront(var);
    compiler.reportCodeChange();
  }
  private void addMutatorFunction(String propName, Node root) {
    String functionName = getMutatorFor(propName);
    String localPropName = getMutatorFor(propName) + "$a";
    String localValueName = getMutatorFor(propName) + "$b";
    Node fnNode = IR.function(IR.name(functionName), IR.paramList(IR.name(localPropName), IR.name(localValueName)), IR.block(IR.returnNode(IR.assign(IR.getprop(IR.name(localPropName), IR.string(propName)), IR.name(localValueName)))));
    root.addChildToFront(fnNode);
    compiler.reportCodeChange();
  }
  private void aliasGlobals(Node externs, Node root) {
    NodeTraversal.traverse(compiler, externs, new GetGlobals());
    NodeTraversal.traverse(compiler, root, new GlobalGatherer());
    for (Symbol global : globals.values()) {
      if(global.mutatorCount > 0) {
        continue ;
      }
      String var_1995 = global.name;
      int currentBytes = var_1995.length() * global.accessorCount;
      int aliasedBytes = 8 + global.name.length() + 2 * global.accessorCount;
      if(aliasedBytes < currentBytes) {
        global.aliasAccessor = true;
      }
    }
    for (Symbol global : globals.values()) {
      for (Node globalUse : global.uses) {
        replaceGlobalUse(globalUse);
      }
      if(global.aliasAccessor) {
        addGlobalAliasNode(global, getAddingRoot(global.deepestModuleAccess));
      }
    }
  }
  private void aliasProperties(Node externs, Node root) {
    NodeTraversal.traverse(compiler, externs, new GetAliasableNames(aliasableGlobals));
    props.put("prototype", newSymbolForProperty("prototype"));
    NodeTraversal.traverse(compiler, root, new PropertyGatherer());
    for (Symbol prop : props.values()) {
      if(prop.name.length() >= MIN_PROP_SIZE) {
        if(prop.accessorCount >= requiredUsage) {
          prop.aliasAccessor = true;
        }
        if(prop.mutatorCount >= requiredUsage) {
          prop.aliasMutator = true;
        }
      }
    }
    for (Node propInfo : accessors) {
      replaceAccessor(propInfo);
    }
    for (Node propInfo : mutators) {
      replaceMutator(propInfo);
    }
    for (Symbol prop : props.values()) {
      if(prop.aliasAccessor) {
        addAccessorPropName(prop.name, getAddingRoot(prop.deepestModuleAccess));
      }
    }
    for (Symbol prop : props.values()) {
      if(prop.aliasMutator) {
        addMutatorFunction(prop.name, getAddingRoot(prop.deepestModuleMutate));
      }
    }
  }
  @Override() public void process(Node externs, Node root) {
    defaultRoot = root.getFirstChild();
    Preconditions.checkState(defaultRoot.isScript());
    aliasProperties(externs, root);
    aliasGlobals(externs, root);
  }
  private void replaceAccessor(Node getPropNode) {
    Node propNameNode = getPropNode.getLastChild();
    String propName = propNameNode.getString();
    if(props.get(propName).aliasAccessor) {
      Node propSrc = getPropNode.getFirstChild();
      getPropNode.removeChild(propSrc);
      Node newNameNode = IR.name(getArrayNotationNameFor(propName));
      Node elemNode = IR.getelem(propSrc, newNameNode);
      replaceNode(getPropNode.getParent(), getPropNode, elemNode);
      compiler.reportCodeChange();
    }
  }
  private void replaceGlobalUse(Node globalUse) {
    String globalName = globalUse.getString();
    if(globals.get(globalName).aliasAccessor) {
      globalUse.setString("GLOBAL_" + globalName);
      globalUse.putBooleanProp(Node.IS_CONSTANT_NAME, false);
      compiler.reportCodeChange();
    }
  }
  private void replaceMutator(Node getPropNode) {
    Node propNameNode = getPropNode.getLastChild();
    Node parentNode = getPropNode.getParent();
    Symbol prop = props.get(propNameNode.getString());
    if(prop.aliasMutator) {
      Node propSrc = getPropNode.getFirstChild();
      Node propDest = parentNode.getLastChild();
      getPropNode.removeChild(propSrc);
      getPropNode.removeChild(propNameNode);
      parentNode.removeChild(propDest);
      Node callName = IR.name(getMutatorFor(propNameNode.getString()));
      Node call = IR.call(callName, propSrc, propDest);
      call.putBooleanProp(Node.FREE_CALL, true);
      replaceNode(parentNode.getParent(), parentNode, call);
      compiler.reportCodeChange();
    }
  }
  private void replaceNode(Node parent, Node before, Node after) {
    if(replacementMap.containsKey(parent)) {
      parent = replacementMap.get(parent);
    }
    parent.replaceChild(before, after);
    replacementMap.put(before, after);
  }
  public void setRequiredUsage(int usage) {
    this.requiredUsage = usage;
  }
  
  private class GetAliasableNames extends AbstractPostOrderCallback  {
    final private Set<String> whitelist;
    public GetAliasableNames(final Set<String> whitelist) {
      super();
      this.whitelist = whitelist;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.GETPROP:
        case Token.GETELEM:
        Node dest = n.getFirstChild().getNext();
        if(dest.isString() && (whitelist.isEmpty() || whitelist.contains(dest.getString()))) {
          props.put(dest.getString(), newSymbolForProperty(dest.getString()));
        }
      }
    }
  }
  
  private class GetGlobals extends NodeTraversal.AbstractShallowCallback  {
    private void getGlobalName(NodeTraversal t, Node dest, Node parent) {
      if(dest.isName()) {
        JSDocInfo docInfo = dest.getJSDocInfo() == null ? parent.getJSDocInfo() : dest.getJSDocInfo();
        boolean aliasable = !unaliasableGlobals.contains(dest.getString()) && (docInfo == null || !docInfo.isNoAlias());
        if(aliasable) {
          String name = dest.getString();
          Scope.Var var = t.getScope().getVar(name);
          if(var != null && !var.isLocal()) {
            globals.put(name, newSymbolForGlobalVar(dest));
          }
        }
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.FUNCTION:
        getGlobalName(t, n.getFirstChild(), n);
        break ;
        case Token.VAR:
        for(com.google.javascript.rhino.Node varChild = n.getFirstChild(); varChild != null; varChild = varChild.getNext()) {
          getGlobalName(t, varChild, n);
        }
        break ;
      }
    }
  }
  
  final private class GlobalGatherer extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isName()) {
        String name = n.getString();
        Scope.Var var = t.getScope().getVar(name);
        if(var != null && var.isLocal()) {
          return ;
        }
        Symbol global = globals.get(name);
        if(global != null) {
          if(n.getParent().isVar() || n.getParent().isFunction()) {
            globals.remove(name);
          }
          boolean isFirst = parent.getFirstChild() == n;
          if((NodeUtil.isAssignmentOp(parent) && isFirst) || (parent.isNew() && isFirst) || parent.isInc() || parent.isDec()) {
            global.recordMutator(t);
          }
          else {
            global.recordAccessor(t);
          }
          global.uses.add(n);
        }
      }
    }
  }
  
  final private class PropertyGatherer extends AbstractPostOrderCallback  {
    private boolean canReplaceWithGetProp(Node propNameNode, Node getPropNode, Node parent) {
      boolean isCallTarget = (parent.isCall()) && (parent.getFirstChild() == getPropNode);
      boolean isAssignTarget = NodeUtil.isAssignmentOp(parent) && (parent.getFirstChild() == getPropNode);
      boolean isIncOrDec = (parent.isInc()) || (parent.isDec());
      return (propNameNode.isString()) && !isAssignTarget && (!isCallTarget || !"eval".equals(propNameNode.getString())) && !isIncOrDec && props.containsKey(propNameNode.getString());
    }
    private boolean canReplaceWithSetProp(Node propNameNode, Node getPropNode, Node parent) {
      boolean isAssignTarget = (parent.isAssign()) && (parent.getFirstChild() == getPropNode);
      return (propNameNode.isString()) && isAssignTarget && props.containsKey(propNameNode.getString());
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isGetProp()) {
        Node propNameNode = n.getLastChild();
        if(canReplaceWithGetProp(propNameNode, n, parent)) {
          String name = propNameNode.getString();
          props.get(name).recordAccessor(t);
          accessors.add(n);
        }
        if(canReplaceWithSetProp(propNameNode, n, parent)) {
          String name = propNameNode.getString();
          props.get(name).recordMutator(t);
          mutators.add(n);
        }
      }
    }
  }
  
  private class Symbol  {
    final public String name;
    public int accessorCount = 0;
    public int mutatorCount = 0;
    public boolean aliasMutator = false;
    public boolean aliasAccessor = false;
    final public boolean isConstant;
    JSModule deepestModuleAccess = null;
    JSModule deepestModuleMutate = null;
    List<Node> uses = Lists.newArrayList();
    private Symbol(String name, boolean isConstant) {
      super();
      this.name = name;
      this.isConstant = isConstant;
    }
    void recordAccessor(NodeTraversal t) {
      accessorCount++;
      if(moduleGraph != null) {
        deepestModuleAccess = (deepestModuleAccess == null) ? t.getModule() : moduleGraph.getDeepestCommonDependencyInclusive(t.getModule(), deepestModuleAccess);
      }
    }
    void recordMutator(NodeTraversal t) {
      mutatorCount++;
      if(moduleGraph != null) {
        deepestModuleMutate = (deepestModuleMutate == null) ? t.getModule() : moduleGraph.getDeepestCommonDependencyInclusive(t.getModule(), deepestModuleMutate);
      }
    }
  }
}