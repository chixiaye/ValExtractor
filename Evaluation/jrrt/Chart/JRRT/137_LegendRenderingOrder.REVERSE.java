package org.jfree.chart;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class LegendRenderingOrder implements Serializable  {
  final private static long serialVersionUID = -3832486612685808616L;
  final public static LegendRenderingOrder STANDARD = new LegendRenderingOrder("LegendRenderingOrder.STANDARD");
  final public static LegendRenderingOrder REVERSE = new LegendRenderingOrder("LegendRenderingOrder.REVERSE");
  private String name;
  private LegendRenderingOrder(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(LegendRenderingOrder.STANDARD)) {
      return LegendRenderingOrder.STANDARD;
    }
    else {
      LegendRenderingOrder var_137 = LegendRenderingOrder.REVERSE;
      if(this.equals(var_137)) {
        return LegendRenderingOrder.REVERSE;
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
    if(!(obj instanceof LegendRenderingOrder)) {
      return false;
    }
    LegendRenderingOrder order = (LegendRenderingOrder)obj;
    if(!this.name.equals(order.toString())) {
      return false;
    }
    return true;
  }
}