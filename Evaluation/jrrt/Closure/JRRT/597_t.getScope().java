package com.google.javascript.jscomp;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.NodeTraversal.ScopedCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.StaticReference;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import com.google.javascript.rhino.jstype.StaticSymbolTable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ReferenceCollectingCallback implements ScopedCallback, HotSwapCompilerPass, StaticSymbolTable<Var, ReferenceCollectingCallback.Reference>  {
  final private Map<Var, ReferenceCollection> referenceMap = Maps.newHashMap();
  final private Deque<BasicBlock> blockStack = new ArrayDeque<BasicBlock>();
  final private Behavior behavior;
  final private AbstractCompiler compiler;
  final private Predicate<Var> varFilter;
  static Behavior DO_NOTHING_BEHAVIOR = new Behavior() {
      @Override() public void afterExitScope(NodeTraversal t, ReferenceMap referenceMap) {
      }
  };
  ReferenceCollectingCallback(AbstractCompiler compiler, Behavior behavior) {
    this(compiler, behavior, Predicates.<Var>alwaysTrue());
  }
  ReferenceCollectingCallback(AbstractCompiler compiler, Behavior behavior, Predicate<Var> varFilter) {
    super();
    this.compiler = compiler;
    this.behavior = behavior;
    this.varFilter = varFilter;
  }
  @Override() public Iterable<Var> getAllSymbols() {
    return referenceMap.keySet();
  }
  @Override() public ReferenceCollection getReferences(Var v) {
    return referenceMap.get(v);
  }
  @Override() public Scope getScope(Var var) {
    return var.scope;
  }
  private static boolean isBlockBoundary(Node n, Node parent) {
    if(parent != null) {
      switch (parent.getType()){
        case Token.DO:
        case Token.FOR:
        case Token.TRY:
        case Token.WHILE:
        case Token.WITH:
        return true;
        case Token.AND:
        case Token.HOOK:
        case Token.IF:
        case Token.OR:
        return n != parent.getFirstChild();
      }
    }
    return n.isCase();
  }
  @Override() public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
    if(isBlockBoundary(n, parent)) {
      blockStack.push(new BasicBlock(blockStack.peek(), n));
    }
    return true;
  }
  private void addReference(NodeTraversal t, Var v, Reference reference) {
    ReferenceCollection referenceInfo = referenceMap.get(v);
    if(referenceInfo == null) {
      referenceInfo = new ReferenceCollection();
      referenceMap.put(v, referenceInfo);
    }
    referenceInfo.add(reference, t, v);
  }
  @Override() public void enterScope(NodeTraversal t) {
    Node n = t.getScope().getRootNode();
    BasicBlock parent = blockStack.isEmpty() ? null : blockStack.peek();
    blockStack.push(new BasicBlock(parent, n));
  }
  @Override() public void exitScope(NodeTraversal t) {
    blockStack.pop();
    if(t.getScope().isGlobal()) {
      compiler.updateGlobalVarReferences(referenceMap, t.getScopeRoot());
      behavior.afterExitScope(t, compiler.getGlobalVarReferences());
    }
    else {
      behavior.afterExitScope(t, new ReferenceMapWrapper(referenceMap));
    }
  }
  @Override() public void hotSwapScript(Node scriptRoot, Node originalRoot) {
    NodeTraversal.traverse(compiler, scriptRoot, this);
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(externs, root), this);
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    if(n.isName()) {
      Var v;
      if(n.getString().equals("arguments")) {
        Scope var_597 = t.getScope();
        v = var_597.getArgumentsVar();
      }
      else {
        v = t.getScope().getVar(n.getString());
      }
      if(v != null && varFilter.apply(v)) {
        addReference(t, v, new Reference(n, t, blockStack.peek()));
      }
    }
    if(isBlockBoundary(n, parent)) {
      blockStack.pop();
    }
  }
  
  final static class BasicBlock  {
    final private BasicBlock parent;
    final private boolean isHoisted;
    final private boolean isFunction;
    final private boolean isLoop;
    BasicBlock(BasicBlock parent, Node root) {
      super();
      this.parent = parent;
      this.isHoisted = NodeUtil.isHoistedFunctionDeclaration(root);
      this.isFunction = root.isFunction();
      if(root.getParent() != null) {
        int pType = root.getParent().getType();
        this.isLoop = pType == Token.DO || pType == Token.WHILE || pType == Token.FOR;
      }
      else {
        this.isLoop = false;
      }
    }
    BasicBlock getParent() {
      return parent;
    }
    boolean isGlobalScopeBlock() {
      return getParent() == null;
    }
    boolean provablyExecutesBefore(BasicBlock thatBlock) {
      BasicBlock currentBlock;
      for(currentBlock = thatBlock; currentBlock != null && currentBlock != this; currentBlock = currentBlock.getParent()) {
        if(currentBlock.isHoisted) {
          return false;
        }
      }
      if(currentBlock == this) {
        return true;
      }
      if(isGlobalScopeBlock() && thatBlock.isGlobalScopeBlock()) {
        return true;
      }
      return false;
    }
  }
  
  interface Behavior  {
    void afterExitScope(NodeTraversal t, ReferenceMap referenceMap);
  }
  
  final static class Reference implements StaticReference<JSType>  {
    final private static Set<Integer> DECLARATION_PARENTS = ImmutableSet.of(Token.VAR, Token.FUNCTION, Token.CATCH);
    final private Node nameNode;
    final private BasicBlock basicBlock;
    final private Scope scope;
    final private InputId inputId;
    final private StaticSourceFile sourceFile;
    private Reference(Node nameNode, BasicBlock basicBlock, Scope scope, InputId inputId) {
      super();
      this.nameNode = nameNode;
      this.basicBlock = basicBlock;
      this.scope = scope;
      this.inputId = inputId;
      this.sourceFile = nameNode.getStaticSourceFile();
    }
    Reference(Node nameNode, NodeTraversal t, BasicBlock basicBlock) {
      this(nameNode, basicBlock, t.getScope(), t.getInput().getInputId());
    }
    BasicBlock getBasicBlock() {
      return basicBlock;
    }
    public InputId getInputId() {
      return inputId;
    }
    Node getAssignedValue() {
      Node parent = getParent();
      return (parent.isFunction()) ? parent : NodeUtil.getAssignedValue(nameNode);
    }
    Node getGrandparent() {
      Node parent = getParent();
      return parent == null ? null : parent.getParent();
    }
    @Override() public Node getNode() {
      return nameNode;
    }
    Node getParent() {
      return getNode().getParent();
    }
    Reference cloneWithNewScope(Scope newScope) {
      return new Reference(nameNode, basicBlock, newScope, inputId);
    }
    @VisibleForTesting() static Reference createRefForTest(CompilerInput input) {
      return new Reference(new Node(Token.NAME), null, null, input.getInputId());
    }
    static Reference newBleedingFunction(NodeTraversal t, BasicBlock basicBlock, Node func) {
      return new Reference(func.getFirstChild(), basicBlock, t.getScope(), t.getInput().getInputId());
    }
    Scope getScope() {
      return scope;
    }
    @Override() public StaticSourceFile getSourceFile() {
      return sourceFile;
    }
    @Override() public Var getSymbol() {
      return scope.getVar(nameNode.getString());
    }
    boolean isDeclaration() {
      Node parent = getParent();
      Node grandparent = parent.getParent();
      return DECLARATION_PARENTS.contains(parent.getType()) || parent.isParamList() && grandparent.isFunction();
    }
    boolean isHoistedFunction() {
      return NodeUtil.isHoistedFunctionDeclaration(getParent());
    }
    boolean isInitializingDeclaration() {
      return isDeclaration() && !getParent().isVar() || nameNode.getFirstChild() != null;
    }
    private static boolean isLhsOfForInExpression(Node n) {
      Node parent = n.getParent();
      if(parent.isVar()) {
        return isLhsOfForInExpression(parent);
      }
      return NodeUtil.isForIn(parent) && parent.getFirstChild() == n;
    }
    boolean isLvalue() {
      Node parent = getParent();
      int parentType = parent.getType();
      return (parentType == Token.VAR && nameNode.getFirstChild() != null) || parentType == Token.INC || parentType == Token.DEC || (NodeUtil.isAssignmentOp(parent) && parent.getFirstChild() == nameNode) || isLhsOfForInExpression(nameNode);
    }
    boolean isSimpleAssignmentToName() {
      Node parent = getParent();
      return parent.isAssign() && parent.getFirstChild() == nameNode;
    }
    boolean isVarDeclaration() {
      return getParent().isVar();
    }
  }
  
  static class ReferenceCollection implements Iterable<Reference>  {
    List<Reference> references = Lists.newArrayList();
    @Override() public Iterator<Reference> iterator() {
      return references.iterator();
    }
    Reference getInitializingReference() {
      if(isInitializingDeclarationAt(0)) {
        return references.get(0);
      }
      else 
        if(isInitializingAssignmentAt(1)) {
          return references.get(1);
        }
      return null;
    }
    Reference getInitializingReferenceForConstants() {
      int size = references.size();
      for(int i = 0; i < size; i++) {
        if(isInitializingDeclarationAt(i) || isInitializingAssignmentAt(i)) {
          return references.get(i);
        }
      }
      return null;
    }
    private Reference getOneAndOnlyAssignment() {
      Reference assignment = null;
      int size = references.size();
      for(int i = 0; i < size; i++) {
        Reference ref = references.get(i);
        if(ref.isLvalue() || ref.isInitializingDeclaration()) {
          if(assignment == null) {
            assignment = ref;
          }
          else {
            return null;
          }
        }
      }
      return assignment;
    }
    boolean firstReferenceIsAssigningDeclaration() {
      int size = references.size();
      if(size > 0 && references.get(0).isInitializingDeclaration()) {
        return true;
      }
      return false;
    }
    boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if(ref == null) {
        return false;
      }
      for(com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock block = ref.getBasicBlock(); block != null; block = block.getParent()) {
        if(block.isFunction) {
          break ;
        }
        else 
          if(block.isLoop) {
            return false;
          }
      }
      return true;
    }
    boolean isEscaped() {
      Scope scope = null;
      for (Reference ref : references) {
        if(scope == null) {
          scope = ref.scope;
        }
        else 
          if(scope != ref.scope) {
            return true;
          }
      }
      return false;
    }
    private boolean isInitializingAssignmentAt(int index) {
      if(index < references.size() && index > 0) {
        Reference maybeDecl = references.get(index - 1);
        if(maybeDecl.isVarDeclaration()) {
          Preconditions.checkState(!maybeDecl.isInitializingDeclaration());
          Reference maybeInit = references.get(index);
          if(maybeInit.isSimpleAssignmentToName()) {
            return true;
          }
        }
      }
      return false;
    }
    private boolean isInitializingDeclarationAt(int index) {
      Reference maybeInit = references.get(index);
      if(maybeInit.isInitializingDeclaration()) {
        return true;
      }
      return false;
    }
    boolean isNeverAssigned() {
      int size = references.size();
      for(int i = 0; i < size; i++) {
        Reference ref = references.get(i);
        if(ref.isLvalue() || ref.isInitializingDeclaration()) {
          return false;
        }
      }
      return true;
    }
    protected boolean isWellDefined() {
      int size = references.size();
      if(size == 0) {
        return false;
      }
      Reference init = getInitializingReference();
      if(init == null) {
        return false;
      }
      Preconditions.checkState(references.get(0).isDeclaration());
      BasicBlock initBlock = init.getBasicBlock();
      for(int i = 1; i < size; i++) {
        if(!initBlock.provablyExecutesBefore(references.get(i).getBasicBlock())) {
          return false;
        }
      }
      return true;
    }
    void add(Reference reference, NodeTraversal t, Var v) {
      references.add(reference);
    }
  }
  
  interface ReferenceMap  {
    ReferenceCollection getReferences(Var var);
  }
  
  private static class ReferenceMapWrapper implements ReferenceMap  {
    final private Map<Var, ReferenceCollection> referenceMap;
    public ReferenceMapWrapper(Map<Var, ReferenceCollection> referenceMap) {
      super();
      this.referenceMap = referenceMap;
    }
    @Override() public ReferenceCollection getReferences(Var var) {
      return referenceMap.get(var);
    }
  }
}