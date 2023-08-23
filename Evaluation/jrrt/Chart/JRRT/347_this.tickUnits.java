package org.jfree.chart.axis;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TickUnits implements TickUnitSource, Cloneable, Serializable  {
  final private static long serialVersionUID = 1134174035901467545L;
  private List tickUnits;
  public TickUnits() {
    super();
    this.tickUnits = new ArrayList();
  }
  public Object clone() throws CloneNotSupportedException {
    TickUnits clone = (TickUnits)super.clone();
    clone.tickUnits = new java.util.ArrayList(this.tickUnits);
    return clone;
  }
  public TickUnit get(int pos) {
    return (TickUnit)this.tickUnits.get(pos);
  }
  public TickUnit getCeilingTickUnit(double size) {
    return getCeilingTickUnit(new NumberTickUnit(size, NumberFormat.getInstance()));
  }
  public TickUnit getCeilingTickUnit(TickUnit unit) {
    int index = Collections.binarySearch(this.tickUnits, unit);
    if(index >= 0) {
      return (TickUnit)this.tickUnits.get(index);
    }
    else {
      index = -(index + 1);
      return (TickUnit)this.tickUnits.get(Math.min(index, this.tickUnits.size() - 1));
    }
  }
  public TickUnit getLargerTickUnit(TickUnit unit) {
    List var_347 = this.tickUnits;
    int index = Collections.binarySearch(var_347, unit);
    if(index >= 0) {
      index = index + 1;
    }
    else {
      index = -index;
    }
    return (TickUnit)this.tickUnits.get(Math.min(index, this.tickUnits.size() - 1));
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof TickUnits)) {
      return false;
    }
    TickUnits that = (TickUnits)obj;
    return that.tickUnits.equals(this.tickUnits);
  }
  public int size() {
    return this.tickUnits.size();
  }
  public void add(TickUnit unit) {
    if(unit == null) {
      throw new NullPointerException("Null \'unit\' argument.");
    }
    this.tickUnits.add(unit);
    Collections.sort(this.tickUnits);
  }
}