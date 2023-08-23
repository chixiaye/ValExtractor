package org.jfree.chart.event;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class ChartChangeEventType implements Serializable  {
  final private static long serialVersionUID = 5481917022435735602L;
  final public static ChartChangeEventType GENERAL = new ChartChangeEventType("ChartChangeEventType.GENERAL");
  final public static ChartChangeEventType NEW_DATASET = new ChartChangeEventType("ChartChangeEventType.NEW_DATASET");
  final public static ChartChangeEventType DATASET_UPDATED = new ChartChangeEventType("ChartChangeEventType.DATASET_UPDATED");
  private String name;
  private ChartChangeEventType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    ChartChangeEventType var_1161 = ChartChangeEventType.GENERAL;
    if(this.equals(var_1161)) {
      return ChartChangeEventType.GENERAL;
    }
    else 
      if(this.equals(ChartChangeEventType.NEW_DATASET)) {
        return ChartChangeEventType.NEW_DATASET;
      }
      else 
        if(this.equals(ChartChangeEventType.DATASET_UPDATED)) {
          return ChartChangeEventType.DATASET_UPDATED;
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
    if(!(obj instanceof ChartChangeEventType)) {
      return false;
    }
    ChartChangeEventType that = (ChartChangeEventType)obj;
    if(!this.name.equals(that.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}