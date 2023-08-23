package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticReference;
import com.google.javascript.rhino.jstype.StaticScope;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import com.google.javascript.rhino.jstype.StaticSymbolTable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Scope implements StaticScope<JSType>, StaticSymbolTable<Scope.Var, Scope.Var>  {
  final private Map<String, Var> vars = new LinkedHashMap<String, Var>();
  final private Scope parent;
  final private int depth;
  final private Node rootNode;
  final private boolean isBottom;
  private Var arguments;
  final private static Predicate<Var> DECLARATIVELY_UNBOUND_VARS_WITHOUT_TYPES = new Predicate<Var>() {
      @Override() public boolean apply(Var var) {
        return var.getParentNode() != null && var.getType() == null && var.getParentNode().isVar() && !var.isExtern();
      }
  };
  private Scope(Node rootNode, boolean isBottom) {
    super();
    this.parent = null;
    this.rootNode = rootNode;
    this.isBottom = isBottom;
    this.depth = 0;
  }
  Scope(Scope parent, Node rootNode) {
    super();
    Preconditions.checkNotNull(parent);
    Preconditions.checkArgument(rootNode != parent.rootNode);
    this.parent = parent;
    this.rootNode = rootNode;
    this.isBottom = false;
    this.depth = parent.depth + 1;
  }
  @Override() public Iterable<Var> getAllSymbols() {
    return Collections.unmodifiableCollection(vars.values());
  }
  @Override() public Iterable<Var> getReferences(Var var) {
    return ImmutableList.of(var);
  }
  Iterable<Var> getVarIterable() {
    return vars.values();
  }
  public Iterator<Var> getDeclarativelyUnboundVarsWithoutTypes() {
    return Iterators.filter(getVars(), DECLARATIVELY_UNBOUND_VARS_WITHOUT_TYPES);
  }
  public Iterator<Var> getVars() {
    return vars.values().iterator();
  }
  @Override() public JSType getTypeOfThis() {
    if(isGlobal()) {
      return ObjectType.cast(rootNode.getJSType());
    }
    Preconditions.checkState(rootNode.isFunction());
    JSType nodeType = rootNode.getJSType();
    if(nodeType != null && nodeType.isFunctionType()) {
      return nodeType.toMaybeFunctionType().getTypeOfThis();
    }
    else {
      return parent.getTypeOfThis();
    }
  }
  @Override() public Node getRootNode() {
    return rootNode;
  }
  static Scope createGlobalScope(Node rootNode) {
    return new Scope(rootNode, false);
  }
  static Scope createLatticeBottom(Node rootNode) {
    return new Scope(rootNode, true);
  }
  Scope getGlobalScope() {
    Scope result = this;
    while(result.getParent() != null){
      result = result.getParent();
    }
    return result;
  }
  public Scope getParent() {
    return parent;
  }
  @Override() public StaticScope<JSType> getParentScope() {
    return parent;
  }
  @Override() public StaticScope<JSType> getScope(Var var) {
    return var.scope;
  }
  Var declare(String name, Node nameNode, JSType type, CompilerInput input) {
    return declare(name, nameNode, type, input, true);
  }
  Var declare(String name, Node nameNode, JSType type, CompilerInput input, boolean inferred) {
    Preconditions.checkState(name != null && name.length() > 0);
    Preconditions.checkState(vars.get(name) == null);
    Var var = new Var(inferred, name, nameNode, type, this, vars.size(), input);
    vars.put(name, var);
    return var;
  }
  public Var getArgumentsVar() {
    if(arguments == null) {
      arguments = new Arguments(this);
    }
    return arguments;
  }
  @Override() public Var getOwnSlot(String name) {
    return vars.get(name);
  }
  @Override() public Var getSlot(String name) {
    return getVar(name);
  }
  public Var getVar(String name) {
    Var var = vars.get(name);
    if(var != null) {
      return var;
    }
    else 
      if(parent != null) {
        return parent.getVar(name);
      }
      else {
        return null;
      }
  }
  boolean isBottom() {
    return isBottom;
  }
  public boolean isDeclared(String name, boolean recurse) {
    Scope scope = this;
    if(scope.vars.containsKey(name)) 
      return true;
    if(scope.parent != null && recurse) {
      return scope.parent.isDeclared(name, recurse);
    }
    return false;
  }
  public boolean isGlobal() {
    return parent == null;
  }
  public boolean isLocal() {
    return !isGlobal();
  }
  int getDepth() {
    return depth;
  }
  public int getVarCount() {
    return vars.size();
  }
  void undeclare(Var var) {
    Preconditions.checkState(var.scope == this);
    String var_1401 = var.name;
    Preconditions.checkState(vars.get(var_1401) == var);
    vars.remove(var.name);
  }
  
  public static class Arguments extends Var  {
    Arguments(Scope scope) {
      super(false, "arguments", null, null, scope, -1, null);
    }
    @Override() public boolean equals(Object other) {
      if(!(other instanceof Arguments)) {
        return false;
      }
      Arguments otherVar = (Arguments)other;
      return otherVar.scope.getRootNode() == scope.getRootNode();
    }
    @Override() public int hashCode() {
      return System.identityHashCode(this);
    }
  }
  
  public static class Var implements StaticSlot<JSType>, StaticReference<JSType>  {
    final String name;
    final Node nameNode;
    private JSType type;
    final private boolean typeInferred;
    final CompilerInput input;
    final int index;
    final Scope scope;
    private boolean markedEscaped = false;
    private boolean markedAssignedExactlyOnce = false;
    private Var(boolean inferred, String name, Node nameNode, JSType type, Scope scope, int index, CompilerInput input) {
      super();
      this.name = name;
      this.nameNode = nameNode;
      this.type = type;
      this.scope = scope;
      this.index = index;
      this.input = input;
      this.typeInferred = inferred;
    }
    CompilerInput getInput() {
      return input;
    }
    @Override() public JSDocInfo getJSDocInfo() {
      return nameNode == null ? null : NodeUtil.getBestJSDocInfo(nameNode);
    }
    @Override() public JSType getType() {
      return type;
    }
    public Node getInitialValue() {
      return NodeUtil.getRValueOfLValue(nameNode);
    }
    public Node getNameNode() {
      return nameNode;
    }
    @Override() public Node getNode() {
      return nameNode;
    }
    public Node getParentNode() {
      return nameNode == null ? null : nameNode.getParent();
    }
    Scope getScope() {
      return scope;
    }
    @Override() public StaticSourceFile getSourceFile() {
      return nameNode.getStaticSourceFile();
    }
    public String getInputName() {
      if(input == null) 
        return "<non-file>";
      else 
        return input.getName();
    }
    @Override() public String getName() {
      return name;
    }
    @Override() public String toString() {
      return "Scope.Var " + name + "{" + type + "}";
    }
    @Override() public Var getDeclaration() {
      return nameNode == null ? null : this;
    }
    @Override() public Var getSymbol() {
      return this;
    }
    @Override() public boolean equals(Object other) {
      if(!(other instanceof Var)) {
        return false;
      }
      Var otherVar = (Var)other;
      return otherVar.nameNode == nameNode;
    }
    public boolean isBleedingFunction() {
      return NodeUtil.isFunctionExpression(getParentNode());
    }
    public boolean isConst() {
      return nameNode != null && NodeUtil.isConstantName(nameNode);
    }
    public boolean isDefine() {
      JSDocInfo info = getJSDocInfo();
      return info != null && info.isDefine();
    }
    boolean isExtern() {
      return input == null || input.isExtern();
    }
    public boolean isGlobal() {
      return scope.isGlobal();
    }
    public boolean isLocal() {
      return scope.isLocal();
    }
    boolean isMarkedAssignedExactlyOnce() {
      return markedAssignedExactlyOnce;
    }
    boolean isMarkedEscaped() {
      return markedEscaped;
    }
    public boolean isNoShadow() {
      JSDocInfo info = getJSDocInfo();
      return info != null && info.isNoShadow();
    }
    @Override() public boolean isTypeInferred() {
      return typeInferred;
    }
    @Override() public int hashCode() {
      return nameNode.hashCode();
    }
    void markAssignedExactlyOnce() {
      markedAssignedExactlyOnce = true;
    }
    void markEscaped() {
      markedEscaped = true;
    }
    void resolveType(ErrorReporter errorReporter) {
      if(type != null) {
        type = type.resolve(errorReporter, scope);
      }
    }
    void setType(JSType type) {
      Preconditions.checkState(isTypeInferred());
      this.type = type;
    }
  }
}