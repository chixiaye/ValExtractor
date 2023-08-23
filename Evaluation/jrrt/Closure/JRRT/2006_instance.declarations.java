package com.google.javascript.jscomp;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.util.LinkedList;
import java.util.List;

class ExtractPrototypeMemberDeclarations implements CompilerPass  {
  private String prototypeAlias = "JSCompiler_prototypeAlias";
  final private AbstractCompiler compiler;
  final private Pattern pattern;
  ExtractPrototypeMemberDeclarations(AbstractCompiler compiler, Pattern pattern) {
    super();
    this.compiler = compiler;
    this.pattern = pattern;
  }
  private void doExtraction(GatherExtractionInfo info) {
    if(pattern == Pattern.USE_GLOBAL_TEMP) {
      Node injectionPoint = compiler.getNodeForCodeInsertion(null);
      Node var = NodeUtil.newVarNode(prototypeAlias, null).copyInformationFromForTree(injectionPoint);
      injectionPoint.addChildrenToFront(var);
    }
    for (ExtractionInstance instance : info.instances) {
      extractInstance(instance);
    }
  }
  private void extractInstance(ExtractionInstance instance) {
    PrototypeMemberDeclaration first = instance.declarations.getFirst();
    String className = first.qualifiedClassName;
    if(pattern == Pattern.USE_GLOBAL_TEMP) {
      Node stmt = new Node(first.node.getType(), IR.assign(IR.name(prototypeAlias), NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), className + ".prototype", instance.parent, className + ".prototype"))).copyInformationFromForTree(first.node);
      instance.parent.addChildBefore(stmt, first.node);
    }
    else 
      if(pattern == Pattern.USE_ANON_FUNCTION) {
        Node block = IR.block();
        Node func = IR.function(IR.name(""), IR.paramList(IR.name(prototypeAlias)), block);
        Node call = IR.call(func, NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), className + ".prototype", instance.parent, className + ".prototype"));
        call.putIntProp(Node.FREE_CALL, 1);
        Node stmt = new Node(first.node.getType(), call);
        stmt.copyInformationFromForTree(first.node);
        instance.parent.addChildBefore(stmt, first.node);
        LinkedList<PrototypeMemberDeclaration> var_2006 = instance.declarations;
        for (PrototypeMemberDeclaration declar : var_2006) {
          block.addChildToBack(declar.node.detachFromParent());
        }
      }
    for (PrototypeMemberDeclaration declar : instance.declarations) {
      replacePrototypeMemberDeclaration(declar);
    }
  }
  @Override() public void process(Node externs, Node root) {
    GatherExtractionInfo extractionInfo = new GatherExtractionInfo();
    NodeTraversal.traverse(compiler, root, extractionInfo);
    if(extractionInfo.shouldExtract()) {
      doExtraction(extractionInfo);
      compiler.reportCodeChange();
    }
  }
  private void replacePrototypeMemberDeclaration(PrototypeMemberDeclaration declar) {
    Node assignment = declar.node.getFirstChild();
    Node lhs = assignment.getFirstChild();
    Node name = NodeUtil.newQualifiedNameNode(compiler.getCodingConvention(), prototypeAlias + "." + declar.memberName, declar.node, declar.memberName);
    Node accessNode = declar.lhs.getFirstChild().getFirstChild();
    Object originalName = accessNode.getProp(Node.ORIGINALNAME_PROP);
    String className = "?";
    if(originalName != null) {
      className = originalName.toString();
    }
    NodeUtil.setDebugInformation(name.getFirstChild(), lhs, className + ".prototype");
    assignment.replaceChild(lhs, name);
  }
  
  private class ExtractionInstance  {
    LinkedList<PrototypeMemberDeclaration> declarations = Lists.newLinkedList();
    private int delta = 0;
    final private Node parent;
    private ExtractionInstance(PrototypeMemberDeclaration head, Node parent) {
      super();
      this.parent = parent;
      declarations.add(head);
      delta = pattern.perExtractionOverhead + pattern.perMemberOverhead;
      for(com.google.javascript.rhino.Node cur = head.node.getNext(); cur != null; cur = cur.getNext()) {
        if(cur.isFunction()) {
          continue ;
        }
        PrototypeMemberDeclaration prototypeMember = PrototypeMemberDeclaration.extractDeclaration(cur);
        if(prototypeMember == null || !head.isSameClass(prototypeMember)) {
          break ;
        }
        declarations.add(prototypeMember);
        delta += pattern.perMemberOverhead;
      }
    }
    boolean isFavorable() {
      return delta <= 0;
    }
  }
  
  private class GatherExtractionInfo extends AbstractShallowCallback  {
    private List<ExtractionInstance> instances = Lists.newLinkedList();
    private int totalDelta = pattern.globalOverhead;
    private boolean shouldExtract() {
      return totalDelta < 0;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(!n.isScript() && !n.isBlock()) {
        return ;
      }
      for(com.google.javascript.rhino.Node cur = n.getFirstChild(); cur != null; cur = cur.getNext()) {
        PrototypeMemberDeclaration prototypeMember = PrototypeMemberDeclaration.extractDeclaration(cur);
        if(prototypeMember == null) {
          continue ;
        }
        ExtractionInstance instance = new ExtractionInstance(prototypeMember, n);
        cur = instance.declarations.getLast().node;
        if(instance.isFavorable()) {
          instances.add(instance);
          totalDelta += instance.delta;
        }
      }
    }
  }
  enum Pattern {
    USE_GLOBAL_TEMP("var t;".length(), "t=y.prototype;".length(), "t.y=".length() - "x[p].y=".length()),

    USE_ANON_FUNCTION(0, "(function(t){})(y.prototype);".length(), "t.y=".length() - "x.prototype.y=".length()),

  ;
    final private int globalOverhead;
    final private int perExtractionOverhead;
    final private int perMemberOverhead;
  private Pattern(int globalOverHead, int perExtractionOverhead, int perMemberOverhead) {
      this.globalOverhead = globalOverHead;
      this.perExtractionOverhead = perExtractionOverhead;
      this.perMemberOverhead = perMemberOverhead;
  }
  }
  
  private static class PrototypeMemberDeclaration  {
    final String memberName;
    final Node node;
    final String qualifiedClassName;
    final Node lhs;
    private PrototypeMemberDeclaration(Node lhs, Node node) {
      super();
      this.lhs = lhs;
      this.memberName = NodeUtil.getPrototypePropertyName(lhs);
      this.node = node;
      this.qualifiedClassName = NodeUtil.getPrototypeClassName(lhs).getQualifiedName();
    }
    private static PrototypeMemberDeclaration extractDeclaration(Node n) {
      if(!NodeUtil.isPrototypePropertyDeclaration(n)) {
        return null;
      }
      Node lhs = n.getFirstChild().getFirstChild();
      return new PrototypeMemberDeclaration(lhs, n);
    }
    private boolean isSameClass(PrototypeMemberDeclaration other) {
      return qualifiedClassName.equals(other.qualifiedClassName);
    }
  }
}