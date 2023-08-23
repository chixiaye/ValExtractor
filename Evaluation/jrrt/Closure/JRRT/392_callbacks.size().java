package com.google.javascript.jscomp;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.jscomp.NodeTraversal.ScopedCallback;
import com.google.javascript.rhino.Node;
import java.util.List;

final class CombinedCompilerPass implements HotSwapCompilerPass, ScopedCallback  {
  final private CallbackWrapper[] callbacks;
  final private AbstractCompiler compiler;
  CombinedCompilerPass(AbstractCompiler compiler, Callback ... callbacks) {
    this(compiler, Lists.<Callback>newArrayList(callbacks));
  }
  CombinedCompilerPass(AbstractCompiler compiler, List<Callback> callbacks) {
    super();
    this.compiler = compiler;
    int var_392 = callbacks.size();
    this.callbacks = new CallbackWrapper[var_392];
    for(int i = 0; i < callbacks.size(); i++) {
      this.callbacks[i] = new CallbackWrapper(callbacks.get(i));
    }
  }
  @Override() public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
    for (CallbackWrapper callback : callbacks) {
      callback.shouldTraverseIfActive(t, n, parent);
    }
    return true;
  }
  @Override() public void enterScope(NodeTraversal t) {
    for (CallbackWrapper callback : callbacks) {
      callback.enterScopeIfActive(t);
    }
  }
  @Override() public void exitScope(NodeTraversal t) {
    for (CallbackWrapper callback : callbacks) {
      callback.exitScopeIfActive(t);
    }
  }
  @Override() public void hotSwapScript(Node scriptRoot, Node originalRoot) {
    NodeTraversal.traverse(compiler, scriptRoot, this);
  }
  @Override() final public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, this);
  }
  static void traverse(AbstractCompiler compiler, Node root, List<Callback> callbacks) {
    if(callbacks.size() == 1) {
      NodeTraversal.traverse(compiler, root, callbacks.get(0));
    }
    else {
      (new CombinedCompilerPass(compiler, callbacks)).process(null, root);
    }
  }
  @Override() public void visit(NodeTraversal t, Node n, Node parent) {
    for (CallbackWrapper callback : callbacks) {
      callback.visitOrMaybeActivate(t, n, parent);
    }
  }
  
  private static class CallbackWrapper  {
    final private Callback callback;
    final private ScopedCallback scopedCallback;
    private Node waiting = null;
    private CallbackWrapper(Callback callback) {
      super();
      this.callback = callback;
      if(callback instanceof ScopedCallback) {
        scopedCallback = (ScopedCallback)callback;
      }
      else {
        scopedCallback = null;
      }
    }
    boolean isActive() {
      return waiting == null;
    }
    void enterScopeIfActive(NodeTraversal t) {
      if(isActive() && scopedCallback != null) {
        scopedCallback.enterScope(t);
      }
    }
    void exitScopeIfActive(NodeTraversal t) {
      if(isActive() && scopedCallback != null) {
        scopedCallback.exitScope(t);
      }
    }
    void shouldTraverseIfActive(NodeTraversal t, Node n, Node parent) {
      if(isActive() && !callback.shouldTraverse(t, n, parent)) {
        waiting = n;
      }
    }
    void visitOrMaybeActivate(NodeTraversal t, Node n, Node parent) {
      if(isActive()) {
        callback.visit(t, n, parent);
      }
      else 
        if(waiting == n) {
          waiting = null;
        }
    }
  }
}