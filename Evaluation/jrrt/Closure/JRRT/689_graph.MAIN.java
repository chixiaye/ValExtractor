package com.google.javascript.jscomp;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NameReferenceGraph.Name;
import com.google.javascript.jscomp.NameReferenceGraph.Reference;
import com.google.javascript.jscomp.graph.FixedPointGraphTraversal;
import com.google.javascript.jscomp.graph.Annotation;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.jscomp.graph.FixedPointGraphTraversal.EdgeCallback;
import com.google.javascript.rhino.Node;

class AnalyzeNameReferences implements CompilerPass  {
  private NameReferenceGraph graph;
  final private JSModuleGraph moduleGraph;
  final private AbstractCompiler compiler;
  AnalyzeNameReferences(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
    this.moduleGraph = compiler.getModuleGraph();
  }
  private NameInfo getInfo(Name symbol) {
    GraphNode<Name, Reference> name = graph.getNode(symbol);
    NameInfo info = name.getAnnotation();
    if(info == null) {
      info = new NameInfo();
      name.setAnnotation(info);
    }
    return info;
  }
  public NameReferenceGraph getGraph() {
    return graph;
  }
  @Override() public void process(Node externs, Node root) {
    NameReferenceGraphConstruction gc = new NameReferenceGraphConstruction(compiler);
    gc.process(externs, root);
    graph = gc.getNameReferenceGraph();
    FixedPointGraphTraversal<Name, Reference> t = FixedPointGraphTraversal.newTraversal(new PropagateReferences());
    Name var_689 = graph.MAIN;
    getInfo(var_689).markReference(null);
    t.computeFixedPoint(graph, Sets.newHashSet(graph.MAIN));
  }
  
  final class NameInfo implements Annotation  {
    private boolean referenced = false;
    private JSModule deepestCommonModuleRef = null;
    JSModule getDeepestCommonModuleRef() {
      return deepestCommonModuleRef;
    }
    boolean isReferenced() {
      return referenced;
    }
    boolean markReference(JSModule module) {
      boolean hasChanged = false;
      if(!referenced) {
        referenced = true;
        hasChanged = true;
      }
      if(moduleGraph != null) {
        JSModule originalDeepestCommon = deepestCommonModuleRef;
        if(deepestCommonModuleRef == null) {
          deepestCommonModuleRef = module;
        }
        else {
          deepestCommonModuleRef = moduleGraph.getDeepestCommonDependencyInclusive(deepestCommonModuleRef, module);
        }
        if(originalDeepestCommon != deepestCommonModuleRef) {
          hasChanged = true;
        }
      }
      return hasChanged;
    }
  }
  
  private class PropagateReferences implements EdgeCallback<Name, Reference>  {
    @Override() public boolean traverseEdge(Name start, Reference edge, Name dest) {
      NameInfo startInfo = getInfo(start);
      NameInfo destInfo = getInfo(dest);
      if(startInfo.isReferenced()) {
        JSModule startModule = startInfo.getDeepestCommonModuleRef();
        if(startModule != null && moduleGraph.dependsOn(startModule, edge.getModule())) {
          return destInfo.markReference(startModule);
        }
        else {
          return destInfo.markReference(edge.getModule());
        }
      }
      return false;
    }
  }
}