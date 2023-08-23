package org.apache.commons.math3.stat;
import java.util.List;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;

final public class StatUtils  {
  final private static UnivariateStatistic SUM = new Sum();
  final private static UnivariateStatistic SUM_OF_SQUARES = new SumOfSquares();
  final private static UnivariateStatistic PRODUCT = new Product();
  final private static UnivariateStatistic SUM_OF_LOGS = new SumOfLogs();
  final private static UnivariateStatistic MIN = new Min();
  final private static UnivariateStatistic MAX = new Max();
  final private static UnivariateStatistic MEAN = new Mean();
  final private static Variance VARIANCE = new Variance();
  final private static Percentile PERCENTILE = new Percentile();
  final private static GeometricMean GEOMETRIC_MEAN = new GeometricMean();
  private StatUtils() {
    super();
  }
  public static double geometricMean(final double[] values) throws MathIllegalArgumentException {
    return GEOMETRIC_MEAN.evaluate(values);
  }
  public static double geometricMean(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return GEOMETRIC_MEAN.evaluate(values, begin, length);
  }
  public static double max(final double[] values) throws MathIllegalArgumentException {
    return MAX.evaluate(values);
  }
  public static double max(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return MAX.evaluate(values, begin, length);
  }
  public static double mean(final double[] values) throws MathIllegalArgumentException {
    return MEAN.evaluate(values);
  }
  public static double mean(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return MEAN.evaluate(values, begin, length);
  }
  public static double meanDifference(final double[] sample1, final double[] sample2) throws DimensionMismatchException, NoDataException {
    return sumDifference(sample1, sample2) / sample1.length;
  }
  public static double min(final double[] values) throws MathIllegalArgumentException {
    return MIN.evaluate(values);
  }
  public static double min(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return MIN.evaluate(values, begin, length);
  }
  public static double percentile(final double[] values, final double p) throws MathIllegalArgumentException {
    return PERCENTILE.evaluate(values, p);
  }
  public static double percentile(final double[] values, final int begin, final int length, final double p) throws MathIllegalArgumentException {
    return PERCENTILE.evaluate(values, begin, length, p);
  }
  public static double populationVariance(final double[] values) throws MathIllegalArgumentException {
    return new Variance(false).evaluate(values);
  }
  public static double populationVariance(final double[] values, final double mean) throws MathIllegalArgumentException {
    return new Variance(false).evaluate(values, mean);
  }
  public static double populationVariance(final double[] values, final double mean, final int begin, final int length) throws MathIllegalArgumentException {
    return new Variance(false).evaluate(values, mean, begin, length);
  }
  public static double populationVariance(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return new Variance(false).evaluate(values, begin, length);
  }
  public static double product(final double[] values) throws MathIllegalArgumentException {
    return PRODUCT.evaluate(values);
  }
  public static double product(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return PRODUCT.evaluate(values, begin, length);
  }
  public static double sum(final double[] values) throws MathIllegalArgumentException {
    return SUM.evaluate(values);
  }
  public static double sum(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return SUM.evaluate(values, begin, length);
  }
  public static double sumDifference(final double[] sample1, final double[] sample2) throws DimensionMismatchException, NoDataException {
    int n = sample1.length;
    if(n != sample2.length) {
      throw new DimensionMismatchException(n, sample2.length);
    }
    if(n <= 0) {
      throw new NoDataException(LocalizedFormats.INSUFFICIENT_DIMENSION);
    }
    double result = 0;
    for(int i = 0; i < n; i++) {
      result += sample1[i] - sample2[i];
    }
    return result;
  }
  public static double sumLog(final double[] values) throws MathIllegalArgumentException {
    return SUM_OF_LOGS.evaluate(values);
  }
  public static double sumLog(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return SUM_OF_LOGS.evaluate(values, begin, length);
  }
  public static double sumSq(final double[] values) throws MathIllegalArgumentException {
    return SUM_OF_SQUARES.evaluate(values);
  }
  public static double sumSq(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return SUM_OF_SQUARES.evaluate(values, begin, length);
  }
  public static double variance(final double[] values) throws MathIllegalArgumentException {
    return VARIANCE.evaluate(values);
  }
  public static double variance(final double[] values, final double mean) throws MathIllegalArgumentException {
    return VARIANCE.evaluate(values, mean);
  }
  public static double variance(final double[] values, final double mean, final int begin, final int length) throws MathIllegalArgumentException {
    return VARIANCE.evaluate(values, mean, begin, length);
  }
  public static double variance(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    return VARIANCE.evaluate(values, begin, length);
  }
  public static double varianceDifference(final double[] sample1, final double[] sample2, double meanDifference) throws DimensionMismatchException, NumberIsTooSmallException {
    double sum1 = 0D;
    double sum2 = 0D;
    double diff = 0D;
    int n = sample1.length;
    if(n != sample2.length) {
      throw new DimensionMismatchException(n, sample2.length);
    }
    if(n < 2) {
      throw new NumberIsTooSmallException(n, 2, true);
    }
    for(int i = 0; i < n; i++) {
      diff = sample1[i] - sample2[i];
      sum1 += (diff - meanDifference) * (diff - meanDifference);
      sum2 += diff - meanDifference;
    }
    return (sum1 - (sum2 * sum2 / n)) / (n - 1);
  }
  private static double[] getMode(double[] values, final int begin, final int length) {
    Frequency freq = new Frequency();
    for(int i = begin; i < begin + length; i++) {
      final double value = values[i];
      if(!Double.isNaN(value)) {
        freq.addValue(Double.valueOf(value));
      }
    }
    List<Comparable<?>> list = freq.getMode();
    double[] modes = new double[list.size()];
    int i = 0;
    for (Comparable<?> c : list) {
      modes[i++] = ((Double)c).doubleValue();
    }
    return modes;
  }
  public static double[] mode(double[] sample) throws MathIllegalArgumentException {
    if(sample == null) {
      throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
    }
    return getMode(sample, 0, sample.length);
  }
  public static double[] mode(double[] sample, final int begin, final int length) {
    if(sample == null) {
      throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
    }
    if(begin < 0) {
      throw new NotPositiveException(LocalizedFormats.START_POSITION, Integer.valueOf(begin));
    }
    if(length < 0) {
      throw new NotPositiveException(LocalizedFormats.LENGTH, Integer.valueOf(length));
    }
    return getMode(sample, begin, length);
  }
  public static double[] normalize(final double[] sample) {
    DescriptiveStatistics stats = new DescriptiveStatistics();
    for(int i = 0; i < sample.length; i++) {
      double var_3698 = sample[i];
      stats.addValue(var_3698);
    }
    double mean = stats.getMean();
    double standardDeviation = stats.getStandardDeviation();
    double[] standardizedSample = new double[sample.length];
    for(int i = 0; i < sample.length; i++) {
      standardizedSample[i] = (sample[i] - mean) / standardDeviation;
    }
    return standardizedSample;
  }
}