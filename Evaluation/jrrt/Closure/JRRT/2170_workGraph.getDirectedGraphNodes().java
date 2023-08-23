package com.google.javascript.jscomp.graph;
import com.google.common.base.Predicate;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
public class GraphPruner<E extends java.lang.Object, N extends java.lang.Object>  {
  final private DiGraph<N, E> graph;
  public GraphPruner(DiGraph<N, E> graph) {
    super();
    this.graph = graph;
  }
  private static  <E extends java.lang.Object, N extends java.lang.Object> LinkedDirectedGraph<N, E> cloneGraph(DiGraph<N, E> graph) {
    LinkedDirectedGraph<N, E> newGraph = LinkedDirectedGraph.create();
    for (DiGraphNode<N, E> node : graph.getDirectedGraphNodes()) {
      newGraph.createNode(node.getValue());
      for (DiGraphEdge<N, E> outEdge : node.getOutEdges()) {
        N dest = outEdge.getDestination().getValue();
        newGraph.createNode(dest);
        newGraph.connect(node.getValue(), outEdge.getValue(), dest);
      }
    }
    return newGraph;
  }
  public LinkedDirectedGraph<N, E> prune(Predicate<N> keep) {
    LinkedDirectedGraph<N, E> workGraph = cloneGraph(graph);
    Iterable<DiGraphNode<N, E>> var_2170 = workGraph.getDirectedGraphNodes();
    for (DiGraphNode<N, E> node : var_2170) {
      for (DiGraphEdge<N, E> inEdge : node.getInEdges()) {
        for (DiGraphEdge<N, E> outEdge : node.getOutEdges()) {
          N source = inEdge.getSource().getValue();
          N dest = outEdge.getDestination().getValue();
          if(!workGraph.isConnectedInDirection(source, dest)) {
            workGraph.connect(source, outEdge.getValue(), dest);
          }
        }
      }
    }
    LinkedDirectedGraph<N, E> resultGraph = LinkedDirectedGraph.create();
    for (DiGraphNode<N, E> node : workGraph.getDirectedGraphNodes()) {
      if(keep.apply(node.getValue())) {
        resultGraph.createNode(node.getValue());
        for (DiGraphEdge<N, E> outEdge : node.getOutEdges()) {
          N source = node.getValue();
          N dest = outEdge.getDestination().getValue();
          if(keep.apply(dest)) {
            resultGraph.createNode(dest);
            if(source != dest && !resultGraph.isConnectedInDirection(source, dest)) {
              resultGraph.connect(source, outEdge.getValue(), dest);
            }
          }
        }
      }
    }
    return resultGraph;
  }
}