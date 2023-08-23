package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.NodeTraversal.ScopedCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

class ExpandJqueryAliases extends AbstractPostOrderCallback implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private CodingConvention convention;
  final private static Logger logger = Logger.getLogger(ExpandJqueryAliases.class.getName());
  final static DiagnosticType JQUERY_UNABLE_TO_EXPAND_INVALID_LIT_ERROR = DiagnosticType.warning("JSC_JQUERY_UNABLE_TO_EXPAND_INVALID_LIT", "jQuery.expandedEach call cannot be expanded because the first " + "argument must be an object literal or an array of strings " + "literal.");
  final static DiagnosticType JQUERY_UNABLE_TO_EXPAND_INVALID_NAME_ERROR = DiagnosticType.error("JSC_JQUERY_UNABLE_TO_EXPAND_INVALID_NAME", "jQuery.expandedEach expansion would result in the invalid " + "property name \"{0}\".");
  final static DiagnosticType JQUERY_USELESS_EACH_EXPANSION = DiagnosticType.warning("JSC_JQUERY_USELESS_EACH_EXPANSION", "jQuery.expandedEach was not expanded as no valid property " + "assignments were encountered. Consider using jQuery.each instead.");
  final private static Set<String> JQUERY_EXTEND_NAMES = ImmutableSet.of("jQuery.extend", "jQuery.fn.extend", "jQuery.prototype.extend");
  final private static String JQUERY_EXPANDED_EACH_NAME = "jQuery.expandedEach";
  final private PeepholeOptimizationsPass peepholePasses;
  ExpandJqueryAliases(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
    this.convention = compiler.getCodingConvention();
    final boolean late = false;
    this.peepholePasses = new PeepholeOptimizationsPass(compiler, new PeepholeSubstituteAlternateSyntax(late), new PeepholeReplaceKnownMethods(late), new PeepholeRemoveDeadCode(), new PeepholeFoldConstants(late), new PeepholeCollectPropertyAssignments());
  }
  private Node tryExpandJqueryEachCall(NodeTraversal t, Node n, Node callbackFunction, List<Node> keyNodes, List<Node> valueNodes) {
    Node callTarget = n.getFirstChild();
    Node objectToLoopOver = callTarget.getNext();
    Node fncBlock = IR.block().srcref(callTarget);
    boolean isValidExpansion = true;
    Node key = objectToLoopOver.getFirstChild();
    Node val = null;
    for(int i = 0; key != null; key = key.getNext(), i++) {
      if(key != null) {
        if(objectToLoopOver.isArrayLit()) {
          val = IR.number(i).srcref(key);
        }
        else {
          val = key.getFirstChild();
        }
      }
      List<Node> newKeys = Lists.newArrayList();
      List<Node> newValues = Lists.newArrayList();
      List<Node> origGetElems = Lists.newArrayList();
      List<Node> newGetProps = Lists.newArrayList();
      for(int j = 0; j < keyNodes.size(); j++) {
        Node origNode = keyNodes.get(j);
        Node ancestor = origNode.getParent();
        Node newNode = IR.string(key.getString()).srcref(key);
        newKeys.add(newNode);
        ancestor.replaceChild(origNode, newNode);
        while(ancestor != null && !NodeUtil.isStatement(ancestor) && !ancestor.isGetElem()){
          ancestor = ancestor.getParent();
        }
        if(ancestor != null && ancestor.isGetElem()) {
          Node propObject = ancestor;
          while(propObject.isGetProp() || propObject.isGetElem()){
            propObject = propObject.getFirstChild();
          }
          Node ancestorClone = ancestor.cloneTree();
          peepholePasses.process(null, ancestorClone.getChildAtIndex(1));
          Node prop = ancestorClone.getChildAtIndex(1);
          if(prop.isString() && NodeUtil.isValidPropertyName(prop.getString())) {
            Node target = ancestorClone.getFirstChild();
            Node newGetProp = IR.getprop(target.detachFromParent(), prop.detachFromParent());
            newGetProps.add(newGetProp);
            origGetElems.add(ancestor);
            ancestor.getParent().replaceChild(ancestor, newGetProp);
          }
          else {
            if(prop.isString() && !NodeUtil.isValidPropertyName(prop.getString())) {
              t.report(n, JQUERY_UNABLE_TO_EXPAND_INVALID_NAME_ERROR, prop.getString());
            }
            isValidExpansion = false;
          }
        }
      }
      if(isValidExpansion) {
        for(int j = 0; val != null && j < valueNodes.size(); j++) {
          Node var_1148 = valueNodes.get(j);
          Node origNode = var_1148;
          Node newNode = val.cloneTree();
          newValues.add(newNode);
          origNode.getParent().replaceChild(origNode, newNode);
        }
        Node fnc = IR.function(IR.name("").srcref(key), IR.paramList().srcref(key), callbackFunction.getChildAtIndex(2).cloneTree()).srcref(key);
        Node call = IR.call(fnc).srcref(key);
        call.putBooleanProp(Node.FREE_CALL, true);
        fncBlock.addChildToBack(IR.exprResult(call).srcref(call));
      }
      for(int j = 0; j < newGetProps.size(); j++) {
        newGetProps.get(j).getParent().replaceChild(newGetProps.get(j), origGetElems.get(j));
      }
      for(int j = 0; j < newKeys.size(); j++) {
        newKeys.get(j).getParent().replaceChild(newKeys.get(j), keyNodes.get(j));
      }
      for(int j = 0; j < newValues.size(); j++) {
        newValues.get(j).getParent().replaceChild(newValues.get(j), valueNodes.get(j));
      }
      if(!isValidExpansion) {
        return null;
      }
    }
    return fncBlock;
  }
  private boolean isArrayLitValidForExpansion(Node n) {
    Iterator<Node> iter = n.children().iterator();
    while(iter.hasNext()){
      Node child = iter.next();
      if(!child.isString()) {
        return false;
      }
    }
    return true;
  }
  public boolean isJqueryExpandedEachCall(Node call, String qName) {
    Preconditions.checkArgument(call.isCall());
    if(call.getFirstChild() != null && JQUERY_EXPANDED_EACH_NAME.equals(qName)) {
      return true;
    }
    return false;
  }
  public static boolean isJqueryExtendCall(Node n, String qname, AbstractCompiler compiler) {
    if(JQUERY_EXTEND_NAMES.contains(qname)) {
      Node firstArgument = n.getNext();
      if(firstArgument == null) {
        return false;
      }
      Node secondArgument = firstArgument.getNext();
      if((firstArgument.isObjectLit() && secondArgument == null) || (firstArgument.isName() || NodeUtil.isGet(firstArgument) && !NodeUtil.mayHaveSideEffects(firstArgument, compiler) && secondArgument != null && secondArgument.isObjectLit() && secondArgument.getNext() == null)) {
        return true;
      }
    }
    return false;
  }
  private void maybeExpandJqueryEachCall(NodeTraversal t, Node n) {
    Node objectToLoopOver = n.getChildAtIndex(1);
    if(objectToLoopOver == null) {
      return ;
    }
    Node callbackFunction = objectToLoopOver.getNext();
    if(callbackFunction == null || !callbackFunction.isFunction()) {
      return ;
    }
    peepholePasses.process(null, n.getChildAtIndex(1));
    Node nClone = n.cloneTree();
    objectToLoopOver = nClone.getChildAtIndex(1);
    if(!objectToLoopOver.isObjectLit() && !(objectToLoopOver.isArrayLit() && isArrayLitValidForExpansion(objectToLoopOver))) {
      t.report(n, JQUERY_UNABLE_TO_EXPAND_INVALID_LIT_ERROR, (String)null);
      return ;
    }
    List<Node> keyNodeReferences = Lists.newArrayList();
    List<Node> valueNodeReferences = Lists.newArrayList();
    NodeTraversal.traverse(compiler, NodeUtil.getFunctionBody(callbackFunction), new FindCallbackArgumentReferences(callbackFunction, keyNodeReferences, valueNodeReferences, objectToLoopOver.isArrayLit()));
    if(keyNodeReferences.size() == 0) {
      t.report(n, JQUERY_USELESS_EACH_EXPANSION, (String)null);
      return ;
    }
    Node fncBlock = tryExpandJqueryEachCall(t, nClone, callbackFunction, keyNodeReferences, valueNodeReferences);
    if(fncBlock != null && fncBlock.hasChildren()) {
      replaceOriginalJqueryEachCall(n, fncBlock);
    }
    else {
      t.report(n, JQUERY_USELESS_EACH_EXPANSION, (String)null);
    }
  }
  private void maybeExpandJqueryExtendCall(Node n) {
    Node callTarget = n.getFirstChild();
    Node objectToExtend = callTarget.getNext();
    Node extendArg = objectToExtend.getNext();
    boolean ensureObjectDefined = true;
    if(extendArg == null) {
      extendArg = objectToExtend;
      objectToExtend = callTarget.getFirstChild();
      ensureObjectDefined = false;
    }
    else 
      if(objectToExtend.isGetProp() && (objectToExtend.getLastChild().getString().equals("prototype") || convention.isPrototypeAlias(objectToExtend))) {
        ensureObjectDefined = false;
      }
    if(!extendArg.hasChildren()) {
      return ;
    }
    Node fncBlock = IR.block().srcref(n);
    if(ensureObjectDefined) {
      Node assignVal = IR.or(objectToExtend.cloneTree(), IR.objectlit().srcref(n)).srcref(n);
      Node assign = IR.assign(objectToExtend.cloneTree(), assignVal).srcref(n);
      fncBlock.addChildrenToFront(IR.exprResult(assign).srcref(n));
    }
    while(extendArg.hasChildren()){
      Node currentProp = extendArg.removeFirstChild();
      currentProp.setType(Token.STRING);
      Node propValue = currentProp.removeFirstChild();
      Node newProp;
      if(currentProp.isQuotedString()) {
        newProp = IR.getelem(objectToExtend.cloneTree(), currentProp).srcref(currentProp);
      }
      else {
        newProp = IR.getprop(objectToExtend.cloneTree(), currentProp).srcref(currentProp);
      }
      Node assignNode = IR.assign(newProp, propValue).srcref(currentProp);
      fncBlock.addChildToBack(IR.exprResult(assignNode).srcref(currentProp));
    }
    if(n.getParent().isExprResult()) {
      Node parent = n.getParent();
      parent.getParent().replaceChild(parent, fncBlock);
    }
    else {
      Node targetVal;
      if("jQuery.prototype".equals(objectToExtend.getQualifiedName())) {
        targetVal = objectToExtend.removeFirstChild();
      }
      else {
        targetVal = objectToExtend.detachFromParent();
      }
      fncBlock.addChildToBack(IR.returnNode(targetVal).srcref(targetVal));
      Node fnc = IR.function(IR.name("").srcref(n), IR.paramList().srcref(n), fncBlock);
      n.replaceChild(callTarget, fnc);
      n.putBooleanProp(Node.FREE_CALL, true);
      while(fnc.getNext() != null){
        n.removeChildAfter(fnc);
      }
    }
    compiler.reportCodeChange();
  }
  private void maybeReplaceJqueryPrototypeAlias(Node n) {
    if(NodeUtil.isLValue(n)) {
      Node maybeAssign = n.getParent();
      while(!NodeUtil.isStatement(maybeAssign) && !maybeAssign.isAssign()){
        maybeAssign = maybeAssign.getParent();
      }
      if(maybeAssign.isAssign()) {
        maybeAssign = maybeAssign.getParent();
        if(maybeAssign.isBlock() || maybeAssign.isScript() || NodeUtil.isStatement(maybeAssign)) {
          return ;
        }
      }
    }
    Node fn = n.getLastChild();
    if(fn != null) {
      n.replaceChild(fn, IR.string("prototype"));
      compiler.reportCodeChange();
    }
  }
  @Override() public void process(Node externs, Node root) {
    logger.fine("Expanding Jquery Aliases");
    NodeTraversal.traverse(compiler, root, this);
  }
  private void replaceOriginalJqueryEachCall(Node n, Node expandedBlock) {
    if(n.getParent().isExprResult()) {
      Node parent = n.getParent();
      Node grandparent = parent.getParent();
      Node insertAfter = parent;
      while(expandedBlock.hasChildren()){
        Node child = expandedBlock.getFirstChild().detachFromParent();
        grandparent.addChildAfter(child, insertAfter);
        insertAfter = child;
      }
      grandparent.removeChild(parent);
    }
    else {
      Node callTarget = n.getFirstChild();
      Node objectToLoopOver = callTarget.getNext();
      objectToLoopOver.detachFromParent();
      Node ret = IR.returnNode(objectToLoopOver).srcref(callTarget);
      expandedBlock.addChildToBack(ret);
      Node fnc = IR.function(IR.name("").srcref(callTarget), IR.paramList().srcref(callTarget), expandedBlock);
      n.replaceChild(callTarget, fnc);
      n.putBooleanProp(Node.FREE_CALL, true);
      while(fnc.getNext() != null){
        n.removeChildAfter(fnc);
      }
    }
    compiler.reportCodeChange();
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    if(n.isGetProp() && convention.isPrototypeAlias(n)) {
      maybeReplaceJqueryPrototypeAlias(n);
    }
    else 
      if(n.isCall()) {
        Node callTarget = n.getFirstChild();
        String qName = callTarget.getQualifiedName();
        if(isJqueryExtendCall(callTarget, qName, this.compiler)) {
          maybeExpandJqueryExtendCall(n);
        }
        else 
          if(isJqueryExpandedEachCall(n, qName)) {
            maybeExpandJqueryEachCall(t, n);
          }
      }
  }
  
  class FindCallbackArgumentReferences extends AbstractPostOrderCallback implements ScopedCallback  {
    final private String keyName;
    final private String valueName;
    private Scope startingScope;
    private List<Node> keyReferences;
    private List<Node> valueReferences;
    FindCallbackArgumentReferences(Node functionRoot, List<Node> keyReferences, List<Node> valueReferences, boolean useArrayMode) {
      super();
      Preconditions.checkState(functionRoot.isFunction());
      String keyString = null;
      String valueString = null;
      Node callbackParams = NodeUtil.getFunctionParameters(functionRoot);
      Node param = callbackParams.getFirstChild();
      if(param != null) {
        Preconditions.checkState(param.isName());
        keyString = param.getString();
        param = param.getNext();
        if(param != null) {
          Preconditions.checkState(param.isName());
          valueString = param.getString();
        }
      }
      this.keyName = keyString;
      this.valueName = valueString;
      if(useArrayMode) {
        this.keyReferences = valueReferences;
        this.valueReferences = keyReferences;
      }
      else {
        this.keyReferences = keyReferences;
        this.valueReferences = valueReferences;
      }
      this.startingScope = null;
    }
    private boolean isShadowed(String name, Scope scope) {
      Var nameVar = scope.getVar(name);
      return nameVar != null && nameVar.getScope() != this.startingScope;
    }
    @Override() public void enterScope(NodeTraversal t) {
      if(this.startingScope == null) {
        this.startingScope = t.getScope();
      }
    }
    @Override() public void exitScope(NodeTraversal t) {
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      boolean isThis = false;
      if(t.getScope() == this.startingScope) {
        isThis = n.isThis();
      }
      if(isThis || n.isName() && !isShadowed(n.getString(), t.getScope())) {
        String nodeValue = isThis ? null : n.getString();
        if(!isThis && keyName != null && nodeValue.equals(keyName)) {
          keyReferences.add(n);
        }
        else 
          if(isThis || (valueName != null && nodeValue.equals(valueName))) {
            valueReferences.add(n);
          }
      }
    }
  }
}