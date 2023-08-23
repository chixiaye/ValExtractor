package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SpecializeModule implements CompilerPass  {
  private AbstractCompiler compiler;
  private Map<Node, Node> specializedInputRootsByOriginal;
  private Map<Node, OriginalFunctionInformation> functionInfoBySpecializedFunctionNode;
  private SpecializationState specializationState;
  final private PassFactory[] specializationPassFactories;
  public SpecializeModule(AbstractCompiler compiler, PassFactory ... specializationPassFactories) {
    super();
    this.compiler = compiler;
    this.specializationPassFactories = specializationPassFactories;
  }
  public Collection<JSModule> getDirectDependents(JSModule module) {
    Set<JSModule> directDependents = Sets.newHashSet();
    for (JSModule possibleDependent : compiler.getModuleGraph().getAllModules()) {
      if(possibleDependent.getDependencies().contains(module)) {
        directDependents.add(possibleDependent);
      }
    }
    return directDependents;
  }
  private Collection<SpecializationAwareCompilerPass> createSpecializingPasses() {
    Collection<SpecializationAwareCompilerPass> passes = Lists.newLinkedList();
    for (PassFactory passFactory : specializationPassFactories) {
      CompilerPass pass = passFactory.create(compiler);
      Preconditions.checkState(pass instanceof SpecializationAwareCompilerPass);
      passes.add((SpecializationAwareCompilerPass)pass);
    }
    return passes;
  }
  private Node copyModuleInputs(JSModule module) {
    specializedInputRootsByOriginal = Maps.newLinkedHashMap();
    functionInfoBySpecializedFunctionNode = Maps.newLinkedHashMap();
    Node syntheticModuleJsRoot = IR.block();
    syntheticModuleJsRoot.setIsSyntheticBlock(true);
    for (CompilerInput input : module.getInputs()) {
      Node originalInputRoot = input.getAstRoot(compiler);
      Node copiedInputRoot = originalInputRoot.cloneTree();
      copiedInputRoot.copyInformationFromForTree(originalInputRoot);
      specializedInputRootsByOriginal.put(originalInputRoot, copiedInputRoot);
      matchTopLevelFunctions(originalInputRoot, copiedInputRoot);
      syntheticModuleJsRoot.addChildToBack(copiedInputRoot);
    }
    Node syntheticExternsAndJsRoot = IR.block();
    syntheticExternsAndJsRoot.addChildToBack(syntheticModuleJsRoot);
    return syntheticModuleJsRoot;
  }
  private void addDummyVarDeclarationsToInitialModule(JSModule module) {
    for (Node modifiedFunction : functionInfoBySpecializedFunctionNode.keySet()) {
      if(specializationState.getRemovedFunctions().contains(modifiedFunction)) {
        OriginalFunctionInformation originalInfo = functionInfoBySpecializedFunctionNode.get(modifiedFunction);
        if(originalInfo.name != null && originalInfo.originalWasDeclaration()) {
          Node block = specializationState.removedFunctionToBlock.get(modifiedFunction);
          if(block != null) {
            Node originalRoot = specializedInputRootsByOriginal.get(block);
            block.addChildrenToBack(originalInfo.generateDummyDeclaration());
          }
        }
      }
    }
  }
  private void addOriginalFunctionVersionsToDependentModules(JSModule module) {
    for (JSModule directDependent : getDirectDependents(module)) {
      CompilerInput firstInput = directDependent.getInputs().get(0);
      Node firstInputRootNode = firstInput.getAstRoot(compiler);
      List<Node> possiblyModifiedFunctions = Lists.newArrayList(functionInfoBySpecializedFunctionNode.keySet());
      Collections.reverse(possiblyModifiedFunctions);
      for (Node modifiedFunction : possiblyModifiedFunctions) {
        boolean declarationWasSpecialized = specializationState.getSpecializedFunctions().contains(modifiedFunction);
        boolean declarationWasRemoved = specializationState.getRemovedFunctions().contains(modifiedFunction);
        if(declarationWasSpecialized || declarationWasRemoved) {
          OriginalFunctionInformation originalInfo = functionInfoBySpecializedFunctionNode.get(modifiedFunction);
          if(originalInfo.name != null) {
            Node newDefinition = originalInfo.generateFixupDefinition();
            firstInputRootNode.addChildrenToFront(newDefinition);
          }
        }
      }
    }
  }
  private void matchTopLevelFunctions(Node original, Node toBeSpecialized) {
    new NodeMatcher() {
        @Override() public void reportMatch(Node original, Node specialized) {
          if(original.isFunction()) {
            OriginalFunctionInformation functionInfo = new OriginalFunctionInformation(original);
            functionInfoBySpecializedFunctionNode.put(specialized, functionInfo);
          }
        }
        @Override() public boolean shouldTraverse(Node n1, Node n2) {
          return !n1.isFunction();
        }
    }.match(original, toBeSpecialized);
  }
  @Override() public void process(Node externs, Node root) {
    JSModuleGraph moduleGraph = compiler.getModuleGraph();
    if(moduleGraph == null) {
      return ;
    }
    JSModule module = moduleGraph.getRootModule();
    Node fakeModuleRoot = copyModuleInputs(module);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, fakeModuleRoot);
    SimpleFunctionAliasAnalysis initialModuleFunctionAliasAnalysis = new SimpleFunctionAliasAnalysis();
    initialModuleFunctionAliasAnalysis.analyze(defFinder);
    specializationState = new SpecializationState(initialModuleFunctionAliasAnalysis);
    do {
      specializationState.resetHasChanged();
      for (SpecializationAwareCompilerPass pass : createSpecializingPasses()) {
        pass.enableSpecialization(specializationState);
        pass.process(externs, fakeModuleRoot);
      }
    }while(specializationState.hasChanged());
    addDummyVarDeclarationsToInitialModule(module);
    replaceOriginalModuleInputsWithSpecialized();
    addOriginalFunctionVersionsToDependentModules(module);
  }
  private void replaceOriginalModuleInputsWithSpecialized() {
    for (Node original : specializedInputRootsByOriginal.keySet()) {
      Node specialized = specializedInputRootsByOriginal.get(original);
      original.removeChildren();
      List<Node> specializedChildren = Lists.newLinkedList();
      while(specialized.getFirstChild() != null){
        original.addChildToBack(specialized.removeFirstChild());
      }
    }
  }
  
  abstract private static class NodeMatcher  {
    public boolean shouldTraverse(Node node1, Node n2) {
      return true;
    }
    public void match(Node ast1, Node ast2) {
      reportMatch(ast1, ast2);
      if(shouldTraverse(ast1, ast2)) {
        Node childOf1 = ast1.getFirstChild();
        Node childOf2 = ast2.getFirstChild();
        while(childOf1 != null){
          match(childOf1, childOf2);
          childOf1 = childOf1.getNext();
          childOf2 = childOf2.getNext();
        }
      }
    }
    abstract public void reportMatch(Node n1, Node n2);
  }
  
  private class OriginalFunctionInformation  {
    private String name;
    private boolean isAssignFunction;
    private boolean assignHasVar;
    private Node originalFunctionCopy;
    public OriginalFunctionInformation(Node originalFunction) {
      super();
      name = NodeUtil.getFunctionName(originalFunction);
      originalFunctionCopy = originalFunction.cloneTree();
      originalFunctionCopy.copyInformationFromForTree(originalFunction);
      Node originalParent = originalFunction.getParent();
      isAssignFunction = originalParent.isAssign() || originalParent.isName();
      assignHasVar = isAssignFunction && originalParent.getParent().isVar();
    }
    private Node copiedOriginalFunction() {
      Node copy = originalFunctionCopy.cloneTree();
      copy.copyInformationFromForTree(originalFunctionCopy);
      return copy;
    }
    private Node generateDummyDeclaration() {
      Node declaration = NodeUtil.newVarNode(name, null);
      declaration.copyInformationFromForTree(originalFunctionCopy);
      return declaration;
    }
    private Node generateFixupDefinition() {
      Node functionCopy = copiedOriginalFunction();
      Node nameNode;
      if(isAssignFunction) {
        CodingConvention var_1650 = compiler.getCodingConvention();
        nameNode = NodeUtil.newQualifiedNameNode(var_1650, name, functionCopy, name);
      }
      else {
        nameNode = functionCopy.getFirstChild();
        functionCopy.replaceChild(nameNode, NodeUtil.newName(compiler.getCodingConvention(), "", nameNode));
      }
      Node assignment = IR.assign(nameNode, functionCopy);
      assignment.copyInformationFrom(functionCopy);
      return NodeUtil.newExpr(assignment);
    }
    private boolean originalWasDeclaration() {
      return (!isAssignFunction) || (assignHasVar);
    }
  }
  
  public static class SpecializationState  {
    private Set<Node> specializedFunctions;
    private Set<Node> removedFunctions;
    private Map<Node, Node> removedFunctionToBlock;
    private SimpleFunctionAliasAnalysis initialModuleAliasAnalysis;
    private boolean hasChanged = false;
    public SpecializationState(SimpleFunctionAliasAnalysis initialModuleAliasAnalysis) {
      super();
      this.initialModuleAliasAnalysis = initialModuleAliasAnalysis;
      specializedFunctions = Sets.newLinkedHashSet();
      removedFunctions = Sets.newLinkedHashSet();
      removedFunctionToBlock = Maps.newLinkedHashMap();
    }
    private Node containingFunction(Node node) {
      for (Node ancestor : node.getAncestors()) {
        if(ancestor.isFunction()) {
          return ancestor;
        }
      }
      return null;
    }
    public Set<Node> getRemovedFunctions() {
      return removedFunctions;
    }
    public Set<Node> getSpecializedFunctions() {
      return specializedFunctions;
    }
    public boolean canFixupFunction(Node functionNode) {
      Preconditions.checkArgument(functionNode.isFunction());
      if(!nodeIsInGlobalScope(functionNode) || initialModuleAliasAnalysis.isAliased(functionNode)) {
        return false;
      }
      if(NodeUtil.isStatement(functionNode)) {
        return true;
      }
      Node parent = functionNode.getParent();
      Node gramps = parent.getParent();
      if(parent.isName() && gramps.isVar()) {
        return true;
      }
      if(NodeUtil.isExprAssign(gramps) && parent.getChildAtIndex(1) == functionNode) {
        return true;
      }
      return false;
    }
    public boolean canFixupSpecializedFunctionContainingNode(Node n) {
      Node containingFunction = containingFunction(n);
      if(containingFunction != null) {
        return canFixupFunction(containingFunction);
      }
      else {
        return true;
      }
    }
    private boolean hasChanged() {
      return hasChanged;
    }
    private boolean nodeIsInGlobalScope(Node node) {
      return containingFunction(node) == null;
    }
    public void reportRemovedFunction(Node functionNode, Node declaringBlock) {
      if(removedFunctions.add(functionNode)) {
        hasChanged = true;
        removedFunctionToBlock.put(functionNode, declaringBlock);
      }
    }
    public void reportSpecializedFunction(Node functionNode) {
      if(specializedFunctions.add(functionNode)) {
        hasChanged = true;
      }
    }
    public void reportSpecializedFunctionContainingNode(Node node) {
      Node containingFunction = containingFunction(node);
      if(containingFunction != null) {
        reportSpecializedFunction(containingFunction);
      }
    }
    private void resetHasChanged() {
      hasChanged = false;
    }
  }
}