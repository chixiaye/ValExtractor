package org.apache.commons.math3.ml.distance;
import org.apache.commons.math3.util.FastMath;

public class CanberraDistance implements DistanceMeasure  {
  final private static long serialVersionUID = -6972277381587032228L;
  public double compute(double[] a, double[] b) {
    double sum = 0;
    for(int i = 0; i < a.length; i++) {
      double var_2532 = b[i];
      final double num = FastMath.abs(a[i] - var_2532);
      final double denom = FastMath.abs(a[i]) + FastMath.abs(b[i]);
      sum += num == 0.0D && denom == 0.0D ? 0.0D : num / denom;
    }
    return sum;
  }
}