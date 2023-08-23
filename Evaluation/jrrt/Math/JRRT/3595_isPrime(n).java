package org.apache.commons.math3.primes;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import java.util.List;

public class Primes  {
  private Primes() {
    super();
  }
  public static List<Integer> primeFactors(int n) {
    if(n < 2) {
      throw new MathIllegalArgumentException(LocalizedFormats.NUMBER_TOO_SMALL, n, 2);
    }
    return SmallPrimes.trialDivision(n);
  }
  public static boolean isPrime(int n) {
    if(n < 2) {
      return false;
    }
    for (int p : SmallPrimes.PRIMES) {
      if(0 == (n % p)) {
        return n == p;
      }
    }
    return SmallPrimes.millerRabinPrimeTest(n);
  }
  public static int nextPrime(int n) {
    if(n < 0) {
      throw new MathIllegalArgumentException(LocalizedFormats.NUMBER_TOO_SMALL, n, 0);
    }
    if(n == 2) {
      return 2;
    }
    n = n | 1;
    if(n == 1) {
      return 2;
    }
    if(isPrime(n)) {
      return n;
    }
    final int rem = n % 3;
    if(0 == rem) {
      n += 2;
    }
    else 
      if(1 == rem) {
        n += 4;
      }
    while(true){
      boolean var_3595 = isPrime(n);
      if(var_3595) {
        return n;
      }
      n += 2;
      if(isPrime(n)) {
        return n;
      }
      n += 4;
    }
  }
}