package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class TableOrder implements Serializable  {
  final private static long serialVersionUID = 525193294068177057L;
  final public static TableOrder BY_ROW = new TableOrder("TableOrder.BY_ROW");
  final public static TableOrder BY_COLUMN = new TableOrder("TableOrder.BY_COLUMN");
  private String name;
  private TableOrder(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(TableOrder.BY_ROW)) {
      return TableOrder.BY_ROW;
    }
    else {
      TableOrder var_3728 = TableOrder.BY_COLUMN;
      if(this.equals(var_3728)) {
        return TableOrder.BY_COLUMN;
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
    if(!(obj instanceof TableOrder)) {
      return false;
    }
    TableOrder that = (TableOrder)obj;
    if(!this.name.equals(that.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}