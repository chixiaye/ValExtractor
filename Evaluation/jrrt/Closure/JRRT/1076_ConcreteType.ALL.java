package com.google.javascript.jscomp;
import static com.google.javascript.rhino.jstype.JSTypeNative.U2U_CONSTRUCTOR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.ConcreteType.ConcreteFunctionType;
import com.google.javascript.jscomp.ConcreteType.ConcreteInstanceType;
import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticReference;
import com.google.javascript.rhino.jstype.StaticScope;
import com.google.javascript.rhino.jstype.StaticSlot;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class TightenTypes implements CompilerPass, ConcreteType.Factory  {
  final public static String NON_HALTING_ERROR_MSG = "TightenTypes pass appears to be stuck in an infinite loop.";
  final private AbstractCompiler compiler;
  final private Map<Node, ConcreteFunctionType> functionFromDeclaration = Maps.newHashMap();
  final private Map<FunctionType, ConcreteFunctionType> functionFromJSType = Maps.newIdentityHashMap();
  final private Map<ObjectType, ConcreteInstanceType> instanceFromJSType = Maps.newHashMap();
  final private Map<ConcreteJSTypePair, ConcreteType> typeIntersectionMemos = Maps.newHashMap();
  private ConcreteScope topScope;
  private Set<ConcreteType> allInstantiatedTypes = Sets.newHashSet();
  TightenTypes(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
  }
  @Override() public ConcreteFunctionType createConcreteFunction(Node decl, StaticScope<ConcreteType> parent) {
    ConcreteFunctionType funType = functionFromDeclaration.get(decl);
    if(funType == null) {
      functionFromDeclaration.put(decl, funType = new ConcreteFunctionType(this, decl, parent));
      if(decl.getJSType() != null) {
        functionFromJSType.put(decl.getJSType().toMaybeFunctionType(), funType);
      }
    }
    return funType;
  }
  ConcreteFunctionType getConcreteFunction(Node decl) {
    return functionFromDeclaration.get(decl);
  }
  @Override() public ConcreteFunctionType getConcreteFunction(FunctionType functionType) {
    return functionFromJSType.get(functionType);
  }
  @Override() public ConcreteInstanceType createConcreteInstance(ObjectType instanceType) {
    Preconditions.checkArgument(!instanceType.isFunctionType() || instanceType == getTypeRegistry().getNativeType(U2U_CONSTRUCTOR_TYPE));
    ConcreteInstanceType instType = instanceFromJSType.get(instanceType);
    if(instType == null) {
      instanceFromJSType.put(instanceType, instType = new ConcreteInstanceType(this, instanceType));
    }
    return instType;
  }
  @Override() public ConcreteInstanceType getConcreteInstance(ObjectType instanceType) {
    return instanceFromJSType.get(instanceType);
  }
  ConcreteScope getTopScope() {
    return topScope;
  }
  private ConcreteType createType(Node name, ConcreteScope scope) {
    Preconditions.checkNotNull(name);
    Preconditions.checkArgument(name.isName());
    if(name.getJSType() == null) {
      return ConcreteType.ALL;
    }
    if((name.getFirstChild() != null) && (name.getFirstChild().isFunction())) {
      return createConcreteFunction(name.getFirstChild(), scope);
    }
    return createType(name.getJSType());
  }
  private ConcreteType createType(JSType jsType) {
    if(jsType.isUnknownType() || jsType.isEmptyType()) {
      return ConcreteType.ALL;
    }
    if(jsType.isUnionType()) {
      ConcreteType type = ConcreteType.NONE;
      for (JSType alt : jsType.toMaybeUnionType().getAlternates()) {
        type = type.unionWith(createType(alt));
      }
      return type;
    }
    if(jsType.isFunctionType()) {
      if(getConcreteFunction(jsType.toMaybeFunctionType()) != null) {
        return getConcreteFunction(jsType.toMaybeFunctionType());
      }
      return ConcreteType.ALL;
    }
    if(jsType.isObject()) {
      return createConcreteInstance(jsType.toObjectType());
    }
    return ConcreteType.NONE;
  }
  private ConcreteType createTypeIntersection(ConcreteType concreteType, JSType jsType) {
    ConcreteJSTypePair key = new ConcreteJSTypePair(concreteType, jsType);
    ConcreteType ret = typeIntersectionMemos.get(key);
    if(ret != null) {
      return ret;
    }
    if(jsType == null || jsType.isUnknownType() || concreteType.isNone()) {
      ret = concreteType;
    }
    else 
      if(concreteType.isUnion() || concreteType.isSingleton()) {
        ret = concreteType.intersectWith(createTypeWithSubTypes(jsType));
      }
      else {
        Preconditions.checkState(concreteType.isAll());
        ret = createTypeWithSubTypes(jsType);
      }
    ret = ret.intersectWith(ConcreteType.createForTypes(allInstantiatedTypes));
    for (ConcreteFunctionType functionType : concreteType.getFunctions()) {
      ret = ret.unionWith(functionType);
    }
    for (ConcreteInstanceType prototype : concreteType.getPrototypeTypes()) {
      ret = ret.unionWith(prototype);
    }
    for (ConcreteInstanceType instance : concreteType.getInstances()) {
      if(!instance.instanceType.isInstanceType() && !instance.isFunctionPrototype()) {
        ret = ret.unionWith(instance);
      }
    }
    typeIntersectionMemos.put(key, ret);
    return ret;
  }
  private ConcreteType createTypeWithSubTypes(JSType jsType) {
    ConcreteType ret = ConcreteType.NONE;
    if(jsType.isUnionType()) {
      for (JSType alt : jsType.toMaybeUnionType().getAlternates()) {
        ret = ret.unionWith(createTypeWithSubTypes(alt));
      }
    }
    else {
      ObjectType instType = ObjectType.cast(jsType);
      if(instType != null && instType.getConstructor() != null && instType.getConstructor().isInterface()) {
        Collection<FunctionType> implementors = getTypeRegistry().getDirectImplementors(instType);
        for (FunctionType implementor : implementors) {
          ret = ret.unionWith(createTypeWithSubTypes(implementor.getInstanceType()));
        }
      }
      else {
        ret = ret.unionWith(createUnionWithSubTypes(createType(jsType)));
      }
    }
    return ret;
  }
  ConcreteType createUnionWithSubTypes(ConcreteType type) {
    Set<ConcreteType> set = null;
    if(type.isInstance()) {
      set = getSubTypes(type.toInstance());
    }
    return ConcreteType.createForTypes(set).unionWith(type);
  }
  ConcreteType inferConcreteType(ConcreteScope scope, Node expr) {
    Preconditions.checkNotNull(scope);
    Preconditions.checkNotNull(expr);
    ConcreteType ret;
    switch (expr.getType()){
      case Token.NAME:
      StaticSlot<ConcreteType> slot = scope.getSlot(expr.getString());
      if(slot != null) {
        ret = slot.getType();
      }
      else {
        ConcreteType var_1076 = ConcreteType.ALL;
        ret = var_1076;
      }
      break ;
      case Token.THIS:
      ret = scope.getTypeOfThis();
      break ;
      case Token.ASSIGN:
      ret = inferConcreteType(scope, expr.getLastChild());
      break ;
      case Token.COMMA:
      ret = inferConcreteType(scope, expr.getLastChild());
      break ;
      case Token.AND:
      ret = inferConcreteType(scope, expr.getLastChild());
      break ;
      case Token.OR:
      ret = inferConcreteType(scope, expr.getFirstChild()).unionWith(inferConcreteType(scope, expr.getLastChild()));
      break ;
      case Token.HOOK:
      ret = inferConcreteType(scope, expr.getFirstChild().getNext()).unionWith(inferConcreteType(scope, expr.getLastChild()));
      break ;
      case Token.GETPROP:
      ConcreteType recvType = inferConcreteType(scope, expr.getFirstChild());
      if(recvType.isAll()) {
        ret = recvType;
        break ;
      }
      Node prop = expr.getLastChild();
      String propName = prop.getString();
      ConcreteType type = recvType.getPropertyType(propName);
      if("prototype".equals(propName)) {
        for (ConcreteFunctionType funType : recvType.getFunctions()) {
          type = type.unionWith(funType.getPrototypeType());
        }
      }
      else 
        if(compiler.getCodingConvention().isSuperClassReference(propName)) {
          for (ConcreteFunctionType superType : recvType.getSuperclassTypes()) {
            type = type.unionWith(superType.getPrototypeType());
          }
        }
        else 
          if("call".equals(propName)) {
            type = recvType;
          }
      ret = type;
      break ;
      case Token.GETELEM:
      ret = ConcreteType.ALL;
      break ;
      case Token.CALL:
      ConcreteType targetType = inferConcreteType(scope, expr.getFirstChild());
      if(targetType.isAll()) {
        ret = targetType;
        break ;
      }
      ret = ConcreteType.NONE;
      for (ConcreteFunctionType funType : targetType.getFunctions()) {
        ret = ret.unionWith(funType.getReturnSlot().getType());
      }
      break ;
      case Token.NEW:
      ConcreteType constructorType = inferConcreteType(scope, expr.getFirstChild());
      if(constructorType.isAll()) {
        throw new AssertionError("Attempted new call on all type!");
      }
      ret = ConcreteType.NONE;
      for (ConcreteInstanceType instType : constructorType.getFunctionInstanceTypes()) {
        ret = ret.unionWith(instType);
      }
      allInstantiatedTypes.add(ret);
      break ;
      case Token.FUNCTION:
      ret = createConcreteFunction(expr, scope);
      break ;
      case Token.OBJECTLIT:
      if((expr.getJSType() != null) && !expr.getJSType().isUnknownType()) {
        JSType exprType = expr.getJSType().restrictByNotNullOrUndefined();
        ConcreteType inst = createConcreteInstance(exprType.toObjectType());
        allInstantiatedTypes.add(inst);
        ret = inst;
      }
      else {
        ret = ConcreteType.ALL;
      }
      break ;
      case Token.ARRAYLIT:
      ObjectType arrayType = (ObjectType)getTypeRegistry().getNativeType(JSTypeNative.ARRAY_TYPE);
      ConcreteInstanceType inst = createConcreteInstance(arrayType);
      allInstantiatedTypes.add(inst);
      ret = inst;
      break ;
      default:
      ret = ConcreteType.NONE;
    }
    return createTypeIntersection(ret, expr.getJSType());
  }
  @Override() public JSTypeRegistry getTypeRegistry() {
    return compiler.getTypeRegistry();
  }
  private List<Assignment> getFunctionCallAssignments(ConcreteType recvType, ConcreteType thisType, List<ConcreteType> argTypes) {
    List<Assignment> assigns = Lists.newArrayList();
    for (ConcreteFunctionType fType : recvType.getFunctions()) {
      assigns.add(new Assignment((ConcreteSlot)fType.getCallSlot(), fType));
      assigns.add(new Assignment((ConcreteSlot)fType.getThisSlot(), thisType));
      for(int i = 0; i < argTypes.size(); ++i) {
        ConcreteSlot variable = (ConcreteSlot)fType.getParameterSlot(i);
        if(variable != null) {
          assigns.add(new Assignment(variable, argTypes.get(i)));
        }
      }
    }
    return assigns;
  }
  private Set<ConcreteType> getSubTypes(ConcreteInstanceType type) {
    if(type.getConstructorType() == null) {
      return null;
    }
    Set<ConcreteType> set = Sets.newHashSet();
    getSubTypes(type.getConstructorType().getJSType(), set);
    return set;
  }
  @Override() public StaticScope<ConcreteType> createFunctionScope(Node decl, StaticScope<ConcreteType> parent) {
    ConcreteScope scope = new ConcreteScope((ConcreteScope)parent);
    scope.declareSlot(ConcreteFunctionType.CALL_SLOT_NAME, decl);
    scope.declareSlot(ConcreteFunctionType.THIS_SLOT_NAME, decl);
    scope.declareSlot(ConcreteFunctionType.RETURN_SLOT_NAME, decl);
    for(com.google.javascript.rhino.Node n = decl.getFirstChild().getNext().getFirstChild(); n != null; n = n.getNext()) {
      scope.declareSlot(n.getString(), n);
    }
    scope.initForScopeRoot(decl.getLastChild());
    return scope;
  }
  @Override() public StaticScope<ConcreteType> createInstanceScope(ObjectType instanceType) {
    ConcreteScope parentScope = null;
    ObjectType implicitProto = instanceType.getImplicitPrototype();
    if(implicitProto != null && !implicitProto.isUnknownType()) {
      ConcreteInstanceType prototype = createConcreteInstance(implicitProto);
      parentScope = (ConcreteScope)prototype.getScope();
    }
    ConcreteScope scope = new ConcreteScope(parentScope);
    for (String propName : instanceType.getOwnPropertyNames()) {
      scope.declareSlot(propName, null);
    }
    return scope;
  }
  private boolean getSubTypes(FunctionType type, Set<ConcreteType> set) {
    if(type.getSubTypes() != null) {
      for (FunctionType sub : type.getSubTypes()) {
        ConcreteType concrete = createType(sub);
        if(concrete.isFunction() && concrete.toFunction().getInstanceType() != null) {
          concrete = concrete.toFunction().getInstanceType();
          if(!set.contains(concrete)) {
            set.add(concrete);
            if(!getSubTypes(sub, set)) {
              return false;
            }
          }
        }
        else {
          set.clear();
          set.add(ConcreteType.ALL);
          return false;
        }
      }
    }
    return true;
  }
  @Override() public void process(Node externRoot, Node jsRoot) {
    topScope = new ConcreteScope(null);
    topScope.initForExternRoot(externRoot);
    topScope.initForScopeRoot(jsRoot);
    long maxIterations = 1000;
    long iterations = 0;
    Set<ConcreteScope> workSet = Sets.newHashSet(topScope);
    List<ConcreteScope> workList = Lists.newArrayList(topScope);
    boolean changed;
    do {
      changed = false;
      for(int i = 0; i < workList.size(); ++i) {
        ConcreteScope scope = workList.get(i);
        for (Action action : scope.getActions()) {
          for (Assignment assign : action.getAssignments(scope)) {
            if(assign.slot.addConcreteType(assign.type)) {
              changed = true;
              ConcreteScope varScope = assign.slot.getScope();
              if((varScope != scope) && !workSet.contains(varScope)) {
                workSet.add(varScope);
                workList.add(varScope);
              }
            }
          }
        }
      }
      Preconditions.checkState(++iterations != maxIterations, NON_HALTING_ERROR_MSG);
    }while(changed);
  }
  
  private static interface Action  {
    Collection<Assignment> getAssignments(ConcreteScope scope);
  }
  
  private static class Assignment  {
    final private ConcreteSlot slot;
    final private ConcreteType type;
    Assignment(ConcreteSlot slot, ConcreteType type) {
      super();
      this.slot = slot;
      this.type = type;
      Preconditions.checkNotNull(slot);
      Preconditions.checkNotNull(type);
    }
  }
  
  static class ConcreteJSTypePair  {
    final ConcreteType concrete;
    final JSType jstype;
    final int hashcode;
    ConcreteJSTypePair(ConcreteType concrete, JSType jstype) {
      super();
      this.concrete = concrete;
      this.jstype = jstype;
      this.hashcode = concrete.hashCode() + getJSTypeHashCode();
    }
    @Override() public boolean equals(Object o) {
      if(o instanceof ConcreteJSTypePair) {
        ConcreteJSTypePair pair = (ConcreteJSTypePair)o;
        if((pair.concrete.equals(this.concrete) && equalsJSType(pair.jstype))) {
          return true;
        }
      }
      return false;
    }
    private boolean equalsJSType(JSType jsType) {
      if(jsType == null || jstype == null) {
        return jstype == jsType;
      }
      else {
        return jsType.equals(this.jstype);
      }
    }
    private int getJSTypeHashCode() {
      return jstype != null ? jstype.hashCode() : 0;
    }
    @Override() public int hashCode() {
      return hashcode;
    }
  }
  
  class ConcreteScope implements StaticScope<ConcreteType>  {
    final private ConcreteScope parent;
    final private Map<String, ConcreteSlot> slots;
    final private List<Action> actions;
    ConcreteScope(ConcreteScope parent) {
      super();
      this.parent = parent;
      this.slots = Maps.newHashMap();
      this.actions = Lists.newArrayList();
    }
    Collection<ConcreteSlot> getSlots() {
      return slots.values();
    }
    @Override() public ConcreteType getTypeOfThis() {
      ConcreteSlot thisVar = slots.get(ConcreteFunctionType.THIS_SLOT_NAME);
      return (thisVar != null) ? thisVar.getType() : ConcreteType.NONE;
    }
    List<Action> getActions() {
      return actions;
    }
    @Override() public Node getRootNode() {
      return null;
    }
    @Override() public StaticScope<ConcreteType> getParentScope() {
      return parent;
    }
    @Override() public StaticSlot<ConcreteType> getOwnSlot(String name) {
      return slots.get(name);
    }
    @Override() public StaticSlot<ConcreteType> getSlot(String name) {
      StaticSlot<ConcreteType> var = getOwnSlot(name);
      if(var != null) {
        return var;
      }
      else 
        if(parent != null) {
          return parent.getSlot(name);
        }
        else {
          return null;
        }
    }
    @Override() public String toString() {
      return getTypeOfThis().toString() + " " + getSlots();
    }
    void addAction(Action action) {
      actions.add(action);
    }
    void declareSlot(String name, Node declaration) {
      slots.put(name, new ConcreteSlot(this, name));
    }
    void declareSlot(String name, Node declaration, ConcreteType type) {
      ConcreteSlot var = new ConcreteSlot(this, name);
      var.addConcreteType(type);
      slots.put(name, var);
    }
    void initForExternRoot(Node decl) {
      Preconditions.checkNotNull(decl);
      Preconditions.checkArgument(decl.isBlock());
      NodeTraversal.traverse(compiler, decl, new CreateScope(this, true));
    }
    void initForScopeRoot(Node decl) {
      Preconditions.checkNotNull(decl);
      if(decl.isFunction()) {
        decl = decl.getLastChild();
      }
      Preconditions.checkArgument(decl.isBlock());
      NodeTraversal.traverse(compiler, decl, new CreateScope(this, false));
    }
  }
  
  static class ConcreteSlot implements StaticSlot<ConcreteType>  {
    final private ConcreteScope scope;
    final private String name;
    private ConcreteType type;
    ConcreteSlot(ConcreteScope scope, String name) {
      super();
      this.scope = scope;
      this.name = name;
      this.type = ConcreteType.NONE;
    }
    ConcreteScope getScope() {
      return scope;
    }
    @Override() public ConcreteType getType() {
      return type;
    }
    @Override() public JSDocInfo getJSDocInfo() {
      return null;
    }
    @Override() public StaticReference<ConcreteType> getDeclaration() {
      return null;
    }
    @Override() public String getName() {
      return name;
    }
    @Override() public String toString() {
      return getName() + ": " + getType();
    }
    boolean addConcreteType(ConcreteType type) {
      ConcreteType origType = this.type;
      this.type = origType.unionWith(type);
      return !this.type.equals(origType);
    }
    @Override() public boolean isTypeInferred() {
      return true;
    }
  }
  
  private class CreateScope extends AbstractShallowCallback  {
    final private ConcreteScope scope;
    final private boolean inExterns;
    CreateScope(ConcreteScope scope, boolean inExterns) {
      super();
      this.scope = scope;
      this.inExterns = inExterns;
    }
    private Collection<Action> getImplicitActions(Node n) {
      switch (n.getType()){
        case Token.CALL:
        Node receiver = n.getFirstChild();
        if(!inExterns && receiver.isGetProp()) {
          return getImplicitActionsFromCall(n, receiver.getJSType());
        }
        break ;
        case Token.ASSIGN:
        Node lhs = n.getFirstChild();
        if(!inExterns && lhs.isGetProp()) {
          return getImplicitActionsFromProp(lhs.getFirstChild().getJSType(), lhs.getLastChild().getString(), n.getLastChild());
        }
        break ;
      }
      return null;
    }
    private Collection<Action> getImplicitActionsFromArgument(Node arg, ObjectType thisType, JSType paramType) {
      if(paramType.isUnionType()) {
        List<Action> actions = Lists.newArrayList();
        for (JSType paramAlt : paramType.toMaybeUnionType().getAlternates()) {
          actions.addAll(getImplicitActionsFromArgument(arg, thisType, paramAlt));
        }
        return actions;
      }
      else 
        if(paramType.isFunctionType()) {
          return Lists.<Action>newArrayList(createExternFunctionCall(arg, thisType, paramType.toMaybeFunctionType()));
        }
        else {
          return Lists.<Action>newArrayList(createExternFunctionCall(arg, thisType, null));
        }
    }
    private Collection<Action> getImplicitActionsFromCall(Node n, JSType recvType) {
      Node receiver = n.getFirstChild();
      if(recvType.isUnionType()) {
        List<Action> actions = Lists.newArrayList();
        for (JSType alt : recvType.toMaybeUnionType().getAlternates()) {
          actions.addAll(getImplicitActionsFromCall(n, alt));
        }
        return actions;
      }
      else 
        if(!(recvType.isFunctionType())) {
          return Lists.<Action>newArrayList();
        }
      ObjectType objType = ObjectType.cast(getJSType(receiver.getFirstChild()).restrictByNotNullOrUndefined());
      String prop = receiver.getLastChild().getString();
      if(objType != null && (objType.isPropertyInExterns(prop)) && (recvType.toMaybeFunctionType()).getParameters() != null) {
        List<Action> actions = Lists.newArrayList();
        Iterator<Node> paramIter = (recvType.toMaybeFunctionType()).getParameters().iterator();
        Iterator<Node> argumentIter = n.children().iterator();
        argumentIter.next();
        while(paramIter.hasNext() && argumentIter.hasNext()){
          Node arg = argumentIter.next();
          Node param = paramIter.next();
          if(arg.getJSType() != null && arg.getJSType().isFunctionType()) {
            actions.addAll(getImplicitActionsFromArgument(arg, arg.getJSType().toMaybeFunctionType().getTypeOfThis().toObjectType(), param.getJSType()));
          }
        }
        return actions;
      }
      return Lists.<Action>newArrayList();
    }
    private Collection<Action> getImplicitActionsFromProp(JSType jsType, String prop, Node fnNode) {
      List<Action> actions = Lists.newArrayList();
      if(jsType.isUnionType()) {
        boolean found = false;
        for (JSType alt : jsType.toMaybeUnionType().getAlternates()) {
          ObjectType altObj = ObjectType.cast(alt);
          if(altObj != null) {
            actions.addAll(getImplicitActionsFromPropNonUnion(altObj, prop, fnNode));
            if(altObj.hasProperty(prop)) {
              found = true;
            }
          }
        }
        if(found) {
          return actions;
        }
      }
      else {
        ObjectType objType = ObjectType.cast(jsType);
        if(objType != null && !objType.isUnknownType() && objType.hasProperty(prop)) {
          return getImplicitActionsFromPropNonUnion(objType, prop, fnNode);
        }
      }
      for (ObjectType type : getTypeRegistry().getEachReferenceTypeWithProperty(prop)) {
        actions.addAll(getImplicitActionsFromPropNonUnion(type, prop, fnNode));
      }
      return actions;
    }
    private Collection<Action> getImplicitActionsFromPropNonUnion(ObjectType jsType, String prop, Node fnNode) {
      JSType propType = jsType.getPropertyType(prop).restrictByNotNullOrUndefined();
      if(jsType.isPropertyInExterns(prop) && propType.isFunctionType()) {
        ObjectType thisType = jsType;
        if(jsType.isFunctionPrototypeType()) {
          thisType = thisType.getOwnerFunction().getInstanceType();
        }
        FunctionType callType = propType.toMaybeFunctionType();
        Action action = createExternFunctionCall(fnNode, thisType, callType);
        return Lists.<Action>newArrayList(action);
      }
      return Lists.<Action>newArrayList();
    }
    private ExternFunctionCall createExternFunctionCall(Node receiver, JSType jsThisType, FunctionType fun) {
      List<ConcreteType> argTypes = Lists.newArrayList();
      ConcreteType thisType;
      if(fun != null) {
        thisType = createType(jsThisType);
        for (Node arg : fun.getParameters()) {
          argTypes.add(createType(arg, scope));
        }
      }
      else {
        thisType = ConcreteType.NONE;
      }
      return new ExternFunctionCall(receiver, thisType, argTypes);
    }
    private JSType getJSType(Node n) {
      if(n.getJSType() != null) {
        return n.getJSType();
      }
      else {
        return getTypeRegistry().getNativeType(UNKNOWN_TYPE);
      }
    }
    private List<Action> createAssignmentActions(Node lhs, Node rhs, Node parent) {
      switch (lhs.getType()){
        case Token.NAME:
        ConcreteSlot var = (ConcreteSlot)scope.getSlot(lhs.getString());
        Preconditions.checkState(var != null, "Type tightener could not find variable with name %s", lhs.getString());
        return Lists.<Action>newArrayList(new VariableAssignAction(var, rhs));
        case Token.GETPROP:
        Node receiver = lhs.getFirstChild();
        return Lists.<Action>newArrayList(new PropertyAssignAction(receiver, rhs));
        case Token.GETELEM:
        return Lists.newArrayList();
        default:
        throw new AssertionError("Bad LHS for assignment: " + parent.toStringTree());
      }
    }
    private void addAction(Action action) {
      Preconditions.checkState(!inExterns, "Unexpected action in externs.");
      scope.addAction(action);
    }
    private void addActions(List<Action> actions) {
      Preconditions.checkState(!inExterns, "Unexpected action in externs.");
      for (Action action : actions) {
        scope.addAction(action);
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.VAR:
        Node name;
        for(name = n.getFirstChild(); name != null; name = name.getNext()) {
          if(inExterns) {
            scope.declareSlot(name.getString(), n, createType(name, scope));
          }
          else {
            scope.declareSlot(name.getString(), n);
            if(name.getFirstChild() != null) {
              addActions(createAssignmentActions(name, name.getFirstChild(), n));
            }
          }
        }
        break ;
        case Token.GETPROP:
        if(inExterns) {
          ConcreteType type = inferConcreteType(getTopScope(), n);
          if(type.isNone()) {
            ConcreteScope scope = (ConcreteScope)inferConcreteType(getTopScope(), n.getFirstChild()).getScope();
            if(scope != null) {
              type = createType(n.getJSType());
              if(type.isNone() || type.isAll()) {
                break ;
              }
              type = createUnionWithSubTypes(type);
              Node nameNode = n.getLastChild();
              scope.declareSlot(nameNode.getString(), n, type);
            }
          }
        }
        break ;
        case Token.FUNCTION:
        if(NodeUtil.isFunctionDeclaration(n)) {
          if(!n.getJSType().isNoObjectType()) {
            ConcreteFunctionType type = createConcreteFunction(n, scope);
            scope.declareSlot(n.getFirstChild().getString(), n, type);
            if(inExterns && type.getInstanceType() != null) {
              allInstantiatedTypes.add(type.getInstanceType());
            }
          }
        }
        break ;
        case Token.ASSIGN:
        Node lhs = n.getFirstChild();
        if(inExterns) {
          ConcreteScope scope;
          if(lhs.isGetProp()) {
            ConcreteType type = inferConcreteType(getTopScope(), lhs.getFirstChild());
            scope = (ConcreteScope)type.getScope();
          }
          else {
            scope = getTopScope();
          }
          if(scope == null) 
            break ;
          ConcreteType type = inferConcreteType(getTopScope(), n);
          if(type.isNone() || type.isAll()) {
            break ;
          }
          if(type.isFunction()) {
            JSType lhsType = lhs.getJSType();
            if(lhsType == null) {
              break ;
            }
            FunctionType funType = lhsType.restrictByNotNullOrUndefined().toMaybeFunctionType();
            if(funType == null) {
              break ;
            }
            ConcreteType retType = createType(funType.getReturnType());
            retType = createUnionWithSubTypes(retType);
            ConcreteType newret = type.toFunction().getReturnSlot().getType().unionWith(retType);
            ((ConcreteScope)type.getScope()).declareSlot(ConcreteFunctionType.RETURN_SLOT_NAME, n, newret);
          }
          scope.declareSlot(lhs.getLastChild().getString(), n, type);
        }
        else {
          addActions(createAssignmentActions(lhs, n.getLastChild(), n));
        }
        break ;
        case Token.NEW:
        case Token.CALL:
        Node receiver = n.getFirstChild();
        if(receiver.isGetProp()) {
          Node first = receiver.getFirstChild();
          if("call".equals(first.getNext().getString())) {
            if(first.isGetProp()) {
              addAction(new FunctionCallBuilder(first, receiver.getNext()).setPropName(first.getFirstChild().getNext().getString()).setIsCallFunction().build());
            }
            else {
              addAction(new FunctionCallBuilder(first, receiver.getNext()).setIsCallFunction().build());
            }
          }
          else {
            addAction(new FunctionCallBuilder(first, receiver.getNext()).setPropName(first.getNext().getString()).build());
          }
        }
        else {
          addAction(new FunctionCallBuilder(receiver, receiver.getNext()).setIsNewCall(n.isNew()).build());
        }
        break ;
        case Token.NAME:
        if(parent.isCatch() && parent.getFirstChild() == n) {
          scope.declareSlot(n.getString(), n, createUnionWithSubTypes(createType(getTypeRegistry().getType("Error")).toInstance()));
        }
        break ;
        case Token.RETURN:
        if(n.getFirstChild() != null) {
          addAction(new VariableAssignAction((ConcreteSlot)scope.getOwnSlot(ConcreteFunctionType.RETURN_SLOT_NAME), n.getFirstChild()));
        }
        break ;
      }
      Collection<Action> actions = getImplicitActions(n);
      if(actions != null) {
        for (Action action : actions) {
          addAction(action);
        }
      }
    }
  }
  
  private class ExternFunctionCall implements Action  {
    private Node receiver;
    private ConcreteType thisType;
    private List<ConcreteType> argTypes;
    ExternFunctionCall(Node receiver, ConcreteType thisType, List<ConcreteType> argTypes) {
      super();
      this.receiver = receiver;
      this.thisType = thisType;
      this.argTypes = argTypes;
    }
    @Override() public Collection<Assignment> getAssignments(ConcreteScope scope) {
      return getFunctionCallAssignments(inferConcreteType(scope, receiver), thisType, argTypes);
    }
  }
  
  private class FunctionCall implements Action  {
    final private boolean isNewCall;
    final private Node receiver;
    final private String propName;
    final private Node firstArgument;
    FunctionCall(boolean isNewCall, Node receiver, String propName, Node firstArgument) {
      super();
      this.isNewCall = isNewCall;
      this.receiver = receiver;
      this.propName = propName;
      this.firstArgument = firstArgument;
      Preconditions.checkNotNull(receiver);
    }
    @Override() public Collection<Assignment> getAssignments(ConcreteScope scope) {
      ConcreteType thisType = ConcreteType.NONE;
      ConcreteType recvType = inferConcreteType(scope, receiver);
      if(propName != null) {
        thisType = recvType;
        recvType = thisType.getPropertyType(propName);
      }
      if(recvType.isAll()) {
        throw new AssertionError("Found call on all type, which makes tighten types useless.");
      }
      if(isNewCall) {
        thisType = ConcreteType.NONE;
        for (ConcreteInstanceType instType : recvType.getFunctionInstanceTypes()) {
          thisType = thisType.unionWith(instType);
        }
        boolean added = allInstantiatedTypes.add(thisType);
        if(added) {
          typeIntersectionMemos.clear();
        }
      }
      List<ConcreteType> argTypes = Lists.newArrayList();
      for(com.google.javascript.rhino.Node arg = firstArgument; arg != null; arg = arg.getNext()) {
        argTypes.add(inferConcreteType(scope, arg));
      }
      return getFunctionCallAssignments(recvType, thisType, argTypes);
    }
  }
  
  private class FunctionCallBuilder  {
    private boolean isNewCall = false;
    private boolean isCallFunction = false;
    final private Node receiver;
    final private Node firstArgument;
    private String propName = null;
    FunctionCallBuilder(Node receiver, Node firstArgument) {
      super();
      this.receiver = receiver;
      this.firstArgument = firstArgument;
    }
    Action build() {
      if(isCallFunction) {
        return new NativeCallFunctionCall(receiver, propName, firstArgument);
      }
      else {
        return new FunctionCall(isNewCall, receiver, propName, firstArgument);
      }
    }
    FunctionCallBuilder setIsCallFunction() {
      Preconditions.checkState(!isNewCall, "A function call cannot be of the form: new Object.call()");
      isCallFunction = true;
      return this;
    }
    FunctionCallBuilder setIsNewCall(boolean isNew) {
      Preconditions.checkState(!(isCallFunction && isNew), "A function call cannot be of the form: new Object.call()");
      isNewCall = isNew;
      return this;
    }
    FunctionCallBuilder setPropName(String propName) {
      this.propName = propName;
      return this;
    }
  }
  
  private class NativeCallFunctionCall implements Action  {
    final private Node receiver;
    final private String propName;
    final private Node firstArgument;
    NativeCallFunctionCall(Node receiver, String propName, Node firstArgument) {
      super();
      this.receiver = receiver;
      this.propName = propName;
      this.firstArgument = firstArgument;
      Preconditions.checkNotNull(receiver);
    }
    @Override() public Collection<Assignment> getAssignments(ConcreteScope scope) {
      ConcreteType thisType = (firstArgument != null) ? inferConcreteType(scope, firstArgument) : getTopScope().getTypeOfThis();
      ConcreteType recvType = inferConcreteType(scope, receiver);
      if(recvType instanceof ConcreteInstanceType && ((ConcreteInstanceType)recvType).isFunctionPrototype()) {
        recvType = thisType.getPropertyType(propName);
      }
      List<ConcreteType> argTypes = Lists.newArrayList();
      for(com.google.javascript.rhino.Node arg = firstArgument.getNext(); arg != null; arg = arg.getNext()) {
        argTypes.add(inferConcreteType(scope, arg));
      }
      return getFunctionCallAssignments(recvType, thisType, argTypes);
    }
  }
  
  private class PropertyAssignAction implements Action  {
    final private Node receiver;
    final private String propName;
    final private Node expression;
    PropertyAssignAction(Node receiver, Node expr) {
      super();
      this.receiver = receiver;
      this.propName = receiver.getNext().getString();
      this.expression = expr;
      Preconditions.checkNotNull(receiver);
      Preconditions.checkNotNull(propName);
      Preconditions.checkNotNull(expr);
    }
    @Override() public Collection<Assignment> getAssignments(ConcreteScope scope) {
      ConcreteType recvType = inferConcreteType(scope, receiver);
      ConcreteType exprType = inferConcreteType(scope, expression);
      List<Assignment> assigns = Lists.newArrayList();
      for (StaticSlot<ConcreteType> prop : recvType.getPropertySlots(propName)) {
        assigns.add(new Assignment((ConcreteSlot)prop, exprType));
      }
      return assigns;
    }
  }
  
  private class VariableAssignAction implements Action  {
    final private ConcreteSlot slot;
    final private Node expression;
    VariableAssignAction(ConcreteSlot slot, Node expr) {
      super();
      this.slot = slot;
      this.expression = expr;
      Preconditions.checkNotNull(slot);
      Preconditions.checkNotNull(expr);
    }
    @Override() public Collection<Assignment> getAssignments(ConcreteScope scope) {
      return Lists.newArrayList(new Assignment(slot, inferConcreteType(scope, expression)));
    }
  }
}