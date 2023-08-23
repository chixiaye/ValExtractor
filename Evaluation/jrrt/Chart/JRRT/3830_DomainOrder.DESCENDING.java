package org.jfree.data;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class DomainOrder implements Serializable  {
  final private static long serialVersionUID = 4902774943512072627L;
  final public static DomainOrder NONE = new DomainOrder("DomainOrder.NONE");
  final public static DomainOrder ASCENDING = new DomainOrder("DomainOrder.ASCENDING");
  final public static DomainOrder DESCENDING = new DomainOrder("DomainOrder.DESCENDING");
  private String name;
  private DomainOrder(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(DomainOrder.ASCENDING)) {
      return DomainOrder.ASCENDING;
    }
    else {
      DomainOrder var_3830 = DomainOrder.DESCENDING;
      if(this.equals(var_3830)) {
        return DomainOrder.DESCENDING;
      }
      else 
        if(this.equals(DomainOrder.NONE)) {
          return DomainOrder.NONE;
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
    if(!(obj instanceof DomainOrder)) {
      return false;
    }
    DomainOrder that = (DomainOrder)obj;
    if(!this.name.equals(that.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}