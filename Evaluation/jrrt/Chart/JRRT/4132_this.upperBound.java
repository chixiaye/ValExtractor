package org.jfree.data.statistics;
import java.io.Serializable;
import org.jfree.chart.util.PublicCloneable;

public class SimpleHistogramBin implements Comparable, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 3480862537505941742L;
  private double lowerBound;
  private double upperBound;
  private boolean includeLowerBound;
  private boolean includeUpperBound;
  private int itemCount;
  private boolean selected;
  public SimpleHistogramBin(double lowerBound, double upperBound) {
    this(lowerBound, upperBound, true, true);
  }
  public SimpleHistogramBin(double lowerBound, double upperBound, boolean includeLowerBound, boolean includeUpperBound) {
    super();
    if(lowerBound >= upperBound) {
      throw new IllegalArgumentException("Invalid bounds; " + lowerBound + " to " + upperBound);
    }
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.includeLowerBound = includeLowerBound;
    this.includeUpperBound = includeUpperBound;
    this.itemCount = 0;
    this.selected = false;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public boolean accepts(double value) {
    if(Double.isNaN(value)) {
      return false;
    }
    if(value < this.lowerBound) {
      return false;
    }
    double var_4132 = this.upperBound;
    if(value > var_4132) {
      return false;
    }
    if(value == this.lowerBound) {
      return this.includeLowerBound;
    }
    if(value == this.upperBound) {
      return this.includeUpperBound;
    }
    return true;
  }
  public boolean equals(Object obj) {
    if(!(obj instanceof SimpleHistogramBin)) {
      return false;
    }
    SimpleHistogramBin that = (SimpleHistogramBin)obj;
    if(this.lowerBound != that.lowerBound) {
      return false;
    }
    if(this.upperBound != that.upperBound) {
      return false;
    }
    if(this.includeLowerBound != that.includeLowerBound) {
      return false;
    }
    if(this.includeUpperBound != that.includeUpperBound) {
      return false;
    }
    if(this.itemCount != that.itemCount) {
      return false;
    }
    if(this.selected != that.selected) {
      return false;
    }
    return true;
  }
  public boolean isSelected() {
    return this.selected;
  }
  public boolean overlapsWith(SimpleHistogramBin bin) {
    if(this.upperBound < bin.lowerBound) {
      return false;
    }
    if(this.lowerBound > bin.upperBound) {
      return false;
    }
    if(this.upperBound == bin.lowerBound) {
      return this.includeUpperBound && bin.includeLowerBound;
    }
    if(this.lowerBound == bin.upperBound) {
      return this.includeLowerBound && bin.includeUpperBound;
    }
    return true;
  }
  public double getLowerBound() {
    return this.lowerBound;
  }
  public double getUpperBound() {
    return this.upperBound;
  }
  public int compareTo(Object obj) {
    if(!(obj instanceof SimpleHistogramBin)) {
      return 0;
    }
    SimpleHistogramBin bin = (SimpleHistogramBin)obj;
    if(this.lowerBound < bin.lowerBound) {
      return -1;
    }
    if(this.lowerBound > bin.lowerBound) {
      return 1;
    }
    if(this.upperBound < bin.upperBound) {
      return -1;
    }
    if(this.upperBound > bin.upperBound) {
      return 1;
    }
    return 0;
  }
  public int getItemCount() {
    return this.itemCount;
  }
  public void setItemCount(int count) {
    this.itemCount = count;
  }
  public void setSelected(boolean selected) {
    this.selected = selected;
  }
}