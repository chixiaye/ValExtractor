package org.jfree.chart.util;
import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PaintList extends AbstractObjectList  {
  public PaintList() {
    super();
  }
  public Paint getPaint(int index) {
    return (Paint)get(index);
  }
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }
    if(obj == this) {
      return true;
    }
    if(obj instanceof PaintList) {
      PaintList that = (PaintList)obj;
      int listSize = size();
      for(int i = 0; i < listSize; i++) {
        if(!PaintUtilities.equal(getPaint(i), that.getPaint(i))) {
          return false;
        }
      }
    }
    return true;
  }
  public int hashCode() {
    int result = 127;
    int size = size();
    result = HashUtilities.hashCode(result, size());
    if(size > 0) {
      result = HashUtilities.hashCode(result, getPaint(0));
      if(size > 1) {
        result = HashUtilities.hashCode(result, getPaint(size - 1));
        if(size > 2) {
          result = HashUtilities.hashCode(result, getPaint(size / 2));
        }
      }
    }
    return result;
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int var_3691 = stream.readInt();
    int count = var_3691;
    for(int i = 0; i < count; i++) {
      final int index = stream.readInt();
      if(index != -1) {
        setPaint(index, SerialUtilities.readPaint(stream));
      }
    }
  }
  public void setPaint(int index, Paint paint) {
    set(index, paint);
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    int count = size();
    stream.writeInt(count);
    for(int i = 0; i < count; i++) {
      Paint paint = getPaint(i);
      if(paint != null) {
        stream.writeInt(i);
        SerialUtilities.writePaint(paint, stream);
      }
      else {
        stream.writeInt(-1);
      }
    }
  }
}