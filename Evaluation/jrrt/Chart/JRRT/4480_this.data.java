package org.jfree.data.xy;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.data.general.Series;
import org.jfree.data.event.SeriesChangeEvent;
import org.jfree.data.general.SeriesException;

public class XYSeries extends Series implements Cloneable, Serializable  {
  final static long serialVersionUID = -5908509288197150436L;
  protected List data;
  private int maximumItemCount = Integer.MAX_VALUE;
  private boolean autoSort;
  private boolean allowDuplicateXValues;
  private double minX;
  private double maxX;
  private double minY;
  private double maxY;
  public XYSeries(Comparable key) {
    this(key, true, true);
  }
  public XYSeries(Comparable key, boolean autoSort) {
    this(key, autoSort, true);
  }
  public XYSeries(Comparable key, boolean autoSort, boolean allowDuplicateXValues) {
    super(key);
    this.data = new java.util.ArrayList();
    this.autoSort = autoSort;
    this.allowDuplicateXValues = allowDuplicateXValues;
    this.minX = Double.NaN;
    this.maxX = Double.NaN;
    this.minY = Double.NaN;
    this.maxY = Double.NaN;
  }
  public List getItems() {
    return Collections.unmodifiableList(this.data);
  }
  public Number getX(int index) {
    return getRawDataItem(index).getX();
  }
  public Number getY(int index) {
    return getRawDataItem(index).getY();
  }
  public Object clone() throws CloneNotSupportedException {
    XYSeries clone = (XYSeries)super.clone();
    clone.data = (List)ObjectUtilities.deepClone(this.data);
    return clone;
  }
  public XYDataItem addOrUpdate(double x, double y) {
    return addOrUpdate(new Double(x), new Double(y));
  }
  public XYDataItem addOrUpdate(Number x, Number y) {
    return addOrUpdate(new XYDataItem(x, y));
  }
  public XYDataItem addOrUpdate(XYDataItem item) {
    if(item == null) {
      throw new IllegalArgumentException("Null \'item\' argument.");
    }
    if(this.allowDuplicateXValues) {
      add(item);
      return null;
    }
    XYDataItem overwritten = null;
    int index = indexOf(item.getX());
    if(index >= 0) {
      List var_4480 = this.data;
      XYDataItem existing = (XYDataItem)var_4480.get(index);
      overwritten = (XYDataItem)existing.clone();
      boolean iterate = false;
      double oldY = existing.getYValue();
      if(!Double.isNaN(oldY)) {
        iterate = oldY <= this.minY || oldY >= this.maxY;
      }
      existing.setY(item.getY());
      if(iterate) {
        findBoundsByIteration();
      }
      else 
        if(item.getY() != null) {
          double yy = item.getY().doubleValue();
          this.minY = minIgnoreNaN(this.minY, yy);
          this.maxY = minIgnoreNaN(this.maxY, yy);
        }
    }
    else {
      item = (XYDataItem)item.clone();
      if(this.autoSort) {
        this.data.add(-index - 1, item);
      }
      else {
        this.data.add(item);
      }
      updateBoundsForAddedItem(item);
      if(getItemCount() > this.maximumItemCount) {
        XYDataItem removed = (XYDataItem)this.data.remove(0);
        updateBoundsForRemovedItem(removed);
      }
    }
    fireSeriesChanged();
    return overwritten;
  }
  public XYDataItem getDataItem(int index) {
    XYDataItem item = (XYDataItem)this.data.get(index);
    return (XYDataItem)item.clone();
  }
  XYDataItem getRawDataItem(int index) {
    return (XYDataItem)this.data.get(index);
  }
  public XYDataItem remove(int index) {
    XYDataItem removed = (XYDataItem)this.data.remove(index);
    updateBoundsForRemovedItem(removed);
    fireSeriesChanged();
    return removed;
  }
  public XYDataItem remove(Number x) {
    return remove(indexOf(x));
  }
  public XYSeries createCopy(int start, int end) throws CloneNotSupportedException {
    XYSeries copy = (XYSeries)super.clone();
    copy.data = new java.util.ArrayList();
    if(this.data.size() > 0) {
      for(int index = start; index <= end; index++) {
        XYDataItem item = (XYDataItem)this.data.get(index);
        XYDataItem clone = (XYDataItem)item.clone();
        try {
          copy.add(clone);
        }
        catch (SeriesException e) {
          System.err.println("Unable to add cloned data item.");
        }
      }
    }
    return copy;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYSeries)) {
      return false;
    }
    if(!super.equals(obj)) {
      return false;
    }
    XYSeries that = (XYSeries)obj;
    if(this.maximumItemCount != that.maximumItemCount) {
      return false;
    }
    if(this.autoSort != that.autoSort) {
      return false;
    }
    if(this.allowDuplicateXValues != that.allowDuplicateXValues) {
      return false;
    }
    if(!ObjectUtilities.equal(this.data, that.data)) {
      return false;
    }
    return true;
  }
  public boolean getAllowDuplicateXValues() {
    return this.allowDuplicateXValues;
  }
  public boolean getAutoSort() {
    return this.autoSort;
  }
  public double getMaxX() {
    return this.maxX;
  }
  public double getMaxY() {
    return this.maxY;
  }
  public double getMinX() {
    return this.minX;
  }
  public double getMinY() {
    return this.minY;
  }
  private double maxIgnoreNaN(double a, double b) {
    if(Double.isNaN(a)) {
      return b;
    }
    else {
      if(Double.isNaN(b)) {
        return a;
      }
      else {
        return Math.max(a, b);
      }
    }
  }
  private double minIgnoreNaN(double a, double b) {
    if(Double.isNaN(a)) {
      return b;
    }
    else {
      if(Double.isNaN(b)) {
        return a;
      }
      else {
        return Math.min(a, b);
      }
    }
  }
  public double[][] toArray() {
    int itemCount = getItemCount();
    double[][] result = new double[2][itemCount];
    for(int i = 0; i < itemCount; i++) {
      result[0][i] = this.getX(i).doubleValue();
      Number y = getY(i);
      if(y != null) {
        result[1][i] = y.doubleValue();
      }
      else {
        result[1][i] = Double.NaN;
      }
    }
    return result;
  }
  public int getItemCount() {
    return this.data.size();
  }
  public int getMaximumItemCount() {
    return this.maximumItemCount;
  }
  public int hashCode() {
    int result = super.hashCode();
    int count = getItemCount();
    if(count > 0) {
      XYDataItem item = getRawDataItem(0);
      result = 29 * result + item.hashCode();
    }
    if(count > 1) {
      XYDataItem item = getRawDataItem(count - 1);
      result = 29 * result + item.hashCode();
    }
    if(count > 2) {
      XYDataItem item = getRawDataItem(count / 2);
      result = 29 * result + item.hashCode();
    }
    result = 29 * result + this.maximumItemCount;
    result = 29 * result + (this.autoSort ? 1 : 0);
    result = 29 * result + (this.allowDuplicateXValues ? 1 : 0);
    return result;
  }
  public int indexOf(Number x) {
    if(this.autoSort) {
      return Collections.binarySearch(this.data, new XYDataItem(x, null));
    }
    else {
      for(int i = 0; i < this.data.size(); i++) {
        XYDataItem item = (XYDataItem)this.data.get(i);
        if(item.getX().equals(x)) {
          return i;
        }
      }
      return -1;
    }
  }
  public void add(double x, double y) {
    add(new Double(x), new Double(y), true);
  }
  public void add(double x, double y, boolean notify) {
    add(new Double(x), new Double(y), notify);
  }
  public void add(double x, Number y) {
    add(new Double(x), y);
  }
  public void add(double x, Number y, boolean notify) {
    add(new Double(x), y, notify);
  }
  public void add(Number x, Number y) {
    add(x, y, true);
  }
  public void add(Number x, Number y, boolean notify) {
    XYDataItem item = new XYDataItem(x, y);
    add(item, notify);
  }
  public void add(XYDataItem item) {
    add(item, true);
  }
  public void add(XYDataItem item, boolean notify) {
    if(item == null) {
      throw new IllegalArgumentException("Null \'item\' argument.");
    }
    item = (XYDataItem)item.clone();
    if(this.autoSort) {
      int index = Collections.binarySearch(this.data, item);
      if(index < 0) {
        this.data.add(-index - 1, item);
      }
      else {
        if(this.allowDuplicateXValues) {
          int size = this.data.size();
          while(index < size && item.compareTo(this.data.get(index)) == 0){
            index++;
          }
          if(index < this.data.size()) {
            this.data.add(index, item);
          }
          else {
            this.data.add(item);
          }
        }
        else {
          throw new SeriesException("X-value already exists.");
        }
      }
    }
    else {
      if(!this.allowDuplicateXValues) {
        int index = indexOf(item.getX());
        if(index >= 0) {
          throw new SeriesException("X-value already exists.");
        }
      }
      this.data.add(item);
    }
    updateBoundsForAddedItem(item);
    if(getItemCount() > this.maximumItemCount) {
      XYDataItem removed = (XYDataItem)this.data.remove(0);
      updateBoundsForRemovedItem(removed);
    }
    if(notify) {
      fireSeriesChanged();
    }
  }
  public void clear() {
    if(this.data.size() > 0) {
      this.data.clear();
      this.minX = Double.NaN;
      this.maxX = Double.NaN;
      this.minY = Double.NaN;
      this.maxY = Double.NaN;
      fireSeriesChanged();
    }
  }
  public void delete(int start, int end) {
    this.data.subList(start, end + 1).clear();
    findBoundsByIteration();
    fireSeriesChanged();
  }
  private void findBoundsByIteration() {
    this.minX = Double.NaN;
    this.maxX = Double.NaN;
    this.minY = Double.NaN;
    this.maxY = Double.NaN;
    Iterator iterator = this.data.iterator();
    while(iterator.hasNext()){
      XYDataItem item = (XYDataItem)iterator.next();
      updateBoundsForAddedItem(item);
    }
  }
  public void setMaximumItemCount(int maximum) {
    this.maximumItemCount = maximum;
    int remove = this.data.size() - maximum;
    if(remove > 0) {
      this.data.subList(0, remove).clear();
      findBoundsByIteration();
      fireSeriesChanged();
    }
  }
  public void update(Number x, Number y) {
    int index = indexOf(x);
    if(index < 0) {
      throw new SeriesException("No observation for x = " + x);
    }
    else {
      updateByIndex(index, y);
    }
  }
  private void updateBoundsForAddedItem(XYDataItem item) {
    double x = item.getXValue();
    this.minX = minIgnoreNaN(this.minX, x);
    this.maxX = maxIgnoreNaN(this.maxX, x);
    if(item.getY() != null) {
      double y = item.getYValue();
      this.minY = minIgnoreNaN(this.minY, y);
      this.maxY = maxIgnoreNaN(this.maxY, y);
    }
  }
  private void updateBoundsForRemovedItem(XYDataItem item) {
    boolean itemContributesToXBounds = false;
    boolean itemContributesToYBounds = false;
    double x = item.getXValue();
    if(!Double.isNaN(x)) {
      if(x <= this.minX || x >= this.maxX) {
        itemContributesToXBounds = true;
      }
    }
    if(item.getY() != null) {
      double y = item.getYValue();
      if(!Double.isNaN(y)) {
        if(y <= this.minY || y >= this.maxY) {
          itemContributesToYBounds = true;
        }
      }
    }
    if(itemContributesToYBounds) {
      findBoundsByIteration();
    }
    else 
      if(itemContributesToXBounds) {
        if(getAutoSort()) {
          this.minX = getX(0).doubleValue();
          this.maxX = getX(getItemCount() - 1).doubleValue();
        }
        else {
          findBoundsByIteration();
        }
      }
  }
  public void updateByIndex(int index, Number y) {
    XYDataItem item = getRawDataItem(index);
    boolean iterate = false;
    double oldY = item.getYValue();
    if(!Double.isNaN(oldY)) {
      iterate = oldY <= this.minY || oldY >= this.maxY;
    }
    item.setY(y);
    if(iterate) {
      findBoundsByIteration();
    }
    else 
      if(y != null) {
        double yy = y.doubleValue();
        this.minY = minIgnoreNaN(this.minY, yy);
        this.maxY = maxIgnoreNaN(this.maxY, yy);
      }
    fireSeriesChanged();
  }
}