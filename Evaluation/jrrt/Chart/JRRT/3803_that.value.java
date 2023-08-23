package org.jfree.data;
import java.io.Serializable;
import org.jfree.chart.util.PublicCloneable;

public class DefaultKeyedValue implements KeyedValue, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -7388924517460437712L;
  private Comparable key;
  private Number value;
  public DefaultKeyedValue(Comparable key, Number value) {
    super();
    if(key == null) {
      throw new IllegalArgumentException("Null \'key\' argument.");
    }
    this.key = key;
    this.value = value;
  }
  public Comparable getKey() {
    return this.key;
  }
  public Number getValue() {
    return this.value;
  }
  public Object clone() throws CloneNotSupportedException {
    DefaultKeyedValue clone = (DefaultKeyedValue)super.clone();
    return clone;
  }
  public String toString() {
    return "(" + this.key.toString() + ", " + this.value.toString() + ")";
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof DefaultKeyedValue)) {
      return false;
    }
    DefaultKeyedValue that = (DefaultKeyedValue)obj;
    if(!this.key.equals(that.key)) {
      return false;
    }
    Number var_3803 = that.value;
    if(this.value != null ? !this.value.equals(var_3803) : that.value != null) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    int result;
    result = (this.key != null ? this.key.hashCode() : 0);
    result = 29 * result + (this.value != null ? this.value.hashCode() : 0);
    return result;
  }
  public synchronized void setValue(Number value) {
    this.value = value;
  }
}