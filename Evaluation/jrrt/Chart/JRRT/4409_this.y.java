package org.jfree.data.xy;
import java.io.Serializable;

public class Vector implements Serializable  {
  private double x;
  private double y;
  public Vector(double x, double y) {
    super();
    this.x = x;
    this.y = y;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof Vector)) {
      return false;
    }
    Vector that = (Vector)obj;
    if(this.x != that.x) {
      return false;
    }
    if(this.y != that.y) {
      return false;
    }
    return true;
  }
  public double getAngle() {
    return Math.atan2(this.y, this.x);
  }
  public double getLength() {
    double var_4409 = this.y;
    return Math.sqrt((this.x * this.x) + (var_4409 * this.y));
  }
  public double getX() {
    return this.x;
  }
  public double getY() {
    return this.y;
  }
  public int hashCode() {
    int result = 193;
    long temp = Double.doubleToLongBits(this.x);
    result = 37 * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.y);
    result = 37 * result + (int)(temp ^ (temp >>> 32));
    return result;
  }
}