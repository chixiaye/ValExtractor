package org.apache.commons.math3.stat.correlation;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.FastMath;

public class PearsonsCorrelation  {
  final private RealMatrix correlationMatrix;
  final private int nObs;
  public PearsonsCorrelation() {
    super();
    correlationMatrix = null;
    nObs = 0;
  }
  public PearsonsCorrelation(Covariance covariance) {
    super();
    RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();
    if(covarianceMatrix == null) {
      throw new NullArgumentException(LocalizedFormats.COVARIANCE_MATRIX);
    }
    nObs = covariance.getN();
    correlationMatrix = covarianceToCorrelation(covarianceMatrix);
  }
  public PearsonsCorrelation(RealMatrix covarianceMatrix, int numberOfObservations) {
    super();
    nObs = numberOfObservations;
    correlationMatrix = covarianceToCorrelation(covarianceMatrix);
  }
  public PearsonsCorrelation(RealMatrix matrix) {
    super();
    checkSufficientData(matrix);
    nObs = matrix.getRowDimension();
    correlationMatrix = computeCorrelationMatrix(matrix);
  }
  public PearsonsCorrelation(double[][] data) {
    this(new BlockRealMatrix(data));
  }
  public RealMatrix computeCorrelationMatrix(double[][] data) {
    return computeCorrelationMatrix(new BlockRealMatrix(data));
  }
  public RealMatrix computeCorrelationMatrix(RealMatrix matrix) {
    int nVars = matrix.getColumnDimension();
    RealMatrix outMatrix = new BlockRealMatrix(nVars, nVars);
    for(int i = 0; i < nVars; i++) {
      for(int j = 0; j < i; j++) {
        double corr = correlation(matrix.getColumn(i), matrix.getColumn(j));
        outMatrix.setEntry(i, j, corr);
        outMatrix.setEntry(j, i, corr);
      }
      outMatrix.setEntry(i, i, 1D);
    }
    return outMatrix;
  }
  public RealMatrix covarianceToCorrelation(RealMatrix covarianceMatrix) {
    int nVars = covarianceMatrix.getColumnDimension();
    RealMatrix outMatrix = new BlockRealMatrix(nVars, nVars);
    for(int i = 0; i < nVars; i++) {
      double sigma = FastMath.sqrt(covarianceMatrix.getEntry(i, i));
      outMatrix.setEntry(i, i, 1D);
      for(int j = 0; j < i; j++) {
        double entry = covarianceMatrix.getEntry(i, j) / (sigma * FastMath.sqrt(covarianceMatrix.getEntry(j, j)));
        outMatrix.setEntry(i, j, entry);
        outMatrix.setEntry(j, i, entry);
      }
    }
    return outMatrix;
  }
  public RealMatrix getCorrelationMatrix() {
    return correlationMatrix;
  }
  public RealMatrix getCorrelationPValues() {
    TDistribution tDistribution = new TDistribution(nObs - 2);
    int nVars = correlationMatrix.getColumnDimension();
    double[][] out = new double[nVars][nVars];
    for(int i = 0; i < nVars; i++) {
      for(int j = 0; j < nVars; j++) {
        if(i == j) {
          out[i][j] = 0D;
        }
        else {
          double r = correlationMatrix.getEntry(i, j);
          double t = FastMath.abs(r * FastMath.sqrt((nObs - 2) / (1 - r * r)));
          double[] var_3722 = out[i];
          var_3722[j] = 2 * tDistribution.cumulativeProbability(-t);
        }
      }
    }
    return new BlockRealMatrix(out);
  }
  public RealMatrix getCorrelationStandardErrors() {
    int nVars = correlationMatrix.getColumnDimension();
    double[][] out = new double[nVars][nVars];
    for(int i = 0; i < nVars; i++) {
      for(int j = 0; j < nVars; j++) {
        double r = correlationMatrix.getEntry(i, j);
        out[i][j] = FastMath.sqrt((1 - r * r) / (nObs - 2));
      }
    }
    return new BlockRealMatrix(out);
  }
  public double correlation(final double[] xArray, final double[] yArray) {
    SimpleRegression regression = new SimpleRegression();
    if(xArray.length != yArray.length) {
      throw new DimensionMismatchException(xArray.length, yArray.length);
    }
    else 
      if(xArray.length < 2) {
        throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, xArray.length, 2);
      }
      else {
        for(int i = 0; i < xArray.length; i++) {
          regression.addData(xArray[i], yArray[i]);
        }
        return regression.getR();
      }
  }
  private void checkSufficientData(final RealMatrix matrix) {
    int nRows = matrix.getRowDimension();
    int nCols = matrix.getColumnDimension();
    if(nRows < 2 || nCols < 2) {
      throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_ROWS_AND_COLUMNS, nRows, nCols);
    }
  }
}