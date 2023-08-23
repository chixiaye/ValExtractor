package com.google.javascript.jscomp;
import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

public class FieldCleanupPass implements HotSwapCompilerPass  {
  final private AbstractCompiler compiler;
  public FieldCleanupPass(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
  }
  @Override() public void hotSwapScript(Node scriptRoot, Node originalRoot) {
    String srcName = originalRoot.getSourceFileName();
    Callback cb = new QualifiedNameSearchTraversal(compiler.getTypeRegistry(), srcName);
    new NodeTraversal(compiler, cb).traverse(originalRoot);
  }
  @Override() public void process(Node externs, Node root) {
  }
  
  static class QualifiedNameSearchTraversal extends AbstractShallowCallback  {
    final private JSTypeRegistry typeRegistry;
    final private String srcName;
    public QualifiedNameSearchTraversal(JSTypeRegistry typeRegistry, String srcName) {
      super();
      this.typeRegistry = typeRegistry;
      this.srcName = srcName;
    }
    private String getFieldName(Node n) {
      return n.getLastChild().getString();
    }
    private void removeProperty(ObjectType type, String propName) {
      Node pNode = type.getPropertyNode(propName);
      if(pNode != null && srcName.equals(pNode.getSourceFileName())) {
        typeRegistry.unregisterPropertyOnType(propName, type);
        type.removeProperty(propName);
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node p) {
      if(n.isGetProp() && !p.isGetProp()) {
        String propName = getFieldName(n);
        JSType type = n.getFirstChild().getJSType();
        if(type == null || type.toObjectType() == null) {
          return ;
        }
        removeProperty(type.toObjectType(), propName);
      }
      com.google.javascript.rhino.JSDocInfo var_1825 = n.getJSDocInfo();
      if(var_1825 != null) {
        n.getJSDocInfo().setAssociatedNode(null);
      }
    }
  }
}