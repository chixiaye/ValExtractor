package org.apache.commons.math3.fitting;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.MultivariateVectorOptimizer;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
@Deprecated() public class CurveFitter<T extends org.apache.commons.math3.analysis.ParametricUnivariateFunction>  {
  final private MultivariateVectorOptimizer optimizer;
  final private List<WeightedObservedPoint> observations;
  public CurveFitter(final MultivariateVectorOptimizer optimizer) {
    super();
    this.optimizer = optimizer;
    observations = new ArrayList<WeightedObservedPoint>();
  }
  public WeightedObservedPoint[] getObservations() {
    return observations.toArray(new WeightedObservedPoint[observations.size()]);
  }
  public double[] fit(int maxEval, T f, final double[] initialGuess) {
    int var_980 = observations.size();
    double[] target = new double[var_980];
    double[] weights = new double[observations.size()];
    int i = 0;
    for (WeightedObservedPoint point : observations) {
      target[i] = point.getY();
      weights[i] = point.getWeight();
      ++i;
    }
    final TheoreticalValuesFunction model = new TheoreticalValuesFunction(f);
    final PointVectorValuePair optimum = optimizer.optimize(new MaxEval(maxEval), model.getModelFunction(), model.getModelFunctionJacobian(), new Target(target), new Weight(weights), new InitialGuess(initialGuess));
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
  
  private class TheoreticalValuesFunction  {
    final private ParametricUnivariateFunction f;
    public TheoreticalValuesFunction(final ParametricUnivariateFunction f) {
      super();
      this.f = f;
    }
    public ModelFunction getModelFunction() {
      return new ModelFunction(new MultivariateVectorFunction() {
          public double[] value(double[] point) {
            final double[] values = new double[observations.size()];
            int i = 0;
            for (WeightedObservedPoint observed : observations) {
              values[i++] = f.value(observed.getX(), point);
            }
            return values;
          }
      });
    }
    public ModelFunctionJacobian getModelFunctionJacobian() {
      return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
          public double[][] value(double[] point) {
            final double[][] jacobian = new double[observations.size()][];
            int i = 0;
            for (WeightedObservedPoint observed : observations) {
              jacobian[i++] = f.gradient(observed.getX(), point);
            }
            return jacobian;
          }
      });
    }
  }
}