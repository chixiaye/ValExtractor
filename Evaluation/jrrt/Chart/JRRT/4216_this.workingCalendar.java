package org.jfree.data.time;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.data.DomainInfo;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.SelectableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYDatasetSelectionState;
import org.jfree.data.xy.XYDomainInfo;
import org.jfree.data.xy.XYRangeInfo;

public class TimeSeriesCollection extends AbstractIntervalXYDataset implements XYDataset, IntervalXYDataset, DomainInfo, XYDomainInfo, XYRangeInfo, XYDatasetSelectionState, SelectableXYDataset, Serializable  {
  final private static long serialVersionUID = 834149929022371137L;
  private List data;
  private Calendar workingCalendar;
  private TimePeriodAnchor xPosition;
  public TimeSeriesCollection() {
    this(null, TimeZone.getDefault());
  }
  public TimeSeriesCollection(TimeSeries series) {
    this(series, TimeZone.getDefault());
  }
  public TimeSeriesCollection(TimeSeries series, TimeZone zone) {
    super();
    if(zone == null) {
      zone = TimeZone.getDefault();
    }
    this.workingCalendar = Calendar.getInstance(zone);
    this.data = new ArrayList();
    if(series != null) {
      this.data.add(series);
      series.addChangeListener(this);
    }
    this.xPosition = TimePeriodAnchor.START;
    setSelectionState(this);
  }
  public TimeSeriesCollection(TimeZone zone) {
    this(null, zone);
  }
  public Comparable getSeriesKey(int series) {
    return getSeries(series).getKey();
  }
  public DomainOrder getDomainOrder() {
    return DomainOrder.ASCENDING;
  }
  public List getSeries() {
    return Collections.unmodifiableList(this.data);
  }
  public synchronized Number getEndX(int series, int item) {
    TimeSeries ts = (TimeSeries)this.data.get(series);
    return new Long(ts.getTimePeriod(item).getLastMillisecond(this.workingCalendar));
  }
  public Number getEndY(int series, int item) {
    return getY(series, item);
  }
  public synchronized Number getStartX(int series, int item) {
    TimeSeries ts = (TimeSeries)this.data.get(series);
    return new Long(ts.getTimePeriod(item).getFirstMillisecond(this.workingCalendar));
  }
  public Number getStartY(int series, int item) {
    return getY(series, item);
  }
  public Number getX(int series, int item) {
    TimeSeries ts = (TimeSeries)this.data.get(series);
    RegularTimePeriod period = ts.getTimePeriod(item);
    return new Long(getX(period));
  }
  public Number getY(int series, int item) {
    TimeSeries ts = (TimeSeries)this.data.get(series);
    return ts.getValue(item);
  }
  public Object clone() throws CloneNotSupportedException {
    TimeSeriesCollection clone = (TimeSeriesCollection)super.clone();
    clone.data = (List)ObjectUtilities.deepClone(this.data);
    clone.workingCalendar = (Calendar)this.workingCalendar.clone();
    return clone;
  }
  public Range getDomainBounds(boolean includeInterval) {
    Range result = null;
    Iterator iterator = this.data.iterator();
    while(iterator.hasNext()){
      TimeSeries series = (TimeSeries)iterator.next();
      int count = series.getItemCount();
      if(count > 0) {
        RegularTimePeriod start = series.getTimePeriod(0);
        RegularTimePeriod end = series.getTimePeriod(count - 1);
        Range temp;
        if(!includeInterval) {
          temp = new Range(getX(start), getX(end));
        }
        else {
          Calendar var_4216 = this.workingCalendar;
          temp = new Range(start.getFirstMillisecond(var_4216), end.getLastMillisecond(this.workingCalendar));
        }
        result = Range.combine(result, temp);
      }
    }
    return result;
  }
  public Range getDomainBounds(List visibleSeriesKeys, boolean includeInterval) {
    Range result = null;
    Iterator iterator = visibleSeriesKeys.iterator();
    while(iterator.hasNext()){
      Comparable seriesKey = (Comparable)iterator.next();
      TimeSeries series = getSeries(seriesKey);
      int count = series.getItemCount();
      if(count > 0) {
        RegularTimePeriod start = series.getTimePeriod(0);
        RegularTimePeriod end = series.getTimePeriod(count - 1);
        Range temp;
        if(!includeInterval) {
          temp = new Range(getX(start), getX(end));
        }
        else {
          temp = new Range(start.getFirstMillisecond(this.workingCalendar), end.getLastMillisecond(this.workingCalendar));
        }
        result = Range.combine(result, temp);
      }
    }
    return result;
  }
  public Range getRangeBounds(List visibleSeriesKeys, Range xRange, boolean includeInterval) {
    Range result = null;
    Iterator iterator = visibleSeriesKeys.iterator();
    while(iterator.hasNext()){
      Comparable seriesKey = (Comparable)iterator.next();
      TimeSeries series = getSeries(seriesKey);
      Range r = null;
      r = new Range(series.getMinY(), series.getMaxY());
      result = Range.combine(result, r);
    }
    return result;
  }
  public TimePeriodAnchor getXPosition() {
    return this.xPosition;
  }
  public TimeSeries getSeries(int series) {
    if((series < 0) || (series >= getSeriesCount())) {
      throw new IllegalArgumentException("The \'series\' argument is out of bounds (" + series + ").");
    }
    return (TimeSeries)this.data.get(series);
  }
  public TimeSeries getSeries(Comparable key) {
    TimeSeries result = null;
    Iterator iterator = this.data.iterator();
    while(iterator.hasNext()){
      TimeSeries series = (TimeSeries)iterator.next();
      Comparable k = series.getKey();
      if(k != null && k.equals(key)) {
        result = series;
      }
    }
    return result;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof TimeSeriesCollection)) {
      return false;
    }
    TimeSeriesCollection that = (TimeSeriesCollection)obj;
    if(this.xPosition != that.xPosition) {
      return false;
    }
    if(!ObjectUtilities.equal(this.data, that.data)) {
      return false;
    }
    return true;
  }
  public boolean isSelected(int series, int item) {
    TimeSeries s = getSeries(series);
    TimeSeriesDataItem i = s.getRawDataItem(item);
    return i.isSelected();
  }
  public double getDomainLowerBound(boolean includeInterval) {
    double result = Double.NaN;
    Range r = getDomainBounds(includeInterval);
    if(r != null) {
      result = r.getLowerBound();
    }
    return result;
  }
  public double getDomainUpperBound(boolean includeInterval) {
    double result = Double.NaN;
    Range r = getDomainBounds(includeInterval);
    if(r != null) {
      result = r.getUpperBound();
    }
    return result;
  }
  public double getXValue(int series, int item) {
    TimeSeries s = (TimeSeries)this.data.get(series);
    RegularTimePeriod period = s.getTimePeriod(item);
    return getX(period);
  }
  public int getItemCount(int series) {
    return getSeries(series).getItemCount();
  }
  public int getSeriesCount() {
    return this.data.size();
  }
  public int hashCode() {
    int result;
    result = this.data.hashCode();
    result = 29 * result + (this.workingCalendar != null ? this.workingCalendar.hashCode() : 0);
    result = 29 * result + (this.xPosition != null ? this.xPosition.hashCode() : 0);
    return result;
  }
  public int indexOf(TimeSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    return this.data.indexOf(series);
  }
  public int[] getSurroundingItems(int series, long milliseconds) {
    int[] result = new int[]{ -1, -1 } ;
    TimeSeries timeSeries = getSeries(series);
    for(int i = 0; i < timeSeries.getItemCount(); i++) {
      Number x = getX(series, i);
      long m = x.longValue();
      if(m <= milliseconds) {
        result[0] = i;
      }
      if(m >= milliseconds) {
        result[1] = i;
        break ;
      }
    }
    return result;
  }
  protected synchronized long getX(RegularTimePeriod period) {
    long result = 0L;
    if(this.xPosition == TimePeriodAnchor.START) {
      result = period.getFirstMillisecond(this.workingCalendar);
    }
    else 
      if(this.xPosition == TimePeriodAnchor.MIDDLE) {
        result = period.getMiddleMillisecond(this.workingCalendar);
      }
      else 
        if(this.xPosition == TimePeriodAnchor.END) {
          result = period.getLastMillisecond(this.workingCalendar);
        }
    return result;
  }
  public void addSeries(TimeSeries series) {
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
      TimeSeries series = (TimeSeries)this.data.get(i);
      series.removeChangeListener(this);
    }
    this.data.clear();
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeSeries(int index) {
    TimeSeries series = getSeries(index);
    if(series != null) {
      removeSeries(series);
    }
  }
  public void removeSeries(TimeSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Null \'series\' argument.");
    }
    this.data.remove(series);
    series.removeChangeListener(this);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void setSelected(int series, int item, boolean selected) {
    setSelected(series, item, selected, true);
  }
  public void setSelected(int series, int item, boolean selected, boolean notify) {
    TimeSeries s = getSeries(series);
    TimeSeriesDataItem i = s.getRawDataItem(item);
    i.setSelected(selected);
    if(notify) {
      fireSelectionEvent();
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