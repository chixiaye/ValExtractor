package org.jfree.data.statistics;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class HistogramType implements Serializable  {
  final private static long serialVersionUID = 2618927186251997727L;
  final public static HistogramType FREQUENCY = new HistogramType("FREQUENCY");
  final public static HistogramType RELATIVE_FREQUENCY = new HistogramType("RELATIVE_FREQUENCY");
  final public static HistogramType SCALE_AREA_TO_1 = new HistogramType("SCALE_AREA_TO_1");
  private String name;
  private HistogramType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    HistogramType var_4129 = HistogramType.FREQUENCY;
    if(this.equals(var_4129)) {
      return HistogramType.FREQUENCY;
    }
    else 
      if(this.equals(HistogramType.RELATIVE_FREQUENCY)) {
        return HistogramType.RELATIVE_FREQUENCY;
      }
      else 
        if(this.equals(HistogramType.SCALE_AREA_TO_1)) {
          return HistogramType.SCALE_AREA_TO_1;
        }
    return null;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof HistogramType)) {
      return false;
    }
    HistogramType t = (HistogramType)obj;
    if(!this.name.equals(t.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}