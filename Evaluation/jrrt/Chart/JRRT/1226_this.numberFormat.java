package org.jfree.chart.labels;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DataUtilities;
import org.jfree.data.category.CategoryDataset;

abstract public class AbstractCategoryItemLabelGenerator implements PublicCloneable, Cloneable, Serializable  {
  final private static long serialVersionUID = -7108591260223293197L;
  private String labelFormat;
  private String nullValueString;
  private NumberFormat numberFormat;
  private DateFormat dateFormat;
  private NumberFormat percentFormat;
  protected AbstractCategoryItemLabelGenerator(String labelFormat, DateFormat formatter) {
    super();
    if(labelFormat == null) {
      throw new IllegalArgumentException("Null \'labelFormat\' argument.");
    }
    if(formatter == null) {
      throw new IllegalArgumentException("Null \'formatter\' argument.");
    }
    this.labelFormat = labelFormat;
    this.numberFormat = null;
    this.percentFormat = NumberFormat.getPercentInstance();
    this.dateFormat = formatter;
    this.nullValueString = "-";
  }
  protected AbstractCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter) {
    this(labelFormat, formatter, NumberFormat.getPercentInstance());
  }
  protected AbstractCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter, NumberFormat percentFormatter) {
    super();
    if(labelFormat == null) {
      throw new IllegalArgumentException("Null \'labelFormat\' argument.");
    }
    if(formatter == null) {
      throw new IllegalArgumentException("Null \'formatter\' argument.");
    }
    if(percentFormatter == null) {
      throw new IllegalArgumentException("Null \'percentFormatter\' argument.");
    }
    this.labelFormat = labelFormat;
    this.numberFormat = formatter;
    this.percentFormat = percentFormatter;
    this.dateFormat = null;
    this.nullValueString = "-";
  }
  public DateFormat getDateFormat() {
    return this.dateFormat;
  }
  public NumberFormat getNumberFormat() {
    return this.numberFormat;
  }
  public Object clone() throws CloneNotSupportedException {
    AbstractCategoryItemLabelGenerator clone = (AbstractCategoryItemLabelGenerator)super.clone();
    if(this.numberFormat != null) {
      clone.numberFormat = (NumberFormat)this.numberFormat.clone();
    }
    if(this.dateFormat != null) {
      clone.dateFormat = (DateFormat)this.dateFormat.clone();
    }
    return clone;
  }
  protected Object[] createItemArray(CategoryDataset dataset, int row, int column) {
    Object[] result = new Object[4];
    result[0] = dataset.getRowKey(row).toString();
    result[1] = dataset.getColumnKey(column).toString();
    Number value = dataset.getValue(row, column);
    if(value != null) {
      NumberFormat var_1226 = this.numberFormat;
      if(var_1226 != null) {
        result[2] = this.numberFormat.format(value);
      }
      else 
        if(this.dateFormat != null) {
          result[2] = this.dateFormat.format(value);
        }
    }
    else {
      result[2] = this.nullValueString;
    }
    if(value != null) {
      double total = DataUtilities.calculateColumnTotal(dataset, column);
      double percent = value.doubleValue() / total;
      result[3] = this.percentFormat.format(percent);
    }
    return result;
  }
  public String generateColumnLabel(CategoryDataset dataset, int column) {
    return dataset.getColumnKey(column).toString();
  }
  protected String generateLabelString(CategoryDataset dataset, int row, int column) {
    if(dataset == null) {
      throw new IllegalArgumentException("Null \'dataset\' argument.");
    }
    String result = null;
    Object[] items = createItemArray(dataset, row, column);
    result = MessageFormat.format(this.labelFormat, items);
    return result;
  }
  public String generateRowLabel(CategoryDataset dataset, int row) {
    return dataset.getRowKey(row).toString();
  }
  public String getLabelFormat() {
    return this.labelFormat;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof AbstractCategoryItemLabelGenerator)) {
      return false;
    }
    AbstractCategoryItemLabelGenerator that = (AbstractCategoryItemLabelGenerator)obj;
    if(!this.labelFormat.equals(that.labelFormat)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.dateFormat, that.dateFormat)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.numberFormat, that.numberFormat)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    int result = 127;
    result = HashUtilities.hashCode(result, this.labelFormat);
    result = HashUtilities.hashCode(result, this.nullValueString);
    result = HashUtilities.hashCode(result, this.dateFormat);
    result = HashUtilities.hashCode(result, this.numberFormat);
    result = HashUtilities.hashCode(result, this.percentFormat);
    return result;
  }
}