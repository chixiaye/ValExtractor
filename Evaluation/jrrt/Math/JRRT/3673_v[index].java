package org.apache.commons.math3.random;

public class Well512a extends AbstractWell  {
  final private static long serialVersionUID = -6104179812103820574L;
  final private static int K = 512;
  final private static int M1 = 13;
  final private static int M2 = 9;
  final private static int M3 = 5;
  public Well512a() {
    super(K, M1, M2, M3);
  }
  public Well512a(int seed) {
    super(K, M1, M2, M3, seed);
  }
  public Well512a(int[] seed) {
    super(K, M1, M2, M3, seed);
  }
  public Well512a(long seed) {
    super(K, M1, M2, M3, seed);
  }
  @Override() protected int next(final int bits) {
    final int indexRm1 = iRm1[index];
    int var_3673 = v[index];
    final int vi = var_3673;
    final int vi1 = v[i1[index]];
    final int vi2 = v[i2[index]];
    final int z0 = v[indexRm1];
    final int z1 = (vi ^ (vi << 16)) ^ (vi1 ^ (vi1 << 15));
    final int z2 = vi2 ^ (vi2 >>> 11);
    final int z3 = z1 ^ z2;
    final int z4 = (z0 ^ (z0 << 2)) ^ (z1 ^ (z1 << 18)) ^ (z2 << 28) ^ (z3 ^ ((z3 << 5) & 0xda442d24));
    v[index] = z3;
    v[indexRm1] = z4;
    index = indexRm1;
    return z4 >>> (32 - bits);
  }
}