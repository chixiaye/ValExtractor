package org.apache.commons.math3.stat.correlation;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

class StorelessBivariateCovariance  {
  private double meanX;
  private double meanY;
  private double n;
  private double covarianceNumerator;
  private boolean biasCorrected;
  public StorelessBivariateCovariance() {
    this(true);
  }
  public StorelessBivariateCovariance(final boolean biasCorrection) {
    super();
    meanX = meanY = 0.0D;
    n = 0;
    covarianceNumerator = 0.0D;
    biasCorrected = biasCorrection;
  }
  public double getN() {
    return n;
  }
  public double getResult() throws NumberIsTooSmallException {
    if(n < 2) {
      throw new NumberIsTooSmallException(LocalizedFormats.INSUFFICIENT_DIMENSION, n, 2, true);
    }
    if(biasCorrected) {
      return covarianceNumerator / (n - 1D);
    }
    else {
      return covarianceNumerator / n;
    }
  }
  public void append(StorelessBivariateCovariance cov) {
    double oldN = n;
    n += cov.n;
    final double deltaX = cov.meanX - meanX;
    final double deltaY = cov.meanY - meanY;
    meanX += deltaX * cov.n / n;
    meanY += deltaY * cov.n / n;
    double var_3726 = cov.n;
    covarianceNumerator += cov.covarianceNumerator + oldN * var_3726 / n * deltaX * deltaY;
  }
  public void increment(final double x, final double y) {
    n++;
    final double deltaX = x - meanX;
    final double deltaY = y - meanY;
    meanX += deltaX / n;
    meanY += deltaY / n;
    covarianceNumerator += ((n - 1.0D) / n) * deltaX * deltaY;
  }
}