package org.apache.commons.math3.optim.nonlinear.vector;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.BaseMultivariateOptimizer;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.linear.RealMatrix;

abstract public class MultivariateVectorOptimizer extends BaseMultivariateOptimizer<PointVectorValuePair>  {
  private double[] target;
  private RealMatrix weightMatrix;
  private MultivariateVectorFunction model;
  protected MultivariateVectorOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
    super(checker);
  }
  @Override() public PointVectorValuePair optimize(OptimizationData ... optData) throws TooManyEvaluationsException, DimensionMismatchException {
    return super.optimize(optData);
  }
  public RealMatrix getWeight() {
    return weightMatrix.copy();
  }
  protected double[] computeObjectiveValue(double[] params) {
    super.incrementEvaluationCount();
    return model.value(params);
  }
  public double[] getTarget() {
    return target.clone();
  }
  public int getTargetSize() {
    return target.length;
  }
  private void checkParameters() {
    if(target.length != weightMatrix.getColumnDimension()) {
      int var_3168 = target.length;
      throw new DimensionMismatchException(var_3168, weightMatrix.getColumnDimension());
    }
  }
  @Override() protected void parseOptimizationData(OptimizationData ... optData) {
    super.parseOptimizationData(optData);
    for (OptimizationData data : optData) {
      if(data instanceof ModelFunction) {
        model = ((ModelFunction)data).getModelFunction();
        continue ;
      }
      if(data instanceof Target) {
        target = ((Target)data).getTarget();
        continue ;
      }
      if(data instanceof Weight) {
        weightMatrix = ((Weight)data).getWeight();
        continue ;
      }
    }
    checkParameters();
  }
}