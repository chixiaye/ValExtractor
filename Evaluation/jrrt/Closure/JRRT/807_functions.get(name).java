package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ReplaceStrings extends AbstractPostOrderCallback implements CompilerPass  {
  final static DiagnosticType BAD_REPLACEMENT_CONFIGURATION = DiagnosticType.warning("JSC_BAD_REPLACEMENT_CONFIGURATION", "Bad replacement configuration.");
  final private String DEFAULT_PLACEHOLDER_TOKEN = "`";
  final private String placeholderToken;
  final private static String REPLACE_ONE_MARKER = "?";
  final private static String REPLACE_ALL_MARKER = "*";
  final private AbstractCompiler compiler;
  final private JSTypeRegistry registry;
  final private Map<String, Config> functions = Maps.newHashMap();
  final private Multimap<String, String> methods = HashMultimap.create();
  final private NameGenerator nameGenerator;
  final private Map<String, Result> results = Maps.newLinkedHashMap();
  final static Predicate<Result> USED_RESULTS = new Predicate<Result>() {
      @Override() public boolean apply(Result result) {
        return !result.replacementLocations.isEmpty();
      }
  };
  ReplaceStrings(AbstractCompiler compiler, String placeholderToken, List<String> functionsToInspect, Set<String> blacklisted, VariableMap previousMappings) {
    super();
    this.compiler = compiler;
    this.placeholderToken = placeholderToken.isEmpty() ? DEFAULT_PLACEHOLDER_TOKEN : placeholderToken;
    this.registry = compiler.getTypeRegistry();
    Iterable<String> reservedNames = blacklisted;
    if(previousMappings != null) {
      Set<String> previous = previousMappings.getOriginalNameToNewNameMap().keySet();
      reservedNames = Iterables.concat(blacklisted, previous);
      initMapping(previousMappings, blacklisted);
    }
    this.nameGenerator = createNameGenerator(reservedNames);
    parseConfiguration(functionsToInspect);
  }
  private Config findMatching(String name) {
    Config var_807 = functions.get(name);
    Config config = var_807;
    if(config == null) {
      name = name.replace('$', '.');
      config = functions.get(name);
    }
    return config;
  }
  private Config findMatchingClass(JSType callClassType, Collection<String> declarationNames) {
    if(!callClassType.isNoObjectType() && !callClassType.isUnknownType()) {
      for (String declarationName : declarationNames) {
        String className = getClassFromDeclarationName(declarationName);
        JSType methodClassType = registry.getType(className);
        if(methodClassType != null && callClassType.isSubtype(methodClassType)) {
          return functions.get(declarationName);
        }
      }
    }
    return null;
  }
  private Config parseConfiguration(String function) {
    int first = function.indexOf('(');
    int last = function.indexOf(')');
    Preconditions.checkState(first != -1 && last != -1);
    String name = function.substring(0, first);
    String params = function.substring(first + 1, last);
    int paramCount = 0;
    int replacementParameter = -1;
    String[] parts = params.split(",");
    for (String param : parts) {
      paramCount++;
      if(param.equals(REPLACE_ALL_MARKER)) {
        Preconditions.checkState(paramCount == 1 && parts.length == 1);
        replacementParameter = Config.REPLACE_ALL_VALUE;
      }
      else 
        if(param.equals(REPLACE_ONE_MARKER)) {
          Preconditions.checkState(replacementParameter == -1);
          replacementParameter = paramCount;
        }
        else {
          Preconditions.checkState(param.isEmpty(), "Unknown marker", param);
        }
    }
    Preconditions.checkState(replacementParameter != -1);
    return new Config(name, replacementParameter);
  }
  List<Result> getResult() {
    return ImmutableList.copyOf(Iterables.filter(results.values(), USED_RESULTS));
  }
  private static NameGenerator createNameGenerator(Iterable<String> reserved) {
    final String namePrefix = "";
    final char[] reservedChars = new char[0];
    return new NameGenerator(ImmutableSet.copyOf(reserved), namePrefix, reservedChars);
  }
  private Node buildReplacement(Node expr, Node prefix, StringBuilder keyBuilder) {
    switch (expr.getType()){
      case Token.ADD:
      Node left = expr.getFirstChild();
      Node right = left.getNext();
      prefix = buildReplacement(left, prefix, keyBuilder);
      return buildReplacement(right, prefix, keyBuilder);
      case Token.STRING:
      keyBuilder.append(expr.getString());
      return prefix;
      default:
      keyBuilder.append(placeholderToken);
      prefix = IR.add(prefix, IR.string(placeholderToken));
      return IR.add(prefix, expr.cloneTree());
    }
  }
  private Node replaceExpression(NodeTraversal t, Node expr, Node parent) {
    Node replacement;
    String key = null;
    String replacementString;
    switch (expr.getType()){
      case Token.STRING:
      key = expr.getString();
      replacementString = getReplacement(key);
      replacement = IR.string(replacementString);
      break ;
      case Token.ADD:
      StringBuilder keyBuilder = new StringBuilder();
      Node keyNode = IR.string("");
      replacement = buildReplacement(expr, keyNode, keyBuilder);
      key = keyBuilder.toString();
      replacementString = getReplacement(key);
      keyNode.setString(replacementString);
      break ;
      case Token.NAME:
      Scope.Var var = t.getScope().getVar(expr.getString());
      if(var != null && var.isConst()) {
        Node value = var.getInitialValue();
        if(value != null && value.isString()) {
          key = value.getString();
          replacementString = getReplacement(key);
          replacement = IR.string(replacementString);
          break ;
        }
      }
      return expr;
      default:
      return expr;
    }
    Preconditions.checkNotNull(key);
    Preconditions.checkNotNull(replacementString);
    recordReplacement(expr, key, replacementString);
    parent.replaceChild(expr, replacement);
    compiler.reportCodeChange();
    return replacement;
  }
  private String getClassFromDeclarationName(String fullDeclarationName) {
    String[] parts = fullDeclarationName.split("\\.prototype\\.");
    Preconditions.checkState(parts.length == 1 || parts.length == 2);
    if(parts.length == 2) {
      return parts[0];
    }
    return null;
  }
  private String getMethodFromDeclarationName(String fullDeclarationName) {
    String[] parts = fullDeclarationName.split("\\.prototype\\.");
    Preconditions.checkState(parts.length == 1 || parts.length == 2);
    if(parts.length == 2) {
      return parts[1];
    }
    return null;
  }
  private String getReplacement(String key) {
    Result result = results.get(key);
    if(result != null) {
      return result.replacement;
    }
    String replacement = nameGenerator.generateNextName();
    result = new Result(key, replacement);
    results.put(key, result);
    return replacement;
  }
  VariableMap getStringMap() {
    ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
    for (Result result : Iterables.filter(results.values(), USED_RESULTS)) {
      map.put(result.replacement, result.original);
    }
    VariableMap stringMap = new VariableMap(map.build());
    return stringMap;
  }
  private void doSubstitutions(NodeTraversal t, Config config, Node n) {
    Preconditions.checkState(n.isNew() || n.isCall());
    if(config.parameter != Config.REPLACE_ALL_VALUE) {
      Node arg = n.getChildAtIndex(config.parameter);
      if(arg != null) {
        replaceExpression(t, arg, n);
      }
    }
    else {
      Node firstParam = n.getFirstChild().getNext();
      for(com.google.javascript.rhino.Node arg = firstParam; arg != null; arg = arg.getNext()) {
        arg = replaceExpression(t, arg, n);
      }
    }
  }
  private void initMapping(VariableMap previousVarMap, Set<String> reservedNames) {
    Map<String, String> previous = previousVarMap.getOriginalNameToNewNameMap();
    for (Map.Entry<String, String> entry : previous.entrySet()) {
      String key = entry.getKey();
      if(!reservedNames.contains(key)) {
        String value = entry.getValue();
        results.put(value, new Result(value, key));
      }
    }
  }
  private void parseConfiguration(List<String> functionsToInspect) {
    for (String function : functionsToInspect) {
      Config config = parseConfiguration(function);
      functions.put(config.name, config);
      String method = getMethodFromDeclarationName(config.name);
      if(method != null) {
        methods.put(method, config.name);
      }
    }
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, this);
  }
  private void recordReplacement(Node n, String key, String replacement) {
    Result result = results.get(key);
    Preconditions.checkState(result != null);
    result.addLocation(n);
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()){
      case Token.NEW:
      case Token.CALL:
      Node calledFn = n.getFirstChild();
      String name = calledFn.getQualifiedName();
      if(name != null) {
        Config config = findMatching(name);
        if(config != null) {
          doSubstitutions(t, config, n);
          return ;
        }
      }
      if(NodeUtil.isGet(calledFn)) {
        Node rhs = calledFn.getLastChild();
        if(rhs.isName() || rhs.isString()) {
          String methodName = rhs.getString();
          Collection<String> classes = methods.get(methodName);
          if(classes != null) {
            Node lhs = calledFn.getFirstChild();
            if(lhs.getJSType() != null) {
              JSType type = lhs.getJSType().restrictByNotNullOrUndefined();
              Config config = findMatchingClass(type, classes);
              if(config != null) {
                doSubstitutions(t, config, n);
                return ;
              }
            }
          }
        }
      }
      break ;
    }
  }
  
  private class Config  {
    final String name;
    final int parameter;
    final static int REPLACE_ALL_VALUE = 0;
    Config(String name, int parameter) {
      super();
      this.name = name;
      this.parameter = parameter;
    }
  }
  
  class Location  {
    final public String sourceFile;
    final public int line;
    final public int column;
    Location(String sourceFile, int line, int column) {
      super();
      this.sourceFile = sourceFile;
      this.line = line;
      this.column = column;
    }
  }
  
  class Result  {
    final public String original;
    final public String replacement;
    final public List<Location> replacementLocations = Lists.newLinkedList();
    Result(String original, String replacement) {
      super();
      this.original = original;
      this.replacement = replacement;
    }
    void addLocation(Node n) {
      replacementLocations.add(new Location(n.getSourceFileName(), n.getLineno(), n.getCharno()));
    }
  }
}