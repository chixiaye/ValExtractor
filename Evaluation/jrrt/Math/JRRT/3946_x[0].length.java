package org.apache.commons.math3.stat.regression;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.FastMath;

abstract public class AbstractMultipleLinearRegression implements MultipleLinearRegression  {
  private RealMatrix xMatrix;
  private RealVector yVector;
  private boolean noIntercept = false;
  abstract protected RealMatrix calculateBetaVariance();
  protected RealMatrix getX() {
    return xMatrix;
  }
  abstract protected RealVector calculateBeta();
  protected RealVector calculateResiduals() {
    RealVector b = calculateBeta();
    return yVector.subtract(xMatrix.operate(b));
  }
  protected RealVector getY() {
    return yVector;
  }
  public boolean isNoIntercept() {
    return noIntercept;
  }
  protected double calculateErrorVariance() {
    RealVector residuals = calculateResiduals();
    return residuals.dotProduct(residuals) / (xMatrix.getRowDimension() - xMatrix.getColumnDimension());
  }
  protected double calculateYVariance() {
    return new Variance().evaluate(yVector.toArray());
  }
  public double estimateErrorVariance() {
    return calculateErrorVariance();
  }
  public double estimateRegressandVariance() {
    return calculateYVariance();
  }
  public double estimateRegressionStandardError() {
    return Math.sqrt(estimateErrorVariance());
  }
  public double[] estimateRegressionParameters() {
    RealVector b = calculateBeta();
    return b.toArray();
  }
  public double[] estimateRegressionParametersStandardErrors() {
    double[][] betaVariance = estimateRegressionParametersVariance();
    double sigma = calculateErrorVariance();
    int length = betaVariance[0].length;
    double[] result = new double[length];
    for(int i = 0; i < length; i++) {
      result[i] = FastMath.sqrt(sigma * betaVariance[i][i]);
    }
    return result;
  }
  public double[] estimateResiduals() {
    RealVector b = calculateBeta();
    RealVector e = yVector.subtract(xMatrix.operate(b));
    return e.toArray();
  }
  public double[][] estimateRegressionParametersVariance() {
    return calculateBetaVariance().getData();
  }
  public void newSampleData(double[] data, int nobs, int nvars) {
    if(data == null) {
      throw new NullArgumentException();
    }
    if(data.length != nobs * (nvars + 1)) {
      throw new DimensionMismatchException(data.length, nobs * (nvars + 1));
    }
    if(nobs <= nvars) {
      throw new NumberIsTooSmallException(nobs, nvars, false);
    }
    double[] y = new double[nobs];
    final int cols = noIntercept ? nvars : nvars + 1;
    double[][] x = new double[nobs][cols];
    int pointer = 0;
    for(int i = 0; i < nobs; i++) {
      y[i] = data[pointer++];
      if(!noIntercept) {
        x[i][0] = 1.0D;
      }
      for(int j = noIntercept ? 0 : 1; j < cols; j++) {
        x[i][j] = data[pointer++];
      }
    }
    this.xMatrix = new Array2DRowRealMatrix(x);
    this.yVector = new ArrayRealVector(y);
  }
  protected void newXSampleData(double[][] x) {
    if(x == null) {
      throw new NullArgumentException();
    }
    if(x.length == 0) {
      throw new NoDataException();
    }
    if(noIntercept) {
      this.xMatrix = new Array2DRowRealMatrix(x, true);
    }
    else {
      final int nVars = x[0].length;
      final double[][] xAug = new double[x.length][nVars + 1];
      for(int i = 0; i < x.length; i++) {
        if(x[i].length != nVars) {
          throw new DimensionMismatchException(x[i].length, nVars);
        }
        xAug[i][0] = 1.0D;
        System.arraycopy(x[i], 0, xAug[i], 1, nVars);
      }
      this.xMatrix = new Array2DRowRealMatrix(xAug, false);
    }
  }
  protected void newYSampleData(double[] y) {
    if(y == null) {
      throw new NullArgumentException();
    }
    if(y.length == 0) {
      throw new NoDataException();
    }
    this.yVector = new ArrayRealVector(y);
  }
  public void setNoIntercept(boolean noIntercept) {
    this.noIntercept = noIntercept;
  }
  protected void validateCovarianceData(double[][] x, double[][] covariance) {
    if(x.length != covariance.length) {
      throw new DimensionMismatchException(x.length, covariance.length);
    }
    if(covariance.length > 0 && covariance.length != covariance[0].length) {
      throw new NonSquareMatrixException(covariance.length, covariance[0].length);
    }
  }
  protected void validateSampleData(double[][] x, double[] y) throws MathIllegalArgumentException {
    if((x == null) || (y == null)) {
      throw new NullArgumentException();
    }
    if(x.length != y.length) {
      throw new DimensionMismatchException(y.length, x.length);
    }
    if(x.length == 0) {
      throw new NoDataException();
    }
    if(x[0].length + 1 > x.length) {
      int var_3946 = x[0].length;
      throw new MathIllegalArgumentException(LocalizedFormats.NOT_ENOUGH_DATA_FOR_NUMBER_OF_PREDICTORS, x.length, var_3946);
    }
  }
}