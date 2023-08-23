package com.google.javascript.jscomp;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.GraphvizGraph;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.jscomp.graph.GraphvizGraph.GraphvizEdge;
import com.google.javascript.jscomp.graph.GraphvizGraph.GraphvizNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DotFormatter  {
  final private static String INDENT = "  ";
  final private static String ARROW = " -> ";
  final private static String LINE = " -- ";
  private HashMap<Node, Integer> assignments = new HashMap<Node, Integer>();
  private int keyCount = 0;
  private Appendable builder;
  final private ControlFlowGraph<Node> cfg;
  final private boolean printAnnotations;
  private DotFormatter() {
    super();
    this.builder = new StringBuilder();
    this.cfg = null;
    this.printAnnotations = false;
  }
  private DotFormatter(Node n, ControlFlowGraph<Node> cfg, Appendable builder, boolean printAnnotations) throws IOException {
    super();
    this.cfg = cfg;
    this.builder = builder;
    this.printAnnotations = printAnnotations;
    formatPreamble();
    traverseNodes(n);
    formatConclusion();
  }
  static DotFormatter newInstanceForTesting() {
    return new DotFormatter();
  }
  private String formatNodeName(Integer key) {
    return "node" + key;
  }
  private String name(Node n) {
    int type = n.getType();
    switch (type){
      case Token.VOID:
      return "VOID";
      default:
      return Token.name(type);
    }
  }
  public static String toDot(GraphvizGraph graph) {
    StringBuilder builder = new StringBuilder();
    builder.append(graph.isDirected() ? "digraph" : "graph");
    builder.append(INDENT);
    builder.append(graph.getName());
    builder.append(" {\n");
    builder.append(INDENT);
    builder.append("node [color=lightblue2, style=filled];\n");
    final String edgeSymbol = graph.isDirected() ? ARROW : LINE;
    List<GraphvizNode> nodes = graph.getGraphvizNodes();
    String[] nodeNames = new String[nodes.size()];
    for(int i = 0; i < nodeNames.length; i++) {
      GraphvizNode gNode = nodes.get(i);
      nodeNames[i] = gNode.getId() + " [label=\"" + gNode.getLabel() + "\" color=\"" + gNode.getColor() + "\"]";
    }
    Arrays.sort(nodeNames);
    for (String nodeName : nodeNames) {
      builder.append(INDENT);
      builder.append(nodeName);
      StringBuilder var_187 = builder.append(";\n");
    }
    List<GraphvizEdge> edges = graph.getGraphvizEdges();
    String[] edgeNames = new String[edges.size()];
    for(int i = 0; i < edgeNames.length; i++) {
      GraphvizEdge edge = edges.get(i);
      edgeNames[i] = edge.getNode1Id() + edgeSymbol + edge.getNode2Id();
    }
    Arrays.sort(edgeNames);
    for (String edgeName : edgeNames) {
      builder.append(INDENT);
      builder.append(edgeName);
      builder.append(";\n");
    }
    builder.append("}\n");
    return builder.toString();
  }
  public static String toDot(Node n) throws IOException {
    return toDot(n, null);
  }
  static String toDot(Node n, ControlFlowGraph<Node> inCFG) throws IOException {
    StringBuilder builder = new StringBuilder();
    new DotFormatter(n, inCFG, builder, false);
    return builder.toString();
  }
  static String toDot(Node n, ControlFlowGraph<Node> inCFG, boolean printAnnotations) throws IOException {
    StringBuilder builder = new StringBuilder();
    new DotFormatter(n, inCFG, builder, printAnnotations);
    return builder.toString();
  }
  int key(Node n) throws IOException {
    Integer key = assignments.get(n);
    if(key == null) {
      key = keyCount++;
      assignments.put(n, key);
      builder.append(INDENT);
      builder.append(formatNodeName(key));
      builder.append(" [label=\"");
      builder.append(name(n));
      JSType type = n.getJSType();
      if(type != null) {
        builder.append(" : ");
        builder.append(type.toString());
      }
      if(printAnnotations && cfg != null && cfg.hasNode(n)) {
        Object annotation = cfg.getNode(n).getAnnotation();
        if(annotation != null) {
          builder.append("\\n");
          builder.append(annotation.toString());
        }
      }
      builder.append("\"");
      if(n.getJSDocInfo() != null) {
        builder.append(" color=\"green\"");
      }
      builder.append("];\n");
    }
    return key;
  }
  static void appendDot(Node n, ControlFlowGraph<Node> inCFG, Appendable builder) throws IOException {
    new DotFormatter(n, inCFG, builder, false);
  }
  private void formatConclusion() throws IOException {
    builder.append("}\n");
  }
  private void formatPreamble() throws IOException {
    builder.append("digraph AST {\n");
    builder.append(INDENT);
    builder.append("node [color=lightblue2, style=filled];\n");
  }
  private void traverseNodes(Node parent) throws IOException {
    int keyParent = key(parent);
    for(com.google.javascript.rhino.Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
      int keyChild = key(child);
      builder.append(INDENT);
      builder.append(formatNodeName(keyParent));
      builder.append(ARROW);
      builder.append(formatNodeName(keyChild));
      builder.append(" [weight=1];\n");
      traverseNodes(child);
    }
    if(cfg != null && cfg.hasNode(parent)) {
      List<DiGraphEdge<Node, Branch>> outEdges = cfg.getOutEdges(parent);
      String[] edgeList = new String[outEdges.size()];
      for(int i = 0; i < edgeList.length; i++) {
        DiGraphEdge<Node, ControlFlowGraph.Branch> edge = outEdges.get(i);
        DiGraphNode<Node, Branch> succ = edge.getDestination();
        String toNode = null;
        if(succ == cfg.getImplicitReturn()) {
          toNode = "RETURN";
        }
        else {
          int keySucc = key(succ.getValue());
          toNode = formatNodeName(keySucc);
        }
        edgeList[i] = formatNodeName(keyParent) + ARROW + toNode + " [label=\"" + edge.getValue().toString() + "\", " + "fontcolor=\"red\", " + "weight=0.01, color=\"red\"];\n";
      }
      Arrays.sort(edgeList);
      for(int i = 0; i < edgeList.length; i++) {
        builder.append(INDENT);
        builder.append(edgeList[i]);
      }
    }
  }
}