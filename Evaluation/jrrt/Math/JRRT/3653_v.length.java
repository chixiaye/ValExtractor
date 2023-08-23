package org.apache.commons.math3.random;
import java.io.Serializable;

abstract public class AbstractWell extends BitsStreamGenerator implements Serializable  {
  final private static long serialVersionUID = -817701723016583596L;
  protected int index;
  final protected int[] v;
  final protected int[] iRm1;
  final protected int[] iRm2;
  final protected int[] i1;
  final protected int[] i2;
  final protected int[] i3;
  protected AbstractWell(final int k, final int m1, final int m2, final int m3) {
    this(k, m1, m2, m3, null);
  }
  protected AbstractWell(final int k, final int m1, final int m2, final int m3, final int seed) {
    this(k, m1, m2, m3, new int[]{ seed } );
  }
  protected AbstractWell(final int k, final int m1, final int m2, final int m3, final int[] seed) {
    super();
    final int w = 32;
    final int r = (k + w - 1) / w;
    this.v = new int[r];
    this.index = 0;
    iRm1 = new int[r];
    iRm2 = new int[r];
    i1 = new int[r];
    i2 = new int[r];
    i3 = new int[r];
    for(int j = 0; j < r; ++j) {
      iRm1[j] = (j + r - 1) % r;
      iRm2[j] = (j + r - 2) % r;
      i1[j] = (j + m1) % r;
      i2[j] = (j + m2) % r;
      i3[j] = (j + m3) % r;
    }
    setSeed(seed);
  }
  protected AbstractWell(final int k, final int m1, final int m2, final int m3, final long seed) {
    this(k, m1, m2, m3, new int[]{ (int)(seed >>> 32), (int)(seed & 0xffffffffL) } );
  }
  abstract @Override() protected int next(final int bits);
  @Override() public void setSeed(final int seed) {
    setSeed(new int[]{ seed } );
  }
  @Override() public void setSeed(final int[] seed) {
    if(seed == null) {
      setSeed(System.currentTimeMillis() + System.identityHashCode(this));
      return ;
    }
    System.arraycopy(seed, 0, v, 0, Math.min(seed.length, v.length));
    int var_3653 = v.length;
    if(seed.length < var_3653) {
      for(int i = seed.length; i < v.length; ++i) {
        final long l = v[i - seed.length];
        v[i] = (int)((1812433253L * (l ^ (l >> 30)) + i) & 0xffffffffL);
      }
    }
    index = 0;
    clear();
  }
  @Override() public void setSeed(final long seed) {
    setSeed(new int[]{ (int)(seed >>> 32), (int)(seed & 0xffffffffL) } );
  }
}