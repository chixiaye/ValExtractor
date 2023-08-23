package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.FunctionInjector.CanInlineResult;
import com.google.javascript.jscomp.FunctionInjector.InliningMode;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class InlineFunctions implements SpecializationAwareCompilerPass  {
  final private Map<String, FunctionState> fns = Maps.newHashMap();
  final private Map<Node, String> anonFns = Maps.newHashMap();
  final private AbstractCompiler compiler;
  final private FunctionInjector injector;
  final private boolean blockFunctionInliningEnabled;
  final private boolean inlineGlobalFunctions;
  final private boolean inlineLocalFunctions;
  final private boolean assumeMinimumCapture;
  private SpecializeModule.SpecializationState specializationState;
  InlineFunctions(AbstractCompiler compiler, Supplier<String> safeNameIdSupplier, boolean inlineGlobalFunctions, boolean inlineLocalFunctions, boolean blockFunctionInliningEnabled, boolean assumeStrictThis, boolean assumeMinimumCapture) {
    super();
    Preconditions.checkArgument(compiler != null);
    Preconditions.checkArgument(safeNameIdSupplier != null);
    this.compiler = compiler;
    this.inlineGlobalFunctions = inlineGlobalFunctions;
    this.inlineLocalFunctions = inlineLocalFunctions;
    this.blockFunctionInliningEnabled = blockFunctionInliningEnabled;
    this.assumeMinimumCapture = assumeMinimumCapture;
    this.injector = new FunctionInjector(compiler, safeNameIdSupplier, true, assumeStrictThis, assumeMinimumCapture);
  }
  FunctionState getOrCreateFunctionState(String fnName) {
    FunctionState fs = fns.get(fnName);
    if(fs == null) {
      fs = new FunctionState();
      fns.put(fnName, fs);
    }
    return fs;
  }
  private static Node getContainingFunction(NodeTraversal t) {
    return (t.inGlobalScope()) ? null : t.getScopeRoot();
  }
  private Set<String> findCalledFunctions(Node node) {
    Set<String> changed = Sets.newHashSet();
    findCalledFunctions(NodeUtil.getFunctionBody(node), changed);
    return changed;
  }
  private boolean hasLocalNames(Node fnNode) {
    Node block = NodeUtil.getFunctionBody(fnNode);
    return NodeUtil.getFunctionParameters(fnNode).hasChildren() || NodeUtil.has(block, new NodeUtil.MatchDeclaration(), new NodeUtil.MatchShallowStatement());
  }
  private boolean inliningLowersCost(FunctionState fs) {
    return injector.inliningLowersCost(fs.getModule(), fs.getFn().getFunctionNode(), fs.getReferences(), fs.getNamesToAlias(), fs.canRemove(), fs.getReferencesThis());
  }
  private boolean isCandidateFunction(Function fn) {
    String fnName = fn.getName();
    if(compiler.getCodingConvention().isExported(fnName)) {
      return false;
    }
    if(RenameProperties.RENAME_PROPERTY_FUNCTION_NAME.equals(fnName)) {
      return false;
    }
    if(specializationState != null && !specializationState.canFixupFunction(fn.getFunctionNode())) {
      return false;
    }
    Node fnNode = fn.getFunctionNode();
    return injector.doesFunctionMeetMinimumRequirements(fnName, fnNode);
  }
  static boolean isCandidateUsage(Node name) {
    Node parent = name.getParent();
    Preconditions.checkState(name.isName());
    if(parent.isVar() || parent.isFunction()) {
      return true;
    }
    if(parent.isCall() && parent.getFirstChild() == name) {
      return true;
    }
    if(NodeUtil.isGet(parent) && name == parent.getFirstChild() && name.getNext().isString() && name.getNext().getString().equals("call")) {
      Node gramps = name.getAncestor(2);
      if(gramps.isCall() && gramps.getFirstChild() == parent) {
        return true;
      }
    }
    return false;
  }
  private boolean mimimizeCost(FunctionState fs) {
    boolean var_1179 = inliningLowersCost(fs);
    if(!var_1179) {
      if(fs.hasBlockInliningReferences()) {
        fs.setRemove(false);
        fs.removeBlockInliningReferences();
        if(!fs.hasReferences() || !inliningLowersCost(fs)) {
          return false;
        }
      }
      else {
        return false;
      }
    }
    return true;
  }
  private void decomposeExpressions(Set<String> fnNames) {
    ExpressionDecomposer decomposer = new ExpressionDecomposer(compiler, compiler.getUniqueNameIdSupplier(), fnNames);
    for (FunctionState fs : fns.values()) {
      if(fs.canInline()) {
        for (Reference ref : fs.getReferences()) {
          if(ref.requiresDecomposition) {
            injector.maybePrepareCall(ref.callNode);
          }
        }
      }
    }
  }
  @Override() public void enableSpecialization(SpecializeModule.SpecializationState specializationState) {
    this.specializationState = specializationState;
  }
  private void findCalledFunctions(Node node, Set<String> changed) {
    Preconditions.checkArgument(changed != null);
    if(node.isName()) {
      if(isCandidateUsage(node)) {
        changed.add(node.getString());
      }
    }
    for(com.google.javascript.rhino.Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
  }
  private void maybeAddFunction(Function fn, JSModule module) {
    String name = fn.getName();
    FunctionState fs = getOrCreateFunctionState(name);
    if(fs.hasExistingFunctionDefinition()) {
      fs.setInline(false);
    }
    else {
      if(fs.canInline()) {
        fs.setFn(fn);
        if(injector.isDirectCallNodeReplacementPossible(fn.getFunctionNode())) {
          fs.inlineDirectly(true);
        }
        if(!isCandidateFunction(fn)) {
          fs.setInline(false);
        }
        if(fs.canInline()) {
          fs.setModule(module);
          Node fnNode = fn.getFunctionNode();
          Set<String> namesToAlias = FunctionArgumentInjector.findModifiedParameters(fnNode);
          if(!namesToAlias.isEmpty()) {
            fs.inlineDirectly(false);
            fs.setNamesToAlias(namesToAlias);
          }
          Node block = NodeUtil.getFunctionBody(fnNode);
          if(NodeUtil.referencesThis(block)) {
            fs.setReferencesThis(true);
          }
          if(NodeUtil.containsFunction(block)) {
            fs.setHasInnerFunctions(true);
            if(!assumeMinimumCapture && hasLocalNames(fnNode)) {
              fs.setInline(false);
            }
          }
        }
        if(fs.canInline() && !fs.canInlineDirectly()) {
          if(!blockFunctionInliningEnabled) {
            fs.setInline(false);
          }
        }
      }
    }
  }
  @Override() public void process(Node externs, Node root) {
    Preconditions.checkState(compiler.getLifeCycleStage().isNormalized());
    NodeTraversal.traverse(compiler, root, new FindCandidateFunctions());
    if(fns.isEmpty()) {
      return ;
    }
    NodeTraversal.traverse(compiler, root, new FindCandidatesReferences(fns, anonFns));
    trimCanidatesNotMeetingMinimumRequirements();
    if(fns.isEmpty()) {
      return ;
    }
    Set<String> fnNames = Sets.newHashSet(fns.keySet());
    injector.setKnownConstants(fnNames);
    trimCanidatesUsingOnCost();
    if(fns.isEmpty()) {
      return ;
    }
    resolveInlineConflicts();
    decomposeExpressions(fnNames);
    NodeTraversal.traverse(compiler, root, new CallVisitor(fns, anonFns, new Inline(injector, specializationState)));
    removeInlinedFunctions();
  }
  void removeInlinedFunctions() {
    for (FunctionState fs : fns.values()) {
      if(fs.canRemove()) {
        Function fn = fs.getFn();
        Preconditions.checkState(fs.canInline());
        Preconditions.checkState(fn != null);
        verifyAllReferencesInlined(fs);
        if(specializationState != null) {
          specializationState.reportRemovedFunction(fn.getFunctionNode(), fn.getDeclaringBlock());
        }
        fn.remove();
        compiler.reportCodeChange();
      }
    }
  }
  private void resolveInlineConflicts() {
    for (FunctionState fs : fns.values()) {
      resolveInlineConflictsForFunction(fs);
    }
  }
  private void resolveInlineConflictsForFunction(FunctionState fs) {
    if(!fs.hasReferences() || !fs.canInline()) {
      return ;
    }
    Node fnNode = fs.getFn().getFunctionNode();
    Set<String> names = findCalledFunctions(fnNode);
    if(!names.isEmpty()) {
      for (String name : names) {
        FunctionState fsCalled = fns.get(name);
        if(fsCalled != null && fsCalled.canRemove()) {
          fsCalled.setRemove(false);
          if(!mimimizeCost(fsCalled)) {
            fsCalled.setInline(false);
          }
        }
      }
      fs.setSafeFnNode(fs.getFn().getFunctionNode().cloneTree());
    }
  }
  private void trimCanidatesNotMeetingMinimumRequirements() {
    Iterator<Entry<String, FunctionState>> i;
    for(i = fns.entrySet().iterator(); i.hasNext(); ) {
      FunctionState fs = i.next().getValue();
      if(!fs.hasExistingFunctionDefinition() || !fs.canInline()) {
        i.remove();
      }
    }
  }
  void trimCanidatesUsingOnCost() {
    Iterator<Entry<String, FunctionState>> i;
    for(i = fns.entrySet().iterator(); i.hasNext(); ) {
      FunctionState fs = i.next().getValue();
      if(fs.hasReferences()) {
        boolean lowersCost = mimimizeCost(fs);
        if(!lowersCost) {
          i.remove();
        }
      }
      else 
        if(!fs.canRemove()) {
          i.remove();
        }
    }
  }
  void verifyAllReferencesInlined(FunctionState fs) {
    for (Reference ref : fs.getReferences()) {
      if(!ref.inlined) {
        throw new IllegalStateException("Call site missed.\n call: " + ref.callNode.toStringTree() + "\n parent:  " + ref.callNode.getParent().toStringTree());
      }
    }
  }
  
  private static class CallVisitor extends AbstractPostOrderCallback  {
    protected CallVisitorCallback callback;
    private Map<String, FunctionState> functionMap;
    private Map<Node, String> anonFunctionMap;
    CallVisitor(Map<String, FunctionState> fns, Map<Node, String> anonFns, CallVisitorCallback callback) {
      super();
      this.functionMap = fns;
      this.anonFunctionMap = anonFns;
      this.callback = callback;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.CALL:
        Node child = n.getFirstChild();
        String name = null;
        if(child.isName()) {
          name = child.getString();
        }
        else 
          if(child.isFunction()) {
            name = anonFunctionMap.get(child);
          }
          else 
            if(NodeUtil.isFunctionObjectCall(n)) {
              Preconditions.checkState(NodeUtil.isGet(child));
              Node fnIdentifingNode = child.getFirstChild();
              if(fnIdentifingNode.isName()) {
                name = fnIdentifingNode.getString();
              }
              else 
                if(fnIdentifingNode.isFunction()) {
                  name = anonFunctionMap.get(fnIdentifingNode);
                }
            }
        if(name != null) {
          FunctionState fs = functionMap.get(name);
          if(fs != null) {
            callback.visitCallSite(t, n, parent, fs);
          }
        }
        break ;
      }
    }
  }
  
  private interface CallVisitorCallback  {
    public void visitCallSite(NodeTraversal t, Node callNode, Node parent, FunctionState fs);
  }
  
  private class FindCandidateFunctions implements Callback  {
    private int callsSeen = 0;
    @Override() public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
      return inlineLocalFunctions || nodeTraversal.inGlobalScope();
    }
    public void findFunctionExpressions(NodeTraversal t, Node n) {
      switch (n.getType()){
        case Token.CALL:
        Node fnNode = null;
        if(n.getFirstChild().isFunction()) {
          fnNode = n.getFirstChild();
        }
        else 
          if(NodeUtil.isFunctionObjectCall(n)) {
            Node fnIdentifingNode = n.getFirstChild().getFirstChild();
            if(fnIdentifingNode.isFunction()) {
              fnNode = fnIdentifingNode;
            }
          }
        if(fnNode != null) {
          Function fn = new FunctionExpression(fnNode, callsSeen++);
          maybeAddFunction(fn, t.getModule());
          anonFns.put(fnNode, fn.getName());
        }
        break ;
      }
    }
    public void findNamedFunctions(NodeTraversal t, Node n, Node parent) {
      if(!NodeUtil.isStatement(n)) {
        return ;
      }
      switch (n.getType()){
        case Token.VAR:
        Preconditions.checkState(n.hasOneChild());
        Node nameNode = n.getFirstChild();
        if(nameNode.isName() && nameNode.hasChildren() && nameNode.getFirstChild().isFunction()) {
          maybeAddFunction(new FunctionVar(n), t.getModule());
        }
        break ;
        case Token.FUNCTION:
        Preconditions.checkState(NodeUtil.isStatementBlock(parent) || parent.isLabel());
        if(!NodeUtil.isFunctionExpression(n)) {
          Function fn = new NamedFunction(n);
          maybeAddFunction(fn, t.getModule());
        }
        break ;
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if((t.inGlobalScope() && inlineGlobalFunctions) || (!t.inGlobalScope() && inlineLocalFunctions)) {
        findNamedFunctions(t, n, parent);
        findFunctionExpressions(t, n);
      }
    }
  }
  
  private class FindCandidatesReferences extends CallVisitor implements CallVisitorCallback  {
    FindCandidatesReferences(Map<String, FunctionState> fns, Map<Node, String> anonFns) {
      super(fns, anonFns, null);
      this.callback = this;
    }
    private boolean maybeAddReferenceUsingMode(NodeTraversal t, FunctionState fs, Node callNode, JSModule module, InliningMode mode) {
      if(specializationState != null) {
        Node containingFunction = getContainingFunction(t);
        if(containingFunction != null && !specializationState.canFixupFunction(containingFunction)) {
          return false;
        }
      }
      CanInlineResult result = injector.canInlineReferenceToFunction(t, callNode, fs.getFn().getFunctionNode(), fs.getNamesToAlias(), mode, fs.getReferencesThis(), fs.hasInnerFunctions());
      if(result != CanInlineResult.NO) {
        boolean decompose = (result == CanInlineResult.AFTER_PREPARATION);
        fs.addReference(new Reference(callNode, module, mode, decompose));
        return true;
      }
      return false;
    }
    private void checkNameUsage(NodeTraversal t, Node n, Node parent) {
      Preconditions.checkState(n.isName());
      if(isCandidateUsage(n)) {
        return ;
      }
      String name = n.getString();
      FunctionState fs = fns.get(name);
      if(fs == null) {
        return ;
      }
      if(parent.isNew()) {
        Node target = parent.getFirstChild();
        if(target.isName() && target.getString().equals(ObjectPropertyStringPreprocess.EXTERN_OBJECT_PROPERTY_STRING)) {
          fs.setInline(false);
        }
      }
      if(parent.isAssign() && parent.getFirstChild() == n) {
        fs.setInline(false);
      }
      else {
        fs.setRemove(false);
      }
    }
    void maybeAddReference(NodeTraversal t, FunctionState fs, Node callNode, JSModule module) {
      if(!fs.canInline()) {
        return ;
      }
      boolean referenceAdded = false;
      InliningMode mode = fs.canInlineDirectly() ? InliningMode.DIRECT : InliningMode.BLOCK;
      referenceAdded = maybeAddReferenceUsingMode(t, fs, callNode, module, mode);
      if(!referenceAdded && mode == InliningMode.DIRECT && blockFunctionInliningEnabled) {
        mode = InliningMode.BLOCK;
        referenceAdded = maybeAddReferenceUsingMode(t, fs, callNode, module, mode);
      }
      if(!referenceAdded) {
        fs.setRemove(false);
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      super.visit(t, n, parent);
      if(n.isName()) {
        checkNameUsage(t, n, parent);
      }
    }
    @Override() public void visitCallSite(NodeTraversal t, Node callNode, Node parent, FunctionState fs) {
      maybeAddReference(t, fs, callNode, t.getModule());
    }
  }
  
  private static interface Function  {
    public Node getDeclaringBlock();
    public Node getFunctionNode();
    public String getName();
    public void remove();
  }
  
  private static class FunctionExpression implements Function  {
    final private Node fn;
    final private String fakeName;
    public FunctionExpression(Node fn, int index) {
      super();
      this.fn = fn;
      this.fakeName = String.valueOf(index);
    }
    @Override() public Node getDeclaringBlock() {
      return null;
    }
    @Override() public Node getFunctionNode() {
      return fn;
    }
    @Override() public String getName() {
      return fakeName;
    }
    @Override() public void remove() {
    }
  }
  
  private static class FunctionState  {
    private Function fn = null;
    private Node safeFnNode = null;
    private boolean inline = true;
    private boolean remove = true;
    private boolean inlineDirectly = false;
    private boolean referencesThis = false;
    private boolean hasInnerFunctions = false;
    private Map<Node, Reference> references = null;
    private JSModule module = null;
    private Set<String> namesToAlias = null;
    public Collection<Reference> getReferences() {
      return getReferencesInternal().values();
    }
    public Function getFn() {
      return fn;
    }
    public JSModule getModule() {
      return module;
    }
    private Map<Node, Reference> getReferencesInternal() {
      if(references == null) {
        return Collections.emptyMap();
      }
      return references;
    }
    public Node getSafeFnNode() {
      return (safeFnNode != null) ? safeFnNode : fn.getFunctionNode();
    }
    public Reference getReference(Node n) {
      return getReferencesInternal().get(n);
    }
    public Set<String> getNamesToAlias() {
      if(namesToAlias == null) {
        return Collections.emptySet();
      }
      return Collections.unmodifiableSet(namesToAlias);
    }
    public boolean canInline() {
      return inline;
    }
    public boolean canInlineDirectly() {
      return inlineDirectly;
    }
    public boolean canRemove() {
      return remove;
    }
    public boolean getReferencesThis() {
      return this.referencesThis;
    }
    public boolean hasBlockInliningReferences() {
      for (Reference r : getReferencesInternal().values()) {
        if(r.mode == InliningMode.BLOCK) {
          return true;
        }
      }
      return false;
    }
    boolean hasExistingFunctionDefinition() {
      return (fn != null);
    }
    public boolean hasInnerFunctions() {
      return hasInnerFunctions;
    }
    public boolean hasReferences() {
      return (references != null && !references.isEmpty());
    }
    public void addReference(Reference ref) {
      if(references == null) {
        references = Maps.newHashMap();
      }
      references.put(ref.callNode, ref);
    }
    public void inlineDirectly(boolean directReplacement) {
      this.inlineDirectly = directReplacement;
    }
    void removeBlockInliningReferences() {
      Iterator<Entry<Node, Reference>> i;
      for(i = getReferencesInternal().entrySet().iterator(); i.hasNext(); ) {
        Entry<Node, Reference> entry = i.next();
        if(entry.getValue().mode == InliningMode.BLOCK) {
          i.remove();
        }
      }
    }
    public void setFn(Function fn) {
      Preconditions.checkState(this.fn == null);
      this.fn = fn;
    }
    public void setHasInnerFunctions(boolean hasInnerFunctions) {
      this.hasInnerFunctions = hasInnerFunctions;
    }
    public void setInline(boolean inline) {
      this.inline = inline;
      if(inline == false) {
        references = null;
        remove = false;
      }
    }
    public void setModule(JSModule module) {
      this.module = module;
    }
    public void setNamesToAlias(Set<String> names) {
      namesToAlias = names;
    }
    public void setReferencesThis(boolean referencesThis) {
      this.referencesThis = referencesThis;
    }
    public void setRemove(boolean remove) {
      this.remove = remove;
    }
    public void setSafeFnNode(Node safeFnNode) {
      this.safeFnNode = safeFnNode;
    }
  }
  
  private static class FunctionVar implements Function  {
    final private Node var;
    public FunctionVar(Node var) {
      super();
      this.var = var;
    }
    @Override() public Node getDeclaringBlock() {
      return var.getParent();
    }
    @Override() public Node getFunctionNode() {
      return var.getFirstChild().getFirstChild();
    }
    @Override() public String getName() {
      return var.getFirstChild().getString();
    }
    @Override() public void remove() {
      NodeUtil.removeChild(var.getParent(), var);
    }
  }
  
  private static class Inline implements CallVisitorCallback  {
    final private FunctionInjector injector;
    final private SpecializeModule.SpecializationState specializationState;
    Inline(FunctionInjector injector, SpecializeModule.SpecializationState specializationState) {
      super();
      this.injector = injector;
      this.specializationState = specializationState;
    }
    private void inlineFunction(NodeTraversal t, Node callNode, FunctionState fs, InliningMode mode) {
      Function fn = fs.getFn();
      String fnName = fn.getName();
      Node fnNode = fs.getSafeFnNode();
      Node newCode = injector.inline(t, callNode, fnName, fnNode, mode);
      t.getCompiler().reportCodeChange();
      t.getCompiler().addToDebugLog("Inlined function: " + fn.getName());
    }
    @Override() public void visitCallSite(NodeTraversal t, Node callNode, Node parent, FunctionState fs) {
      Preconditions.checkState(fs.hasExistingFunctionDefinition());
      if(fs.canInline()) {
        Reference ref = fs.getReference(callNode);
        if(ref != null) {
          if(specializationState != null) {
            Node containingFunction = getContainingFunction(t);
            if(containingFunction != null) {
              specializationState.reportSpecializedFunction(containingFunction);
            }
          }
          inlineFunction(t, callNode, fs, ref.mode);
          ref.inlined = true;
        }
      }
    }
  }
  
  private static class NamedFunction implements Function  {
    final private Node fn;
    public NamedFunction(Node fn) {
      super();
      this.fn = fn;
    }
    @Override() public Node getDeclaringBlock() {
      return fn.getParent();
    }
    @Override() public Node getFunctionNode() {
      return fn;
    }
    @Override() public String getName() {
      return fn.getFirstChild().getString();
    }
    @Override() public void remove() {
      NodeUtil.removeChild(fn.getParent(), fn);
    }
  }
  
  class Reference extends FunctionInjector.Reference  {
    final boolean requiresDecomposition;
    boolean inlined = false;
    Reference(Node callNode, JSModule module, InliningMode mode, boolean decompose) {
      super(callNode, module, mode);
      this.requiresDecomposition = decompose;
    }
  }
}