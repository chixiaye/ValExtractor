package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.NameReferenceGraph.Name;
import com.google.javascript.jscomp.NameReferenceGraph.Reference;
import com.google.javascript.jscomp.NodeTraversal.ScopedCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;

class NameReferenceGraphConstruction implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private NameReferenceGraph graph;
  final private Multimap<String, NameUse> unknownNameUse = HashMultimap.create();
  final private static boolean CONSERVATIVE = false;
  final private ArrayList<Name> currentFunctionStack = new ArrayList<Name>();
  NameReferenceGraphConstruction(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
    this.graph = new NameReferenceGraph(compiler);
  }
  private JSType getType(Node n) {
    JSType type = n.getJSType();
    if(type == null) {
      if(CONSERVATIVE) {
        throw new RuntimeException("Type system failed us :(");
      }
      else {
        return compiler.getTypeRegistry().getNativeType(JSTypeNative.UNKNOWN_TYPE);
      }
    }
    return type.restrictByNotNullOrUndefined();
  }
  private Name getNamedContainingFunction() {
    Name containingFn = null;
    int pos;
    for(pos = currentFunctionStack.size() - 1; pos >= 0; pos = pos - 1) {
      Name cf = currentFunctionStack.get(pos);
      if(cf != graph.UNKNOWN) {
        containingFn = cf;
        break ;
      }
    }
    Preconditions.checkNotNull(containingFn);
    return containingFn;
  }
  NameReferenceGraph getNameReferenceGraph() {
    return this.graph;
  }
  private void connectUnknowns() {
    for (GraphNode<Name, Reference> node : graph.getNodes()) {
      Name name = node.getValue();
      String propName = name.getPropertyName();
      if(propName == null) {
        continue ;
      }
      Collection<NameUse> uses = unknownNameUse.get(propName);
      if(uses != null) {
        for (NameUse use : uses) {
          graph.connect(use.name, use.reference, name);
        }
      }
    }
  }
  private void popContainingFunction() {
    currentFunctionStack.remove(currentFunctionStack.size() - 1);
  }
  @Override() public void process(Node externs, Node root) {
    ScopeCreator scopeCreator = compiler.getTypedScopeCreator();
    if(scopeCreator == null) {
      scopeCreator = new MemoizedScopeCreator(new TypedScopeCreator(compiler));
    }
    NodeTraversal externsTraversal = new NodeTraversal(compiler, new Traversal(true), scopeCreator);
    NodeTraversal codeTraversal = new NodeTraversal(compiler, new Traversal(false), scopeCreator);
    Scope topScope = compiler.getTopScope();
    if(topScope != null) {
      externsTraversal.traverseWithScope(externs, topScope);
      codeTraversal.traverseWithScope(root, topScope);
    }
    else {
      externsTraversal.traverse(externs);
      codeTraversal.traverse(root);
    }
    connectUnknowns();
  }
  private void pushContainingFunction(Name functionNode) {
    currentFunctionStack.add(functionNode);
  }
  
  private static class NameUse  {
    final private Name name;
    final private Reference reference;
    private NameUse(Name name, Reference reference) {
      super();
      this.name = name;
      this.reference = reference;
    }
  }
  
  private class Traversal implements ScopedCallback  {
    final boolean isExtern;
    private Traversal(boolean isExtern) {
      super();
      this.isExtern = isExtern;
      pushContainingFunction(graph.MAIN);
    }
    private Name recordClassConstructorOrInterface(String name, FunctionType type, @Nullable() Node n, @Nullable() Node parent, @Nullable() Node gParent, @Nullable() Node rhs) {
      Preconditions.checkArgument(type.isConstructor() || type.isInterface());
      Name symbol = graph.defineNameIfNotExists(name, isExtern);
      if(rhs != null) {
        symbol.setType(getType(rhs));
        if(n.isAssign()) {
          symbol.addAssignmentDeclaration(n);
        }
        else {
          symbol.addFunctionDeclaration(n);
        }
      }
      ObjectType prototype = type.getPrototype();
      for (String prop : prototype.getOwnPropertyNames()) {
        graph.defineNameIfNotExists(name + ".prototype." + prop, isExtern);
      }
      return symbol;
    }
    private Name recordPrototypePropDefinition(NodeTraversal t, Node qName, JSType type, @Nullable() Node assign, @Nullable() Node parent, @Nullable() Node gParent) {
      JSType constructor = getType(NodeUtil.getPrototypeClassName(qName));
      FunctionType classType = null;
      String className = null;
      if(constructor != null && constructor.isConstructor()) {
        classType = constructor.toMaybeFunctionType();
        className = classType.getReferenceName();
      }
      else {
        classType = compiler.getTypeRegistry().getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
        className = NodeUtil.getPrototypeClassName(qName).getQualifiedName();
      }
      recordClassConstructorOrInterface(className, classType, null, null, null, null);
      String qNameStr = className + ".prototype." + NodeUtil.getPrototypePropertyName(qName);
      Name prototypeProp = graph.defineNameIfNotExists(qNameStr, isExtern);
      Preconditions.checkNotNull(prototypeProp, "%s should be in the name graph as a node.", qNameStr);
      if(assign != null) {
        prototypeProp.addAssignmentDeclaration(assign);
      }
      prototypeProp.setType(type);
      return prototypeProp;
    }
    private Name recordStaticNameDefinition(NodeTraversal t, String name, JSType type, Node n, Node parent, Node gParent, Node rValue) {
      if(getNamedContainingFunction() != graph.MAIN) {
      }
      if(type.isConstructor()) {
        return recordClassConstructorOrInterface(name, type.toMaybeFunctionType(), n, parent, parent.getParent(), rValue);
      }
      else {
        Name symbol = graph.defineNameIfNotExists(name, isExtern);
        symbol.setType(type);
        if(n.isAssign()) {
          symbol.addAssignmentDeclaration(n);
        }
        else {
          symbol.addFunctionDeclaration(n);
        }
        return symbol;
      }
    }
    private Reference recordStaticNameUse(NodeTraversal t, Node n, Node parent) {
      if(isExtern) {
        return null;
      }
      else {
        Reference reference = new Reference(n, parent);
        Name name = graph.defineNameIfNotExists(n.getQualifiedName(), isExtern);
        name.setType(getType(n));
        graph.connect(getNamedContainingFunction(), reference, name);
        return reference;
      }
    }
    private boolean containsName(Node n) {
      return NodeUtil.containsType(n, Token.NAME) || NodeUtil.containsType(n, Token.GETELEM) || NodeUtil.containsType(n, Token.GETPROP);
    }
    private boolean isLocalNameReference(NodeTraversal t, Node n) {
      if(n.isName()) {
        Var v = t.getScope().getVar(n.getString());
        return v != null && v.isLocal();
      }
      return false;
    }
    private boolean isPrototypeNameReference(Node n) {
      if(!n.isGetProp()) {
        return false;
      }
      JSType type = getType(n.getFirstChild());
      if(type.isUnknownType() || type.isUnionType()) {
        return false;
      }
      return (type.isInstanceType() || type.autoboxesTo() != null);
    }
    private boolean isStaticNameReference(Node n, Scope scope) {
      Preconditions.checkArgument(n.isName() || n.isGetProp());
      if(n.isName()) {
        return true;
      }
      String qName = n.getQualifiedName();
      if(qName == null) {
        return false;
      }
      return scope.isDeclared(qName, true);
    }
    @Override() public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      return true;
    }
    private void defineAndAlias(String name) {
      graph.defineNameIfNotExists(name, isExtern).setAliased(true);
    }
    @Override() public void enterScope(NodeTraversal t) {
      Node root = t.getScopeRoot();
      Node parent = root.getParent();
      if(!t.inGlobalScope()) {
        String name = NodeUtil.getFunctionName(root);
        if(name == null) {
          pushContainingFunction(graph.UNKNOWN);
          return ;
        }
        JSType type = getType(root);
        Node gParent = parent.getParent();
        Node ggParent = gParent.getParent();
        if(parent.isAssign() && NodeUtil.isPrototypeProperty(parent.getFirstChild())) {
          pushContainingFunction(recordPrototypePropDefinition(t, parent.getFirstChild(), type, parent, gParent, ggParent));
        }
        else {
          pushContainingFunction(recordStaticNameDefinition(t, name, type, root, parent, gParent, root.getLastChild()));
        }
      }
    }
    @Override() public void exitScope(NodeTraversal t) {
      if(!t.inGlobalScope()) {
        popContainingFunction();
      }
    }
    private void maybeAliasNamesOnAssign(Node lhs, Node rhs) {
      if((lhs.isName() || lhs.isGetProp()) && containsName(rhs) && !rhs.isFunction() && !rhs.isNew()) {
        safeAlias(lhs);
        safeAlias(rhs);
      }
    }
    private void maybeRecordExport(Node call) {
      Preconditions.checkArgument(call.isCall());
      Node getProp = call.getFirstChild();
      if(!getProp.isGetProp()) {
        return ;
      }
      String propQName = getProp.getQualifiedName();
      if(propQName == null) {
        return ;
      }
      if(propQName.endsWith(".call") || propQName.endsWith(".apply")) {
        graph.defineNameIfNotExists(getProp.getFirstChild().getQualifiedName(), isExtern).markExposedToCallOrApply();
      }
      if(!"goog.exportSymbol".equals(propQName)) {
        return ;
      }
      Node symbol = getProp.getNext();
      if(!symbol.isString()) {
        return ;
      }
      Node obj = symbol.getNext();
      String qName = obj.getQualifiedName();
      if(qName == null || obj.getNext() != null) {
        return ;
      }
      graph.defineNameIfNotExists(qName, false).markExported();
    }
    private void recordPrototypePropUse(NodeTraversal t, Node n, Node parent) {
      Preconditions.checkArgument(n.isGetProp());
      Node instance = n.getFirstChild();
      JSType instanceType = getType(instance);
      JSType boxedType = instanceType.autoboxesTo();
      instanceType = boxedType != null ? boxedType : instanceType;
      ObjectType objType = instanceType.toObjectType();
      Preconditions.checkState(objType != null);
      if(!isExtern) {
        Reference ref = new Reference(n, parent);
        FunctionType constructor = objType.getConstructor();
        if(constructor != null) {
          String propName = n.getLastChild().getString();
          if(!constructor.getPrototype().hasOwnProperty(propName)) {
            recordSuperClassPrototypePropUse(constructor, propName, ref);
          }
          recordSubclassPrototypePropUse(constructor, propName, ref);
        }
        else {
          recordUnknownUse(t, n, parent);
        }
      }
    }
    private void recordSubclassPrototypePropUse(FunctionType classType, String prop, Reference ref) {
      if(classType.getPrototype().hasOwnProperty(prop)) {
        graph.connect(getNamedContainingFunction(), ref, graph.defineNameIfNotExists(classType.getReferenceName() + ".prototype." + prop, false));
      }
      java.util.List<FunctionType> var_1273 = classType.getSubTypes();
      if(var_1273 != null) {
        for (FunctionType subclass : classType.getSubTypes()) {
          recordSubclassPrototypePropUse(subclass, prop, ref);
        }
      }
    }
    private void recordSuperClassPrototypePropUse(FunctionType classType, String prop, Reference ref) {
      FunctionType superClass = classType.getSuperClassConstructor();
      while(superClass != null){
        if(superClass.getPrototype().hasOwnProperty(prop)) {
          graph.connect(getNamedContainingFunction(), ref, graph.defineNameIfNotExists(superClass.getReferenceName() + ".prototype." + prop, false));
          return ;
        }
        else {
          superClass = superClass.getSuperClassConstructor();
        }
      }
    }
    private void recordUnknownUse(NodeTraversal t, Node n, Node parent) {
      if(isExtern) {
        return ;
      }
      else {
        Preconditions.checkArgument(n.isGetProp());
        Reference ref = new Reference(n, parent);
        ref.setUnknown(true);
        unknownNameUse.put(n.getLastChild().getString(), new NameUse(getNamedContainingFunction(), ref));
      }
    }
    private void safeAlias(Node n) {
      if(n.isName() || n.isGetProp()) {
        String name = n.getQualifiedName();
        if(name != null) {
          defineAndAlias(name);
          return ;
        }
      }
      if(n.isGetProp()) {
        defineAndAlias(n.getLastChild().getString());
      }
      else 
        if(n.isAssign()) {
          safeAlias(n.getFirstChild());
        }
        else 
          if(n.hasChildren()) {
            Node cur = n.getFirstChild();
            do {
              safeAlias(cur);
            }while((cur = cur.getNext()) != null);
          }
          else {
          }
    }
    @SuppressWarnings(value = {"fallthrough", }) @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.NAME:
        case Token.GETPROP:
        if(parent.isGetProp()) {
          return ;
        }
        else 
          if(parent.isFunction()) {
            return ;
          }
          else 
            if(parent.isAssign()) {
              return ;
            }
        if(isLocalNameReference(t, n)) {
          return ;
        }
        if(isPrototypeNameReference(n)) {
          recordPrototypePropUse(t, n, parent);
        }
        else 
          if(isStaticNameReference(n, t.getScope())) {
            recordStaticNameUse(t, n, parent);
          }
          else {
            recordUnknownUse(t, n, parent);
          }
        break ;
        case Token.ASSIGN:
        Node lhs = n.getFirstChild();
        Node rhs = n.getLastChild();
        if(rhs.isFunction()) {
          return ;
        }
        if(lhs.isName() || lhs.isGetProp() || rhs.isGetProp()) {
          if(NodeUtil.isPrototypeProperty(lhs)) {
            Name name = recordPrototypePropDefinition(t, lhs, getType(rhs), n, parent, parent.getParent());
            name.setAliased(true);
          }
        }
        maybeAliasNamesOnAssign(lhs, rhs);
        break ;
        case Token.VAR:
        Node varName = n.getFirstChild();
        Node assignedValue = varName.getFirstChild();
        if(assignedValue == null) {
          return ;
        }
        maybeAliasNamesOnAssign(varName, assignedValue);
        break ;
        case Token.CALL:
        Node param = n.getFirstChild();
        while((param = param.getNext()) != null){
          if(param.isName() || param.isGetProp()) {
            safeAlias(param);
          }
        }
        maybeRecordExport(n);
        break ;
      }
    }
  }
}