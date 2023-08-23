package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticScope;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CodingConventions  {
  private CodingConventions() {
    super();
  }
  public static CodingConvention getDefault() {
    return new DefaultCodingConvention();
  }
  
  private static class DefaultCodingConvention implements CodingConvention  {
    final private static long serialVersionUID = 1L;
    @Override() public Bind describeFunctionBind(Node n) {
      return describeFunctionBind(n, false);
    }
    @Override() public Bind describeFunctionBind(Node n, boolean useTypeInfo) {
      if(!n.isCall()) {
        return null;
      }
      Node callTarget = n.getFirstChild();
      String name = callTarget.getQualifiedName();
      if(name != null) {
        if(name.equals("Function.prototype.bind.call")) {
          Node var_775 = callTarget.getNext();
          Node fn = var_775;
          if(fn == null) {
            return null;
          }
          Node thisValue = safeNext(fn);
          Node parameters = safeNext(thisValue);
          return new Bind(fn, thisValue, parameters);
        }
      }
      if(callTarget.isGetProp() && callTarget.getLastChild().getString().equals("bind")) {
        Node maybeFn = callTarget.getFirstChild();
        JSType maybeFnType = maybeFn.getJSType();
        FunctionType fnType = null;
        if(useTypeInfo && maybeFnType != null) {
          fnType = maybeFnType.restrictByNotNullOrUndefined().toMaybeFunctionType();
        }
        if(fnType != null || maybeFn.isFunction()) {
          Node thisValue = callTarget.getNext();
          Node parameters = safeNext(thisValue);
          return new Bind(maybeFn, thisValue, parameters);
        }
      }
      return null;
    }
    @Override() public Collection<AssertionFunctionSpec> getAssertionFunctions() {
      return Collections.emptySet();
    }
    @Override() public Collection<String> getIndirectlyDeclaredProperties() {
      return ImmutableList.of();
    }
    @Override() public DelegateRelationship getDelegateRelationship(Node callNode) {
      return null;
    }
    @Override() public List<String> identifyTypeDeclarationCall(Node n) {
      return null;
    }
    private Node safeNext(Node n) {
      if(n != null) {
        return n.getNext();
      }
      return null;
    }
    @Override() public ObjectLiteralCast getObjectLiteralCast(Node callNode) {
      return null;
    }
    @Override() public String extractClassNameIfProvide(Node node, Node parent) {
      String message = "only implemented in GoogleCodingConvention";
      throw new UnsupportedOperationException(message);
    }
    @Override() public String extractClassNameIfRequire(Node node, Node parent) {
      String message = "only implemented in GoogleCodingConvention";
      throw new UnsupportedOperationException(message);
    }
    @Override() public String getAbstractMethodName() {
      return null;
    }
    @Override() public String getDelegateSuperclassName() {
      return null;
    }
    @Override() public String getExportPropertyFunction() {
      return null;
    }
    @Override() public String getExportSymbolFunction() {
      return null;
    }
    @Override() public String getGlobalObject() {
      return "window";
    }
    @Override() public String getSingletonGetterClassName(Node callNode) {
      return null;
    }
    @Override() public SubclassRelationship getClassesDefinedByCall(Node callNode) {
      return null;
    }
    @Override() public boolean isConstant(String variableName) {
      return false;
    }
    @Override() public boolean isConstantKey(String variableName) {
      return false;
    }
    @Override() public boolean isExported(String name) {
      return isExported(name, false) || isExported(name, true);
    }
    @Override() public boolean isExported(String name, boolean local) {
      return local && name.startsWith("$super");
    }
    @Override() public boolean isInlinableFunction(Node n) {
      Preconditions.checkState(n.isFunction());
      return true;
    }
    @Override() public boolean isOptionalParameter(Node parameter) {
      return !isVarArgsParameter(parameter);
    }
    @Override() public boolean isPrivate(String name) {
      return false;
    }
    @Override() public boolean isPropertyTestFunction(Node call) {
      return false;
    }
    @Override() public boolean isPrototypeAlias(Node getProp) {
      return false;
    }
    @Override() public boolean isSuperClassReference(String propertyName) {
      return false;
    }
    @Override() public boolean isValidEnumKey(String key) {
      return key != null && key.length() > 0;
    }
    @Override() public boolean isVarArgsParameter(Node parameter) {
      return parameter.getParent().getLastChild() == parameter;
    }
    @Override() public void applyDelegateRelationship(ObjectType delegateSuperclass, ObjectType delegateBase, ObjectType delegator, FunctionType delegateProxy, FunctionType findDelegate) {
    }
    @Override() public void applySingletonGetter(FunctionType functionType, FunctionType getterType, ObjectType objectType) {
    }
    @Override() public void applySubclassRelationship(FunctionType parentCtor, FunctionType childCtor, SubclassType type) {
    }
    @Override() public void checkForCallingConventionDefiningCalls(Node n, Map<String, String> delegateCallingConventions) {
    }
    @Override() public void defineDelegateProxyPrototypeProperties(JSTypeRegistry registry, StaticScope<JSType> scope, List<ObjectType> delegateProxyPrototypes, Map<String, String> delegateCallingConventions) {
    }
  }
  
  public static class Proxy implements CodingConvention  {
    final protected CodingConvention nextConvention;
    protected Proxy(CodingConvention convention) {
      super();
      this.nextConvention = convention;
    }
    @Override() public Bind describeFunctionBind(Node n) {
      return describeFunctionBind(n, false);
    }
    @Override() public Bind describeFunctionBind(Node n, boolean useTypeInfo) {
      return nextConvention.describeFunctionBind(n, useTypeInfo);
    }
    @Override() public Collection<AssertionFunctionSpec> getAssertionFunctions() {
      return nextConvention.getAssertionFunctions();
    }
    @Override() public Collection<String> getIndirectlyDeclaredProperties() {
      return nextConvention.getIndirectlyDeclaredProperties();
    }
    @Override() public DelegateRelationship getDelegateRelationship(Node callNode) {
      return nextConvention.getDelegateRelationship(callNode);
    }
    @Override() public List<String> identifyTypeDeclarationCall(Node n) {
      return nextConvention.identifyTypeDeclarationCall(n);
    }
    @Override() public ObjectLiteralCast getObjectLiteralCast(Node callNode) {
      return nextConvention.getObjectLiteralCast(callNode);
    }
    @Override() public String extractClassNameIfProvide(Node node, Node parent) {
      return nextConvention.extractClassNameIfProvide(node, parent);
    }
    @Override() public String extractClassNameIfRequire(Node node, Node parent) {
      return nextConvention.extractClassNameIfRequire(node, parent);
    }
    @Override() public String getAbstractMethodName() {
      return nextConvention.getAbstractMethodName();
    }
    @Override() public String getDelegateSuperclassName() {
      return nextConvention.getDelegateSuperclassName();
    }
    @Override() public String getExportPropertyFunction() {
      return nextConvention.getExportPropertyFunction();
    }
    @Override() public String getExportSymbolFunction() {
      return nextConvention.getExportSymbolFunction();
    }
    @Override() public String getGlobalObject() {
      return nextConvention.getGlobalObject();
    }
    @Override() public String getSingletonGetterClassName(Node callNode) {
      return nextConvention.getSingletonGetterClassName(callNode);
    }
    @Override() public SubclassRelationship getClassesDefinedByCall(Node callNode) {
      return nextConvention.getClassesDefinedByCall(callNode);
    }
    @Override() public boolean isConstant(String variableName) {
      return nextConvention.isConstant(variableName);
    }
    @Override() public boolean isConstantKey(String keyName) {
      return nextConvention.isConstantKey(keyName);
    }
    @Override() final public boolean isExported(String name) {
      return isExported(name, false) || isExported(name, true);
    }
    @Override() public boolean isExported(String name, boolean local) {
      return nextConvention.isExported(name, local);
    }
    @Override() public boolean isInlinableFunction(Node n) {
      return nextConvention.isInlinableFunction(n);
    }
    @Override() public boolean isOptionalParameter(Node parameter) {
      return nextConvention.isOptionalParameter(parameter);
    }
    @Override() public boolean isPrivate(String name) {
      return nextConvention.isPrivate(name);
    }
    @Override() public boolean isPropertyTestFunction(Node call) {
      return nextConvention.isPropertyTestFunction(call);
    }
    @Override() public boolean isPrototypeAlias(Node getProp) {
      return false;
    }
    @Override() public boolean isSuperClassReference(String propertyName) {
      return nextConvention.isSuperClassReference(propertyName);
    }
    @Override() public boolean isValidEnumKey(String key) {
      return nextConvention.isValidEnumKey(key);
    }
    @Override() public boolean isVarArgsParameter(Node parameter) {
      return nextConvention.isVarArgsParameter(parameter);
    }
    @Override() public void applyDelegateRelationship(ObjectType delegateSuperclass, ObjectType delegateBase, ObjectType delegator, FunctionType delegateProxy, FunctionType findDelegate) {
      nextConvention.applyDelegateRelationship(delegateSuperclass, delegateBase, delegator, delegateProxy, findDelegate);
    }
    @Override() public void applySingletonGetter(FunctionType functionType, FunctionType getterType, ObjectType objectType) {
      nextConvention.applySingletonGetter(functionType, getterType, objectType);
    }
    @Override() public void applySubclassRelationship(FunctionType parentCtor, FunctionType childCtor, SubclassType type) {
      nextConvention.applySubclassRelationship(parentCtor, childCtor, type);
    }
    @Override() public void checkForCallingConventionDefiningCalls(Node n, Map<String, String> delegateCallingConventions) {
      nextConvention.checkForCallingConventionDefiningCalls(n, delegateCallingConventions);
    }
    @Override() public void defineDelegateProxyPrototypeProperties(JSTypeRegistry registry, StaticScope<JSType> scope, List<ObjectType> delegateProxyPrototypes, Map<String, String> delegateCallingConventions) {
      nextConvention.defineDelegateProxyPrototypeProperties(registry, scope, delegateProxyPrototypes, delegateCallingConventions);
    }
  }
}