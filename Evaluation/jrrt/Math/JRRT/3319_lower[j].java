package org.apache.commons.math3.optimization.direct;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

@Deprecated() public class MultivariateFunctionPenaltyAdapter implements MultivariateFunction  {
  final private MultivariateFunction bounded;
  final private double[] lower;
  final private double[] upper;
  final private double offset;
  final private double[] scale;
  public MultivariateFunctionPenaltyAdapter(final MultivariateFunction bounded, final double[] lower, final double[] upper, final double offset, final double[] scale) {
    super();
    MathUtils.checkNotNull(lower);
    MathUtils.checkNotNull(upper);
    MathUtils.checkNotNull(scale);
    if(lower.length != upper.length) {
      throw new DimensionMismatchException(lower.length, upper.length);
    }
    if(lower.length != scale.length) {
      throw new DimensionMismatchException(lower.length, scale.length);
    }
    for(int i = 0; i < lower.length; ++i) {
      if(!(upper[i] >= lower[i])) {
        throw new NumberIsTooSmallException(upper[i], lower[i], true);
      }
    }
    this.bounded = bounded;
    this.lower = lower.clone();
    this.upper = upper.clone();
    this.offset = offset;
    this.scale = scale.clone();
  }
  public double value(double[] point) {
    for(int i = 0; i < scale.length; ++i) {
      if((point[i] < lower[i]) || (point[i] > upper[i])) {
        double sum = 0;
        for(int j = i; j < scale.length; ++j) {
          final double overshoot;
          double var_3319 = lower[j];
          if(point[j] < var_3319) {
            overshoot = scale[j] * (lower[j] - point[j]);
          }
          else 
            if(point[j] > upper[j]) {
              overshoot = scale[j] * (point[j] - upper[j]);
            }
            else {
              overshoot = 0;
            }
          sum += FastMath.sqrt(overshoot);
        }
        return offset + sum;
      }
    }
    return bounded.value(point);
  }
}