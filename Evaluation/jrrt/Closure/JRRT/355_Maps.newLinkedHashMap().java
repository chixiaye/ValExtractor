package com.google.javascript.jscomp;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.DefinitionsRemover.Definition;
import com.google.javascript.jscomp.NameReferenceGraph.Name;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.graph.DiGraph;
import com.google.javascript.jscomp.graph.LinkedDirectedGraph;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class CallGraph implements CompilerPass  {
  private AbstractCompiler compiler;
  private Map<Node, Callsite> callsitesByNode;
  private Map<Node, Function> functionsByNode;
  private boolean computeBackwardGraph;
  private boolean computeForwardGraph;
  private boolean useNameReferenceGraph = false;
  private boolean alreadyRun = false;
  @VisibleForTesting() final public static String MAIN_FUNCTION_NAME = "{main}";
  private Function mainFunction;
  public CallGraph(AbstractCompiler compiler) {
    this(compiler, true, true);
  }
  public CallGraph(AbstractCompiler compiler, boolean computeForwardGraph, boolean computeBackwardGraph) {
    super();
    Preconditions.checkArgument(computeForwardGraph || computeBackwardGraph);
    this.compiler = compiler;
    this.computeForwardGraph = computeForwardGraph;
    this.computeBackwardGraph = computeBackwardGraph;
    java.util.LinkedHashMap<Node, Callsite> var_355 = Maps.newLinkedHashMap();
    callsitesByNode = var_355;
    functionsByNode = Maps.newLinkedHashMap();
  }
  private Callsite createCallsite(Node callsiteNode) {
    Callsite callsite = new Callsite(callsiteNode);
    callsitesByNode.put(callsiteNode, callsite);
    return callsite;
  }
  public Callsite getCallsiteForAstNode(Node callsiteNode) {
    Preconditions.checkArgument(callsiteNode.isCall() || callsiteNode.isNew());
    return callsitesByNode.get(callsiteNode);
  }
  public Collection<Callsite> getAllCallsites() {
    return callsitesByNode.values();
  }
  private Collection<Definition> lookupDefinitionsForTargetsOfCall(Node callsite, DefinitionProvider definitionProvider) {
    Preconditions.checkArgument(callsite.isCall() || callsite.isNew());
    Node targetExpression = callsite.getFirstChild();
    if(!useNameReferenceGraph || (targetExpression.isGetProp() || targetExpression.isName())) {
      Collection<Definition> definitions = definitionProvider.getDefinitionsReferencedAt(targetExpression);
      if(definitions != null && !definitions.isEmpty()) {
        return definitions;
      }
    }
    return null;
  }
  public Collection<Function> getAllFunctions() {
    return functionsByNode.values();
  }
  private DefinitionProvider constructDefinitionProvider(Node externsRoot, Node jsRoot) {
    if(useNameReferenceGraph) {
      NameReferenceGraphConstruction graphConstruction = new NameReferenceGraphConstruction(compiler);
      graphConstruction.process(externsRoot, jsRoot);
      return graphConstruction.getNameReferenceGraph();
    }
    else {
      SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
      defFinder.process(externsRoot, jsRoot);
      return defFinder;
    }
  }
  private DiGraph<Function, Callsite> constructDirectedGraph(boolean forward) {
    DiGraph<Function, Callsite> digraph = LinkedDirectedGraph.createWithoutAnnotations();
    for (Function function : getAllFunctions()) {
      digraph.createNode(function);
    }
    if(computeForwardGraph) {
      for (Function caller : getAllFunctions()) {
        for (Callsite callsite : caller.getCallsitesInFunction()) {
          for (Function callee : callsite.getPossibleTargets()) {
            digraphConnect(digraph, caller, callsite, callee, forward);
          }
        }
      }
    }
    else {
      for (Function callee : getAllFunctions()) {
        for (Callsite callsite : callee.getCallsitesPossiblyTargetingFunction()) {
          Function caller = callsite.getContainingFunction();
          digraphConnect(digraph, caller, callsite, callee, forward);
        }
      }
    }
    return digraph;
  }
  public DiGraph<Function, Callsite> getBackwardDirectedGraph() {
    return constructDirectedGraph(false);
  }
  public DiGraph<Function, Callsite> getForwardDirectedGraph() {
    return constructDirectedGraph(true);
  }
  private Function createFunction(Node functionNode) {
    Function function = new Function(functionNode);
    functionsByNode.put(functionNode, function);
    return function;
  }
  public Function getFunctionForAstNode(Node functionNode) {
    Preconditions.checkArgument(functionNode.isFunction());
    return functionsByNode.get(functionNode);
  }
  public Function getMainFunction() {
    return mainFunction;
  }
  @VisibleForTesting() public Function getUniqueFunctionWithName(final String desiredName) {
    Collection<Function> functions = Collections2.<Function>filter(getAllFunctions(), new Predicate<Function>() {
        @Override() public boolean apply(Function function) {
          String functionName = function.getName();
          if(functionName != null && desiredName != null) {
            return desiredName.equals(functionName);
          }
          else {
            return desiredName == functionName;
          }
        }
    });
    if(functions.size() == 1) {
      return functions.iterator().next();
    }
    else {
      throw new IllegalStateException("Found " + functions.size() + " functions with name " + desiredName);
    }
  }
  private Function lookupFunctionForDefinition(Definition definition) {
    if(definition != null && !definition.isExtern()) {
      Node rValue = definition.getRValue();
      if(rValue != null && rValue.isFunction()) {
        Function function = functionsByNode.get(rValue);
        Preconditions.checkNotNull(function);
        return function;
      }
    }
    return null;
  }
  private void connectCallsiteToTargets(Callsite callsite, DefinitionProvider definitionProvider) {
    Collection<Definition> definitions = lookupDefinitionsForTargetsOfCall(callsite.getAstNode(), definitionProvider);
    if(definitions == null) {
      callsite.hasUnknownTarget = true;
    }
    else {
      for (Definition definition : definitions) {
        if(definition.isExtern()) {
          callsite.hasExternTarget = true;
        }
        else {
          Node target = definition.getRValue();
          if(target != null && target.isFunction()) {
            Function targetFunction = functionsByNode.get(target);
            if(targetFunction == null) {
              targetFunction = createFunction(target);
            }
            if(computeForwardGraph) {
              callsite.addPossibleTarget(targetFunction);
            }
            if(computeBackwardGraph) {
              targetFunction.addCallsitePossiblyTargetingFunction(callsite);
            }
          }
          else {
            callsite.hasUnknownTarget = true;
          }
        }
      }
    }
  }
  private void createFunctionsAndCallsites(Node jsRoot, final DefinitionProvider provider) {
    mainFunction = createFunction(jsRoot);
    NodeTraversal.traverse(compiler, jsRoot, new AbstractPostOrderCallback() {
        @Override() public void visit(NodeTraversal t, Node n, Node parent) {
          int nodeType = n.getType();
          if(nodeType == Token.CALL || nodeType == Token.NEW) {
            Callsite callsite = createCallsite(n);
            Node containingFunctionNode = t.getScopeRoot();
            Function containingFunction = functionsByNode.get(containingFunctionNode);
            if(containingFunction == null) {
              containingFunction = createFunction(containingFunctionNode);
            }
            callsite.containingFunction = containingFunction;
            containingFunction.addCallsiteInFunction(callsite);
            connectCallsiteToTargets(callsite, provider);
          }
          else 
            if(n.isFunction()) {
              if(!functionsByNode.containsKey(n)) {
                createFunction(n);
              }
            }
        }
    });
  }
  private static void digraphConnect(DiGraph<Function, Callsite> digraph, Function caller, Callsite callsite, Function callee, boolean forward) {
    Function source;
    Function destination;
    if(forward) {
      source = caller;
      destination = callee;
    }
    else {
      source = callee;
      destination = caller;
    }
    digraph.connect(source, callsite, destination);
  }
  private void fillInFunctionInformation(DefinitionProvider provider) {
    if(useNameReferenceGraph) {
      NameReferenceGraph referenceGraph = (NameReferenceGraph)provider;
      for (Function function : getAllFunctions()) {
        if(!function.isMain()) {
          String functionName = function.getName();
          if(functionName != null) {
            Name symbol = referenceGraph.getSymbol(functionName);
            updateFunctionForName(function, symbol);
          }
        }
      }
    }
    else {
      SimpleDefinitionFinder finder = (SimpleDefinitionFinder)provider;
      for (DefinitionSite definitionSite : finder.getDefinitionSites()) {
        Definition definition = definitionSite.definition;
        Function function = lookupFunctionForDefinition(definition);
        if(function != null) {
          for (UseSite useSite : finder.getUseSites(definition)) {
            updateFunctionForUse(function, useSite.node);
          }
        }
      }
    }
  }
  @Override() public void process(Node externsRoot, Node jsRoot) {
    Preconditions.checkState(alreadyRun == false);
    DefinitionProvider definitionProvider = constructDefinitionProvider(externsRoot, jsRoot);
    createFunctionsAndCallsites(jsRoot, definitionProvider);
    fillInFunctionInformation(definitionProvider);
    alreadyRun = true;
  }
  private void updateFunctionForName(Function function, Name name) {
    if(name.isAliased()) {
      function.isAliased = true;
    }
    if(name.exposedToCallOrApply()) {
      function.isExposedToCallOrApply = true;
    }
  }
  private void updateFunctionForUse(Function function, Node useNode) {
    Node useParent = useNode.getParent();
    int parentType = useParent.getType();
    if((parentType == Token.CALL || parentType == Token.NEW) && useParent.getFirstChild() == useNode) {
    }
    else 
      if(NodeUtil.isGet(useParent)) {
        if(useParent.isGetProp()) {
          Node gramps = useParent.getParent();
          if(NodeUtil.isFunctionObjectApply(gramps) || NodeUtil.isFunctionObjectCall(gramps)) {
            function.isExposedToCallOrApply = true;
          }
        }
      }
      else {
        function.isAliased = true;
      }
  }
  
  public class Callsite  {
    private Node astNode;
    private boolean hasUnknownTarget = false;
    private boolean hasExternTarget = false;
    private Function containingFunction = null;
    private Collection<Function> possibleTargets;
    private Callsite(Node callsiteAstNode) {
      super();
      astNode = callsiteAstNode;
    }
    public Collection<Function> getPossibleTargets() {
      if(computeForwardGraph) {
        if(possibleTargets != null) {
          return possibleTargets;
        }
        else {
          return ImmutableList.of();
        }
      }
      else {
        throw new UnsupportedOperationException("Cannot call " + "getPossibleTargets() on a Callsite from a non-forward " + "CallGraph");
      }
    }
    public Function getContainingFunction() {
      return containingFunction;
    }
    public Node getAstNode() {
      return astNode;
    }
    public boolean hasExternTarget() {
      return hasExternTarget;
    }
    public boolean hasUnknownTarget() {
      return hasUnknownTarget;
    }
    private void addPossibleTarget(Function target) {
      Preconditions.checkState(computeForwardGraph);
      if(possibleTargets == null) {
        possibleTargets = new LinkedList<Function>();
      }
      possibleTargets.add(target);
    }
  }
  
  public class Function  {
    private Node astNode;
    private boolean isAliased = false;
    private boolean isExposedToCallOrApply = false;
    private Collection<Callsite> callsitesInFunction;
    private Collection<Callsite> callsitesPossiblyTargetingFunction;
    private Function(Node functionAstNode) {
      super();
      astNode = functionAstNode;
    }
    public Collection<Callsite> getCallsitesInFunction() {
      if(callsitesInFunction != null) {
        return callsitesInFunction;
      }
      else {
        return ImmutableList.of();
      }
    }
    public Collection<Callsite> getCallsitesPossiblyTargetingFunction() {
      if(computeBackwardGraph) {
        if(callsitesPossiblyTargetingFunction != null) {
          return callsitesPossiblyTargetingFunction;
        }
        else {
          return ImmutableList.of();
        }
      }
      else {
        throw new UnsupportedOperationException("Cannot call " + "getCallsitesPossiblyTargetingFunction() on a Function " + "from a non-backward CallGraph");
      }
    }
    public Node getAstNode() {
      return astNode;
    }
    public Node getBodyNode() {
      if(isMain()) {
        return astNode;
      }
      else {
        return NodeUtil.getFunctionBody(astNode);
      }
    }
    public String getName() {
      if(isMain()) {
        return MAIN_FUNCTION_NAME;
      }
      else {
        return NodeUtil.getFunctionName(astNode);
      }
    }
    public boolean isAliased() {
      return isAliased;
    }
    public boolean isExposedToCallOrApply() {
      return isExposedToCallOrApply;
    }
    public boolean isMain() {
      return (this == CallGraph.this.mainFunction);
    }
    private void addCallsiteInFunction(Callsite callsite) {
      if(callsitesInFunction == null) {
        callsitesInFunction = new LinkedList<Callsite>();
      }
      callsitesInFunction.add(callsite);
    }
    private void addCallsitePossiblyTargetingFunction(Callsite callsite) {
      Preconditions.checkState(computeBackwardGraph);
      if(callsitesPossiblyTargetingFunction == null) {
        callsitesPossiblyTargetingFunction = new LinkedList<Callsite>();
      }
      callsitesPossiblyTargetingFunction.add(callsite);
    }
  }
}