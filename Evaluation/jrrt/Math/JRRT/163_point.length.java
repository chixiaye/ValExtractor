package org.apache.commons.math3.analysis.differentiation;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;

public class JacobianFunction implements MultivariateMatrixFunction  {
  final private MultivariateDifferentiableVectorFunction f;
  public JacobianFunction(final MultivariateDifferentiableVectorFunction f) {
    super();
    this.f = f;
  }
  public double[][] value(double[] point) {
    final DerivativeStructure[] dsX = new DerivativeStructure[point.length];
    for(int i = 0; i < point.length; ++i) {
      dsX[i] = new DerivativeStructure(point.length, 1, i, point[i]);
    }
    final DerivativeStructure[] dsY = f.value(dsX);
    int var_163 = point.length;
    final double[][] y = new double[dsY.length][var_163];
    final int[] orders = new int[point.length];
    for(int i = 0; i < dsY.length; ++i) {
      for(int j = 0; j < point.length; ++j) {
        orders[j] = 1;
        y[i][j] = dsY[i].getPartialDerivative(orders);
        orders[j] = 0;
      }
    }
    return y;
  }
}