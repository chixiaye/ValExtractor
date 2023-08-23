package org.apache.commons.math3.stat.inference;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;

public class ChiSquareTest  {
  public ChiSquareTest() {
    super();
  }
  public boolean chiSquareTest(final double[] expected, final long[] observed, final double alpha) throws NotPositiveException, NotStrictlyPositiveException, DimensionMismatchException, OutOfRangeException, MaxCountExceededException {
    if((alpha <= 0) || (alpha > 0.5D)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0, 0.5D);
    }
    return chiSquareTest(expected, observed) < alpha;
  }
  public boolean chiSquareTest(final long[][] counts, final double alpha) throws NullArgumentException, DimensionMismatchException, NotPositiveException, OutOfRangeException, MaxCountExceededException {
    if((alpha <= 0) || (alpha > 0.5D)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0, 0.5D);
    }
    return chiSquareTest(counts) < alpha;
  }
  public boolean chiSquareTestDataSetsComparison(final long[] observed1, final long[] observed2, final double alpha) throws DimensionMismatchException, NotPositiveException, ZeroException, OutOfRangeException, MaxCountExceededException {
    if(alpha <= 0 || alpha > 0.5D) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0, 0.5D);
    }
    return chiSquareTestDataSetsComparison(observed1, observed2) < alpha;
  }
  public double chiSquare(final double[] expected, final long[] observed) throws NotPositiveException, NotStrictlyPositiveException, DimensionMismatchException {
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
    double ratio = 1.0D;
    boolean rescale = false;
    if(FastMath.abs(sumExpected - sumObserved) > 10E-6D) {
      ratio = sumObserved / sumExpected;
      rescale = true;
    }
    double sumSq = 0.0D;
    for(int i = 0; i < observed.length; i++) {
      if(rescale) {
        final double dev = observed[i] - ratio * expected[i];
        sumSq += dev * dev / (ratio * expected[i]);
      }
      else {
        final double dev = observed[i] - expected[i];
        sumSq += dev * dev / expected[i];
      }
    }
    return sumSq;
  }
  public double chiSquare(final long[][] counts) throws NullArgumentException, NotPositiveException, DimensionMismatchException {
    checkArray(counts);
    int nRows = counts.length;
    int nCols = counts[0].length;
    double[] rowSum = new double[nRows];
    double[] colSum = new double[nCols];
    double total = 0.0D;
    for(int row = 0; row < nRows; row++) {
      for(int col = 0; col < nCols; col++) {
        rowSum[row] += counts[row][col];
        colSum[col] += counts[row][col];
        total += counts[row][col];
      }
    }
    double sumSq = 0.0D;
    double expected = 0.0D;
    for(int row = 0; row < nRows; row++) {
      for(int col = 0; col < nCols; col++) {
        expected = (rowSum[row] * colSum[col]) / total;
        sumSq += ((counts[row][col] - expected) * (counts[row][col] - expected)) / expected;
      }
    }
    return sumSq;
  }
  public double chiSquareDataSetsComparison(long[] observed1, long[] observed2) throws DimensionMismatchException, NotPositiveException, ZeroException {
    if(observed1.length < 2) {
      throw new DimensionMismatchException(observed1.length, 2);
    }
    if(observed1.length != observed2.length) {
      throw new DimensionMismatchException(observed1.length, observed2.length);
    }
    MathArrays.checkNonNegative(observed1);
    MathArrays.checkNonNegative(observed2);
    long countSum1 = 0;
    long countSum2 = 0;
    boolean unequalCounts = false;
    double weight = 0.0D;
    for(int i = 0; i < observed1.length; i++) {
      countSum1 += observed1[i];
      countSum2 += observed2[i];
    }
    if(countSum1 == 0 || countSum2 == 0) {
      throw new ZeroException();
    }
    unequalCounts = countSum1 != countSum2;
    if(unequalCounts) {
      weight = FastMath.sqrt((double)countSum1 / (double)countSum2);
    }
    double sumSq = 0.0D;
    double dev = 0.0D;
    double obs1 = 0.0D;
    double obs2 = 0.0D;
    for(int i = 0; i < observed1.length; i++) {
      if(observed1[i] == 0 && observed2[i] == 0) {
        throw new ZeroException(LocalizedFormats.OBSERVED_COUNTS_BOTTH_ZERO_FOR_ENTRY, i);
      }
      else {
        obs1 = observed1[i];
        obs2 = observed2[i];
        if(unequalCounts) {
          dev = obs1 / weight - obs2 * weight;
        }
        else {
          dev = obs1 - obs2;
        }
        sumSq += (dev * dev) / (obs1 + obs2);
      }
    }
    return sumSq;
  }
  public double chiSquareTest(final double[] expected, final long[] observed) throws NotPositiveException, NotStrictlyPositiveException, DimensionMismatchException, MaxCountExceededException {
    ChiSquaredDistribution distribution = new ChiSquaredDistribution(expected.length - 1.0D);
    return 1.0D - distribution.cumulativeProbability(chiSquare(expected, observed));
  }
  public double chiSquareTest(final long[][] counts) throws NullArgumentException, DimensionMismatchException, NotPositiveException, MaxCountExceededException {
    checkArray(counts);
    double df = ((double)counts.length - 1) * ((double)counts[0].length - 1);
    ChiSquaredDistribution distribution;
    distribution = new ChiSquaredDistribution(df);
    return 1 - distribution.cumulativeProbability(chiSquare(counts));
  }
  public double chiSquareTestDataSetsComparison(long[] observed1, long[] observed2) throws DimensionMismatchException, NotPositiveException, ZeroException, MaxCountExceededException {
    ChiSquaredDistribution distribution;
    distribution = new ChiSquaredDistribution((double)observed1.length - 1);
    return 1 - distribution.cumulativeProbability(chiSquareDataSetsComparison(observed1, observed2));
  }
  private void checkArray(final long[][] in) throws NullArgumentException, DimensionMismatchException, NotPositiveException {
    if(in.length < 2) {
      int var_3858 = in.length;
      throw new DimensionMismatchException(var_3858, 2);
    }
    if(in[0].length < 2) {
      throw new DimensionMismatchException(in[0].length, 2);
    }
    MathArrays.checkRectangular(in);
    MathArrays.checkNonNegative(in);
  }
}