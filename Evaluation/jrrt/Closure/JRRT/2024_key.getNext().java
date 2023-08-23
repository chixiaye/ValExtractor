package com.google.javascript.jscomp;
import javax.annotation.Nullable;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.CodingConvention.SubclassRelationship;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Set;

class StripCode implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private Set<String> stripTypes;
  final private Set<String> stripNameSuffixes;
  final private Set<String> stripTypePrefixes;
  final private Set<String> stripNamePrefixes;
  final private Set<Scope.Var> varsToRemove;
  final static DiagnosticType STRIP_TYPE_INHERIT_ERROR = DiagnosticType.error("JSC_STRIP_TYPE_INHERIT_ERROR", "Non-strip type {0} cannot inherit from strip type {1}");
  final static DiagnosticType STRIP_ASSIGNMENT_ERROR = DiagnosticType.error("JSC_STRIP_ASSIGNMENT_ERROR", "Unable to strip assignment to {0}");
  StripCode(AbstractCompiler compiler, Set<String> stripTypes, Set<String> stripNameSuffixes, Set<String> stripTypePrefixes, Set<String> stripNamePrefixes) {
    super();
    this.compiler = compiler;
    this.stripTypes = Sets.newHashSet(stripTypes);
    this.stripNameSuffixes = Sets.newHashSet(stripNameSuffixes);
    this.stripTypePrefixes = Sets.newHashSet(stripTypePrefixes);
    this.stripNamePrefixes = Sets.newHashSet(stripNamePrefixes);
    this.varsToRemove = Sets.newHashSet();
  }
  public void enableTweakStripping() {
    stripTypes.add("goog.tweak");
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, new Strip());
  }
  
  private class Strip extends AbstractPostOrderCallback  {
    private boolean actsOnStripType(NodeTraversal t, Node callNode) {
      SubclassRelationship classes = compiler.getCodingConvention().getClassesDefinedByCall(callNode);
      if(classes != null) {
        if(qualifiedNameBeginsWithStripType(classes.subclassName)) {
          return true;
        }
        if(qualifiedNameBeginsWithStripType(classes.superclassName)) {
          t.report(callNode, STRIP_TYPE_INHERIT_ERROR, classes.subclassName, classes.superclassName);
        }
      }
      return false;
    }
    boolean isCallWhoseReturnValueShouldBeStripped(@Nullable() Node n) {
      return n != null && (n.isCall() || n.isNew()) && n.hasChildren() && (qualifiedNameBeginsWithStripType(n.getFirstChild()) || nameEndsWithFieldNameToStrip(n.getFirstChild()));
    }
    boolean isMethodOrCtorCallThatTriggersRemoval(NodeTraversal t, Node n, Node parent) {
      Node function = n.getFirstChild();
      if(function == null || !function.isGetProp()) {
        return false;
      }
      if(parent != null && parent.isName()) {
        Node gramps = parent.getParent();
        if(gramps != null && gramps.isVar()) {
          return false;
        }
      }
      Node callee = function.getFirstChild();
      return nameEndsWithFieldNameToStrip(callee) || nameEndsWithFieldNameToStrip(function) || qualifiedNameBeginsWithStripType(function) || actsOnStripType(t, n);
    }
    boolean isReferenceToRemovedVar(NodeTraversal t, Node n) {
      String name = n.getString();
      Scope scope = t.getScope();
      Scope.Var var = scope.getVar(name);
      return varsToRemove.contains(var);
    }
    boolean isStripName(String name) {
      if(stripNameSuffixes.contains(name) || stripNamePrefixes.contains(name)) {
        return true;
      }
      if((name.length() == 0) || Character.isUpperCase(name.charAt(0))) {
        return false;
      }
      String lcName = name.toLowerCase();
      for (String stripName : stripNamePrefixes) {
        if(lcName.startsWith(stripName.toLowerCase())) {
          return true;
        }
      }
      for (String stripName : stripNameSuffixes) {
        if(lcName.endsWith(stripName.toLowerCase())) {
          return true;
        }
      }
      return false;
    }
    boolean nameEndsWithFieldNameToStrip(@Nullable() Node n) {
      if(n != null && n.isGetProp()) {
        Node propNode = n.getLastChild();
        return propNode != null && propNode.isString() && isStripName(propNode.getString());
      }
      return false;
    }
    boolean qualifiedNameBeginsWithStripType(Node n) {
      String name = n.getQualifiedName();
      return qualifiedNameBeginsWithStripType(name);
    }
    boolean qualifiedNameBeginsWithStripType(String name) {
      if(name != null) {
        for (String type : stripTypes) {
          if(name.equals(type) || name.startsWith(type + ".")) {
            return true;
          }
        }
        for (String type : stripTypePrefixes) {
          if(name.startsWith(type)) {
            return true;
          }
        }
      }
      return false;
    }
    void eliminateKeysWithStripNamesFromObjLit(NodeTraversal t, Node n) {
      Node key = n.getFirstChild();
      while(key != null){
        if(isStripName(key.getString())) {
          Node value = key.getFirstChild();
          Node var_2024 = key.getNext();
          Node next = var_2024;
          n.removeChild(key);
          key = next;
          compiler.reportCodeChange();
        }
        else {
          key = key.getNext();
        }
      }
    }
    void maybeEliminateAssignmentByLvalueName(NodeTraversal t, Node n, Node parent) {
      Node lvalue = n.getFirstChild();
      if(nameEndsWithFieldNameToStrip(lvalue) || qualifiedNameBeginsWithStripType(lvalue)) {
        if(parent.isExprResult()) {
          Node gramps = parent.getParent();
          replaceWithEmpty(parent, gramps);
          compiler.reportCodeChange();
        }
        else {
          t.report(n, STRIP_ASSIGNMENT_ERROR, lvalue.getQualifiedName());
        }
      }
    }
    void maybeEliminateExpressionByName(NodeTraversal t, Node n, Node parent) {
      Node expression = n.getFirstChild();
      if(nameEndsWithFieldNameToStrip(expression) || qualifiedNameBeginsWithStripType(expression)) {
        if(parent.isExprResult()) {
          Node gramps = parent.getParent();
          replaceWithEmpty(parent, gramps);
        }
        else {
          replaceWithEmpty(n, parent);
        }
        compiler.reportCodeChange();
      }
    }
    void maybeRemoveCall(NodeTraversal t, Node n, Node parent) {
      if(isMethodOrCtorCallThatTriggersRemoval(t, n, parent)) {
        replaceHighestNestedCallWithNull(n, parent);
      }
    }
    void maybeRemoveReferenceToRemovedVariable(NodeTraversal t, Node n, Node parent) {
      switch (parent.getType()){
        case Token.VAR:
        break ;
        case Token.GETPROP:
        case Token.GETELEM:
        if(parent.getFirstChild() == n && isReferenceToRemovedVar(t, n)) {
          replaceHighestNestedCallWithNull(parent, parent.getParent());
        }
        break ;
        case Token.ASSIGN:
        case Token.ASSIGN_BITOR:
        case Token.ASSIGN_BITXOR:
        case Token.ASSIGN_BITAND:
        case Token.ASSIGN_LSH:
        case Token.ASSIGN_RSH:
        case Token.ASSIGN_URSH:
        case Token.ASSIGN_ADD:
        case Token.ASSIGN_SUB:
        case Token.ASSIGN_MUL:
        case Token.ASSIGN_DIV:
        case Token.ASSIGN_MOD:
        if(isReferenceToRemovedVar(t, n)) {
          if(parent.getFirstChild() == n) {
            Node gramps = parent.getParent();
            if(gramps.isExprResult()) {
              Node greatGramps = gramps.getParent();
              replaceWithEmpty(gramps, greatGramps);
              compiler.reportCodeChange();
            }
            else {
              Node rvalue = n.getNext();
              parent.removeChild(rvalue);
              gramps.replaceChild(parent, rvalue);
              compiler.reportCodeChange();
            }
          }
          else {
            replaceWithNull(n, parent);
            compiler.reportCodeChange();
          }
        }
        break ;
        default:
        if(isReferenceToRemovedVar(t, n)) {
          replaceWithNull(n, parent);
          compiler.reportCodeChange();
        }
        break ;
      }
    }
    void removeVarDeclarationsByNameOrRvalue(NodeTraversal t, Node n, Node parent) {
      for(com.google.javascript.rhino.Node nameNode = n.getFirstChild(); nameNode != null; nameNode = nameNode.getNext()) {
        String name = nameNode.getString();
        if(isStripName(name) || isCallWhoseReturnValueShouldBeStripped(nameNode.getFirstChild())) {
          Scope scope = t.getScope();
          varsToRemove.add(scope.getVar(name));
          n.removeChild(nameNode);
          compiler.reportCodeChange();
        }
      }
      if(!n.hasChildren()) {
        replaceWithEmpty(n, parent);
        compiler.reportCodeChange();
      }
    }
    void replaceHighestNestedCallWithNull(Node node, Node parent) {
      Node ancestor = parent;
      Node ancestorChild = node;
      while(true){
        if(ancestor.getFirstChild() != ancestorChild) {
          replaceWithNull(ancestorChild, ancestor);
          break ;
        }
        if(ancestor.isExprResult()) {
          Node ancParent = ancestor.getParent();
          replaceWithEmpty(ancestor, ancParent);
          break ;
        }
        int type = ancestor.getType();
        if(type != Token.GETPROP && type != Token.GETELEM && type != Token.CALL) {
          replaceWithNull(ancestorChild, ancestor);
          break ;
        }
        ancestorChild = ancestor;
        ancestor = ancestor.getParent();
      }
      compiler.reportCodeChange();
    }
    void replaceWithEmpty(Node n, Node parent) {
      NodeUtil.removeChild(parent, n);
    }
    void replaceWithNull(Node n, Node parent) {
      parent.replaceChild(n, IR.nullNode());
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.VAR:
        removeVarDeclarationsByNameOrRvalue(t, n, parent);
        break ;
        case Token.NAME:
        maybeRemoveReferenceToRemovedVariable(t, n, parent);
        break ;
        case Token.ASSIGN:
        case Token.ASSIGN_BITOR:
        case Token.ASSIGN_BITXOR:
        case Token.ASSIGN_BITAND:
        case Token.ASSIGN_LSH:
        case Token.ASSIGN_RSH:
        case Token.ASSIGN_URSH:
        case Token.ASSIGN_ADD:
        case Token.ASSIGN_SUB:
        case Token.ASSIGN_MUL:
        case Token.ASSIGN_DIV:
        case Token.ASSIGN_MOD:
        maybeEliminateAssignmentByLvalueName(t, n, parent);
        break ;
        case Token.CALL:
        case Token.NEW:
        maybeRemoveCall(t, n, parent);
        break ;
        case Token.OBJECTLIT:
        eliminateKeysWithStripNamesFromObjLit(t, n);
        break ;
        case Token.EXPR_RESULT:
        maybeEliminateExpressionByName(t, n, parent);
        break ;
      }
    }
  }
}