package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.javascript.jscomp.CodingConvention.SubclassRelationship;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

class CrossModuleCodeMotion extends AbstractPostOrderCallback implements CompilerPass  {
  final private static Logger logger = Logger.getLogger(CrossModuleCodeMotion.class.getName());
  final private AbstractCompiler compiler;
  final private JSModuleGraph graph;
  final private Map<JSModule, Node> moduleVarParentMap = new HashMap<JSModule, Node>();
  final private Map<Scope.Var, NamedInfo> namedInfo = new LinkedHashMap<Var, NamedInfo>();
  CrossModuleCodeMotion(AbstractCompiler compiler, JSModuleGraph graph) {
    super();
    this.compiler = compiler;
    this.graph = graph;
  }
  private NamedInfo getNamedInfo(Var v) {
    NamedInfo info = namedInfo.get(v);
    if(info == null) {
      info = new NamedInfo();
      namedInfo.put(v, info);
    }
    return info;
  }
  private boolean canMoveValue(Node n) {
    if(n == null || NodeUtil.isLiteralValue(n, true) || n.isFunction()) {
      return true;
    }
    else 
      if(n.isCall()) {
        Node functionName = n.getFirstChild();
        return functionName.isName() && (functionName.getString().equals(CrossModuleMethodMotion.STUB_METHOD_NAME) || functionName.getString().equals(CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
      }
      else 
        if(n.isArrayLit() || n.isObjectLit()) {
          boolean isObjectLit = n.isObjectLit();
          for(com.google.javascript.rhino.Node child = n.getFirstChild(); child != null; child = child.getNext()) {
            if(!canMoveValue(isObjectLit ? child.getFirstChild() : child)) {
              return false;
            }
          }
          return true;
        }
    return false;
  }
  private boolean hasConditionalAncestor(Node n) {
    for (Node ancestor : n.getAncestors()) {
      switch (ancestor.getType()){
        case Token.DO:
        case Token.FOR:
        case Token.HOOK:
        case Token.IF:
        case Token.SWITCH:
        case Token.WHILE:
        case Token.FUNCTION:
        return true;
      }
    }
    return false;
  }
  private boolean maybeProcessDeclaration(NodeTraversal t, Node name, Node parent, NamedInfo info) {
    Node gramps = parent.getParent();
    switch (parent.getType()){
      case Token.VAR:
      if(canMoveValue(name.getFirstChild())) {
        return info.addDeclaration(new Declaration(t.getModule(), name, parent, gramps));
      }
      return false;
      case Token.FUNCTION:
      if(NodeUtil.isFunctionDeclaration(parent)) {
        return info.addDeclaration(new Declaration(t.getModule(), name, parent, gramps));
      }
      return false;
      case Token.ASSIGN:
      case Token.GETPROP:
      Node child = name;
      for (Node current : name.getAncestors()) {
        if(current.isGetProp()) {
        }
        else 
          if(current.isAssign() && current.getFirstChild() == child) {
            Node currentParent = current.getParent();
            if(currentParent.isExprResult() && canMoveValue(current.getLastChild())) {
              return info.addDeclaration(new Declaration(t.getModule(), current, currentParent, currentParent.getParent()));
            }
          }
          else {
            return false;
          }
        child = current;
      }
      return false;
      case Token.CALL:
      if(NodeUtil.isExprCall(gramps)) {
        SubclassRelationship relationship = compiler.getCodingConvention().getClassesDefinedByCall(parent);
        if(relationship != null && name.getString().equals(relationship.subclassName)) {
          return info.addDeclaration(new Declaration(t.getModule(), parent, gramps, gramps.getParent()));
        }
      }
      return false;
      default:
      return false;
    }
  }
  private void moveCode() {
    for (NamedInfo info : namedInfo.values()) {
      JSModule deepestDependency = info.deepestModule;
      if(info.allowMove && deepestDependency != null) {
        Iterator<Declaration> it = info.declarationIterator();
        JSModuleGraph moduleGraph = compiler.getModuleGraph();
        while(it.hasNext()){
          Declaration decl = it.next();
          if(decl.module != null && moduleGraph.dependsOn(deepestDependency, decl.module)) {
            Node destParent = moduleVarParentMap.get(deepestDependency);
            if(destParent == null) {
              destParent = compiler.getNodeForCodeInsertion(deepestDependency);
              moduleVarParentMap.put(deepestDependency, destParent);
            }
            Node declParent = decl.node.getParent();
            Preconditions.checkState(!declParent.isVar() || declParent.hasOneChild(), "AST not normalized.");
            declParent.detachFromParent();
            destParent.addChildToFront(declParent);
            compiler.reportCodeChange();
          }
        }
      }
    }
  }
  @Override() public void process(Node externs, Node root) {
    logger.fine("Moving functions + variable into deeper modules");
    if(graph != null && graph.getModuleCount() > 1) {
      NodeTraversal.traverse(compiler, root, this);
      moveCode();
    }
  }
  private void processReference(NodeTraversal t, NamedInfo info, String name) {
    boolean recursive = false;
    Scope var_583 = t.getScope();
    Node rootNode = var_583.getRootNode();
    if(rootNode.isFunction()) {
      String scopeFuncName = rootNode.getFirstChild().getString();
      Node scopeFuncParent = rootNode.getParent();
      if(scopeFuncName.equals(name)) {
        recursive = true;
      }
      else 
        if(scopeFuncParent.isName() && scopeFuncParent.getString().equals(name)) {
          recursive = true;
        }
        else {
          for(com.google.javascript.jscomp.Scope s = t.getScope(); s.getParent() != null; s = s.getParent()) {
            Node curRoot = s.getRootNode();
            if(curRoot.getParent().isAssign()) {
              Node owner = curRoot.getParent().getFirstChild();
              while(owner.isGetProp()){
                owner = owner.getFirstChild();
              }
              if(owner.isName() && owner.getString().equals(name)) {
                recursive = true;
                break ;
              }
            }
          }
        }
    }
    if(!recursive) {
      info.addUsedModule(t.getModule());
    }
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    if(!n.isName()) {
      return ;
    }
    String name = n.getString();
    if(name.isEmpty() || compiler.getCodingConvention().isExported(name)) {
      return ;
    }
    Var v = t.getScope().getVar(name);
    if(v == null || !v.isGlobal()) {
      return ;
    }
    NamedInfo info = getNamedInfo(v);
    if(info.allowMove) {
      if(maybeProcessDeclaration(t, n, parent, info)) {
        if(hasConditionalAncestor(parent.getParent())) {
          info.allowMove = false;
        }
      }
      else {
        processReference(t, info, name);
      }
    }
  }
  
  private class Declaration  {
    final JSModule module;
    final Node node;
    Declaration(JSModule module, Node node, Node parent, Node gramps) {
      super();
      this.module = module;
      this.node = node;
    }
  }
  
  private class NamedInfo  {
    boolean allowMove = true;
    private JSModule deepestModule = null;
    private JSModule declModule = null;
    final private Deque<Declaration> declarations = new ArrayDeque<Declaration>();
    Iterator<Declaration> declarationIterator() {
      return declarations.iterator();
    }
    boolean addDeclaration(Declaration d) {
      if(declModule != null && d.module != declModule) {
        return false;
      }
      declarations.push(d);
      declModule = d.module;
      return true;
    }
    void addUsedModule(JSModule m) {
      if(!allowMove) {
        return ;
      }
      if(deepestModule == null) {
        deepestModule = m;
      }
      else {
        deepestModule = graph.getDeepestCommonDependencyInclusive(m, deepestModule);
      }
    }
  }
}