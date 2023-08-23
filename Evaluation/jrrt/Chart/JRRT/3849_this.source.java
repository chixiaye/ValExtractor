package org.jfree.data.category;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.pie.AbstractPieDataset;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.event.DatasetChangeListener;
import org.jfree.data.pie.PieDataset;

public class CategoryToPieDataset extends AbstractPieDataset implements PieDataset, DatasetChangeListener  {
  final static long serialVersionUID = 5516396319762189617L;
  private CategoryDataset source;
  private TableOrder extract;
  private int index;
  public CategoryToPieDataset(CategoryDataset source, TableOrder extract, int index) {
    super();
    if(extract == null) {
      throw new IllegalArgumentException("Null \'extract\' argument.");
    }
    this.source = source;
    if(this.source != null) {
      this.source.addChangeListener(this);
    }
    this.extract = extract;
    this.index = index;
  }
  public CategoryDataset getUnderlyingDataset() {
    return this.source;
  }
  public Comparable getKey(int index) {
    Comparable result = null;
    if(index < 0 || index >= getItemCount()) {
      throw new IndexOutOfBoundsException("Invalid \'index\': " + index);
    }
    if(this.extract == TableOrder.BY_ROW) {
      result = this.source.getColumnKey(index);
    }
    else 
      if(this.extract == TableOrder.BY_COLUMN) {
        result = this.source.getRowKey(index);
      }
    return result;
  }
  public List getKeys() {
    List result = Collections.EMPTY_LIST;
    CategoryDataset var_3849 = this.source;
    if(var_3849 != null) {
      if(this.extract == TableOrder.BY_ROW) {
        result = this.source.getColumnKeys();
      }
      else 
        if(this.extract == TableOrder.BY_COLUMN) {
          result = this.source.getRowKeys();
        }
    }
    return result;
  }
  public Number getValue(int item) {
    Number result = null;
    if(item < 0 || item >= getItemCount()) {
      throw new IndexOutOfBoundsException("The \'item\' index is out of bounds.");
    }
    if(this.extract == TableOrder.BY_ROW) {
      result = this.source.getValue(this.index, item);
    }
    else 
      if(this.extract == TableOrder.BY_COLUMN) {
        result = this.source.getValue(item, this.index);
      }
    return result;
  }
  public Number getValue(Comparable key) {
    Number result = null;
    int keyIndex = getIndex(key);
    if(keyIndex != -1) {
      if(this.extract == TableOrder.BY_ROW) {
        result = this.source.getValue(this.index, keyIndex);
      }
      else 
        if(this.extract == TableOrder.BY_COLUMN) {
          result = this.source.getValue(keyIndex, this.index);
        }
    }
    return result;
  }
  public TableOrder getExtractType() {
    return this.extract;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof PieDataset)) {
      return false;
    }
    PieDataset that = (PieDataset)obj;
    int count = getItemCount();
    if(that.getItemCount() != count) {
      return false;
    }
    for(int i = 0; i < count; i++) {
      Comparable k1 = getKey(i);
      Comparable k2 = that.getKey(i);
      if(!k1.equals(k2)) {
        return false;
      }
      Number v1 = getValue(i);
      Number v2 = that.getValue(i);
      if(v1 == null) {
        if(v2 != null) {
          return false;
        }
      }
      else {
        if(!v1.equals(v2)) {
          return false;
        }
      }
    }
    return true;
  }
  public int getExtractIndex() {
    return this.index;
  }
  public int getIndex(Comparable key) {
    int result = -1;
    if(this.source != null) {
      if(this.extract == TableOrder.BY_ROW) {
        result = this.source.getColumnIndex(key);
      }
      else 
        if(this.extract == TableOrder.BY_COLUMN) {
          result = this.source.getRowIndex(key);
        }
    }
    return result;
  }
  public int getItemCount() {
    int result = 0;
    if(this.source != null) {
      if(this.extract == TableOrder.BY_ROW) {
        result = this.source.getColumnCount();
      }
      else 
        if(this.extract == TableOrder.BY_COLUMN) {
          result = this.source.getRowCount();
        }
    }
    return result;
  }
  public void datasetChanged(DatasetChangeEvent event) {
    fireDatasetChanged(new DatasetChangeInfo());
  }
}