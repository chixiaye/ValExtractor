package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.NodeTraversal.ScopedCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

final class RenameLabels implements CompilerPass  {
  final private AbstractCompiler compiler;
  final private Supplier<String> nameSupplier;
  final private boolean removeUnused;
  RenameLabels(AbstractCompiler compiler) {
    this(compiler, new DefaultNameSupplier(), true);
  }
  RenameLabels(AbstractCompiler compiler, Supplier<String> supplier, boolean removeUnused) {
    super();
    this.compiler = compiler;
    this.nameSupplier = supplier;
    this.removeUnused = removeUnused;
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, new ProcessLabels());
  }
  
  static class DefaultNameSupplier implements Supplier<String>  {
    final NameGenerator nameGenerator = new NameGenerator(new HashSet<String>(), "", null);
    @Override() public String get() {
      return nameGenerator.generateNextName();
    }
  }
  
  private static class LabelInfo  {
    boolean referenced = false;
    final int id;
    LabelInfo(int id) {
      super();
      this.id = id;
    }
  }
  
  private static class LabelNamespace  {
    final Map<String, LabelInfo> renameMap = new HashMap<String, LabelInfo>();
  }
  
  class ProcessLabels implements ScopedCallback  {
    final Deque<LabelNamespace> namespaceStack = Lists.newLinkedList();
    final ArrayList<String> names = new ArrayList<String>();
    ProcessLabels() {
      super();
      namespaceStack.push(new LabelNamespace());
    }
    LabelInfo getLabelInfo(String name) {
      return namespaceStack.peek().renameMap.get(name);
    }
    String getNameForId(int id) {
      return names.get(id - 1);
    }
    @Override() public boolean shouldTraverse(NodeTraversal nodeTraversal, Node node, Node parent) {
      if(node.isLabel()) {
        LabelNamespace current = namespaceStack.peek();
        Map<String, LabelInfo> var_189 = current.renameMap;
        int currentDepth = var_189.size() + 1;
        String name = node.getFirstChild().getString();
        LabelInfo li = new LabelInfo(currentDepth);
        Preconditions.checkState(!current.renameMap.containsKey(name));
        current.renameMap.put(name, li);
        if(names.size() < currentDepth) {
          names.add(nameSupplier.get());
        }
        String newName = getNameForId(currentDepth);
        compiler.addToDebugLog("label renamed: " + name + " => " + newName);
      }
      return true;
    }
    @Override() public void enterScope(NodeTraversal nodeTraversal) {
      namespaceStack.push(new LabelNamespace());
    }
    @Override() public void exitScope(NodeTraversal nodeTraversal) {
      namespaceStack.pop();
    }
    @Override() public void visit(NodeTraversal nodeTraversal, Node node, Node parent) {
      switch (node.getType()){
        case Token.LABEL:
        visitLabel(node, parent);
        break ;
        case Token.BREAK:
        case Token.CONTINUE:
        visitBreakOrContinue(node);
        break ;
      }
    }
    private void visitBreakOrContinue(Node node) {
      Node nameNode = node.getFirstChild();
      if(nameNode != null) {
        String name = nameNode.getString();
        Preconditions.checkState(name.length() != 0);
        LabelInfo li = getLabelInfo(name);
        if(li != null) {
          String newName = getNameForId(li.id);
          li.referenced = true;
          if(!name.equals(newName)) {
            nameNode.setString(newName);
            compiler.reportCodeChange();
          }
        }
      }
    }
    private void visitLabel(Node node, Node parent) {
      Node nameNode = node.getFirstChild();
      Preconditions.checkState(nameNode != null);
      String name = nameNode.getString();
      LabelInfo li = getLabelInfo(name);
      if(li.referenced || !removeUnused) {
        String newName = getNameForId(li.id);
        if(!name.equals(newName)) {
          nameNode.setString(newName);
          compiler.reportCodeChange();
        }
      }
      else {
        Node newChild = node.getLastChild();
        node.removeChild(newChild);
        parent.replaceChild(node, newChild);
        if(newChild.isBlock()) {
          NodeUtil.tryMergeBlock(newChild);
        }
        compiler.reportCodeChange();
      }
      namespaceStack.peek().renameMap.remove(name);
    }
  }
}