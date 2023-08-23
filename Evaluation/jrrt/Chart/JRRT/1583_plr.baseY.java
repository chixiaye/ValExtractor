package org.jfree.chart.plot;
import java.io.Serializable;
import org.jfree.chart.text.TextBox;

public class PieLabelRecord implements Comparable, Serializable  {
  private Comparable key;
  private double angle;
  private double baseY;
  private double allocatedY;
  private TextBox label;
  private double labelHeight;
  private double gap;
  private double linkPercent;
  public PieLabelRecord(Comparable key, double angle, double baseY, TextBox label, double labelHeight, double gap, double linkPercent) {
    super();
    this.key = key;
    this.angle = angle;
    this.baseY = baseY;
    this.allocatedY = baseY;
    this.label = label;
    this.labelHeight = labelHeight;
    this.gap = gap;
    this.linkPercent = linkPercent;
  }
  public Comparable getKey() {
    return this.key;
  }
  public String toString() {
    return this.baseY + ", " + this.key.toString();
  }
  public TextBox getLabel() {
    return this.label;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof PieLabelRecord)) {
      return false;
    }
    PieLabelRecord that = (PieLabelRecord)obj;
    if(!this.key.equals(that.key)) {
      return false;
    }
    if(this.angle != that.angle) {
      return false;
    }
    if(this.gap != that.gap) {
      return false;
    }
    if(this.allocatedY != that.allocatedY) {
      return false;
    }
    if(this.baseY != that.baseY) {
      return false;
    }
    if(this.labelHeight != that.labelHeight) {
      return false;
    }
    if(this.linkPercent != that.linkPercent) {
      return false;
    }
    if(!this.label.equals(that.label)) {
      return false;
    }
    return true;
  }
  public double getAllocatedY() {
    return this.allocatedY;
  }
  public double getAngle() {
    return this.angle;
  }
  public double getBaseY() {
    return this.baseY;
  }
  public double getGap() {
    return this.gap;
  }
  public double getLabelHeight() {
    return this.labelHeight;
  }
  public double getLinkPercent() {
    return this.linkPercent;
  }
  public double getLowerY() {
    return this.allocatedY - this.labelHeight / 2.0D;
  }
  public double getUpperY() {
    return this.allocatedY + this.labelHeight / 2.0D;
  }
  public int compareTo(Object obj) {
    int result = 0;
    if(obj instanceof PieLabelRecord) {
      PieLabelRecord plr = (PieLabelRecord)obj;
      double var_1583 = plr.baseY;
      if(this.baseY < var_1583) {
        result = -1;
      }
      else 
        if(this.baseY > plr.baseY) {
          result = 1;
        }
    }
    return result;
  }
  public void setAllocatedY(double y) {
    this.allocatedY = y;
  }
  public void setBaseY(double base) {
    this.baseY = base;
  }
}