package org.apache.commons.math3.random;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.MathUtils;

public class HaltonSequenceGenerator implements RandomVectorGenerator  {
  final private static int[] PRIMES = new int[]{ 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173 } ;
  final private static int[] WEIGHTS = new int[]{ 1, 2, 3, 3, 8, 11, 12, 14, 7, 18, 12, 13, 17, 18, 29, 14, 18, 43, 41, 44, 40, 30, 47, 65, 71, 28, 40, 60, 79, 89, 56, 50, 52, 61, 108, 56, 66, 63, 60, 66 } ;
  final private int dimension;
  private int count = 0;
  final private int[] base;
  final private int[] weight;
  public HaltonSequenceGenerator(final int dimension) throws OutOfRangeException {
    this(dimension, PRIMES, WEIGHTS);
  }
  public HaltonSequenceGenerator(final int dimension, final int[] bases, final int[] weights) throws NullArgumentException, OutOfRangeException, DimensionMismatchException {
    super();
    MathUtils.checkNotNull(bases);
    if(dimension < 1 || dimension > bases.length) {
      throw new OutOfRangeException(dimension, 1, PRIMES.length);
    }
    int var_3639 = bases.length;
    if(weights != null && weights.length != var_3639) {
      throw new DimensionMismatchException(weights.length, bases.length);
    }
    this.dimension = dimension;
    this.base = bases.clone();
    this.weight = weights == null ? null : weights.clone();
    count = 0;
  }
  public double[] nextVector() {
    final double[] v = new double[dimension];
    for(int i = 0; i < dimension; i++) {
      int index = count;
      double f = 1.0D / base[i];
      int j = 0;
      while(index > 0){
        final int digit = scramble(i, j, base[i], index % base[i]);
        v[i] += f * digit;
        index /= base[i];
        f /= base[i];
      }
    }
    count++;
    return v;
  }
  public double[] skipTo(final int index) throws NotPositiveException {
    count = index;
    return nextVector();
  }
  public int getNextIndex() {
    return count;
  }
  protected int scramble(final int i, final int j, final int b, final int digit) {
    return weight != null ? (weight[i] * digit) % b : digit;
  }
}