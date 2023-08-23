package org.jfree.chart.entity;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;

public class StandardEntityCollection implements EntityCollection, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 5384773031184897047L;
  private List entities;
  public StandardEntityCollection() {
    super();
    this.entities = new java.util.ArrayList();
  }
  public ChartEntity getEntity(double x, double y) {
    int entityCount = this.entities.size();
    for(int i = entityCount - 1; i >= 0; i--) {
      ChartEntity entity = (ChartEntity)this.entities.get(i);
      if(entity.getArea().contains(x, y)) {
        return entity;
      }
    }
    return null;
  }
  public ChartEntity getEntity(int index) {
    return (ChartEntity)this.entities.get(index);
  }
  public Collection getEntities() {
    return Collections.unmodifiableCollection(this.entities);
  }
  public Iterator iterator() {
    return this.entities.iterator();
  }
  public Object clone() throws CloneNotSupportedException {
    StandardEntityCollection clone = (StandardEntityCollection)super.clone();
    int var_1159 = this.entities.size();
    clone.entities = new java.util.ArrayList(var_1159);
    for(int i = 0; i < this.entities.size(); i++) {
      ChartEntity entity = (ChartEntity)this.entities.get(i);
      clone.entities.add(entity.clone());
    }
    return clone;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(obj instanceof StandardEntityCollection) {
      StandardEntityCollection that = (StandardEntityCollection)obj;
      return ObjectUtilities.equal(this.entities, that.entities);
    }
    return false;
  }
  public int getEntityCount() {
    return this.entities.size();
  }
  public void add(ChartEntity entity) {
    if(entity == null) {
      throw new IllegalArgumentException("Null \'entity\' argument.");
    }
    this.entities.add(entity);
  }
  public void addAll(EntityCollection collection) {
    this.entities.addAll(collection.getEntities());
  }
  public void clear() {
    this.entities.clear();
  }
}