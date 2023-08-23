package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.NameReferenceGraph.Name;
import com.google.javascript.jscomp.NameReferenceGraph.Reference;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class NameReferenceGraphReport  {
  private NameReferenceGraph graph = null;
  NameReferenceGraphReport(NameReferenceGraph g) {
    super();
    this.graph = g;
  }
  public String getHtmlReport() {
    StringBuilder builder = new StringBuilder();
    List<DiGraphNode<Name, Reference>> nodes = Lists.newArrayList(graph.getDirectedGraphNodes());
    generateHtmlReportHeader(builder);
    builder.append("<h1>Name Reference Graph Dump</h1>\n");
    builder.append("OVERALL STATS\n");
    builder.append("<ul>\n");
    builder.append("<li>Total names: " + nodes.size());
    builder.append("</ul>\n");
    builder.append("ALL NAMES\n");
    builder.append("<UL>\n");
    Collections.sort(nodes, new DiGraphNodeComparator());
    for (DiGraphNode<Name, Reference> n : nodes) {
      generateDeclarationReport(builder, n);
      List<DiGraphEdge<Name, Reference>> outEdges = graph.getOutEdges(n.getValue());
      List<DiGraphEdge<Name, Reference>> inEdges = graph.getInEdges(n.getValue());
      if(!outEdges.isEmpty() || !inEdges.isEmpty()) {
        builder.append("<ul>");
        if(outEdges.size() > 0) {
          builder.append("<li>REFERS TO:<br>\n");
          builder.append("<ul>");
          for (DiGraphEdge<Name, Reference> edge : outEdges) {
            generateEdgeReport(builder, edge.getDestination().getValue(), edge);
          }
          builder.append("</ul>\n");
        }
        if(inEdges.size() > 0) {
          builder.append("<li>REFERENCED BY:<br>\n");
          builder.append("<ul>");
          for (DiGraphEdge<Name, Reference> edge : inEdges) {
            generateEdgeReport(builder, edge.getSource().getValue(), edge);
          }
          builder.append("</ul>");
        }
        builder.append("</ul>\n");
      }
    }
    builder.append("</ul>\n");
    generateHtmlReportFooter(builder);
    return builder.toString();
  }
  private String getSourceFile(Node node) {
    String filename = node.getSourceFileName();
    if(filename == null) {
      return "";
    }
    return filename;
  }
  private void generateDeclarationReport(StringBuilder builder, DiGraphNode<Name, Reference> declarationNode) {
    String declName = declarationNode.getValue().getQualifiedName();
    JSType declType = declarationNode.getValue().getType();
    builder.append("<LI> ");
    builder.append("<A NAME=\"" + declName + "\">");
    builder.append(declName);
    builder.append("\n");
    generateType(builder, declType);
    List<DefinitionsRemover.Definition> defs = declarationNode.getValue().getDeclarations();
    if(defs.size() == 0) {
      builder.append("<br>No definitions found<br>");
    }
    else {
      builder.append("<ul>");
      for (DefinitionsRemover.Definition def : defs) {
        Node fnDef = def.getRValue();
        String sourceFileName = getSourceFile(fnDef);
        builder.append("<li> Defined: ");
        generateSourceReferenceLink(builder, sourceFileName, fnDef.getLineno(), fnDef.getCharno());
      }
      builder.append("</ul>");
    }
  }
  private void generateEdgeReport(StringBuilder builder, Name referencedDecl, DiGraphEdge<Name, Reference> edge) {
    String srcDeclName = referencedDecl.getQualifiedName();
    builder.append("<li><A HREF=\"#" + srcDeclName + "\">");
    builder.append(srcDeclName);
    builder.append("</a> ");
    Node def = edge.getValue().getSite();
    int lineNumber = def.getLineno();
    int columnNumber = def.getCharno();
    String sourceFile = getSourceFile(def);
    generateSourceReferenceLink(builder, sourceFile, lineNumber, columnNumber);
    JSType defType = edge.getValue().getSite().getJSType();
    generateType(builder, defType);
  }
  private void generateHtmlReportFooter(StringBuilder builder) {
    builder.append("</body></html>");
  }
  private void generateHtmlReportHeader(StringBuilder builder) {
    builder.append("<!DOCTYPE html>\n" + "<html>" + "<head>" + "<meta http-equiv=\"Content-Type\" " + "content=\"text/html;charset=utf-8\" >" + "<title>Name Reference Graph Dump</title>" + "<style type=\"text/css\">body, td, ");
    builder.append("p {font-family: Arial; font-size: 83%} ");
    builder.append("ul {margin-top:2px; margin-left:0px; padding-left:1em;}");
    builder.append("li {margin-top:3px; margin-left:24px;" + "padding-left:0px;padding-bottom: 4px}");
    builder.append("</style></head><body>\n");
  }
  private void generateSourceReferenceLink(StringBuilder builder, String sourceFile, int lineNumber, int columnNumber) {
    assert (sourceFile != null);
    builder.append("(");
    builder.append(sourceFile + ":" + lineNumber + "," + columnNumber);
    builder.append(")");
  }
  private void generateType(StringBuilder builder, JSType defType) {
    if(defType == null) {
      builder.append(" (type: null) ");
    }
    else 
      if(defType.isUnknownType()) {
        builder.append(" (type: unknown) ");
      }
      else {
        builder.append(" (type: " + defType.toString() + ") ");
      }
  }
  
  class DiGraphNodeComparator implements Comparator<DiGraphNode<Name, Reference>>  {
    @Override() public int compare(DiGraphNode<Name, Reference> node1, DiGraphNode<Name, Reference> node2) {
      Preconditions.checkNotNull(node1.getValue());
      Preconditions.checkNotNull(node2.getValue());
      if((node1.getValue().getQualifiedName() == null) && (node2.getValue().getQualifiedName() == null)) {
        return 0;
      }
      if(node1.getValue().getQualifiedName() == null) {
        return -1;
      }
      String var_1739 = node2.getValue().getQualifiedName();
      if(var_1739 == null) {
        return 1;
      }
      return node1.getValue().getQualifiedName().compareTo(node2.getValue().getQualifiedName());
    }
  }
}