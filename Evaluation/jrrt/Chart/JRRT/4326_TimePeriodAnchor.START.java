package org.jfree.data.time;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class TimePeriodAnchor implements Serializable  {
  final private static long serialVersionUID = 2011955697457548862L;
  final public static TimePeriodAnchor START = new TimePeriodAnchor("TimePeriodAnchor.START");
  final public static TimePeriodAnchor MIDDLE = new TimePeriodAnchor("TimePeriodAnchor.MIDDLE");
  final public static TimePeriodAnchor END = new TimePeriodAnchor("TimePeriodAnchor.END");
  private String name;
  private TimePeriodAnchor(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    TimePeriodAnchor var_4326 = TimePeriodAnchor.START;
    if(this.equals(var_4326)) {
      return TimePeriodAnchor.START;
    }
    else 
      if(this.equals(TimePeriodAnchor.MIDDLE)) {
        return TimePeriodAnchor.MIDDLE;
      }
      else 
        if(this.equals(TimePeriodAnchor.END)) {
          return TimePeriodAnchor.END;
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
    if(!(obj instanceof TimePeriodAnchor)) {
      return false;
    }
    TimePeriodAnchor position = (TimePeriodAnchor)obj;
    if(!this.name.equals(position.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}