package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Marker;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.SimpleReference;
import com.google.javascript.rhino.jstype.SimpleSlot;
import com.google.javascript.rhino.jstype.StaticReference;
import com.google.javascript.rhino.jstype.StaticScope;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.StaticSymbolTable;
import com.google.javascript.rhino.jstype.UnionType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final public class SymbolTable implements StaticSymbolTable<SymbolTable.Symbol, SymbolTable.Reference>  {
  final private static Logger logger = Logger.getLogger(SymbolTable.class.getName());
  final public static String GLOBAL_THIS = "*global*";
  final private Table<Node, String, Symbol> symbols = HashBasedTable.create();
  final private Map<Node, SymbolScope> scopes = Maps.newLinkedHashMap();
  final private List<JSDocInfo> docInfos = Lists.newArrayList();
  private SymbolScope globalScope = null;
  final private JSTypeRegistry registry;
  final private Ordering<String> SOURCE_NAME_ORDERING = Ordering.natural().nullsFirst();
  final private Ordering<Node> NODE_ORDERING = new Ordering<Node>() {
      @Override() public int compare(Node a, Node b) {
        int result = SOURCE_NAME_ORDERING.compare(a.getSourceFileName(), b.getSourceFileName());
        if(result != 0) {
          return result;
        }
        return a.getSourcePosition() - b.getSourcePosition();
      }
  };
  final private Ordering<SymbolScope> LEXICAL_SCOPE_ORDERING = new Ordering<SymbolScope>() {
      @Override() public int compare(SymbolScope a, SymbolScope b) {
        Preconditions.checkState(a.isLexicalScope() && b.isLexicalScope(), "We can only sort lexical scopes");
        return NODE_ORDERING.compare(a.getRootNode(), b.getRootNode());
      }
  };
  final private Ordering<Symbol> SYMBOL_ORDERING = new Ordering<Symbol>() {
      @Override() public int compare(Symbol a, Symbol b) {
        SymbolScope scopeA = getScope(a);
        SymbolScope scopeB = getScope(b);
        int result = getLexicalScopeDepth(scopeA) - getLexicalScopeDepth(scopeB);
        if(result != 0) {
          return result;
        }
        return a.getName().compareTo(b.getName());
      }
  };
  SymbolTable(JSTypeRegistry registry) {
    super();
    this.registry = registry;
  }
  public Collection<JSDocInfo> getAllJSDocInfo() {
    return Collections.unmodifiableList(docInfos);
  }
  public Collection<SymbolScope> getAllScopes() {
    return Collections.unmodifiableCollection(scopes.values());
  }
  @Override() public Iterable<Reference> getReferences(Symbol symbol) {
    return Collections.unmodifiableCollection(symbol.references.values());
  }
  @Override() public Iterable<Symbol> getAllSymbols() {
    return Collections.unmodifiableCollection(symbols.values());
  }
  public Iterable<Symbol> getAllSymbolsForTypeOf(Symbol sym) {
    return getAllSymbolsForType(sym.getType());
  }
  public List<Reference> getReferenceList(Symbol symbol) {
    return ImmutableList.copyOf(symbol.references.values());
  }
  public List<Symbol> getAllSymbolsForType(JSType type) {
    if(type == null) {
      return ImmutableList.of();
    }
    UnionType unionType = type.toMaybeUnionType();
    if(unionType != null) {
      List<Symbol> result = Lists.newArrayListWithExpectedSize(2);
      for (JSType alt : unionType.getAlternates()) {
        Symbol altSym = getSymbolForTypeHelper(alt, true);
        if(altSym != null) {
          result.add(altSym);
        }
      }
      return result;
    }
    Symbol result = getSymbolForTypeHelper(type, true);
    return result == null ? ImmutableList.<Symbol>of() : ImmutableList.of(result);
  }
  public List<Symbol> getAllSymbolsSorted() {
    List<Symbol> sortedSymbols = Lists.newArrayList(symbols.values());
    Collections.sort(sortedSymbols, getNaturalSymbolOrdering());
    return sortedSymbols;
  }
  public Ordering<Symbol> getNaturalSymbolOrdering() {
    return SYMBOL_ORDERING;
  }
  private  <R extends com.google.javascript.rhino.jstype.StaticReference<com.google.javascript.rhino.jstype.JSType>, S extends com.google.javascript.rhino.jstype.StaticSlot<com.google.javascript.rhino.jstype.JSType>> StaticReference<JSType> findBestDeclToAdd(StaticSymbolTable<S, R> otherSymbolTable, S slot) {
    StaticReference<JSType> decl = slot.getDeclaration();
    if(isGoodRefToAdd(decl)) {
      return decl;
    }
    for (R ref : otherSymbolTable.getReferences(slot)) {
      if(isGoodRefToAdd(ref)) {
        return ref;
      }
    }
    return null;
  }
  public String toDebugString() {
    StringBuilder builder = new StringBuilder();
    for (Symbol symbol : getAllSymbols()) {
      toDebugString(builder, symbol);
    }
    return builder.toString();
  }
  private Symbol addSymbol(String name, JSType type, boolean inferred, SymbolScope scope, Node declNode) {
    Symbol symbol = new Symbol(name, type, inferred, scope);
    Symbol replacedSymbol = symbols.put(declNode, name, symbol);
    Preconditions.checkState(replacedSymbol == null, "Found duplicate symbol %s in global index. Type %s", name, type);
    replacedSymbol = scope.ownSymbols.put(name, symbol);
    Preconditions.checkState(replacedSymbol == null, "Found duplicate symbol %s in its scope. Type %s", name, type);
    return symbol;
  }
  private Symbol copySymbolTo(StaticSlot<JSType> sym, SymbolScope scope) {
    return copySymbolTo(sym, sym.getDeclaration().getNode(), scope);
  }
  private Symbol copySymbolTo(StaticSlot<JSType> sym, Node declNode, SymbolScope scope) {
    Preconditions.checkNotNull(declNode);
    return declareSymbol(sym.getName(), sym.getType(), sym.isTypeInferred(), scope, declNode, sym.getJSDocInfo());
  }
  public Symbol declareInferredSymbol(SymbolScope scope, String name, Node declNode) {
    return declareSymbol(name, null, true, scope, declNode, null);
  }
  private Symbol declareSymbol(String name, JSType type, boolean inferred, SymbolScope scope, Node declNode, JSDocInfo info) {
    Symbol symbol = addSymbol(name, type, inferred, scope, declNode);
    symbol.setJSDocInfo(info);
    symbol.setDeclaration(symbol.defineReferenceAt(declNode));
    return symbol;
  }
  private Symbol findSymbolForScope(SymbolScope scope) {
    Node rootNode = scope.getRootNode();
    if(rootNode.getParent() == null) {
      return globalScope.getSlot(GLOBAL_THIS);
    }
    if(!rootNode.isFunction()) {
      return null;
    }
    String name = NodeUtil.getBestLValueName(NodeUtil.getBestLValue(rootNode));
    return name == null ? null : scope.getParentScope().getQualifiedSlot(name);
  }
  public Symbol getParameterInFunction(Symbol sym, String paramName) {
    SymbolScope scope = getScopeInFunction(sym);
    if(scope != null) {
      Symbol param = scope.getSlot(paramName);
      if(param != null && param.scope == scope) {
        return param;
      }
    }
    return null;
  }
  public Symbol getSymbolDeclaredBy(EnumType enumType) {
    return getSymbolForName(null, enumType.getElementsType().getReferenceName());
  }
  public Symbol getSymbolDeclaredBy(FunctionType fn) {
    Preconditions.checkState(fn.isConstructor() || fn.isInterface());
    ObjectType instanceType = fn.getInstanceType();
    return getSymbolForName(fn.getSource(), instanceType.getReferenceName());
  }
  public Symbol getSymbolForInstancesOf(Symbol sym) {
    FunctionType fn = sym.getFunctionType();
    if(fn != null && fn.isNominalConstructor()) {
      return getSymbolForInstancesOf(fn);
    }
    return null;
  }
  public Symbol getSymbolForInstancesOf(FunctionType fn) {
    Preconditions.checkState(fn.isConstructor() || fn.isInterface());
    ObjectType pType = fn.getPrototype();
    return getSymbolForName(fn.getSource(), pType.getReferenceName());
  }
  private Symbol getSymbolForName(Node source, String name) {
    if(name == null || globalScope == null) {
      return null;
    }
    SymbolScope scope = source == null ? globalScope : getEnclosingScope(source);
    return scope == null ? null : scope.getQualifiedSlot(name);
  }
  public Symbol getSymbolForScope(SymbolScope scope) {
    if(scope.getSymbolForScope() == null) {
      scope.setSymbolForScope(findSymbolForScope(scope));
    }
    return scope.getSymbolForScope();
  }
  private Symbol getSymbolForTypeHelper(JSType type, boolean linkToCtor) {
    if(type == null) {
      return null;
    }
    if(type.isGlobalThisType()) {
      return globalScope.getSlot(GLOBAL_THIS);
    }
    else 
      if(type.isNominalConstructor()) {
        return linkToCtor ? globalScope.getSlot("Function") : getSymbolDeclaredBy(type.toMaybeFunctionType());
      }
      else 
        if(type.isFunctionPrototypeType()) {
          FunctionType ownerFn = ((ObjectType)type).getOwnerFunction();
          if(!ownerFn.isConstructor() && !ownerFn.isInterface()) {
            return null;
          }
          return linkToCtor ? getSymbolDeclaredBy(ownerFn) : getSymbolForInstancesOf(ownerFn);
        }
        else 
          if(type.isInstanceType()) {
            FunctionType ownerFn = ((ObjectType)type).getConstructor();
            return linkToCtor ? getSymbolDeclaredBy(ownerFn) : getSymbolForInstancesOf(ownerFn);
          }
          else 
            if(type.isFunctionType()) {
              return linkToCtor ? globalScope.getSlot("Function") : globalScope.getQualifiedSlot("Function.prototype");
            }
            else 
              if(type.autoboxesTo() != null) {
                return getSymbolForTypeHelper(type.autoboxesTo(), linkToCtor);
              }
              else {
                return null;
              }
  }
  private Symbol isAnySymbolDeclared(String name, Node declNode, SymbolScope scope) {
    Symbol sym = symbols.get(declNode, name);
    if(sym == null) {
      return scope.ownSymbols.get(name);
    }
    return sym;
  }
  private SymbolScope createScopeFrom(StaticScope<JSType> otherScope) {
    Node otherScopeRoot = otherScope.getRootNode();
    SymbolScope myScope = scopes.get(otherScopeRoot);
    if(myScope == null) {
      StaticScope<JSType> otherScopeParent = otherScope.getParentScope();
      if(otherScopeParent == null) {
        Preconditions.checkState(globalScope == null, "Global scopes found at different roots");
      }
      myScope = new SymbolScope(otherScopeRoot, otherScopeParent == null ? null : createScopeFrom(otherScopeParent), otherScope.getTypeOfThis(), null);
      scopes.put(otherScopeRoot, myScope);
      if(myScope.isGlobalScope()) {
        globalScope = myScope;
      }
    }
    return myScope;
  }
  public SymbolScope getEnclosingScope(Node n) {
    Node current = n.getParent();
    if(n.isName() && n.getParent().isFunction()) {
      current = current.getParent();
    }
    for(; current != null; current = current.getParent()) {
      if(scopes.containsKey(current)) {
        return scopes.get(current);
      }
    }
    return null;
  }
  public SymbolScope getGlobalScope() {
    return globalScope;
  }
  @Override() public SymbolScope getScope(Symbol slot) {
    return slot.scope;
  }
  private SymbolScope getScopeInFunction(Symbol sym) {
    FunctionType type = sym.getFunctionType();
    if(type == null) {
      return null;
    }
    Node functionNode = type.getSource();
    if(functionNode == null) {
      return null;
    }
    return scopes.get(functionNode);
  }
  private boolean isGoodRefToAdd(@Nullable() StaticReference<JSType> ref) {
    return ref != null && ref.getNode() != null && ref.getNode().getStaticSourceFile() != null && !Compiler.SYNTHETIC_EXTERNS.equals(ref.getNode().getStaticSourceFile().getName());
  }
  private boolean needsPropertyScope(Symbol sym) {
    ObjectType type = ObjectType.cast(sym.getType());
    if(type == null) {
      return false;
    }
    if(type.getReferenceName() == null) {
      return true;
    }
    if(sym.getName().equals(type.getReferenceName())) {
      return true;
    }
    if(type.isEnumType() && sym.getName().equals(type.toMaybeEnumType().getElementsType().getReferenceName())) {
      return true;
    }
    return false;
  }
  private int getLexicalScopeDepth(SymbolScope scope) {
    if(scope.isLexicalScope() || scope.isDocScope()) {
      return scope.getScopeDepth();
    }
    else {
      Preconditions.checkState(scope.isPropertyScope());
      Symbol sym = scope.getSymbolForScope();
      Preconditions.checkNotNull(sym);
      return getLexicalScopeDepth(getScope(sym)) + 1;
    }
  }
  public void addAnonymousFunctions() {
    TreeSet<SymbolScope> scopes = Sets.newTreeSet(LEXICAL_SCOPE_ORDERING);
    for (SymbolScope scope : getAllScopes()) {
      if(scope.isLexicalScope()) {
        scopes.add(scope);
      }
    }
    for (SymbolScope scope : scopes) {
      addAnonymousFunctionsInScope(scope);
    }
  }
  private void addAnonymousFunctionsInScope(SymbolScope scope) {
    Symbol sym = getSymbolForScope(scope);
    if(sym == null) {
      if(scope.isLexicalScope() && !scope.isGlobalScope() && scope.getRootNode() != null && !scope.getRootNode().isFromExterns() && scope.getParentScope() != null) {
        SymbolScope parent = scope.getParentScope();
        int count = parent.innerAnonFunctionsWithNames++;
        String innerName = "function%" + count;
        scope.setSymbolForScope(declareInferredSymbol(parent, innerName, scope.getRootNode()));
      }
    }
  }
   <S extends com.google.javascript.rhino.jstype.StaticScope<com.google.javascript.rhino.jstype.JSType>> void addScopes(Collection<S> scopes) {
    for (S scope : scopes) {
      createScopeFrom(scope);
    }
  }
   <R extends com.google.javascript.rhino.jstype.StaticReference<com.google.javascript.rhino.jstype.JSType>, S extends com.google.javascript.rhino.jstype.StaticSlot<com.google.javascript.rhino.jstype.JSType>> void addSymbolsFrom(StaticSymbolTable<S, R> otherSymbolTable) {
    for (S otherSymbol : otherSymbolTable.getAllSymbols()) {
      String name = otherSymbol.getName();
      SymbolScope myScope = createScopeFrom(otherSymbolTable.getScope(otherSymbol));
      StaticReference<JSType> decl = findBestDeclToAdd(otherSymbolTable, otherSymbol);
      Symbol mySymbol = null;
      if(decl != null) {
        Node declNode = decl.getNode();
        mySymbol = isAnySymbolDeclared(name, declNode, myScope);
        if(mySymbol == null) {
          mySymbol = copySymbolTo(otherSymbol, declNode, myScope);
        }
      }
      else {
        mySymbol = myScope.getOwnSlot(name);
      }
      if(mySymbol != null) {
        for (R otherRef : otherSymbolTable.getReferences(otherSymbol)) {
          if(isGoodRefToAdd(otherRef)) {
            mySymbol.defineReferenceAt(otherRef.getNode());
          }
        }
      }
    }
  }
  private void createPropertyScopeFor(Symbol s) {
    if(s.propertyScope != null) {
      return ;
    }
    SymbolScope parentPropertyScope = null;
    ObjectType type = s.getType() == null ? null : s.getType().toObjectType();
    if(type == null) {
      return ;
    }
    ObjectType proto = type.getParentScope();
    if(proto != null && proto != type && proto.getConstructor() != null) {
      Symbol parentSymbol = getSymbolForInstancesOf(proto.getConstructor());
      if(parentSymbol != null) {
        createPropertyScopeFor(parentSymbol);
        parentPropertyScope = parentSymbol.getPropertyScope();
      }
    }
    ObjectType instanceType = type;
    Iterable<String> propNames = type.getOwnPropertyNames();
    if(instanceType.isFunctionPrototypeType()) {
      instanceType = instanceType.getOwnerFunction().getInstanceType();
      Set<String> set = Sets.newHashSet(propNames);
      Iterables.addAll(set, instanceType.getOwnPropertyNames());
      propNames = set;
    }
    s.setPropertyScope(new SymbolScope(null, parentPropertyScope, type, s));
    for (String propName : propNames) {
      StaticSlot<JSType> newProp = instanceType.getSlot(propName);
      if(newProp.getDeclaration() == null) {
        continue ;
      }
      Symbol oldProp = symbols.get(newProp.getDeclaration().getNode(), s.getName() + "." + propName);
      if(oldProp != null) {
        removeSymbol(oldProp);
      }
      if(symbols.get(newProp.getDeclaration().getNode(), newProp.getName()) != null) {
        logger.warning("Found duplicate symbol " + newProp);
        continue ;
      }
      Symbol newSym = copySymbolTo(newProp, s.propertyScope);
      if(oldProp != null) {
        if(newSym.getJSDocInfo() == null) {
          newSym.setJSDocInfo(oldProp.getJSDocInfo());
        }
        newSym.setPropertyScope(oldProp.propertyScope);
        for (Reference ref : oldProp.references.values()) {
          newSym.defineReferenceAt(ref.getNode());
        }
      }
    }
  }
  void fillJSDocInfo(AbstractCompiler compiler, Node externs, Node root) {
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(externs, root), new JSDocInfoCollector(compiler.getTypeRegistry()));
    for (Symbol sym : getAllSymbolsSorted()) {
      JSDocInfo info = sym.getJSDocInfo();
      if(info == null) {
        continue ;
      }
      for (Marker marker : info.getMarkers()) {
        SourcePosition<Node> pos = marker.getNameNode();
        if(pos == null) {
          continue ;
        }
        Node paramNode = pos.getItem();
        String name = paramNode.getString();
        Symbol param = getParameterInFunction(sym, name);
        if(param == null) {
          SourcePosition<Node> typePos = marker.getType();
          JSType type = null;
          if(typePos != null) {
            type = typePos.getItem().getJSType();
          }
          SymbolScope var_1310 = sym.docScope;
          if(var_1310 == null) {
            sym.docScope = new SymbolScope(null, null, null, sym);
          }
          Symbol existingSymbol = isAnySymbolDeclared(name, paramNode, sym.docScope);
          if(existingSymbol == null) {
            declareSymbol(name, type, type == null, sym.docScope, paramNode, null);
          }
        }
        else {
          param.defineReferenceAt(paramNode);
        }
      }
    }
  }
  void fillNamespaceReferences() {
    for (Symbol symbol : getAllSymbolsSorted()) {
      String qName = symbol.getName();
      int rootIndex = qName.indexOf('.');
      if(rootIndex == -1) {
        continue ;
      }
      Symbol root = symbol.scope.getQualifiedSlot(qName.substring(0, rootIndex));
      if(root == null) {
        continue ;
      }
      for (Reference ref : getReferences(symbol)) {
        Node currentNode = ref.getNode();
        if(!currentNode.isQualifiedName()) {
          continue ;
        }
        while(currentNode.isGetProp()){
          currentNode = currentNode.getFirstChild();
          String name = currentNode.getQualifiedName();
          if(name != null) {
            Symbol namespace = isAnySymbolDeclared(name, currentNode, root.scope);
            if(namespace == null) {
              namespace = root.scope.getQualifiedSlot(name);
            }
            if(namespace == null && root.scope.isGlobalScope()) {
              namespace = declareSymbol(name, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), true, root.scope, currentNode, null);
            }
            if(namespace != null) {
              namespace.defineReferenceAt(currentNode);
            }
          }
        }
      }
    }
  }
  void fillPropertyScopes() {
    List<Symbol> types = Lists.newArrayList();
    for (Symbol sym : getAllSymbols()) {
      if(needsPropertyScope(sym)) {
        types.add(sym);
      }
    }
    Collections.sort(types, Collections.reverseOrder(getNaturalSymbolOrdering()));
    for (Symbol s : types) {
      createPropertyScopeFor(s);
    }
    pruneOrphanedNames();
  }
  void fillPropertySymbols(AbstractCompiler compiler, Node externs, Node root) {
    (new PropertyRefCollector(compiler)).process(externs, root);
  }
  void fillThisReferences(AbstractCompiler compiler, Node externs, Node root) {
    (new ThisRefCollector(compiler)).process(externs, root);
  }
  void findScopes(AbstractCompiler compiler, Node externs, Node root) {
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(externs, root), new NodeTraversal.AbstractScopedCallback() {
        @Override() public void enterScope(NodeTraversal t) {
          createScopeFrom(t.getScope());
        }
        @Override() public void visit(NodeTraversal t, Node n, Node p) {
        }
    });
  }
  void pruneOrphanedNames() {
    nextSymbol:
      for (Symbol s : getAllSymbolsSorted()) {
        if(s.isProperty()) {
          continue ;
        }
        String currentName = s.getName();
        int dot = -1;
        while(-1 != (dot = currentName.lastIndexOf('.'))){
          currentName = currentName.substring(0, dot);
          Symbol owner = s.scope.getQualifiedSlot(currentName);
          if(owner != null && owner.getType() != null && (owner.getType().isNominalConstructor() || owner.getType().isFunctionPrototypeType() || owner.getType().isEnumType())) {
            removeSymbol(s);
            continue nextSymbol;
          }
        }
      }
  }
  private void removeSymbol(Symbol s) {
    SymbolScope scope = getScope(s);
    if(scope.ownSymbols.remove(s.getName()) != s) {
      throw new IllegalStateException("Symbol not found in scope " + s);
    }
    if(symbols.remove(s.getDeclaration().getNode(), s.getName()) != s) {
      throw new IllegalStateException("Symbol not found in table " + s);
    }
  }
  private void toDebugString(StringBuilder builder, Symbol symbol) {
    SymbolScope scope = symbol.scope;
    if(scope.isGlobalScope()) {
      builder.append(String.format("\'%s\' : in global scope:\n", symbol.getName()));
    }
    else 
      if(scope.getRootNode() != null) {
        builder.append(String.format("\'%s\' : in scope %s:%d\n", symbol.getName(), scope.getRootNode().getSourceFileName(), scope.getRootNode().getLineno()));
      }
      else 
        if(scope.getSymbolForScope() != null) {
          builder.append(String.format("\'%s\' : in scope %s\n", symbol.getName(), scope.getSymbolForScope().getName()));
        }
        else {
          builder.append(String.format("\'%s\' : in unknown scope\n", symbol.getName()));
        }
    int refCount = 0;
    for (Reference ref : getReferences(symbol)) {
      builder.append(String.format("  Ref %d: %s:%d\n", refCount, ref.getNode().getSourceFileName(), ref.getNode().getLineno()));
      refCount++;
    }
  }
  
  private class JSDocInfoCollector extends NodeTraversal.AbstractPostOrderCallback  {
    final private JSTypeRegistry typeRegistry;
    private JSDocInfoCollector(JSTypeRegistry registry) {
      super();
      this.typeRegistry = registry;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.getJSDocInfo() != null) {
        JSDocInfo info = n.getJSDocInfo();
        docInfos.add(info);
        for (Node typeAst : info.getTypeNodes()) {
          SymbolScope scope = scopes.get(t.getScopeRoot());
          visitTypeNode(scope == null ? globalScope : scope, typeAst);
        }
      }
    }
    public void visitTypeNode(SymbolScope scope, Node n) {
      if(n.isString()) {
        Symbol symbol = scope.getSlot(n.getString());
        if(symbol == null) {
          JSType type = typeRegistry.getType(n.getString());
          JSType autobox = type == null ? null : type.autoboxesTo();
          symbol = autobox == null ? null : getSymbolForTypeHelper(autobox, true);
        }
        if(symbol != null) {
          symbol.defineReferenceAt(n);
        }
      }
      for(com.google.javascript.rhino.Node child = n.getFirstChild(); child != null; child = child.getNext()) {
        visitTypeNode(scope, child);
      }
    }
  }
  
  private class PropertyRefCollector extends NodeTraversal.AbstractPostOrderCallback implements CompilerPass  {
    final private AbstractCompiler compiler;
    PropertyRefCollector(AbstractCompiler compiler) {
      super();
      this.compiler = compiler;
    }
    private boolean maybeDefineReference(Node n, String propName, Symbol ownerSymbol) {
      if(ownerSymbol != null && ownerSymbol.getPropertyScope() != null) {
        Symbol prop = ownerSymbol.getPropertyScope().getSlot(propName);
        if(prop != null) {
          prop.defineReferenceAt(n);
          return true;
        }
      }
      return false;
    }
    private boolean maybeDefineTypedReference(Node n, String propName, JSType owner) {
      if(owner.isGlobalThisType()) {
        Symbol sym = globalScope.getSlot(propName);
        if(sym != null) {
          sym.defineReferenceAt(n);
          return true;
        }
      }
      else 
        if(owner.isNominalConstructor()) {
          return maybeDefineReference(n, propName, getSymbolDeclaredBy(owner.toMaybeFunctionType()));
        }
        else 
          if(owner.isEnumType()) {
            return maybeDefineReference(n, propName, getSymbolDeclaredBy(owner.toMaybeEnumType()));
          }
          else {
            boolean defined = false;
            for (Symbol ctor : getAllSymbolsForType(owner)) {
              if(maybeDefineReference(n, propName, getSymbolForInstancesOf(ctor))) {
                defined = true;
              }
            }
            return defined;
          }
      return false;
    }
    private boolean tryDefineLexicalQualifiedNameRef(String name, Node n) {
      if(name != null) {
        Symbol lexicalSym = getEnclosingScope(n).getQualifiedSlot(name);
        if(lexicalSym != null) {
          lexicalSym.defineReferenceAt(n);
          return true;
        }
      }
      return false;
    }
    @Override() public void process(Node externs, Node root) {
      NodeTraversal.traverseRoots(compiler, Lists.newArrayList(externs, root), this);
    }
    private void tryRemoveLexicalQualifiedNameRef(String name, Node n) {
      if(name != null) {
        Symbol lexicalSym = getEnclosingScope(n).getQualifiedSlot(name);
        if(lexicalSym != null && lexicalSym.isLexicalVariable() && lexicalSym.getDeclaration().getNode() == n) {
          removeSymbol(lexicalSym);
        }
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isGetProp()) {
        JSType owner = n.getFirstChild().getJSType();
        if(owner != null) {
          boolean defined = maybeDefineTypedReference(n, n.getLastChild().getString(), owner);
          if(defined) {
            tryRemoveLexicalQualifiedNameRef(n.getQualifiedName(), n);
            return ;
          }
        }
        tryDefineLexicalQualifiedNameRef(n.getQualifiedName(), n);
      }
      else 
        if(n.isStringKey()) {
          JSType owner = parent.getJSType();
          if(owner != null) {
            boolean defined = maybeDefineTypedReference(n, n.getString(), owner);
            if(defined) {
              tryRemoveLexicalQualifiedNameRef(NodeUtil.getBestLValueName(n), n);
              return ;
            }
          }
          tryDefineLexicalQualifiedNameRef(NodeUtil.getBestLValueName(n), n);
        }
    }
  }
  
  final public static class Reference extends SimpleReference<Symbol>  {
    Reference(Symbol symbol, Node node) {
      super(symbol, node);
    }
  }
  
  final public static class Symbol extends SimpleSlot  {
    final private Map<Node, Reference> references = Maps.newLinkedHashMap();
    final private SymbolScope scope;
    private SymbolScope propertyScope = null;
    private Reference declaration = null;
    private JSDocInfo docInfo = null;
    private SymbolScope docScope = null;
    Symbol(String name, JSType type, boolean inferred, SymbolScope scope) {
      super(name, type, inferred);
      this.scope = scope;
    }
    public FunctionType getFunctionType() {
      return JSType.toMaybeFunctionType(getType());
    }
    @Override() public JSDocInfo getJSDocInfo() {
      return docInfo;
    }
    public Node getDeclarationNode() {
      return declaration == null ? null : declaration.getNode();
    }
    public Reference defineReferenceAt(Node n) {
      Reference result = references.get(n);
      if(result == null) {
        result = new Reference(this, n);
        references.put(n, result);
      }
      return result;
    }
    @Override() public Reference getDeclaration() {
      return declaration;
    }
    public String getSourceFileName() {
      Node n = getDeclarationNode();
      return n == null ? null : n.getSourceFileName();
    }
    @Override() public String toString() {
      Node n = getDeclarationNode();
      int lineNo = n == null ? -1 : n.getLineno();
      return getName() + "@" + getSourceFileName() + ":" + lineNo;
    }
    public SymbolScope getPropertyScope() {
      return propertyScope;
    }
    public boolean inExterns() {
      Node n = getDeclarationNode();
      return n == null ? false : n.isFromExterns();
    }
    public boolean inGlobalScope() {
      return scope.isGlobalScope();
    }
    public boolean isDocOnlyParameter() {
      return scope.isDocScope();
    }
    public boolean isLexicalVariable() {
      return scope.isLexicalScope();
    }
    public boolean isProperty() {
      return scope.isPropertyScope();
    }
    void setDeclaration(Reference ref) {
      Preconditions.checkState(this.declaration == null);
      this.declaration = ref;
    }
    void setJSDocInfo(JSDocInfo info) {
      this.docInfo = info;
    }
    void setPropertyScope(SymbolScope scope) {
      this.propertyScope = scope;
      if(scope != null) {
        this.propertyScope.setSymbolForScope(this);
      }
    }
  }
  
  final public static class SymbolScope implements StaticScope<JSType>  {
    final private Node rootNode;
    final private SymbolScope parent;
    final private JSType typeOfThis;
    final private Map<String, Symbol> ownSymbols = Maps.newLinkedHashMap();
    final private int scopeDepth;
    private int innerAnonFunctionsWithNames = 0;
    private Symbol mySymbol;
    SymbolScope(Node rootNode, @Nullable() SymbolScope parent, JSType typeOfThis, Symbol mySymbol) {
      super();
      this.rootNode = rootNode;
      this.parent = parent;
      this.typeOfThis = typeOfThis;
      this.scopeDepth = parent == null ? 0 : (parent.getScopeDepth() + 1);
      this.mySymbol = mySymbol;
    }
    @Override() public JSType getTypeOfThis() {
      return typeOfThis;
    }
    @Override() public Node getRootNode() {
      return rootNode;
    }
    @Override() public String toString() {
      Node n = getRootNode();
      if(n != null) {
        return "Scope@" + n.getSourceFileName() + ":" + n.getLineno();
      }
      else {
        return "PropertyScope@" + getSymbolForScope();
      }
    }
    @Override() public Symbol getOwnSlot(String name) {
      return ownSymbols.get(name);
    }
    public Symbol getQualifiedSlot(String name) {
      Symbol fullyNamedSym = getSlot(name);
      if(fullyNamedSym != null) {
        return fullyNamedSym;
      }
      int dot = name.lastIndexOf(".");
      if(dot != -1) {
        Symbol owner = getQualifiedSlot(name.substring(0, dot));
        if(owner != null && owner.getPropertyScope() != null) {
          return owner.getPropertyScope().getSlot(name.substring(dot + 1));
        }
      }
      return null;
    }
    @Override() public Symbol getSlot(String name) {
      Symbol own = getOwnSlot(name);
      if(own != null) {
        return own;
      }
      Symbol ancestor = parent == null ? null : parent.getSlot(name);
      if(ancestor != null) {
        return ancestor;
      }
      return null;
    }
    Symbol getSymbolForScope() {
      return mySymbol;
    }
    @Override() public SymbolScope getParentScope() {
      return parent;
    }
    public boolean isDocScope() {
      return getRootNode() == null && mySymbol != null && mySymbol.docScope == this;
    }
    public boolean isGlobalScope() {
      return getParentScope() == null && getRootNode() != null;
    }
    public boolean isLexicalScope() {
      return getRootNode() != null;
    }
    public boolean isPropertyScope() {
      return getRootNode() == null && !isDocScope();
    }
    public int getIndexOfSymbol(Symbol sym) {
      return Iterables.indexOf(ownSymbols.values(), Predicates.equalTo(sym));
    }
    public int getScopeDepth() {
      return scopeDepth;
    }
    void setSymbolForScope(Symbol sym) {
      this.mySymbol = sym;
    }
  }
  
  private class ThisRefCollector extends NodeTraversal.AbstractScopedCallback implements CompilerPass  {
    final private AbstractCompiler compiler;
    final private List<Symbol> thisStack = Lists.newArrayList();
    ThisRefCollector(AbstractCompiler compiler) {
      super();
      this.compiler = compiler;
    }
    @Override() public void enterScope(NodeTraversal t) {
      Symbol symbol = null;
      if(t.inGlobalScope()) {
        Node firstInputRoot = t.getScopeRoot().getLastChild().getFirstChild();
        if(firstInputRoot != null) {
          symbol = addSymbol(GLOBAL_THIS, registry.getNativeType(JSTypeNative.GLOBAL_THIS), false, globalScope, firstInputRoot);
          symbol.setDeclaration(new Reference(symbol, firstInputRoot));
        }
      }
      else {
        SymbolScope scope = scopes.get(t.getScopeRoot());
        Preconditions.checkNotNull(scope);
        Symbol scopeSymbol = getSymbolForScope(scope);
        if(scopeSymbol != null) {
          SymbolScope propScope = scopeSymbol.getPropertyScope();
          if(propScope != null) {
            symbol = propScope.getOwnSlot("this");
            if(symbol == null) {
              JSType rootType = t.getScopeRoot().getJSType();
              FunctionType fnType = rootType == null ? null : rootType.toMaybeFunctionType();
              JSType type = fnType == null ? null : fnType.getTypeOfThis();
              symbol = addSymbol("this", type, false, scope, t.getScopeRoot());
            }
          }
        }
      }
      thisStack.add(symbol);
    }
    @Override() public void exitScope(NodeTraversal t) {
      thisStack.remove(thisStack.size() - 1);
    }
    @Override() public void process(Node externs, Node root) {
      NodeTraversal.traverseRoots(compiler, Lists.newArrayList(externs, root), this);
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isThis()) {
        return ;
      }
      Symbol symbol = thisStack.get(thisStack.size() - 1);
      if(symbol != null) {
        Reference ref = symbol.defineReferenceAt(n);
        if(symbol.getDeclaration() == null) {
          symbol.setDeclaration(ref);
        }
      }
    }
  }
}