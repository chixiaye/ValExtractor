package org.apache.commons.math3.stat.descriptive;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.apache.commons.math3.util.FastMath;

public class DescriptiveStatistics implements StatisticalSummary, Serializable  {
  final public static int INFINITE_WINDOW = -1;
  final private static long serialVersionUID = 4133067267405273064L;
  final private static String SET_QUANTILE_METHOD_NAME = "setQuantile";
  protected int windowSize = INFINITE_WINDOW;
  private ResizableDoubleArray eDA = new ResizableDoubleArray();
  private UnivariateStatistic meanImpl = new Mean();
  private UnivariateStatistic geometricMeanImpl = new GeometricMean();
  private UnivariateStatistic kurtosisImpl = new Kurtosis();
  private UnivariateStatistic maxImpl = new Max();
  private UnivariateStatistic minImpl = new Min();
  private UnivariateStatistic percentileImpl = new Percentile();
  private UnivariateStatistic skewnessImpl = new Skewness();
  private UnivariateStatistic varianceImpl = new Variance();
  private UnivariateStatistic sumsqImpl = new SumOfSquares();
  private UnivariateStatistic sumImpl = new Sum();
  public DescriptiveStatistics() {
    super();
  }
  public DescriptiveStatistics(DescriptiveStatistics original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  public DescriptiveStatistics(double[] initialDoubleArray) {
    super();
    if(initialDoubleArray != null) {
      eDA = new ResizableDoubleArray(initialDoubleArray);
    }
  }
  public DescriptiveStatistics(int window) throws MathIllegalArgumentException {
    super();
    setWindowSize(window);
  }
  public DescriptiveStatistics copy() {
    DescriptiveStatistics result = new DescriptiveStatistics();
    copy(this, result);
    return result;
  }
  @Override() public String toString() {
    StringBuilder outBuffer = new StringBuilder();
    String endl = "\n";
    outBuffer.append("DescriptiveStatistics:").append(endl);
    outBuffer.append("n: ").append(getN()).append(endl);
    outBuffer.append("min: ").append(getMin()).append(endl);
    outBuffer.append("max: ").append(getMax()).append(endl);
    outBuffer.append("mean: ").append(getMean()).append(endl);
    outBuffer.append("std dev: ").append(getStandardDeviation()).append(endl);
    try {
      outBuffer.append("median: ").append(getPercentile(50)).append(endl);
    }
    catch (MathIllegalStateException ex) {
      outBuffer.append("median: unavailable").append(endl);
    }
    outBuffer.append("skewness: ").append(getSkewness()).append(endl);
    outBuffer.append("kurtosis: ").append(getKurtosis()).append(endl);
    return outBuffer.toString();
  }
  public synchronized UnivariateStatistic getGeometricMeanImpl() {
    return geometricMeanImpl;
  }
  public synchronized UnivariateStatistic getKurtosisImpl() {
    return kurtosisImpl;
  }
  public synchronized UnivariateStatistic getMaxImpl() {
    return maxImpl;
  }
  public synchronized UnivariateStatistic getMeanImpl() {
    return meanImpl;
  }
  public synchronized UnivariateStatistic getMinImpl() {
    return minImpl;
  }
  public synchronized UnivariateStatistic getPercentileImpl() {
    return percentileImpl;
  }
  public synchronized UnivariateStatistic getSkewnessImpl() {
    return skewnessImpl;
  }
  public synchronized UnivariateStatistic getSumImpl() {
    return sumImpl;
  }
  public synchronized UnivariateStatistic getSumsqImpl() {
    return sumsqImpl;
  }
  public synchronized UnivariateStatistic getVarianceImpl() {
    return varianceImpl;
  }
  public double apply(UnivariateStatistic stat) {
    return eDA.compute(stat);
  }
  public double getElement(int index) {
    return eDA.getElement(index);
  }
  public double getGeometricMean() {
    return apply(geometricMeanImpl);
  }
  public double getKurtosis() {
    return apply(kurtosisImpl);
  }
  public double getMax() {
    return apply(maxImpl);
  }
  public double getMean() {
    return apply(meanImpl);
  }
  public double getMin() {
    return apply(minImpl);
  }
  public double getPercentile(double p) throws MathIllegalStateException, MathIllegalArgumentException {
    if(percentileImpl instanceof Percentile) {
      ((Percentile)percentileImpl).setQuantile(p);
    }
    else {
      try {
        percentileImpl.getClass().getMethod(SET_QUANTILE_METHOD_NAME, new Class[]{ Double.TYPE } ).invoke(percentileImpl, new Object[]{ Double.valueOf(p) } );
      }
      catch (NoSuchMethodException e1) {
        throw new MathIllegalStateException(LocalizedFormats.PERCENTILE_IMPLEMENTATION_UNSUPPORTED_METHOD, percentileImpl.getClass().getName(), SET_QUANTILE_METHOD_NAME);
      }
      catch (IllegalAccessException e2) {
        throw new MathIllegalStateException(LocalizedFormats.PERCENTILE_IMPLEMENTATION_CANNOT_ACCESS_METHOD, SET_QUANTILE_METHOD_NAME, percentileImpl.getClass().getName());
      }
      catch (InvocationTargetException e3) {
        throw new IllegalStateException(e3.getCause());
      }
    }
    return apply(percentileImpl);
  }
  public double getPopulationVariance() {
    return apply(new Variance(false));
  }
  public double getSkewness() {
    return apply(skewnessImpl);
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
    return apply(sumImpl);
  }
  public double getSumsq() {
    return apply(sumsqImpl);
  }
  public double getVariance() {
    return apply(varianceImpl);
  }
  public double replaceMostRecentValue(double v) throws MathIllegalStateException {
    return eDA.substituteMostRecentElement(v);
  }
  public double[] getSortedValues() {
    double[] sort = getValues();
    Arrays.sort(sort);
    return sort;
  }
  public double[] getValues() {
    return eDA.getElements();
  }
  public int getWindowSize() {
    return windowSize;
  }
  public long getN() {
    return eDA.getNumElements();
  }
  public void addValue(double v) {
    if(windowSize != INFINITE_WINDOW) {
      long var_3775 = getN();
      if(var_3775 == windowSize) {
        eDA.addElementRolling(v);
      }
      else 
        if(getN() < windowSize) {
          eDA.addElement(v);
        }
    }
    else {
      eDA.addElement(v);
    }
  }
  public void clear() {
    eDA.clear();
  }
  public static void copy(DescriptiveStatistics source, DescriptiveStatistics dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.eDA = source.eDA.copy();
    dest.windowSize = source.windowSize;
    dest.maxImpl = source.maxImpl.copy();
    dest.meanImpl = source.meanImpl.copy();
    dest.minImpl = source.minImpl.copy();
    dest.sumImpl = source.sumImpl.copy();
    dest.varianceImpl = source.varianceImpl.copy();
    dest.sumsqImpl = source.sumsqImpl.copy();
    dest.geometricMeanImpl = source.geometricMeanImpl.copy();
    dest.kurtosisImpl = source.kurtosisImpl;
    dest.skewnessImpl = source.skewnessImpl;
    dest.percentileImpl = source.percentileImpl;
  }
  public void removeMostRecentValue() throws MathIllegalStateException {
    try {
      eDA.discardMostRecentElements(1);
    }
    catch (MathIllegalArgumentException ex) {
      throw new MathIllegalStateException(LocalizedFormats.NO_DATA);
    }
  }
  public synchronized void setGeometricMeanImpl(UnivariateStatistic geometricMeanImpl) {
    this.geometricMeanImpl = geometricMeanImpl;
  }
  public synchronized void setKurtosisImpl(UnivariateStatistic kurtosisImpl) {
    this.kurtosisImpl = kurtosisImpl;
  }
  public synchronized void setMaxImpl(UnivariateStatistic maxImpl) {
    this.maxImpl = maxImpl;
  }
  public synchronized void setMeanImpl(UnivariateStatistic meanImpl) {
    this.meanImpl = meanImpl;
  }
  public synchronized void setMinImpl(UnivariateStatistic minImpl) {
    this.minImpl = minImpl;
  }
  public synchronized void setPercentileImpl(UnivariateStatistic percentileImpl) throws MathIllegalArgumentException {
    try {
      percentileImpl.getClass().getMethod(SET_QUANTILE_METHOD_NAME, new Class[]{ Double.TYPE } ).invoke(percentileImpl, new Object[]{ Double.valueOf(50.0D) } );
    }
    catch (NoSuchMethodException e1) {
      throw new MathIllegalArgumentException(LocalizedFormats.PERCENTILE_IMPLEMENTATION_UNSUPPORTED_METHOD, percentileImpl.getClass().getName(), SET_QUANTILE_METHOD_NAME);
    }
    catch (IllegalAccessException e2) {
      throw new MathIllegalArgumentException(LocalizedFormats.PERCENTILE_IMPLEMENTATION_CANNOT_ACCESS_METHOD, SET_QUANTILE_METHOD_NAME, percentileImpl.getClass().getName());
    }
    catch (InvocationTargetException e3) {
      throw new IllegalArgumentException(e3.getCause());
    }
    this.percentileImpl = percentileImpl;
  }
  public synchronized void setSkewnessImpl(UnivariateStatistic skewnessImpl) {
    this.skewnessImpl = skewnessImpl;
  }
  public synchronized void setSumImpl(UnivariateStatistic sumImpl) {
    this.sumImpl = sumImpl;
  }
  public synchronized void setSumsqImpl(UnivariateStatistic sumsqImpl) {
    this.sumsqImpl = sumsqImpl;
  }
  public synchronized void setVarianceImpl(UnivariateStatistic varianceImpl) {
    this.varianceImpl = varianceImpl;
  }
  public void setWindowSize(int windowSize) throws MathIllegalArgumentException {
    if(windowSize < 1 && windowSize != INFINITE_WINDOW) {
      throw new MathIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_WINDOW_SIZE, windowSize);
    }
    this.windowSize = windowSize;
    if(windowSize != INFINITE_WINDOW && windowSize < eDA.getNumElements()) {
      eDA.discardFrontElements(eDA.getNumElements() - windowSize);
    }
  }
}