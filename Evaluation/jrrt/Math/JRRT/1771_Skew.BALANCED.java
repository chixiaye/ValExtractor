package org.apache.commons.math3.geometry.partitioning.utilities;
public class AVLTree<T extends java.lang.Comparable<org.apache.commons.math3.geometry.partitioning.utilities.AVLTree@T>>  {
  private Node top;
  public AVLTree() {
    super();
    top = null;
  }
  public Node getLargest() {
    return (top == null) ? null : top.getLargest();
  }
  public Node getNotLarger(final T reference) {
    Node candidate = null;
    for(org.apache.commons.math3.geometry.partitioning.utilities.AVLTree.Node node = top; node != null; ) {
      if(node.element.compareTo(reference) > 0) {
        if(node.left == null) {
          return candidate;
        }
        node = node.left;
      }
      else {
        candidate = node;
        if(node.right == null) {
          return candidate;
        }
        node = node.right;
      }
    }
    return null;
  }
  public Node getNotSmaller(final T reference) {
    Node candidate = null;
    for(org.apache.commons.math3.geometry.partitioning.utilities.AVLTree.Node node = top; node != null; ) {
      if(node.element.compareTo(reference) < 0) {
        if(node.right == null) {
          return candidate;
        }
        node = node.right;
      }
      else {
        candidate = node;
        if(node.left == null) {
          return candidate;
        }
        node = node.left;
      }
    }
    return null;
  }
  public Node getSmallest() {
    return (top == null) ? null : top.getSmallest();
  }
  public boolean delete(final T element) {
    if(element != null) {
      for(org.apache.commons.math3.geometry.partitioning.utilities.AVLTree.Node node = getNotSmaller(element); node != null; node = node.getNext()) {
        if(node.element == element) {
          node.delete();
          return true;
        }
        else 
          if(node.element.compareTo(element) > 0) {
            return false;
          }
      }
    }
    return false;
  }
  public boolean isEmpty() {
    return top == null;
  }
  public int size() {
    return (top == null) ? 0 : top.size();
  }
  public void insert(final T element) {
    if(element != null) {
      if(top == null) {
        top = new Node(element, null);
      }
      else {
        top.insert(element);
      }
    }
  }
  
  public class Node  {
    private T element;
    private Node left;
    private Node right;
    private Node parent;
    private Skew skew;
    Node(final T element, final Node parent) {
      super();
      this.element = element;
      left = null;
      right = null;
      this.parent = parent;
      skew = Skew.BALANCED;
    }
    Node getLargest() {
      Node node = this;
      while(node.right != null){
        node = node.right;
      }
      return node;
    }
    public Node getNext() {
      if(right != null) {
        final Node node = right.getSmallest();
        if(node != null) {
          return node;
        }
      }
      for(org.apache.commons.math3.geometry.partitioning.utilities.AVLTree.Node node = this; node.parent != null; node = node.parent) {
        if(node != node.parent.right) {
          return node.parent;
        }
      }
      return null;
    }
    public Node getPrevious() {
      if(left != null) {
        final Node node = left.getLargest();
        if(node != null) {
          return node;
        }
      }
      for(org.apache.commons.math3.geometry.partitioning.utilities.AVLTree.Node node = this; node.parent != null; node = node.parent) {
        if(node != node.parent.left) {
          return node.parent;
        }
      }
      return null;
    }
    Node getSmallest() {
      Node node = this;
      while(node.left != null){
        node = node.left;
      }
      return node;
    }
    public T getElement() {
      return element;
    }
    boolean insert(final T newElement) {
      if(newElement.compareTo(this.element) < 0) {
        if(left == null) {
          left = new Node(newElement, this);
          return rebalanceLeftGrown();
        }
        return left.insert(newElement) ? rebalanceLeftGrown() : false;
      }
      if(right == null) {
        right = new Node(newElement, this);
        return rebalanceRightGrown();
      }
      return right.insert(newElement) ? rebalanceRightGrown() : false;
    }
    private boolean rebalanceLeftGrown() {
      switch (skew){
        case LEFT_HIGH:
        if(left.skew == Skew.LEFT_HIGH) {
          rotateCW();
          skew = Skew.BALANCED;
          right.skew = Skew.BALANCED;
        }
        else {
          final Skew s = left.right.skew;
          left.rotateCCW();
          rotateCW();
          switch (s){
            case LEFT_HIGH:
            left.skew = Skew.BALANCED;
            right.skew = Skew.RIGHT_HIGH;
            break ;
            case RIGHT_HIGH:
            left.skew = Skew.LEFT_HIGH;
            right.skew = Skew.BALANCED;
            break ;
            default:
            left.skew = Skew.BALANCED;
            right.skew = Skew.BALANCED;
          }
          skew = Skew.BALANCED;
        }
        return false;
        case RIGHT_HIGH:
        skew = Skew.BALANCED;
        return false;
        default:
        skew = Skew.LEFT_HIGH;
        return true;
      }
    }
    private boolean rebalanceLeftShrunk() {
      switch (skew){
        case LEFT_HIGH:
        skew = Skew.BALANCED;
        return true;
        case RIGHT_HIGH:
        if(right.skew == Skew.RIGHT_HIGH) {
          rotateCCW();
          skew = Skew.BALANCED;
          left.skew = Skew.BALANCED;
          return true;
        }
        else 
          if(right.skew == Skew.BALANCED) {
            rotateCCW();
            skew = Skew.LEFT_HIGH;
            left.skew = Skew.RIGHT_HIGH;
            return false;
          }
          else {
            final Skew s = right.left.skew;
            right.rotateCW();
            rotateCCW();
            switch (s){
              case LEFT_HIGH:
              left.skew = Skew.BALANCED;
              right.skew = Skew.RIGHT_HIGH;
              break ;
              case RIGHT_HIGH:
              left.skew = Skew.LEFT_HIGH;
              right.skew = Skew.BALANCED;
              break ;
              default:
              left.skew = Skew.BALANCED;
              right.skew = Skew.BALANCED;
            }
            skew = Skew.BALANCED;
            return true;
          }
        default:
        skew = Skew.RIGHT_HIGH;
        return false;
      }
    }
    private boolean rebalanceRightGrown() {
      switch (skew){
        case LEFT_HIGH:
        skew = Skew.BALANCED;
        return false;
        case RIGHT_HIGH:
        if(right.skew == Skew.RIGHT_HIGH) {
          rotateCCW();
          skew = Skew.BALANCED;
          left.skew = Skew.BALANCED;
        }
        else {
          final Skew s = right.left.skew;
          right.rotateCW();
          rotateCCW();
          switch (s){
            case LEFT_HIGH:
            left.skew = Skew.BALANCED;
            right.skew = Skew.RIGHT_HIGH;
            break ;
            case RIGHT_HIGH:
            left.skew = Skew.LEFT_HIGH;
            Skew var_1771 = Skew.BALANCED;
            right.skew = var_1771;
            break ;
            default:
            left.skew = Skew.BALANCED;
            right.skew = Skew.BALANCED;
          }
          skew = Skew.BALANCED;
        }
        return false;
        default:
        skew = Skew.RIGHT_HIGH;
        return true;
      }
    }
    private boolean rebalanceRightShrunk() {
      switch (skew){
        case RIGHT_HIGH:
        skew = Skew.BALANCED;
        return true;
        case LEFT_HIGH:
        if(left.skew == Skew.LEFT_HIGH) {
          rotateCW();
          skew = Skew.BALANCED;
          right.skew = Skew.BALANCED;
          return true;
        }
        else 
          if(left.skew == Skew.BALANCED) {
            rotateCW();
            skew = Skew.RIGHT_HIGH;
            right.skew = Skew.LEFT_HIGH;
            return false;
          }
          else {
            final Skew s = left.right.skew;
            left.rotateCCW();
            rotateCW();
            switch (s){
              case LEFT_HIGH:
              left.skew = Skew.BALANCED;
              right.skew = Skew.RIGHT_HIGH;
              break ;
              case RIGHT_HIGH:
              left.skew = Skew.LEFT_HIGH;
              right.skew = Skew.BALANCED;
              break ;
              default:
              left.skew = Skew.BALANCED;
              right.skew = Skew.BALANCED;
            }
            skew = Skew.BALANCED;
            return true;
          }
        default:
        skew = Skew.LEFT_HIGH;
        return false;
      }
    }
    int size() {
      return 1 + ((left == null) ? 0 : left.size()) + ((right == null) ? 0 : right.size());
    }
    public void delete() {
      if((parent == null) && (left == null) && (right == null)) {
        element = null;
        top = null;
      }
      else {
        Node node;
        Node child;
        boolean leftShrunk;
        if((left == null) && (right == null)) {
          node = this;
          element = null;
          leftShrunk = node == node.parent.left;
          child = null;
        }
        else {
          node = (left != null) ? left.getLargest() : right.getSmallest();
          element = node.element;
          leftShrunk = node == node.parent.left;
          child = (node.left != null) ? node.left : node.right;
        }
        node = node.parent;
        if(leftShrunk) {
          node.left = child;
        }
        else {
          node.right = child;
        }
        if(child != null) {
          child.parent = node;
        }
        while(leftShrunk ? node.rebalanceLeftShrunk() : node.rebalanceRightShrunk()){
          if(node.parent == null) {
            return ;
          }
          leftShrunk = node == node.parent.left;
          node = node.parent;
        }
      }
    }
    private void rotateCCW() {
      final T tmpElt = element;
      element = right.element;
      right.element = tmpElt;
      final Node tmpNode = right;
      right = tmpNode.right;
      tmpNode.right = tmpNode.left;
      tmpNode.left = left;
      left = tmpNode;
      if(right != null) {
        right.parent = this;
      }
      if(left.left != null) {
        left.left.parent = left;
      }
    }
    private void rotateCW() {
      final T tmpElt = element;
      element = left.element;
      left.element = tmpElt;
      final Node tmpNode = left;
      left = tmpNode.left;
      tmpNode.left = tmpNode.right;
      tmpNode.right = right;
      right = tmpNode;
      if(left != null) {
        left.parent = this;
      }
      if(right.right != null) {
        right.right.parent = right;
      }
    }
  }
  private static enum Skew {
    LEFT_HIGH(),

    RIGHT_HIGH(),

    BALANCED(),

  ;
  private Skew() {
  }
  }
}