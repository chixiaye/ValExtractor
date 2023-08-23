package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class PlotOrientation implements Serializable  {
  final private static long serialVersionUID = -2508771828190337782L;
  final public static PlotOrientation HORIZONTAL = new PlotOrientation("PlotOrientation.HORIZONTAL");
  final public static PlotOrientation VERTICAL = new PlotOrientation("PlotOrientation.VERTICAL");
  private String name;
  private PlotOrientation(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    Object result = null;
    PlotOrientation var_1381 = PlotOrientation.HORIZONTAL;
    if(this.equals(var_1381)) {
      result = PlotOrientation.HORIZONTAL;
    }
    else 
      if(this.equals(PlotOrientation.VERTICAL)) {
        result = PlotOrientation.VERTICAL;
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
    if(!(obj instanceof PlotOrientation)) {
      return false;
    }
    PlotOrientation orientation = (PlotOrientation)obj;
    if(!this.name.equals(orientation.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}