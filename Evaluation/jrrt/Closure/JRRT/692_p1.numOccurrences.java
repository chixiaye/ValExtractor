package com.google.javascript.jscomp;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.TypeValidator.TypeMismatch;
import com.google.javascript.jscomp.graph.AdjacencyGraph;
import com.google.javascript.jscomp.graph.Annotation;
import com.google.javascript.jscomp.graph.GraphColoring;
import com.google.javascript.jscomp.graph.GraphColoring.GreedyGraphColoring;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.jscomp.graph.SubGraph;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

class AmbiguateProperties implements CompilerPass  {
  final private static Logger logger = Logger.getLogger(AmbiguateProperties.class.getName());
  final private AbstractCompiler compiler;
  final private List<Node> stringNodesToRename = Lists.newArrayList();
  final private char[] reservedCharacters;
  final private Map<String, Property> propertyMap = Maps.newHashMap();
  final private Set<String> externedNames = Sets.newHashSet();
  final private Set<String> quotedNames = Sets.newHashSet();
  final private Map<String, String> renamingMap = Maps.newHashMap();
  final private static Comparator<Property> FREQUENCY_COMPARATOR = new Comparator<Property>() {
      @Override() public int compare(Property p1, Property p2) {
        int var_692 = p1.numOccurrences;
        if(var_692 != p2.numOccurrences) {
          return p2.numOccurrences - p1.numOccurrences;
        }
        return p1.oldName.compareTo(p2.oldName);
      }
  };
  private BiMap<JSType, Integer> intForType = HashBiMap.create();
  private Map<JSType, JSTypeBitSet> relatedBitsets = Maps.newHashMap();
  final private Set<JSType> invalidatingTypes;
  final static String SKIP_PREFIX = "JSAbstractCompiler";
  AmbiguateProperties(AbstractCompiler compiler, char[] reservedCharacters) {
    super();
    Preconditions.checkState(compiler.getLifeCycleStage().isNormalized());
    this.compiler = compiler;
    this.reservedCharacters = reservedCharacters;
    JSTypeRegistry r = compiler.getTypeRegistry();
    invalidatingTypes = Sets.newHashSet(r.getNativeType(JSTypeNative.ALL_TYPE), r.getNativeType(JSTypeNative.NO_OBJECT_TYPE), r.getNativeType(JSTypeNative.NO_TYPE), r.getNativeType(JSTypeNative.NULL_TYPE), r.getNativeType(JSTypeNative.VOID_TYPE), r.getNativeType(JSTypeNative.FUNCTION_FUNCTION_TYPE), r.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE), r.getNativeType(JSTypeNative.FUNCTION_PROTOTYPE), r.getNativeType(JSTypeNative.GLOBAL_THIS), r.getNativeType(JSTypeNative.OBJECT_TYPE), r.getNativeType(JSTypeNative.OBJECT_PROTOTYPE), r.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE), r.getNativeType(JSTypeNative.TOP_LEVEL_PROTOTYPE), r.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    for (TypeMismatch mis : compiler.getTypeValidator().getMismatches()) {
      addInvalidatingType(mis.typeA);
      addInvalidatingType(mis.typeB);
    }
  }
  private BitSet getRelatedTypesOnNonUnion(JSType type) {
    if(relatedBitsets.containsKey(type)) {
      return relatedBitsets.get(type);
    }
    else {
      throw new RuntimeException("Related types should have been computed for" + " type: " + type + " but have not been.");
    }
  }
  private JSType getJSType(Node n) {
    JSType jsType = n.getJSType();
    if(jsType == null) {
      return compiler.getTypeRegistry().getNativeType(JSTypeNative.UNKNOWN_TYPE);
    }
    else {
      return jsType;
    }
  }
  Map<String, String> getRenamingMap() {
    return renamingMap;
  }
  private Property getProperty(String name) {
    Property prop = propertyMap.get(name);
    if(prop == null) {
      prop = new Property(name);
      propertyMap.put(name, prop);
    }
    return prop;
  }
  private boolean isInvalidatingType(JSType type) {
    if(type.isUnionType()) {
      type = type.restrictByNotNullOrUndefined();
      if(type.isUnionType()) {
        for (JSType alt : type.toMaybeUnionType().getAlternates()) {
          if(isInvalidatingType(alt)) {
            return true;
          }
        }
        return false;
      }
    }
    ObjectType objType = ObjectType.cast(type);
    return objType == null || invalidatingTypes.contains(objType) || !objType.hasReferenceName() || objType.isUnknownType() || objType.isEmptyType() || objType.isEnumType() || objType.autoboxesTo() != null;
  }
  private int getIntForType(JSType type) {
    if(intForType.containsKey(type)) {
      return intForType.get(type).intValue();
    }
    int newInt = intForType.size() + 1;
    intForType.put(type, newInt);
    return newInt;
  }
  private void addInvalidatingType(JSType type) {
    type = type.restrictByNotNullOrUndefined();
    if(type.isUnionType()) {
      for (JSType alt : type.toMaybeUnionType().getAlternates()) {
        addInvalidatingType(alt);
      }
    }
    invalidatingTypes.add(type);
    ObjectType objType = ObjectType.cast(type);
    if(objType != null && objType.isInstanceType()) {
      invalidatingTypes.add(objType.getImplicitPrototype());
    }
  }
  private void addRelatedInstance(FunctionType constructor, JSTypeBitSet related) {
    if(constructor.hasInstanceType()) {
      ObjectType instanceType = constructor.getInstanceType();
      related.set(getIntForType(instanceType.getImplicitPrototype()));
      computeRelatedTypes(instanceType);
      related.or(relatedBitsets.get(instanceType));
    }
  }
  private void computeRelatedTypes(JSType type) {
    if(type.isUnionType()) {
      type = type.restrictByNotNullOrUndefined();
      if(type.isUnionType()) {
        for (JSType alt : type.toMaybeUnionType().getAlternates()) {
          computeRelatedTypes(alt);
        }
        return ;
      }
    }
    if(relatedBitsets.containsKey(type)) {
      return ;
    }
    JSTypeBitSet related = new JSTypeBitSet(intForType.size());
    relatedBitsets.put(type, related);
    related.set(getIntForType(type));
    if(type.isFunctionPrototypeType()) {
      addRelatedInstance(((ObjectType)type).getOwnerFunction(), related);
      return ;
    }
    FunctionType constructor = type.toObjectType().getConstructor();
    if(constructor != null && constructor.getSubTypes() != null) {
      for (FunctionType subType : constructor.getSubTypes()) {
        addRelatedInstance(subType, related);
      }
    }
    for (FunctionType implementor : compiler.getTypeRegistry().getDirectImplementors(type.toObjectType())) {
      addRelatedInstance(implementor, related);
    }
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, externs, new ProcessExterns());
    NodeTraversal.traverse(compiler, root, new ProcessProperties());
    Set<String> reservedNames = new HashSet<String>(externedNames.size() + quotedNames.size());
    reservedNames.addAll(externedNames);
    reservedNames.addAll(quotedNames);
    int numRenamedPropertyNames = 0;
    int numSkippedPropertyNames = 0;
    Set<Property> propsByFreq = new TreeSet<Property>(FREQUENCY_COMPARATOR);
    for (Property p : propertyMap.values()) {
      if(!p.skipAmbiguating) {
        ++numRenamedPropertyNames;
        propsByFreq.add(p);
      }
      else {
        ++numSkippedPropertyNames;
        reservedNames.add(p.oldName);
      }
    }
    PropertyGraph graph = new PropertyGraph(Lists.newLinkedList(propsByFreq));
    GraphColoring<Property, Void> coloring = new GreedyGraphColoring<Property, Void>(graph, FREQUENCY_COMPARATOR);
    int numNewPropertyNames = coloring.color();
    NameGenerator nameGen = new NameGenerator(reservedNames, "", reservedCharacters);
    Map<Integer, String> colorMap = Maps.newHashMap();
    for(int i = 0; i < numNewPropertyNames; ++i) {
      colorMap.put(i, nameGen.generateNextName());
    }
    for (GraphNode<Property, Void> node : graph.getNodes()) {
      node.getValue().newName = colorMap.get(node.getAnnotation().hashCode());
      renamingMap.put(node.getValue().oldName, node.getValue().newName);
    }
    for (Node n : stringNodesToRename) {
      String oldName = n.getString();
      Property p = propertyMap.get(oldName);
      if(p != null && p.newName != null) {
        Preconditions.checkState(oldName.equals(p.oldName));
        if(!p.newName.equals(oldName)) {
          n.setString(p.newName);
          compiler.reportCodeChange();
        }
      }
    }
    logger.fine("Collapsed " + numRenamedPropertyNames + " properties into " + numNewPropertyNames + " and skipped renaming " + numSkippedPropertyNames + " properties.");
  }
  
  private class JSTypeBitSet extends BitSet  {
    final private static long serialVersionUID = 1L;
    private JSTypeBitSet() {
      super();
    }
    private JSTypeBitSet(int size) {
      super(size);
    }
    @Override() public String toString() {
      int from = 0;
      int current = 0;
      List<String> types = Lists.newArrayList();
      while(-1 != (current = nextSetBit(from))){
        types.add(intForType.inverse().get(current).toString());
        from = current + 1;
      }
      return Joiner.on(" && ").join(types);
    }
  }
  
  private class ProcessExterns extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.GETPROP:
        Node dest = n.getFirstChild().getNext();
        externedNames.add(dest.getString());
        break ;
        case Token.OBJECTLIT:
        for(com.google.javascript.rhino.Node child = n.getFirstChild(); child != null; child = child.getNext()) {
          externedNames.add(child.getString());
        }
        break ;
      }
    }
  }
  
  private class ProcessProperties extends AbstractPostOrderCallback  {
    private Property recordProperty(String name, JSType type) {
      Property prop = getProperty(name);
      prop.addType(type);
      return prop;
    }
    private void maybeMarkCandidate(Node n, JSType type, NodeTraversal t) {
      String name = n.getString();
      if(!externedNames.contains(name)) {
        stringNodesToRename.add(n);
        recordProperty(name, type);
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.GETPROP:
        {
          Node propNode = n.getFirstChild().getNext();
          JSType jstype = getJSType(n.getFirstChild());
          maybeMarkCandidate(propNode, jstype, t);
          break ;
        }
        case Token.OBJECTLIT:
        for(com.google.javascript.rhino.Node key = n.getFirstChild(); key != null; key = key.getNext()) {
          if(!key.isQuotedString()) {
            JSType jstype = getJSType(n.getFirstChild());
            maybeMarkCandidate(key, jstype, t);
          }
          else {
            quotedNames.add(key.getString());
          }
        }
        break ;
        case Token.GETELEM:
        Node child = n.getLastChild();
        if(child.isString()) {
          quotedNames.add(child.getString());
        }
        break ;
      }
    }
  }
  
  private class Property  {
    final String oldName;
    String newName;
    int numOccurrences;
    boolean skipAmbiguating;
    JSTypeBitSet relatedTypes = new JSTypeBitSet(intForType.size());
    Property(String name) {
      super();
      this.oldName = name;
      if(name.startsWith(SKIP_PREFIX)) {
        skipAmbiguating = true;
      }
    }
    private void addNonUnionType(JSType newType) {
      if(skipAmbiguating || isInvalidatingType(newType)) {
        skipAmbiguating = true;
        return ;
      }
      if(!relatedTypes.get(getIntForType(newType))) {
        computeRelatedTypes(newType);
        relatedTypes.or(getRelatedTypesOnNonUnion(newType));
      }
    }
    void addType(JSType newType) {
      if(skipAmbiguating) {
        return ;
      }
      ++numOccurrences;
      if(newType.isUnionType()) {
        newType = newType.restrictByNotNullOrUndefined();
        if(newType.isUnionType()) {
          for (JSType alt : newType.toMaybeUnionType().getAlternates()) {
            addNonUnionType(alt);
          }
          return ;
        }
      }
      addNonUnionType(newType);
    }
  }
  
  class PropertyGraph implements AdjacencyGraph<Property, Void>  {
    final protected Map<Property, PropertyGraphNode> nodes = Maps.newHashMap();
    PropertyGraph(Collection<Property> props) {
      super();
      for (Property prop : props) {
        nodes.put(prop, new PropertyGraphNode(prop));
      }
    }
    @Override() public GraphNode<Property, Void> getNode(Property property) {
      return nodes.get(property);
    }
    @Override() public List<GraphNode<Property, Void>> getNodes() {
      return Lists.<GraphNode<Property, Void>>newArrayList(nodes.values());
    }
    @Override() public SubGraph<Property, Void> newSubGraph() {
      return new PropertySubGraph();
    }
    @Override() public int getWeight(Property value) {
      return value.numOccurrences;
    }
    @Override() public void clearNodeAnnotations() {
      for (PropertyGraphNode node : nodes.values()) {
        node.setAnnotation(null);
      }
    }
  }
  
  class PropertyGraphNode implements GraphNode<Property, Void>  {
    Property property;
    protected Annotation annotation;
    PropertyGraphNode(Property property) {
      super();
      this.property = property;
    }
    @Override() @SuppressWarnings(value = {"unchecked", }) public  <A extends com.google.javascript.jscomp.graph.Annotation> A getAnnotation() {
      return (A)annotation;
    }
    @Override() public Property getValue() {
      return property;
    }
    @Override() public void setAnnotation(Annotation data) {
      annotation = data;
    }
  }
  
  class PropertySubGraph implements SubGraph<Property, Void>  {
    JSTypeBitSet relatedTypes = new JSTypeBitSet(intForType.size());
    @Override() public boolean isIndependentOf(Property prop) {
      return !relatedTypes.intersects(prop.relatedTypes);
    }
    @Override() public void addNode(Property prop) {
      relatedTypes.or(prop.relatedTypes);
    }
  }
}