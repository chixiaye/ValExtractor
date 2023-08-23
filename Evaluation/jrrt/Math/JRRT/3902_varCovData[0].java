package org.apache.commons.math3.stat.regression;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.exception.OutOfRangeException;

public class RegressionResults implements Serializable  {
  final private static int SSE_IDX = 0;
  final private static int SST_IDX = 1;
  final private static int RSQ_IDX = 2;
  final private static int MSE_IDX = 3;
  final private static int ADJRSQ_IDX = 4;
  final private static long serialVersionUID = 1L;
  final private double[] parameters;
  final private double[][] varCovData;
  final private boolean isSymmetricVCD;
  @SuppressWarnings(value = {"unused", }) final private int rank;
  final private long nobs;
  final private boolean containsConstant;
  final private double[] globalFitInfo;
  @SuppressWarnings(value = {"unused", }) private RegressionResults() {
    super();
    this.parameters = null;
    this.varCovData = null;
    this.rank = -1;
    this.nobs = -1;
    this.containsConstant = false;
    this.isSymmetricVCD = false;
    this.globalFitInfo = null;
  }
  public RegressionResults(final double[] parameters, final double[][] varcov, final boolean isSymmetricCompressed, final long nobs, final int rank, final double sumy, final double sumysq, final double sse, final boolean containsConstant, final boolean copyData) {
    super();
    if(copyData) {
      this.parameters = MathArrays.copyOf(parameters);
      this.varCovData = new double[varcov.length][];
      for(int i = 0; i < varcov.length; i++) {
        this.varCovData[i] = MathArrays.copyOf(varcov[i]);
      }
    }
    else {
      this.parameters = parameters;
      this.varCovData = varcov;
    }
    this.isSymmetricVCD = isSymmetricCompressed;
    this.nobs = nobs;
    this.rank = rank;
    this.containsConstant = containsConstant;
    this.globalFitInfo = new double[5];
    Arrays.fill(this.globalFitInfo, Double.NaN);
    if(rank > 0) {
      this.globalFitInfo[SST_IDX] = containsConstant ? (sumysq - sumy * sumy / nobs) : sumysq;
    }
    this.globalFitInfo[SSE_IDX] = sse;
    this.globalFitInfo[MSE_IDX] = this.globalFitInfo[SSE_IDX] / (nobs - rank);
    this.globalFitInfo[RSQ_IDX] = 1.0D - this.globalFitInfo[SSE_IDX] / this.globalFitInfo[SST_IDX];
    if(!containsConstant) {
      this.globalFitInfo[ADJRSQ_IDX] = 1.0D - (1.0D - this.globalFitInfo[RSQ_IDX]) * ((double)nobs / ((double)(nobs - rank)));
    }
    else {
      this.globalFitInfo[ADJRSQ_IDX] = 1.0D - (sse * (nobs - 1.0D)) / (globalFitInfo[SST_IDX] * (nobs - rank));
    }
  }
  public boolean hasIntercept() {
    return this.containsConstant;
  }
  public double getAdjustedRSquared() {
    return this.globalFitInfo[ADJRSQ_IDX];
  }
  public double getCovarianceOfParameters(int i, int j) throws OutOfRangeException {
    if(parameters == null) {
      return Double.NaN;
    }
    if(i < 0 || i >= this.parameters.length) {
      throw new OutOfRangeException(i, 0, this.parameters.length - 1);
    }
    if(j < 0 || j >= this.parameters.length) {
      throw new OutOfRangeException(j, 0, this.parameters.length - 1);
    }
    return this.getVcvElement(i, j);
  }
  public double getErrorSumSquares() {
    return this.globalFitInfo[SSE_IDX];
  }
  public double getMeanSquareError() {
    return this.globalFitInfo[MSE_IDX];
  }
  public double getParameterEstimate(int index) throws OutOfRangeException {
    if(parameters == null) {
      return Double.NaN;
    }
    if(index < 0 || index >= this.parameters.length) {
      throw new OutOfRangeException(index, 0, this.parameters.length - 1);
    }
    return this.parameters[index];
  }
  public double getRSquared() {
    return this.globalFitInfo[RSQ_IDX];
  }
  public double getRegressionSumSquares() {
    return this.globalFitInfo[SST_IDX] - this.globalFitInfo[SSE_IDX];
  }
  public double getStdErrorOfEstimate(int index) throws OutOfRangeException {
    if(parameters == null) {
      return Double.NaN;
    }
    if(index < 0 || index >= this.parameters.length) {
      throw new OutOfRangeException(index, 0, this.parameters.length - 1);
    }
    double var = this.getVcvElement(index, index);
    if(!Double.isNaN(var) && var > Double.MIN_VALUE) {
      return FastMath.sqrt(var);
    }
    return Double.NaN;
  }
  public double getTotalSumSquares() {
    return this.globalFitInfo[SST_IDX];
  }
  private double getVcvElement(int i, int j) {
    if(this.isSymmetricVCD) {
      if(this.varCovData.length > 1) {
        if(i == j) {
          return varCovData[i][i];
        }
        else 
          if(i >= varCovData[j].length) {
            return varCovData[i][j];
          }
          else {
            return varCovData[j][i];
          }
      }
      else {
        if(i > j) {
          double[] var_3902 = varCovData[0];
          return var_3902[(i + 1) * i / 2 + j];
        }
        else {
          return varCovData[0][(j + 1) * j / 2 + i];
        }
      }
    }
    else {
      return this.varCovData[i][j];
    }
  }
  public double[] getParameterEstimates() {
    if(this.parameters == null) {
      return null;
    }
    return MathArrays.copyOf(parameters);
  }
  public double[] getStdErrorOfEstimates() {
    if(parameters == null) {
      return null;
    }
    double[] se = new double[this.parameters.length];
    for(int i = 0; i < this.parameters.length; i++) {
      double var = this.getVcvElement(i, i);
      if(!Double.isNaN(var) && var > Double.MIN_VALUE) {
        se[i] = FastMath.sqrt(var);
        continue ;
      }
      se[i] = Double.NaN;
    }
    return se;
  }
  public int getNumberOfParameters() {
    if(this.parameters == null) {
      return -1;
    }
    return this.parameters.length;
  }
  public long getN() {
    return this.nobs;
  }
}