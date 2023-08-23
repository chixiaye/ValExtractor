package com.google.javascript.jscomp;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

class AliasStrings extends AbstractPostOrderCallback implements CompilerPass  {
  final private static Logger logger = Logger.getLogger(AliasStrings.class.getName());
  final private static String STRING_ALIAS_PREFIX = "$$S_";
  final private AbstractCompiler compiler;
  final private JSModuleGraph moduleGraph;
  private Matcher blacklist = null;
  final private Set<String> aliasableStrings;
  final private boolean outputStringUsage;
  final private SortedMap<String, StringInfo> stringInfoMap = Maps.newTreeMap();
  final private Set<String> usedHashedAliases = new LinkedHashSet<String>();
  final private Map<JSModule, Node> moduleVarParentMap = new HashMap<JSModule, Node>();
  long unitTestHashReductionMask = ~0L;
  AliasStrings(AbstractCompiler compiler, JSModuleGraph moduleGraph, Set<String> strings, String blacklistRegex, boolean outputStringUsage) {
    super();
    this.compiler = compiler;
    this.moduleGraph = moduleGraph;
    this.aliasableStrings = strings;
    if(blacklistRegex.length() != 0) {
      this.blacklist = Pattern.compile(blacklistRegex).matcher("");
    }
    else {
      this.blacklist = null;
    }
    this.outputStringUsage = outputStringUsage;
  }
  private StringInfo getOrCreateStringInfo(String string) {
    StringInfo info = stringInfoMap.get(string);
    if(info == null) {
      info = new StringInfo(stringInfoMap.size());
      stringInfoMap.put(string, info);
    }
    return info;
  }
  private static boolean isInThrowExpression(Node n) {
    for (Node ancestor : n.getAncestors()) {
      switch (ancestor.getType()){
        case Token.THROW:
        return true;
        case Token.IF:
        case Token.WHILE:
        case Token.DO:
        case Token.FOR:
        case Token.SWITCH:
        case Token.CASE:
        case Token.DEFAULT_CASE:
        case Token.BLOCK:
        case Token.SCRIPT:
        case Token.FUNCTION:
        case Token.TRY:
        case Token.CATCH:
        case Token.RETURN:
        case Token.EXPR_RESULT:
        return false;
      }
    }
    return false;
  }
  private static boolean shouldReplaceWithAlias(String str, StringInfo info) {
    if(info.numOccurrences > info.numOccurrencesInfrequentlyExecuted) {
      return true;
    }
    int sizeOfLiteral = 2 + str.length();
    int sizeOfStrings = info.numOccurrences * sizeOfLiteral;
    int sizeOfVariable = 3;
    int sizeOfAliases = 6 + sizeOfVariable + sizeOfLiteral + info.numOccurrences * sizeOfVariable;
    return sizeOfAliases < sizeOfStrings;
  }
  private void addAliasDeclarationNodes() {
    for (Entry<String, StringInfo> entry : stringInfoMap.entrySet()) {
      StringInfo info = entry.getValue();
      if(!info.isAliased) {
        continue ;
      }
      String alias = info.getVariableName(entry.getKey());
      Node var = IR.var(IR.name(alias), IR.string(entry.getKey()));
      if(info.siblingToInsertVarDeclBefore == null) {
        info.parentForNewVarDecl.addChildToFront(var);
      }
      else {
        info.parentForNewVarDecl.addChildBefore(var, info.siblingToInsertVarDeclBefore);
      }
      compiler.reportCodeChange();
    }
  }
  private void outputStringUsage() {
    StringBuilder sb = new StringBuilder("Strings used more than once:\n");
    for (Entry<String, StringInfo> stringInfoEntry : stringInfoMap.entrySet()) {
      StringInfo info = stringInfoEntry.getValue();
      if(info.numOccurrences > 1) {
        sb.append(info.numOccurrences);
        sb.append(": ");
        sb.append(stringInfoEntry.getKey());
        sb.append('\n');
      }
    }
    logger.fine(sb.toString());
  }
  @Override() public void process(Node externs, Node root) {
    logger.fine("Aliasing common strings");
    NodeTraversal.traverse(compiler, root, this);
    replaceStringsWithAliases();
    addAliasDeclarationNodes();
    if(outputStringUsage) {
      outputStringUsage();
    }
  }
  private void replaceStringWithAliasName(StringOccurrence occurrence, String name, StringInfo info) {
    occurrence.parent.replaceChild(occurrence.node, IR.name(name));
    info.isAliased = true;
    compiler.reportCodeChange();
  }
  private void replaceStringsWithAliases() {
    for (Entry<String, StringInfo> entry : stringInfoMap.entrySet()) {
      String literal = entry.getKey();
      StringInfo info = entry.getValue();
      if(shouldReplaceWithAlias(literal, info)) {
        for (StringOccurrence occurrence : info.occurrences) {
          replaceStringWithAliasName(occurrence, info.getVariableName(literal), info);
        }
      }
    }
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    if(n.isString() && !parent.isGetProp() && !parent.isRegExp()) {
      String str = n.getString();
      if("undefined".equals(str)) {
        return ;
      }
      if(blacklist != null && blacklist.reset(str).find()) {
        return ;
      }
      if(aliasableStrings == null || aliasableStrings.contains(str)) {
        StringOccurrence occurrence = new StringOccurrence(n, parent);
        StringInfo info = getOrCreateStringInfo(str);
        info.occurrences.add(occurrence);
        info.numOccurrences++;
        if(t.inGlobalScope() || isInThrowExpression(n)) {
          info.numOccurrencesInfrequentlyExecuted++;
        }
        JSModule module = t.getModule();
        if(info.numOccurrences != 1) {
          if(module != null && info.moduleToContainDecl != null && module != info.moduleToContainDecl && !moduleGraph.dependsOn(module, info.moduleToContainDecl)) {
            module = moduleGraph.getDeepestCommonDependency(module, info.moduleToContainDecl);
          }
          else {
            return ;
          }
        }
        Node varParent = moduleVarParentMap.get(module);
        if(varParent == null) {
          varParent = compiler.getNodeForCodeInsertion(module);
          moduleVarParentMap.put(module, varParent);
        }
        info.moduleToContainDecl = module;
        info.parentForNewVarDecl = varParent;
        info.siblingToInsertVarDeclBefore = varParent.getFirstChild();
      }
    }
  }
  
  final private class StringInfo  {
    final int id;
    boolean isAliased;
    final List<StringOccurrence> occurrences;
    int numOccurrences;
    int numOccurrencesInfrequentlyExecuted;
    JSModule moduleToContainDecl;
    Node parentForNewVarDecl;
    Node siblingToInsertVarDeclBefore;
    String aliasName;
    StringInfo(int id) {
      super();
      this.id = id;
      this.occurrences = new ArrayList<StringOccurrence>();
      this.isAliased = false;
    }
    String encodeStringAsIdentifier(String prefix, String s) {
      final int MAX_LIMIT = 20;
      final int length = s.length();
      final int limit = Math.min(length, MAX_LIMIT);
      StringBuilder sb = new StringBuilder();
      sb.append(prefix);
      boolean protectHex = false;
      for(int i = 0; i < limit; i++) {
        char ch = s.charAt(i);
        if(protectHex) {
          if((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f')) {
            StringBuilder var_386 = sb.append('_');
          }
          protectHex = false;
        }
        if((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
          sb.append(ch);
        }
        else {
          sb.append('$');
          sb.append(Integer.toHexString(ch));
          protectHex = true;
        }
      }
      if(length == limit) {
        return sb.toString();
      }
      CRC32 crc32 = new CRC32();
      crc32.update(s.getBytes());
      long hash = crc32.getValue() & unitTestHashReductionMask;
      sb.append('_');
      sb.append(Long.toHexString(hash));
      String encoded = sb.toString();
      if(!usedHashedAliases.add(encoded)) {
        encoded += "_" + id;
      }
      return encoded;
    }
    String getVariableName(String stringLiteral) {
      if(aliasName == null) {
        aliasName = encodeStringAsIdentifier(STRING_ALIAS_PREFIX, stringLiteral);
      }
      return aliasName;
    }
  }
  
  final private static class StringOccurrence  {
    final Node node;
    final Node parent;
    StringOccurrence(Node node, Node parent) {
      super();
      this.node = node;
      this.parent = parent;
    }
  }
}