package org.apache.commons.math3.stat.regression;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;

public class OLSMultipleLinearRegression extends AbstractMultipleLinearRegression  {
  private QRDecomposition qr = null;
  @Override() protected RealMatrix calculateBetaVariance() {
    int p = getX().getColumnDimension();
    RealMatrix Raug = qr.getR().getSubMatrix(0, p - 1, 0, p - 1);
    RealMatrix Rinv = new LUDecomposition(Raug).getSolver().getInverse();
    return Rinv.multiply(Rinv.transpose());
  }
  public RealMatrix calculateHat() {
    RealMatrix Q = qr.getQ();
    final int p = qr.getR().getColumnDimension();
    final int n = Q.getColumnDimension();
    Array2DRowRealMatrix augI = new Array2DRowRealMatrix(n, n);
    double[][] augIData = augI.getDataRef();
    for(int i = 0; i < n; i++) {
      for(int j = 0; j < n; j++) {
        if(i == j && i < p) {
          augIData[i][j] = 1D;
        }
        else {
          augIData[i][j] = 0D;
        }
      }
    }
    return Q.multiply(augI).multiply(Q.transpose());
  }
  @Override() protected RealVector calculateBeta() {
    return qr.getSolver().solve(getY());
  }
  public double calculateAdjustedRSquared() throws MathIllegalArgumentException {
    final double n = getX().getRowDimension();
    if(isNoIntercept()) {
      return 1 - (1 - calculateRSquared()) * (n / (n - getX().getColumnDimension()));
    }
    else {
      return 1 - (calculateResidualSumOfSquares() * (n - 1)) / (calculateTotalSumOfSquares() * (n - getX().getColumnDimension()));
    }
  }
  public double calculateRSquared() throws MathIllegalArgumentException {
    return 1 - calculateResidualSumOfSquares() / calculateTotalSumOfSquares();
  }
  public double calculateResidualSumOfSquares() {
    final RealVector residuals = calculateResiduals();
    return residuals.dotProduct(residuals);
  }
  public double calculateTotalSumOfSquares() throws MathIllegalArgumentException {
    if(isNoIntercept()) {
      double[] var_3931 = getY().toArray();
      return StatUtils.sumSq(var_3931);
    }
    else {
      return new SecondMoment().evaluate(getY().toArray());
    }
  }
  public void newSampleData(double[] y, double[][] x) throws MathIllegalArgumentException {
    validateSampleData(x, y);
    newYSampleData(y);
    newXSampleData(x);
  }
  @Override() public void newSampleData(double[] data, int nobs, int nvars) {
    super.newSampleData(data, nobs, nvars);
    qr = new QRDecomposition(getX());
  }
  @Override() protected void newXSampleData(double[][] x) {
    super.newXSampleData(x);
    qr = new QRDecomposition(getX());
  }
}