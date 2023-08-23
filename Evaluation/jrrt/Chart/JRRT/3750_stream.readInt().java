package org.jfree.chart.util;
import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ShapeList extends AbstractObjectList  {
  public ShapeList() {
    super();
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Shape getShape(int index) {
    return (Shape)get(index);
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof ShapeList)) {
      return false;
    }
    ShapeList that = (ShapeList)obj;
    int listSize = size();
    for(int i = 0; i < listSize; i++) {
      if(!ShapeUtilities.equal((Shape)get(i), (Shape)that.get(i))) {
        return false;
      }
    }
    return true;
  }
  public int hashCode() {
    return super.hashCode();
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int var_3750 = stream.readInt();
    int count = var_3750;
    for(int i = 0; i < count; i++) {
      int index = stream.readInt();
      if(index != -1) {
        setShape(index, SerialUtilities.readShape(stream));
      }
    }
  }
  public void setShape(int index, Shape shape) {
    set(index, shape);
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    int count = size();
    stream.writeInt(count);
    for(int i = 0; i < count; i++) {
      Shape shape = getShape(i);
      if(shape != null) {
        stream.writeInt(i);
        SerialUtilities.writeShape(shape, stream);
      }
      else {
        stream.writeInt(-1);
      }
    }
  }
}