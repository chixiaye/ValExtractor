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
    if(this.equals(SeriesRenderingOrder.FORWARD)) {
      return SeriesRenderingOrder.FORWARD;
    }
    else {
      SeriesRenderingOrder var_1383 = SeriesRenderingOrder.REVERSE;
      if(this.equals(var_1383)) {
        return SeriesRenderingOrder.REVERSE;
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