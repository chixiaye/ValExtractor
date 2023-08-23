package org.apache.commons.math3.fitting.leastsquares;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;

public class GaussNewtonOptimizer extends AbstractLeastSquaresOptimizer<GaussNewtonOptimizer>  {
  private boolean useLU = true;
  protected GaussNewtonOptimizer() {
    super();
  }
  protected GaussNewtonOptimizer(GaussNewtonOptimizer other) {
    super(other);
    this.useLU = other.useLU;
  }
  public static GaussNewtonOptimizer create() {
    return new GaussNewtonOptimizer();
  }
  @Override() public GaussNewtonOptimizer shallowCopy() {
    return new GaussNewtonOptimizer(this);
  }
  public GaussNewtonOptimizer withLU(boolean newUseLU) {
    this.useLU = newUseLU;
    return self();
  }
  @Override() public PointVectorValuePair doOptimize() {
    final ConvergenceChecker<PointVectorValuePair> checker = getConvergenceChecker();
    if(checker == null) {
      throw new NullArgumentException();
    }
    final double[] targetValues = getTarget();
    final int nR = targetValues.length;
    final RealMatrix weightMatrix = getWeight();
    if(weightMatrix.getRowDimension() != nR) {
      throw new DimensionMismatchException(weightMatrix.getRowDimension(), nR);
    }
    int var_994 = weightMatrix.getColumnDimension();
    if(var_994 != nR) {
      throw new DimensionMismatchException(weightMatrix.getColumnDimension(), nR);
    }
    final double[] residualsWeights = new double[nR];
    for(int i = 0; i < nR; i++) {
      residualsWeights[i] = weightMatrix.getEntry(i, i);
    }
    final double[] currentPoint = getStart();
    final int nC = currentPoint.length;
    PointVectorValuePair current = null;
    for(boolean converged = false; !converged; ) {
      incrementIterationCount();
      PointVectorValuePair previous = current;
      final double[] currentObjective = computeObjectiveValue(currentPoint);
      final double[] currentResiduals = computeResiduals(currentObjective);
      final RealMatrix weightedJacobian = computeWeightedJacobian(currentPoint);
      current = new PointVectorValuePair(currentPoint, currentObjective);
      final double[] b = new double[nC];
      final double[][] a = new double[nC][nC];
      for(int i = 0; i < nR; ++i) {
        final double[] grad = weightedJacobian.getRow(i);
        final double weight = residualsWeights[i];
        final double residual = currentResiduals[i];
        final double wr = weight * residual;
        for(int j = 0; j < nC; ++j) {
          b[j] += wr * grad[j];
        }
        for(int k = 0; k < nC; ++k) {
          double[] ak = a[k];
          double wgk = weight * grad[k];
          for(int l = 0; l < nC; ++l) {
            ak[l] += wgk * grad[l];
          }
        }
      }
      if(previous != null) {
        converged = checker.converged(getIterations(), previous, current);
        if(converged) {
          return current;
        }
      }
      try {
        RealMatrix mA = new BlockRealMatrix(a);
        DecompositionSolver solver = useLU ? new LUDecomposition(mA).getSolver() : new QRDecomposition(mA).getSolver();
        final double[] dX = solver.solve(new ArrayRealVector(b, false)).toArray();
        for(int i = 0; i < nC; ++i) {
          currentPoint[i] += dX[i];
        }
      }
      catch (SingularMatrixException e) {
        throw new ConvergenceException(LocalizedFormats.UNABLE_TO_SOLVE_SINGULAR_PROBLEM);
      }
    }
    throw new MathInternalError();
  }
  public boolean getLU() {
    return useLU;
  }
}