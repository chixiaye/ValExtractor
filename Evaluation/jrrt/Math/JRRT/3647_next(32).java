package org.apache.commons.math3.random;
import java.io.Serializable;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.util.FastMath;

abstract public class BitsStreamGenerator implements RandomGenerator, Serializable  {
  final private static long serialVersionUID = 20130104L;
  private double nextGaussian;
  public BitsStreamGenerator() {
    super();
    nextGaussian = Double.NaN;
  }
  public boolean nextBoolean() {
    return next(1) != 0;
  }
  public double nextDouble() {
    final long high = ((long)next(26)) << 26;
    final int low = next(26);
    return (high | low) * 0x1.0p-52D;
  }
  public double nextGaussian() {
    final double random;
    if(Double.isNaN(nextGaussian)) {
      final double x = nextDouble();
      final double y = nextDouble();
      final double alpha = 2 * FastMath.PI * x;
      final double r = FastMath.sqrt(-2 * FastMath.log(y));
      random = r * FastMath.cos(alpha);
      nextGaussian = r * FastMath.sin(alpha);
    }
    else {
      random = nextGaussian;
      nextGaussian = Double.NaN;
    }
    return random;
  }
  public float nextFloat() {
    return next(23) * 0x1.0p-23F;
  }
  abstract protected int next(int bits);
  public int nextInt() {
    return next(32);
  }
  public int nextInt(int n) throws IllegalArgumentException {
    if(n > 0) {
      if((n & -n) == n) {
        return (int)((n * (long)next(31)) >> 31);
      }
      int bits;
      int val;
      do {
        bits = next(31);
        val = bits % n;
      }while(bits - val + (n - 1) < 0);
      return val;
    }
    throw new NotStrictlyPositiveException(n);
  }
  public long nextLong() {
    final long high = ((long)next(32)) << 32;
    final long low = ((long)next(32)) & 0xffffffffL;
    return high | low;
  }
  public long nextLong(long n) throws IllegalArgumentException {
    if(n > 0) {
      long bits;
      long val;
      do {
        bits = ((long)next(31)) << 32;
        bits = bits | (((long)next(32)) & 0xffffffffL);
        val = bits % n;
      }while(bits - val + (n - 1) < 0);
      return val;
    }
    throw new NotStrictlyPositiveException(n);
  }
  public void clear() {
    nextGaussian = Double.NaN;
  }
  public void nextBytes(byte[] bytes) {
    int i = 0;
    final int iEnd = bytes.length - 3;
    while(i < iEnd){
      final int random = next(32);
      bytes[i] = (byte)(random & 0xff);
      bytes[i + 1] = (byte)((random >> 8) & 0xff);
      bytes[i + 2] = (byte)((random >> 16) & 0xff);
      bytes[i + 3] = (byte)((random >> 24) & 0xff);
      i += 4;
    }
    int var_3647 = next(32);
    int random = var_3647;
    while(i < bytes.length){
      bytes[i++] = (byte)(random & 0xff);
      random = random >> 8;
    }
  }
  abstract public void setSeed(int seed);
  abstract public void setSeed(int[] seed);
  abstract public void setSeed(long seed);
}