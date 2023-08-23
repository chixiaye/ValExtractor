package org.jfree.data.general;
import java.util.Set;
import java.util.TreeSet;
import org.jfree.data.DefaultKeyedValues2D;

public class WaferMapDataset extends AbstractDataset  {
  private DefaultKeyedValues2D data;
  private int maxChipX;
  private int maxChipY;
  private double chipSpace;
  private Double maxValue;
  private Double minValue;
  final private static double DEFAULT_CHIP_SPACE = 1D;
  public WaferMapDataset(int maxChipX, int maxChipY) {
    this(maxChipX, maxChipY, null);
  }
  public WaferMapDataset(int maxChipX, int maxChipY, Number chipSpace) {
    super();
    this.maxValue = new Double(Double.NEGATIVE_INFINITY);
    this.minValue = new Double(Double.POSITIVE_INFINITY);
    this.data = new DefaultKeyedValues2D();
    this.maxChipX = maxChipX;
    this.maxChipY = maxChipY;
    if(chipSpace == null) {
      this.chipSpace = DEFAULT_CHIP_SPACE;
    }
    else {
      this.chipSpace = chipSpace.doubleValue();
    }
  }
  public Number getChipValue(int chipx, int chipy) {
    return getChipValue(new Integer(chipx), new Integer(chipy));
  }
  public Number getChipValue(Comparable chipx, Comparable chipy) {
    DefaultKeyedValues2D var_3969 = this.data;
    int rowIndex = var_3969.getRowIndex(chipx);
    if(rowIndex < 0) {
      return null;
    }
    int colIndex = this.data.getColumnIndex(chipy);
    if(colIndex < 0) {
      return null;
    }
    return this.data.getValue(rowIndex, colIndex);
  }
  public Number getMaxValue() {
    return this.maxValue;
  }
  public Number getMinValue() {
    return this.minValue;
  }
  public Set getUniqueValues() {
    Set unique = new TreeSet();
    for(int r = 0; r < this.data.getRowCount(); r++) {
      for(int c = 0; c < this.data.getColumnCount(); c++) {
        Number value = this.data.getValue(r, c);
        if(value != null) {
          unique.add(value);
        }
      }
    }
    return unique;
  }
  public boolean isMaxValue(Number check) {
    if(check.doubleValue() > this.maxValue.doubleValue()) {
      return true;
    }
    return false;
  }
  public boolean isMinValue(Number check) {
    if(check.doubleValue() < this.minValue.doubleValue()) {
      return true;
    }
    return false;
  }
  public double getChipSpace() {
    return this.chipSpace;
  }
  public int getMaxChipX() {
    return this.maxChipX;
  }
  public int getMaxChipY() {
    return this.maxChipY;
  }
  public int getUniqueValueCount() {
    return getUniqueValues().size();
  }
  public void addValue(int v, int x, int y) {
    setValue(new Double(v), new Integer(x), new Integer(y));
  }
  public void addValue(Number value, Comparable chipx, Comparable chipy) {
    setValue(value, chipx, chipy);
  }
  public void setChipSpace(double space) {
    this.chipSpace = space;
  }
  public void setMaxChipX(int maxChipX) {
    this.maxChipX = maxChipX;
  }
  public void setMaxChipY(int maxChipY) {
    this.maxChipY = maxChipY;
  }
  public void setValue(Number value, Comparable chipx, Comparable chipy) {
    this.data.setValue(value, chipx, chipy);
    if(isMaxValue(value)) {
      this.maxValue = (Double)value;
    }
    if(isMinValue(value)) {
      this.minValue = (Double)value;
    }
  }
}