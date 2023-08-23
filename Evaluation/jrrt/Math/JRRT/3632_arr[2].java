package org.apache.commons.math3.random;
import java.io.Serializable;

public class ISAACRandom extends BitsStreamGenerator implements Serializable  {
  final private static long serialVersionUID = 7288197941165002400L;
  final private static int SIZE_L = 8;
  final private static int SIZE = 1 << SIZE_L;
  final private static int H_SIZE = SIZE >> 1;
  final private static int MASK = SIZE - 1 << 2;
  final private static int GLD_RATIO = 0x9e3779b9;
  final private int[] rsl = new int[SIZE];
  final private int[] mem = new int[SIZE];
  private int count;
  private int isaacA;
  private int isaacB;
  private int isaacC;
  final private int[] arr = new int[8];
  private int isaacX;
  private int isaacI;
  private int isaacJ;
  public ISAACRandom() {
    super();
    setSeed(System.currentTimeMillis() + System.identityHashCode(this));
  }
  public ISAACRandom(int[] seed) {
    super();
    setSeed(seed);
  }
  public ISAACRandom(long seed) {
    super();
    setSeed(seed);
  }
  @Override() protected int next(int bits) {
    if(count < 0) {
      isaac();
      count = SIZE - 1;
    }
    return rsl[count--] >>> 32 - bits;
  }
  private void initState() {
    isaacA = 0;
    isaacB = 0;
    isaacC = 0;
    for(int j = 0; j < arr.length; j++) {
      arr[j] = GLD_RATIO;
    }
    for(int j = 0; j < 4; j++) {
      shuffle();
    }
    for(int j = 0; j < SIZE; j += 8) {
      arr[0] += rsl[j];
      arr[1] += rsl[j + 1];
      arr[2] += rsl[j + 2];
      arr[3] += rsl[j + 3];
      arr[4] += rsl[j + 4];
      arr[5] += rsl[j + 5];
      arr[6] += rsl[j + 6];
      arr[7] += rsl[j + 7];
      shuffle();
      setState(j);
    }
    for(int j = 0; j < SIZE; j += 8) {
      arr[0] += mem[j];
      arr[1] += mem[j + 1];
      arr[2] += mem[j + 2];
      arr[3] += mem[j + 3];
      arr[4] += mem[j + 4];
      arr[5] += mem[j + 5];
      arr[6] += mem[j + 6];
      arr[7] += mem[j + 7];
      shuffle();
      setState(j);
    }
    isaac();
    count = SIZE - 1;
    clear();
  }
  private void isaac() {
    isaacI = 0;
    isaacJ = H_SIZE;
    isaacB += ++isaacC;
    while(isaacI < H_SIZE){
      isaac2();
    }
    isaacJ = 0;
    while(isaacJ < H_SIZE){
      isaac2();
    }
  }
  private void isaac2() {
    isaacX = mem[isaacI];
    isaacA ^= isaacA << 13;
    isaacA += mem[isaacJ++];
    isaac3();
    isaacX = mem[isaacI];
    isaacA ^= isaacA >>> 6;
    isaacA += mem[isaacJ++];
    isaac3();
    isaacX = mem[isaacI];
    isaacA ^= isaacA << 2;
    isaacA += mem[isaacJ++];
    isaac3();
    isaacX = mem[isaacI];
    isaacA ^= isaacA >>> 16;
    isaacA += mem[isaacJ++];
    isaac3();
  }
  private void isaac3() {
    mem[isaacI] = mem[(isaacX & MASK) >> 2] + isaacA + isaacB;
    isaacB = mem[(mem[isaacI] >> SIZE_L & MASK) >> 2] + isaacX;
    rsl[isaacI++] = isaacB;
  }
  @Override() public void setSeed(int seed) {
    setSeed(new int[]{ seed } );
  }
  @Override() public void setSeed(int[] seed) {
    if(seed == null) {
      setSeed(System.currentTimeMillis() + System.identityHashCode(this));
      return ;
    }
    final int seedLen = seed.length;
    final int rslLen = rsl.length;
    System.arraycopy(seed, 0, rsl, 0, Math.min(seedLen, rslLen));
    if(seedLen < rslLen) {
      for(int j = seedLen; j < rslLen; j++) {
        long k = rsl[j - seedLen];
        rsl[j] = (int)(0x6c078965L * (k ^ k >> 30) + j & 0xffffffffL);
      }
    }
    initState();
  }
  @Override() public void setSeed(long seed) {
    setSeed(new int[]{ (int)(seed >>> 32), (int)(seed & 0xffffffffL) } );
  }
  private void setState(int start) {
    mem[start] = arr[0];
    mem[start + 1] = arr[1];
    mem[start + 2] = arr[2];
    mem[start + 3] = arr[3];
    mem[start + 4] = arr[4];
    mem[start + 5] = arr[5];
    mem[start + 6] = arr[6];
    mem[start + 7] = arr[7];
  }
  private void shuffle() {
    arr[0] ^= arr[1] << 11;
    arr[3] += arr[0];
    arr[1] += arr[2];
    int var_3632 = arr[2];
    arr[1] ^= var_3632 >>> 2;
    arr[4] += arr[1];
    arr[2] += arr[3];
    arr[2] ^= arr[3] << 8;
    arr[5] += arr[2];
    arr[3] += arr[4];
    arr[3] ^= arr[4] >>> 16;
    arr[6] += arr[3];
    arr[4] += arr[5];
    arr[4] ^= arr[5] << 10;
    arr[7] += arr[4];
    arr[5] += arr[6];
    arr[5] ^= arr[6] >>> 4;
    arr[0] += arr[5];
    arr[6] += arr[7];
    arr[6] ^= arr[7] << 8;
    arr[1] += arr[6];
    arr[7] += arr[0];
    arr[7] ^= arr[0] >>> 9;
    arr[2] += arr[7];
    arr[0] += arr[1];
  }
}