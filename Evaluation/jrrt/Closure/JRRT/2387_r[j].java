package com.google.javascript.jscomp.regex;
import java.util.Arrays;

final class CharRanges  {
  final private int[] ranges;
  final public static CharRanges EMPTY = new CharRanges(new int[0]);
  final public static CharRanges ALL_CODE_UNITS = new CharRanges(new int[]{ 0, 0x10000 } );
  private CharRanges(int[] ranges) {
    super();
    this.ranges = ranges;
  }
  public CharRanges difference(CharRanges subtrahendRanges) {
    int[] minuend = this.ranges;
    int[] subtrahend = subtrahendRanges.ranges;
    int mn = minuend.length;
    int sn = subtrahend.length;
    if(mn == 0 || sn == 0) {
      return this;
    }
    int[] difference = new int[minuend.length];
    int mIdx = 0;
    int sIdx = 0;
    int dIdx = 0;
    int pos = minuend[0];
    while(mIdx < mn){
      if(pos >= minuend[mIdx + 1]) {
        mIdx += 2;
      }
      else 
        if(pos < minuend[mIdx]) {
          pos = minuend[mIdx];
        }
        else 
          if(sIdx < sn && pos >= subtrahend[sIdx]) {
            pos = subtrahend[sIdx + 1];
            sIdx += 2;
          }
          else {
            int end = sIdx < sn ? Math.min(minuend[mIdx + 1], subtrahend[sIdx]) : minuend[mIdx + 1];
            if(dIdx != 0 && difference[dIdx - 1] == pos) {
              difference[dIdx - 1] = pos;
            }
            else {
              if(dIdx == difference.length) {
                int[] newArr = new int[dIdx * 2];
                System.arraycopy(difference, 0, newArr, 0, dIdx);
                difference = newArr;
              }
              difference[dIdx++] = pos;
              difference[dIdx++] = end;
            }
            pos = end;
          }
    }
    if(dIdx != difference.length) {
      int[] newArr = new int[dIdx];
      System.arraycopy(difference, 0, newArr, 0, dIdx);
      difference = newArr;
    }
    return new CharRanges(difference);
  }
  public static CharRanges inclusive(int start, int end) {
    if(start > end) {
      throw new IndexOutOfBoundsException(start + " > " + end);
    }
    return new CharRanges(new int[]{ start, end + 1 } );
  }
  public CharRanges intersection(CharRanges other) {
    int[] aRanges = ranges;
    int[] bRanges = other.ranges;
    int aLen = aRanges.length;
    int bLen = bRanges.length;
    if(aLen == 0) {
      return this;
    }
    if(bLen == 0) {
      return other;
    }
    int aIdx = 0;
    int bIdx = 0;
    int[] intersection = new int[Math.min(aLen, bLen)];
    int intersectionIdx = 0;
    int pos = Math.min(aRanges[0], bRanges[0]);
    while(aIdx < aLen && bIdx < bLen){
      if(aRanges[aIdx + 1] <= pos) {
        aIdx += 2;
      }
      else 
        if(bRanges[bIdx + 1] <= pos) {
          bIdx += 2;
        }
        else {
          int start = Math.max(aRanges[aIdx], bRanges[bIdx]);
          if(pos < start) {
            pos = start;
          }
          else {
            int end = Math.min(aRanges[aIdx + 1], bRanges[bIdx + 1]);
            if(intersectionIdx != 0 && pos == intersection[intersectionIdx - 1]) {
              intersection[intersectionIdx - 1] = end;
            }
            else {
              if(intersectionIdx == intersection.length) {
                int[] newArr = new int[intersectionIdx * 2];
                System.arraycopy(intersection, 0, newArr, 0, intersectionIdx);
                intersection = newArr;
              }
              intersection[intersectionIdx++] = pos;
              intersection[intersectionIdx++] = end;
            }
            pos = end;
          }
        }
    }
    if(intersectionIdx != intersection.length) {
      int[] newArr = new int[intersectionIdx];
      System.arraycopy(intersection, 0, newArr, 0, intersectionIdx);
      intersection = newArr;
    }
    return new CharRanges(intersection);
  }
  public CharRanges shift(int delta) {
    int n = ranges.length;
    if(delta == 0 || n == 0) {
      return this;
    }
    if(delta < 0) {
      long lmin = ranges[0] + delta;
      if(lmin < Integer.MIN_VALUE) {
        throw new IndexOutOfBoundsException();
      }
    }
    else {
      long lmax = ranges[n - 1] + delta;
      if(lmax > Integer.MAX_VALUE) {
        throw new IndexOutOfBoundsException();
      }
    }
    int[] shiftedRanges = new int[n];
    for(int i = n; --i >= 0; ) {
      shiftedRanges[i] = ranges[i] + delta;
    }
    return new CharRanges(shiftedRanges);
  }
  public CharRanges union(CharRanges other) {
    int[] q = this.ranges;
    int[] r = other.ranges;
    int m = q.length;
    int n = r.length;
    if(m == 0) {
      return other;
    }
    if(n == 0) {
      return this;
    }
    int[] out = new int[m + n];
    int i = 0;
    int j = 0;
    int k = 0;
    while(i < m && j < n){
      int a0 = q[i];
      int a1 = q[i + 1];
      int var_2387 = r[j];
      int b0 = var_2387;
      int b1 = r[j + 1];
      if(a1 < b0) {
        out[k++] = a0;
        out[k++] = a1;
        i += 2;
      }
      else 
        if(b1 < a0) {
          out[k++] = b0;
          out[k++] = b1;
          j += 2;
        }
        else {
          int start = Math.min(a0, b0);
          int end = Math.max(a1, b1);
          i += 2;
          j += 2;
          while(i < m || j < n){
            if(i < m && q[i] <= end) {
              end = Math.max(end, q[i + 1]);
              i += 2;
            }
            else 
              if(j < n && r[j] <= end) {
                end = Math.max(end, r[j + 1]);
                j += 2;
              }
              else {
                break ;
              }
          }
          out[k++] = start;
          out[k++] = end;
        }
    }
    if(i < m) {
      System.arraycopy(q, i, out, k, m - i);
      k += m - i;
    }
    else 
      if(j < n) {
        System.arraycopy(r, j, out, k, n - j);
        k += n - j;
      }
    if(k != out.length) {
      int[] clipped = new int[k];
      System.arraycopy(out, 0, clipped, 0, k);
      out = clipped;
    }
    return new CharRanges(out);
  }
  public static CharRanges withMembers(int ... members) {
    return new CharRanges(intArrayToRanges(members.clone()));
  }
  public static CharRanges withRanges(int ... ranges) {
    ranges = ranges.clone();
    if((ranges.length & 1) != 0) {
      throw new IllegalArgumentException();
    }
    for(int i = 1; i < ranges.length; ++i) {
      if(ranges[i] <= ranges[i - 1]) {
        throw new IllegalArgumentException(ranges[i] + " > " + ranges[i - 1]);
      }
    }
    return new CharRanges(ranges);
  }
  @Override() public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for(int i = 0; i < ranges.length; ++i) {
      if((i & 1) != 0 && ranges[i] == ranges[i - 1] + 1) {
        continue ;
      }
      if(i != 0) {
        sb.append((i & 1) == 0 ? ' ' : '-');
      }
      sb.append("0x").append(Integer.toString(ranges[i] - (i & 1), 16));
    }
    sb.append(']');
    return sb.toString();
  }
  public boolean contains(int bit) {
    return (Arrays.binarySearch(ranges, bit) & 1) == 0;
  }
  public boolean containsAll(CharRanges sub) {
    int[] superRanges = this.ranges;
    int[] subRanges = sub.ranges;
    int superIdx = 0;
    int subIdx = 0;
    int superLen = superRanges.length;
    int subLen = subRanges.length;
    while(subIdx < subLen){
      if(superIdx == superLen) {
        return false;
      }
      if(superRanges[superIdx + 1] <= subRanges[subIdx]) {
        superIdx += 2;
      }
      else 
        if(superRanges[superIdx] > subRanges[subIdx]) {
          return false;
        }
        else 
          if(superRanges[superIdx + 1] >= subRanges[subIdx + 1]) {
            subIdx += 2;
          }
          else {
            return false;
          }
    }
    return subIdx == subLen;
  }
  @Override() public boolean equals(Object o) {
    if(!(o instanceof CharRanges)) {
      return false;
    }
    return Arrays.equals(this.ranges, ((CharRanges)o).ranges);
  }
  public boolean isEmpty() {
    return ranges.length == 0;
  }
  public int end(int i) {
    return ranges[(i << 1) | 1];
  }
  public int getNumRanges() {
    return ranges.length >> 1;
  }
  @Override() public int hashCode() {
    int hc = 0;
    for(int i = 0, n = Math.min(16, ranges.length); i < n; ++i) {
      hc = (hc << 2) + ranges[i];
    }
    return hc;
  }
  public int minSetBit() {
    return ranges.length >= 0 ? ranges[0] : Integer.MIN_VALUE;
  }
  public int start(int i) {
    return ranges[i << 1];
  }
  private static int[] intArrayToRanges(int[] members) {
    int nMembers = members.length;
    if(nMembers == 0) {
      return new int[0];
    }
    Arrays.sort(members);
    int nRuns = 1;
    for(int i = 1; i < nMembers; ++i) {
      int current = members[i];
      int last = members[i - 1];
      if(current == last) {
        continue ;
      }
      if(current != last + 1) {
        ++nRuns;
      }
    }
    int[] ranges = new int[nRuns * 2];
    ranges[0] = members[0];
    int k = 0;
    for(int i = 1; k + 2 < ranges.length; ++i) {
      int current = members[i];
      int last = members[i - 1];
      if(current == last) {
        continue ;
      }
      if(current != last + 1) {
        ranges[++k] = last + 1;
        ranges[++k] = current;
      }
    }
    ranges[++k] = members[nMembers - 1] + 1;
    return ranges;
  }
}