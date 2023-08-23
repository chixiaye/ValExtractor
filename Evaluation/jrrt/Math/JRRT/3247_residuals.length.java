package org.apache.commons.math3.optimization;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealMatrix;

@Deprecated() public class LeastSquaresConverter implements MultivariateFunction  {
  final private MultivariateVectorFunction function;
  final private double[] observations;
  final private double[] weights;
  final private RealMatrix scale;
  public LeastSquaresConverter(final MultivariateVectorFunction function, final double[] observations) {
    super();
    this.function = function;
    this.observations = observations.clone();
    this.weights = null;
    this.scale = null;
  }
  public LeastSquaresConverter(final MultivariateVectorFunction function, final double[] observations, final RealMatrix scale) {
    super();
    if(observations.length != scale.getColumnDimension()) {
      throw new DimensionMismatchException(observations.length, scale.getColumnDimension());
    }
    this.function = function;
    this.observations = observations.clone();
    this.weights = null;
    this.scale = scale.copy();
  }
  public LeastSquaresConverter(final MultivariateVectorFunction function, final double[] observations, final double[] weights) {
    super();
    if(observations.length != weights.length) {
      throw new DimensionMismatchException(observations.length, weights.length);
    }
    this.function = function;
    this.observations = observations.clone();
    this.weights = weights.clone();
    this.scale = null;
  }
  public double value(final double[] point) {
    final double[] residuals = function.value(point);
    int var_3247 = residuals.length;
    if(var_3247 != observations.length) {
      throw new DimensionMismatchException(residuals.length, observations.length);
    }
    for(int i = 0; i < residuals.length; ++i) {
      residuals[i] -= observations[i];
    }
    double sumSquares = 0;
    if(weights != null) {
      for(int i = 0; i < residuals.length; ++i) {
        final double ri = residuals[i];
        sumSquares += weights[i] * ri * ri;
      }
    }
    else 
      if(scale != null) {
        for (final double yi : scale.operate(residuals)) {
          sumSquares += yi * yi;
        }
      }
      else {
        for (final double ri : residuals) {
          sumSquares += ri * ri;
        }
      }
    return sumSquares;
  }
}