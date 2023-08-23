package com.google.javascript.jscomp;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.graph.LatticeElement;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticScope;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.UnknownType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

abstract class ConcreteType implements LatticeElement  {
  final static ConcreteType NONE = new ConcreteNoneType();
  final static ConcreteType ALL = new ConcreteAll();
  final private static List<ConcreteFunctionType> NO_FUNCTIONS = Lists.<ConcreteFunctionType>newArrayList();
  final private static List<ConcreteInstanceType> NO_INSTANCES = Lists.<ConcreteInstanceType>newArrayList();
  final private static List<StaticSlot<ConcreteType>> NO_SLOTS = Lists.<StaticSlot<ConcreteType>>newArrayList();
  ConcreteFunctionType toFunction() {
    return null;
  }
  ConcreteInstanceType toInstance() {
    return null;
  }
  protected static ConcreteType createForTypes(Collection<ConcreteType> types) {
    if(types == null || types.size() == 0) {
      return NONE;
    }
    else 
      if(types.size() == 1) {
        return types.iterator().next();
      }
      else {
        return new ConcreteUnionType(Sets.newHashSet(types));
      }
  }
  ConcreteType getPropertyType(final String name) {
    ConcreteType ret = NONE;
    for (StaticSlot<ConcreteType> slot : getPropertySlots(name)) {
      ret = ret.unionWith(slot.getType());
    }
    return ret;
  }
  ConcreteType intersectWith(ConcreteType other) {
    if(!other.isSingleton()) {
      return other.intersectWith(this);
    }
    else 
      if(equals(other)) {
        return this;
      }
      else {
        return NONE;
      }
  }
  ConcreteType unionWith(ConcreteType other) {
    Preconditions.checkState(this.isSingleton());
    if(!other.isSingleton()) {
      return other.unionWith(this);
    }
    else 
      if(equals(other)) {
        return this;
      }
      else {
        return new ConcreteUnionType(this, other);
      }
  }
  ConcreteUnionType toUnion() {
    return null;
  }
  private  <C extends java.lang.Object> List<C> getMatchingTypes(TypeFilter<C> filter) {
    C type = null;
    if(isUnion()) {
      List<C> list = Lists.newArrayList();
      for (ConcreteType alt : toUnion().getAlternatives()) {
        if((type = filter.filter(alt)) != null) {
          list.add(type);
        }
      }
      return list;
    }
    else 
      if((type = filter.filter(this)) != null) {
        List<C> list = Lists.newArrayList();
        list.add(type);
        return list;
      }
      else {
        return filter.emptyList;
      }
  }
  List<ConcreteFunctionType> getFunctions() {
    return getMatchingTypes(new TypeFilter<ConcreteFunctionType>(NO_FUNCTIONS) {
        @Override() public ConcreteFunctionType filter(ConcreteType type) {
          return type.isFunction() ? type.toFunction() : null;
        }
    });
  }
  List<ConcreteFunctionType> getSuperclassTypes() {
    return getMatchingTypes(new TypeFilter<ConcreteFunctionType>(NO_FUNCTIONS) {
        @Override() public ConcreteFunctionType filter(ConcreteType type) {
          return type.isFunction() && type.toFunction().getSuperclassType() != null ? type.toFunction().getSuperclassType() : null;
        }
    });
  }
  List<ConcreteInstanceType> getFunctionInstanceTypes() {
    return getMatchingTypes(new TypeFilter<ConcreteInstanceType>(NO_INSTANCES) {
        @Override() public ConcreteInstanceType filter(ConcreteType type) {
          if(type.isFunction()) {
            return type.toFunction().getInstanceType();
          }
          return null;
        }
    });
  }
  List<ConcreteInstanceType> getInstances() {
    return getMatchingTypes(new TypeFilter<ConcreteInstanceType>(NO_INSTANCES) {
        @Override() public ConcreteInstanceType filter(ConcreteType type) {
          return type.isInstance() ? type.toInstance() : null;
        }
    });
  }
  List<ConcreteInstanceType> getPrototypeTypes() {
    return getMatchingTypes(new TypeFilter<ConcreteInstanceType>(NO_INSTANCES) {
        @Override() public ConcreteInstanceType filter(ConcreteType type) {
          if(type.isInstance() && type.toInstance().isFunctionPrototype()) {
            return type.toInstance();
          }
          return null;
        }
    });
  }
  List<StaticSlot<ConcreteType>> getParameterSlots(final int index) {
    return getMatchingTypes(new TypeFilter<StaticSlot<ConcreteType>>(NO_SLOTS) {
        @Override() public StaticSlot<ConcreteType> filter(ConcreteType type) {
          return type.isFunction() && toFunction().getParameterSlot(index) != null ? toFunction().getParameterSlot(index) : null;
        }
    });
  }
  List<StaticSlot<ConcreteType>> getPropertySlots(final String name) {
    return getMatchingTypes(new TypeFilter<StaticSlot<ConcreteType>>(NO_SLOTS) {
        @Override() public StaticSlot<ConcreteType> filter(ConcreteType type) {
          StaticSlot<ConcreteType> slot = null;
          if(type.isInstance()) {
            slot = type.toInstance().getPropertySlot(name);
          }
          return slot;
        }
    });
  }
  StaticScope<ConcreteType> getScope() {
    return null;
  }
  boolean isAll() {
    return false;
  }
  boolean isFunction() {
    return false;
  }
  boolean isInstance() {
    return false;
  }
  boolean isNone() {
    return false;
  }
  boolean isSingleton() {
    return !isNone() && !isUnion() && !isAll();
  }
  boolean isUnion() {
    return false;
  }
  
  private static class ConcreteAll extends ConcreteType  {
    @Override() ConcreteType intersectWith(ConcreteType other) {
      return other;
    }
    @Override() ConcreteType unionWith(ConcreteType other) {
      return this;
    }
    @Override() public String toString() {
      return "*";
    }
    @Override() boolean isAll() {
      return true;
    }
  }
  
  static class ConcreteFunctionType extends ConcreteType  {
    final static String CALL_SLOT_NAME = ":call";
    final static String THIS_SLOT_NAME = ":this";
    final static String RETURN_SLOT_NAME = ":return";
    final private Factory factory;
    final private Node declaration;
    final private StaticScope<ConcreteType> parentScope;
    private StaticScope<ConcreteType> bodyScope;
    private ConcreteInstanceType instanceType;
    private ConcreteInstanceType prototypeType;
    ConcreteFunctionType(Factory factory, Node declaration, StaticScope<ConcreteType> parentScope) {
      super();
      this.factory = factory;
      this.declaration = declaration;
      this.parentScope = parentScope;
      Preconditions.checkArgument(declaration.isFunction());
      Preconditions.checkArgument(declaration.getJSType() != null);
      Preconditions.checkArgument(declaration.getJSType().isFunctionType());
    }
    ConcreteFunctionType getSuperclassType() {
      FunctionType superConstructor = getJSType().getSuperClassConstructor();
      return (superConstructor != null) ? factory.getConcreteFunction(superConstructor) : null;
    }
    @Override() ConcreteFunctionType toFunction() {
      return this;
    }
    ConcreteInstanceType getInstanceType() {
      if(instanceType == null) {
        if(getJSType().isConstructor()) {
          instanceType = factory.createConcreteInstance(getJSType().getInstanceType());
        }
      }
      return instanceType;
    }
    ConcreteInstanceType getPrototypeType() {
      if(prototypeType == null) {
        prototypeType = factory.createConcreteInstance(getJSType().getPrototype());
      }
      return prototypeType;
    }
    public FunctionType getJSType() {
      return JSType.toMaybeFunctionType(declaration.getJSType());
    }
    private Node getFirstParameter() {
      return declaration.getFirstChild().getNext().getFirstChild();
    }
    @Override() StaticScope<ConcreteType> getScope() {
      if(bodyScope == null) {
        bodyScope = factory.createFunctionScope(declaration, parentScope);
      }
      return bodyScope;
    }
    StaticSlot<ConcreteType> getCallSlot() {
      return getScope().getOwnSlot(CALL_SLOT_NAME);
    }
    StaticSlot<ConcreteType> getParameterSlot(int index) {
      return getScope().getOwnSlot(getParameterName(index));
    }
    StaticSlot<ConcreteType> getReturnSlot() {
      return getScope().getOwnSlot(RETURN_SLOT_NAME);
    }
    StaticSlot<ConcreteType> getThisSlot() {
      return getScope().getOwnSlot(THIS_SLOT_NAME);
    }
    private String getParameterName(int index) {
      int count = 0;
      for(com.google.javascript.rhino.Node n = getFirstParameter(); n != null; n = n.getNext()) {
        if(count++ == index) {
          return n.getString();
        }
      }
      return null;
    }
    @Override() public String toString() {
      StringBuilder b = new StringBuilder(32);
      b.append("function (");
      ConcreteType var_1764 = getThisSlot().getType();
      boolean hasKnownTypeOfThis = !var_1764.isNone();
      if(hasKnownTypeOfThis) {
        b.append("this:");
        b.append(getThisSlot().getType().toString());
      }
      Node n = getFirstParameter();
      if(hasKnownTypeOfThis && n != null) {
        b.append(", ");
      }
      for(int i = 0; n != null; ++i, n = n.getNext()) {
        String paramName = n.getString();
        StaticSlot<ConcreteType> var = getScope().getOwnSlot(paramName);
        b.append(var.getType());
        getParameterSlot(i).getType();
        if(n.getNext() != null) {
          b.append(", ");
        }
      }
      b.append(")");
      if(getReturnSlot().getType() != null) {
        b.append(": ");
        b.append(getReturnSlot().getType().toString());
      }
      return b.toString();
    }
    @Override() boolean isFunction() {
      return true;
    }
  }
  
  static class ConcreteInstanceType extends ConcreteType  {
    final private Factory factory;
    final public ObjectType instanceType;
    private ConcreteInstanceType prototype;
    private StaticScope<ConcreteType> scope;
    ConcreteInstanceType(Factory factory, ObjectType instanceType) {
      super();
      this.factory = factory;
      this.instanceType = instanceType;
      Preconditions.checkArgument(!(instanceType instanceof UnknownType));
    }
    ConcreteFunctionType getConstructorType() {
      if(instanceType.isFunctionPrototypeType()) {
        return factory.getConcreteFunction(instanceType.getOwnerFunction());
      }
      else {
        FunctionType constructor = instanceType.getConstructor();
        return (constructor != null) ? factory.getConcreteFunction(constructor) : null;
      }
    }
    ConcreteInstanceType getImplicitPrototype() {
      if((prototype == null) && (instanceType.getImplicitPrototype() != null)) {
        ObjectType proto = instanceType.getImplicitPrototype();
        if((proto != instanceType) && !(proto instanceof UnknownType)) {
          prototype = factory.createConcreteInstance(proto);
        }
      }
      return prototype;
    }
    ConcreteInstanceType getInstanceTypeWithProperty(String propName) {
      if(getScope().getOwnSlot(propName) != null) {
        if(instanceType.getConstructor() != null) {
          return getConstructorType().getPrototypeType();
        }
        return this;
      }
      else 
        if(getImplicitPrototype() != null) {
          return getImplicitPrototype().getInstanceTypeWithProperty(propName);
        }
        else {
          return null;
        }
    }
    @Override() ConcreteInstanceType toInstance() {
      return this;
    }
    @Override() StaticScope<ConcreteType> getScope() {
      if(scope == null) {
        scope = factory.createInstanceScope(instanceType);
      }
      return scope;
    }
    StaticSlot<ConcreteType> getPropertySlot(String propName) {
      return getScope().getSlot(propName);
    }
    @Override() public String toString() {
      return instanceType.toString();
    }
    boolean isFunctionPrototype() {
      return instanceType.isFunctionPrototypeType();
    }
    @Override() boolean isInstance() {
      return true;
    }
  }
  
  private static class ConcreteNoneType extends ConcreteType  {
    @Override() ConcreteType intersectWith(ConcreteType other) {
      return NONE;
    }
    @Override() ConcreteType unionWith(ConcreteType other) {
      return other;
    }
    @Override() public String toString() {
      return "()";
    }
    @Override() boolean isNone() {
      return true;
    }
  }
  
  static class ConcreteUnionType extends ConcreteType  {
    final private Set<ConcreteType> alternatives;
    ConcreteUnionType(ConcreteType ... alternatives) {
      this(Sets.newHashSet(alternatives));
    }
    ConcreteUnionType(Set<ConcreteType> alternatives) {
      super();
      Preconditions.checkArgument(alternatives.size() > 1);
      this.alternatives = alternatives;
    }
    @Override() ConcreteType intersectWith(ConcreteType other) {
      if(other.isSingleton()) {
        if(alternatives.contains(other)) {
          return other;
        }
        else {
          return NONE;
        }
      }
      else 
        if(other.isUnion()) {
          Set<ConcreteType> types = Sets.newHashSet(alternatives);
          types.retainAll(other.toUnion().alternatives);
          return createForTypes(types);
        }
        else {
          Preconditions.checkArgument(other.isNone() || other.isAll());
          return other.intersectWith(this);
        }
    }
    @Override() ConcreteType unionWith(ConcreteType other) {
      if(other.isSingleton()) {
        if(alternatives.contains(other)) {
          return this;
        }
        else {
          Set<ConcreteType> alts = Sets.newHashSet(alternatives);
          alts.add(other);
          return new ConcreteUnionType(alts);
        }
      }
      else 
        if(other.isUnion()) {
          ConcreteUnionType otherUnion = other.toUnion();
          if(alternatives.containsAll(otherUnion.alternatives)) {
            return this;
          }
          else 
            if(otherUnion.alternatives.containsAll(alternatives)) {
              return otherUnion;
            }
            else {
              Set<ConcreteType> alts = Sets.newHashSet(alternatives);
              alts.addAll(otherUnion.alternatives);
              return new ConcreteUnionType(alts);
            }
        }
        else {
          Preconditions.checkArgument(other.isNone() || other.isAll());
          return other.unionWith(this);
        }
    }
    @Override() ConcreteUnionType toUnion() {
      return this;
    }
    Set<ConcreteType> getAlternatives() {
      return alternatives;
    }
    @Override() public String toString() {
      List<String> names = Lists.newArrayList();
      for (ConcreteType type : alternatives) {
        names.add(type.toString());
      }
      Collections.sort(names);
      return "(" + Joiner.on(",").join(names) + ")";
    }
    @Override() public boolean equals(Object obj) {
      return (obj instanceof ConcreteUnionType) && alternatives.equals(((ConcreteUnionType)obj).alternatives);
    }
    @Override() boolean isUnion() {
      return true;
    }
    @Override() public int hashCode() {
      return alternatives.hashCode() ^ 0x5f6e7d8c;
    }
  }
  
  static class ConcreteUniqueType extends ConcreteType  {
    final private int id;
    ConcreteUniqueType(int id) {
      super();
      this.id = id;
      Preconditions.checkArgument(id >= 0);
    }
    @Override() public String toString() {
      return "Unique$" + id;
    }
    @Override() public boolean equals(Object o) {
      return (o instanceof ConcreteUniqueType) && (id == ((ConcreteUniqueType)o).id);
    }
    @Override() public int hashCode() {
      return ConcreteUniqueType.class.hashCode() ^ id;
    }
  }
  
  interface Factory  {
    ConcreteFunctionType createConcreteFunction(Node declaration, StaticScope<ConcreteType> parent);
    ConcreteFunctionType getConcreteFunction(FunctionType function);
    ConcreteInstanceType createConcreteInstance(ObjectType instanceType);
    ConcreteInstanceType getConcreteInstance(ObjectType instance);
    JSTypeRegistry getTypeRegistry();
    StaticScope<ConcreteType> createFunctionScope(Node declaration, StaticScope<ConcreteType> parent);
    StaticScope<ConcreteType> createInstanceScope(ObjectType instanceType);
  }
  abstract class TypeFilter<C extends java.lang.Object>  {
    final List<C> emptyList;
    TypeFilter(List<C> emptyList) {
      super();
      this.emptyList = emptyList;
    }
    abstract protected C filter(ConcreteType type);
  }
}