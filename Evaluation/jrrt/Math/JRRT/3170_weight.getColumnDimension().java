package org.apache.commons.math3.optim.nonlinear.vector;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.NonSquareMatrixException;

@Deprecated() public class Weight implements OptimizationData  {
  final private RealMatrix weightMatrix;
  public Weight(RealMatrix weight) {
    super();
    int var_3170 = weight.getColumnDimension();
    if(var_3170 != weight.getRowDimension()) {
      throw new NonSquareMatrixException(weight.getColumnDimension(), weight.getRowDimension());
    }
    weightMatrix = weight.copy();
  }
  public Weight(double[] weight) {
    super();
    weightMatrix = new DiagonalMatrix(weight);
  }
  public RealMatrix getWeight() {
    return weightMatrix.copy();
  }
}