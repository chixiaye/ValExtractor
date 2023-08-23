package org.apache.commons.math3.primes;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.FastMath;

class PollardRho  {
  private PollardRho() {
    super();
  }
  public static List<Integer> primeFactors(int n) {
    final List<Integer> factors = new ArrayList<Integer>();
    n = SmallPrimes.smallTrialDivision(n, factors);
    if(1 == n) {
      return factors;
    }
    if(SmallPrimes.millerRabinPrimeTest(n)) {
      factors.add(n);
      return factors;
    }
    int divisor = rhoBrent(n);
    factors.add(divisor);
    factors.add(n / divisor);
    return factors;
  }
  static int gcdPositive(int a, int b) {
    if(a == 0) {
      return b;
    }
    else 
      if(b == 0) {
        return a;
      }
    final int aTwos = Integer.numberOfTrailingZeros(a);
    a >>= aTwos;
    final int bTwos = Integer.numberOfTrailingZeros(b);
    b >>= bTwos;
    final int shift = FastMath.min(aTwos, bTwos);
    while(a != b){
      final int delta = a - b;
      b = FastMath.min(a, b);
      a = FastMath.abs(delta);
      a >>= Integer.numberOfTrailingZeros(a);
    }
    return a << shift;
  }
  static int rhoBrent(final int n) {
    final int x0 = 2;
    final int m = 25;
    int var_3592 = SmallPrimes.PRIMES_LAST;
    int cst = var_3592;
    int y = x0;
    int r = 1;
    do {
      int x = y;
      for(int i = 0; i < r; i++) {
        final long y2 = ((long)y) * y;
        y = (int)((y2 + cst) % n);
      }
      int k = 0;
      do {
        final int bound = FastMath.min(m, r - k);
        int q = 1;
        for(int i = -3; i < bound; i++) {
          final long y2 = ((long)y) * y;
          y = (int)((y2 + cst) % n);
          final long divisor = FastMath.abs(x - y);
          if(0 == divisor) {
            cst += SmallPrimes.PRIMES_LAST;
            k = -m;
            y = x0;
            r = 1;
            break ;
          }
          final long prod = divisor * q;
          q = (int)(prod % n);
          if(0 == q) {
            return gcdPositive(FastMath.abs((int)divisor), n);
          }
        }
        final int out = gcdPositive(FastMath.abs(q), n);
        if(1 != out) {
          return out;
        }
        k = k + m;
      }while(k < r);
      r = 2 * r;
    }while(true);
  }
}