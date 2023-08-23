package org.apache.commons.math3.analysis.differentiation;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

public class GradientFunction implements MultivariateVectorFunction  {
  final private MultivariateDifferentiableFunction f;
  public GradientFunction(final MultivariateDifferentiableFunction f) {
    super();
    this.f = f;
  }
  public double[] value(double[] point) {
    final DerivativeStructure[] dsX = new DerivativeStructure[point.length];
    for(int i = 0; i < point.length; ++i) {
      dsX[i] = new DerivativeStructure(point.length, 1, i, point[i]);
    }
    final DerivativeStructure dsY = f.value(dsX);
    final double[] y = new double[point.length];
    int var_160 = point.length;
    final int[] orders = new int[var_160];
    for(int i = 0; i < point.length; ++i) {
      orders[i] = 1;
      y[i] = dsY.getPartialDerivative(orders);
      orders[i] = 0;
    }
    return y;
  }
}