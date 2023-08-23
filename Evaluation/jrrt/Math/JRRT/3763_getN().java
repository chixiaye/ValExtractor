package org.apache.commons.math3.stat.descriptive;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.VectorialCovariance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.math3.util.FastMath;

public class MultivariateSummaryStatistics implements StatisticalMultivariateSummary, Serializable  {
  final private static long serialVersionUID = 2271900808994826718L;
  private int k;
  private long n = 0;
  private StorelessUnivariateStatistic[] sumImpl;
  private StorelessUnivariateStatistic[] sumSqImpl;
  private StorelessUnivariateStatistic[] minImpl;
  private StorelessUnivariateStatistic[] maxImpl;
  private StorelessUnivariateStatistic[] sumLogImpl;
  private StorelessUnivariateStatistic[] geoMeanImpl;
  private StorelessUnivariateStatistic[] meanImpl;
  private VectorialCovariance covarianceImpl;
  public MultivariateSummaryStatistics(int k, boolean isCovarianceBiasCorrected) {
    super();
    this.k = k;
    sumImpl = new StorelessUnivariateStatistic[k];
    sumSqImpl = new StorelessUnivariateStatistic[k];
    minImpl = new StorelessUnivariateStatistic[k];
    maxImpl = new StorelessUnivariateStatistic[k];
    sumLogImpl = new StorelessUnivariateStatistic[k];
    geoMeanImpl = new StorelessUnivariateStatistic[k];
    meanImpl = new StorelessUnivariateStatistic[k];
    for(int i = 0; i < k; ++i) {
      sumImpl[i] = new Sum();
      sumSqImpl[i] = new SumOfSquares();
      minImpl[i] = new Min();
      maxImpl[i] = new Max();
      sumLogImpl[i] = new SumOfLogs();
      geoMeanImpl[i] = new GeometricMean();
      meanImpl[i] = new Mean();
    }
    covarianceImpl = new VectorialCovariance(k, isCovarianceBiasCorrected);
  }
  public RealMatrix getCovariance() {
    return covarianceImpl.getResult();
  }
  public StorelessUnivariateStatistic[] getGeoMeanImpl() {
    return geoMeanImpl.clone();
  }
  public StorelessUnivariateStatistic[] getMaxImpl() {
    return maxImpl.clone();
  }
  public StorelessUnivariateStatistic[] getMeanImpl() {
    return meanImpl.clone();
  }
  public StorelessUnivariateStatistic[] getMinImpl() {
    return minImpl.clone();
  }
  public StorelessUnivariateStatistic[] getSumImpl() {
    return sumImpl.clone();
  }
  public StorelessUnivariateStatistic[] getSumLogImpl() {
    return sumLogImpl.clone();
  }
  public StorelessUnivariateStatistic[] getSumsqImpl() {
    return sumSqImpl.clone();
  }
  @Override() public String toString() {
    final String separator = ", ";
    final String suffix = System.getProperty("line.separator");
    StringBuilder outBuffer = new StringBuilder();
    outBuffer.append("MultivariateSummaryStatistics:" + suffix);
    outBuffer.append("n: " + getN() + suffix);
    append(outBuffer, getMin(), "min: ", separator, suffix);
    append(outBuffer, getMax(), "max: ", separator, suffix);
    append(outBuffer, getMean(), "mean: ", separator, suffix);
    append(outBuffer, getGeometricMean(), "geometric mean: ", separator, suffix);
    append(outBuffer, getSumSq(), "sum of squares: ", separator, suffix);
    append(outBuffer, getSumLog(), "sum of logarithms: ", separator, suffix);
    append(outBuffer, getStandardDeviation(), "standard deviation: ", separator, suffix);
    outBuffer.append("covariance: " + getCovariance().toString() + suffix);
    return outBuffer.toString();
  }
  @Override() public boolean equals(Object object) {
    if(object == this) {
      return true;
    }
    if(object instanceof MultivariateSummaryStatistics == false) {
      return false;
    }
    MultivariateSummaryStatistics stat = (MultivariateSummaryStatistics)object;
    return MathArrays.equalsIncludingNaN(stat.getGeometricMean(), getGeometricMean()) && MathArrays.equalsIncludingNaN(stat.getMax(), getMax()) && MathArrays.equalsIncludingNaN(stat.getMean(), getMean()) && MathArrays.equalsIncludingNaN(stat.getMin(), getMin()) && Precision.equalsIncludingNaN(stat.getN(), getN()) && MathArrays.equalsIncludingNaN(stat.getSum(), getSum()) && MathArrays.equalsIncludingNaN(stat.getSumSq(), getSumSq()) && MathArrays.equalsIncludingNaN(stat.getSumLog(), getSumLog()) && stat.getCovariance().equals(getCovariance());
  }
  public double[] getGeometricMean() {
    return getResults(geoMeanImpl);
  }
  public double[] getMax() {
    return getResults(maxImpl);
  }
  public double[] getMean() {
    return getResults(meanImpl);
  }
  public double[] getMin() {
    return getResults(minImpl);
  }
  private double[] getResults(StorelessUnivariateStatistic[] stats) {
    double[] results = new double[stats.length];
    for(int i = 0; i < results.length; ++i) {
      results[i] = stats[i].getResult();
    }
    return results;
  }
  public double[] getStandardDeviation() {
    double[] stdDev = new double[k];
    if(getN() < 1) {
      Arrays.fill(stdDev, Double.NaN);
    }
    else {
      long var_3763 = getN();
      if(var_3763 < 2) {
        Arrays.fill(stdDev, 0.0D);
      }
      else {
        RealMatrix matrix = covarianceImpl.getResult();
        for(int i = 0; i < k; ++i) {
          stdDev[i] = FastMath.sqrt(matrix.getEntry(i, i));
        }
      }
    }
    return stdDev;
  }
  public double[] getSum() {
    return getResults(sumImpl);
  }
  public double[] getSumLog() {
    return getResults(sumLogImpl);
  }
  public double[] getSumSq() {
    return getResults(sumSqImpl);
  }
  public int getDimension() {
    return k;
  }
  @Override() public int hashCode() {
    int result = 31 + MathUtils.hash(getGeometricMean());
    result = result * 31 + MathUtils.hash(getGeometricMean());
    result = result * 31 + MathUtils.hash(getMax());
    result = result * 31 + MathUtils.hash(getMean());
    result = result * 31 + MathUtils.hash(getMin());
    result = result * 31 + MathUtils.hash(getN());
    result = result * 31 + MathUtils.hash(getSum());
    result = result * 31 + MathUtils.hash(getSumSq());
    result = result * 31 + MathUtils.hash(getSumLog());
    result = result * 31 + getCovariance().hashCode();
    return result;
  }
  public long getN() {
    return n;
  }
  public void addValue(double[] value) throws DimensionMismatchException {
    checkDimension(value.length);
    for(int i = 0; i < k; ++i) {
      double v = value[i];
      sumImpl[i].increment(v);
      sumSqImpl[i].increment(v);
      minImpl[i].increment(v);
      maxImpl[i].increment(v);
      sumLogImpl[i].increment(v);
      geoMeanImpl[i].increment(v);
      meanImpl[i].increment(v);
    }
    covarianceImpl.increment(value);
    n++;
  }
  private void append(StringBuilder buffer, double[] data, String prefix, String separator, String suffix) {
    buffer.append(prefix);
    for(int i = 0; i < data.length; ++i) {
      if(i > 0) {
        buffer.append(separator);
      }
      buffer.append(data[i]);
    }
    buffer.append(suffix);
  }
  private void checkDimension(int dimension) throws DimensionMismatchException {
    if(dimension != k) {
      throw new DimensionMismatchException(dimension, k);
    }
  }
  private void checkEmpty() throws MathIllegalStateException {
    if(n > 0) {
      throw new MathIllegalStateException(LocalizedFormats.VALUES_ADDED_BEFORE_CONFIGURING_STATISTIC, n);
    }
  }
  public void clear() {
    this.n = 0;
    for(int i = 0; i < k; ++i) {
      minImpl[i].clear();
      maxImpl[i].clear();
      sumImpl[i].clear();
      sumLogImpl[i].clear();
      sumSqImpl[i].clear();
      geoMeanImpl[i].clear();
      meanImpl[i].clear();
    }
    covarianceImpl.clear();
  }
  public void setGeoMeanImpl(StorelessUnivariateStatistic[] geoMeanImpl) throws MathIllegalStateException, DimensionMismatchException {
    setImpl(geoMeanImpl, this.geoMeanImpl);
  }
  private void setImpl(StorelessUnivariateStatistic[] newImpl, StorelessUnivariateStatistic[] oldImpl) throws MathIllegalStateException, DimensionMismatchException {
    checkEmpty();
    checkDimension(newImpl.length);
    System.arraycopy(newImpl, 0, oldImpl, 0, newImpl.length);
  }
  public void setMaxImpl(StorelessUnivariateStatistic[] maxImpl) throws MathIllegalStateException, DimensionMismatchException {
    setImpl(maxImpl, this.maxImpl);
  }
  public void setMeanImpl(StorelessUnivariateStatistic[] meanImpl) throws MathIllegalStateException, DimensionMismatchException {
    setImpl(meanImpl, this.meanImpl);
  }
  public void setMinImpl(StorelessUnivariateStatistic[] minImpl) throws MathIllegalStateException, DimensionMismatchException {
    setImpl(minImpl, this.minImpl);
  }
  public void setSumImpl(StorelessUnivariateStatistic[] sumImpl) throws MathIllegalStateException, DimensionMismatchException {
    setImpl(sumImpl, this.sumImpl);
  }
  public void setSumLogImpl(StorelessUnivariateStatistic[] sumLogImpl) throws MathIllegalStateException, DimensionMismatchException {
    setImpl(sumLogImpl, this.sumLogImpl);
  }
  public void setSumsqImpl(StorelessUnivariateStatistic[] sumsqImpl) throws MathIllegalStateException, DimensionMismatchException {
    setImpl(sumsqImpl, this.sumSqImpl);
  }
}