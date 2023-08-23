package org.apache.commons.math3.optim.nonlinear.scalar;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.Logit;
import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class MultivariateFunctionMappingAdapter implements MultivariateFunction  {
  final private MultivariateFunction bounded;
  final private Mapper[] mappers;
  public MultivariateFunctionMappingAdapter(final MultivariateFunction bounded, final double[] lower, final double[] upper) {
    super();
    MathUtils.checkNotNull(lower);
    MathUtils.checkNotNull(upper);
    if(lower.length != upper.length) {
      throw new DimensionMismatchException(lower.length, upper.length);
    }
    for(int i = 0; i < lower.length; ++i) {
      if(!(upper[i] >= lower[i])) {
        throw new NumberIsTooSmallException(upper[i], lower[i], true);
      }
    }
    this.bounded = bounded;
    this.mappers = new Mapper[lower.length];
    for(int i = 0; i < mappers.length; ++i) {
      if(Double.isInfinite(lower[i])) {
        if(Double.isInfinite(upper[i])) {
          mappers[i] = new NoBoundsMapper();
        }
        else {
          mappers[i] = new UpperBoundMapper(upper[i]);
        }
      }
      else {
        boolean var_2967 = Double.isInfinite(upper[i]);
        if(var_2967) {
          mappers[i] = new LowerBoundMapper(lower[i]);
        }
        else {
          mappers[i] = new LowerUpperBoundMapper(lower[i], upper[i]);
        }
      }
    }
  }
  public double value(double[] point) {
    return bounded.value(unboundedToBounded(point));
  }
  public double[] boundedToUnbounded(double[] point) {
    final double[] mapped = new double[mappers.length];
    for(int i = 0; i < mappers.length; ++i) {
      mapped[i] = mappers[i].boundedToUnbounded(point[i]);
    }
    return mapped;
  }
  public double[] unboundedToBounded(double[] point) {
    final double[] mapped = new double[mappers.length];
    for(int i = 0; i < mappers.length; ++i) {
      mapped[i] = mappers[i].unboundedToBounded(point[i]);
    }
    return mapped;
  }
  
  private static class LowerBoundMapper implements Mapper  {
    final private double lower;
    public LowerBoundMapper(final double lower) {
      super();
      this.lower = lower;
    }
    public double boundedToUnbounded(final double x) {
      return FastMath.log(x - lower);
    }
    public double unboundedToBounded(final double y) {
      return lower + FastMath.exp(y);
    }
  }
  
  private static class LowerUpperBoundMapper implements Mapper  {
    final private UnivariateFunction boundingFunction;
    final private UnivariateFunction unboundingFunction;
    public LowerUpperBoundMapper(final double lower, final double upper) {
      super();
      boundingFunction = new Sigmoid(lower, upper);
      unboundingFunction = new Logit(lower, upper);
    }
    public double boundedToUnbounded(final double x) {
      return unboundingFunction.value(x);
    }
    public double unboundedToBounded(final double y) {
      return boundingFunction.value(y);
    }
  }
  
  private interface Mapper  {
    double boundedToUnbounded(double x);
    double unboundedToBounded(double y);
  }
  
  private static class NoBoundsMapper implements Mapper  {
    public double boundedToUnbounded(final double x) {
      return x;
    }
    public double unboundedToBounded(final double y) {
      return y;
    }
  }
  
  private static class UpperBoundMapper implements Mapper  {
    final private double upper;
    public UpperBoundMapper(final double upper) {
      super();
      this.upper = upper;
    }
    public double boundedToUnbounded(final double x) {
      return -FastMath.log(upper - x);
    }
    public double unboundedToBounded(final double y) {
      return upper - FastMath.exp(-y);
    }
  }
}