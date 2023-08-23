package org.jfree.chart.renderer;
import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.PublicCloneable;

public class GrayPaintScale implements PaintScale, PublicCloneable, Serializable  {
  private double lowerBound;
  private double upperBound;
  private int alpha;
  public GrayPaintScale() {
    this(0.0D, 1.0D);
  }
  public GrayPaintScale(double lowerBound, double upperBound) {
    this(lowerBound, upperBound, 255);
  }
  public GrayPaintScale(double lowerBound, double upperBound, int alpha) {
    super();
    if(lowerBound >= upperBound) {
      throw new IllegalArgumentException("Requires lowerBound < upperBound.");
    }
    if(alpha < 0 || alpha > 255) {
      throw new IllegalArgumentException("Requires alpha in the range 0 to 255.");
    }
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.alpha = alpha;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Paint getPaint(double value) {
    double var_2343 = this.lowerBound;
    double v = Math.max(value, var_2343);
    v = Math.min(v, this.upperBound);
    int g = (int)((v - this.lowerBound) / (this.upperBound - this.lowerBound) * 255.0D);
    return new Color(g, g, g, this.alpha);
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof GrayPaintScale)) {
      return false;
    }
    GrayPaintScale that = (GrayPaintScale)obj;
    if(this.lowerBound != that.lowerBound) {
      return false;
    }
    if(this.upperBound != that.upperBound) {
      return false;
    }
    if(this.alpha != that.alpha) {
      return false;
    }
    return true;
  }
  public double getLowerBound() {
    return this.lowerBound;
  }
  public double getUpperBound() {
    return this.upperBound;
  }
  public int getAlpha() {
    return this.alpha;
  }
  public int hashCode() {
    int hash = 7;
    hash = HashUtilities.hashCode(hash, this.lowerBound);
    hash = HashUtilities.hashCode(hash, this.upperBound);
    hash = 43 * hash + this.alpha;
    return hash;
  }
}