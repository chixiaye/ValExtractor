package com.google.javascript.jscomp;
import static com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.TokenStream;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.StaticReference;
import com.google.javascript.rhino.jstype.StaticScope;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import com.google.javascript.rhino.jstype.StaticSymbolTable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GlobalNamespace implements StaticScope<JSType>, StaticSymbolTable<GlobalNamespace.Name, GlobalNamespace.Ref>  {
  private AbstractCompiler compiler;
  final private Node root;
  final private Node externsRoot;
  private boolean inExterns;
  private Scope externsScope;
  private boolean generated = false;
  private int currentPreOrderIndex = 0;
  private List<Name> globalNames = new ArrayList<Name>();
  private Map<String, Name> nameMap = new HashMap<String, Name>();
  GlobalNamespace(AbstractCompiler compiler, Node externsRoot, Node root) {
    super();
    this.compiler = compiler;
    this.externsRoot = externsRoot;
    this.root = root;
  }
  GlobalNamespace(AbstractCompiler compiler, Node root) {
    this(compiler, null, root);
  }
  @Override() public Iterable<Name> getAllSymbols() {
    ensureGenerated();
    return Collections.unmodifiableCollection(getNameIndex().values());
  }
  @Override() public Iterable<Ref> getReferences(Name slot) {
    ensureGenerated();
    return Collections.unmodifiableList(slot.getRefs());
  }
  @Override() public JSType getTypeOfThis() {
    return compiler.getTypeRegistry().getNativeObjectType(GLOBAL_THIS);
  }
  List<Name> getNameForest() {
    ensureGenerated();
    return globalNames;
  }
  Map<String, Name> getNameIndex() {
    ensureGenerated();
    return nameMap;
  }
  @Override() public Name getOwnSlot(String name) {
    ensureGenerated();
    return nameMap.get(name);
  }
  @Override() public Name getSlot(String name) {
    return getOwnSlot(name);
  }
  @Override() public Node getRootNode() {
    return root.getParent();
  }
  @Override() public StaticScope<JSType> getParentScope() {
    return null;
  }
  @Override() public StaticScope<JSType> getScope(Name slot) {
    return this;
  }
  private String getTopVarName(String name) {
    int firstDotIndex = name.indexOf('.');
    return firstDotIndex == -1 ? name : name.substring(0, firstDotIndex);
  }
  boolean hasExternsRoot() {
    return externsRoot != null;
  }
  private boolean isGlobalNameReference(String name, Scope s) {
    String topVarName = getTopVarName(name);
    return isGlobalVarReference(topVarName, s);
  }
  private boolean isGlobalScope(Scope s) {
    return s.getParent() == null;
  }
  private boolean isGlobalVarReference(String name, Scope s) {
    Scope.Var v = s.getVar(name);
    if(v == null && externsScope != null) {
      v = externsScope.getVar(name);
    }
    return v != null && !v.isLocal();
  }
  private void ensureGenerated() {
    if(!generated) {
      process();
    }
  }
  private void process() {
    if(externsRoot != null) {
      inExterns = true;
      NodeTraversal.traverse(compiler, externsRoot, new BuildGlobalNamespace());
    }
    inExterns = false;
    NodeTraversal.traverse(compiler, root, new BuildGlobalNamespace());
    generated = true;
  }
  void scanNewNodes(Scope scope, Set<Node> newNodes) {
    NodeTraversal t = new NodeTraversal(compiler, new BuildGlobalNamespace(new NodeFilter(newNodes)));
    t.traverseAtScope(scope);
  }
  
  private class BuildGlobalNamespace implements NodeTraversal.Callback  {
    final private Predicate<Node> nodeFilter;
    BuildGlobalNamespace() {
      this(null);
    }
    BuildGlobalNamespace(Predicate<Node> nodeFilter) {
      super();
      this.nodeFilter = nodeFilter;
    }
    Name.Type getValueType(Node n) {
      switch (n.getType()){
        case Token.OBJECTLIT:
        return Name.Type.OBJECTLIT;
        case Token.FUNCTION:
        return Name.Type.FUNCTION;
        case Token.OR:
        return getValueType(n.getLastChild());
        case Token.HOOK:
        Node second = n.getFirstChild().getNext();
        Name.Type t = getValueType(second);
        if(t != Name.Type.OTHER) 
          return t;
        Node third = second.getNext();
        return getValueType(third);
      }
      return Name.Type.OTHER;
    }
    Name getOrCreateName(String name) {
      Name node = nameMap.get(name);
      if(node == null) {
        int i = name.lastIndexOf('.');
        if(i >= 0) {
          String parentName = name.substring(0, i);
          Name parent = getOrCreateName(parentName);
          node = parent.addProperty(name.substring(i + 1), inExterns);
        }
        else {
          node = new Name(name, null, inExterns);
          globalNames.add(node);
        }
        nameMap.put(name, node);
      }
      return node;
    }
    Ref.Type determineGetTypeForHookOrBooleanExpr(NodeTraversal t, Node parent, String name) {
      Node prev = parent;
      for (Node anc : parent.getAncestors()) {
        switch (anc.getType()){
          case Token.EXPR_RESULT:
          case Token.VAR:
          case Token.IF:
          case Token.WHILE:
          case Token.FOR:
          case Token.TYPEOF:
          case Token.VOID:
          case Token.NOT:
          case Token.BITNOT:
          case Token.POS:
          case Token.NEG:
          return Ref.Type.DIRECT_GET;
          case Token.HOOK:
          if(anc.getFirstChild() == prev) {
            return Ref.Type.DIRECT_GET;
          }
          break ;
          case Token.ASSIGN:
          if(!name.equals(anc.getFirstChild().getQualifiedName())) {
            return Ref.Type.ALIASING_GET;
          }
          break ;
          case Token.NAME:
          if(!name.equals(anc.getString())) {
            return Ref.Type.ALIASING_GET;
          }
          break ;
          case Token.CALL:
          if(anc.getFirstChild() != prev) {
            return Ref.Type.ALIASING_GET;
          }
          break ;
          case Token.DELPROP:
          return Ref.Type.DELETE_PROP;
        }
        prev = anc;
      }
      return Ref.Type.ALIASING_GET;
    }
    String getNameForObjLitKey(Node n) {
      Node parent = n.getParent();
      Preconditions.checkState(parent.isObjectLit());
      Node gramps = parent.getParent();
      if(gramps == null) {
        return null;
      }
      Node greatGramps = gramps.getParent();
      String name;
      switch (gramps.getType()){
        case Token.NAME:
        if(greatGramps == null || !greatGramps.isVar()) {
          return null;
        }
        name = gramps.getString();
        break ;
        case Token.ASSIGN:
        Node lvalue = gramps.getFirstChild();
        name = lvalue.getQualifiedName();
        break ;
        case Token.STRING_KEY:
        if(greatGramps != null && greatGramps.isObjectLit()) {
          name = getNameForObjLitKey(gramps);
        }
        else {
          return null;
        }
        break ;
        default:
        return null;
      }
      if(name != null) {
        String key = n.getString();
        if(TokenStream.isJSIdentifier(key)) {
          return name + '.' + key;
        }
      }
      return null;
    }
    boolean isNestedAssign(Node parent) {
      return parent.isAssign() && !parent.getParent().isExprResult();
    }
    private boolean isTypeDeclaration(Node n, Node parent) {
      Node valueNode = NodeUtil.getRValueOfLValue(n);
      JSDocInfo info = NodeUtil.getBestJSDocInfo(n);
      return info != null && valueNode != null && (info.isConstructor() && valueNode.isFunction() || info.isInterface() && valueNode.isFunction() || info.hasEnumParameterType() && valueNode.isObjectLit());
    }
    boolean maybeHandlePrototypePrefix(NodeTraversal t, Node n, Node parent, String name) {
      int numLevelsToRemove;
      String prefix;
      if(name.endsWith(".prototype")) {
        numLevelsToRemove = 1;
        prefix = name.substring(0, name.length() - 10);
      }
      else {
        int i = name.indexOf(".prototype.");
        if(i == -1) {
          return false;
        }
        prefix = name.substring(0, i);
        numLevelsToRemove = 2;
        i = name.indexOf('.', i + 11);
        while(i >= 0){
          numLevelsToRemove++;
          i = name.indexOf('.', i + 1);
        }
      }
      if(parent != null && NodeUtil.isObjectLitKey(n, parent)) {
        return true;
      }
      for(int i = 0; i < numLevelsToRemove; i++) {
        parent = n;
        n = n.getFirstChild();
      }
      handleGet(t, n, parent, prefix, Ref.Type.PROTOTYPE_GET);
      return true;
    }
    @Override() public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      collect(t, n, parent);
      return true;
    }
    public void collect(NodeTraversal t, Node n, Node parent) {
      if(nodeFilter != null && !nodeFilter.apply(n)) {
        return ;
      }
      if(externsRoot != null && n == externsRoot) {
        externsScope = t.getScope();
      }
      String name;
      boolean isSet = false;
      Name.Type type = Name.Type.OTHER;
      boolean isPropAssign = false;
      switch (n.getType()){
        case Token.GETTER_DEF:
        case Token.SETTER_DEF:
        case Token.STRING_KEY:
        name = null;
        if(parent != null && parent.isObjectLit()) {
          name = getNameForObjLitKey(n);
        }
        if(name == null) 
          return ;
        isSet = true;
        switch (n.getType()){
          case Token.STRING_KEY:
          type = getValueType(n.getFirstChild());
          break ;
          case Token.GETTER_DEF:
          type = Name.Type.GET;
          break ;
          case Token.SETTER_DEF:
          type = Name.Type.SET;
          break ;
          default:
          throw new IllegalStateException("unexpected:" + n);
        }
        break ;
        case Token.NAME:
        if(parent != null) {
          switch (parent.getType()){
            case Token.VAR:
            isSet = true;
            Node rvalue = n.getFirstChild();
            type = rvalue == null ? Name.Type.OTHER : getValueType(rvalue);
            break ;
            case Token.ASSIGN:
            if(parent.getFirstChild() == n) {
              isSet = true;
              type = getValueType(n.getNext());
            }
            break ;
            case Token.GETPROP:
            return ;
            case Token.FUNCTION:
            Node gramps = parent.getParent();
            if(gramps == null || NodeUtil.isFunctionExpression(parent)) 
              return ;
            isSet = true;
            type = Name.Type.FUNCTION;
            break ;
            case Token.INC:
            case Token.DEC:
            isSet = true;
            type = Name.Type.OTHER;
            break ;
            default:
            if(NodeUtil.isAssignmentOp(parent) && parent.getFirstChild() == n) {
              isSet = true;
              type = Name.Type.OTHER;
            }
          }
        }
        name = n.getString();
        break ;
        case Token.GETPROP:
        if(parent != null) {
          switch (parent.getType()){
            case Token.ASSIGN:
            if(parent.getFirstChild() == n) {
              isSet = true;
              type = getValueType(n.getNext());
              isPropAssign = true;
            }
            break ;
            case Token.INC:
            case Token.DEC:
            isSet = true;
            type = Name.Type.OTHER;
            break ;
            case Token.GETPROP:
            return ;
            default:
            if(NodeUtil.isAssignmentOp(parent) && parent.getFirstChild() == n) {
              isSet = true;
              type = Name.Type.OTHER;
            }
          }
        }
        name = n.getQualifiedName();
        if(name == null) 
          return ;
        break ;
        default:
        return ;
      }
      Scope scope = t.getScope();
      if(!isGlobalNameReference(name, scope)) {
        return ;
      }
      if(isSet) {
        if(isGlobalScope(scope)) {
          handleSetFromGlobal(t, n, parent, name, isPropAssign, type);
        }
        else {
          handleSetFromLocal(t, n, parent, name);
        }
      }
      else {
        handleGet(t, n, parent, name);
      }
    }
    void handleGet(NodeTraversal t, Node n, Node parent, String name) {
      if(maybeHandlePrototypePrefix(t, n, parent, name)) 
        return ;
      Ref.Type type = Ref.Type.DIRECT_GET;
      if(parent != null) {
        switch (parent.getType()){
          case Token.IF:
          case Token.TYPEOF:
          case Token.VOID:
          case Token.NOT:
          case Token.BITNOT:
          case Token.POS:
          case Token.NEG:
          break ;
          case Token.CALL:
          type = n == parent.getFirstChild() ? Ref.Type.CALL_GET : Ref.Type.ALIASING_GET;
          break ;
          case Token.NEW:
          type = n == parent.getFirstChild() ? Ref.Type.DIRECT_GET : Ref.Type.ALIASING_GET;
          break ;
          case Token.OR:
          case Token.AND:
          type = determineGetTypeForHookOrBooleanExpr(t, parent, name);
          break ;
          case Token.HOOK:
          if(n != parent.getFirstChild()) {
            type = determineGetTypeForHookOrBooleanExpr(t, parent, name);
          }
          break ;
          case Token.DELPROP:
          type = Ref.Type.DELETE_PROP;
          break ;
          default:
          type = Ref.Type.ALIASING_GET;
          break ;
        }
      }
      handleGet(t, n, parent, name, type);
    }
    void handleGet(NodeTraversal t, Node n, Node parent, String name, Ref.Type type) {
      Name nameObj = getOrCreateName(name);
      nameObj.addRef(new Ref(t, n, nameObj, type, currentPreOrderIndex++));
    }
    void handleSetFromGlobal(NodeTraversal t, Node n, Node parent, String name, boolean isPropAssign, Name.Type type) {
      if(maybeHandlePrototypePrefix(t, n, parent, name)) 
        return ;
      Name nameObj = getOrCreateName(name);
      nameObj.type = type;
      Ref set = new Ref(t, n, nameObj, Ref.Type.SET_FROM_GLOBAL, currentPreOrderIndex++);
      nameObj.addRef(set);
      if(isNestedAssign(parent)) {
        Ref get = new Ref(t, n, nameObj, Ref.Type.ALIASING_GET, currentPreOrderIndex++);
        nameObj.addRef(get);
        Ref.markTwins(set, get);
      }
      else 
        if(isTypeDeclaration(n, parent)) {
          nameObj.setDeclaredType();
        }
    }
    void handleSetFromLocal(NodeTraversal t, Node n, Node parent, String name) {
      if(maybeHandlePrototypePrefix(t, n, parent, name)) 
        return ;
      Name nameObj = getOrCreateName(name);
      Ref set = new Ref(t, n, nameObj, Ref.Type.SET_FROM_LOCAL, currentPreOrderIndex++);
      nameObj.addRef(set);
      if(isNestedAssign(parent)) {
        Ref get = new Ref(t, n, nameObj, Ref.Type.ALIASING_GET, currentPreOrderIndex++);
        nameObj.addRef(get);
        Ref.markTwins(set, get);
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    }
  }
  
  static class Name implements StaticSlot<JSType>  {
    final private String baseName;
    final Name parent;
    List<Name> props;
    private Ref declaration;
    private List<Ref> refs;
    Type type;
    private boolean declaredType = false;
    private boolean hasDeclaredTypeDescendant = false;
    int globalSets = 0;
    int localSets = 0;
    int aliasingGets = 0;
    int totalGets = 0;
    int callGets = 0;
    int deleteProps = 0;
    final boolean inExterns;
    JSDocInfo docInfo = null;
    Name(String name, Name parent, boolean inExterns) {
      super();
      this.baseName = name;
      this.parent = parent;
      this.type = Type.OTHER;
      this.inExterns = inExterns;
    }
    private static JSDocInfo getDocInfoForDeclaration(Ref ref) {
      if(ref.node != null) {
        Node refParent = ref.node.getParent();
        switch (refParent.getType()){
          case Token.FUNCTION:
          case Token.ASSIGN:
          return refParent.getJSDocInfo();
          case Token.VAR:
          return ref.node == refParent.getFirstChild() ? refParent.getJSDocInfo() : ref.node.getJSDocInfo();
        }
      }
      return null;
    }
    @Override() public JSDocInfo getJSDocInfo() {
      return docInfo;
    }
    @Override() public JSType getType() {
      return null;
    }
    List<Ref> getRefs() {
      return refs == null ? ImmutableList.<Ref>of() : refs;
    }
    Name addProperty(String name, boolean inExterns) {
      if(props == null) {
        props = new ArrayList<Name>();
      }
      Name node = new Name(name, this, inExterns);
      props.add(node);
      return node;
    }
    @Override() public Ref getDeclaration() {
      return declaration;
    }
    String getBaseName() {
      return baseName;
    }
    String getFullName() {
      return parent == null ? baseName : parent.getFullName() + '.' + baseName;
    }
    @Override() public String getName() {
      return getFullName();
    }
    @Override() public String toString() {
      return getFullName() + " (" + type + "): globalSets=" + globalSets + ", localSets=" + localSets + ", totalGets=" + totalGets + ", aliasingGets=" + aliasingGets + ", callGets=" + callGets;
    }
    boolean canCollapse() {
      return !inExterns && !isGetOrSetDefinition() && (declaredType || (parent == null || parent.canCollapseUnannotatedChildNames()) && (globalSets > 0 || localSets > 0) && deleteProps == 0);
    }
    boolean canCollapseUnannotatedChildNames() {
      if(type == Type.OTHER || isGetOrSetDefinition() || globalSets != 1 || localSets != 0 || deleteProps != 0) {
        return false;
      }
      Preconditions.checkNotNull(declaration);
      if(declaration.getTwin() != null) {
        return false;
      }
      if(declaredType) {
        return true;
      }
      if(parent != null && parent.shouldKeepKeys()) {
        return false;
      }
      if(aliasingGets > 0) {
        return false;
      }
      return (parent == null || parent.canCollapseUnannotatedChildNames());
    }
    boolean canEliminate() {
      if(!canCollapseUnannotatedChildNames() || totalGets > 0) {
        return false;
      }
      if(props != null) {
        for (Name n : props) {
          if(!n.canCollapse()) {
            return false;
          }
        }
      }
      return true;
    }
    boolean isDeclaredType() {
      return declaredType;
    }
    boolean isGetOrSetDefinition() {
      return this.type == Type.GET || this.type == Type.SET;
    }
    boolean isNamespace() {
      return hasDeclaredTypeDescendant && type == Type.OBJECTLIT;
    }
    boolean isSimpleName() {
      return parent == null;
    }
    boolean isSimpleStubDeclaration() {
      if(getRefs().size() == 1) {
        Ref ref = refs.get(0);
        JSDocInfo info = ref.node.getJSDocInfo();
        Node var_1618 = ref.node;
        if(var_1618.getParent() != null && ref.node.getParent().isExprResult()) {
          return true;
        }
      }
      return false;
    }
    @Override() public boolean isTypeInferred() {
      return false;
    }
    boolean needsToBeStubbed() {
      return globalSets == 0 && localSets > 0;
    }
    boolean shouldKeepKeys() {
      return type == Type.OBJECTLIT && aliasingGets > 0;
    }
    void addRef(Ref ref) {
      addRefInternal(ref);
      switch (ref.type){
        case SET_FROM_GLOBAL:
        if(declaration == null) {
          declaration = ref;
          docInfo = getDocInfoForDeclaration(ref);
        }
        globalSets++;
        break ;
        case SET_FROM_LOCAL:
        localSets++;
        break ;
        case PROTOTYPE_GET:
        case DIRECT_GET:
        totalGets++;
        break ;
        case ALIASING_GET:
        aliasingGets++;
        totalGets++;
        break ;
        case CALL_GET:
        callGets++;
        totalGets++;
        break ;
        case DELETE_PROP:
        deleteProps++;
        break ;
        default:
        throw new IllegalStateException();
      }
    }
    void addRefInternal(Ref ref) {
      if(refs == null) {
        refs = Lists.newArrayList();
      }
      refs.add(ref);
    }
    void removeRef(Ref ref) {
      if(refs != null && refs.remove(ref)) {
        if(ref == declaration) {
          declaration = null;
          if(refs != null) {
            for (Ref maybeNewDecl : refs) {
              if(maybeNewDecl.type == Ref.Type.SET_FROM_GLOBAL) {
                declaration = maybeNewDecl;
                break ;
              }
            }
          }
        }
        switch (ref.type){
          case SET_FROM_GLOBAL:
          globalSets--;
          break ;
          case SET_FROM_LOCAL:
          localSets--;
          break ;
          case PROTOTYPE_GET:
          case DIRECT_GET:
          totalGets--;
          break ;
          case ALIASING_GET:
          aliasingGets--;
          totalGets--;
          break ;
          case CALL_GET:
          callGets--;
          totalGets--;
          break ;
          case DELETE_PROP:
          deleteProps--;
          break ;
          default:
          throw new IllegalStateException();
        }
      }
    }
    void setDeclaredType() {
      declaredType = true;
      for(com.google.javascript.jscomp.GlobalNamespace.Name ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
        ancestor.hasDeclaredTypeDescendant = true;
      }
    }
    enum Type {
      OBJECTLIT(),

      FUNCTION(),

      GET(),

      SET(),

      OTHER(),

    ;
    private Type() {
    }
    }
  }
  
  private static class NodeFilter implements Predicate<Node>  {
    final private Set<Node> newNodes;
    NodeFilter(Set<Node> newNodes) {
      super();
      this.newNodes = newNodes;
    }
    @Override() public boolean apply(Node n) {
      if(!n.isQualifiedName()) {
        return false;
      }
      Node current;
      for(current = n; current.isGetProp(); current = current.getFirstChild()) {
        if(newNodes.contains(current)) {
          return true;
        }
      }
      return current.isName() && newNodes.contains(current);
    }
  }
  
  static class Ref implements StaticReference<JSType>  {
    Node node;
    final JSModule module;
    final StaticSourceFile source;
    final Name name;
    final Type type;
    final Scope scope;
    final int preOrderIndex;
    private Ref twin = null;
    Ref(NodeTraversal t, Node node, Name name, Type type, int index) {
      super();
      this.node = node;
      this.name = name;
      this.module = t.getInput() == null ? null : t.getInput().getModule();
      this.source = node.getStaticSourceFile();
      this.type = type;
      this.scope = t.getScope();
      this.preOrderIndex = index;
    }
    private Ref(Ref original, Type type, int index) {
      super();
      this.node = original.node;
      this.name = original.name;
      this.module = original.module;
      this.source = original.source;
      this.type = type;
      this.scope = original.scope;
      this.preOrderIndex = index;
    }
    private Ref(Type type, int index) {
      super();
      this.type = type;
      this.module = null;
      this.source = null;
      this.scope = null;
      this.name = null;
      this.preOrderIndex = index;
    }
    JSModule getModule() {
      return module;
    }
    @Override() public Node getNode() {
      return node;
    }
    Ref cloneAndReclassify(Type type) {
      return new Ref(this, type, this.preOrderIndex);
    }
    static Ref createRefForTesting(Type type) {
      return new Ref(type, -1);
    }
    Ref getTwin() {
      return twin;
    }
    @Override() public StaticSlot<JSType> getSymbol() {
      return name;
    }
    @Override() public StaticSourceFile getSourceFile() {
      return source;
    }
    String getSourceName() {
      return source == null ? "" : source.getName();
    }
    boolean isSet() {
      return type == Type.SET_FROM_GLOBAL || type == Type.SET_FROM_LOCAL;
    }
    static void markTwins(Ref a, Ref b) {
      Preconditions.checkArgument((a.type == Type.ALIASING_GET || b.type == Type.ALIASING_GET) && (a.type == Type.SET_FROM_GLOBAL || a.type == Type.SET_FROM_LOCAL || b.type == Type.SET_FROM_GLOBAL || b.type == Type.SET_FROM_LOCAL));
      a.twin = b;
      b.twin = a;
    }
    enum Type {
      SET_FROM_GLOBAL(),

      SET_FROM_LOCAL(),

      PROTOTYPE_GET(),

      ALIASING_GET(),

      DIRECT_GET(),

      CALL_GET(),

      DELETE_PROP(),

    ;
    private Type() {
    }
    }
  }
  
  static class Tracker implements CompilerPass  {
    final private AbstractCompiler compiler;
    final private PrintStream stream;
    final private Predicate<String> isInterestingSymbol;
    private Set<String> previousSymbolsInTree = ImmutableSet.of();
    Tracker(AbstractCompiler compiler, PrintStream stream, Predicate<String> isInterestingSymbol) {
      super();
      this.compiler = compiler;
      this.stream = stream;
      this.isInterestingSymbol = isInterestingSymbol;
    }
    @Override() public void process(Node externs, Node root) {
      GlobalNamespace namespace = new GlobalNamespace(compiler, externs, root);
      Set<String> currentSymbols = Sets.newTreeSet();
      for (String name : namespace.getNameIndex().keySet()) {
        if(isInterestingSymbol.apply(name)) {
          currentSymbols.add(name);
        }
      }
      String passName = compiler.getLastPassName();
      if(passName == null) {
        passName = "[Unknown pass]";
      }
      for (String sym : currentSymbols) {
        if(!previousSymbolsInTree.contains(sym)) {
          stream.println(String.format("%s: Added by %s", sym, passName));
        }
      }
      for (String sym : previousSymbolsInTree) {
        if(!currentSymbols.contains(sym)) {
          stream.println(String.format("%s: Removed by %s", sym, passName));
        }
      }
      previousSymbolsInTree = currentSymbols;
    }
  }
}