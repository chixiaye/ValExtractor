package com.google.javascript.jscomp;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.graph.Annotation;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.jscomp.graph.LatticeElement;
import com.google.javascript.rhino.Node;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
abstract class DataFlowAnalysis<L extends com.google.javascript.jscomp.graph.LatticeElement, N extends java.lang.Object>  {
  final private ControlFlowGraph<N> cfg;
  final JoinOp<L> joinOp;
  final protected Set<DiGraphNode<N, Branch>> orderedWorkSet;
  final public static int MAX_STEPS = 200000;
  DataFlowAnalysis(ControlFlowGraph<N> targetCfg, JoinOp<L> joinOp) {
    super();
    this.cfg = targetCfg;
    this.joinOp = joinOp;
    Comparator<DiGraphNode<N, Branch>> nodeComparator = cfg.getOptionalNodeComparator(isForward());
    if(nodeComparator != null) {
      this.orderedWorkSet = Sets.newTreeSet(nodeComparator);
    }
    else {
      this.orderedWorkSet = Sets.newLinkedHashSet();
    }
  }
  final ControlFlowGraph<N> getCfg() {
    return cfg;
  }
  abstract L createEntryLattice();
  abstract L createInitialEstimateLattice();
  abstract L flowThrough(N node, L input);
  L getExitLatticeElement() {
    DiGraphNode<N, Branch> node = getCfg().getImplicitReturn();
    FlowState<L> state = node.getAnnotation();
    return state.getIn();
  }
  @SuppressWarnings(value = {"unchecked", }) protected L join(L latticeA, L latticeB) {
    return joinOp.apply(Lists.<L>newArrayList(latticeA, latticeB));
  }
  protected boolean flow(DiGraphNode<N, Branch> node) {
    FlowState<L> state = node.getAnnotation();
    if(isForward()) {
      L outBefore = state.out;
      N var_748 = node.getValue();
      state.out = flowThrough(var_748, state.in);
      return !outBefore.equals(state.out);
    }
    else {
      L inBefore = state.in;
      state.in = flowThrough(node.getValue(), state.out);
      return !inBefore.equals(state.in);
    }
  }
  abstract boolean isForward();
  final void analyze() {
    analyze(MAX_STEPS);
  }
  final void analyze(int maxSteps) {
    initialize();
    int step = 0;
    while(!orderedWorkSet.isEmpty()){
      if(step > maxSteps) {
        throw new MaxIterationsExceededException("Analysis did not terminate after " + maxSteps + " iterations");
      }
      DiGraphNode<N, Branch> curNode = orderedWorkSet.iterator().next();
      orderedWorkSet.remove(curNode);
      joinInputs(curNode);
      if(flow(curNode)) {
        List<DiGraphNode<N, Branch>> nextNodes = isForward() ? cfg.getDirectedSuccNodes(curNode) : cfg.getDirectedPredNodes(curNode);
        for (DiGraphNode<N, Branch> nextNode : nextNodes) {
          if(nextNode != cfg.getImplicitReturn()) {
            orderedWorkSet.add(nextNode);
          }
        }
      }
      step++;
    }
    if(isForward()) {
      joinInputs(getCfg().getImplicitReturn());
    }
  }
  static void computeEscaped(final Scope jsScope, final Set<Var> escaped, AbstractCompiler compiler) {
    AbstractPostOrderCallback finder = new AbstractPostOrderCallback() {
        @Override() public void visit(NodeTraversal t, Node n, Node parent) {
          if(jsScope == t.getScope() || !n.isName() || parent.isFunction()) {
            return ;
          }
          String name = n.getString();
          Var var = t.getScope().getVar(name);
          if(var != null && var.scope == jsScope) {
            escaped.add(jsScope.getVar(name));
          }
        }
    };
    NodeTraversal t = new NodeTraversal(compiler, finder);
    t.traverseAtScope(jsScope);
    for(java.util.Iterator<com.google.javascript.jscomp.Scope.Var> i = jsScope.getVars(); i.hasNext(); ) {
      Var var = i.next();
      if(var.getParentNode().isCatch() || compiler.getCodingConvention().isExported(var.getName())) {
        escaped.add(var);
      }
    }
  }
  protected void initialize() {
    orderedWorkSet.clear();
    for (DiGraphNode<N, Branch> node : cfg.getDirectedGraphNodes()) {
      node.setAnnotation(new FlowState<L>(createInitialEstimateLattice(), createInitialEstimateLattice()));
      if(node != cfg.getImplicitReturn()) {
        orderedWorkSet.add(node);
      }
    }
  }
  protected void joinInputs(DiGraphNode<N, Branch> node) {
    FlowState<L> state = node.getAnnotation();
    if(isForward()) {
      if(cfg.getEntry() == node) {
        state.setIn(createEntryLattice());
      }
      else {
        List<DiGraphNode<N, Branch>> inNodes = cfg.getDirectedPredNodes(node);
        if(inNodes.size() == 1) {
          FlowState<L> inNodeState = inNodes.get(0).getAnnotation();
          state.setIn(inNodeState.getOut());
        }
        else 
          if(inNodes.size() > 1) {
            List<L> values = new ArrayList<L>(inNodes.size());
            for (DiGraphNode<N, Branch> currentNode : inNodes) {
              FlowState<L> currentNodeState = currentNode.getAnnotation();
              values.add(currentNodeState.getOut());
            }
            state.setIn(joinOp.apply(values));
          }
      }
    }
    else {
      List<DiGraphNode<N, Branch>> inNodes = cfg.getDirectedSuccNodes(node);
      if(inNodes.size() == 1) {
        DiGraphNode<N, Branch> inNode = inNodes.get(0);
        if(inNode == cfg.getImplicitReturn()) {
          state.setOut(createEntryLattice());
        }
        else {
          FlowState<L> inNodeState = inNode.getAnnotation();
          state.setOut(inNodeState.getIn());
        }
      }
      else 
        if(inNodes.size() > 1) {
          List<L> values = new ArrayList<L>(inNodes.size());
          for (DiGraphNode<N, Branch> currentNode : inNodes) {
            FlowState<L> currentNodeState = currentNode.getAnnotation();
            values.add(currentNodeState.getIn());
          }
          state.setOut(joinOp.apply(values));
        }
    }
  }
  static class BranchedFlowState<L extends com.google.javascript.jscomp.graph.LatticeElement> implements Annotation  {
    private L in;
    private List<L> out;
    private BranchedFlowState(L inState, List<L> outState) {
      super();
      Preconditions.checkNotNull(inState);
      Preconditions.checkNotNull(outState);
      this.in = inState;
      this.out = outState;
    }
    L getIn() {
      return in;
    }
    List<L> getOut() {
      return out;
    }
    @Override() public String toString() {
      return String.format("IN: %s OUT: %s", in, out);
    }
    @Override() public int hashCode() {
      return Objects.hashCode(in, out);
    }
    void setIn(L in) {
      Preconditions.checkNotNull(in);
      this.in = in;
    }
    void setOut(List<L> out) {
      Preconditions.checkNotNull(out);
      for (L item : out) {
        Preconditions.checkNotNull(item);
      }
      this.out = out;
    }
  }
  abstract static class BranchedForwardDataFlowAnalysis<L extends com.google.javascript.jscomp.graph.LatticeElement, N extends java.lang.Object> extends DataFlowAnalysis<N, L>  {
    BranchedForwardDataFlowAnalysis(ControlFlowGraph<N> targetCfg, JoinOp<L> joinOp) {
      super(targetCfg, joinOp);
    }
    @Override() L getExitLatticeElement() {
      DiGraphNode<N, Branch> node = getCfg().getImplicitReturn();
      BranchedFlowState<L> state = node.getAnnotation();
      return state.getIn();
    }
    abstract List<L> branchedFlowThrough(N node, L input);
    @Override() final protected boolean flow(DiGraphNode<N, Branch> node) {
      BranchedFlowState<L> state = node.getAnnotation();
      List<L> outBefore = state.out;
      state.out = branchedFlowThrough(node.getValue(), state.in);
      Preconditions.checkState(outBefore.size() == state.out.size());
      for(int i = 0; i < outBefore.size(); i++) {
        if(!outBefore.get(i).equals(state.out.get(i))) {
          return true;
        }
      }
      return false;
    }
    @Override() final boolean isForward() {
      return true;
    }
    @Override() protected void initialize() {
      orderedWorkSet.clear();
      for (DiGraphNode<N, Branch> node : getCfg().getDirectedGraphNodes()) {
        int outEdgeCount = getCfg().getOutEdges(node.getValue()).size();
        List<L> outLattices = Lists.newArrayList();
        for(int i = 0; i < outEdgeCount; i++) {
          outLattices.add(createInitialEstimateLattice());
        }
        node.setAnnotation(new BranchedFlowState<L>(createInitialEstimateLattice(), outLattices));
        if(node != getCfg().getImplicitReturn()) {
          orderedWorkSet.add(node);
        }
      }
    }
    @Override() protected void joinInputs(DiGraphNode<N, Branch> node) {
      BranchedFlowState<L> state = node.getAnnotation();
      List<DiGraphNode<N, Branch>> predNodes = getCfg().getDirectedPredNodes(node);
      List<L> values = new ArrayList<L>(predNodes.size());
      for (DiGraphNode<N, Branch> predNode : predNodes) {
        BranchedFlowState<L> predNodeState = predNode.getAnnotation();
        L in = predNodeState.out.get(getCfg().getDirectedSuccNodes(predNode).indexOf(node));
        values.add(in);
      }
      if(getCfg().getEntry() == node) {
        state.setIn(createEntryLattice());
      }
      else 
        if(!values.isEmpty()) {
          state.setIn(joinOp.apply(values));
        }
    }
  }
  static class FlowState<L extends com.google.javascript.jscomp.graph.LatticeElement> implements Annotation  {
    private L in;
    private L out;
    private FlowState(L inState, L outState) {
      super();
      Preconditions.checkNotNull(inState);
      Preconditions.checkNotNull(outState);
      this.in = inState;
      this.out = outState;
    }
    L getIn() {
      return in;
    }
    L getOut() {
      return out;
    }
    @Override() public String toString() {
      return String.format("IN: %s OUT: %s", in, out);
    }
    @Override() public int hashCode() {
      return Objects.hashCode(in, out);
    }
    void setIn(L in) {
      Preconditions.checkNotNull(in);
      this.in = in;
    }
    void setOut(L out) {
      Preconditions.checkNotNull(out);
      this.out = out;
    }
  }
  
  static class MaxIterationsExceededException extends RuntimeException  {
    final private static long serialVersionUID = 1L;
    MaxIterationsExceededException(String msg) {
      super(msg);
    }
  }
}