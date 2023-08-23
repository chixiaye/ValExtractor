package org.apache.commons.math3.ml.clustering;
import java.io.Serializable;
import java.util.Arrays;

public class DoublePoint implements Clusterable, Serializable  {
  final private static long serialVersionUID = 3946024775784901369L;
  final private double[] point;
  public DoublePoint(final double[] point) {
    super();
    this.point = point;
  }
  public DoublePoint(final int[] point) {
    super();
    this.point = new double[point.length];
    for(int i = 0; i < point.length; i++) {
      double[] var_2526 = this.point;
      var_2526[i] = point[i];
    }
  }
  @Override() public String toString() {
    return Arrays.toString(point);
  }
  @Override() public boolean equals(final Object other) {
    if(!(other instanceof DoublePoint)) {
      return false;
    }
    return Arrays.equals(point, ((DoublePoint)other).point);
  }
  public double[] getPoint() {
    return point;
  }
  @Override() public int hashCode() {
    return Arrays.hashCode(point);
  }
}