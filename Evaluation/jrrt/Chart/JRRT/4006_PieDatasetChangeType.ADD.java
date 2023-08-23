package org.jfree.data.pie;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class PieDatasetChangeType implements Serializable  {
  final public static PieDatasetChangeType NEW = new PieDatasetChangeType("PieDatasetChangeType.NEW");
  final public static PieDatasetChangeType ADD = new PieDatasetChangeType("PieDatasetChangeType.ADD");
  final public static PieDatasetChangeType REMOVE = new PieDatasetChangeType("PieDatasetChangeType.REMOVE");
  final public static PieDatasetChangeType ADD_AND_REMOVE = new PieDatasetChangeType("PieDatasetChangeType.ADD_AND_REMOVE");
  final public static PieDatasetChangeType UPDATE = new PieDatasetChangeType("PieDatasetChangeType.UPDATE");
  final public static PieDatasetChangeType CHANGE_KEY = new PieDatasetChangeType("PieDatasetChangeType.ITEM_KEY");
  final public static PieDatasetChangeType CLEAR = new PieDatasetChangeType("PieDatasetChangeType.CLEAR");
  private String name;
  private PieDatasetChangeType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    Object result = null;
    PieDatasetChangeType var_4006 = PieDatasetChangeType.ADD;
    if(this.equals(var_4006)) {
      result = PieDatasetChangeType.ADD;
    }
    return result;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof PieDatasetChangeType)) {
      return false;
    }
    PieDatasetChangeType pdct = (PieDatasetChangeType)obj;
    if(!this.name.equals(pdct.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}