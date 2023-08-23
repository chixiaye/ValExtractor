package org.jfree.data.xy;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DomainInfo;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.event.DatasetChangeEvent;

public class XYSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, DomainInfo, RangeInfo, XYDatasetSelectionState, SelectableXYDataset, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -7590013825931496766L;
  private List data;
  private IntervalXYDelegate intervalDelegate;
  public XYSeriesCollection() {
    this(null);
  }
  public XYSeriesCollection(XYSeries series) {
    super();
    this.data = new java.util.ArrayList();
    this.intervalDelegate = new IntervalXYDelegate(this, false);
    addChangeListener(this.intervalDelegate);
    if(series != null) {
      this.data.add(series);
      series.addChangeListener(this);
    }
    setSelectionState(this);
  }
  public Comparable getSeriesKey(int series) {
    return getSeries(series).getKey();
  }
  public DomainOrder getDomainOrder() {
    int seriesCount = getSeriesCount();
    for(int i = 0; i < seriesCount; i++) {
      XYSeries s = getSeries(i);
      if(!s.getAutoSort()) {
        return DomainOrder.NONE;
      }
    }
    return DomainOrder.ASCENDING;
  }
  public List getSeries() {
    return Collections.unmodifiableList(this.data);
  }
  public Number getEndX(int series, int item) {
    return this.intervalDelegate.getEndX(series, item);
  }
  public Number getEndY(int series, int item) {
    return getY(series, item);
  }
  public Number getStartX(int series, int item) {
    return this.intervalDelegate.getStartX(series, item);
  }
  public Number getStartY(int series, int item) {
    return getY(series, item);
  }
  public Number getX(int series, int item) {
    XYSeries s = (XYSeries)this.data.get(series);
    return s.getX(item);
  }
  public Number getY(int series, int index) {
    XYSeries s = (XYSeries)this.data.get(series);
    return s.getY(index);
  }
  public Object clone() throws CloneNotSupportedException {
    XYSeriesCollection clone = (XYSeriesCollection)super.clone();
    clone.data = (List)ObjectUtilities.deepClone(this.data);
    clone.intervalDelegate = (IntervalXYDelegate)this.intervalDelegate.clone();
    return clone;
  }
  public Range getDomainBounds(boolean includeInterval) {
    if(includeInterval) {
      return this.intervalDelegate.getDomainBounds(includeInterval);
    }
    else {
      double lower = Double.POSITIVE_INFINITY;
      double upper = Double.NEGATIVE_INFINITY;
      int seriesCount = getSeriesCount();
      for(int s = 0; s < seriesCount; s++) {
        XYSeries series = getSeries(s);
        double minX = series.getMinX();
        if(!Double.isNaN(minX)) {
          lower = Math.min(lower, minX);
        }
        double maxX = series.getMaxX();
        if(!Double.isNaN(maxX)) {
          upper = Math.max(upper, maxX);
        }
      }
      if(lower > upper) {
        return null;
      }
      else {
        return new Range(lower, upper);
      }
    }
  }
  public Range getRangeBounds(boolean includeInterval) {
    double lower = Double.POSITIVE_INFINITY;
    double upper = Double.NEGATIVE_INFINITY;
    int seriesCount = getSeriesCount();
    for(int s = 0; s < seriesCount; s++) {
      XYSeries series = getSeries(s);
      double minY = series.getMinY();
      if(!Double.isNaN(minY)) {
        lower = Math.min(lower, minY);
      }
      double maxY = series.getMaxY();
      if(!Double.isNaN(maxY)) {
        upper = Math.max(upper, maxY);
      }
    }
    if(lower > upper) {
      return null;
    }
    else {
      return new Range(lower, upper);
    }
  }
  public XYSeries getSeries(int series) {
    if((series < 0) || (series >= getSeriesCount())) {
      throw new IllegalArgumentException("Series index out of bounds");
    }
    return (XYSeries)this.data.get(series);
  }
  public XYSeries getSeries(Comparable key) {
    if(key == null) {
      throw new IllegalArgumentException("Null \'key\' argument.");
    }
    Iterator iterator = this.data.iterator();
    while(iterator.hasNext()){
      XYSeries series = (XYSeries)iterator.next();
      if(key.equals(series.getKey())) {
        return series;
      }
    }
    throw new UnknownKeyException("Key not found: " + key);
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYSeriesCollection)) {
      return false;
    }
    XYSeriesCollection that = (XYSeriesCollection)obj;
    if(!this.intervalDelegate.equals(that.intervalDelegate)) {
      return false;
    }
    return ObjectUtilities.equal(this.data, that.data);
  }
  public boolean isAutoWidth() {
    return this.intervalDelegate.isAutoWidth();
  }
  public boolean isSelected(int series, int item) {
    XYSeries s = getSeries(series);
    XYDataItem i = s.getRawDataItem(item);
    return i.isSelected();
  }
  public double getDomainLowerBound(boolean includeInterval) {
    if(includeInterval) {
      return this.intervalDelegate.getDomainLowerBound(includeInterval);
    }
    else {
      double result = Double.NaN;
      int seriesCount = getSeriesCount();
      for(int s = 0; s < seriesCount; s++) {
        XYSeries series = getSeries(s);
        double lowX = series.getMinX();
        if(Double.isNaN(result)) {
          result = lowX;
        }
        else {
          if(!Double.isNaN(lowX)) {
            result = Math.min(result, lowX);
          }
        }
      }
      return result;
    }
  }
  public double getDomainUpperBound(boolean includeInterval) {
    if(includeInterval) {
      return this.intervalDelegate.getDomainUpperBound(includeInterval);
    }
    else {
      double result = Double.NaN;
      int seriesCount = getSeriesCount();
      for(int s = 0; s < seriesCount; s++) {
        XYSeries series = getSeries(s);
        double hiX = series.getMaxX();
        if(Double.isNaN(result)) {
          result = hiX;
        }
        else {
          if(!Double.isNaN(hiX)) {
            result = Math.max(result, hiX);
          }
        }
      }
      return result;
    }
  }
  public double getIntervalPositionFactor() {
    return this.intervalDelegate.getIntervalPositionFactor();
  }
  public double getIntervalWidth() {
    return this.intervalDelegate.getIntervalWidth();
  }
  public double getRangeLowerBound(boolean includeInterval) {
    double result = Double.NaN;
    int seriesCount = getSeriesCount();
    for(int s = 0; s < seriesCount; s++) {
      XYSeries series = getSeries(s);
      double lowY = series.getMinY();
      if(Double.isNaN(result)) {
        result = lowY;
      }
      else {
        if(!Double.isNaN(lowY)) {
          result = Math.min(result, lowY);
        }
      }
    }
    return result;
  }
  public double getRangeUpperBound(boolean includeInterval) {
    double result = Double.NaN;
    int seriesCount = getSeriesCount();
    for(int s = 0; s < seriesCount; s++) {
      XYSeries series = getSeries(s);
      double hiY = series.getMaxY();
      if(Double.isNaN(result)) {
        result = hiY;
      }
      else {
        if(!Double.isNaN(hiY)) {
          result = Math.max(result, hiY);
        }
      }
    }
    return result;
  }
  public int getItemCount(int series) {
    return getSeries(series).getItemCount();
  }
  public int getSeriesCount() {
    return this.data.size();
  }
  public int hashCode() {
    int hash = 5;
    hash = HashUtilities.hashCode(hash, this.intervalDelegate);
    hash = HashUtilities.hashCode(hash, this.data);
    return hash;
  }
  public int indexOf(XYSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    return this.data.indexOf(series);
  }
  public void addSeries(XYSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    this.data.add(series);
    series.addChangeListener(this);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void clearSelection() {
    int seriesCount = getSeriesCount();
    for(int s = 0; s < seriesCount; s++) {
      int itemCount = getItemCount(s);
      for(int i = 0; i < itemCount; i++) {
        setSelected(s, i, false, false);
      }
    }
  }
  public void fireSelectionEvent() {
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeAllSeries() {
    for(int i = 0; i < this.data.size(); i++) {
      XYSeries series = (XYSeries)this.data.get(i);
      series.removeChangeListener(this);
    }
    this.data.clear();
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeSeries(int series) {
    if((series < 0) || (series >= getSeriesCount())) {
      throw new IllegalArgumentException("Series index out of bounds.");
    }
    List var_4399 = this.data;
    XYSeries ts = (XYSeries)var_4399.get(series);
    ts.removeChangeListener(this);
    this.data.remove(series);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeSeries(XYSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    if(this.data.contains(series)) {
      series.removeChangeListener(this);
      this.data.remove(series);
      fireDatasetChanged(new DatasetChangeInfo());
    }
  }
  public void setAutoWidth(boolean b) {
    this.intervalDelegate.setAutoWidth(b);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void setIntervalPositionFactor(double factor) {
    this.intervalDelegate.setIntervalPositionFactor(factor);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void setIntervalWidth(double width) {
    if(width < 0.0D) {
      throw new IllegalArgumentException("Negative \'width\' argument.");
    }
    this.intervalDelegate.setFixedIntervalWidth(width);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void setSelected(int series, int item, boolean selected) {
    setSelected(series, item, selected, true);
  }
  public void setSelected(int series, int item, boolean selected, boolean notify) {
    XYSeries s = getSeries(series);
    XYDataItem i = s.getRawDataItem(item);
    i.setSelected(selected);
    if(notify) {
      fireSelectionEvent();
    }
  }
}