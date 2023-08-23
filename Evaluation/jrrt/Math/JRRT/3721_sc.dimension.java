package org.apache.commons.math3.stat.correlation;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class StorelessCovariance extends Covariance  {
  private StorelessBivariateCovariance[] covMatrix;
  private int dimension;
  public StorelessCovariance(final int dim) {
    this(dim, true);
  }
  public StorelessCovariance(final int dim, final boolean biasCorrected) {
    super();
    dimension = dim;
    covMatrix = new StorelessBivariateCovariance[dimension * (dimension + 1) / 2];
    initializeMatrix(biasCorrected);
  }
  @Override() public RealMatrix getCovarianceMatrix() throws NumberIsTooSmallException {
    return MatrixUtils.createRealMatrix(getData());
  }
  private StorelessBivariateCovariance getElement(final int i, final int j) {
    return covMatrix[indexOf(i, j)];
  }
  public double getCovariance(final int xIndex, final int yIndex) throws NumberIsTooSmallException {
    return getElement(xIndex, yIndex).getResult();
  }
  public double[][] getData() throws NumberIsTooSmallException {
    final double[][] data = new double[dimension][dimension];
    for(int i = 0; i < dimension; i++) {
      for(int j = 0; j < dimension; j++) {
        data[i][j] = getElement(i, j).getResult();
      }
    }
    return data;
  }
  @Override() public int getN() throws MathUnsupportedOperationException {
    throw new MathUnsupportedOperationException();
  }
  private int indexOf(final int i, final int j) {
    return j < i ? i * (i + 1) / 2 + j : j * (j + 1) / 2 + i;
  }
  public void append(StorelessCovariance sc) throws DimensionMismatchException {
    int var_3721 = sc.dimension;
    if(var_3721 != dimension) {
      throw new DimensionMismatchException(sc.dimension, dimension);
    }
    for(int i = 0; i < dimension; i++) {
      for(int j = i; j < dimension; j++) {
        getElement(i, j).append(sc.getElement(i, j));
      }
    }
  }
  public void increment(final double[] data) throws DimensionMismatchException {
    int length = data.length;
    if(length != dimension) {
      throw new DimensionMismatchException(length, dimension);
    }
    for(int i = 0; i < length; i++) {
      for(int j = i; j < length; j++) {
        getElement(i, j).increment(data[i], data[j]);
      }
    }
  }
  private void initializeMatrix(final boolean biasCorrected) {
    for(int i = 0; i < dimension; i++) {
      for(int j = 0; j < dimension; j++) {
        setElement(i, j, new StorelessBivariateCovariance(biasCorrected));
      }
    }
  }
  private void setElement(final int i, final int j, final StorelessBivariateCovariance cov) {
    covMatrix[indexOf(i, j)] = cov;
  }
}