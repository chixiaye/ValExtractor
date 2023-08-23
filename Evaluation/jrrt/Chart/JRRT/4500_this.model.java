package org.jfree.data.xy;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.event.DatasetChangeListener;

public class XYDatasetTableModel extends AbstractTableModel implements TableModel, DatasetChangeListener  {
  TableXYDataset model = null;
  public XYDatasetTableModel() {
    super();
  }
  public XYDatasetTableModel(TableXYDataset dataset) {
    this();
    this.model = dataset;
    this.model.addChangeListener(this);
  }
  public Object getValueAt(int row, int column) {
    if(this.model == null) {
      return null;
    }
    if(column < 1) {
      return this.model.getX(0, row);
    }
    else {
      return this.model.getY(column - 1, row);
    }
  }
  public String getColumnName(int column) {
    TableXYDataset var_4500 = this.model;
    if(var_4500 == null) {
      return super.getColumnName(column);
    }
    if(column < 1) {
      return "X Value";
    }
    else {
      return this.model.getSeriesKey(column - 1).toString();
    }
  }
  public boolean isCellEditable(int row, int column) {
    return false;
  }
  public int getColumnCount() {
    if(this.model == null) {
      return 0;
    }
    return this.model.getSeriesCount() + 1;
  }
  public int getRowCount() {
    if(this.model == null) {
      return 0;
    }
    return this.model.getItemCount();
  }
  public void datasetChanged(DatasetChangeEvent event) {
    fireTableDataChanged();
  }
  public void setModel(TableXYDataset dataset) {
    this.model = dataset;
    this.model.addChangeListener(this);
    fireTableDataChanged();
  }
  public void setValueAt(Object value, int row, int column) {
    if(isCellEditable(row, column)) {
    }
  }
}