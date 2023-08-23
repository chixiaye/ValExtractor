package org.apache.commons.math3.util;
import java.math.BigInteger;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.util.Localizable;
import org.apache.commons.math3.exception.util.LocalizedFormats;

final public class ArithmeticUtils  {
  private ArithmeticUtils() {
    super();
  }
  public static BigInteger pow(final BigInteger k, int e) throws NotPositiveException {
    if(e < 0) {
      throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
    }
    return k.pow(e);
  }
  public static BigInteger pow(final BigInteger k, BigInteger e) throws NotPositiveException {
    if(e.compareTo(BigInteger.ZERO) < 0) {
      throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
    }
    BigInteger result = BigInteger.ONE;
    BigInteger k2p = k;
    while(!BigInteger.ZERO.equals(e)){
      if(e.testBit(0)) {
        result = result.multiply(k2p);
      }
      k2p = k2p.multiply(k2p);
      e = e.shiftRight(1);
    }
    return result;
  }
  public static BigInteger pow(final BigInteger k, long e) throws NotPositiveException {
    if(e < 0) {
      throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
    }
    BigInteger result = BigInteger.ONE;
    BigInteger k2p = k;
    while(e != 0){
      if((e & 0x1) != 0) {
        result = result.multiply(k2p);
      }
      k2p = k2p.multiply(k2p);
      e = e >> 1;
    }
    return result;
  }
  public static boolean isPowerOfTwo(long n) {
    return (n > 0) && ((n & (n - 1)) == 0);
  }
  public static double binomialCoefficientDouble(final int n, final int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
    return CombinatoricsUtils.binomialCoefficientDouble(n, k);
  }
  public static double binomialCoefficientLog(final int n, final int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
    return CombinatoricsUtils.binomialCoefficientLog(n, k);
  }
  public static double factorialDouble(final int n) throws NotPositiveException {
    return CombinatoricsUtils.factorialDouble(n);
  }
  public static double factorialLog(final int n) throws NotPositiveException {
    return CombinatoricsUtils.factorialLog(n);
  }
  public static int addAndCheck(int x, int y) throws MathArithmeticException {
    long s = (long)x + (long)y;
    if(s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
      throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, x, y);
    }
    return (int)s;
  }
  public static int gcd(int p, int q) throws MathArithmeticException {
    int a = p;
    int b = q;
    if(a == 0 || b == 0) {
      if(a == Integer.MIN_VALUE || b == Integer.MIN_VALUE) {
        throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_32_BITS, p, q);
      }
      return FastMath.abs(a + b);
    }
    long al = a;
    long bl = b;
    boolean useLong = false;
    if(a < 0) {
      if(Integer.MIN_VALUE == a) {
        useLong = true;
      }
      else {
        a = -a;
      }
      al = -al;
    }
    if(b < 0) {
      if(Integer.MIN_VALUE == b) {
        useLong = true;
      }
      else {
        b = -b;
      }
      bl = -bl;
    }
    if(useLong) {
      if(al == bl) {
        throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_32_BITS, p, q);
      }
      long blbu = bl;
      bl = al;
      al = blbu % al;
      if(al == 0) {
        if(bl > Integer.MAX_VALUE) {
          throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_32_BITS, p, q);
        }
        return (int)bl;
      }
      blbu = bl;
      b = (int)al;
      a = (int)(blbu % al);
    }
    return gcdPositive(a, b);
  }
  private static int gcdPositive(int a, int b) {
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
    final int shift = Math.min(aTwos, bTwos);
    while(a != b){
      final int delta = a - b;
      b = Math.min(a, b);
      a = Math.abs(delta);
      int var_4137 = Integer.numberOfTrailingZeros(a);
      a >>= var_4137;
    }
    return a << shift;
  }
  public static int lcm(int a, int b) throws MathArithmeticException {
    if(a == 0 || b == 0) {
      return 0;
    }
    int lcm = FastMath.abs(ArithmeticUtils.mulAndCheck(a / gcd(a, b), b));
    if(lcm == Integer.MIN_VALUE) {
      throw new MathArithmeticException(LocalizedFormats.LCM_OVERFLOW_32_BITS, a, b);
    }
    return lcm;
  }
  public static int mulAndCheck(int x, int y) throws MathArithmeticException {
    long m = ((long)x) * ((long)y);
    if(m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
      throw new MathArithmeticException();
    }
    return (int)m;
  }
  public static int pow(final int k, int e) throws NotPositiveException {
    if(e < 0) {
      throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
    }
    int result = 1;
    int k2p = k;
    while(e != 0){
      if((e & 0x1) != 0) {
        result *= k2p;
      }
      k2p *= k2p;
      e = e >> 1;
    }
    return result;
  }
  public static int pow(final int k, long e) throws NotPositiveException {
    if(e < 0) {
      throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
    }
    int result = 1;
    int k2p = k;
    while(e != 0){
      if((e & 0x1) != 0) {
        result *= k2p;
      }
      k2p *= k2p;
      e = e >> 1;
    }
    return result;
  }
  public static int subAndCheck(int x, int y) throws MathArithmeticException {
    long s = (long)x - (long)y;
    if(s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
      throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_SUBTRACTION, x, y);
    }
    return (int)s;
  }
  public static long addAndCheck(long a, long b) throws MathArithmeticException {
    return ArithmeticUtils.addAndCheck(a, b, LocalizedFormats.OVERFLOW_IN_ADDITION);
  }
  private static long addAndCheck(long a, long b, Localizable pattern) throws MathArithmeticException {
    long ret;
    if(a > b) {
      ret = addAndCheck(b, a, pattern);
    }
    else {
      if(a < 0) {
        if(b < 0) {
          if(Long.MIN_VALUE - b <= a) {
            ret = a + b;
          }
          else {
            throw new MathArithmeticException(pattern, a, b);
          }
        }
        else {
          ret = a + b;
        }
      }
      else {
        if(a <= Long.MAX_VALUE - b) {
          ret = a + b;
        }
        else {
          throw new MathArithmeticException(pattern, a, b);
        }
      }
    }
    return ret;
  }
  public static long binomialCoefficient(final int n, final int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
    return CombinatoricsUtils.binomialCoefficient(n, k);
  }
  public static long factorial(final int n) throws NotPositiveException, MathArithmeticException {
    return CombinatoricsUtils.factorial(n);
  }
  public static long gcd(final long p, final long q) throws MathArithmeticException {
    long u = p;
    long v = q;
    if((u == 0) || (v == 0)) {
      if((u == Long.MIN_VALUE) || (v == Long.MIN_VALUE)) {
        throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_64_BITS, p, q);
      }
      return FastMath.abs(u) + FastMath.abs(v);
    }
    if(u > 0) {
      u = -u;
    }
    if(v > 0) {
      v = -v;
    }
    int k = 0;
    while((u & 1) == 0 && (v & 1) == 0 && k < 63){
      u /= 2;
      v /= 2;
      k++;
    }
    if(k == 63) {
      throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_64_BITS, p, q);
    }
    long t = ((u & 1) == 1) ? v : -(u / 2);
    do {
      while((t & 1) == 0){
        t /= 2;
      }
      if(t > 0) {
        u = -t;
      }
      else {
        v = t;
      }
      t = (v - u) / 2;
    }while(t != 0);
    return -u * (1L << k);
  }
  public static long lcm(long a, long b) throws MathArithmeticException {
    if(a == 0 || b == 0) {
      return 0;
    }
    long lcm = FastMath.abs(ArithmeticUtils.mulAndCheck(a / gcd(a, b), b));
    if(lcm == Long.MIN_VALUE) {
      throw new MathArithmeticException(LocalizedFormats.LCM_OVERFLOW_64_BITS, a, b);
    }
    return lcm;
  }
  public static long mulAndCheck(long a, long b) throws MathArithmeticException {
    long ret;
    if(a > b) {
      ret = mulAndCheck(b, a);
    }
    else {
      if(a < 0) {
        if(b < 0) {
          if(a >= Long.MAX_VALUE / b) {
            ret = a * b;
          }
          else {
            throw new MathArithmeticException();
          }
        }
        else 
          if(b > 0) {
            if(Long.MIN_VALUE / b <= a) {
              ret = a * b;
            }
            else {
              throw new MathArithmeticException();
            }
          }
          else {
            ret = 0;
          }
      }
      else 
        if(a > 0) {
          if(a <= Long.MAX_VALUE / b) {
            ret = a * b;
          }
          else {
            throw new MathArithmeticException();
          }
        }
        else {
          ret = 0;
        }
    }
    return ret;
  }
  public static long pow(final long k, int e) throws NotPositiveException {
    if(e < 0) {
      throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
    }
    long result = 1L;
    long k2p = k;
    while(e != 0){
      if((e & 0x1) != 0) {
        result *= k2p;
      }
      k2p *= k2p;
      e = e >> 1;
    }
    return result;
  }
  public static long pow(final long k, long e) throws NotPositiveException {
    if(e < 0) {
      throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
    }
    long result = 1L;
    long k2p = k;
    while(e != 0){
      if((e & 0x1) != 0) {
        result *= k2p;
      }
      k2p *= k2p;
      e = e >> 1;
    }
    return result;
  }
  public static long stirlingS2(final int n, final int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
    return CombinatoricsUtils.stirlingS2(n, k);
  }
  public static long subAndCheck(long a, long b) throws MathArithmeticException {
    long ret;
    if(b == Long.MIN_VALUE) {
      if(a < 0) {
        ret = a - b;
      }
      else {
        throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, a, -b);
      }
    }
    else {
      ret = addAndCheck(a, -b, LocalizedFormats.OVERFLOW_IN_ADDITION);
    }
    return ret;
  }
}