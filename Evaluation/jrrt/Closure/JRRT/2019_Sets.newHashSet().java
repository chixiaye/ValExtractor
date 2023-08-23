package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.DefinitionsRemover.Definition;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Set;

class SimpleFunctionAliasAnalysis  {
  private Set<Node> aliasedFunctions;
  private Set<Node> functionsExposedToCallOrApply;
  public boolean isAliased(Node functionNode) {
    Preconditions.checkNotNull(aliasedFunctions);
    Preconditions.checkArgument(functionNode.isFunction());
    return aliasedFunctions.contains(functionNode);
  }
  public boolean isExposedToCallOrApply(Node functionNode) {
    Preconditions.checkNotNull(functionsExposedToCallOrApply);
    Preconditions.checkArgument(functionNode.isFunction());
    return functionsExposedToCallOrApply.contains(functionNode);
  }
  public void analyze(SimpleDefinitionFinder finder) {
    Preconditions.checkState(aliasedFunctions == null);
    java.util.HashSet<Node> var_2019 = Sets.newHashSet();
    aliasedFunctions = var_2019;
    functionsExposedToCallOrApply = Sets.newHashSet();
    for (DefinitionSite definitionSite : finder.getDefinitionSites()) {
      Definition definition = definitionSite.definition;
      if(!definition.isExtern()) {
        Node rValue = definition.getRValue();
        if(rValue != null && rValue.isFunction()) {
          for (UseSite useSite : finder.getUseSites(definition)) {
            updateFunctionForUse(rValue, useSite.node);
          }
        }
      }
    }
  }
  private void updateFunctionForUse(Node function, Node useNode) {
    Node useParent = useNode.getParent();
    int parentType = useParent.getType();
    if((parentType == Token.CALL || parentType == Token.NEW) && useParent.getFirstChild() == useNode) {
    }
    else 
      if(NodeUtil.isGet(useParent)) {
        if(useParent.isGetProp()) {
          Node gramps = useParent.getParent();
          if(NodeUtil.isFunctionObjectApply(gramps) || NodeUtil.isFunctionObjectCall(gramps)) {
            functionsExposedToCallOrApply.add(function);
          }
        }
      }
      else {
        aliasedFunctions.add(function);
      }
  }
}