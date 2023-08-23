package org.jfree.chart.util;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StrokeMap implements Cloneable, Serializable  {
  final static long serialVersionUID = -8148916785963525169L;
  private transient Map store;
  public StrokeMap() {
    super();
    this.store = new TreeMap();
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Stroke getStroke(Comparable key) {
    if(key == null) {
      throw new IllegalArgumentException("Null \'key\' argument.");
    }
    return (Stroke)this.store.get(key);
  }
  public boolean containsKey(Comparable key) {
    return this.store.containsKey(key);
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof StrokeMap)) {
      return false;
    }
    StrokeMap that = (StrokeMap)obj;
    Map var_3694 = this.store;
    if(var_3694.size() != that.store.size()) {
      return false;
    }
    Set keys = this.store.keySet();
    Iterator iterator = keys.iterator();
    while(iterator.hasNext()){
      Comparable key = (Comparable)iterator.next();
      Stroke s1 = getStroke(key);
      Stroke s2 = that.getStroke(key);
      if(!ObjectUtilities.equal(s1, s2)) {
        return false;
      }
    }
    return true;
  }
  public void clear() {
    this.store.clear();
  }
  public void put(Comparable key, Stroke stroke) {
    if(key == null) {
      throw new IllegalArgumentException("Null \'key\' argument.");
    }
    this.store.put(key, stroke);
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.store = new TreeMap();
    int keyCount = stream.readInt();
    for(int i = 0; i < keyCount; i++) {
      Comparable key = (Comparable)stream.readObject();
      Stroke stroke = SerialUtilities.readStroke(stream);
      this.store.put(key, stroke);
    }
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeInt(this.store.size());
    Set keys = this.store.keySet();
    Iterator iterator = keys.iterator();
    while(iterator.hasNext()){
      Comparable key = (Comparable)iterator.next();
      stream.writeObject(key);
      Stroke stroke = getStroke(key);
      SerialUtilities.writeStroke(stroke, stream);
    }
  }
}