package org.apache.commons.math3.stat.inference;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.util.FastMath;

public class TTest  {
  public boolean homoscedasticTTest(final double[] sample1, final double[] sample2, final double alpha) throws NullArgumentException, NumberIsTooSmallException, OutOfRangeException, MaxCountExceededException {
    checkSignificanceLevel(alpha);
    return homoscedasticTTest(sample1, sample2) < alpha;
  }
  public boolean pairedTTest(final double[] sample1, final double[] sample2, final double alpha) throws NullArgumentException, NoDataException, DimensionMismatchException, NumberIsTooSmallException, OutOfRangeException, MaxCountExceededException {
    checkSignificanceLevel(alpha);
    return pairedTTest(sample1, sample2) < alpha;
  }
  public boolean tTest(final double mu, final double[] sample, final double alpha) throws NullArgumentException, NumberIsTooSmallException, OutOfRangeException, MaxCountExceededException {
    checkSignificanceLevel(alpha);
    return tTest(mu, sample) < alpha;
  }
  public boolean tTest(final double mu, final StatisticalSummary sampleStats, final double alpha) throws NullArgumentException, NumberIsTooSmallException, OutOfRangeException, MaxCountExceededException {
    checkSignificanceLevel(alpha);
    return tTest(mu, sampleStats) < alpha;
  }
  public boolean tTest(final double[] sample1, final double[] sample2, final double alpha) throws NullArgumentException, NumberIsTooSmallException, OutOfRangeException, MaxCountExceededException {
    checkSignificanceLevel(alpha);
    return tTest(sample1, sample2) < alpha;
  }
  public boolean tTest(final StatisticalSummary sampleStats1, final StatisticalSummary sampleStats2, final double alpha) throws NullArgumentException, NumberIsTooSmallException, OutOfRangeException, MaxCountExceededException {
    checkSignificanceLevel(alpha);
    return tTest(sampleStats1, sampleStats2) < alpha;
  }
  protected double df(double v1, double v2, double n1, double n2) {
    return (((v1 / n1) + (v2 / n2)) * ((v1 / n1) + (v2 / n2))) / ((v1 * v1) / (n1 * n1 * (n1 - 1D)) + (v2 * v2) / (n2 * n2 * (n2 - 1D)));
  }
  protected double homoscedasticT(final double m1, final double m2, final double v1, final double v2, final double n1, final double n2) {
    final double pooledVariance = ((n1 - 1) * v1 + (n2 - 1) * v2) / (n1 + n2 - 2);
    return (m1 - m2) / FastMath.sqrt(pooledVariance * (1D / n1 + 1D / n2));
  }
  public double homoscedasticT(final double[] sample1, final double[] sample2) throws NullArgumentException, NumberIsTooSmallException {
    checkSampleData(sample1);
    checkSampleData(sample2);
    return homoscedasticT(StatUtils.mean(sample1), StatUtils.mean(sample2), StatUtils.variance(sample1), StatUtils.variance(sample2), sample1.length, sample2.length);
  }
  public double homoscedasticT(final StatisticalSummary sampleStats1, final StatisticalSummary sampleStats2) throws NullArgumentException, NumberIsTooSmallException {
    checkSampleData(sampleStats1);
    checkSampleData(sampleStats2);
    return homoscedasticT(sampleStats1.getMean(), sampleStats2.getMean(), sampleStats1.getVariance(), sampleStats2.getVariance(), sampleStats1.getN(), sampleStats2.getN());
  }
  protected double homoscedasticTTest(double m1, double m2, double v1, double v2, double n1, double n2) throws MaxCountExceededException, NotStrictlyPositiveException {
    final double t = FastMath.abs(homoscedasticT(m1, m2, v1, v2, n1, n2));
    final double degreesOfFreedom = n1 + n2 - 2;
    TDistribution distribution = new TDistribution(degreesOfFreedom);
    return 2.0D * distribution.cumulativeProbability(-t);
  }
  public double homoscedasticTTest(final double[] sample1, final double[] sample2) throws NullArgumentException, NumberIsTooSmallException, MaxCountExceededException {
    checkSampleData(sample1);
    checkSampleData(sample2);
    return homoscedasticTTest(StatUtils.mean(sample1), StatUtils.mean(sample2), StatUtils.variance(sample1), StatUtils.variance(sample2), sample1.length, sample2.length);
  }
  public double homoscedasticTTest(final StatisticalSummary sampleStats1, final StatisticalSummary sampleStats2) throws NullArgumentException, NumberIsTooSmallException, MaxCountExceededException {
    checkSampleData(sampleStats1);
    checkSampleData(sampleStats2);
    return homoscedasticTTest(sampleStats1.getMean(), sampleStats2.getMean(), sampleStats1.getVariance(), sampleStats2.getVariance(), sampleStats1.getN(), sampleStats2.getN());
  }
  public double pairedT(final double[] sample1, final double[] sample2) throws NullArgumentException, NoDataException, DimensionMismatchException, NumberIsTooSmallException {
    checkSampleData(sample1);
    checkSampleData(sample2);
    double meanDifference = StatUtils.meanDifference(sample1, sample2);
    return t(meanDifference, 0, StatUtils.varianceDifference(sample1, sample2, meanDifference), sample1.length);
  }
  public double pairedTTest(final double[] sample1, final double[] sample2) throws NullArgumentException, NoDataException, DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException {
    double meanDifference = StatUtils.meanDifference(sample1, sample2);
    return tTest(meanDifference, 0, StatUtils.varianceDifference(sample1, sample2, meanDifference), sample1.length);
  }
  protected double t(final double m, final double mu, final double v, final double n) {
    return (m - mu) / FastMath.sqrt(v / n);
  }
  protected double t(final double m1, final double m2, final double v1, final double v2, final double n1, final double n2) {
    return (m1 - m2) / FastMath.sqrt((v1 / n1) + (v2 / n2));
  }
  public double t(final double mu, final double[] observed) throws NullArgumentException, NumberIsTooSmallException {
    checkSampleData(observed);
    return t(StatUtils.mean(observed), mu, StatUtils.variance(observed), observed.length);
  }
  public double t(final double mu, final StatisticalSummary sampleStats) throws NullArgumentException, NumberIsTooSmallException {
    checkSampleData(sampleStats);
    return t(sampleStats.getMean(), mu, sampleStats.getVariance(), sampleStats.getN());
  }
  public double t(final double[] sample1, final double[] sample2) throws NullArgumentException, NumberIsTooSmallException {
    checkSampleData(sample1);
    checkSampleData(sample2);
    return t(StatUtils.mean(sample1), StatUtils.mean(sample2), StatUtils.variance(sample1), StatUtils.variance(sample2), sample1.length, sample2.length);
  }
  public double t(final StatisticalSummary sampleStats1, final StatisticalSummary sampleStats2) throws NullArgumentException, NumberIsTooSmallException {
    checkSampleData(sampleStats1);
    checkSampleData(sampleStats2);
    return t(sampleStats1.getMean(), sampleStats2.getMean(), sampleStats1.getVariance(), sampleStats2.getVariance(), sampleStats1.getN(), sampleStats2.getN());
  }
  protected double tTest(final double m, final double mu, final double v, final double n) throws MaxCountExceededException, MathIllegalArgumentException {
    double t = FastMath.abs(t(m, mu, v, n));
    TDistribution distribution = new TDistribution(n - 1);
    return 2.0D * distribution.cumulativeProbability(-t);
  }
  protected double tTest(final double m1, final double m2, final double v1, final double v2, final double n1, final double n2) throws MaxCountExceededException, NotStrictlyPositiveException {
    final double t = FastMath.abs(t(m1, m2, v1, v2, n1, n2));
    final double degreesOfFreedom = df(v1, v2, n1, n2);
    TDistribution distribution = new TDistribution(degreesOfFreedom);
    return 2.0D * distribution.cumulativeProbability(-t);
  }
  public double tTest(final double mu, final double[] sample) throws NullArgumentException, NumberIsTooSmallException, MaxCountExceededException {
    checkSampleData(sample);
    return tTest(StatUtils.mean(sample), mu, StatUtils.variance(sample), sample.length);
  }
  public double tTest(final double mu, final StatisticalSummary sampleStats) throws NullArgumentException, NumberIsTooSmallException, MaxCountExceededException {
    checkSampleData(sampleStats);
    return tTest(sampleStats.getMean(), mu, sampleStats.getVariance(), sampleStats.getN());
  }
  public double tTest(final double[] sample1, final double[] sample2) throws NullArgumentException, NumberIsTooSmallException, MaxCountExceededException {
    checkSampleData(sample1);
    checkSampleData(sample2);
    return tTest(StatUtils.mean(sample1), StatUtils.mean(sample2), StatUtils.variance(sample1), StatUtils.variance(sample2), sample1.length, sample2.length);
  }
  public double tTest(final StatisticalSummary sampleStats1, final StatisticalSummary sampleStats2) throws NullArgumentException, NumberIsTooSmallException, MaxCountExceededException {
    checkSampleData(sampleStats1);
    checkSampleData(sampleStats2);
    return tTest(sampleStats1.getMean(), sampleStats2.getMean(), sampleStats1.getVariance(), sampleStats2.getVariance(), sampleStats1.getN(), sampleStats2.getN());
  }
  private void checkSampleData(final double[] data) throws NullArgumentException, NumberIsTooSmallException {
    if(data == null) {
      throw new NullArgumentException();
    }
    if(data.length < 2) {
      int var_3835 = data.length;
      throw new NumberIsTooSmallException(LocalizedFormats.INSUFFICIENT_DATA_FOR_T_STATISTIC, var_3835, 2, true);
    }
  }
  private void checkSampleData(final StatisticalSummary stat) throws NullArgumentException, NumberIsTooSmallException {
    if(stat == null) {
      throw new NullArgumentException();
    }
    if(stat.getN() < 2) {
      throw new NumberIsTooSmallException(LocalizedFormats.INSUFFICIENT_DATA_FOR_T_STATISTIC, stat.getN(), 2, true);
    }
  }
  private void checkSignificanceLevel(final double alpha) throws OutOfRangeException {
    if(alpha <= 0 || alpha > 0.5D) {
      throw new OutOfRangeException(LocalizedFormats.SIGNIFICANCE_LEVEL, alpha, 0.0D, 0.5D);
    }
  }
}