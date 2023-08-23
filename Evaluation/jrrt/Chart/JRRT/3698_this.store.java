package org.jfree.chart.util;
import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PaintMap implements Cloneable, Serializable  {
  final static long serialVersionUID = -4639833772123069274L;
  private transient Map store;
  public PaintMap() {
    super();
    this.store = new HashMap();
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Paint getPaint(Comparable key) {
    if(key == null) {
      throw new IllegalArgumentException("Null \'key\' argument.");
    }
    return (Paint)this.store.get(key);
  }
  public boolean containsKey(Comparable key) {
    return this.store.containsKey(key);
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof PaintMap)) {
      return false;
    }
    PaintMap that = (PaintMap)obj;
    if(this.store.size() != that.store.size()) {
      return false;
    }
    Set keys = this.store.keySet();
    Iterator iterator = keys.iterator();
    while(iterator.hasNext()){
      Comparable key = (Comparable)iterator.next();
      Paint p1 = getPaint(key);
      Paint p2 = that.getPaint(key);
      if(!PaintUtilities.equal(p1, p2)) {
        return false;
      }
    }
    return true;
  }
  public void clear() {
    this.store.clear();
  }
  public void put(Comparable key, Paint paint) {
    if(key == null) {
      throw new IllegalArgumentException("Null \'key\' argument.");
    }
    this.store.put(key, paint);
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.store = new HashMap();
    int keyCount = stream.readInt();
    for(int i = 0; i < keyCount; i++) {
      Comparable key = (Comparable)stream.readObject();
      Paint paint = SerialUtilities.readPaint(stream);
      this.store.put(key, paint);
    }
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Map var_3698 = this.store;
    stream.writeInt(var_3698.size());
    Set keys = this.store.keySet();
    Iterator iterator = keys.iterator();
    while(iterator.hasNext()){
      Comparable key = (Comparable)iterator.next();
      stream.writeObject(key);
      Paint paint = getPaint(key);
      SerialUtilities.writePaint(paint, stream);
    }
  }
}