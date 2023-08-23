package com.google.javascript.rhino.jstype;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import java.util.List;

public class ModificationVisitor implements Visitor<JSType>  {
  final private JSTypeRegistry registry;
  public ModificationVisitor(JSTypeRegistry registry) {
    super();
    this.registry = registry;
  }
  @Override() public JSType caseAllType() {
    return getNativeType(JSTypeNative.ALL_TYPE);
  }
  @Override() public JSType caseBooleanType() {
    return getNativeType(JSTypeNative.BOOLEAN_TYPE);
  }
  @Override() public JSType caseEnumElementType(EnumElementType type) {
    return type;
  }
  @Override() public JSType caseFunctionType(FunctionType type) {
    if(isNativeFunctionType(type)) {
      return type;
    }
    if(!type.isOrdinaryFunction()) {
      return type;
    }
    boolean changed = false;
    JSType beforeThis = type.getTypeOfThis();
    JSType afterThis = coerseToThisType(beforeThis.visit(this));
    if(beforeThis != afterThis) {
      changed = true;
    }
    JSType beforeReturn = type.getReturnType();
    JSType afterReturn = beforeReturn.visit(this);
    if(beforeReturn != afterReturn) {
      changed = true;
    }
    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    for (Node paramNode : type.getParameters()) {
      JSType beforeParamType = paramNode.getJSType();
      JSType afterParamType = beforeParamType.visit(this);
      if(beforeParamType != afterParamType) {
        changed = true;
      }
      if(paramNode.isOptionalArg()) {
        paramBuilder.addOptionalParams(afterParamType);
      }
      else 
        if(paramNode.isVarArgs()) {
          paramBuilder.addVarArgs(afterParamType);
        }
        else {
          paramBuilder.addRequiredParams(afterParamType);
        }
    }
    if(changed) {
      FunctionBuilder builder = new FunctionBuilder(registry);
      builder.withParams(paramBuilder);
      builder.withReturnType(afterReturn);
      builder.withTypeOfThis(afterThis);
      return builder.build();
    }
    return type;
  }
  @Override() public JSType caseNoObjectType() {
    return getNativeType(JSTypeNative.NO_OBJECT_TYPE);
  }
  @Override() public JSType caseNoType() {
    return getNativeType(JSTypeNative.NO_TYPE);
  }
  @Override() public JSType caseNullType() {
    return getNativeType(JSTypeNative.NULL_TYPE);
  }
  @Override() public JSType caseNumberType() {
    return getNativeType(JSTypeNative.NUMBER_TYPE);
  }
  @Override() public JSType caseObjectType(ObjectType objType) {
    if(objType.isTemplatized()) {
      ImmutableList.Builder<JSType> builder = ImmutableList.builder();
      for (JSType templatizedType : objType.getTemplatizedTypes()) {
        builder.add(templatizedType.visit(this));
      }
      return registry.createTemplatizedType(objType, builder.build());
    }
    else {
      return objType;
    }
  }
  @Override() public JSType caseParameterizedType(ParameterizedType type) {
    ObjectType genericType = ObjectType.cast(type.getReferencedTypeInternal().visit(this));
    JSType var_2690 = type.getParameterType();
    JSType paramType = var_2690.visit(this);
    if(type.getReferencedTypeInternal() != genericType || type.getParameterType() != paramType) {
      type = registry.createParameterizedType(genericType, paramType);
    }
    return type;
  }
  @Override() public JSType caseStringType() {
    return getNativeType(JSTypeNative.STRING_TYPE);
  }
  @Override() public JSType caseTemplateType(TemplateType type) {
    return type;
  }
  @Override() public JSType caseUnionType(UnionType type) {
    boolean changed = false;
    List<JSType> results = Lists.newArrayList();
    for (JSType alternative : type.getAlternates()) {
      JSType replacement = alternative.visit(this);
      if(replacement != alternative) {
        changed = true;
      }
      results.add(replacement);
    }
    if(changed) {
      UnionTypeBuilder builder = new UnionTypeBuilder(registry);
      for (JSType alternate : results) {
        builder.addAlternate(alternate);
      }
      return builder.build();
    }
    return type;
  }
  @Override() public JSType caseUnknownType() {
    return getNativeType(JSTypeNative.UNKNOWN_TYPE);
  }
  @Override() public JSType caseVoidType() {
    return getNativeType(JSTypeNative.VOID_TYPE);
  }
  private JSType coerseToThisType(JSType type) {
    return type != null ? type : registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE);
  }
  private JSType getNativeType(JSTypeNative nativeType) {
    return registry.getNativeType(nativeType);
  }
  private boolean isNativeFunctionType(FunctionType type) {
    return type.isNativeObjectType();
  }
}