package com.google.javascript.jscomp;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.util.List;

class AstParallelizer  {
  final public static String TEMP_NAME = "JSC_TMP_PLACE_HOLDER";
  final private Predicate<Node> shouldSplit;
  final private Supplier<Node> placeHolderProvider;
  final private List<Node> forest;
  final private Node root;
  final private boolean includeRoot;
  final private List<DetachPoint> detachPointList;
  public AstParallelizer(Predicate<Node> shouldSplit, Predicate<Node> shouldTraverse, Supplier<Node> placeHolderProvider, Node root, boolean includeRoot) {
    super();
    this.shouldSplit = shouldSplit;
    this.placeHolderProvider = placeHolderProvider;
    this.root = root;
    this.includeRoot = includeRoot;
    java.util.LinkedList<Node> var_2112 = Lists.newLinkedList();
    this.forest = var_2112;
    this.detachPointList = Lists.newLinkedList();
  }
  public static AstParallelizer createNewFileLevelAstParallelizer(Node root) {
    Predicate<Node> shouldSplit = new Predicate<Node>() {
        @Override() public boolean apply(Node input) {
          return input.getSourceFileName() != null;
        }
    };
    Supplier<Node> placeHolders = new Supplier<Node>() {
        @Override() public Node get() {
          return NodeUtil.newExpr(IR.string(TEMP_NAME));
        }
    };
    Predicate<Node> shouldTraverse = new Predicate<Node>() {
        @Override() public boolean apply(Node n) {
          return n.isBlock();
        }
    };
    return new AstParallelizer(shouldSplit, shouldTraverse, placeHolders, root, false);
  }
  public static AstParallelizer createNewFunctionLevelAstParallelizer(Node root, boolean globalPass) {
    Predicate<Node> shouldSplit = new Predicate<Node>() {
        @Override() public boolean apply(Node input) {
          return input.isFunction();
        }
    };
    Predicate<Node> shouldTraverse = new Predicate<Node>() {
        @Override() public boolean apply(Node ignored) {
          return true;
        }
    };
    Supplier<Node> placeHolders = new Supplier<Node>() {
        @Override() public Node get() {
          return IR.function(IR.name(TEMP_NAME), IR.paramList(), IR.block());
        }
    };
    return new AstParallelizer(shouldSplit, shouldTraverse, placeHolders, root, globalPass);
  }
  public List<Node> split() {
    if(includeRoot) {
      forest.add(root);
    }
    split(root);
    return forest;
  }
  public void join() {
    while(!detachPointList.isEmpty()){
      DetachPoint entry = detachPointList.remove(detachPointList.size() - 1);
      entry.reattach();
    }
  }
  private void recordSplitPoint(Node placeHolder, Node before, Node original) {
    detachPointList.add(new DetachPoint(placeHolder, before, original));
  }
  private void split(Node n) {
    Node c = n.getFirstChild();
    Node before = null;
    while(c != null){
      Node next = c.getNext();
      if(shouldSplit.apply(c)) {
        Node placeHolder = placeHolderProvider.get();
        if(before == null) {
          forest.add(n.removeFirstChild());
          n.addChildToFront(placeHolder);
        }
        else {
          n.addChildAfter(placeHolder, c);
          n.removeChildAfter(before);
          forest.add(c);
        }
        recordSplitPoint(placeHolder, before, c);
        before = placeHolder;
      }
      else {
        split(c);
        before = c;
      }
      c = next;
    }
  }
  
  private static class DetachPoint  {
    private Node placeHolder;
    private Node before;
    private Node original;
    private DetachPoint(Node placeHolder, Node before, Node original) {
      super();
      this.placeHolder = placeHolder;
      this.before = before;
      this.original = original;
    }
    public void reattach() {
      if(placeHolder.getParent() != null) {
        if(before == null) {
          placeHolder.getParent().addChildrenToFront(original);
          placeHolder.getParent().removeChildAfter(original);
        }
        else {
          placeHolder.getParent().addChildAfter(original, before);
          placeHolder.getParent().removeChildAfter(original);
        }
      }
    }
  }
}