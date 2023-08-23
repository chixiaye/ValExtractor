package com.google.javascript.jscomp.graph;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.graph.Annotation;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.jscomp.graph.SubGraph;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
abstract public class GraphColoring<E extends java.lang.Object, N extends java.lang.Object>  {
  protected N[] colorToNodeMap;
  final protected AdjacencyGraph<N, E> graph;
  public GraphColoring(AdjacencyGraph<N, E> graph) {
    super();
    this.graph = graph;
  }
  public AdjacencyGraph<N, E> getGraph() {
    return graph;
  }
  public N getPartitionSuperNode(N node) {
    Preconditions.checkNotNull(colorToNodeMap, "No coloring founded. color() should be called first.");
    Color color = graph.getNode(node).getAnnotation();
    N var_2182 = colorToNodeMap[color.value];
    N headNode = var_2182;
    if(headNode == null) {
      colorToNodeMap[color.value] = node;
      return node;
    }
    else {
      return headNode;
    }
  }
  abstract public int color();
  
  public static class Color implements Annotation  {
    int value = 0;
    Color(int value) {
      super();
      this.value = value;
    }
    @Override() public boolean equals(Object other) {
      if(!(other instanceof Color)) {
        return false;
      }
      else {
        return value == ((Color)other).value;
      }
    }
    @Override() public int hashCode() {
      return value;
    }
  }
  public static class GreedyGraphColoring<E extends java.lang.Object, N extends java.lang.Object> extends GraphColoring<N, E>  {
    final private Comparator<N> tieBreaker;
    public GreedyGraphColoring(AdjacencyGraph<N, E> graph) {
      this(graph, null);
    }
    public GreedyGraphColoring(AdjacencyGraph<N, E> graph, Comparator<N> tieBreaker) {
      super(graph);
      this.tieBreaker = tieBreaker;
    }
    @Override() public int color() {
      graph.clearNodeAnnotations();
      List<GraphNode<N, E>> worklist = Lists.newArrayList(graph.getNodes());
      Collections.sort(worklist, new Comparator<GraphNode<N, E>>() {
          @Override() public int compare(GraphNode<N, E> o1, GraphNode<N, E> o2) {
            int result = graph.getWeight(o2.getValue()) - graph.getWeight(o1.getValue());
            return result == 0 && tieBreaker != null ? tieBreaker.compare(o1.getValue(), o2.getValue()) : result;
          }
      });
      int count = 0;
      do {
        Color color = new Color(count);
        SubGraph<N, E> subgraph = graph.newSubGraph();
        for(java.util.Iterator<com.google.javascript.jscomp.graph.GraphNode<com.google.javascript.jscomp.graph.GraphColoring.GreedyGraphColoring@N, com.google.javascript.jscomp.graph.GraphColoring.GreedyGraphColoring@E>> i = worklist.iterator(); i.hasNext(); ) {
          GraphNode<N, E> node = i.next();
          if(subgraph.isIndependentOf(node.getValue())) {
            subgraph.addNode(node.getValue());
            node.setAnnotation(color);
            i.remove();
          }
        }
        count++;
      }while(!worklist.isEmpty());
      @SuppressWarnings(value = {"unchecked", }) N[] map = (N[])new Object[count];
      colorToNodeMap = map;
      return count;
    }
  }
}