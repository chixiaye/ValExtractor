package com.google.javascript.jscomp;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.javascript.jscomp.MakeDeclaredNamesUnique.ContextualRenamer;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Set;

class ExpressionDecomposer  {
  final private AbstractCompiler compiler;
  final private Supplier<String> safeNameIdSupplier;
  final private Set<String> knownConstants;
  final private static int MAX_INTERATIONS = 100;
  private String tempNamePrefix = "JSCompiler_temp";
  private String resultNamePrefix = "JSCompiler_inline_result";
  public ExpressionDecomposer(AbstractCompiler compiler, Supplier<String> safeNameIdSupplier, Set<String> constNames) {
    super();
    Preconditions.checkNotNull(compiler);
    Preconditions.checkNotNull(safeNameIdSupplier);
    Preconditions.checkNotNull(constNames);
    this.compiler = compiler;
    this.safeNameIdSupplier = safeNameIdSupplier;
    this.knownConstants = constNames;
  }
  DecompositionType canExposeExpression(Node subExpression) {
    Node expressionRoot = findExpressionRoot(subExpression);
    if(expressionRoot != null) {
      return isSubexpressionMovable(expressionRoot, subExpression);
    }
    return DecompositionType.UNDECOMPOSABLE;
  }
  private DecompositionType isSubexpressionMovable(Node expressionRoot, Node subExpression) {
    boolean requiresDecomposition = false;
    boolean seenSideEffects = NodeUtil.mayHaveSideEffects(subExpression, compiler);
    Node child = subExpression;
    for (Node parent : child.getAncestors()) {
      if(parent == expressionRoot) {
        return requiresDecomposition ? DecompositionType.DECOMPOSABLE : DecompositionType.MOVABLE;
      }
      int parentType = parent.getType();
      if(isConditionalOp(parent)) {
        if(child != parent.getFirstChild()) {
          requiresDecomposition = true;
        }
      }
      else {
        if(isSafeAssign(parent, seenSideEffects)) {
        }
        else {
          for (Node n : parent.children()) {
            if(n == child) {
              break ;
            }
            if(isExpressionTreeUnsafe(n, seenSideEffects)) {
              seenSideEffects = true;
              requiresDecomposition = true;
            }
          }
          Node first = parent.getFirstChild();
          if(requiresDecomposition && parent.isCall() && NodeUtil.isGet(first)) {
            if(maybeExternMethod(first)) {
              return DecompositionType.UNDECOMPOSABLE;
            }
            else {
              return DecompositionType.DECOMPOSABLE;
            }
          }
        }
      }
      child = parent;
    }
    throw new IllegalStateException("Unexpected.");
  }
  private static Node buildResultExpression(Node expr, boolean needResult, String tempName) {
    if(needResult) {
      return IR.assign(IR.name(tempName), expr).srcrefTree(expr);
    }
    else {
      return expr;
    }
  }
  private Node extractConditional(Node expr, Node injectionPoint, boolean needResult) {
    Node parent = expr.getParent();
    String tempName = getTempValueName();
    Node first = expr.getFirstChild();
    Node second = first.getNext();
    Node last = expr.getLastChild();
    expr.detachChildren();
    Node cond = null;
    Node trueExpr = IR.block().srcref(expr);
    Node falseExpr = IR.block().srcref(expr);
    switch (expr.getType()){
      case Token.HOOK:
      cond = first;
      trueExpr.addChildToFront(NodeUtil.newExpr(buildResultExpression(second, needResult, tempName)));
      falseExpr.addChildToFront(NodeUtil.newExpr(buildResultExpression(last, needResult, tempName)));
      break ;
      case Token.AND:
      cond = buildResultExpression(first, needResult, tempName);
      trueExpr.addChildToFront(NodeUtil.newExpr(buildResultExpression(last, needResult, tempName)));
      break ;
      case Token.OR:
      cond = buildResultExpression(first, needResult, tempName);
      falseExpr.addChildToFront(NodeUtil.newExpr(buildResultExpression(last, needResult, tempName)));
      break ;
      default:
      throw new IllegalStateException("Unexpected.");
    }
    Node ifNode;
    if(falseExpr.hasChildren()) {
      ifNode = IR.ifNode(cond, trueExpr, falseExpr);
    }
    else {
      ifNode = IR.ifNode(cond, trueExpr);
    }
    ifNode.copyInformationFrom(expr);
    if(needResult) {
      Node tempVarNode = NodeUtil.newVarNode(tempName, null).copyInformationFromForTree(expr);
      Node injectionPointParent = injectionPoint.getParent();
      injectionPointParent.addChildBefore(tempVarNode, injectionPoint);
      injectionPointParent.addChildAfter(ifNode, tempVarNode);
      Node replacementValueNode = IR.name(tempName);
      parent.replaceChild(expr, replacementValueNode);
    }
    else {
      Preconditions.checkArgument(parent.isExprResult());
      Node gramps = parent.getParent();
      gramps.replaceChild(parent, ifNode);
    }
    return ifNode;
  }
  private Node extractExpression(Node expr, Node injectionPoint) {
    Node parent = expr.getParent();
    boolean isLhsOfAssignOp = NodeUtil.isAssignmentOp(parent) && !parent.isAssign() && parent.getFirstChild() == expr;
    Node firstExtractedNode = null;
    if(isLhsOfAssignOp && NodeUtil.isGet(expr)) {
      for (Node n : expr.children()) {
        if(!n.isString() && !isConstantName(n, knownConstants)) {
          Node extractedNode = extractExpression(n, injectionPoint);
          if(firstExtractedNode == null) {
            firstExtractedNode = extractedNode;
          }
        }
      }
    }
    String tempName = getTempConstantValueName();
    Node replacementValueNode = IR.name(tempName).srcref(expr);
    Node tempNameValue;
    if(isLhsOfAssignOp) {
      Preconditions.checkState(expr.isName() || NodeUtil.isGet(expr));
      Node opNode = new Node(NodeUtil.getOpFromAssignmentOp(parent)).copyInformationFrom(parent);
      Node rightOperand = parent.getLastChild();
      parent.setType(Token.ASSIGN);
      parent.replaceChild(rightOperand, opNode);
      opNode.addChildToFront(replacementValueNode);
      opNode.addChildToBack(rightOperand);
      tempNameValue = expr.cloneTree();
    }
    else {
      parent.replaceChild(expr, replacementValueNode);
      tempNameValue = expr;
    }
    Node tempVarNode = NodeUtil.newVarNode(tempName, tempNameValue);
    Node injectionPointParent = injectionPoint.getParent();
    injectionPointParent.addChildBefore(tempVarNode, injectionPoint);
    if(firstExtractedNode == null) {
      firstExtractedNode = tempVarNode;
    }
    return firstExtractedNode;
  }
  static Node findExpressionRoot(Node subExpression) {
    Node child = subExpression;
    for (Node parent : child.getAncestors()) {
      int parentType = parent.getType();
      switch (parentType){
        case Token.EXPR_RESULT:
        case Token.IF:
        case Token.SWITCH:
        case Token.RETURN:
        case Token.VAR:
        Preconditions.checkState(child == parent.getFirstChild());
        return parent;
        case Token.SCRIPT:
        case Token.BLOCK:
        case Token.LABEL:
        case Token.CASE:
        case Token.DEFAULT_CASE:
        return null;
      }
      child = parent;
    }
    throw new IllegalStateException("Unexpected AST structure.");
  }
  static Node findInjectionPoint(Node subExpression) {
    Node expressionRoot = findExpressionRoot(subExpression);
    Preconditions.checkNotNull(expressionRoot);
    Node injectionPoint = expressionRoot;
    Node parent = injectionPoint.getParent();
    while(parent.isLabel()){
      injectionPoint = parent;
      parent = injectionPoint.getParent();
    }
    Preconditions.checkState(NodeUtil.isStatementBlock(injectionPoint.getParent()));
    return injectionPoint;
  }
  private static Node findNonconditionalParent(Node subExpression, Node expressionRoot) {
    Node result = subExpression;
    for(com.google.javascript.rhino.Node child = subExpression, parent = child.getParent(); parent != expressionRoot; child = parent, parent = child.getParent()) {
      if(isConditionalOp(parent)) {
        if(child != parent.getFirstChild()) {
          result = parent;
        }
      }
    }
    return result;
  }
  private Node rewriteCallExpression(Node call, DecompositionState state) {
    Preconditions.checkArgument(call.isCall());
    Node first = call.getFirstChild();
    Preconditions.checkArgument(NodeUtil.isGet(first));
    Node getVarNode = extractExpression(first, state.extractBeforeStatement);
    state.extractBeforeStatement = getVarNode;
    Node getExprNode = getVarNode.getFirstChild().getFirstChild();
    Preconditions.checkArgument(NodeUtil.isGet(getExprNode));
    Node thisVarNode = extractExpression(getExprNode.getFirstChild(), state.extractBeforeStatement);
    state.extractBeforeStatement = thisVarNode;
    Node thisNameNode = thisVarNode.getFirstChild();
    Node functionNameNode = getVarNode.getFirstChild();
    Node newCall = IR.call(IR.getprop(functionNameNode.cloneNode(), IR.string("call")), thisNameNode.cloneNode()).srcref(call);
    call.removeFirstChild();
    if(call.hasChildren()) {
      newCall.addChildrenToBack(call.removeChildren());
    }
    Node callParent = call.getParent();
    callParent.replaceChild(call, newCall);
    return newCall;
  }
  private String getResultValueName() {
    return resultNamePrefix + ContextualRenamer.UNIQUE_ID_SEPARATOR + safeNameIdSupplier.get();
  }
  private String getTempConstantValueName() {
    String name = tempNamePrefix + "_const" + ContextualRenamer.UNIQUE_ID_SEPARATOR + safeNameIdSupplier.get();
    this.knownConstants.add(name);
    return name;
  }
  private String getTempValueName() {
    return tempNamePrefix + ContextualRenamer.UNIQUE_ID_SEPARATOR + safeNameIdSupplier.get();
  }
  private static boolean allowObjectCallDecomposing() {
    return false;
  }
  private static boolean isConditionalOp(Node n) {
    switch (n.getType()){
      case Token.HOOK:
      case Token.AND:
      case Token.OR:
      return true;
      default:
      return false;
    }
  }
  private boolean isConstantName(Node n, Set<String> knownConstants) {
    return n.isName() && (NodeUtil.isConstantName(n) || knownConstants.contains(n.getString()));
  }
  private boolean isExpressionTreeUnsafe(Node n, boolean followingSideEffectsExist) {
    if(followingSideEffectsExist) {
      return NodeUtil.canBeSideEffected(n, this.knownConstants);
    }
    else {
      return NodeUtil.mayHaveSideEffects(n, compiler);
    }
  }
  private boolean isSafeAssign(Node n, boolean seenSideEffects) {
    if(n.isAssign()) {
      Node lhs = n.getFirstChild();
      switch (lhs.getType()){
        case Token.NAME:
        return true;
        case Token.GETPROP:
        return !isExpressionTreeUnsafe(lhs.getFirstChild(), seenSideEffects);
        case Token.GETELEM:
        return !isExpressionTreeUnsafe(lhs.getFirstChild(), seenSideEffects) && !isExpressionTreeUnsafe(lhs.getLastChild(), seenSideEffects);
      }
    }
    return false;
  }
  private boolean maybeExternMethod(Node node) {
    return true;
  }
  private void decomposeObjectLiteralKeys(Node key, Node stopNode, DecompositionState state) {
    if(key == null || key == stopNode) {
      return ;
    }
    decomposeObjectLiteralKeys(key.getNext(), stopNode, state);
    decomposeSubExpressions(key.getFirstChild(), stopNode, state);
  }
  private void decomposeSubExpressions(Node n, Node stopNode, DecompositionState state) {
    if(n == null || n == stopNode) {
      return ;
    }
    Preconditions.checkState(!NodeUtil.isObjectLitKey(n, n.getParent()));
    decomposeSubExpressions(n.getNext(), stopNode, state);
    if(isExpressionTreeUnsafe(n, state.sideEffects)) {
      state.sideEffects = true;
      state.extractBeforeStatement = extractExpression(n, state.extractBeforeStatement);
    }
  }
  void exposeExpression(Node expression) {
    Node expressionRoot = findExpressionRoot(expression);
    Preconditions.checkState(expressionRoot != null);
    exposeExpression(expressionRoot, expression);
    compiler.reportCodeChange();
  }
  private void exposeExpression(Node expressionRoot, Node subExpression) {
    Node nonconditionalExpr = findNonconditionalParent(subExpression, expressionRoot);
    boolean hasFollowingSideEffects = NodeUtil.mayHaveSideEffects(nonconditionalExpr, compiler);
    Node exprInjectionPoint = findInjectionPoint(nonconditionalExpr);
    DecompositionState state = new DecompositionState();
    state.sideEffects = hasFollowingSideEffects;
    state.extractBeforeStatement = exprInjectionPoint;
    for(com.google.javascript.rhino.Node grandchild = null, child = nonconditionalExpr, parent = child.getParent(); parent != expressionRoot; grandchild = child, child = parent, parent = child.getParent()) {
      int parentType = parent.getType();
      Preconditions.checkState(!isConditionalOp(parent) || child == parent.getFirstChild());
      if(parentType == Token.ASSIGN) {
        boolean var_1955 = state.sideEffects;
        if(isSafeAssign(parent, var_1955)) {
        }
        else {
          Node left = parent.getFirstChild();
          int type = left.getType();
          if(left != child) {
            Preconditions.checkState(NodeUtil.isGet(left));
            if(type == Token.GETELEM) {
              decomposeSubExpressions(left.getLastChild(), null, state);
            }
            decomposeSubExpressions(left.getFirstChild(), null, state);
          }
        }
      }
      else 
        if(parentType == Token.CALL && NodeUtil.isGet(parent.getFirstChild())) {
          Node functionExpression = parent.getFirstChild();
          decomposeSubExpressions(functionExpression.getNext(), child, state);
          if(isExpressionTreeUnsafe(functionExpression, state.sideEffects) && functionExpression.getFirstChild() != grandchild) {
            Preconditions.checkState(allowObjectCallDecomposing(), "Object method calls can not be decomposed.");
            state.sideEffects = true;
            Node replacement = rewriteCallExpression(parent, state);
            parent = replacement;
          }
        }
        else 
          if(parentType == Token.OBJECTLIT) {
            decomposeObjectLiteralKeys(parent.getFirstChild(), child, state);
          }
          else {
            decomposeSubExpressions(parent.getFirstChild(), child, state);
          }
    }
    if(nonconditionalExpr == subExpression) {
    }
    else {
      Node parent = nonconditionalExpr.getParent();
      boolean needResult = !parent.isExprResult();
      Node extractedConditional = extractConditional(nonconditionalExpr, exprInjectionPoint, needResult);
    }
  }
  void maybeExposeExpression(Node expression) {
    int i = 0;
    while(DecompositionType.DECOMPOSABLE == canExposeExpression(expression)){
      exposeExpression(expression);
      i++;
      if(i > MAX_INTERATIONS) {
        throw new IllegalStateException("DecomposeExpression depth exceeded on :\n" + expression.toStringTree());
      }
    }
  }
  void moveExpression(Node expression) {
    String resultName = getResultValueName();
    Node injectionPoint = findInjectionPoint(expression);
    Preconditions.checkNotNull(injectionPoint);
    Node injectionPointParent = injectionPoint.getParent();
    Preconditions.checkNotNull(injectionPointParent);
    Preconditions.checkState(NodeUtil.isStatementBlock(injectionPointParent));
    Node expressionParent = expression.getParent();
    expressionParent.replaceChild(expression, IR.name(resultName));
    Node newExpressionRoot = NodeUtil.newVarNode(resultName, expression);
    injectionPointParent.addChildBefore(newExpressionRoot, injectionPoint);
    compiler.reportCodeChange();
  }
  @VisibleForTesting() public void setResultNamePrefix(String prefix) {
    this.resultNamePrefix = prefix;
  }
  @VisibleForTesting() public void setTempNamePrefix(String prefix) {
    this.tempNamePrefix = prefix;
  }
  
  private static class DecompositionState  {
    boolean sideEffects;
    Node extractBeforeStatement;
  }
  enum DecompositionType {
    UNDECOMPOSABLE(),

    MOVABLE(),

    DECOMPOSABLE(),

  ;
  private DecompositionType() {
  }
  }
}