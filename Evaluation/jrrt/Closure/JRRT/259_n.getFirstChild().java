package com.google.javascript.jscomp;
import static com.google.javascript.jscomp.FunctionArgumentInjector.THIS_MARKER;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.MakeDeclaredNamesUnique.InlineRenamer;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class FunctionToBlockMutator  {
  private AbstractCompiler compiler;
  private Supplier<String> safeNameIdSupplier;
  FunctionToBlockMutator(AbstractCompiler compiler, Supplier<String> safeNameIdSupplier) {
    super();
    this.compiler = compiler;
    this.safeNameIdSupplier = safeNameIdSupplier;
  }
  private Node aliasAndInlineArguments(Node fnTemplateRoot, LinkedHashMap<String, Node> argMap, Set<String> namesToAlias) {
    if(namesToAlias == null || namesToAlias.isEmpty()) {
      Node result = FunctionArgumentInjector.inject(compiler, fnTemplateRoot, null, argMap);
      Preconditions.checkState(result == fnTemplateRoot);
      return result;
    }
    else {
      Map<String, Node> newArgMap = Maps.newHashMap(argMap);
      List<Node> newVars = Lists.newLinkedList();
      for (Entry<String, Node> entry : argMap.entrySet()) {
        String name = entry.getKey();
        if(namesToAlias.contains(name)) {
          if(name.equals(THIS_MARKER)) {
            boolean referencesThis = NodeUtil.referencesThis(fnTemplateRoot);
            Node value = entry.getValue();
            if(!value.isThis() && (referencesThis || NodeUtil.mayHaveSideEffects(value, compiler))) {
              String newName = getUniqueThisName();
              Node newValue = entry.getValue().cloneTree();
              Node newNode = NodeUtil.newVarNode(newName, newValue).copyInformationFromForTree(newValue);
              newVars.add(0, newNode);
              newArgMap.put(THIS_MARKER, IR.name(newName).srcrefTree(newValue));
            }
          }
          else {
            Node newValue = entry.getValue().cloneTree();
            Node newNode = NodeUtil.newVarNode(name, newValue).copyInformationFromForTree(newValue);
            newVars.add(0, newNode);
            newArgMap.remove(name);
          }
        }
      }
      Node result = FunctionArgumentInjector.inject(compiler, fnTemplateRoot, null, newArgMap);
      Preconditions.checkState(result == fnTemplateRoot);
      for (Node n : newVars) {
        fnTemplateRoot.addChildToFront(n);
      }
      return result;
    }
  }
  private static Node createAssignStatementNode(String name, Node expression) {
    Node nameNode = IR.name(name);
    Node assign = IR.assign(nameNode, expression);
    return NodeUtil.newExpr(assign);
  }
  private static Node getReplacementReturnStatement(Node node, String resultName) {
    Node resultNode = null;
    Node retVal = null;
    if(node.hasChildren()) {
      retVal = node.getFirstChild().cloneTree();
    }
    if(resultName == null) {
      if(retVal != null) {
        resultNode = NodeUtil.newExpr(retVal);
      }
    }
    else {
      if(retVal == null) {
        Node srcLocation = node;
        retVal = NodeUtil.newUndefinedNode(srcLocation);
      }
      resultNode = createAssignStatementNode(resultName, retVal);
    }
    return resultNode;
  }
  Node mutate(String fnName, Node fnNode, Node callNode, String resultName, boolean needsDefaultResult, boolean isCallInLoop) {
    Node newFnNode = fnNode.cloneTree();
    makeLocalNamesUnique(newFnNode, isCallInLoop);
    rewriteFunctionDeclarations(newFnNode.getLastChild());
    Set<String> namesToAlias = FunctionArgumentInjector.findModifiedParameters(newFnNode);
    LinkedHashMap<String, Node> args = FunctionArgumentInjector.getFunctionCallParameterMap(newFnNode, callNode, this.safeNameIdSupplier);
    boolean hasArgs = !args.isEmpty();
    if(hasArgs) {
      FunctionArgumentInjector.maybeAddTempsForCallArguments(newFnNode, args, namesToAlias, compiler.getCodingConvention());
    }
    Node newBlock = NodeUtil.getFunctionBody(newFnNode);
    newBlock.detachFromParent();
    if(hasArgs) {
      Node inlineResult = aliasAndInlineArguments(newBlock, args, namesToAlias);
      Preconditions.checkState(newBlock == inlineResult);
    }
    if(isCallInLoop) {
      fixUnitializedVarDeclarations(newBlock);
    }
    String labelName = getLabelNameForFunction(fnName);
    Node injectableBlock = replaceReturns(newBlock, resultName, labelName, needsDefaultResult);
    Preconditions.checkState(injectableBlock != null);
    return injectableBlock;
  }
  private static Node replaceReturnWithBreak(Node current, Node parent, String resultName, String labelName) {
    if(current.isFunction() || current.isExprResult()) {
      return current;
    }
    if(current.isReturn()) {
      Preconditions.checkState(NodeUtil.isStatementBlock(parent));
      Node resultNode = getReplacementReturnStatement(current, resultName);
      Node breakNode = IR.breakNode(IR.labelName(labelName));
      breakNode.copyInformationFromForTree(current);
      parent.replaceChild(current, breakNode);
      if(resultNode != null) {
        resultNode.copyInformationFromForTree(current);
        parent.addChildBefore(resultNode, breakNode);
      }
      current = breakNode;
    }
    else {
      for(com.google.javascript.rhino.Node c = current.getFirstChild(); c != null; c = c.getNext()) {
        c = replaceReturnWithBreak(c, current, resultName, labelName);
      }
    }
    return current;
  }
  private static Node replaceReturns(Node block, String resultName, String labelName, boolean resultMustBeSet) {
    Preconditions.checkNotNull(block);
    Preconditions.checkNotNull(labelName);
    Node root = block;
    boolean hasReturnAtExit = false;
    int returnCount = NodeUtil.getNodeTypeReferenceCount(block, Token.RETURN, new NodeUtil.MatchShallowStatement());
    if(returnCount > 0) {
      hasReturnAtExit = hasReturnAtExit(block);
      if(hasReturnAtExit) {
        convertLastReturnToStatement(block, resultName);
        returnCount--;
      }
      if(returnCount > 0) {
        replaceReturnWithBreak(block, null, resultName, labelName);
        Node name = IR.labelName(labelName).srcref(block);
        Node label = IR.label(name, block).srcref(block);
        Node newRoot = IR.block().srcref(block);
        newRoot.addChildrenToBack(label);
        root = newRoot;
      }
    }
    if(resultMustBeSet && !hasReturnAtExit && resultName != null) {
      addDummyAssignment(block, resultName);
    }
    return root;
  }
  private String getLabelNameForFunction(String fnName) {
    String name = (fnName == null || fnName.isEmpty()) ? "anon" : fnName;
    return "JSCompiler_inline_label_" + name + "_" + safeNameIdSupplier.get();
  }
  private String getUniqueThisName() {
    return "JSCompiler_inline_this_" + safeNameIdSupplier.get();
  }
  private static boolean hasReturnAtExit(Node block) {
    return (block.getLastChild().isReturn());
  }
  private static void addDummyAssignment(Node node, String resultName) {
    Preconditions.checkArgument(node.isBlock());
    Node srcLocation = node;
    Node retVal = NodeUtil.newUndefinedNode(srcLocation);
    Node resultNode = createAssignStatementNode(resultName, retVal);
    resultNode.copyInformationFromForTree(node);
    node.addChildrenToBack(resultNode);
  }
  private static void convertLastReturnToStatement(Node block, String resultName) {
    Node ret = block.getLastChild();
    Preconditions.checkArgument(ret.isReturn());
    Node resultNode = getReplacementReturnStatement(ret, resultName);
    if(resultNode == null) {
      block.removeChild(ret);
    }
    else {
      resultNode.copyInformationFromForTree(ret);
      block.replaceChild(ret, resultNode);
    }
  }
  private void fixUnitializedVarDeclarations(Node n) {
    if(NodeUtil.isLoopStructure(n)) {
      return ;
    }
    if(n.isVar()) {
      Node name = n.getFirstChild();
      if(!name.hasChildren()) {
        Node srcLocation = name;
        name.addChildToBack(NodeUtil.newUndefinedNode(srcLocation));
      }
      return ;
    }
    for(com.google.javascript.rhino.Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      fixUnitializedVarDeclarations(c);
    }
  }
  private void makeLocalNamesUnique(Node fnNode, boolean isCallInLoop) {
    Supplier<String> idSupplier = compiler.getUniqueNameIdSupplier();
    NodeTraversal.traverse(compiler, fnNode, new MakeDeclaredNamesUnique(new InlineRenamer(idSupplier, "inline_", isCallInLoop)));
    new RenameLabels(compiler, new LabelNameSupplier(idSupplier), false).process(null, fnNode);
  }
  private void rewriteFunctionDeclarations(Node n) {
    if(n.isFunction()) {
      if(NodeUtil.isFunctionDeclaration(n)) {
        Node var_259 = n.getFirstChild();
        Node fnNameNode = var_259;
        Node name = IR.name(fnNameNode.getString()).srcref(fnNameNode);
        Node var = IR.var(name).srcref(n);
        fnNameNode.setString("");
        n.getParent().replaceChild(n, var);
        name.addChildToFront(n);
      }
      return ;
    }
    for(com.google.javascript.rhino.Node c = n.getFirstChild(), next; c != null; c = next) {
      next = c.getNext();
      rewriteFunctionDeclarations(c);
    }
  }
  
  static class LabelNameSupplier implements Supplier<String>  {
    final Supplier<String> idSupplier;
    LabelNameSupplier(Supplier<String> idSupplier) {
      super();
      this.idSupplier = idSupplier;
    }
    @Override() public String get() {
      return "JSCompiler_inline_label_" + idSupplier.get();
    }
  }
}