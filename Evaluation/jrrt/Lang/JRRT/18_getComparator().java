package org.apache.commons.lang3;
import java.io.Serializable;
import java.util.Comparator;
final public class Range<T extends java.lang.Object> implements Serializable  {
  final private static long serialVersionUID = 1L;
  final private Comparator<T> comparator;
  final private T minimum;
  final private T maximum;
  private transient int hashCode;
  private transient String toString;
  @SuppressWarnings(value = {"unchecked", }) private Range(final T element1, final T element2, Comparator<T> comparator) {
    super();
    if(element1 == null || element2 == null) {
      throw new IllegalArgumentException("Elements in a range must not be null: element1=" + element1 + ", element2=" + element2);
    }
    if(comparator == null) {
      comparator = ComparableComparator.INSTANCE;
    }
    if(comparator.compare(element1, element2) < 1) {
      this.minimum = element1;
      this.maximum = element2;
    }
    else {
      this.minimum = element2;
      this.maximum = element1;
    }
    this.comparator = comparator;
  }
  public Comparator<T> getComparator() {
    return comparator;
  }
  public static  <T extends java.lang.Comparable<org.apache.commons.lang3.T>> Range<T> between(final T fromInclusive, final T toInclusive) {
    return between(fromInclusive, toInclusive, null);
  }
  public static  <T extends java.lang.Object> Range<T> between(final T fromInclusive, final T toInclusive, final Comparator<T> comparator) {
    return new Range<T>(fromInclusive, toInclusive, comparator);
  }
  public Range<T> intersectionWith(final Range<T> other) {
    if(!this.isOverlappedBy(other)) {
      throw new IllegalArgumentException(String.format("Cannot calculate intersection with non-overlapping range %s", other));
    }
    if(this.equals(other)) {
      return this;
    }
    Comparator<T> var_18 = getComparator();
    final T min = var_18.compare(minimum, other.minimum) < 0 ? other.minimum : minimum;
    final T max = getComparator().compare(maximum, other.maximum) < 0 ? maximum : other.maximum;
    return between(min, max, getComparator());
  }
  public static  <T extends java.lang.Comparable<org.apache.commons.lang3.T>> Range<T> is(final T element) {
    return between(element, element, null);
  }
  public static  <T extends java.lang.Object> Range<T> is(final T element, final Comparator<T> comparator) {
    return between(element, element, comparator);
  }
  @Override() public String toString() {
    String result = toString;
    if(result == null) {
      final StringBuilder buf = new StringBuilder(32);
      buf.append('[');
      buf.append(minimum);
      buf.append("..");
      buf.append(maximum);
      buf.append(']');
      result = buf.toString();
      toString = result;
    }
    return result;
  }
  public String toString(final String format) {
    return String.format(format, minimum, maximum, comparator);
  }
  public T getMaximum() {
    return maximum;
  }
  public T getMinimum() {
    return minimum;
  }
  public boolean contains(final T element) {
    if(element == null) {
      return false;
    }
    return comparator.compare(element, minimum) > -1 && comparator.compare(element, maximum) < 1;
  }
  public boolean containsRange(final Range<T> otherRange) {
    if(otherRange == null) {
      return false;
    }
    return contains(otherRange.minimum) && contains(otherRange.maximum);
  }
  @Override() public boolean equals(final Object obj) {
    if(obj == this) {
      return true;
    }
    else 
      if(obj == null || obj.getClass() != getClass()) {
        return false;
      }
      else {
        @SuppressWarnings(value = {"unchecked", }) final Range<T> range = (Range<T>)obj;
        return minimum.equals(range.minimum) && maximum.equals(range.maximum);
      }
  }
  public boolean isAfter(final T element) {
    if(element == null) {
      return false;
    }
    return comparator.compare(element, minimum) < 0;
  }
  public boolean isAfterRange(final Range<T> otherRange) {
    if(otherRange == null) {
      return false;
    }
    return isAfter(otherRange.maximum);
  }
  public boolean isBefore(final T element) {
    if(element == null) {
      return false;
    }
    return comparator.compare(element, maximum) > 0;
  }
  public boolean isBeforeRange(final Range<T> otherRange) {
    if(otherRange == null) {
      return false;
    }
    return isBefore(otherRange.minimum);
  }
  public boolean isEndedBy(final T element) {
    if(element == null) {
      return false;
    }
    return comparator.compare(element, maximum) == 0;
  }
  public boolean isNaturalOrdering() {
    return comparator == ComparableComparator.INSTANCE;
  }
  public boolean isOverlappedBy(final Range<T> otherRange) {
    if(otherRange == null) {
      return false;
    }
    return otherRange.contains(minimum) || otherRange.contains(maximum) || contains(otherRange.minimum);
  }
  public boolean isStartedBy(final T element) {
    if(element == null) {
      return false;
    }
    return comparator.compare(element, minimum) == 0;
  }
  public int elementCompareTo(final T element) {
    if(element == null) {
      throw new NullPointerException("Element is null");
    }
    if(isAfter(element)) {
      return -1;
    }
    else 
      if(isBefore(element)) {
        return 1;
      }
      else {
        return 0;
      }
  }
  @Override() public int hashCode() {
    int result = hashCode;
    if(hashCode == 0) {
      result = 17;
      result = 37 * result + getClass().hashCode();
      result = 37 * result + minimum.hashCode();
      result = 37 * result + maximum.hashCode();
      hashCode = result;
    }
    return result;
  }
  @SuppressWarnings(value = {"rawtypes", "unchecked", }) private enum ComparableComparator implements Comparator {
    INSTANCE(),

  ;
    @Override() public int compare(final Object obj1, final Object obj2) {
      return ((Comparable)obj1).compareTo(obj2);
    }
  private ComparableComparator() {
  }
  }
}