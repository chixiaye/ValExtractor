package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import java.util.ArrayList;

class PeepholeOptimizationsPass implements CompilerPass  {
  private AbstractCompiler compiler;
  final private AbstractPeepholeOptimization[] peepholeOptimizations;
  private StateStack traversalState = new StateStack();
  private boolean retraverseOnChange = true;
  PeepholeOptimizationsPass(AbstractCompiler compiler, AbstractPeepholeOptimization ... optimizations) {
    super();
    this.compiler = compiler;
    this.peepholeOptimizations = optimizations;
  }
  public AbstractCompiler getCompiler() {
    return compiler;
  }
  PeepholeOptimizationsPass setRetraverseOnChange(boolean retraverse) {
    this.retraverseOnChange = retraverse;
    return this;
  }
  private boolean shouldRetraverse(Node node) {
    if(retraverseOnChange && node.getParent() != null && (node.isFunction() || node.isScript())) {
      ScopeState state = traversalState.peek();
      boolean var_744 = state.changed;
      if(var_744) {
        state.changed = false;
        state.traverseChildScopes = false;
        return true;
      }
    }
    return false;
  }
  private boolean shouldVisit(Node node) {
    if(node.isFunction() || node.isScript()) {
      ScopeState previous = traversalState.peek();
      if(!previous.traverseChildScopes) {
        return false;
      }
      traversalState.push();
    }
    return true;
  }
  private void beginTraversal() {
    for (AbstractPeepholeOptimization optimization : peepholeOptimizations) {
      optimization.beginTraversal(compiler);
    }
  }
  private void endTraversal() {
    for (AbstractPeepholeOptimization optimization : peepholeOptimizations) {
      optimization.endTraversal(compiler);
    }
  }
  private void exitNode(Node node) {
    if(node.isFunction() || node.isScript()) {
      traversalState.pop();
    }
  }
  @Override() public void process(Node externs, Node root) {
    PeepholeChangeHandler handler = new PeepholeChangeHandler();
    compiler.addChangeHandler(handler);
    beginTraversal();
    traverse(root);
    endTraversal();
    compiler.removeChangeHandler(handler);
  }
  private void traverse(Node node) {
    if(!shouldVisit(node)) {
      return ;
    }
    int visits = 0;
    do {
      Node c = node.getFirstChild();
      while(c != null){
        Node next = c.getNext();
        traverse(c);
        c = next;
      }
      visit(node);
      visits++;
      Preconditions.checkState(visits < 10000, "too many interations");
    }while(shouldRetraverse(node));
    exitNode(node);
  }
  public void visit(Node n) {
    Node currentVersionOfNode = n;
    boolean somethingChanged = false;
    do {
      somethingChanged = false;
      for (AbstractPeepholeOptimization optimization : peepholeOptimizations) {
        Node newVersionOfNode = optimization.optimizeSubtree(currentVersionOfNode);
        if(newVersionOfNode != currentVersionOfNode) {
          somethingChanged = true;
          currentVersionOfNode = newVersionOfNode;
        }
        if(currentVersionOfNode == null) {
          return ;
        }
      }
    }while(somethingChanged);
  }
  
  private class PeepholeChangeHandler implements CodeChangeHandler  {
    @Override() public void reportChange() {
      traversalState.peek().changed = true;
    }
  }
  
  private static class ScopeState  {
    boolean changed;
    boolean traverseChildScopes;
    ScopeState() {
      super();
      reset();
    }
    void reset() {
      changed = false;
      traverseChildScopes = true;
    }
  }
  
  private static class StateStack  {
    private ArrayList<ScopeState> states = Lists.newArrayList();
    private int currentDepth = 0;
    StateStack() {
      super();
      states.add(new ScopeState());
    }
    ScopeState peek() {
      return states.get(currentDepth);
    }
    void pop() {
      currentDepth--;
    }
    void push() {
      currentDepth++;
      if(states.size() <= currentDepth) {
        states.add(new ScopeState());
      }
      else {
        states.get(currentDepth).reset();
      }
    }
  }
}