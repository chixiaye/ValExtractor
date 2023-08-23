package com.google.javascript.rhino.jstype;
import static com.google.javascript.rhino.jstype.TernaryValue.FALSE;
import static com.google.javascript.rhino.jstype.TernaryValue.UNKNOWN;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import java.util.Set;

abstract public class ObjectType extends JSType implements StaticScope<JSType>  {
  private boolean visited;
  private JSDocInfo docInfo = null;
  private boolean unknown = true;
  ObjectType(JSTypeRegistry registry) {
    super(registry);
  }
  ObjectType(JSTypeRegistry registry, ImmutableList<String> templateKeys, ImmutableList<JSType> templatizedTypes) {
    super(registry, templateKeys, templatizedTypes);
  }
  @Override() public BooleanLiteralSet getPossibleToBooleanOutcomes() {
    return BooleanLiteralSet.TRUE;
  }
  abstract public FunctionType getConstructor();
  public FunctionType getOwnerFunction() {
    return null;
  }
  public Iterable<ObjectType> getCtorExtendedInterfaces() {
    return ImmutableSet.of();
  }
  public Iterable<ObjectType> getCtorImplementedInterfaces() {
    return ImmutableSet.of();
  }
  @Override() public JSDocInfo getJSDocInfo() {
    return docInfo;
  }
  public JSDocInfo getOwnPropertyJSDocInfo(String propertyName) {
    Property p = getOwnSlot(propertyName);
    return p == null ? null : p.getJSDocInfo();
  }
  @Override() public JSType findPropertyType(String propertyName) {
    return hasProperty(propertyName) ? getPropertyType(propertyName) : null;
  }
  public JSType getIndexType() {
    return null;
  }
  public JSType getParameterType() {
    return null;
  }
  public JSType getPropertyType(String propertyName) {
    StaticSlot<JSType> slot = getSlot(propertyName);
    if(slot == null) {
      if(isNoResolvedType() || isCheckedUnknownType()) {
        return getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
      }
      else 
        if(isEmptyType()) {
          return getNativeType(JSTypeNative.NO_TYPE);
        }
      return getNativeType(JSTypeNative.UNKNOWN_TYPE);
    }
    return slot.getType();
  }
  @Override() public JSType getTypeOfThis() {
    return null;
  }
  public Node getPropertyNode(String propertyName) {
    Property p = getSlot(propertyName);
    return p == null ? null : p.getNode();
  }
  @Override() public Node getRootNode() {
    return null;
  }
  public static ObjectType cast(JSType type) {
    return type == null ? null : type.toObjectType();
  }
  abstract public ObjectType getImplicitPrototype();
  @Override() public ObjectType getParentScope() {
    return getImplicitPrototype();
  }
  @Override() public Property getOwnSlot(String name) {
    return getPropertyMap().getOwnProperty(name);
  }
  @Override() public Property getSlot(String name) {
    return getPropertyMap().getSlot(name);
  }
  PropertyMap getPropertyMap() {
    return PropertyMap.immutableEmptyMap();
  }
  public Set<String> getOwnPropertyNames() {
    return getPropertyMap().getOwnPropertyNames();
  }
  public Set<String> getPropertyNames() {
    Set<String> props = Sets.newTreeSet();
    collectPropertyNames(props);
    return props;
  }
  public static String createDelegateSuffix(String suffix) {
    return "(" + suffix + ")";
  }
  @Override() public String getDisplayName() {
    return getNormalizedReferenceName();
  }
  public String getNormalizedReferenceName() {
    String name = getReferenceName();
    if(name != null) {
      int pos = name.indexOf("(");
      if(pos != -1) {
        return name.substring(0, pos);
      }
    }
    return name;
  }
  abstract public String getReferenceName();
  @Override<T>() T visit(RelationshipVisitor<T> visitor, JSType that) {
    return visitor.caseObjectType(this, that);
  }
  @Override() public  <T extends java.lang.Object> T visit(Visitor<T> visitor) {
    return visitor.caseObjectType(this);
  }
  @Override() public TernaryValue testForEquality(JSType that) {
    TernaryValue result = super.testForEquality(that);
    if(result != null) {
      return result;
    }
    if(that.isSubtype(getNativeType(JSTypeNative.OBJECT_NUMBER_STRING_BOOLEAN))) {
      return UNKNOWN;
    }
    else {
      return FALSE;
    }
  }
  final public boolean defineDeclaredProperty(String propertyName, JSType type, Node propertyNode) {
    boolean result = defineProperty(propertyName, type, false, propertyNode);
    registry.registerPropertyOnType(propertyName, this);
    return result;
  }
  final public boolean defineInferredProperty(String propertyName, JSType type, Node propertyNode) {
    StaticSlot<JSType> originalSlot = getSlot(propertyName);
    if(hasProperty(propertyName)) {
      if(isPropertyTypeDeclared(propertyName)) {
        return true;
      }
      JSType originalType = getPropertyType(propertyName);
      type = originalType == null ? type : originalType.getLeastSupertype(type);
    }
    boolean result = defineProperty(propertyName, type, true, propertyNode);
    registry.registerPropertyOnType(propertyName, this);
    return result;
  }
  abstract boolean defineProperty(String propertyName, JSType type, boolean inferred, Node propertyNode);
  final public boolean defineSynthesizedProperty(String propertyName, JSType type, Node propertyNode) {
    return defineProperty(propertyName, type, false, propertyNode);
  }
  final boolean detectImplicitPrototypeCycle() {
    this.visited = true;
    ObjectType p = getImplicitPrototype();
    while(p != null){
      boolean var_2624 = p.visited;
      if(var_2624) {
        return true;
      }
      else {
        p.visited = true;
      }
      p = p.getImplicitPrototype();
    }
    p = this;
    do {
      p.visited = false;
      p = p.getImplicitPrototype();
    }while(p != null);
    return false;
  }
  final boolean detectInheritanceCycle() {
    return detectImplicitPrototypeCycle() || Iterables.contains(this.getCtorImplementedInterfaces(), this) || Iterables.contains(this.getCtorExtendedInterfaces(), this);
  }
  public boolean hasCachedValues() {
    return !unknown;
  }
  final boolean hasOwnDeclaredProperty(String name) {
    return hasOwnProperty(name) && isPropertyTypeDeclared(name);
  }
  public boolean hasOwnProperty(String propertyName) {
    return getOwnSlot(propertyName) != null;
  }
  @Override() public boolean hasProperty(String propertyName) {
    return isEmptyType() || isUnknownType() || getSlot(propertyName) != null;
  }
  public boolean hasReferenceName() {
    return false;
  }
  @Override() final public boolean isFunctionPrototypeType() {
    return getOwnerFunction() != null;
  }
  final boolean isImplicitPrototype(ObjectType prototype) {
    for(com.google.javascript.rhino.jstype.ObjectType current = this; current != null; current = current.getImplicitPrototype()) {
      if(current.isEquivalentTo(prototype)) {
        return true;
      }
    }
    return false;
  }
  public boolean isNativeObjectType() {
    return false;
  }
  @Override() public boolean isObject() {
    return true;
  }
  public boolean isPropertyInExterns(String propertyName) {
    Property p = getSlot(propertyName);
    return p == null ? false : p.isFromExterns();
  }
  public boolean isPropertyTypeDeclared(String propertyName) {
    StaticSlot<JSType> slot = getSlot(propertyName);
    return slot == null ? false : !slot.isTypeInferred();
  }
  public boolean isPropertyTypeInferred(String propertyName) {
    StaticSlot<JSType> slot = getSlot(propertyName);
    return slot == null ? false : slot.isTypeInferred();
  }
  @Override() public boolean isUnknownType() {
    if(unknown) {
      ObjectType implicitProto = getImplicitPrototype();
      if(implicitProto == null || implicitProto.isNativeObjectType()) {
        unknown = false;
        for (ObjectType interfaceType : getCtorExtendedInterfaces()) {
          if(interfaceType.isUnknownType()) {
            unknown = true;
            break ;
          }
        }
      }
      else {
        unknown = implicitProto.isUnknownType();
      }
    }
    return unknown;
  }
  public boolean removeProperty(String propertyName) {
    return false;
  }
  public int getPropertiesCount() {
    return getPropertyMap().getPropertiesCount();
  }
  public void clearCachedValues() {
    unknown = true;
  }
  final void collectPropertyNames(Set<String> props) {
    getPropertyMap().collectPropertyNames(props);
  }
  public void setJSDocInfo(JSDocInfo info) {
    docInfo = info;
  }
  void setOwnerFunction(FunctionType type) {
  }
  public void setPropertyJSDocInfo(String propertyName, JSDocInfo info) {
  }
}