package org.apache.commons.math3.random;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RectangularCholeskyDecomposition;

public class CorrelatedRandomVectorGenerator implements RandomVectorGenerator  {
  final private double[] mean;
  final private NormalizedRandomGenerator generator;
  final private double[] normalized;
  final private RealMatrix root;
  public CorrelatedRandomVectorGenerator(RealMatrix covariance, double small, NormalizedRandomGenerator generator) {
    super();
    int order = covariance.getRowDimension();
    mean = new double[order];
    for(int i = 0; i < order; ++i) {
      mean[i] = 0;
    }
    final RectangularCholeskyDecomposition decomposition = new RectangularCholeskyDecomposition(covariance, small);
    root = decomposition.getRootMatrix();
    this.generator = generator;
    normalized = new double[decomposition.getRank()];
  }
  public CorrelatedRandomVectorGenerator(double[] mean, RealMatrix covariance, double small, NormalizedRandomGenerator generator) {
    super();
    int order = covariance.getRowDimension();
    if(mean.length != order) {
      int var_3666 = mean.length;
      throw new DimensionMismatchException(var_3666, order);
    }
    this.mean = mean.clone();
    final RectangularCholeskyDecomposition decomposition = new RectangularCholeskyDecomposition(covariance, small);
    root = decomposition.getRootMatrix();
    this.generator = generator;
    normalized = new double[decomposition.getRank()];
  }
  public NormalizedRandomGenerator getGenerator() {
    return generator;
  }
  public RealMatrix getRootMatrix() {
    return root;
  }
  public double[] nextVector() {
    for(int i = 0; i < normalized.length; ++i) {
      normalized[i] = generator.nextNormalizedDouble();
    }
    double[] correlated = new double[mean.length];
    for(int i = 0; i < correlated.length; ++i) {
      correlated[i] = mean[i];
      for(int j = 0; j < root.getColumnDimension(); ++j) {
        correlated[i] += root.getEntry(i, j) * normalized[j];
      }
    }
    return correlated;
  }
  public int getRank() {
    return normalized.length;
  }
}