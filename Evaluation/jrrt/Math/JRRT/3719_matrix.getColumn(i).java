package org.apache.commons.math3.stat.correlation;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

public class Covariance  {
  final private RealMatrix covarianceMatrix;
  final private int n;
  public Covariance() {
    super();
    covarianceMatrix = null;
    n = 0;
  }
  public Covariance(RealMatrix matrix) throws MathIllegalArgumentException {
    this(matrix, true);
  }
  public Covariance(RealMatrix matrix, boolean biasCorrected) throws MathIllegalArgumentException {
    super();
    checkSufficientData(matrix);
    n = matrix.getRowDimension();
    covarianceMatrix = computeCovarianceMatrix(matrix, biasCorrected);
  }
  public Covariance(double[][] data) throws MathIllegalArgumentException, NotStrictlyPositiveException {
    this(data, true);
  }
  public Covariance(double[][] data, boolean biasCorrected) throws MathIllegalArgumentException, NotStrictlyPositiveException {
    this(new BlockRealMatrix(data), biasCorrected);
  }
  protected RealMatrix computeCovarianceMatrix(double[][] data) throws MathIllegalArgumentException, NotStrictlyPositiveException {
    return computeCovarianceMatrix(data, true);
  }
  protected RealMatrix computeCovarianceMatrix(double[][] data, boolean biasCorrected) throws MathIllegalArgumentException, NotStrictlyPositiveException {
    return computeCovarianceMatrix(new BlockRealMatrix(data), biasCorrected);
  }
  protected RealMatrix computeCovarianceMatrix(RealMatrix matrix) throws MathIllegalArgumentException {
    return computeCovarianceMatrix(matrix, true);
  }
  protected RealMatrix computeCovarianceMatrix(RealMatrix matrix, boolean biasCorrected) throws MathIllegalArgumentException {
    int dimension = matrix.getColumnDimension();
    Variance variance = new Variance(biasCorrected);
    RealMatrix outMatrix = new BlockRealMatrix(dimension, dimension);
    for(int i = 0; i < dimension; i++) {
      for(int j = 0; j < i; j++) {
        double[] var_3719 = matrix.getColumn(i);
        double cov = covariance(var_3719, matrix.getColumn(j), biasCorrected);
        outMatrix.setEntry(i, j, cov);
        outMatrix.setEntry(j, i, cov);
      }
      outMatrix.setEntry(i, i, variance.evaluate(matrix.getColumn(i)));
    }
    return outMatrix;
  }
  public RealMatrix getCovarianceMatrix() {
    return covarianceMatrix;
  }
  public double covariance(final double[] xArray, final double[] yArray) throws MathIllegalArgumentException {
    return covariance(xArray, yArray, true);
  }
  public double covariance(final double[] xArray, final double[] yArray, boolean biasCorrected) throws MathIllegalArgumentException {
    Mean mean = new Mean();
    double result = 0D;
    int length = xArray.length;
    if(length != yArray.length) {
      throw new MathIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, length, yArray.length);
    }
    else 
      if(length < 2) {
        throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE, length, 2);
      }
      else {
        double xMean = mean.evaluate(xArray);
        double yMean = mean.evaluate(yArray);
        for(int i = 0; i < length; i++) {
          double xDev = xArray[i] - xMean;
          double yDev = yArray[i] - yMean;
          result += (xDev * yDev - result) / (i + 1);
        }
      }
    return biasCorrected ? result * ((double)length / (double)(length - 1)) : result;
  }
  public int getN() {
    return n;
  }
  private void checkSufficientData(final RealMatrix matrix) throws MathIllegalArgumentException {
    int nRows = matrix.getRowDimension();
    int nCols = matrix.getColumnDimension();
    if(nRows < 2 || nCols < 1) {
      throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_ROWS_AND_COLUMNS, nRows, nCols);
    }
  }
}