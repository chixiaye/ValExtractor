package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.TypeValidator.TypeMismatch;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import java.util.Map;
import java.util.Set;

public class InlineProperties implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private static PropertyInfo INVALIDATED = new PropertyInfo(null, null);
  final private Map<String, PropertyInfo> props = Maps.newHashMap();
  private Set<JSType> invalidatingTypes;
  InlineProperties(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
    buildInvalidatingTypeSet();
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
  private void buildInvalidatingTypeSet() {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    invalidatingTypes = Sets.newHashSet(registry.getNativeType(JSTypeNative.ALL_TYPE), registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), registry.getNativeType(JSTypeNative.NO_TYPE), registry.getNativeType(JSTypeNative.NULL_TYPE), registry.getNativeType(JSTypeNative.VOID_TYPE), registry.getNativeType(JSTypeNative.FUNCTION_FUNCTION_TYPE), registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE), registry.getNativeType(JSTypeNative.FUNCTION_PROTOTYPE), registry.getNativeType(JSTypeNative.GLOBAL_THIS), registry.getNativeType(JSTypeNative.OBJECT_TYPE), registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE), registry.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE), registry.getNativeType(JSTypeNative.TOP_LEVEL_PROTOTYPE), registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    for (TypeMismatch mis : compiler.getTypeValidator().getMismatches()) {
      addInvalidatingType(mis.typeA);
      addInvalidatingType(mis.typeB);
    }
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverseRoots(compiler, new GatherCandidates(), externs, root);
    NodeTraversal.traverseRoots(compiler, new ReplaceCandidates(), externs, root);
  }
  
  class GatherCandidates extends AbstractPostOrderCallback  {
    private JSType maybeGetInstanceTypeFromPrototypeRef(Node src) {
      JSType ownerType = getJSType(src.getFirstChild());
      if(ownerType.isFunctionType() && ownerType.isConstructor()) {
        FunctionType functionType = ((FunctionType)ownerType);
        return functionType.getInstanceType();
      }
      return null;
    }
    private boolean inContructor(NodeTraversal t) {
      Node root = t.getScopeRoot();
      JSDocInfo info = NodeUtil.getBestJSDocInfo(root);
      return info != null && info.isConstructor();
    }
    private boolean maybeCandidateDefinition(NodeTraversal t, Node n, Node parent) {
      Preconditions.checkState(n.isGetProp() && parent.isAssign());
      boolean isCandidate = false;
      Node src = n.getFirstChild();
      String propName = n.getLastChild().getString();
      Node value = parent.getLastChild();
      if(src.isThis()) {
        if(inContructor(t)) {
          isCandidate = maybeStoreCandidateValue(getJSType(src), propName, value);
        }
      }
      else 
        if(t.inGlobalScope() && src.isGetProp() && src.getLastChild().getString().equals("prototype")) {
          JSType instanceType = maybeGetInstanceTypeFromPrototypeRef(src);
          if(instanceType != null) {
            isCandidate = maybeStoreCandidateValue(instanceType, propName, value);
          }
        }
      return isCandidate;
    }
    private boolean maybeStoreCandidateValue(JSType type, String propName, Node value) {
      Preconditions.checkNotNull(value);
      if(!props.containsKey(propName) && !isInvalidatingType(type) && NodeUtil.isImmutableValue(value) && NodeUtil.isExecutedExactlyOnce(value)) {
        props.put(propName, new PropertyInfo(type, value));
        return true;
      }
      return false;
    }
    private void invalidateProperty(String propName) {
      props.put(propName, INVALIDATED);
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      boolean invalidatingPropRef = false;
      String propName = null;
      if(n.isGetProp()) {
        propName = n.getLastChild().getString();
        CompilerInput var_593 = t.getInput();
        if(var_593.isExtern()) {
          invalidatingPropRef = true;
        }
        else 
          if(parent.isAssign()) {
            invalidatingPropRef = !maybeCandidateDefinition(t, n, parent);
          }
          else 
            if(NodeUtil.isLValue(n)) {
              invalidatingPropRef = true;
            }
            else 
              if(parent.isDelProp()) {
                invalidatingPropRef = true;
              }
              else {
                invalidatingPropRef = false;
              }
      }
      else 
        if(n.isStringKey()) {
          propName = n.getString();
          if(t.getInput().isExtern()) {
            invalidatingPropRef = true;
          }
          else {
            invalidatingPropRef = true;
          }
        }
      if(invalidatingPropRef) {
        Preconditions.checkNotNull(propName);
        invalidateProperty(propName);
      }
    }
  }
  
  static class PropertyInfo  {
    final JSType type;
    final Node value;
    PropertyInfo(JSType type, Node value) {
      super();
      this.type = type;
      this.value = value;
    }
  }
  
  class ReplaceCandidates extends AbstractPostOrderCallback  {
    private boolean isMatchingType(Node n, JSType src) {
      src = src.restrictByNotNullOrUndefined();
      JSType dest = getJSType(n).restrictByNotNullOrUndefined();
      if(!isInvalidatingType(dest) && dest.isSubtype(src)) {
        return true;
      }
      return false;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isGetProp() && !NodeUtil.isLValue(n)) {
        Node target = n.getFirstChild();
        String propName = n.getLastChild().getString();
        PropertyInfo info = props.get(propName);
        if(info != null && info != INVALIDATED && isMatchingType(target, info.type)) {
          Node replacement = info.value.cloneTree();
          if(NodeUtil.mayHaveSideEffects(n.getFirstChild(), compiler)) {
            replacement = IR.comma(n.removeFirstChild(), replacement).srcref(n);
          }
          parent.replaceChild(n, replacement);
          compiler.reportCodeChange();
        }
      }
    }
  }
}