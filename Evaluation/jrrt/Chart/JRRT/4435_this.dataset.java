package org.jfree.data.xy;
import java.io.Serializable;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.event.DatasetChangeListener;
import org.jfree.data.general.DatasetUtilities;

public class IntervalXYDelegate implements DatasetChangeListener, DomainInfo, Serializable, Cloneable, PublicCloneable  {
  final private static long serialVersionUID = -685166711639592857L;
  private XYDataset dataset;
  private boolean autoWidth;
  private double intervalPositionFactor;
  private double fixedIntervalWidth;
  private double autoIntervalWidth;
  public IntervalXYDelegate(XYDataset dataset) {
    this(dataset, true);
  }
  public IntervalXYDelegate(XYDataset dataset, boolean autoWidth) {
    super();
    if(dataset == null) {
      throw new IllegalArgumentException("Null \'dataset\' argument.");
    }
    this.dataset = dataset;
    this.autoWidth = autoWidth;
    this.intervalPositionFactor = 0.5D;
    this.autoIntervalWidth = Double.POSITIVE_INFINITY;
    this.fixedIntervalWidth = 1.0D;
  }
  public Number getEndX(int series, int item) {
    Number endX = null;
    Number x = this.dataset.getX(series, item);
    if(x != null) {
      endX = new Double(x.doubleValue() + ((1.0D - getIntervalPositionFactor()) * getIntervalWidth()));
    }
    return endX;
  }
  public Number getStartX(int series, int item) {
    Number startX = null;
    Number x = this.dataset.getX(series, item);
    if(x != null) {
      startX = new Double(x.doubleValue() - (getIntervalPositionFactor() * getIntervalWidth()));
    }
    return startX;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Range getDomainBounds(boolean includeInterval) {
    Range range = DatasetUtilities.findDomainBounds(this.dataset, false);
    if(includeInterval && range != null) {
      double lowerAdj = getIntervalWidth() * getIntervalPositionFactor();
      double upperAdj = getIntervalWidth() - lowerAdj;
      range = new Range(range.getLowerBound() - lowerAdj, range.getUpperBound() + upperAdj);
    }
    return range;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof IntervalXYDelegate)) {
      return false;
    }
    IntervalXYDelegate that = (IntervalXYDelegate)obj;
    if(this.autoWidth != that.autoWidth) {
      return false;
    }
    if(this.intervalPositionFactor != that.intervalPositionFactor) {
      return false;
    }
    if(this.fixedIntervalWidth != that.fixedIntervalWidth) {
      return false;
    }
    return true;
  }
  public boolean isAutoWidth() {
    return this.autoWidth;
  }
  private double calculateIntervalForSeries(int series) {
    double result = Double.POSITIVE_INFINITY;
    int itemCount = this.dataset.getItemCount(series);
    if(itemCount > 1) {
      XYDataset var_4435 = this.dataset;
      double prev = var_4435.getXValue(series, 0);
      for(int item = 1; item < itemCount; item++) {
        double x = this.dataset.getXValue(series, item);
        result = Math.min(result, x - prev);
        prev = x;
      }
    }
    return result;
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
  public double getEndXValue(int series, int item) {
    return this.dataset.getXValue(series, item) + (1.0D - getIntervalPositionFactor()) * getIntervalWidth();
  }
  public double getFixedIntervalWidth() {
    return this.fixedIntervalWidth;
  }
  public double getIntervalPositionFactor() {
    return this.intervalPositionFactor;
  }
  public double getIntervalWidth() {
    if(isAutoWidth() && !Double.isInfinite(this.autoIntervalWidth)) {
      return this.autoIntervalWidth;
    }
    else {
      return this.fixedIntervalWidth;
    }
  }
  public double getStartXValue(int series, int item) {
    return this.dataset.getXValue(series, item) - getIntervalPositionFactor() * getIntervalWidth();
  }
  private double recalculateInterval() {
    double result = Double.POSITIVE_INFINITY;
    int seriesCount = this.dataset.getSeriesCount();
    for(int series = 0; series < seriesCount; series++) {
      result = Math.min(result, calculateIntervalForSeries(series));
    }
    return result;
  }
  public int hashCode() {
    int hash = 5;
    hash = HashUtilities.hashCode(hash, this.autoWidth);
    hash = HashUtilities.hashCode(hash, this.intervalPositionFactor);
    hash = HashUtilities.hashCode(hash, this.fixedIntervalWidth);
    return hash;
  }
  public void datasetChanged(DatasetChangeEvent e) {
    if(this.autoWidth) {
      this.autoIntervalWidth = recalculateInterval();
    }
  }
  public void setAutoWidth(boolean b) {
    this.autoWidth = b;
    if(b) {
      this.autoIntervalWidth = recalculateInterval();
    }
  }
  public void setFixedIntervalWidth(double w) {
    if(w < 0.0D) {
      throw new IllegalArgumentException("Negative \'w\' argument.");
    }
    this.fixedIntervalWidth = w;
    this.autoWidth = false;
  }
  public void setIntervalPositionFactor(double d) {
    if(d < 0.0D || 1.0D < d) {
      throw new IllegalArgumentException("Argument \'d\' outside valid range.");
    }
    this.intervalPositionFactor = d;
  }
}