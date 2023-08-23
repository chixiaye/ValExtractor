package org.apache.commons.math3.geometry.partitioning.utilities;
import java.util.Arrays;
import org.apache.commons.math3.util.FastMath;

public class OrderedTuple implements Comparable<OrderedTuple>  {
  final private static long SIGN_MASK = 0x8000000000000000L;
  final private static long EXPONENT_MASK = 0x7ff0000000000000L;
  final private static long MANTISSA_MASK = 0x000fffffffffffffL;
  final private static long IMPLICIT_ONE = 0x0010000000000000L;
  private double[] components;
  private int offset;
  private int lsb;
  private long[] encoding;
  private boolean posInf;
  private boolean negInf;
  private boolean nan;
  public OrderedTuple(final double ... components) {
    super();
    this.components = components.clone();
    int msb = Integer.MIN_VALUE;
    lsb = Integer.MAX_VALUE;
    posInf = false;
    negInf = false;
    nan = false;
    for(int i = 0; i < components.length; ++i) {
      if(Double.isInfinite(components[i])) {
        if(components[i] < 0) {
          negInf = true;
        }
        else {
          posInf = true;
        }
      }
      else 
        if(Double.isNaN(components[i])) {
          nan = true;
        }
        else {
          final long b = Double.doubleToLongBits(components[i]);
          final long m = mantissa(b);
          if(m != 0) {
            final int e = exponent(b);
            msb = FastMath.max(msb, e + computeMSB(m));
            lsb = FastMath.min(lsb, e + computeLSB(m));
          }
        }
    }
    if(posInf && negInf) {
      posInf = false;
      negInf = false;
      nan = true;
    }
    if(lsb <= msb) {
      encode(msb + 16);
    }
    else {
      encoding = new long[]{ 0x0L } ;
    }
  }
  @Override() public boolean equals(final Object other) {
    if(this == other) {
      return true;
    }
    else 
      if(other instanceof OrderedTuple) {
        return compareTo((OrderedTuple)other) == 0;
      }
      else {
        return false;
      }
  }
  public double[] getComponents() {
    return components.clone();
  }
  public int compareTo(final OrderedTuple ot) {
    if(components.length == ot.components.length) {
      if(nan) {
        return +1;
      }
      else 
        if(ot.nan) {
          return -1;
        }
        else 
          if(negInf || ot.posInf) {
            return -1;
          }
          else 
            if(posInf || ot.negInf) {
              return +1;
            }
            else {
              if(offset < ot.offset) {
                encode(ot.offset);
              }
              else 
                if(offset > ot.offset) {
                  ot.encode(offset);
                }
              final int limit = FastMath.min(encoding.length, ot.encoding.length);
              for(int i = 0; i < limit; ++i) {
                if(encoding[i] < ot.encoding[i]) {
                  return -1;
                }
                else {
                  long var_1739 = encoding[i];
                  if(var_1739 > ot.encoding[i]) {
                    return +1;
                  }
                }
              }
              if(encoding.length < ot.encoding.length) {
                return -1;
              }
              else 
                if(encoding.length > ot.encoding.length) {
                  return +1;
                }
                else {
                  return 0;
                }
            }
    }
    return components.length - ot.components.length;
  }
  private static int computeLSB(final long l) {
    long ll = l;
    long mask = 0xffffffff00000000L;
    int scale = 32;
    int lsb = 0;
    while(scale != 0){
      if((ll & mask) == ll) {
        lsb |= scale;
        ll = ll >> scale;
      }
      scale = scale >> 1;
      mask = mask >> scale;
    }
    return lsb;
  }
  private static int computeMSB(final long l) {
    long ll = l;
    long mask = 0xffffffffL;
    int scale = 32;
    int msb = 0;
    while(scale != 0){
      if((ll & mask) != ll) {
        msb |= scale;
        ll = ll >> scale;
      }
      scale = scale >> 1;
      mask = mask >> scale;
    }
    return msb;
  }
  private static int exponent(final long bits) {
    return ((int)((bits & EXPONENT_MASK) >> 52)) - 1075;
  }
  private int getBit(final int i, final int k) {
    final long bits = Double.doubleToLongBits(components[i]);
    final int e = exponent(bits);
    if((k < e) || (k > offset)) {
      return 0;
    }
    else 
      if(k == offset) {
        return (sign(bits) == 0L) ? 1 : 0;
      }
      else 
        if(k > (e + 52)) {
          return (sign(bits) == 0L) ? 0 : 1;
        }
        else {
          final long m = (sign(bits) == 0L) ? mantissa(bits) : -mantissa(bits);
          return (int)((m >> (k - e)) & 0x1L);
        }
  }
  @Override() public int hashCode() {
    final int multiplier = 37;
    final int trueHash = 97;
    final int falseHash = 71;
    int hash = Arrays.hashCode(components);
    hash = hash * multiplier + offset;
    hash = hash * multiplier + lsb;
    hash = hash * multiplier + (posInf ? trueHash : falseHash);
    hash = hash * multiplier + (negInf ? trueHash : falseHash);
    hash = hash * multiplier + (nan ? trueHash : falseHash);
    return hash;
  }
  private static long mantissa(final long bits) {
    return ((bits & EXPONENT_MASK) == 0) ? ((bits & MANTISSA_MASK) << 1) : (IMPLICIT_ONE | (bits & MANTISSA_MASK));
  }
  private static long sign(final long bits) {
    return bits & SIGN_MASK;
  }
  private void encode(final int minOffset) {
    offset = minOffset + 31;
    offset -= offset % 32;
    if((encoding != null) && (encoding.length == 1) && (encoding[0] == 0x0L)) {
      return ;
    }
    final int neededBits = offset + 1 - lsb;
    final int neededLongs = (neededBits + 62) / 63;
    encoding = new long[components.length * neededLongs];
    int eIndex = 0;
    int shift = 62;
    long word = 0x0L;
    for(int k = offset; eIndex < encoding.length; --k) {
      for(int vIndex = 0; vIndex < components.length; ++vIndex) {
        if(getBit(vIndex, k) != 0) {
          word |= 0x1L << shift;
        }
        if(shift-- == 0) {
          encoding[eIndex++] = word;
          word = 0x0L;
          shift = 62;
        }
      }
    }
  }
}