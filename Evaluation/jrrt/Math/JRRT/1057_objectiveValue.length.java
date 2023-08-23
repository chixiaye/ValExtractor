package org.apache.commons.math3.fitting.leastsquares;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.optim.AbstractOptimizer;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.util.FastMath;
abstract public class AbstractLeastSquaresOptimizer<OPTIM extends org.apache.commons.math3.fitting.leastsquares.AbstractLeastSquaresOptimizer<org.apache.commons.math3.fitting.leastsquares.AbstractLeastSquaresOptimizer@OPTIM>> extends AbstractOptimizer<PointVectorValuePair, OPTIM> implements WithTarget<OPTIM>, WithWeight<OPTIM>, WithModelAndJacobian<OPTIM>, WithStartPoint<OPTIM>  {
  private double[] target;
  private RealMatrix weight;
  private MultivariateVectorFunction model;
  private MultivariateMatrixFunction jacobian;
  private RealMatrix weightSqrt;
  private double[] start;
  protected AbstractLeastSquaresOptimizer() {
    super();
  }
  protected AbstractLeastSquaresOptimizer(AbstractLeastSquaresOptimizer other) {
    super(other);
    target = other.target == null ? null : other.target.clone();
    start = other.start == null ? null : other.start.clone();
    weight = other.weight == null ? null : other.weight.copy();
    weightSqrt = other.weightSqrt == null ? null : other.weightSqrt.copy();
    model = other.model;
    jacobian = other.jacobian;
  }
  public MultivariateMatrixFunction getJacobian() {
    return jacobian;
  }
  public MultivariateVectorFunction getModel() {
    return model;
  }
  public OPTIM withModelAndJacobian(MultivariateVectorFunction newModel, MultivariateMatrixFunction newJacobian) {
    this.model = newModel;
    this.jacobian = newJacobian;
    return self();
  }
  public OPTIM withStartPoint(double[] newStart) {
    this.start = newStart.clone();
    return self();
  }
  public OPTIM withTarget(double[] newTarget) {
    this.target = newTarget.clone();
    return self();
  }
  public OPTIM withWeight(RealMatrix newWeight) {
    this.weight = newWeight;
    weightSqrt = squareRoot(newWeight);
    return self();
  }
  protected RealMatrix computeWeightedJacobian(double[] params) {
    return weightSqrt.multiply(MatrixUtils.createRealMatrix(computeJacobian(params)));
  }
  public RealMatrix getWeight() {
    return weight.copy();
  }
  public RealMatrix getWeightSquareRoot() {
    return weightSqrt == null ? null : weightSqrt.copy();
  }
  private RealMatrix squareRoot(RealMatrix m) {
    if(m instanceof DiagonalMatrix) {
      final int dim = m.getRowDimension();
      final RealMatrix sqrtM = new DiagonalMatrix(dim);
      for(int i = 0; i < dim; i++) {
        sqrtM.setEntry(i, i, FastMath.sqrt(m.getEntry(i, i)));
      }
      return sqrtM;
    }
    else {
      final EigenDecomposition dec = new EigenDecomposition(m);
      return dec.getSquareRoot();
    }
  }
  protected double computeCost(double[] residuals) {
    final ArrayRealVector r = new ArrayRealVector(residuals);
    return FastMath.sqrt(r.dotProduct(weight.operate(r)));
  }
  public double computeRMS(double[] params) {
    final double cost = computeCost(computeResiduals(getModel().value(params)));
    return FastMath.sqrt(cost * cost / target.length);
  }
  protected double[] computeObjectiveValue(double[] params) {
    super.incrementEvaluationCount();
    return model.value(params);
  }
  protected double[] computeResiduals(double[] objectiveValue) {
    if(objectiveValue.length != target.length) {
      int var_1057 = objectiveValue.length;
      throw new DimensionMismatchException(target.length, var_1057);
    }
    final double[] residuals = new double[target.length];
    for(int i = 0; i < target.length; i++) {
      residuals[i] = target[i] - objectiveValue[i];
    }
    return residuals;
  }
  public double[] computeSigma(double[] params, double covarianceSingularityThreshold) {
    final int nC = params.length;
    final double[] sig = new double[nC];
    final double[][] cov = computeCovariances(params, covarianceSingularityThreshold);
    for(int i = 0; i < nC; ++i) {
      sig[i] = FastMath.sqrt(cov[i][i]);
    }
    return sig;
  }
  public double[] getStart() {
    return start == null ? null : start.clone();
  }
  public double[] getTarget() {
    return target == null ? null : target.clone();
  }
  public double[][] computeCovariances(double[] params, double threshold) {
    final RealMatrix j = computeWeightedJacobian(params);
    final RealMatrix jTj = j.transpose().multiply(j);
    final DecompositionSolver solver = new QRDecomposition(jTj, threshold).getSolver();
    return solver.getInverse().getData();
  }
  protected double[][] computeJacobian(final double[] params) {
    return jacobian.value(params);
  }
}