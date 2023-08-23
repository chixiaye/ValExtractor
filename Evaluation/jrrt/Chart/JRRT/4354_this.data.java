package org.jfree.data.time.ohlc;
import java.io.Serializable;
import java.util.List;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

public class OHLCSeriesCollection extends AbstractXYDataset implements OHLCDataset, Serializable  {
  private List data;
  private TimePeriodAnchor xPosition = TimePeriodAnchor.MIDDLE;
  public OHLCSeriesCollection() {
    super();
    this.data = new java.util.ArrayList();
  }
  public Comparable getSeriesKey(int series) {
    return getSeries(series).getKey();
  }
  public Number getClose(int series, int item) {
    return new Double(getCloseValue(series, item));
  }
  public Number getHigh(int series, int item) {
    return new Double(getHighValue(series, item));
  }
  public Number getLow(int series, int item) {
    return new Double(getLowValue(series, item));
  }
  public Number getOpen(int series, int item) {
    return new Double(getOpenValue(series, item));
  }
  public Number getVolume(int series, int item) {
    return null;
  }
  public Number getX(int series, int item) {
    return new Double(getXValue(series, item));
  }
  public Number getY(int series, int item) {
    OHLCSeries s = (OHLCSeries)this.data.get(series);
    OHLCItem di = (OHLCItem)s.getDataItem(item);
    return new Double(di.getYValue());
  }
  public OHLCSeries getSeries(int series) {
    if((series < 0) || (series >= getSeriesCount())) {
      throw new IllegalArgumentException("Series index out of bounds");
    }
    return (OHLCSeries)this.data.get(series);
  }
  public Object clone() throws CloneNotSupportedException {
    OHLCSeriesCollection clone = (OHLCSeriesCollection)super.clone();
    clone.data = (List)ObjectUtilities.deepClone(this.data);
    return clone;
  }
  public TimePeriodAnchor getXPosition() {
    return this.xPosition;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof OHLCSeriesCollection)) {
      return false;
    }
    OHLCSeriesCollection that = (OHLCSeriesCollection)obj;
    if(!this.xPosition.equals(that.xPosition)) {
      return false;
    }
    return ObjectUtilities.equal(this.data, that.data);
  }
  public boolean removeSeries(OHLCSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    boolean removed = this.data.remove(series);
    if(removed) {
      series.removeChangeListener(this);
      fireDatasetChanged(new DatasetChangeInfo());
    }
    return removed;
  }
  public double getCloseValue(int series, int item) {
    OHLCSeries s = (OHLCSeries)this.data.get(series);
    OHLCItem di = (OHLCItem)s.getDataItem(item);
    return di.getCloseValue();
  }
  public double getHighValue(int series, int item) {
    OHLCSeries s = (OHLCSeries)this.data.get(series);
    OHLCItem di = (OHLCItem)s.getDataItem(item);
    return di.getHighValue();
  }
  public double getLowValue(int series, int item) {
    OHLCSeries s = (OHLCSeries)this.data.get(series);
    OHLCItem di = (OHLCItem)s.getDataItem(item);
    return di.getLowValue();
  }
  public double getOpenValue(int series, int item) {
    OHLCSeries s = (OHLCSeries)this.data.get(series);
    OHLCItem di = (OHLCItem)s.getDataItem(item);
    return di.getOpenValue();
  }
  public double getVolumeValue(int series, int item) {
    return Double.NaN;
  }
  public double getXValue(int series, int item) {
    OHLCSeries s = (OHLCSeries)this.data.get(series);
    OHLCItem di = (OHLCItem)s.getDataItem(item);
    RegularTimePeriod period = di.getPeriod();
    return getX(period);
  }
  public int getItemCount(int series) {
    return getSeries(series).getItemCount();
  }
  public int getSeriesCount() {
    return this.data.size();
  }
  public int hashCode() {
    int result = 137;
    result = HashUtilities.hashCode(result, this.xPosition);
    for(int i = 0; i < this.data.size(); i++) {
      result = HashUtilities.hashCode(result, this.data.get(i));
    }
    return result;
  }
  protected synchronized long getX(RegularTimePeriod period) {
    long result = 0L;
    if(this.xPosition == TimePeriodAnchor.START) {
      result = period.getFirstMillisecond();
    }
    else 
      if(this.xPosition == TimePeriodAnchor.MIDDLE) {
        result = period.getMiddleMillisecond();
      }
      else 
        if(this.xPosition == TimePeriodAnchor.END) {
          result = period.getLastMillisecond();
        }
    return result;
  }
  public void addSeries(OHLCSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    this.data.add(series);
    series.addChangeListener(this);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeAllSeries() {
    if(this.data.size() == 0) {
      return ;
    }
    for(int i = 0; i < this.data.size(); i++) {
      List var_4354 = this.data;
      OHLCSeries series = (OHLCSeries)var_4354.get(i);
      series.removeChangeListener(this);
    }
    this.data.clear();
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeSeries(int index) {
    OHLCSeries series = getSeries(index);
    if(series != null) {
      removeSeries(series);
    }
  }
  public void setXPosition(TimePeriodAnchor anchor) {
    if(anchor == null) {
      throw new IllegalArgumentException("Null \'anchor\' argument.");
    }
    this.xPosition = anchor;
    fireDatasetChanged(new DatasetChangeInfo());
  }
}