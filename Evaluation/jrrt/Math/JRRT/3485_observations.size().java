package org.apache.commons.math3.optimization.fitting;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction;
import org.apache.commons.math3.optimization.DifferentiableMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.MultivariateDifferentiableVectorOptimizer;
import org.apache.commons.math3.optimization.PointVectorValuePair;
@Deprecated() public class CurveFitter<T extends org.apache.commons.math3.analysis.ParametricUnivariateFunction>  {
  @Deprecated() final private DifferentiableMultivariateVectorOptimizer oldOptimizer;
  final private MultivariateDifferentiableVectorOptimizer optimizer;
  final private List<WeightedObservedPoint> observations;
  @Deprecated() public CurveFitter(final DifferentiableMultivariateVectorOptimizer optimizer) {
    super();
    this.oldOptimizer = optimizer;
    this.optimizer = null;
    observations = new ArrayList<WeightedObservedPoint>();
  }
  public CurveFitter(final MultivariateDifferentiableVectorOptimizer optimizer) {
    super();
    this.oldOptimizer = null;
    this.optimizer = optimizer;
    observations = new ArrayList<WeightedObservedPoint>();
  }
  public WeightedObservedPoint[] getObservations() {
    return observations.toArray(new WeightedObservedPoint[observations.size()]);
  }
  public double[] fit(int maxEval, T f, final double[] initialGuess) {
    double[] target = new double[observations.size()];
    int var_3485 = observations.size();
    double[] weights = new double[var_3485];
    int i = 0;
    for (WeightedObservedPoint point : observations) {
      target[i] = point.getY();
      weights[i] = point.getWeight();
      ++i;
    }
    final PointVectorValuePair optimum;
    if(optimizer == null) {
      optimum = oldOptimizer.optimize(maxEval, new OldTheoreticalValuesFunction(f), target, weights, initialGuess);
    }
    else {
      optimum = optimizer.optimize(maxEval, new TheoreticalValuesFunction(f), target, weights, initialGuess);
    }
    return optimum.getPointRef();
  }
  public double[] fit(T f, final double[] initialGuess) {
    return fit(Integer.MAX_VALUE, f, initialGuess);
  }
  public void addObservedPoint(double x, double y) {
    addObservedPoint(1.0D, x, y);
  }
  public void addObservedPoint(double weight, double x, double y) {
    observations.add(new WeightedObservedPoint(weight, x, y));
  }
  public void addObservedPoint(WeightedObservedPoint observed) {
    observations.add(observed);
  }
  public void clearObservations() {
    observations.clear();
  }
  
  @Deprecated() private class OldTheoreticalValuesFunction implements DifferentiableMultivariateVectorFunction  {
    final private ParametricUnivariateFunction f;
    public OldTheoreticalValuesFunction(final ParametricUnivariateFunction f) {
      super();
      this.f = f;
    }
    public MultivariateMatrixFunction jacobian() {
      return new MultivariateMatrixFunction() {
          public double[][] value(double[] point) {
            final double[][] jacobian = new double[observations.size()][];
            int i = 0;
            for (WeightedObservedPoint observed : observations) {
              jacobian[i++] = f.gradient(observed.getX(), point);
            }
            return jacobian;
          }
      };
    }
    public double[] value(double[] point) {
      final double[] values = new double[observations.size()];
      int i = 0;
      for (WeightedObservedPoint observed : observations) {
        values[i++] = f.value(observed.getX(), point);
      }
      return values;
    }
  }
  
  private class TheoreticalValuesFunction implements MultivariateDifferentiableVectorFunction  {
    final private ParametricUnivariateFunction f;
    public TheoreticalValuesFunction(final ParametricUnivariateFunction f) {
      super();
      this.f = f;
    }
    public DerivativeStructure[] value(DerivativeStructure[] point) {
      final double[] parameters = new double[point.length];
      for(int k = 0; k < point.length; ++k) {
        parameters[k] = point[k].getValue();
      }
      final DerivativeStructure[] values = new DerivativeStructure[observations.size()];
      int i = 0;
      for (WeightedObservedPoint observed : observations) {
        DerivativeStructure vi = new DerivativeStructure(point.length, 1, f.value(observed.getX(), parameters));
        for(int k = 0; k < point.length; ++k) {
          vi = vi.add(new DerivativeStructure(point.length, 1, k, 0.0D));
        }
        values[i++] = vi;
      }
      return values;
    }
    public double[] value(double[] point) {
      final double[] values = new double[observations.size()];
      int i = 0;
      for (WeightedObservedPoint observed : observations) {
        values[i++] = f.value(observed.getX(), point);
      }
      return values;
    }
  }
}