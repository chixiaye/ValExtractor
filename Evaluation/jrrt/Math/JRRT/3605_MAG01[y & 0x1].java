package org.apache.commons.math3.random;
import java.io.Serializable;
import org.apache.commons.math3.util.FastMath;

public class MersenneTwister extends BitsStreamGenerator implements Serializable  {
  final private static long serialVersionUID = 8661194735290153518L;
  final private static int N = 624;
  final private static int M = 397;
  final private static int[] MAG01 = { 0x0, 0x9908b0df } ;
  private int[] mt;
  private int mti;
  public MersenneTwister() {
    super();
    mt = new int[N];
    setSeed(System.currentTimeMillis() + System.identityHashCode(this));
  }
  public MersenneTwister(int seed) {
    super();
    mt = new int[N];
    setSeed(seed);
  }
  public MersenneTwister(int[] seed) {
    super();
    mt = new int[N];
    setSeed(seed);
  }
  public MersenneTwister(long seed) {
    super();
    mt = new int[N];
    setSeed(seed);
  }
  @Override() protected int next(int bits) {
    int y;
    if(mti >= N) {
      int mtNext = mt[0];
      for(int k = 0; k < N - M; ++k) {
        int mtCurr = mtNext;
        mtNext = mt[k + 1];
        y = (mtCurr & 0x80000000) | (mtNext & 0x7fffffff);
        int var_3605 = MAG01[y & 0x1];
        mt[k] = mt[k + M] ^ (y >>> 1) ^ var_3605;
      }
      for(int k = N - M; k < N - 1; ++k) {
        int mtCurr = mtNext;
        mtNext = mt[k + 1];
        y = (mtCurr & 0x80000000) | (mtNext & 0x7fffffff);
        mt[k] = mt[k + (M - N)] ^ (y >>> 1) ^ MAG01[y & 0x1];
      }
      y = (mtNext & 0x80000000) | (mt[0] & 0x7fffffff);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ MAG01[y & 0x1];
      mti = 0;
    }
    y = mt[mti++];
    y ^= y >>> 11;
    y ^= (y << 7) & 0x9d2c5680;
    y ^= (y << 15) & 0xefc60000;
    y ^= y >>> 18;
    return y >>> (32 - bits);
  }
  @Override() public void setSeed(int seed) {
    long longMT = seed;
    mt[0] = (int)longMT;
    for(mti = 1; mti < N; ++mti) {
      longMT = (1812433253L * (longMT ^ (longMT >> 30)) + mti) & 0xffffffffL;
      mt[mti] = (int)longMT;
    }
    clear();
  }
  @Override() public void setSeed(int[] seed) {
    if(seed == null) {
      setSeed(System.currentTimeMillis() + System.identityHashCode(this));
      return ;
    }
    setSeed(19650218);
    int i = 1;
    int j = 0;
    for(int k = FastMath.max(N, seed.length); k != 0; k--) {
      long l0 = (mt[i] & 0x7fffffffL) | ((mt[i] < 0) ? 0x80000000L : 0x0L);
      long l1 = (mt[i - 1] & 0x7fffffffL) | ((mt[i - 1] < 0) ? 0x80000000L : 0x0L);
      long l = (l0 ^ ((l1 ^ (l1 >> 30)) * 1664525L)) + seed[j] + j;
      mt[i] = (int)(l & 0xffffffffL);
      i++;
      j++;
      if(i >= N) {
        mt[0] = mt[N - 1];
        i = 1;
      }
      if(j >= seed.length) {
        j = 0;
      }
    }
    for(int k = N - 1; k != 0; k--) {
      long l0 = (mt[i] & 0x7fffffffL) | ((mt[i] < 0) ? 0x80000000L : 0x0L);
      long l1 = (mt[i - 1] & 0x7fffffffL) | ((mt[i - 1] < 0) ? 0x80000000L : 0x0L);
      long l = (l0 ^ ((l1 ^ (l1 >> 30)) * 1566083941L)) - i;
      mt[i] = (int)(l & 0xffffffffL);
      i++;
      if(i >= N) {
        mt[0] = mt[N - 1];
        i = 1;
      }
    }
    mt[0] = 0x80000000;
    clear();
  }
  @Override() public void setSeed(long seed) {
    setSeed(new int[]{ (int)(seed >>> 32), (int)(seed & 0xffffffffL) } );
  }
}