package org.jfree.data.category;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.event.DatasetChangeEvent;

public class SlidingCategoryDataset extends AbstractDataset implements CategoryDataset  {
  private CategoryDataset underlying;
  private int firstCategoryIndex;
  private int maximumCategoryCount;
  public SlidingCategoryDataset(CategoryDataset underlying, int firstColumn, int maxColumns) {
    super();
    this.underlying = underlying;
    this.firstCategoryIndex = firstColumn;
    this.maximumCategoryCount = maxColumns;
  }
  public CategoryDataset getUnderlyingDataset() {
    return this.underlying;
  }
  public Comparable getColumnKey(int column) {
    return this.underlying.getColumnKey(column + this.firstCategoryIndex);
  }
  public Comparable getRowKey(int row) {
    return this.underlying.getRowKey(row);
  }
  public List getColumnKeys() {
    List result = new java.util.ArrayList();
    int last = lastCategoryIndex();
    for(int i = this.firstCategoryIndex; i <= last; i++) {
      result.add(this.underlying.getColumnKey(i));
    }
    return Collections.unmodifiableList(result);
  }
  public List getRowKeys() {
    return this.underlying.getRowKeys();
  }
  public Number getValue(int row, int column) {
    return this.underlying.getValue(row, column + this.firstCategoryIndex);
  }
  public Number getValue(Comparable rowKey, Comparable columnKey) {
    int r = getRowIndex(rowKey);
    int c = getColumnIndex(columnKey);
    if(c != -1) {
      return this.underlying.getValue(r, c + this.firstCategoryIndex);
    }
    else {
      throw new UnknownKeyException("Unknown columnKey: " + columnKey);
    }
  }
  public Object clone() throws CloneNotSupportedException {
    SlidingCategoryDataset clone = (SlidingCategoryDataset)super.clone();
    if(this.underlying instanceof PublicCloneable) {
      PublicCloneable pc = (PublicCloneable)this.underlying;
      clone.underlying = (CategoryDataset)pc.clone();
    }
    return clone;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof SlidingCategoryDataset)) {
      return false;
    }
    SlidingCategoryDataset that = (SlidingCategoryDataset)obj;
    if(this.firstCategoryIndex != that.firstCategoryIndex) {
      return false;
    }
    if(this.maximumCategoryCount != that.maximumCategoryCount) {
      return false;
    }
    if(!this.underlying.equals(that.underlying)) {
      return false;
    }
    return true;
  }
  public int getColumnCount() {
    int last = lastCategoryIndex();
    if(last == -1) {
      return 0;
    }
    else {
      return Math.max(last - this.firstCategoryIndex + 1, 0);
    }
  }
  public int getColumnIndex(Comparable key) {
    int index = this.underlying.getColumnIndex(key);
    if(index >= this.firstCategoryIndex && index <= lastCategoryIndex()) {
      return index - this.firstCategoryIndex;
    }
    return -1;
  }
  public int getFirstCategoryIndex() {
    return this.firstCategoryIndex;
  }
  public int getMaximumCategoryCount() {
    return this.maximumCategoryCount;
  }
  public int getRowCount() {
    return this.underlying.getRowCount();
  }
  public int getRowIndex(Comparable key) {
    return this.underlying.getRowIndex(key);
  }
  private int lastCategoryIndex() {
    int var_3854 = this.maximumCategoryCount;
    if(var_3854 == 0) {
      return -1;
    }
    return Math.min(this.firstCategoryIndex + this.maximumCategoryCount, this.underlying.getColumnCount()) - 1;
  }
  public void setFirstCategoryIndex(int first) {
    if(first < 0 || first >= this.underlying.getColumnCount()) {
      throw new IllegalArgumentException("Invalid index.");
    }
    this.firstCategoryIndex = first;
    fireDatasetChanged(new DatasetChangeInfo());
  }
  public void setMaximumCategoryCount(int max) {
    if(max < 0) {
      throw new IllegalArgumentException("Requires \'max\' >= 0.");
    }
    this.maximumCategoryCount = max;
    fireDatasetChanged(new DatasetChangeInfo());
  }
}