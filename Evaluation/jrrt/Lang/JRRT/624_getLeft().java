package org.apache.commons.lang3.tuple;
import java.io.Serializable;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
abstract public class Triple<L extends java.lang.Object, M extends java.lang.Object, R extends java.lang.Object> implements Comparable<Triple<L, M, R>>, Serializable  {
  final private static long serialVersionUID = 1L;
  abstract public L getLeft();
  abstract public M getMiddle();
  abstract public R getRight();
  @Override() public String toString() {
    return new StringBuilder().append('(').append(getLeft()).append(',').append(getMiddle()).append(',').append(getRight()).append(')').toString();
  }
  public String toString(final String format) {
    return String.format(format, getLeft(), getMiddle(), getRight());
  }
  public static  <L extends java.lang.Object, M extends java.lang.Object, R extends java.lang.Object> Triple<L, M, R> of(final L left, final M middle, final R right) {
    return new ImmutableTriple<L, M, R>(left, middle, right);
  }
  @Override() public boolean equals(final Object obj) {
    if(obj == this) {
      return true;
    }
    if(obj instanceof Triple<?, ?, ?>) {
      final Triple<?, ?, ?> other = (Triple<?, ?, ?>)obj;
      return ObjectUtils.equals(getLeft(), other.getLeft()) && ObjectUtils.equals(getMiddle(), other.getMiddle()) && ObjectUtils.equals(getRight(), other.getRight());
    }
    return false;
  }
  @Override() public int compareTo(final Triple<L, M, R> other) {
    return new CompareToBuilder().append(getLeft(), other.getLeft()).append(getMiddle(), other.getMiddle()).append(getRight(), other.getRight()).toComparison();
  }
  @Override() public int hashCode() {
    L var_624 = getLeft();
    return (var_624 == null ? 0 : getLeft().hashCode()) ^ (getMiddle() == null ? 0 : getMiddle().hashCode()) ^ (getRight() == null ? 0 : getRight().hashCode());
  }
}