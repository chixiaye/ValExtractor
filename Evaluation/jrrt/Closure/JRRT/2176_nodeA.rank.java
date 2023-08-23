package com.google.javascript.jscomp.graph;
import javax.annotation.Nullable;
import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Iterators.filter;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
public class StandardUnionFind<E extends java.lang.Object> implements Serializable, UnionFind<E>  {
  final private static long serialVersionUID = -1L;
  final private Map<E, Node<E>> elmap = Maps.newLinkedHashMap();
  public StandardUnionFind() {
    super();
  }
  public StandardUnionFind(UnionFind<E> other) {
    super();
    for (E elem : other.elements()) {
      union(elem, other.find(elem));
    }
  }
  @Override() public Collection<Set<E>> allEquivalenceClasses() {
    Map<Node<E>, ImmutableSet.Builder<E>> groupsTmp = Maps.newHashMap();
    for (Node<E> elem : elmap.values()) {
      Node<E> root = findRoot(elem);
      ImmutableSet.Builder<E> builder = groupsTmp.get(root);
      if(builder == null) {
        builder = ImmutableSet.builder();
        groupsTmp.put(root, builder);
      }
      builder.add(elem.element);
    }
    ImmutableList.Builder<Set<E>> result = ImmutableList.builder();
    for (ImmutableSet.Builder<E> group : groupsTmp.values()) {
      result.add(group.build());
    }
    return result.build();
  }
  @Override() public E find(E e) {
    checkArgument(elmap.containsKey(e), "Element does not exist: %s", e);
    return findRoot(elmap.get(e)).element;
  }
  @Override() public E union(E a, E b) {
    Node<E> nodeA = findRootOrCreateNode(a);
    Node<E> nodeB = findRootOrCreateNode(b);
    if(nodeA == nodeB) {
      return nodeA.element;
    }
    int var_2176 = nodeA.rank;
    if(var_2176 > nodeB.rank) {
      nodeB.parent = nodeA;
      nodeA.size += nodeB.size;
      return nodeA.element;
    }
    nodeA.parent = nodeB;
    if(nodeA.rank == nodeB.rank) {
      nodeB.rank++;
    }
    nodeB.size += nodeA.size;
    return nodeB.element;
  }
  private Node<E> findRoot(Node<E> node) {
    if(node.parent != node) {
      node.parent = findRoot(node.parent);
    }
    return node.parent;
  }
  private Node<E> findRootOrCreateNode(E e) {
    Node<E> node = elmap.get(e);
    if(node != null) {
      return findRoot(node);
    }
    node = new Node<E>(e);
    elmap.put(e, node);
    return node;
  }
  @Override() public Set<E> elements() {
    return Collections.unmodifiableSet(elmap.keySet());
  }
  @Override() public Set<E> findAll(final E value) {
    checkArgument(elmap.containsKey(value), "Element does not exist: " + value);
    final Predicate<Object> isSameRoot = new Predicate<Object>() {
        Node<E> nodeForValue = elmap.get(value);
        @Override() public boolean apply(@Nullable() Object b) {
          if(Objects.equal(value, b)) {
            return true;
          }
          Node<E> nodeForB = elmap.get(b);
          if(nodeForB == null) {
            return false;
          }
          nodeForValue = findRoot(nodeForValue);
          return findRoot(nodeForB) == nodeForValue;
        }
    };
    return new AbstractSet<E>() {
        @Override() public boolean contains(Object o) {
          return isSameRoot.apply(o);
        }
        @Override() public Iterator<E> iterator() {
          return filter(elmap.keySet().iterator(), isSameRoot);
        }
        @Override() public int size() {
          return findRoot(elmap.get(value)).size;
        }
    };
  }
  @Override() public boolean areEquivalent(E a, E b) {
    E aRep = find(a);
    E bRep = find(b);
    return aRep == bRep;
  }
  @Override() public void add(E e) {
    union(e, e);
  }
  private static class Node<E extends java.lang.Object>  {
    Node<E> parent;
    final E element;
    int rank = 0;
    int size = 1;
    Node(E element) {
      super();
      this.parent = this;
      this.element = element;
    }
  }
}