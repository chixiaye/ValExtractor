package org.jfree.data.xy;
import java.io.Serializable;
import java.util.List;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;

public class MatrixSeriesCollection extends AbstractXYZDataset implements XYZDataset, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -3197705779242543945L;
  private List seriesList;
  public MatrixSeriesCollection() {
    this(null);
  }
  public MatrixSeriesCollection(MatrixSeries series) {
    super();
    this.seriesList = new java.util.ArrayList();
    if(series != null) {
      this.seriesList.add(series);
      series.addChangeListener(this);
    }
  }
  public Comparable getSeriesKey(int seriesIndex) {
    return getSeries(seriesIndex).getKey();
  }
  public MatrixSeries getSeries(int seriesIndex) {
    if((seriesIndex < 0) || (seriesIndex > getSeriesCount())) {
      throw new IllegalArgumentException("Index outside valid range.");
    }
    MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
    return series;
  }
  public Number getX(int seriesIndex, int itemIndex) {
    MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
    int x = series.getItemColumn(itemIndex);
    return new Integer(x);
  }
  public Number getY(int seriesIndex, int itemIndex) {
    MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
    int y = series.getItemRow(itemIndex);
    return new Integer(y);
  }
  public Number getZ(int seriesIndex, int itemIndex) {
    MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
    Number z = series.getItem(itemIndex);
    return z;
  }
  public Object clone() throws CloneNotSupportedException {
    MatrixSeriesCollection clone = (MatrixSeriesCollection)super.clone();
    clone.seriesList = (List)ObjectUtilities.deepClone(this.seriesList);
    return clone;
  }
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }
    if(obj == this) {
      return true;
    }
    if(obj instanceof MatrixSeriesCollection) {
      MatrixSeriesCollection c = (MatrixSeriesCollection)obj;
      return ObjectUtilities.equal(this.seriesList, c.seriesList);
    }
    return false;
  }
  public int getItemCount(int seriesIndex) {
    return getSeries(seriesIndex).getItemCount();
  }
  public int getSeriesCount() {
    return this.seriesList.size();
  }
  public int hashCode() {
    List var_4443 = this.seriesList;
    return (var_4443 != null ? this.seriesList.hashCode() : 0);
  }
  public void addSeries(MatrixSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Cannot add null series.");
    }
    this.seriesList.add(series);
    series.addChangeListener(this);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeAllSeries() {
    for(int i = 0; i < this.seriesList.size(); i++) {
      MatrixSeries series = (MatrixSeries)this.seriesList.get(i);
      series.removeChangeListener(this);
    }
    this.seriesList.clear();
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeSeries(int seriesIndex) {
    if((seriesIndex < 0) || (seriesIndex > getSeriesCount())) {
      throw new IllegalArgumentException("Index outside valid range.");
    }
    MatrixSeries series = (MatrixSeries)this.seriesList.get(seriesIndex);
    series.removeChangeListener(this);
    this.seriesList.remove(seriesIndex);
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void removeSeries(MatrixSeries series) {
    if(series == null) {
      throw new IllegalArgumentException("Cannot remove null series.");
    }
    if(this.seriesList.contains(series)) {
      series.removeChangeListener(this);
      this.seriesList.remove(series);
      fireDatasetChanged(new DatasetChangeInfo());
    }
  }
}