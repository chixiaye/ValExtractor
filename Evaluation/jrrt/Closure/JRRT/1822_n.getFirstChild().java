package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.javascript.jscomp.AbstractCompiler.LifeCycleStage;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.TokenStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;

class RenamePrototypes implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private boolean aggressiveRenaming;
  final private char[] reservedCharacters;
  final private VariableMap prevUsedRenameMap;
  final private static Comparator<Property> FREQUENCY_COMPARATOR = new Comparator<Property>() {
      @Override() public int compare(Property a1, Property a2) {
        int n1 = a1.count();
        int n2 = a2.count();
        if(n1 != n2) {
          return n2 - n1;
        }
        return a1.oldName.compareTo(a2.oldName);
      }
  };
  final private Set<Node> stringNodes = new HashSet<Node>();
  final private Map<String, Property> properties = new HashMap<String, Property>();
  final private Set<String> reservedNames = new HashSet<String>(Arrays.asList("indexOf", "lastIndexOf", "toString", "valueOf"));
  final private Set<Node> prototypeObjLits = new HashSet<Node>();
  RenamePrototypes(AbstractCompiler compiler, boolean aggressiveRenaming, @Nullable() char[] reservedCharacters, @Nullable() VariableMap prevUsedRenameMap) {
    super();
    this.compiler = compiler;
    this.aggressiveRenaming = aggressiveRenaming;
    this.reservedCharacters = reservedCharacters;
    this.prevUsedRenameMap = prevUsedRenameMap;
  }
  VariableMap getPropertyMap() {
    ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
    for (Property p : properties.values()) {
      if(p.newName != null) {
        map.put(p.oldName, p.newName);
      }
    }
    return new VariableMap(map.build());
  }
  @Override() public void process(Node externs, Node root) {
    Preconditions.checkState(compiler.getLifeCycleStage().isNormalized());
    NodeTraversal.traverse(compiler, externs, new ProcessExternedProperties());
    NodeTraversal.traverse(compiler, root, new ProcessProperties());
    SortedSet<Property> propsByFrequency = new TreeSet<Property>(FREQUENCY_COMPARATOR);
    for(java.util.Iterator<java.util.Map.Entry<java.lang.String, com.google.javascript.jscomp.RenamePrototypes.Property>> it = properties.entrySet().iterator(); it.hasNext(); ) {
      Property a = it.next().getValue();
      if(a.canRename() && !reservedNames.contains(a.oldName)) {
        propsByFrequency.add(a);
      }
      else {
        it.remove();
        reservedNames.add(a.oldName);
      }
    }
    if(prevUsedRenameMap != null) {
      reusePrototypeNames(propsByFrequency);
    }
    NameGenerator nameGen = new NameGenerator(reservedNames, "", reservedCharacters);
    StringBuilder debug = new StringBuilder();
    for (Property a : propsByFrequency) {
      if(a.newName == null) {
        a.newName = nameGen.generateNextName();
        reservedNames.add(a.newName);
      }
      debug.append(a.oldName).append(" => ").append(a.newName).append('\n');
    }
    compiler.addToDebugLog("JS property assignments:\n" + debug);
    boolean changed = false;
    for (Node n : stringNodes) {
      String oldName = n.getString();
      Property a = properties.get(oldName);
      if(a != null && a.newName != null) {
        n.setString(a.newName);
        changed = changed || !a.newName.equals(oldName);
      }
    }
    if(changed) {
      compiler.reportCodeChange();
    }
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED_OBFUSCATED);
  }
  private void reusePrototypeNames(Set<Property> properties) {
    for (Property prop : properties) {
      String prevName = prevUsedRenameMap.lookupNewName(prop.oldName);
      if(prevName != null) {
        if(reservedNames.contains(prevName)) {
          continue ;
        }
        prop.newName = prevName;
        reservedNames.add(prevName);
      }
    }
  }
  
  private class ProcessExternedProperties extends AbstractPostOrderCallback  {
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.GETPROP:
        case Token.GETELEM:
        Node dest = n.getFirstChild().getNext();
        if(dest.isString()) {
          reservedNames.add(dest.getString());
        }
      }
    }
  }
  
  private class ProcessProperties extends AbstractPostOrderCallback  {
    private Property getProperty(String name) {
      Property prop = properties.get(name);
      if(prop == null) {
        prop = new Property(name);
        properties.put(name, prop);
      }
      return prop;
    }
    private void markObjLitPropertyCandidate(Node n, CompilerInput input) {
      stringNodes.add(n);
      getProperty(n.getString()).objLitCount++;
    }
    private void markPropertyAccessCandidate(Node n, CompilerInput input) {
      stringNodes.add(n);
      getProperty(n.getString()).refCount++;
    }
    private void markPrototypePropertyCandidate(Node n, CompilerInput input) {
      stringNodes.add(n);
      getProperty(n.getString()).prototypeCount++;
    }
    private void processPrototypeParent(Node n, CompilerInput input) {
      switch (n.getType()){
        case Token.GETPROP:
        case Token.GETELEM:
        Node var_1822 = n.getFirstChild();
        Node dest = var_1822.getNext();
        if(dest.isString()) {
          markPrototypePropertyCandidate(dest, input);
        }
        break ;
        case Token.ASSIGN:
        case Token.CALL:
        Node map;
        if(n.isAssign()) {
          map = n.getFirstChild().getNext();
        }
        else {
          map = n.getLastChild();
        }
        if(map.isObjectLit()) {
          prototypeObjLits.add(map);
          for(com.google.javascript.rhino.Node key = map.getFirstChild(); key != null; key = key.getNext()) {
            if(TokenStream.isJSIdentifier(key.getString())) {
              markPrototypePropertyCandidate(key, input);
            }
          }
        }
        break ;
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()){
        case Token.GETPROP:
        case Token.GETELEM:
        Node dest = n.getFirstChild().getNext();
        if(dest.isString()) {
          String s = dest.getString();
          if(s.equals("prototype")) {
            processPrototypeParent(parent, t.getInput());
          }
          else {
            markPropertyAccessCandidate(dest, t.getInput());
          }
        }
        break ;
        case Token.OBJECTLIT:
        if(!prototypeObjLits.contains(n)) {
          for(com.google.javascript.rhino.Node child = n.getFirstChild(); child != null; child = child.getNext()) {
            if(TokenStream.isJSIdentifier(child.getString())) {
              markObjLitPropertyCandidate(child, t.getInput());
            }
          }
        }
        break ;
      }
    }
  }
  
  private class Property  {
    String oldName;
    String newName;
    int prototypeCount;
    int objLitCount;
    int refCount;
    Property(String name) {
      super();
      this.oldName = name;
      this.newName = null;
      this.prototypeCount = 0;
      this.objLitCount = 0;
      this.refCount = 0;
    }
    boolean canRename() {
      if(this.prototypeCount > 0 && this.objLitCount == 0) {
        return canRenamePrototypeProperty();
      }
      if(this.objLitCount > 0 && this.prototypeCount == 0) {
        return canRenameObjLitProperty();
      }
      return canRenamePrototypeProperty() && canRenameObjLitProperty();
    }
    private boolean canRenameObjLitProperty() {
      if(compiler.getCodingConvention().isExported(oldName)) {
        return false;
      }
      if(compiler.getCodingConvention().isPrivate(oldName)) {
        return true;
      }
      return false;
    }
    private boolean canRenamePrototypeProperty() {
      if(compiler.getCodingConvention().isExported(oldName)) {
        return false;
      }
      if(compiler.getCodingConvention().isPrivate(oldName)) {
        return true;
      }
      if(aggressiveRenaming) {
        return true;
      }
      for(int i = 0, n = oldName.length(); i < n; i++) {
        char ch = oldName.charAt(i);
        if(Character.isUpperCase(ch) || !Character.isLetter(ch)) {
          return true;
        }
      }
      return false;
    }
    int count() {
      return prototypeCount + objLitCount + refCount;
    }
  }
}