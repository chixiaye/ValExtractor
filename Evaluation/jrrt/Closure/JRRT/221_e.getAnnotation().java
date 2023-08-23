package com.google.javascript.jscomp;
import com.google.common.base.Predicate;
import com.google.javascript.jscomp.graph.Annotation;
import com.google.javascript.jscomp.graph.DiGraph;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
class CheckPathsBetweenNodes<E extends java.lang.Object, N extends java.lang.Object>  {
  final private Predicate<N> nodePredicate;
  final private Predicate<DiGraphEdge<N, E>> edgePredicate;
  final private boolean inclusive;
  final private static Annotation BACK_EDGE = new Annotation() {
  };
  final private static Annotation VISITED_EDGE = new Annotation() {
  };
  final private static Annotation WHITE = null;
  final private static Annotation GRAY = new Annotation() {
  };
  final private static Annotation BLACK = new Annotation() {
  };
  final private DiGraph<N, E> graph;
  final private DiGraphNode<N, E> start;
  final private DiGraphNode<N, E> end;
  CheckPathsBetweenNodes(DiGraph<N, E> graph, DiGraphNode<N, E> a, DiGraphNode<N, E> b, Predicate<N> nodePredicate, Predicate<DiGraphEdge<N, E>> edgePredicate) {
    this(graph, a, b, nodePredicate, edgePredicate, true);
  }
  CheckPathsBetweenNodes(DiGraph<N, E> graph, DiGraphNode<N, E> a, DiGraphNode<N, E> b, Predicate<N> nodePredicate, Predicate<DiGraphEdge<N, E>> edgePredicate, boolean inclusive) {
    super();
    this.graph = graph;
    this.start = a;
    this.end = b;
    this.nodePredicate = nodePredicate;
    this.edgePredicate = edgePredicate;
    this.inclusive = inclusive;
  }
  public boolean allPathsSatisfyPredicate() {
    setUp();
    boolean result = checkAllPathsWithoutBackEdges(start, end);
    tearDown();
    return result;
  }
  private boolean checkAllPathsWithoutBackEdges(DiGraphNode<N, E> a, DiGraphNode<N, E> b) {
    if(nodePredicate.apply(a.getValue()) && (inclusive || (a != start && a != end))) {
      return true;
    }
    if(a == b) {
      return false;
    }
    for (DiGraphEdge<N, E> e : a.getOutEdges()) {
      if(e.getAnnotation() == VISITED_EDGE) {
        continue ;
      }
      e.setAnnotation(VISITED_EDGE);
      if(ignoreEdge(e)) {
        continue ;
      }
      if(e.getAnnotation() == BACK_EDGE) {
        continue ;
      }
      DiGraphNode<N, E> next = e.getDestination();
      if(!checkAllPathsWithoutBackEdges(next, b)) {
        return false;
      }
    }
    return true;
  }
  private boolean checkSomePathsWithoutBackEdges(DiGraphNode<N, E> a, DiGraphNode<N, E> b) {
    if(nodePredicate.apply(a.getValue()) && (inclusive || (a != start && a != end))) {
      return true;
    }
    if(a == b) {
      return false;
    }
    for (DiGraphEdge<N, E> e : a.getOutEdges()) {
      Annotation var_221 = e.getAnnotation();
      if(var_221 == VISITED_EDGE) {
        continue ;
      }
      e.setAnnotation(VISITED_EDGE);
      if(ignoreEdge(e)) {
        continue ;
      }
      if(e.getAnnotation() == BACK_EDGE) {
        continue ;
      }
      DiGraphNode<N, E> next = e.getDestination();
      if(checkSomePathsWithoutBackEdges(next, b)) {
        return true;
      }
    }
    return false;
  }
  private boolean ignoreEdge(DiGraphEdge<N, E> e) {
    return !edgePredicate.apply(e);
  }
  public boolean somePathsSatisfyPredicate() {
    setUp();
    boolean result = checkSomePathsWithoutBackEdges(start, end);
    tearDown();
    return result;
  }
  private void discoverBackEdges(DiGraphNode<N, E> u) {
    u.setAnnotation(GRAY);
    for (DiGraphEdge<N, E> e : u.getOutEdges()) {
      if(ignoreEdge(e)) {
        continue ;
      }
      DiGraphNode<N, E> v = e.getDestination();
      if(v.getAnnotation() == WHITE) {
        discoverBackEdges(v);
      }
      else 
        if(v.getAnnotation() == GRAY) {
          e.setAnnotation(BACK_EDGE);
        }
    }
    u.setAnnotation(BLACK);
  }
  private void setUp() {
    graph.pushNodeAnnotations();
    graph.pushEdgeAnnotations();
    discoverBackEdges(this.start);
  }
  private void tearDown() {
    graph.popNodeAnnotations();
    graph.popEdgeAnnotations();
  }
}