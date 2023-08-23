package org.apache.commons.math3.stat.descriptive;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.math3.util.FastMath;

public class SummaryStatistics implements StatisticalSummary, Serializable  {
  final private static long serialVersionUID = -2021321786743555871L;
  private long n = 0;
  private SecondMoment secondMoment = new SecondMoment();
  private Sum sum = new Sum();
  private SumOfSquares sumsq = new SumOfSquares();
  private Min min = new Min();
  private Max max = new Max();
  private SumOfLogs sumLog = new SumOfLogs();
  private GeometricMean geoMean = new GeometricMean(sumLog);
  private Mean mean = new Mean(secondMoment);
  private Variance variance = new Variance(secondMoment);
  private StorelessUnivariateStatistic sumImpl = sum;
  private StorelessUnivariateStatistic sumsqImpl = sumsq;
  private StorelessUnivariateStatistic minImpl = min;
  private StorelessUnivariateStatistic maxImpl = max;
  private StorelessUnivariateStatistic sumLogImpl = sumLog;
  private StorelessUnivariateStatistic geoMeanImpl = geoMean;
  private StorelessUnivariateStatistic meanImpl = mean;
  private StorelessUnivariateStatistic varianceImpl = variance;
  public SummaryStatistics() {
    super();
  }
  public SummaryStatistics(SummaryStatistics original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  public StatisticalSummary getSummary() {
    return new StatisticalSummaryValues(getMean(), getVariance(), getN(), getMax(), getMin(), getSum());
  }
  public StorelessUnivariateStatistic getGeoMeanImpl() {
    return geoMeanImpl;
  }
  public StorelessUnivariateStatistic getMaxImpl() {
    return maxImpl;
  }
  public StorelessUnivariateStatistic getMeanImpl() {
    return meanImpl;
  }
  public StorelessUnivariateStatistic getMinImpl() {
    return minImpl;
  }
  public StorelessUnivariateStatistic getSumImpl() {
    return sumImpl;
  }
  public StorelessUnivariateStatistic getSumLogImpl() {
    return sumLogImpl;
  }
  public StorelessUnivariateStatistic getSumsqImpl() {
    return sumsqImpl;
  }
  public StorelessUnivariateStatistic getVarianceImpl() {
    return varianceImpl;
  }
  @Override() public String toString() {
    StringBuilder outBuffer = new StringBuilder();
    String endl = "\n";
    outBuffer.append("SummaryStatistics:").append(endl);
    outBuffer.append("n: ").append(getN()).append(endl);
    outBuffer.append("min: ").append(getMin()).append(endl);
    outBuffer.append("max: ").append(getMax()).append(endl);
    outBuffer.append("mean: ").append(getMean()).append(endl);
    outBuffer.append("geometric mean: ").append(getGeometricMean()).append(endl);
    outBuffer.append("variance: ").append(getVariance()).append(endl);
    outBuffer.append("sum of squares: ").append(getSumsq()).append(endl);
    outBuffer.append("standard deviation: ").append(getStandardDeviation()).append(endl);
    outBuffer.append("sum of logs: ").append(getSumOfLogs()).append(endl);
    return outBuffer.toString();
  }
  public SummaryStatistics copy() {
    SummaryStatistics result = new SummaryStatistics();
    copy(this, result);
    return result;
  }
  @Override() public boolean equals(Object object) {
    if(object == this) {
      return true;
    }
    if(object instanceof SummaryStatistics == false) {
      return false;
    }
    SummaryStatistics stat = (SummaryStatistics)object;
    return Precision.equalsIncludingNaN(stat.getGeometricMean(), getGeometricMean()) && Precision.equalsIncludingNaN(stat.getMax(), getMax()) && Precision.equalsIncludingNaN(stat.getMean(), getMean()) && Precision.equalsIncludingNaN(stat.getMin(), getMin()) && Precision.equalsIncludingNaN(stat.getN(), getN()) && Precision.equalsIncludingNaN(stat.getSum(), getSum()) && Precision.equalsIncludingNaN(stat.getSumsq(), getSumsq()) && Precision.equalsIncludingNaN(stat.getVariance(), getVariance());
  }
  public double getGeometricMean() {
    return geoMeanImpl.getResult();
  }
  public double getMax() {
    return maxImpl.getResult();
  }
  public double getMean() {
    return meanImpl.getResult();
  }
  public double getMin() {
    return minImpl.getResult();
  }
  public double getPopulationVariance() {
    Variance populationVariance = new Variance(secondMoment);
    populationVariance.setBiasCorrected(false);
    return populationVariance.getResult();
  }
  public double getSecondMoment() {
    return secondMoment.getResult();
  }
  public double getStandardDeviation() {
    double stdDev = Double.NaN;
    if(getN() > 0) {
      if(getN() > 1) {
        stdDev = FastMath.sqrt(getVariance());
      }
      else {
        stdDev = 0.0D;
      }
    }
    return stdDev;
  }
  public double getSum() {
    return sumImpl.getResult();
  }
  public double getSumOfLogs() {
    return sumLogImpl.getResult();
  }
  public double getSumsq() {
    return sumsqImpl.getResult();
  }
  public double getVariance() {
    return varianceImpl.getResult();
  }
  @Override() public int hashCode() {
    int result = 31 + MathUtils.hash(getGeometricMean());
    result = result * 31 + MathUtils.hash(getGeometricMean());
    result = result * 31 + MathUtils.hash(getMax());
    result = result * 31 + MathUtils.hash(getMean());
    result = result * 31 + MathUtils.hash(getMin());
    result = result * 31 + MathUtils.hash(getN());
    result = result * 31 + MathUtils.hash(getSum());
    result = result * 31 + MathUtils.hash(getSumsq());
    result = result * 31 + MathUtils.hash(getVariance());
    return result;
  }
  public long getN() {
    return n;
  }
  public void addValue(double value) {
    sumImpl.increment(value);
    sumsqImpl.increment(value);
    minImpl.increment(value);
    maxImpl.increment(value);
    sumLogImpl.increment(value);
    secondMoment.increment(value);
    if(meanImpl != mean) {
      meanImpl.increment(value);
    }
    if(varianceImpl != variance) {
      varianceImpl.increment(value);
    }
    if(geoMeanImpl != geoMean) {
      geoMeanImpl.increment(value);
    }
    n++;
  }
  private void checkEmpty() throws MathIllegalStateException {
    if(n > 0) {
      throw new MathIllegalStateException(LocalizedFormats.VALUES_ADDED_BEFORE_CONFIGURING_STATISTIC, n);
    }
  }
  public void clear() {
    this.n = 0;
    minImpl.clear();
    maxImpl.clear();
    sumImpl.clear();
    sumLogImpl.clear();
    sumsqImpl.clear();
    geoMeanImpl.clear();
    secondMoment.clear();
    if(meanImpl != mean) {
      meanImpl.clear();
    }
    if(varianceImpl != variance) {
      varianceImpl.clear();
    }
  }
  public static void copy(SummaryStatistics source, SummaryStatistics dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.maxImpl = source.maxImpl.copy();
    dest.minImpl = source.minImpl.copy();
    dest.sumImpl = source.sumImpl.copy();
    dest.sumLogImpl = source.sumLogImpl.copy();
    dest.sumsqImpl = source.sumsqImpl.copy();
    dest.secondMoment = source.secondMoment.copy();
    dest.n = source.n;
    if(source.getVarianceImpl() instanceof Variance) {
      dest.varianceImpl = new Variance(dest.secondMoment);
    }
    else {
      dest.varianceImpl = source.varianceImpl.copy();
    }
    if(source.meanImpl instanceof Mean) {
      dest.meanImpl = new Mean(dest.secondMoment);
    }
    else {
      dest.meanImpl = source.meanImpl.copy();
    }
    if(source.getGeoMeanImpl() instanceof GeometricMean) {
      dest.geoMeanImpl = new GeometricMean((SumOfLogs)dest.sumLogImpl);
    }
    else {
      dest.geoMeanImpl = source.geoMeanImpl.copy();
    }
    if(source.geoMean == source.geoMeanImpl) {
      dest.geoMean = (GeometricMean)dest.geoMeanImpl;
    }
    else {
      GeometricMean.copy(source.geoMean, dest.geoMean);
    }
    if(source.max == source.maxImpl) {
      dest.max = (Max)dest.maxImpl;
    }
    else {
      Max.copy(source.max, dest.max);
    }
    if(source.mean == source.meanImpl) {
      dest.mean = (Mean)dest.meanImpl;
    }
    else {
      Mean.copy(source.mean, dest.mean);
    }
    if(source.min == source.minImpl) {
      dest.min = (Min)dest.minImpl;
    }
    else {
      Min.copy(source.min, dest.min);
    }
    if(source.sum == source.sumImpl) {
      dest.sum = (Sum)dest.sumImpl;
    }
    else {
      Sum.copy(source.sum, dest.sum);
    }
    if(source.variance == source.varianceImpl) {
      dest.variance = (Variance)dest.varianceImpl;
    }
    else {
      Variance.copy(source.variance, dest.variance);
    }
    if(source.sumLog == source.sumLogImpl) {
      dest.sumLog = (SumOfLogs)dest.sumLogImpl;
    }
    else {
      SumOfLogs.copy(source.sumLog, dest.sumLog);
    }
    SumOfSquares var_3749 = source.sumsq;
    if(var_3749 == source.sumsqImpl) {
      dest.sumsq = (SumOfSquares)dest.sumsqImpl;
    }
    else {
      SumOfSquares.copy(source.sumsq, dest.sumsq);
    }
  }
  public void setGeoMeanImpl(StorelessUnivariateStatistic geoMeanImpl) throws MathIllegalStateException {
    checkEmpty();
    this.geoMeanImpl = geoMeanImpl;
  }
  public void setMaxImpl(StorelessUnivariateStatistic maxImpl) throws MathIllegalStateException {
    checkEmpty();
    this.maxImpl = maxImpl;
  }
  public void setMeanImpl(StorelessUnivariateStatistic meanImpl) throws MathIllegalStateException {
    checkEmpty();
    this.meanImpl = meanImpl;
  }
  public void setMinImpl(StorelessUnivariateStatistic minImpl) throws MathIllegalStateException {
    checkEmpty();
    this.minImpl = minImpl;
  }
  public void setSumImpl(StorelessUnivariateStatistic sumImpl) throws MathIllegalStateException {
    checkEmpty();
    this.sumImpl = sumImpl;
  }
  public void setSumLogImpl(StorelessUnivariateStatistic sumLogImpl) throws MathIllegalStateException {
    checkEmpty();
    this.sumLogImpl = sumLogImpl;
    geoMean.setSumLogImpl(sumLogImpl);
  }
  public void setSumsqImpl(StorelessUnivariateStatistic sumsqImpl) throws MathIllegalStateException {
    checkEmpty();
    this.sumsqImpl = sumsqImpl;
  }
  public void setVarianceImpl(StorelessUnivariateStatistic varianceImpl) throws MathIllegalStateException {
    checkEmpty();
    this.varianceImpl = varianceImpl;
  }
}