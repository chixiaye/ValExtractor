package org.jfree.data.xy;
import java.io.Serializable;
import java.util.List;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.event.DatasetChangeEvent;

public class YIntervalSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, PublicCloneable, Serializable  {
  private List data;
  public YIntervalSeriesCollection() {
    super();
    this.data = new java.util.ArrayList();
  }
  public Comparable getSeriesKey(int series) {
    return getSeries(series).getKey();
  }
  public Number getEndX(int series, int item) {
    return getX(series, item);
  }
  public Number getEndY(int series, int item) {
    YIntervalSeries s = (YIntervalSeries)this.data.get(series);
    return new Double(s.getYHighValue(item));
  }
  public Number getStartX(int series, int item) {
    return getX(series, item);
  }
  public Number getStartY(int series, int item) {
    YIntervalSeries s = (YIntervalSeries)this.data.get(series);
    return new Double(s.getYLowValue(item));
  }
  public Number getX(int series, int item) {
    YIntervalSeries s = (YIntervalSeries)this.data.get(series);
    return s.getX(item);
  }
  public Number getY(int series, int item) {
    YIntervalSeries s = (YIntervalSeries)this.data.get(series);
    return new Double(s.getYValue(item));
  }
  public Object clone() throws CloneNotSupportedException {
    YIntervalSeriesCollection clone = (YIntervalSeriesCollection)super.clone();
    clone.data = (List)ObjectUtilities.deepClone(this.data);
    return clone;
  }
  public YIntervalSeries getSeries(int series) {
    if((series < 0) || (series >= getSeriesCount())) {
      throw new IllegalArgumentException("Series index out of bounds");
    }
    return (YIntervalSeries)this.data.get(series);
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof YIntervalSeriesCollection)) {
      return false;
    }
    YIntervalSeriesCollection that = (YIntervalSeriesCollection)obj;
    return ObjectUtilities.equal(this.data, that.data);
  }
  public double getEndYValue(int series, int item) {
    YIntervalSeries s = (YIntervalSeries)this.data.get(series);
    return s.getYHighValue(item);
  }
  public double getStartYValue(int series, int item) {
    YIntervalSeries s = (YIntervalSeries)this.data.get(series);
    return s.getYLowValue(item);
  }
  public double getYValue(int series, int item) {
    YIntervalSeries s = (YIntervalSeries)this.data.get(series);
    return s.getYValue(item);
  }
  public int getItemCount(int series) {
    return getSeries(series).getItemCount();
  }
  public int getSeriesCount() {
    return this.data.size();
  }
  public void addSeries(YIntervalSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    this.data.add(series);
    series.addChangeListener(this);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeAllSeries() {
    for(int i = 0; i < this.data.size(); i++) {
      List var_4490 = this.data;
      YIntervalSeries series = (YIntervalSeries)var_4490.get(i);
      series.removeChangeListener(this);
    }
    this.data.clear();
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeSeries(int series) {
    if((series < 0) || (series >= getSeriesCount())) {
      throw new IllegalArgumentException("Series index out of bounds.");
    }
    YIntervalSeries ts = (YIntervalSeries)this.data.get(series);
    ts.removeChangeListener(this);
    this.data.remove(series);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeSeries(YIntervalSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    if(this.data.contains(series)) {
      series.removeChangeListener(this);
      this.data.remove(series);
      fireDatasetChanged(new DatasetChangeInfo());
    }
  }
}