package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class SeriesRenderingOrder implements Serializable  {
  final private static long serialVersionUID = 209336477448807735L;
  final public static SeriesRenderingOrder FORWARD = new SeriesRenderingOrder("SeriesRenderingOrder.FORWARD");
  final public static SeriesRenderingOrder REVERSE = new SeriesRenderingOrder("SeriesRenderingOrder.REVERSE");
  private String name;
  private SeriesRenderingOrder(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    SeriesRenderingOrder var_1382 = SeriesRenderingOrder.FORWARD;
    if(this.equals(var_1382)) {
      return SeriesRenderingOrder.FORWARD;
    }
    else 
      if(this.equals(SeriesRenderingOrder.REVERSE)) {
        return SeriesRenderingOrder.REVERSE;
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
    if(!(obj instanceof SeriesRenderingOrder)) {
      return false;
    }
    SeriesRenderingOrder order = (SeriesRenderingOrder)obj;
    if(!this.name.equals(order.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}