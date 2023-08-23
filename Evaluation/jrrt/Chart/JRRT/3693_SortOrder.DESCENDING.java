package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class SortOrder implements Serializable  {
  final private static long serialVersionUID = -2124469847758108312L;
  final public static SortOrder ASCENDING = new SortOrder("SortOrder.ASCENDING");
  final public static SortOrder DESCENDING = new SortOrder("SortOrder.DESCENDING");
  private String name;
  private SortOrder(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(SortOrder.ASCENDING)) {
      return SortOrder.ASCENDING;
    }
    else {
      SortOrder var_3693 = SortOrder.DESCENDING;
      if(this.equals(var_3693)) {
        return SortOrder.DESCENDING;
      }
    }
    return null;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof SortOrder)) {
      return false;
    }
    final SortOrder that = (SortOrder)obj;
    if(!this.name.equals(that.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}