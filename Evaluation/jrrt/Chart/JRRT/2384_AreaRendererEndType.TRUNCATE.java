package org.jfree.chart.renderer;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class AreaRendererEndType implements Serializable  {
  final private static long serialVersionUID = -1774146392916359839L;
  final public static AreaRendererEndType TAPER = new AreaRendererEndType("AreaRendererEndType.TAPER");
  final public static AreaRendererEndType TRUNCATE = new AreaRendererEndType("AreaRendererEndType.TRUNCATE");
  final public static AreaRendererEndType LEVEL = new AreaRendererEndType("AreaRendererEndType.LEVEL");
  private String name;
  private AreaRendererEndType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    Object result = null;
    if(this.equals(AreaRendererEndType.LEVEL)) {
      result = AreaRendererEndType.LEVEL;
    }
    else 
      if(this.equals(AreaRendererEndType.TAPER)) {
        result = AreaRendererEndType.TAPER;
      }
      else {
        AreaRendererEndType var_2384 = AreaRendererEndType.TRUNCATE;
        if(this.equals(var_2384)) {
          result = AreaRendererEndType.TRUNCATE;
        }
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
    if(!(obj instanceof AreaRendererEndType)) {
      return false;
    }
    AreaRendererEndType t = (AreaRendererEndType)obj;
    if(!this.name.equals(t.toString())) {
      return false;
    }
    return true;
  }
}