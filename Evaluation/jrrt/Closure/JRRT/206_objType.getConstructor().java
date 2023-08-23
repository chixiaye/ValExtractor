package com.google.javascript.jscomp;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import javax.annotation.Nullable;

class RuntimeTypeCheck implements CompilerPass  {
  final private static Comparator<JSType> ALPHA = new Comparator<JSType>() {
      @Override() public int compare(JSType t1, JSType t2) {
        return getName(t1).compareTo(getName(t2));
      }
      private String getName(JSType type) {
        if(type.isInstanceType()) {
          return ((ObjectType)type).getReferenceName();
        }
        else 
          if(type.isNullType() || type.isBooleanValueType() || type.isNumberValueType() || type.isStringValueType() || type.isVoidType()) {
            return type.toString();
          }
          else {
            return "";
          }
      }
  };
  final private AbstractCompiler compiler;
  final private String logFunction;
  RuntimeTypeCheck(AbstractCompiler compiler, @Nullable() String logFunction) {
    super();
    this.compiler = compiler;
    this.logFunction = logFunction;
  }
  private Node jsCode(String prop) {
    return NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), "$jscomp.typecheck." + prop);
  }
  private void addBoilerplateCode() {
    Node newNode = compiler.ensureLibraryInjected("runtime_type_check");
    if(newNode != null && logFunction != null) {
      Node logOverride = IR.exprResult(IR.assign(NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), "$jscomp.typecheck.log"), NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), logFunction)));
      newNode.getParent().addChildAfter(logOverride, newNode);
      compiler.reportCodeChange();
    }
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, new AddMarkers(compiler));
    NodeTraversal.traverse(compiler, root, new AddChecks());
    addBoilerplateCode();
  }
  
  private class AddChecks extends NodeTraversal.AbstractPostOrderCallback  {
    private AddChecks() {
      super();
    }
    private Node createCheckTypeCallNode(JSType type, Node expr) {
      Node arrayNode = IR.arraylit();
      Collection<JSType> alternates;
      if(type.isUnionType()) {
        alternates = Sets.newTreeSet(ALPHA);
        Iterables.addAll(alternates, type.toMaybeUnionType().getAlternates());
      }
      else {
        alternates = ImmutableList.of(type);
      }
      for (JSType alternate : alternates) {
        Node checkerNode = createCheckerNode(alternate);
        if(checkerNode == null) {
          return null;
        }
        arrayNode.addChildToBack(checkerNode);
      }
      return IR.call(jsCode("checkType"), expr, arrayNode);
    }
    private Node createCheckerNode(JSType type) {
      if(type.isNullType()) {
        return jsCode("nullChecker");
      }
      else 
        if(type.isBooleanValueType() || type.isNumberValueType() || type.isStringValueType() || type.isVoidType()) {
          return IR.call(jsCode("valueChecker"), IR.string(type.toString()));
        }
        else 
          if(type.isInstanceType()) {
            ObjectType objType = (ObjectType)type;
            String refName = objType.getReferenceName();
            FunctionType var_206 = objType.getConstructor();
            StaticSourceFile sourceFile = NodeUtil.getSourceFile(var_206.getSource());
            if(sourceFile == null || sourceFile.isExtern()) {
              return IR.call(jsCode("externClassChecker"), IR.string(refName));
            }
            return IR.call(jsCode(objType.getConstructor().isInterface() ? "interfaceChecker" : "classChecker"), IR.string(refName));
          }
          else {
            return null;
          }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isFunction()) {
        visitFunction(t, n);
      }
      else 
        if(n.isReturn()) {
          visitReturn(t, n);
        }
    }
    private void visitFunction(NodeTraversal t, Node n) {
      FunctionType funType = JSType.toMaybeFunctionType(n.getJSType());
      Node block = n.getLastChild();
      Node paramName = NodeUtil.getFunctionParameters(n).getFirstChild();
      Node insertionPoint = null;
      for(com.google.javascript.rhino.Node next = block.getFirstChild(); next != null && NodeUtil.isFunctionDeclaration(next); next = next.getNext()) {
        insertionPoint = next;
      }
      for (Node paramType : funType.getParameters()) {
        if(paramName == null) {
          return ;
        }
        Node checkNode = createCheckTypeCallNode(paramType.getJSType(), paramName.cloneTree());
        if(checkNode == null) {
          paramName = paramName.getNext();
          continue ;
        }
        checkNode = IR.exprResult(checkNode);
        if(insertionPoint == null) {
          block.addChildToFront(checkNode);
        }
        else {
          block.addChildAfter(checkNode, insertionPoint);
        }
        compiler.reportCodeChange();
        paramName = paramName.getNext();
        insertionPoint = checkNode;
      }
    }
    private void visitReturn(NodeTraversal t, Node n) {
      Node function = t.getEnclosingFunction();
      FunctionType funType = function.getJSType().toMaybeFunctionType();
      Node retValue = n.getFirstChild();
      if(retValue == null) {
        return ;
      }
      Node checkNode = createCheckTypeCallNode(funType.getReturnType(), retValue.cloneTree());
      if(checkNode == null) {
        return ;
      }
      n.replaceChild(retValue, checkNode);
      compiler.reportCodeChange();
    }
  }
  
  private static class AddMarkers extends NodeTraversal.AbstractPostOrderCallback  {
    final private AbstractCompiler compiler;
    private AddMarkers(AbstractCompiler compiler) {
      super();
      this.compiler = compiler;
    }
    private Node addMarker(FunctionType funType, Node nodeToInsertAfter, @Nullable() ObjectType interfaceType) {
      if(funType.getSource() == null) {
        return nodeToInsertAfter;
      }
      String className = NodeUtil.getFunctionName(funType.getSource());
      if(className == null) {
        return nodeToInsertAfter;
      }
      Node classNode = NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), className);
      Node marker = IR.string(interfaceType == null ? "instance_of__" + className : "implements__" + interfaceType.getReferenceName());
      Node assign = IR.exprResult(IR.assign(IR.getelem(IR.getprop(classNode, IR.string("prototype")), marker), IR.trueNode()));
      nodeToInsertAfter.getParent().addChildAfter(assign, nodeToInsertAfter);
      compiler.reportCodeChange();
      nodeToInsertAfter = assign;
      return nodeToInsertAfter;
    }
    private Node findEnclosingConstructorDeclaration(Node n) {
      while(!n.getParent().isScript() && !n.getParent().isBlock()){
        n = n.getParent();
      }
      return n;
    }
    private Node findNodeToInsertAfter(Node n) {
      Node nodeToInsertAfter = findEnclosingConstructorDeclaration(n);
      Node next = nodeToInsertAfter.getNext();
      while(next != null && isClassDefiningCall(next)){
        nodeToInsertAfter = next;
        next = nodeToInsertAfter.getNext();
      }
      return nodeToInsertAfter;
    }
    private boolean isClassDefiningCall(Node next) {
      return NodeUtil.isExprCall(next) && compiler.getCodingConvention().getClassesDefinedByCall(next.getFirstChild()) != null;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isFunction()) {
        visitFunction(t, n);
      }
    }
    private void visitFunction(NodeTraversal t, Node n) {
      FunctionType funType = n.getJSType().toMaybeFunctionType();
      if(funType != null && !funType.isConstructor()) {
        return ;
      }
      Node nodeToInsertAfter = findNodeToInsertAfter(n);
      nodeToInsertAfter = addMarker(funType, nodeToInsertAfter, null);
      TreeSet<ObjectType> stuff = Sets.newTreeSet(ALPHA);
      Iterables.addAll(stuff, funType.getAllImplementedInterfaces());
      for (ObjectType interfaceType : stuff) {
        nodeToInsertAfter = addMarker(funType, nodeToInsertAfter, interfaceType);
      }
    }
  }
}