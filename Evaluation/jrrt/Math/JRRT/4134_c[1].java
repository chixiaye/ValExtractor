package org.apache.commons.math3.util;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

final public class CombinatoricsUtils  {
  final static long[] FACTORIALS = new long[]{ 1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L, 3628800L, 39916800L, 479001600L, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L, 355687428096000L, 6402373705728000L, 121645100408832000L, 2432902008176640000L } ;
  final static AtomicReference<long[][]> STIRLING_S2 = new AtomicReference<long[][]>(null);
  private CombinatoricsUtils() {
    super();
  }
  public static Iterator<int[]> combinationsIterator(int n, int k) {
    checkBinomial(n, k);
    if(k == 0) {
      return new SingletonIterator(new int[]{  } );
    }
    if(k == n) {
      final int[] natural = new int[n];
      for(int i = 0; i < n; i++) {
        natural[i] = i;
      }
      return new SingletonIterator(natural);
    }
    return new LexicographicCombinationIterator(n, k);
  }
  public static double binomialCoefficientDouble(final int n, final int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
    CombinatoricsUtils.checkBinomial(n, k);
    if((n == k) || (k == 0)) {
      return 1D;
    }
    if((k == 1) || (k == n - 1)) {
      return n;
    }
    if(k > n / 2) {
      return binomialCoefficientDouble(n, n - k);
    }
    if(n < 67) {
      return binomialCoefficient(n, k);
    }
    double result = 1D;
    for(int i = 1; i <= k; i++) {
      result *= (double)(n - k + i) / (double)i;
    }
    return FastMath.floor(result + 0.5D);
  }
  public static double binomialCoefficientLog(final int n, final int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
    CombinatoricsUtils.checkBinomial(n, k);
    if((n == k) || (k == 0)) {
      return 0;
    }
    if((k == 1) || (k == n - 1)) {
      return FastMath.log(n);
    }
    if(n < 67) {
      return FastMath.log(binomialCoefficient(n, k));
    }
    if(n < 1030) {
      return FastMath.log(binomialCoefficientDouble(n, k));
    }
    if(k > n / 2) {
      return binomialCoefficientLog(n, n - k);
    }
    double logSum = 0;
    for(int i = n - k + 1; i <= n; i++) {
      logSum += FastMath.log(i);
    }
    for(int i = 2; i <= k; i++) {
      logSum -= FastMath.log(i);
    }
    return logSum;
  }
  public static double factorialDouble(final int n) throws NotPositiveException {
    if(n < 0) {
      throw new NotPositiveException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
    }
    if(n < 21) {
      return FACTORIALS[n];
    }
    return FastMath.floor(FastMath.exp(CombinatoricsUtils.factorialLog(n)) + 0.5D);
  }
  public static double factorialLog(final int n) throws NotPositiveException {
    if(n < 0) {
      throw new NotPositiveException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
    }
    if(n < 21) {
      return FastMath.log(FACTORIALS[n]);
    }
    double logSum = 0;
    for(int i = 2; i <= n; i++) {
      logSum += FastMath.log(i);
    }
    return logSum;
  }
  public static long binomialCoefficient(final int n, final int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
    CombinatoricsUtils.checkBinomial(n, k);
    if((n == k) || (k == 0)) {
      return 1;
    }
    if((k == 1) || (k == n - 1)) {
      return n;
    }
    if(k > n / 2) {
      return binomialCoefficient(n, n - k);
    }
    long result = 1;
    if(n <= 61) {
      int i = n - k + 1;
      for(int j = 1; j <= k; j++) {
        result = result * i / j;
        i++;
      }
    }
    else 
      if(n <= 66) {
        int i = n - k + 1;
        for(int j = 1; j <= k; j++) {
          final long d = ArithmeticUtils.gcd(i, j);
          result = (result / (j / d)) * (i / d);
          i++;
        }
      }
      else {
        int i = n - k + 1;
        for(int j = 1; j <= k; j++) {
          final long d = ArithmeticUtils.gcd(i, j);
          result = ArithmeticUtils.mulAndCheck(result / (j / d), i / d);
          i++;
        }
      }
    return result;
  }
  public static long factorial(final int n) throws NotPositiveException, MathArithmeticException {
    if(n < 0) {
      throw new NotPositiveException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
    }
    if(n > 20) {
      throw new MathArithmeticException();
    }
    return FACTORIALS[n];
  }
  public static long stirlingS2(final int n, final int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
    if(k < 0) {
      throw new NotPositiveException(k);
    }
    if(k > n) {
      throw new NumberIsTooLargeException(k, n, true);
    }
    long[][] stirlingS2 = STIRLING_S2.get();
    if(stirlingS2 == null) {
      final int maxIndex = 26;
      stirlingS2 = new long[maxIndex][];
      stirlingS2[0] = new long[]{ 1L } ;
      for(int i = 1; i < stirlingS2.length; ++i) {
        stirlingS2[i] = new long[i + 1];
        stirlingS2[i][0] = 0;
        stirlingS2[i][1] = 1;
        stirlingS2[i][i] = 1;
        for(int j = 2; j < i; ++j) {
          stirlingS2[i][j] = j * stirlingS2[i - 1][j] + stirlingS2[i - 1][j - 1];
        }
      }
      STIRLING_S2.compareAndSet(null, stirlingS2);
    }
    if(n < stirlingS2.length) {
      return stirlingS2[n][k];
    }
    else {
      if(k == 0) {
        return 0;
      }
      else 
        if(k == 1 || k == n) {
          return 1;
        }
        else 
          if(k == 2) {
            return (1L << (n - 1)) - 1L;
          }
          else 
            if(k == n - 1) {
              return binomialCoefficient(n, 2);
            }
            else {
              long sum = 0;
              long sign = ((k & 0x1) == 0) ? 1 : -1;
              for(int j = 1; j <= k; ++j) {
                sign = -sign;
                sum += sign * binomialCoefficient(k, j) * ArithmeticUtils.pow(j, n);
                if(sum < 0) {
                  throw new MathArithmeticException(LocalizedFormats.ARGUMENT_OUTSIDE_DOMAIN, n, 0, stirlingS2.length - 1);
                }
              }
              return sum / factorial(k);
            }
    }
  }
  private static void checkBinomial(final int n, final int k) throws NumberIsTooLargeException, NotPositiveException {
    if(n < k) {
      throw new NumberIsTooLargeException(LocalizedFormats.BINOMIAL_INVALID_PARAMETERS_ORDER, k, n, true);
    }
    if(n < 0) {
      throw new NotPositiveException(LocalizedFormats.BINOMIAL_NEGATIVE_PARAMETER, n);
    }
  }
  
  private static class LexicographicCombinationIterator implements Iterator<int[]>  {
    final private int k;
    final private int[] c;
    private boolean more = true;
    private int j;
    public LexicographicCombinationIterator(int n, int k) {
      super();
      this.k = k;
      c = new int[k + 3];
      if(k == 0 || k >= n) {
        more = false;
        return ;
      }
      for(int i = 1; i <= k; i++) {
        c[i] = i - 1;
      }
      c[k + 1] = n;
      c[k + 2] = 0;
      j = k;
    }
    public boolean hasNext() {
      return more;
    }
    public int[] next() {
      if(!more) {
        throw new NoSuchElementException();
      }
      final int[] ret = new int[k];
      System.arraycopy(c, 1, ret, 0, k);
      int x = 0;
      if(j > 0) {
        x = j;
        c[j] = x;
        j--;
        return ret;
      }
      int var_4134 = c[1];
      if(var_4134 + 1 < c[2]) {
        c[1] = c[1] + 1;
        return ret;
      }
      else {
        j = 2;
      }
      boolean stepDone = false;
      while(!stepDone){
        c[j - 1] = j - 2;
        x = c[j] + 1;
        if(x == c[j + 1]) {
          j++;
        }
        else {
          stepDone = true;
        }
      }
      if(j > k) {
        more = false;
        return ret;
      }
      c[j] = x;
      j--;
      return ret;
    }
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  
  private static class SingletonIterator implements Iterator<int[]>  {
    final private int[] singleton;
    private boolean more = true;
    public SingletonIterator(final int[] singleton) {
      super();
      this.singleton = singleton;
    }
    public boolean hasNext() {
      return more;
    }
    public int[] next() {
      if(more) {
        more = false;
        return singleton;
      }
      else {
        throw new NoSuchElementException();
      }
    }
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}