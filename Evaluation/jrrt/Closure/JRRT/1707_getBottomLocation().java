package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.VariableVisibilityAnalysis.VariableVisibility;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

class SideEffectsAnalysis implements CompilerPass  {
  final private static Predicate<Node> NOT_FUNCTION_PREDICATE = new Predicate<Node>() {
      @Override() public boolean apply(Node input) {
        return !input.isFunction();
      }
  };
  private AbstractCompiler compiler;
  private LocationAbstraction locationAbstraction;
  final private LocationAbstractionMode locationAbstractionIdentifier;
  public SideEffectsAnalysis(AbstractCompiler compiler) {
    this(compiler, LocationAbstractionMode.DEGENERATE);
  }
  public SideEffectsAnalysis(AbstractCompiler compiler, LocationAbstractionMode locationAbstractionMode) {
    super();
    this.compiler = compiler;
    this.locationAbstractionIdentifier = locationAbstractionMode;
  }
  private LocationAbstraction createVisibilityAbstraction(Node externs, Node root) {
    VariableVisibilityAnalysis variableVisibility = new VariableVisibilityAnalysis(compiler);
    variableVisibility.process(externs, root);
    VariableUseDeclarationMap variableMap = new VariableUseDeclarationMap(compiler);
    variableMap.mapUses(root);
    return new VisibilityLocationAbstraction(compiler, variableVisibility, variableMap);
  }
  private static Node closestControlDependentAncestor(Node node) {
    if(isControlDependentChild(node)) {
      return node;
    }
    for (Node ancestor : node.getAncestors()) {
      if(isControlDependentChild(ancestor)) {
        return ancestor;
      }
    }
    return null;
  }
  private static boolean isControlDependentChild(Node child) {
    Node parent = child.getParent();
    if(parent == null) {
      return false;
    }
    ArrayList<Node> siblings = Lists.newArrayList(parent.children());
    int indexOfChildInParent = siblings.indexOf(child);
    switch (parent.getType()){
      case Token.IF:
      case Token.HOOK:
      return (indexOfChildInParent == 1 || indexOfChildInParent == 2);
      case Token.WHILE:
      case Token.DO:
      return true;
      case Token.FOR:
      return indexOfChildInParent != 0;
      case Token.SWITCH:
      return indexOfChildInParent > 0;
      case Token.AND:
      return true;
      case Token.OR:
      return true;
      case Token.FUNCTION:
      return true;
      default:
      return false;
    }
  }
  private boolean isPure(Node node) {
    return false;
  }
  private static boolean nodeHasAncestor(Node node, Node possibleAncestor) {
    for (Node ancestor : node.getAncestors()) {
      if(ancestor == possibleAncestor) {
        return true;
      }
    }
    return false;
  }
  private boolean nodeHasCall(Node node) {
    return NodeUtil.has(node, new Predicate<Node>() {
        @Override() public boolean apply(Node input) {
          return input.isCall() || input.isNew();
        }
    }, NOT_FUNCTION_PREDICATE);
  }
  private static boolean nodesHaveSameControlFlow(Node node1, Node node2) {
    Node node1DeepestControlDependentBlock = closestControlDependentAncestor(node1);
    Node node2DeepestControlDependentBlock = closestControlDependentAncestor(node2);
    if(node1DeepestControlDependentBlock == node2DeepestControlDependentBlock) {
      if(node2DeepestControlDependentBlock != null) {
        if(node2DeepestControlDependentBlock.isCase()) {
          return false;
        }
        Predicate<Node> isEarlyExitPredicate = new Predicate<Node>() {
            @Override() public boolean apply(Node input) {
              int nodeType = input.getType();
              return nodeType == Token.RETURN || nodeType == Token.BREAK || nodeType == Token.CONTINUE;
            }
        };
        return !NodeUtil.has(node2DeepestControlDependentBlock, isEarlyExitPredicate, NOT_FUNCTION_PREDICATE);
      }
      else {
        return true;
      }
    }
    else {
      return false;
    }
  }
  public boolean safeToMoveBefore(Node source, AbstractMotionEnvironment environment, Node destination) {
    Preconditions.checkNotNull(locationAbstraction);
    Preconditions.checkArgument(!nodeHasAncestor(destination, source));
    if(isPure(source)) {
      return true;
    }
    if(nodeHasCall(source)) {
      return false;
    }
    LocationSummary sourceLocationSummary = locationAbstraction.calculateLocationSummary(source);
    EffectLocation sourceModSet = sourceLocationSummary.getModSet();
    if(!sourceModSet.isEmpty() && !nodesHaveSameControlFlow(source, destination)) {
      return false;
    }
    EffectLocation sourceRefSet = sourceLocationSummary.getRefSet();
    Set<Node> environmentNodes = environment.calculateEnvironment();
    for (Node environmentNode : environmentNodes) {
      if(nodeHasCall(environmentNode)) {
        return false;
      }
    }
    LocationSummary environmentLocationSummary = locationAbstraction.calculateLocationSummary(environmentNodes);
    EffectLocation environmentModSet = environmentLocationSummary.getModSet();
    EffectLocation environmentRefSet = environmentLocationSummary.getRefSet();
    if(!environmentModSet.intersectsLocation(sourceRefSet) && !environmentRefSet.intersectsLocation(sourceModSet) && !environmentModSet.intersectsLocation(sourceModSet)) {
      return true;
    }
    return false;
  }
  @Override() public void process(Node externs, Node root) {
    switch (locationAbstractionIdentifier){
      case DEGENERATE:
      locationAbstraction = new DegenerateLocationAbstraction();
      break ;
      case VISIBILITY_BASED:
      locationAbstraction = createVisibilityAbstraction(externs, root);
      break ;
      default:
      throw new IllegalStateException("Unrecognized location abstraction " + "identifier: " + locationAbstractionIdentifier);
    }
  }
  
  abstract public static class AbstractMotionEnvironment  {
    abstract public Set<Node> calculateEnvironment();
  }
  
  public static class CrossModuleMotionEnvironment extends AbstractMotionEnvironment  {
    public CrossModuleMotionEnvironment(Node sourceNode, JSModule sourceModule, Node destinationNode, JSModule destinationModule, JSModuleGraph moduleGraph) {
      super();
    }
    @Override() public Set<Node> calculateEnvironment() {
      return null;
    }
  }
  
  private static class DegenerateLocationAbstraction extends LocationAbstraction  {
    final private static EffectLocation EVERY_LOCATION = new DegenerateEffectLocation();
    final private static EffectLocation NO_LOCATION = new DegenerateEffectLocation();
    EffectLocation calculateModSet(Node node) {
      if(NodeUtil.mayHaveSideEffects(node)) {
        return EVERY_LOCATION;
      }
      else {
        return NO_LOCATION;
      }
    }
    EffectLocation calculateRefSet(Node node) {
      if(NodeUtil.canBeSideEffected(node)) {
        return EVERY_LOCATION;
      }
      else {
        return NO_LOCATION;
      }
    }
    @Override() EffectLocation getBottomLocation() {
      return NO_LOCATION;
    }
    @Override() public LocationSummary calculateLocationSummary(Node node) {
      return new LocationSummary(calculateModSet(node), calculateRefSet(node));
    }
    
    private static class DegenerateEffectLocation implements EffectLocation  {
      @Override() public EffectLocation join(EffectLocation otherLocation) {
        if(otherLocation == EVERY_LOCATION) {
          return otherLocation;
        }
        else {
          return this;
        }
      }
      @Override() public boolean intersectsLocation(EffectLocation otherLocation) {
        return this == EVERY_LOCATION && otherLocation == EVERY_LOCATION;
      }
      @Override() public boolean isEmpty() {
        return this == NO_LOCATION;
      }
    }
  }
  
  private static interface EffectLocation  {
    public EffectLocation join(EffectLocation otherLocation);
    public boolean intersectsLocation(EffectLocation otherLocation);
    public boolean isEmpty();
  }
  
  public static class IntraproceduralMotionEnvironment extends AbstractMotionEnvironment  {
    public IntraproceduralMotionEnvironment(ControlFlowGraph<Node> controlFlowGraph, Node cfgSource, Node cfgDestination) {
      super();
    }
    @Override() public Set<Node> calculateEnvironment() {
      return null;
    }
  }
  
  abstract private static class LocationAbstraction  {
    abstract EffectLocation getBottomLocation();
    abstract LocationSummary calculateLocationSummary(Node node);
    public LocationSummary calculateLocationSummary(Set<Node> nodes) {
      EffectLocation var_1707 = getBottomLocation();
      EffectLocation modAccumulator = var_1707;
      EffectLocation refAccumulator = getBottomLocation();
      for (Node node : nodes) {
        LocationSummary nodeLocationSummary = calculateLocationSummary(node);
        modAccumulator = modAccumulator.join(nodeLocationSummary.getModSet());
        refAccumulator = refAccumulator.join(nodeLocationSummary.getRefSet());
      }
      return new LocationSummary(modAccumulator, refAccumulator);
    }
  }
  enum LocationAbstractionMode {
    DEGENERATE(),

    VISIBILITY_BASED(),

  ;
  private LocationAbstractionMode() {
  }
  }
  
  private static class LocationSummary  {
    private EffectLocation modSet;
    private EffectLocation refSet;
    public LocationSummary(EffectLocation modSet, EffectLocation refSet) {
      super();
      this.modSet = modSet;
      this.refSet = refSet;
    }
    public EffectLocation getModSet() {
      return modSet;
    }
    public EffectLocation getRefSet() {
      return refSet;
    }
  }
  
  public static class RawMotionEnvironment extends AbstractMotionEnvironment  {
    Set<Node> environment;
    public RawMotionEnvironment(Set<Node> environment) {
      super();
      this.environment = environment;
    }
    @Override() public Set<Node> calculateEnvironment() {
      return environment;
    }
  }
  
  private static class VariableUseDeclarationMap  {
    private AbstractCompiler compiler;
    private Map<Node, Node> referencesByNameNode;
    public VariableUseDeclarationMap(AbstractCompiler compiler) {
      super();
      this.compiler = compiler;
    }
    public Node findDeclaringNameNodeForUse(Node usingNameNode) {
      Preconditions.checkArgument(usingNameNode.isName());
      return referencesByNameNode.get(usingNameNode);
    }
    public void mapUses(Node root) {
      referencesByNameNode = Maps.newHashMap();
      ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
      NodeTraversal.traverse(compiler, root, callback);
      for (Var variable : callback.getAllSymbols()) {
        ReferenceCollection referenceCollection = callback.getReferences(variable);
        for (Reference reference : referenceCollection.references) {
          Node referenceNameNode = reference.getNode();
          referencesByNameNode.put(referenceNameNode, variable.getNameNode());
        }
      }
    }
  }
  
  private static class VisibilityLocationAbstraction extends LocationAbstraction  {
    final private static int VISIBILITY_LOCATION_NONE = 0;
    final private static int UNKNOWN_LOCATION_MASK = 0xFFFFFFFF;
    final private static int LOCAL_VARIABLE_LOCATION_MASK = 1 << 1;
    final private static int CAPTURED_LOCAL_VARIABLE_LOCATION_MASK = 1 << 2;
    final private static int GLOBAL_VARIABLE_LOCATION_MASK = 1 << 3;
    final private static int HEAP_LOCATION_MASK = 1 << 4;
    AbstractCompiler compiler;
    VariableVisibilityAnalysis variableVisibilityAnalysis;
    VariableUseDeclarationMap variableUseMap;
    private VisibilityLocationAbstraction(AbstractCompiler compiler, VariableVisibilityAnalysis variableVisibilityAnalysis, VariableUseDeclarationMap variableUseMap) {
      super();
      this.compiler = compiler;
      this.variableVisibilityAnalysis = variableVisibilityAnalysis;
      this.variableUseMap = variableUseMap;
    }
    @Override() EffectLocation getBottomLocation() {
      return new VisibilityBasedEffectLocation(VISIBILITY_LOCATION_NONE);
    }
    @Override() LocationSummary calculateLocationSummary(Node node) {
      int visibilityRefLocations = VISIBILITY_LOCATION_NONE;
      int visibilityModLocations = VISIBILITY_LOCATION_NONE;
      for (Node reference : findStorageLocationReferences(node)) {
        int effectMask;
        if(reference.isName()) {
          effectMask = effectMaskForVariableReference(reference);
        }
        else {
          effectMask = HEAP_LOCATION_MASK;
        }
        if(storageNodeIsLValue(reference)) {
          visibilityModLocations |= effectMask;
        }
        if(storageNodeIsRValue(reference)) {
          visibilityRefLocations |= effectMask;
        }
      }
      VisibilityBasedEffectLocation modSet = new VisibilityBasedEffectLocation(visibilityModLocations);
      VisibilityBasedEffectLocation refSet = new VisibilityBasedEffectLocation(visibilityRefLocations);
      return new LocationSummary(modSet, refSet);
    }
    private Set<Node> findStorageLocationReferences(Node root) {
      final Set<Node> references = Sets.newHashSet();
      NodeTraversal.traverse(compiler, root, new AbstractShallowCallback() {
          @Override() public void visit(NodeTraversal t, Node n, Node parent) {
            if(NodeUtil.isGet(n) || (n.isName() && !parent.isFunction())) {
              references.add(n);
            }
          }
      });
      return references;
    }
    private static boolean isStorageNode(Node node) {
      return node.isName() || NodeUtil.isGet(node);
    }
    private static boolean storageNodeIsLValue(Node node) {
      Preconditions.checkArgument(isStorageNode(node));
      return NodeUtil.isLValue(node);
    }
    private static boolean storageNodeIsRValue(Node node) {
      Preconditions.checkArgument(isStorageNode(node));
      Node parent = node.getParent();
      if(storageNodeIsLValue(node)) {
        boolean nonSimpleAssign = NodeUtil.isAssignmentOp(parent) && !parent.isAssign();
        return (nonSimpleAssign || parent.isDec() || parent.isInc());
      }
      return true;
    }
    private int effectMaskForVariableReference(Node variableReference) {
      Preconditions.checkArgument(variableReference.isName());
      int effectMask = VISIBILITY_LOCATION_NONE;
      Node declaringNameNode = variableUseMap.findDeclaringNameNodeForUse(variableReference);
      if(declaringNameNode != null) {
        VariableVisibility visibility = variableVisibilityAnalysis.getVariableVisibility(declaringNameNode);
        switch (visibility){
          case LOCAL:
          effectMask = LOCAL_VARIABLE_LOCATION_MASK;
          break ;
          case CAPTURED_LOCAL:
          effectMask = CAPTURED_LOCAL_VARIABLE_LOCATION_MASK;
          break ;
          case PARAMETER:
          effectMask = HEAP_LOCATION_MASK;
          break ;
          case GLOBAL:
          effectMask = GLOBAL_VARIABLE_LOCATION_MASK;
          break ;
          default:
          throw new IllegalStateException("Unrecognized variable" + " visibility: " + visibility);
        }
      }
      else {
        effectMask = UNKNOWN_LOCATION_MASK;
      }
      return effectMask;
    }
    
    private static class VisibilityBasedEffectLocation implements EffectLocation  {
      int visibilityMask = VISIBILITY_LOCATION_NONE;
      public VisibilityBasedEffectLocation(int visibilityMask) {
        super();
        this.visibilityMask = visibilityMask;
      }
      @Override() public EffectLocation join(EffectLocation otherLocation) {
        Preconditions.checkArgument(otherLocation instanceof VisibilityBasedEffectLocation);
        int otherMask = ((VisibilityBasedEffectLocation)otherLocation).visibilityMask;
        int joinedMask = visibilityMask | otherMask;
        return new VisibilityBasedEffectLocation(joinedMask);
      }
      @Override() public boolean intersectsLocation(EffectLocation otherLocation) {
        Preconditions.checkArgument(otherLocation instanceof VisibilityBasedEffectLocation);
        int otherMask = ((VisibilityBasedEffectLocation)otherLocation).visibilityMask;
        return (visibilityMask & otherMask) > 0;
      }
      @Override() public boolean isEmpty() {
        return visibilityMask == VISIBILITY_LOCATION_NONE;
      }
    }
  }
}