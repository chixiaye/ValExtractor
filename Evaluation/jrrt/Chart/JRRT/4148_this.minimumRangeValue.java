package org.jfree.data.statistics;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.xy.AbstractXYDataset;

public class DefaultBoxAndWhiskerXYDataset extends AbstractXYDataset implements BoxAndWhiskerXYDataset, RangeInfo  {
  private Comparable seriesKey;
  private List dates;
  private List items;
  private Number minimumRangeValue;
  private Number maximumRangeValue;
  private Range rangeBounds;
  private double outlierCoefficient = 1.5D;
  private double faroutCoefficient = 2.0D;
  public DefaultBoxAndWhiskerXYDataset(Comparable seriesKey) {
    super();
    this.seriesKey = seriesKey;
    this.dates = new ArrayList();
    this.items = new ArrayList();
    this.minimumRangeValue = null;
    this.maximumRangeValue = null;
    this.rangeBounds = null;
  }
  public BoxAndWhiskerItem getItem(int series, int item) {
    return (BoxAndWhiskerItem)this.items.get(item);
  }
  public Comparable getSeriesKey(int i) {
    return this.seriesKey;
  }
  public Date getXDate(int series, int item) {
    return (Date)this.dates.get(item);
  }
  public List getOutliers(int series, int item) {
    List result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getOutliers();
    }
    return result;
  }
  public Number getMaxOutlier(int series, int item) {
    Number result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getMaxOutlier();
    }
    return result;
  }
  public Number getMaxRegularValue(int series, int item) {
    Number result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getMaxRegularValue();
    }
    return result;
  }
  public Number getMeanValue(int series, int item) {
    Number result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getMean();
    }
    return result;
  }
  public Number getMedianValue(int series, int item) {
    Number result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getMedian();
    }
    return result;
  }
  public Number getMinOutlier(int series, int item) {
    Number result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getMinOutlier();
    }
    return result;
  }
  public Number getMinRegularValue(int series, int item) {
    Number result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getMinRegularValue();
    }
    return result;
  }
  public Number getQ1Value(int series, int item) {
    Number result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getQ1();
    }
    return result;
  }
  public Number getQ3Value(int series, int item) {
    Number result = null;
    BoxAndWhiskerItem stats = (BoxAndWhiskerItem)this.items.get(item);
    if(stats != null) {
      result = stats.getQ3();
    }
    return result;
  }
  public Number getX(int series, int item) {
    return new Long(((Date)this.dates.get(item)).getTime());
  }
  public Number getY(int series, int item) {
    return getMeanValue(series, item);
  }
  public Object clone() throws CloneNotSupportedException {
    DefaultBoxAndWhiskerXYDataset clone = (DefaultBoxAndWhiskerXYDataset)super.clone();
    clone.dates = new java.util.ArrayList(this.dates);
    clone.items = new java.util.ArrayList(this.items);
    return clone;
  }
  public Range getRangeBounds(boolean includeInterval) {
    return this.rangeBounds;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof DefaultBoxAndWhiskerXYDataset)) {
      return false;
    }
    DefaultBoxAndWhiskerXYDataset that = (DefaultBoxAndWhiskerXYDataset)obj;
    if(!ObjectUtilities.equal(this.seriesKey, that.seriesKey)) {
      return false;
    }
    if(!this.dates.equals(that.dates)) {
      return false;
    }
    if(!this.items.equals(that.items)) {
      return false;
    }
    return true;
  }
  public double getFaroutCoefficient() {
    return this.faroutCoefficient;
  }
  public double getOutlierCoefficient() {
    return this.outlierCoefficient;
  }
  public double getRangeLowerBound(boolean includeInterval) {
    double result = Double.NaN;
    Number var_4148 = this.minimumRangeValue;
    if(var_4148 != null) {
      result = this.minimumRangeValue.doubleValue();
    }
    return result;
  }
  public double getRangeUpperBound(boolean includeInterval) {
    double result = Double.NaN;
    if(this.maximumRangeValue != null) {
      result = this.maximumRangeValue.doubleValue();
    }
    return result;
  }
  public int getItemCount(int series) {
    return this.dates.size();
  }
  public int getSeriesCount() {
    return 1;
  }
  public void add(Date date, BoxAndWhiskerItem item) {
    this.dates.add(date);
    this.items.add(item);
    if(this.minimumRangeValue == null) {
      this.minimumRangeValue = item.getMinRegularValue();
    }
    else {
      if(item.getMinRegularValue().doubleValue() < this.minimumRangeValue.doubleValue()) {
        this.minimumRangeValue = item.getMinRegularValue();
      }
    }
    if(this.maximumRangeValue == null) {
      this.maximumRangeValue = item.getMaxRegularValue();
    }
    else {
      if(item.getMaxRegularValue().doubleValue() > this.maximumRangeValue.doubleValue()) {
        this.maximumRangeValue = item.getMaxRegularValue();
      }
    }
    this.rangeBounds = new Range(this.minimumRangeValue.doubleValue(), this.maximumRangeValue.doubleValue());
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void setFaroutCoefficient(double faroutCoefficient) {
    if(faroutCoefficient > getOutlierCoefficient()) {
      this.faroutCoefficient = faroutCoefficient;
    }
    else {
      throw new IllegalArgumentException("Farout value must be greater " + "than the outlier value, which is currently set at: (" + getOutlierCoefficient() + ")");
    }
  }
  public void setOutlierCoefficient(double outlierCoefficient) {
    this.outlierCoefficient = outlierCoefficient;
  }
}