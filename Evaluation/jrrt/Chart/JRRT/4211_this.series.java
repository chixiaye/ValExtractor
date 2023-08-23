package org.jfree.data.time;
import javax.swing.table.AbstractTableModel;
import org.jfree.data.event.SeriesChangeEvent;
import org.jfree.data.event.SeriesChangeListener;

public class TimeSeriesTableModel extends AbstractTableModel implements SeriesChangeListener  {
  private TimeSeries series;
  private boolean editable;
  private RegularTimePeriod newTimePeriod;
  private Number newValue;
  public TimeSeriesTableModel() {
    this(new TimeSeries("Untitled"));
  }
  public TimeSeriesTableModel(TimeSeries series) {
    this(series, false);
  }
  public TimeSeriesTableModel(TimeSeries series, boolean editable) {
    super();
    this.series = series;
    this.series.addChangeListener(this);
    this.editable = editable;
  }
  public Class getColumnClass(int column) {
    if(column == 0) {
      return String.class;
    }
    else {
      if(column == 1) {
        return Double.class;
      }
      else {
        return null;
      }
    }
  }
  public Object getValueAt(int row, int column) {
    if(row < this.series.getItemCount()) {
      if(column == 0) {
        return this.series.getTimePeriod(row);
      }
      else {
        if(column == 1) {
          return this.series.getValue(row);
        }
        else {
          return null;
        }
      }
    }
    else {
      if(column == 0) {
        return this.newTimePeriod;
      }
      else {
        if(column == 1) {
          return this.newValue;
        }
        else {
          return null;
        }
      }
    }
  }
  public String getColumnName(int column) {
    if(column == 0) {
      return "Period:";
    }
    else {
      if(column == 1) {
        return "Value:";
      }
      else {
        return null;
      }
    }
  }
  public boolean isCellEditable(int row, int column) {
    if(this.editable) {
      if((column == 0) || (column == 1)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }
  public int getColumnCount() {
    return 2;
  }
  public int getRowCount() {
    return this.series.getItemCount();
  }
  public void seriesChanged(SeriesChangeEvent event) {
    fireTableDataChanged();
  }
  public void setValueAt(Object value, int row, int column) {
    TimeSeries var_4211 = this.series;
    if(row < var_4211.getItemCount()) {
      if(column == 1) {
        try {
          Double v = Double.valueOf(value.toString());
          this.series.update(row, v);
        }
        catch (NumberFormatException nfe) {
          System.err.println("Number format exception");
        }
      }
    }
    else {
      if(column == 0) {
        this.newTimePeriod = null;
      }
      else 
        if(column == 1) {
          this.newValue = Double.valueOf(value.toString());
        }
    }
  }
}