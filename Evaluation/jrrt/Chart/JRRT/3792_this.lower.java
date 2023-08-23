package org.jfree.data;
import java.io.Serializable;

public strictfp class Range implements Serializable  {
  final private static long serialVersionUID = -906333695431863380L;
  private double lower;
  private double upper;
  public Range(double lower, double upper) {
    super();
    if(lower > upper) {
      String msg = "Range(double, double): require lower (" + lower + ") <= upper (" + upper + ").";
      throw new IllegalArgumentException(msg);
    }
    this.lower = lower;
    this.upper = upper;
  }
  public static Range combine(Range range1, Range range2) {
    if(range1 == null) {
      return range2;
    }
    else {
      if(range2 == null) {
        return range1;
      }
      else {
        double l = Math.min(range1.getLowerBound(), range2.getLowerBound());
        double u = Math.max(range1.getUpperBound(), range2.getUpperBound());
        return new Range(l, u);
      }
    }
  }
  public static Range expand(Range range, double lowerMargin, double upperMargin) {
    if(range == null) {
      throw new IllegalArgumentException("Null \'range\' argument.");
    }
    double length = range.getLength();
    double lower = range.getLowerBound() - length * lowerMargin;
    double upper = range.getUpperBound() + length * upperMargin;
    if(lower > upper) {
      lower = lower / 2.0D + upper / 2.0D;
      upper = lower;
    }
    return new Range(lower, upper);
  }
  public static Range expandToInclude(Range range, double value) {
    if(range == null) {
      return new Range(value, value);
    }
    if(value < range.getLowerBound()) {
      return new Range(value, range.getUpperBound());
    }
    else 
      if(value > range.getUpperBound()) {
        return new Range(range.getLowerBound(), value);
      }
      else {
        return range;
      }
  }
  public static Range scale(Range base, double factor) {
    if(base == null) {
      throw new IllegalArgumentException("Null \'base\' argument.");
    }
    if(factor < 0) {
      throw new IllegalArgumentException("Negative \'factor\' argument.");
    }
    return new Range(base.getLowerBound() * factor, base.getUpperBound() * factor);
  }
  public static Range shift(Range base, double delta) {
    return shift(base, delta, false);
  }
  public static Range shift(Range base, double delta, boolean allowZeroCrossing) {
    if(base == null) {
      throw new IllegalArgumentException("Null \'base\' argument.");
    }
    if(allowZeroCrossing) {
      return new Range(base.getLowerBound() + delta, base.getUpperBound() + delta);
    }
    else {
      return new Range(shiftWithNoZeroCrossing(base.getLowerBound(), delta), shiftWithNoZeroCrossing(base.getUpperBound(), delta));
    }
  }
  public String toString() {
    return ("Range[" + this.lower + "," + this.upper + "]");
  }
  public boolean contains(double value) {
    return (value >= this.lower && value <= this.upper);
  }
  public boolean equals(Object obj) {
    if(!(obj instanceof Range)) {
      return false;
    }
    Range range = (Range)obj;
    if(!(this.lower == range.lower)) {
      return false;
    }
    if(!(this.upper == range.upper)) {
      return false;
    }
    return true;
  }
  public boolean intersects(double b0, double b1) {
    double var_3792 = this.lower;
    if(b0 <= var_3792) {
      return (b1 > this.lower);
    }
    else {
      return (b0 < this.upper && b1 >= b0);
    }
  }
  public boolean intersects(Range range) {
    return intersects(range.getLowerBound(), range.getUpperBound());
  }
  public double constrain(double value) {
    double result = value;
    if(!contains(value)) {
      if(value > this.upper) {
        result = this.upper;
      }
      else 
        if(value < this.lower) {
          result = this.lower;
        }
    }
    return result;
  }
  public double getCentralValue() {
    return this.lower / 2.0D + this.upper / 2.0D;
  }
  public double getLength() {
    return this.upper - this.lower;
  }
  public double getLowerBound() {
    return this.lower;
  }
  public double getUpperBound() {
    return this.upper;
  }
  private static double shiftWithNoZeroCrossing(double value, double delta) {
    if(value > 0.0D) {
      return Math.max(value + delta, 0.0D);
    }
    else 
      if(value < 0.0D) {
        return Math.min(value + delta, 0.0D);
      }
      else {
        return value + delta;
      }
  }
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(this.lower);
    result = (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.upper);
    result = 29 * result + (int)(temp ^ (temp >>> 32));
    return result;
  }
}