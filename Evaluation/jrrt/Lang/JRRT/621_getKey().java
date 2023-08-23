package org.apache.commons.lang3.tuple;
import java.io.Serializable;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
abstract public class Pair<L extends java.lang.Object, R extends java.lang.Object> implements Map.Entry<L, R>, Comparable<Pair<L, R>>, Serializable  {
  final private static long serialVersionUID = 4954918890077093841L;
  @Override() final public L getKey() {
    return getLeft();
  }
  abstract public L getLeft();
  public static  <L extends java.lang.Object, R extends java.lang.Object> Pair<L, R> of(final L left, final R right) {
    return new ImmutablePair<L, R>(left, right);
  }
  abstract public R getRight();
  @Override() public R getValue() {
    return getRight();
  }
  @Override() public String toString() {
    return new StringBuilder().append('(').append(getLeft()).append(',').append(getRight()).append(')').toString();
  }
  public String toString(final String format) {
    return String.format(format, getLeft(), getRight());
  }
  @Override() public boolean equals(final Object obj) {
    if(obj == this) {
      return true;
    }
    if(obj instanceof Map.Entry<?, ?>) {
      final Map.Entry<?, ?> other = (Map.Entry<?, ?>)obj;
      return ObjectUtils.equals(getKey(), other.getKey()) && ObjectUtils.equals(getValue(), other.getValue());
    }
    return false;
  }
  @Override() public int compareTo(final Pair<L, R> other) {
    return new CompareToBuilder().append(getLeft(), other.getLeft()).append(getRight(), other.getRight()).toComparison();
  }
  @Override() public int hashCode() {
    L var_621 = getKey();
    return (var_621 == null ? 0 : getKey().hashCode()) ^ (getValue() == null ? 0 : getValue().hashCode());
  }
}