package org.apache.commons.math3.random;

public class Well44497b extends AbstractWell  {
  final private static long serialVersionUID = 4032007538246675492L;
  final private static int K = 44497;
  final private static int M1 = 23;
  final private static int M2 = 481;
  final private static int M3 = 229;
  public Well44497b() {
    super(K, M1, M2, M3);
  }
  public Well44497b(int seed) {
    super(K, M1, M2, M3, seed);
  }
  public Well44497b(int[] seed) {
    super(K, M1, M2, M3, seed);
  }
  public Well44497b(long seed) {
    super(K, M1, M2, M3, seed);
  }
  @Override() protected int next(final int bits) {
    final int indexRm1 = iRm1[index];
    final int indexRm2 = iRm2[index];
    final int v0 = v[index];
    final int vM1 = v[i1[index]];
    final int vM2 = v[i2[index]];
    final int vM3 = v[i3[index]];
    int var_3610 = v[indexRm1];
    final int z0 = (0xFFFF8000 & var_3610) ^ (0x00007FFF & v[indexRm2]);
    final int z1 = (v0 ^ (v0 << 24)) ^ (vM1 ^ (vM1 >>> 30));
    final int z2 = (vM2 ^ (vM2 << 10)) ^ (vM3 << 26);
    final int z3 = z1 ^ z2;
    final int z2Prime = ((z2 << 9) ^ (z2 >>> 23)) & 0xfbffffff;
    final int z2Second = ((z2 & 0x00020000) != 0) ? (z2Prime ^ 0xb729fcec) : z2Prime;
    int z4 = z0 ^ (z1 ^ (z1 >>> 20)) ^ z2Second ^ z3;
    v[index] = z3;
    v[indexRm1] = z4;
    v[indexRm2] &= 0xFFFF8000;
    index = indexRm1;
    z4 = z4 ^ ((z4 << 7) & 0x93dd1400);
    z4 = z4 ^ ((z4 << 15) & 0xfa118000);
    return z4 >>> (32 - bits);
  }
}