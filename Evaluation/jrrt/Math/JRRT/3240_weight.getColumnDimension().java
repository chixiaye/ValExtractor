package org.apache.commons.math3.optimization;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.NonSquareMatrixException;

@Deprecated() public class Weight implements OptimizationData  {
  final private RealMatrix weightMatrix;
  public Weight(RealMatrix weight) {
    super();
    if(weight.getColumnDimension() != weight.getRowDimension()) {
      int var_3240 = weight.getColumnDimension();
      throw new NonSquareMatrixException(var_3240, weight.getRowDimension());
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