package org.apache.commons.math3.stat.inference;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;

public class GTest  {
  public boolean gTest(final double[] expected, final long[] observed, final double alpha) throws NotPositiveException, NotStrictlyPositiveException, DimensionMismatchException, OutOfRangeException, MaxCountExceededException {
    if((alpha <= 0) || (alpha > 0.5D)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0, 0.5D);
    }
    return gTest(expected, observed) < alpha;
  }
  public boolean gTestDataSetsComparison(final long[] observed1, final long[] observed2, final double alpha) throws DimensionMismatchException, NotPositiveException, ZeroException, OutOfRangeException, MaxCountExceededException {
    if(alpha <= 0 || alpha > 0.5D) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0, 0.5D);
    }
    return gTestDataSetsComparison(observed1, observed2) < alpha;
  }
  private double entropy(final long[] k) {
    double h = 0D;
    double sum_k = 0D;
    for(int i = 0; i < k.length; i++) {
      sum_k += (double)k[i];
    }
    for(int i = 0; i < k.length; i++) {
      if(k[i] != 0) {
        final double p_i = (double)k[i] / sum_k;
        h += p_i * Math.log(p_i);
      }
    }
    return -h;
  }
  private double entropy(final long[][] k) {
    double h = 0D;
    double sum_k = 0D;
    for(int i = 0; i < k.length; i++) {
      for(int j = 0; j < k[i].length; j++) {
        sum_k += (double)k[i][j];
      }
    }
    for(int i = 0; i < k.length; i++) {
      for(int j = 0; j < k[i].length; j++) {
        if(k[i][j] != 0) {
          final double p_ij = (double)k[i][j] / sum_k;
          h += p_ij * Math.log(p_ij);
        }
      }
    }
    return -h;
  }
  public double g(final double[] expected, final long[] observed) throws NotPositiveException, NotStrictlyPositiveException, DimensionMismatchException {
    if(expected.length < 2) {
      throw new DimensionMismatchException(expected.length, 2);
    }
    if(expected.length != observed.length) {
      throw new DimensionMismatchException(expected.length, observed.length);
    }
    MathArrays.checkPositive(expected);
    MathArrays.checkNonNegative(observed);
    double sumExpected = 0D;
    double sumObserved = 0D;
    for(int i = 0; i < observed.length; i++) {
      sumExpected += expected[i];
      sumObserved += observed[i];
    }
    double ratio = 1D;
    boolean rescale = false;
    if(Math.abs(sumExpected - sumObserved) > 10E-6D) {
      ratio = sumObserved / sumExpected;
      rescale = true;
    }
    double sum = 0D;
    for(int i = 0; i < observed.length; i++) {
      final double dev = rescale ? FastMath.log((double)observed[i] / (ratio * expected[i])) : FastMath.log((double)observed[i] / expected[i]);
      sum += ((double)observed[i]) * dev;
    }
    return 2D * sum;
  }
  public double gDataSetsComparison(final long[] observed1, final long[] observed2) throws DimensionMismatchException, NotPositiveException, ZeroException {
    if(observed1.length < 2) {
      throw new DimensionMismatchException(observed1.length, 2);
    }
    if(observed1.length != observed2.length) {
      int var_3834 = observed2.length;
      throw new DimensionMismatchException(observed1.length, var_3834);
    }
    MathArrays.checkNonNegative(observed1);
    MathArrays.checkNonNegative(observed2);
    long countSum1 = 0;
    long countSum2 = 0;
    final long[] collSums = new long[observed1.length];
    final long[][] k = new long[2][observed1.length];
    for(int i = 0; i < observed1.length; i++) {
      if(observed1[i] == 0 && observed2[i] == 0) {
        throw new ZeroException(LocalizedFormats.OBSERVED_COUNTS_BOTTH_ZERO_FOR_ENTRY, i);
      }
      else {
        countSum1 += observed1[i];
        countSum2 += observed2[i];
        collSums[i] = observed1[i] + observed2[i];
        k[0][i] = observed1[i];
        k[1][i] = observed2[i];
      }
    }
    if(countSum1 == 0 || countSum2 == 0) {
      throw new ZeroException();
    }
    final long[] rowSums = { countSum1, countSum2 } ;
    final double sum = (double)countSum1 + (double)countSum2;
    return 2 * sum * (entropy(rowSums) + entropy(collSums) - entropy(k));
  }
  public double gTest(final double[] expected, final long[] observed) throws NotPositiveException, NotStrictlyPositiveException, DimensionMismatchException, MaxCountExceededException {
    final ChiSquaredDistribution distribution = new ChiSquaredDistribution(expected.length - 1.0D);
    return 1.0D - distribution.cumulativeProbability(g(expected, observed));
  }
  public double gTestDataSetsComparison(final long[] observed1, final long[] observed2) throws DimensionMismatchException, NotPositiveException, ZeroException, MaxCountExceededException {
    final ChiSquaredDistribution distribution = new ChiSquaredDistribution((double)observed1.length - 1);
    return 1 - distribution.cumulativeProbability(gDataSetsComparison(observed1, observed2));
  }
  public double gTestIntrinsic(final double[] expected, final long[] observed) throws NotPositiveException, NotStrictlyPositiveException, DimensionMismatchException, MaxCountExceededException {
    final ChiSquaredDistribution distribution = new ChiSquaredDistribution(expected.length - 2.0D);
    return 1.0D - distribution.cumulativeProbability(g(expected, observed));
  }
  public double rootLogLikelihoodRatio(final long k11, long k12, final long k21, final long k22) {
    final double llr = gDataSetsComparison(new long[]{ k11, k12 } , new long[]{ k21, k22 } );
    double sqrt = FastMath.sqrt(llr);
    if((double)k11 / (k11 + k12) < (double)k21 / (k21 + k22)) {
      sqrt = -sqrt;
    }
    return sqrt;
  }
}