package org.jfree.data.xy;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.event.DatasetChangeListener;

public class XYBarDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, DatasetChangeListener, PublicCloneable  {
  private XYDataset underlying;
  private double barWidth;
  public XYBarDataset(XYDataset underlying, double barWidth) {
    super();
    this.underlying = underlying;
    this.underlying.addChangeListener(this);
    this.barWidth = barWidth;
  }
  public Comparable getSeriesKey(int series) {
    return this.underlying.getSeriesKey(series);
  }
  public Number getEndX(int series, int item) {
    Number result = null;
    Number xnum = this.underlying.getX(series, item);
    if(xnum != null) {
      result = new Double(xnum.doubleValue() + this.barWidth / 2.0D);
    }
    return result;
  }
  public Number getEndY(int series, int item) {
    return this.underlying.getY(series, item);
  }
  public Number getStartX(int series, int item) {
    Number result = null;
    Number xnum = this.underlying.getX(series, item);
    if(xnum != null) {
      result = new Double(xnum.doubleValue() - this.barWidth / 2.0D);
    }
    return result;
  }
  public Number getStartY(int series, int item) {
    return this.underlying.getY(series, item);
  }
  public Number getX(int series, int item) {
    return this.underlying.getX(series, item);
  }
  public Number getY(int series, int item) {
    return this.underlying.getY(series, item);
  }
  public Object clone() throws CloneNotSupportedException {
    XYBarDataset clone = (XYBarDataset)super.clone();
    XYDataset var_4393 = this.underlying;
    if(var_4393 instanceof PublicCloneable) {
      PublicCloneable pc = (PublicCloneable)this.underlying;
      clone.underlying = (XYDataset)pc.clone();
    }
    return clone;
  }
  public XYDataset getUnderlyingDataset() {
    return this.underlying;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYBarDataset)) {
      return false;
    }
    XYBarDataset that = (XYBarDataset)obj;
    if(!this.underlying.equals(that.underlying)) {
      return false;
    }
    if(this.barWidth != that.barWidth) {
      return false;
    }
    return true;
  }
  public double getBarWidth() {
    return this.barWidth;
  }
  public double getEndXValue(int series, int item) {
    return getXValue(series, item) + this.barWidth / 2.0D;
  }
  public double getEndYValue(int series, int item) {
    return getYValue(series, item);
  }
  public double getStartXValue(int series, int item) {
    return getXValue(series, item) - this.barWidth / 2.0D;
  }
  public double getStartYValue(int series, int item) {
    return getYValue(series, item);
  }
  public double getXValue(int series, int item) {
    return this.underlying.getXValue(series, item);
  }
  public double getYValue(int series, int item) {
    return this.underlying.getYValue(series, item);
  }
  public int getItemCount(int series) {
    return this.underlying.getItemCount(series);
  }
  public int getSeriesCount() {
    return this.underlying.getSeriesCount();
  }
  public void datasetChanged(DatasetChangeEvent event) {
    notifyListeners(event);
  }
  public void setBarWidth(double barWidth) {
    this.barWidth = barWidth;
    fireDatasetChanged(new DatasetChangeInfo());
  }
}