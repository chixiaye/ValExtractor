package com.google.javascript.rhino.jstype;
import static com.google.javascript.rhino.jstype.TernaryValue.UNKNOWN;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.jstype.JSTypeRegistry.ResolveMode;
import java.io.Serializable;
import java.util.Comparator;

abstract public class JSType implements Serializable  {
  final private static long serialVersionUID = 1L;
  private boolean resolved = false;
  private JSType resolveResult = null;
  final private ImmutableList<String> templateKeys;
  final private ImmutableList<JSType> templatizedTypes;
  private boolean inTemplatedCheckVisit = false;
  final private static CanCastToVisitor CAN_CAST_TO_VISITOR = new CanCastToVisitor();
  final public static String UNKNOWN_NAME = "Unknown class name";
  final public static String NOT_A_CLASS = "Not declared as a constructor";
  final public static String NOT_A_TYPE = "Not declared as a type name";
  final public static String EMPTY_TYPE_COMPONENT = "Named type with empty name component";
  final static Comparator<JSType> ALPHA = new Comparator<JSType>() {
      @Override() public int compare(JSType t1, JSType t2) {
        return t1.toString().compareTo(t2.toString());
      }
  };
  final public static int ENUMDECL = 1;
  final public static int NOT_ENUMDECL = 0;
  final JSTypeRegistry registry;
  JSType(JSTypeRegistry registry) {
    this(registry, null, null);
  }
  JSType(JSTypeRegistry registry, ImmutableList<String> templateKeys, ImmutableList<JSType> templatizedTypes) {
    super();
    this.registry = registry;
    int keysLength = templateKeys == null ? 0 : templateKeys.size();
    int typesLength = templatizedTypes == null ? 0 : templatizedTypes.size();
    if(typesLength > keysLength) {
      throw new IllegalArgumentException("Cannot have more templatized types than template keys");
    }
    else 
      if(typesLength < keysLength) {
        ImmutableList.Builder<JSType> builder = ImmutableList.builder();
        if(typesLength > 0) {
          builder.addAll(templatizedTypes);
        }
        for(int i = 0; i < keysLength - typesLength; i++) {
          builder.add(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
        }
        templatizedTypes = builder.build();
      }
      else 
        if(keysLength == 0 && typesLength == 0) {
          templateKeys = ImmutableList.of();
          templatizedTypes = ImmutableList.of();
        }
    this.templateKeys = templateKeys;
    this.templatizedTypes = templatizedTypes;
  }
  abstract public BooleanLiteralSet getPossibleToBooleanOutcomes();
  public EnumElementType toMaybeEnumElementType() {
    return null;
  }
  public EnumType toMaybeEnumType() {
    return null;
  }
  public FunctionType toMaybeFunctionType() {
    return null;
  }
  public static FunctionType toMaybeFunctionType(JSType type) {
    return type == null ? null : type.toMaybeFunctionType();
  }
  public ImmutableList<JSType> getTemplatizedTypes() {
    return templatizedTypes;
  }
  public ImmutableList<String> getTemplateKeys() {
    return templateKeys;
  }
  public JSDocInfo getJSDocInfo() {
    return null;
  }
  public JSType autobox() {
    JSType restricted = restrictByNotNullOrUndefined();
    JSType autobox = restricted.autoboxesTo();
    return autobox == null ? restricted : autobox;
  }
  public JSType autoboxesTo() {
    return null;
  }
  public JSType collapseUnion() {
    return this;
  }
  static JSType filterNoResolvedType(JSType type) {
    if(type.isNoResolvedType()) {
      return type.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    }
    else 
      if(type.isUnionType()) {
        UnionType unionType = type.toMaybeUnionType();
        boolean needsFiltering = false;
        for (JSType alt : unionType.getAlternates()) {
          boolean var_2653 = alt.isNoResolvedType();
          if(var_2653) {
            needsFiltering = true;
            break ;
          }
        }
        if(needsFiltering) {
          UnionTypeBuilder builder = new UnionTypeBuilder(type.registry);
          builder.addAlternate(type.getNativeType(JSTypeNative.NO_RESOLVED_TYPE));
          for (JSType alt : unionType.getAlternates()) {
            if(!alt.isNoResolvedType()) {
              builder.addAlternate(alt);
            }
          }
          return builder.build();
        }
      }
    return type;
  }
  public JSType findPropertyType(String propertyName) {
    ObjectType autoboxObjType = ObjectType.cast(autoboxesTo());
    if(autoboxObjType != null) {
      return autoboxObjType.findPropertyType(propertyName);
    }
    return null;
  }
  final public JSType forceResolve(ErrorReporter t, StaticScope<JSType> scope) {
    ResolveMode oldResolveMode = registry.getResolveMode();
    registry.setResolveMode(ResolveMode.IMMEDIATE);
    JSType result = resolve(t, scope);
    registry.setResolveMode(oldResolveMode);
    return result;
  }
  public JSType getGreatestSubtype(JSType that) {
    return getGreatestSubtype(this, that);
  }
  static JSType getGreatestSubtype(JSType thisType, JSType thatType) {
    if(thisType.isFunctionType() && thatType.isFunctionType()) {
      return thisType.toMaybeFunctionType().supAndInfHelper(thatType.toMaybeFunctionType(), false);
    }
    else 
      if(thisType.isEquivalentTo(thatType)) {
        return thisType;
      }
      else 
        if(thisType.isUnknownType() || thatType.isUnknownType()) {
          return thisType.isEquivalentTo(thatType) ? thisType : thisType.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        }
        else 
          if(thisType.isUnionType()) {
            return thisType.toMaybeUnionType().meet(thatType);
          }
          else 
            if(thatType.isUnionType()) {
              return thatType.toMaybeUnionType().meet(thisType);
            }
            else 
              if(thisType.isParameterizedType()) {
                return thisType.toMaybeParameterizedType().getGreatestSubtypeHelper(thatType);
              }
              else 
                if(thatType.isParameterizedType()) {
                  return thatType.toMaybeParameterizedType().getGreatestSubtypeHelper(thisType);
                }
                else 
                  if(thisType.isSubtype(thatType)) {
                    return filterNoResolvedType(thisType);
                  }
                  else 
                    if(thatType.isSubtype(thisType)) {
                      return filterNoResolvedType(thatType);
                    }
                    else 
                      if(thisType.isRecordType()) {
                        return thisType.toMaybeRecordType().getGreatestSubtypeHelper(thatType);
                      }
                      else 
                        if(thatType.isRecordType()) {
                          return thatType.toMaybeRecordType().getGreatestSubtypeHelper(thisType);
                        }
    if(thisType.isEnumElementType()) {
      JSType inf = thisType.toMaybeEnumElementType().meet(thatType);
      if(inf != null) {
        return inf;
      }
    }
    else 
      if(thatType.isEnumElementType()) {
        JSType inf = thatType.toMaybeEnumElementType().meet(thisType);
        if(inf != null) {
          return inf;
        }
      }
    if(thisType.isObject() && thatType.isObject()) {
      return thisType.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    }
    return thisType.getNativeType(JSTypeNative.NO_TYPE);
  }
  public JSType getLeastSupertype(JSType that) {
    if(that.isUnionType()) {
      return that.toMaybeUnionType().getLeastSupertype(this);
    }
    return getLeastSupertype(this, that);
  }
  static JSType getLeastSupertype(JSType thisType, JSType thatType) {
    boolean areEquivalent = thisType.isEquivalentTo(thatType);
    return areEquivalent ? thisType : filterNoResolvedType(thisType.registry.createUnionType(thisType, thatType));
  }
  JSType getNativeType(JSTypeNative typeId) {
    return registry.getNativeType(typeId);
  }
  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {
    if(outcome && this == getNativeType(JSTypeNative.UNKNOWN_TYPE)) {
      return getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
    }
    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if(literals.contains(outcome)) {
      return this;
    }
    else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }
  public JSType getTemplatizedType(String key) {
    int index = templateKeys.indexOf(key);
    if(index < 0) {
      return registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    }
    return templatizedTypes.get(index);
  }
  final public JSType resolve(ErrorReporter t, StaticScope<JSType> scope) {
    if(resolved) {
      if(resolveResult == null) {
        return registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
      }
      return resolveResult;
    }
    resolved = true;
    resolveResult = resolveInternal(t, scope);
    resolveResult.setResolvedTypeInternal(resolveResult);
    return resolveResult;
  }
  abstract JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope);
  public JSType restrictByNotNullOrUndefined() {
    return this;
  }
  final static JSType safeResolve(JSType type, ErrorReporter t, StaticScope<JSType> scope) {
    return type == null ? null : type.resolve(t, scope);
  }
  public JSType unboxesTo() {
    return null;
  }
  final public ObjectType dereference() {
    return autobox().toObjectType();
  }
  public ObjectType toObjectType() {
    return this instanceof ObjectType ? (ObjectType)this : null;
  }
  public ParameterizedType toMaybeParameterizedType() {
    return null;
  }
  public static ParameterizedType toMaybeParameterizedType(JSType type) {
    return type == null ? null : type.toMaybeParameterizedType();
  }
  RecordType toMaybeRecordType() {
    return null;
  }
  public String getDisplayName() {
    return null;
  }
  final public String toAnnotationString() {
    return toStringHelper(true);
  }
  public String toDebugHashCodeString() {
    return "{" + hashCode() + "}";
  }
  @Override() public String toString() {
    return toStringHelper(false);
  }
  abstract String toStringHelper(boolean forAnnotations);
  abstract  <T extends java.lang.Object> T visit(RelationshipVisitor<T> visitor, JSType that);

  abstract public  <T extends java.lang.Object> T visit(Visitor<T> visitor);

  public TemplateType toMaybeTemplateType() {
    return null;
  }
  public static TemplateType toMaybeTemplateType(JSType type) {
    return type == null ? null : type.toMaybeTemplateType();
  }
  public TernaryValue testForEquality(JSType that) {
    return testForEqualityHelper(this, that);
  }
  TernaryValue testForEqualityHelper(JSType aType, JSType bType) {
    if(bType.isAllType() || bType.isUnknownType() || bType.isNoResolvedType() || aType.isAllType() || aType.isUnknownType() || aType.isNoResolvedType()) {
      return UNKNOWN;
    }
    boolean aIsEmpty = aType.isEmptyType();
    boolean bIsEmpty = bType.isEmptyType();
    if(aIsEmpty || bIsEmpty) {
      if(aIsEmpty && bIsEmpty) {
        return TernaryValue.TRUE;
      }
      else {
        return UNKNOWN;
      }
    }
    if(aType.isFunctionType() || bType.isFunctionType()) {
      JSType otherType = aType.isFunctionType() ? bType : aType;
      JSType meet = otherType.getGreatestSubtype(getNativeType(JSTypeNative.OBJECT_TYPE));
      if(meet.isNoType() || meet.isNoObjectType()) {
        return TernaryValue.FALSE;
      }
      else {
        return TernaryValue.UNKNOWN;
      }
    }
    if(bType.isEnumElementType() || bType.isUnionType()) {
      return bType.testForEquality(aType);
    }
    return null;
  }
  public TypePair getTypesUnderEquality(JSType that) {
    if(that.isUnionType()) {
      TypePair p = that.toMaybeUnionType().getTypesUnderEquality(this);
      return new TypePair(p.typeB, p.typeA);
    }
    switch (testForEquality(that)){
      case FALSE:
      return new TypePair(null, null);
      case TRUE:
      case UNKNOWN:
      return new TypePair(this, that);
    }
    throw new IllegalStateException();
  }
  public TypePair getTypesUnderInequality(JSType that) {
    if(that.isUnionType()) {
      TypePair p = that.toMaybeUnionType().getTypesUnderInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }
    switch (testForEquality(that)){
      case TRUE:
      JSType noType = getNativeType(JSTypeNative.NO_TYPE);
      return new TypePair(noType, noType);
      case FALSE:
      case UNKNOWN:
      return new TypePair(this, that);
    }
    throw new IllegalStateException();
  }
  public TypePair getTypesUnderShallowEquality(JSType that) {
    JSType commonType = getGreatestSubtype(that);
    return new TypePair(commonType, commonType);
  }
  public TypePair getTypesUnderShallowInequality(JSType that) {
    if(that.isUnionType()) {
      TypePair p = that.toMaybeUnionType().getTypesUnderShallowInequality(this);
      return new TypePair(p.typeB, p.typeA);
    }
    if(isNullType() && that.isNullType() || isVoidType() && that.isVoidType()) {
      return new TypePair(null, null);
    }
    else {
      return new TypePair(this, that);
    }
  }
  public UnionType toMaybeUnionType() {
    return null;
  }
  public boolean canBeCalled() {
    return false;
  }
  public boolean canCastTo(JSType that) {
    return this.visit(CAN_CAST_TO_VISITOR, that);
  }
  final public boolean canTestForEqualityWith(JSType that) {
    return testForEquality(that).equals(UNKNOWN);
  }
  final public boolean canTestForShallowEqualityWith(JSType that) {
    if(isEmptyType() || that.isEmptyType()) {
      return isSubtype(that) || that.isSubtype(this);
    }
    JSType inf = getGreatestSubtype(that);
    return !inf.isEmptyType() || inf == registry.getNativeType(JSTypeNative.LEAST_FUNCTION_TYPE);
  }
  boolean checkEquivalenceHelper(JSType that, EquivalenceMethod eqMethod) {
    if(this == that) {
      return true;
    }
    boolean thisUnknown = isUnknownType();
    boolean thatUnknown = that.isUnknownType();
    if(thisUnknown || thatUnknown) {
      if(eqMethod == EquivalenceMethod.INVARIANT) {
        return true;
      }
      else 
        if(eqMethod == EquivalenceMethod.DATA_FLOW) {
          return thisUnknown && thatUnknown;
        }
        else 
          if(thisUnknown && thatUnknown && (isNominalType() ^ that.isNominalType())) {
            return false;
          }
    }
    if(isUnionType() && that.isUnionType()) {
      return toMaybeUnionType().checkUnionEquivalenceHelper(that.toMaybeUnionType(), eqMethod);
    }
    if(isFunctionType() && that.isFunctionType()) {
      return toMaybeFunctionType().checkFunctionEquivalenceHelper(that.toMaybeFunctionType(), eqMethod);
    }
    if(isRecordType() && that.isRecordType()) {
      return toMaybeRecordType().checkRecordEquivalenceHelper(that.toMaybeRecordType(), eqMethod);
    }
    ParameterizedType thisParamType = toMaybeParameterizedType();
    ParameterizedType thatParamType = that.toMaybeParameterizedType();
    if(thisParamType != null || thatParamType != null) {
      boolean paramsMatch = false;
      if(thisParamType != null && thatParamType != null) {
        paramsMatch = thisParamType.getParameterType().checkEquivalenceHelper(thatParamType.getParameterType(), eqMethod);
      }
      else 
        if(eqMethod == EquivalenceMethod.IDENTITY) {
          paramsMatch = false;
        }
        else {
          paramsMatch = true;
        }
      JSType thisRootType = thisParamType == null ? this : thisParamType.getReferencedTypeInternal();
      JSType thatRootType = thatParamType == null ? that : thatParamType.getReferencedTypeInternal();
      return paramsMatch && thisRootType.checkEquivalenceHelper(thatRootType, eqMethod);
    }
    if(isNominalType() && that.isNominalType()) {
      return toObjectType().getReferenceName().equals(that.toObjectType().getReferenceName());
    }
    if(this instanceof ProxyObjectType) {
      return ((ProxyObjectType)this).getReferencedTypeInternal().checkEquivalenceHelper(that, eqMethod);
    }
    if(that instanceof ProxyObjectType) {
      return checkEquivalenceHelper(((ProxyObjectType)that).getReferencedTypeInternal(), eqMethod);
    }
    return this == that;
  }
  final public boolean differsFrom(JSType that) {
    return !checkEquivalenceHelper(that, EquivalenceMethod.DATA_FLOW);
  }
  @Override() public boolean equals(Object jsType) {
    return (jsType instanceof JSType) ? isEquivalentTo((JSType)jsType) : false;
  }
  public boolean hasAnyTemplateTypes() {
    if(!this.inTemplatedCheckVisit) {
      this.inTemplatedCheckVisit = true;
      boolean result = hasAnyTemplateTypesInternal();
      this.inTemplatedCheckVisit = false;
      return result;
    }
    else {
      return false;
    }
  }
  boolean hasAnyTemplateTypesInternal() {
    if(isTemplatized()) {
      for (JSType templatizedType : templatizedTypes) {
        if(templatizedType.hasAnyTemplateTypes()) {
          return true;
        }
      }
    }
    return false;
  }
  public boolean hasDisplayName() {
    String displayName = getDisplayName();
    return displayName != null && !displayName.isEmpty();
  }
  static boolean hasEquivalentTemplateTypes(JSType type1, JSType type2, EquivalenceMethod eqMethod) {
    ImmutableList<JSType> templatizedTypes1 = type1.getTemplatizedTypes();
    ImmutableList<JSType> templatizedTypes2 = type2.getTemplatizedTypes();
    int nTemplatizedTypes1 = templatizedTypes1.size();
    int nTemplatizedTypes2 = templatizedTypes2.size();
    if(nTemplatizedTypes1 != nTemplatizedTypes2) {
      return false;
    }
    for(int i = 0; i < nTemplatizedTypes1; i++) {
      JSType templatizedType1 = templatizedTypes1.get(i);
      JSType templatizedType2 = templatizedTypes2.get(i);
      if(templatizedType1.checkEquivalenceHelper(templatizedType2, eqMethod)) {
        return false;
      }
    }
    return true;
  }
  public boolean hasProperty(String pname) {
    return false;
  }
  public boolean hasTemplatizedType(String key) {
    return templateKeys.contains(key);
  }
  public boolean isAllType() {
    return false;
  }
  public boolean isArrayType() {
    return false;
  }
  public boolean isBooleanObjectType() {
    return false;
  }
  public boolean isBooleanValueType() {
    return false;
  }
  public boolean isCheckedUnknownType() {
    return false;
  }
  public boolean isConstructor() {
    return false;
  }
  public boolean isDateType() {
    return false;
  }
  public boolean isDict() {
    if(isObject()) {
      ObjectType objType = toObjectType();
      ObjectType iproto = objType.getImplicitPrototype();
      if(iproto != null && iproto.isDict()) {
        return true;
      }
      FunctionType ctor = objType.getConstructor();
      if(ctor == null) {
        JSDocInfo info = objType.getJSDocInfo();
        return info != null && info.makesDicts();
      }
      else {
        return ctor.makesDicts();
      }
    }
    return false;
  }
  final public boolean isEmptyType() {
    return isNoType() || isNoObjectType() || isNoResolvedType() || (registry.getNativeFunctionType(JSTypeNative.LEAST_FUNCTION_TYPE) == this);
  }
  final public boolean isEnumElementType() {
    return toMaybeEnumElementType() != null;
  }
  public boolean isEnumType() {
    return toMaybeEnumType() != null;
  }
  public static boolean isEquivalent(JSType typeA, JSType typeB) {
    return (typeA == null || typeB == null) ? typeA == typeB : typeA.isEquivalentTo(typeB);
  }
  final public boolean isEquivalentTo(JSType that) {
    return checkEquivalenceHelper(that, EquivalenceMethod.IDENTITY);
  }
  public boolean isFunctionPrototypeType() {
    return false;
  }
  final public boolean isFunctionType() {
    return toMaybeFunctionType() != null;
  }
  final public boolean isGlobalThisType() {
    return this == registry.getNativeType(JSTypeNative.GLOBAL_THIS);
  }
  public boolean isInstanceType() {
    return false;
  }
  public boolean isInterface() {
    return false;
  }
  final public boolean isInvariant(JSType that) {
    return checkEquivalenceHelper(that, EquivalenceMethod.INVARIANT);
  }
  boolean isNamedType() {
    return false;
  }
  public boolean isNoObjectType() {
    return false;
  }
  public boolean isNoResolvedType() {
    return false;
  }
  public boolean isNoType() {
    return false;
  }
  final public boolean isNominalConstructor() {
    if(isConstructor() || isInterface()) {
      FunctionType fn = toMaybeFunctionType();
      if(fn == null) {
        return false;
      }
      if(fn.getSource() != null) {
        return true;
      }
      return fn.isNativeObjectType();
    }
    return false;
  }
  public boolean isNominalType() {
    return false;
  }
  public boolean isNullType() {
    return false;
  }
  public boolean isNullable() {
    return isSubtype(getNativeType(JSTypeNative.NULL_TYPE));
  }
  final public boolean isNumber() {
    return isSubtype(getNativeType(JSTypeNative.NUMBER_VALUE_OR_OBJECT_TYPE));
  }
  public boolean isNumberObjectType() {
    return false;
  }
  public boolean isNumberValueType() {
    return false;
  }
  public boolean isObject() {
    return false;
  }
  public boolean isOrdinaryFunction() {
    return false;
  }
  final public boolean isParameterizedType() {
    return toMaybeParameterizedType() != null;
  }
  public boolean isRecordType() {
    return toMaybeRecordType() != null;
  }
  public boolean isRegexpType() {
    return false;
  }
  final public boolean isResolved() {
    return resolved;
  }
  final public boolean isString() {
    return isSubtype(getNativeType(JSTypeNative.STRING_VALUE_OR_OBJECT_TYPE));
  }
  public boolean isStringObjectType() {
    return false;
  }
  public boolean isStringValueType() {
    return false;
  }
  public boolean isStruct() {
    if(isObject()) {
      ObjectType objType = toObjectType();
      ObjectType iproto = objType.getImplicitPrototype();
      if(iproto != null && iproto.isStruct()) {
        return true;
      }
      FunctionType ctor = objType.getConstructor();
      if(ctor == null) {
        JSDocInfo info = objType.getJSDocInfo();
        return info != null && info.makesStructs();
      }
      else {
        return ctor.makesStructs();
      }
    }
    return false;
  }
  public boolean isSubtype(JSType that) {
    return isSubtypeHelper(this, that);
  }
  static boolean isSubtypeHelper(JSType thisType, JSType thatType) {
    if(thatType.isUnknownType()) {
      return true;
    }
    if(thatType.isAllType()) {
      return true;
    }
    if(thisType.isEquivalentTo(thatType)) {
      return true;
    }
    if(thatType.isUnionType()) {
      UnionType union = thatType.toMaybeUnionType();
      for (JSType element : union.alternates) {
        if(thisType.isSubtype(element)) {
          return true;
        }
      }
      return false;
    }
    if(thisType.isParameterizedType()) {
      return thisType.toMaybeParameterizedType().isParameterizeSubtypeOf(thatType);
    }
    if(thatType instanceof ProxyObjectType) {
      return thisType.isSubtype(((ProxyObjectType)thatType).getReferencedTypeInternal());
    }
    return false;
  }
  final public boolean isTemplateType() {
    return toMaybeTemplateType() != null;
  }
  public boolean isTemplatized() {
    return !templateKeys.isEmpty();
  }
  boolean isTheObjectType() {
    return false;
  }
  final public boolean isUnionType() {
    return toMaybeUnionType() != null;
  }
  public boolean isUnknownType() {
    return false;
  }
  public boolean isVoidType() {
    return false;
  }
  final public boolean matchesInt32Context() {
    return matchesNumberContext();
  }
  public boolean matchesNumberContext() {
    return false;
  }
  public boolean matchesObjectContext() {
    return false;
  }
  public boolean matchesStringContext() {
    return false;
  }
  final public boolean matchesUint32Context() {
    return matchesNumberContext();
  }
  public boolean setValidator(Predicate<JSType> validator) {
    return validator.apply(this);
  }
  @Override() public int hashCode() {
    return System.identityHashCode(this);
  }
  final public void clearResolved() {
    resolved = false;
    resolveResult = null;
  }
  public void matchConstraint(JSType constraint) {
  }
  void setResolvedTypeInternal(JSType type) {
    resolveResult = type;
    resolved = true;
  }
  
  public static class TypePair  {
    final public JSType typeA;
    final public JSType typeB;
    public TypePair(JSType typeA, JSType typeB) {
      super();
      this.typeA = typeA;
      this.typeB = typeB;
    }
  }
}