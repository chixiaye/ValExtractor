package org.apache.commons.math3.random;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.util.FastMath;

abstract public class AbstractRandomGenerator implements RandomGenerator  {
  private double cachedNormalDeviate = Double.NaN;
  public AbstractRandomGenerator() {
    super();
  }
  public boolean nextBoolean() {
    return nextDouble() <= 0.5D;
  }
  abstract public double nextDouble();
  public double nextGaussian() {
    if(!Double.isNaN(cachedNormalDeviate)) {
      double dev = cachedNormalDeviate;
      cachedNormalDeviate = Double.NaN;
      return dev;
    }
    double v1 = 0;
    double v2 = 0;
    double s = 1;
    while(s >= 1){
      v1 = 2 * nextDouble() - 1;
      double var_3597 = nextDouble();
      v2 = 2 * var_3597 - 1;
      s = v1 * v1 + v2 * v2;
    }
    if(s != 0) {
      s = FastMath.sqrt(-2 * FastMath.log(s) / s);
    }
    cachedNormalDeviate = v2 * s;
    return v1 * s;
  }
  public float nextFloat() {
    return (float)nextDouble();
  }
  public int nextInt() {
    return (int)((2D * nextDouble() - 1D) * Integer.MAX_VALUE);
  }
  public int nextInt(int n) {
    if(n <= 0) {
      throw new NotStrictlyPositiveException(n);
    }
    int result = (int)(nextDouble() * n);
    return result < n ? result : n - 1;
  }
  public long nextLong() {
    return (long)((2D * nextDouble() - 1D) * Long.MAX_VALUE);
  }
  public void clear() {
    cachedNormalDeviate = Double.NaN;
  }
  public void nextBytes(byte[] bytes) {
    int bytesOut = 0;
    while(bytesOut < bytes.length){
      int randInt = nextInt();
      for(int i = 0; i < 3; i++) {
        if(i > 0) {
          randInt = randInt >> 8;
        }
        bytes[bytesOut++] = (byte)randInt;
        if(bytesOut == bytes.length) {
          return ;
        }
      }
    }
  }
  public void setSeed(int seed) {
    setSeed((long)seed);
  }
  public void setSeed(int[] seed) {
    final long prime = 4294967291L;
    long combined = 0L;
    for (int s : seed) {
      combined = combined * prime + s;
    }
    setSeed(combined);
  }
  abstract public void setSeed(long seed);
}