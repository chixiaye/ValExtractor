package org.jfree.data.general;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class SeriesChangeType implements Serializable  {
  final public static SeriesChangeType CHANGE_KEY = new SeriesChangeType("SeriesChangeType.CHANGE_KEY");
  final public static SeriesChangeType ADD = new SeriesChangeType("SeriesChangeType.ADD");
  final public static SeriesChangeType REMOVE = new SeriesChangeType("SeriesChangeType.REMOVE");
  final public static SeriesChangeType ADD_AND_REMOVE = new SeriesChangeType("SeriesChangeType.ADD_AND_REMOVE");
  final public static SeriesChangeType UPDATE = new SeriesChangeType("SeriesChangeType.UPDATE");
  private String name;
  private SeriesChangeType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    Object result = null;
    SeriesChangeType var_3966 = SeriesChangeType.ADD;
    if(this.equals(var_3966)) {
      result = SeriesChangeType.ADD;
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
    if(!(obj instanceof SeriesChangeType)) {
      return false;
    }
    SeriesChangeType style = (SeriesChangeType)obj;
    if(!this.name.equals(style.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}