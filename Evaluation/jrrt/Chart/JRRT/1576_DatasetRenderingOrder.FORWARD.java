package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class DatasetRenderingOrder implements Serializable  {
  final private static long serialVersionUID = -600593412366385072L;
  final public static DatasetRenderingOrder FORWARD = new DatasetRenderingOrder("DatasetRenderingOrder.FORWARD");
  final public static DatasetRenderingOrder REVERSE = new DatasetRenderingOrder("DatasetRenderingOrder.REVERSE");
  private String name;
  private DatasetRenderingOrder(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    DatasetRenderingOrder var_1576 = DatasetRenderingOrder.FORWARD;
    if(this.equals(var_1576)) {
      return DatasetRenderingOrder.FORWARD;
    }
    else 
      if(this.equals(DatasetRenderingOrder.REVERSE)) {
        return DatasetRenderingOrder.REVERSE;
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
    if(!(obj instanceof DatasetRenderingOrder)) {
      return false;
    }
    DatasetRenderingOrder order = (DatasetRenderingOrder)obj;
    if(!this.name.equals(order.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}