package org.apache.commons.math3.util;
import java.io.PrintStream;

public class FastMath  {
  final public static double PI = 105414357.0D / 33554432.0D + 1.984187159361080883e-9D;
  final public static double E = 2850325.0D / 1048576.0D + 8.254840070411028747e-8D;
  final static int EXP_INT_TABLE_MAX_INDEX = 750;
  final static int EXP_INT_TABLE_LEN = EXP_INT_TABLE_MAX_INDEX * 2;
  final static int LN_MANT_LEN = 1024;
  final static int EXP_FRAC_TABLE_LEN = 1025;
  final private static double LOG_MAX_VALUE = StrictMath.log(Double.MAX_VALUE);
  final private static boolean RECOMPUTE_TABLES_AT_RUNTIME = false;
  final private static double LN_2_A = 0.693147063255310059D;
  final private static double LN_2_B = 1.17304635250823482e-7D;
  final private static double[][] LN_QUICK_COEF = { { 1.0D, 5.669184079525E-24D } , { -0.25D, -0.25D } , { 0.3333333134651184D, 1.986821492305628E-8D } , { -0.25D, -6.663542893624021E-14D } , { 0.19999998807907104D, 1.1921056801463227E-8D } , { -0.1666666567325592D, -7.800414592973399E-9D } , { 0.1428571343421936D, 5.650007086920087E-9D } , { -0.12502530217170715D, -7.44321345601866E-11D } , { 0.11113807559013367D, 9.219544613762692E-9D }  } ;
  final private static double[][] LN_HI_PREC_COEF = { { 1.0D, -6.032174644509064E-23D } , { -0.25D, -0.25D } , { 0.3333333134651184D, 1.9868161777724352E-8D } , { -0.2499999701976776D, -2.957007209750105E-8D } , { 0.19999954104423523D, 1.5830993332061267E-10D } , { -0.16624879837036133D, -2.6033824355191673E-8D }  } ;
  final private static int SINE_TABLE_LEN = 14;
  final private static double[] SINE_TABLE_A = { +0.0D, +0.1246747374534607D, +0.24740394949913025D, +0.366272509098053D, +0.4794255495071411D, +0.5850973129272461D, +0.6816387176513672D, +0.7675435543060303D, +0.8414709568023682D, +0.902267575263977D, +0.9489846229553223D, +0.9808930158615112D, +0.9974949359893799D, +0.9985313415527344D } ;
  final private static double[] SINE_TABLE_B = { +0.0D, -4.068233003401932E-9D, +9.755392680573412E-9D, +1.9987994582857286E-8D, -1.0902938113007961E-8D, -3.9986783938944604E-8D, +4.23719669792332E-8D, -5.207000323380292E-8D, +2.800552834259E-8D, +1.883511811213715E-8D, -3.5997360512765566E-9D, +4.116164446561962E-8D, +5.0614674548127384E-8D, -1.0129027912496858E-9D } ;
  final private static double[] COSINE_TABLE_A = { +1.0D, +0.9921976327896118D, +0.9689123630523682D, +0.9305076599121094D, +0.8775825500488281D, +0.8109631538391113D, +0.7316888570785522D, +0.6409968137741089D, +0.5403022766113281D, +0.4311765432357788D, +0.3153223395347595D, +0.19454771280288696D, +0.07073719799518585D, -0.05417713522911072D } ;
  final private static double[] COSINE_TABLE_B = { +0.0D, +3.4439717236742845E-8D, +5.865827662008209E-8D, -3.7999795083850525E-8D, +1.184154459111628E-8D, -3.43338934259355E-8D, +1.1795268640216787E-8D, +4.438921624363781E-8D, +2.925681159240093E-8D, -2.6437112632041807E-8D, +2.2860509143963117E-8D, -4.813899778443457E-9D, +3.6725170580355583E-9D, +2.0217439756338078E-10D } ;
  final private static double[] TANGENT_TABLE_A = { +0.0D, +0.1256551444530487D, +0.25534194707870483D, +0.3936265707015991D, +0.5463024377822876D, +0.7214844226837158D, +0.9315965175628662D, +1.1974215507507324D, +1.5574076175689697D, +2.092571258544922D, +3.0095696449279785D, +5.041914939880371D, +14.101419448852539D, -18.430862426757812D } ;
  final private static double[] TANGENT_TABLE_B = { +0.0D, -7.877917738262007E-9D, -2.5857668567479893E-8D, +5.2240336371356666E-9D, +5.206150291559893E-8D, +1.8307188599677033E-8D, -5.7618793749770706E-8D, +7.848361555046424E-8D, +1.0708593250394448E-7D, +1.7827257129423813E-8D, +2.893485277253286E-8D, +3.1660099222737955E-7D, +4.983191803254889E-7D, -3.356118100840571E-7D } ;
  final private static long[] RECIP_2PI = new long[]{ (0x28be60dbL << 32) | 0x9391054aL, (0x7f09d5f4L << 32) | 0x7d4d3770L, (0x36d8a566L << 32) | 0x4f10e410L, (0x7f9458eaL << 32) | 0xf7aef158L, (0x6dc91b8eL << 32) | 0x909374b8L, (0x01924bbaL << 32) | 0x82746487L, (0x3f877ac7L << 32) | 0x2c4a69cfL, (0xba208d7dL << 32) | 0x4baed121L, (0x3a671c09L << 32) | 0xad17df90L, (0x4e64758eL << 32) | 0x60d4ce7dL, (0x272117e2L << 32) | 0xef7e4a0eL, (0xc7fe25ffL << 32) | 0xf7816603L, (0xfbcbc462L << 32) | 0xd6829b47L, (0xdb4d9fb3L << 32) | 0xc9f2c26dL, (0xd3d18fd9L << 32) | 0xa797fa8bL, (0x5d49eeb1L << 32) | 0xfaf97c5eL, (0xcf41ce7dL << 32) | 0xe294a4baL, 0x9afed7ecL << 32 } ;
  final private static long[] PI_O_4_BITS = new long[]{ (0xc90fdaa2L << 32) | 0x2168c234L, (0xc4c6628bL << 32) | 0x80dc1cd1L } ;
  final private static double[] EIGHTHS = { 0, 0.125D, 0.25D, 0.375D, 0.5D, 0.625D, 0.75D, 0.875D, 1.0D, 1.125D, 1.25D, 1.375D, 1.5D, 1.625D } ;
  final private static double[] CBRTTWO = { 0.6299605249474366D, 0.7937005259840998D, 1.0D, 1.2599210498948732D, 1.5874010519681994D } ;
  final private static long HEX_40000000 = 0x40000000L;
  final private static long MASK_30BITS = -1L - (HEX_40000000 - 1);
  final private static int MASK_NON_SIGN_INT = 0x7fffffff;
  final private static long MASK_NON_SIGN_LONG = 0x7fffffffffffffffL;
  final private static double TWO_POWER_52 = 4503599627370496.0D;
  final private static double TWO_POWER_53 = 2 * TWO_POWER_52;
  final private static double F_1_3 = 1D / 3D;
  final private static double F_1_5 = 1D / 5D;
  final private static double F_1_7 = 1D / 7D;
  final private static double F_1_9 = 1D / 9D;
  final private static double F_1_11 = 1D / 11D;
  final private static double F_1_13 = 1D / 13D;
  final private static double F_1_15 = 1D / 15D;
  final private static double F_1_17 = 1D / 17D;
  final private static double F_3_4 = 3D / 4D;
  final private static double F_15_16 = 15D / 16D;
  final private static double F_13_14 = 13D / 14D;
  final private static double F_11_12 = 11D / 12D;
  final private static double F_9_10 = 9D / 10D;
  final private static double F_7_8 = 7D / 8D;
  final private static double F_5_6 = 5D / 6D;
  final private static double F_1_2 = 1D / 2D;
  final private static double F_1_4 = 1D / 4D;
  private FastMath() {
    super();
  }
  public static double IEEEremainder(double dividend, double divisor) {
    return StrictMath.IEEEremainder(dividend, divisor);
  }
  public static double abs(double x) {
    return Double.longBitsToDouble(MASK_NON_SIGN_LONG & Double.doubleToRawLongBits(x));
  }
  public static double acos(double x) {
    if(x != x) {
      return Double.NaN;
    }
    if(x > 1.0D || x < -1.0D) {
      return Double.NaN;
    }
    if(x == -1.0D) {
      return Math.PI;
    }
    if(x == 1.0D) {
      return 0.0D;
    }
    if(x == 0) {
      return Math.PI / 2.0D;
    }
    double temp = x * HEX_40000000;
    final double xa = x + temp - temp;
    final double xb = x - xa;
    double ya = xa * xa;
    double yb = xa * xb * 2.0D + xb * xb;
    ya = -ya;
    yb = -yb;
    double za = 1.0D + ya;
    double zb = -(za - 1.0D - ya);
    temp = za + yb;
    zb += -(temp - za - yb);
    za = temp;
    double y = sqrt(za);
    temp = y * HEX_40000000;
    ya = y + temp - temp;
    yb = y - ya;
    yb += (za - ya * ya - 2 * ya * yb - yb * yb) / (2.0D * y);
    yb += zb / (2.0D * y);
    y = ya + yb;
    yb = -(y - ya - yb);
    double r = y / x;
    if(Double.isInfinite(r)) {
      return Math.PI / 2;
    }
    double ra = doubleHighPart(r);
    double rb = r - ra;
    rb += (y - ra * xa - ra * xb - rb * xa - rb * xb) / x;
    rb += yb / x;
    temp = ra + rb;
    rb = -(temp - ra - rb);
    ra = temp;
    return atan(ra, rb, x < 0);
  }
  public static double acosh(final double a) {
    return FastMath.log(a + FastMath.sqrt(a * a - 1));
  }
  public static double asin(double x) {
    if(x != x) {
      return Double.NaN;
    }
    if(x > 1.0D || x < -1.0D) {
      return Double.NaN;
    }
    if(x == 1.0D) {
      return Math.PI / 2.0D;
    }
    if(x == -1.0D) {
      return -Math.PI / 2.0D;
    }
    if(x == 0.0D) {
      return x;
    }
    double temp = x * HEX_40000000;
    final double xa = x + temp - temp;
    final double xb = x - xa;
    double ya = xa * xa;
    double yb = xa * xb * 2.0D + xb * xb;
    ya = -ya;
    yb = -yb;
    double za = 1.0D + ya;
    double zb = -(za - 1.0D - ya);
    temp = za + yb;
    zb += -(temp - za - yb);
    za = temp;
    double y;
    y = sqrt(za);
    temp = y * HEX_40000000;
    ya = y + temp - temp;
    yb = y - ya;
    yb += (za - ya * ya - 2 * ya * yb - yb * yb) / (2.0D * y);
    double dx = zb / (2.0D * y);
    double r = x / y;
    temp = r * HEX_40000000;
    double ra = r + temp - temp;
    double rb = r - ra;
    rb += (x - ra * ya - ra * yb - rb * ya - rb * yb) / y;
    rb += -x * dx / y / y;
    temp = ra + rb;
    rb = -(temp - ra - rb);
    ra = temp;
    return atan(ra, rb, false);
  }
  public static double asinh(double a) {
    boolean negative = false;
    if(a < 0) {
      negative = true;
      a = -a;
    }
    double absAsinh;
    if(a > 0.167D) {
      absAsinh = FastMath.log(FastMath.sqrt(a * a + 1) + a);
    }
    else {
      final double a2 = a * a;
      if(a > 0.097D) {
        absAsinh = a * (1 - a2 * (F_1_3 - a2 * (F_1_5 - a2 * (F_1_7 - a2 * (F_1_9 - a2 * (F_1_11 - a2 * (F_1_13 - a2 * (F_1_15 - a2 * F_1_17 * F_15_16) * F_13_14) * F_11_12) * F_9_10) * F_7_8) * F_5_6) * F_3_4) * F_1_2);
      }
      else 
        if(a > 0.036D) {
          absAsinh = a * (1 - a2 * (F_1_3 - a2 * (F_1_5 - a2 * (F_1_7 - a2 * (F_1_9 - a2 * (F_1_11 - a2 * F_1_13 * F_11_12) * F_9_10) * F_7_8) * F_5_6) * F_3_4) * F_1_2);
        }
        else 
          if(a > 0.0036D) {
            absAsinh = a * (1 - a2 * (F_1_3 - a2 * (F_1_5 - a2 * (F_1_7 - a2 * F_1_9 * F_7_8) * F_5_6) * F_3_4) * F_1_2);
          }
          else {
            absAsinh = a * (1 - a2 * (F_1_3 - a2 * F_1_5 * F_3_4) * F_1_2);
          }
    }
    return negative ? -absAsinh : absAsinh;
  }
  public static double atan(double x) {
    return atan(x, 0.0D, false);
  }
  private static double atan(double xa, double xb, boolean leftPlane) {
    boolean negate = false;
    int idx;
    if(xa == 0.0D) {
      return leftPlane ? copySign(Math.PI, xa) : xa;
    }
    if(xa < 0) {
      xa = -xa;
      xb = -xb;
      negate = true;
    }
    if(xa > 1.633123935319537E16D) {
      return (negate ^ leftPlane) ? (-Math.PI * F_1_2) : (Math.PI * F_1_2);
    }
    if(xa < 1) {
      idx = (int)(((-1.7168146928204136D * xa * xa + 8.0D) * xa) + 0.5D);
    }
    else {
      final double oneOverXa = 1 / xa;
      idx = (int)(-((-1.7168146928204136D * oneOverXa * oneOverXa + 8.0D) * oneOverXa) + 13.07D);
    }
    double epsA = xa - TANGENT_TABLE_A[idx];
    double epsB = -(epsA - xa + TANGENT_TABLE_A[idx]);
    epsB += xb - TANGENT_TABLE_B[idx];
    double temp = epsA + epsB;
    epsB = -(temp - epsA - epsB);
    epsA = temp;
    temp = xa * HEX_40000000;
    double ya = xa + temp - temp;
    double yb = xb + xa - ya;
    xa = ya;
    xb += yb;
    if(idx == 0) {
      final double denom = 1D / (1D + (xa + xb) * (TANGENT_TABLE_A[idx] + TANGENT_TABLE_B[idx]));
      ya = epsA * denom;
      yb = epsB * denom;
    }
    else {
      double temp2 = xa * TANGENT_TABLE_A[idx];
      double za = 1D + temp2;
      double zb = -(za - 1D - temp2);
      temp2 = xb * TANGENT_TABLE_A[idx] + xa * TANGENT_TABLE_B[idx];
      temp = za + temp2;
      zb += -(temp - za - temp2);
      za = temp;
      zb += xb * TANGENT_TABLE_B[idx];
      ya = epsA / za;
      temp = ya * HEX_40000000;
      final double yaa = (ya + temp) - temp;
      final double yab = ya - yaa;
      temp = za * HEX_40000000;
      final double zaa = (za + temp) - temp;
      final double zab = za - zaa;
      yb = (epsA - yaa * zaa - yaa * zab - yab * zaa - yab * zab) / za;
      yb += -epsA * zb / za / za;
      yb += epsB / za;
    }
    epsA = ya;
    epsB = yb;
    final double epsA2 = epsA * epsA;
    yb = 0.07490822288864472D;
    yb = yb * epsA2 + -0.09088450866185192D;
    yb = yb * epsA2 + 0.11111095942313305D;
    yb = yb * epsA2 + -0.1428571423679182D;
    yb = yb * epsA2 + 0.19999999999923582D;
    yb = yb * epsA2 + -0.33333333333333287D;
    yb = yb * epsA2 * epsA;
    ya = epsA;
    temp = ya + yb;
    yb = -(temp - ya - yb);
    ya = temp;
    yb += epsB / (1D + epsA * epsA);
    double za = EIGHTHS[idx] + ya;
    double zb = -(za - EIGHTHS[idx] - ya);
    temp = za + yb;
    zb += -(temp - za - yb);
    za = temp;
    double result = za + zb;
    if(leftPlane) {
      final double resultb = -(result - za - zb);
      final double pia = 1.5707963267948966D * 2;
      final double pib = 6.123233995736766E-17D * 2;
      za = pia - result;
      zb = -(za - pia + result);
      zb += pib - resultb;
      result = za + zb;
    }
    if(negate ^ leftPlane) {
      result = -result;
    }
    return result;
  }
  public static double atan2(double y, double x) {
    if(x != x || y != y) {
      return Double.NaN;
    }
    if(y == 0) {
      final double result = x * y;
      final double invx = 1D / x;
      final double invy = 1D / y;
      if(invx == 0) {
        if(x > 0) {
          return y;
        }
        else {
          return copySign(Math.PI, y);
        }
      }
      if(x < 0 || invx < 0) {
        if(y < 0 || invy < 0) {
          return -Math.PI;
        }
        else {
          return Math.PI;
        }
      }
      else {
        return result;
      }
    }
    if(y == Double.POSITIVE_INFINITY) {
      if(x == Double.POSITIVE_INFINITY) {
        return Math.PI * F_1_4;
      }
      if(x == Double.NEGATIVE_INFINITY) {
        return Math.PI * F_3_4;
      }
      return Math.PI * F_1_2;
    }
    if(y == Double.NEGATIVE_INFINITY) {
      if(x == Double.POSITIVE_INFINITY) {
        return -Math.PI * F_1_4;
      }
      if(x == Double.NEGATIVE_INFINITY) {
        return -Math.PI * F_3_4;
      }
      return -Math.PI * F_1_2;
    }
    if(x == Double.POSITIVE_INFINITY) {
      if(y > 0 || 1 / y > 0) {
        return 0D;
      }
      if(y < 0 || 1 / y < 0) {
        return -0D;
      }
    }
    if(x == Double.NEGATIVE_INFINITY) {
      if(y > 0.0D || 1 / y > 0.0D) {
        return Math.PI;
      }
      if(y < 0 || 1 / y < 0) {
        return -Math.PI;
      }
    }
    if(x == 0) {
      if(y > 0 || 1 / y > 0) {
        return Math.PI * F_1_2;
      }
      if(y < 0 || 1 / y < 0) {
        return -Math.PI * F_1_2;
      }
    }
    final double r = y / x;
    if(Double.isInfinite(r)) {
      return atan(r, 0, x < 0);
    }
    double ra = doubleHighPart(r);
    double rb = r - ra;
    final double xa = doubleHighPart(x);
    final double xb = x - xa;
    rb += (y - ra * xa - ra * xb - rb * xa - rb * xb) / x;
    final double temp = ra + rb;
    rb = -(temp - ra - rb);
    ra = temp;
    if(ra == 0) {
      ra = copySign(0D, y);
    }
    final double result = atan(ra, rb, x < 0);
    return result;
  }
  public static double atanh(double a) {
    boolean negative = false;
    if(a < 0) {
      negative = true;
      a = -a;
    }
    double absAtanh;
    if(a > 0.15D) {
      absAtanh = 0.5D * FastMath.log((1 + a) / (1 - a));
    }
    else {
      final double a2 = a * a;
      if(a > 0.087D) {
        absAtanh = a * (1 + a2 * (F_1_3 + a2 * (F_1_5 + a2 * (F_1_7 + a2 * (F_1_9 + a2 * (F_1_11 + a2 * (F_1_13 + a2 * (F_1_15 + a2 * F_1_17))))))));
      }
      else 
        if(a > 0.031D) {
          absAtanh = a * (1 + a2 * (F_1_3 + a2 * (F_1_5 + a2 * (F_1_7 + a2 * (F_1_9 + a2 * (F_1_11 + a2 * F_1_13))))));
        }
        else 
          if(a > 0.003D) {
            absAtanh = a * (1 + a2 * (F_1_3 + a2 * (F_1_5 + a2 * (F_1_7 + a2 * F_1_9))));
          }
          else {
            absAtanh = a * (1 + a2 * (F_1_3 + a2 * F_1_5));
          }
    }
    return negative ? -absAtanh : absAtanh;
  }
  public static double cbrt(double x) {
    long inbits = Double.doubleToRawLongBits(x);
    int exponent = (int)((inbits >> 52) & 0x7ff) - 1023;
    boolean subnormal = false;
    if(exponent == -1023) {
      if(x == 0) {
        return x;
      }
      subnormal = true;
      x *= 1.8014398509481984E16D;
      inbits = Double.doubleToRawLongBits(x);
      exponent = (int)((inbits >> 52) & 0x7ff) - 1023;
    }
    if(exponent == 1024) {
      return x;
    }
    int exp3 = exponent / 3;
    double p2 = Double.longBitsToDouble((inbits & 0x8000000000000000L) | (long)(((exp3 + 1023) & 0x7ff)) << 52);
    final double mant = Double.longBitsToDouble((inbits & 0x000fffffffffffffL) | 0x3ff0000000000000L);
    double est = -0.010714690733195933D;
    est = est * mant + 0.0875862700108075D;
    est = est * mant + -0.3058015757857271D;
    est = est * mant + 0.7249995199969751D;
    est = est * mant + 0.5039018405998233D;
    est *= CBRTTWO[exponent % 3 + 2];
    final double xs = x / (p2 * p2 * p2);
    est += (xs - est * est * est) / (3 * est * est);
    est += (xs - est * est * est) / (3 * est * est);
    double temp = est * HEX_40000000;
    double ya = est + temp - temp;
    double yb = est - ya;
    double za = ya * ya;
    double zb = ya * yb * 2.0D + yb * yb;
    temp = za * HEX_40000000;
    double temp2 = za + temp - temp;
    zb += za - temp2;
    za = temp2;
    zb = za * yb + ya * zb + zb * yb;
    za = za * ya;
    double na = xs - za;
    double nb = -(na - xs + za);
    nb -= zb;
    est += (na + nb) / (3 * est * est);
    est *= p2;
    if(subnormal) {
      est *= 3.814697265625E-6D;
    }
    return est;
  }
  public static double ceil(double x) {
    double y;
    if(x != x) {
      return x;
    }
    y = floor(x);
    if(y == x) {
      return y;
    }
    y += 1.0D;
    if(y == 0) {
      return x * y;
    }
    return y;
  }
  public static double copySign(double magnitude, double sign) {
    final long m = Double.doubleToRawLongBits(magnitude);
    final long s = Double.doubleToRawLongBits(sign);
    if((m ^ s) >= 0) {
      return magnitude;
    }
    return -magnitude;
  }
  public static double cos(double x) {
    int quadrant = 0;
    double xa = x;
    if(x < 0) {
      xa = -xa;
    }
    if(xa != xa || xa == Double.POSITIVE_INFINITY) {
      return Double.NaN;
    }
    double xb = 0;
    if(xa > 3294198.0D) {
      double[] reduceResults = new double[3];
      reducePayneHanek(xa, reduceResults);
      quadrant = ((int)reduceResults[0]) & 3;
      xa = reduceResults[1];
      xb = reduceResults[2];
    }
    else 
      if(xa > 1.5707963267948966D) {
        final CodyWaite cw = new CodyWaite(xa);
        quadrant = cw.getK() & 3;
        xa = cw.getRemA();
        xb = cw.getRemB();
      }
    switch (quadrant){
      case 0:
      return cosQ(xa, xb);
      case 1:
      return -sinQ(xa, xb);
      case 2:
      return -cosQ(xa, xb);
      case 3:
      return sinQ(xa, xb);
      default:
      return Double.NaN;
    }
  }
  private static double cosQ(double xa, double xb) {
    final double pi2a = 1.5707963267948966D;
    final double pi2b = 6.123233995736766E-17D;
    final double a = pi2a - xa;
    double b = -(a - pi2a + xa);
    b += pi2b - xb;
    return sinQ(a, b);
  }
  public static double cosh(double x) {
    if(x != x) {
      return x;
    }
    if(x > 20) {
      if(x >= LOG_MAX_VALUE) {
        final double t = exp(0.5D * x);
        return (0.5D * t) * t;
      }
      else {
        return 0.5D * exp(x);
      }
    }
    else 
      if(x < -20) {
        if(x <= -LOG_MAX_VALUE) {
          final double t = exp(-0.5D * x);
          return (0.5D * t) * t;
        }
        else {
          return 0.5D * exp(-x);
        }
      }
    final double[] hiPrec = new double[2];
    if(x < 0.0D) {
      x = -x;
    }
    exp(x, 0.0D, hiPrec);
    double ya = hiPrec[0] + hiPrec[1];
    double yb = -(ya - hiPrec[0] - hiPrec[1]);
    double temp = ya * HEX_40000000;
    double yaa = ya + temp - temp;
    double yab = ya - yaa;
    double recip = 1.0D / ya;
    temp = recip * HEX_40000000;
    double recipa = recip + temp - temp;
    double recipb = recip - recipa;
    recipb += (1.0D - yaa * recipa - yaa * recipb - yab * recipa - yab * recipb) * recip;
    recipb += -yb * recip * recip;
    temp = ya + recipa;
    yb += -(temp - ya - recipa);
    ya = temp;
    temp = ya + recipb;
    yb += -(temp - ya - recipb);
    ya = temp;
    double result = ya + yb;
    result *= 0.5D;
    return result;
  }
  private static double doubleHighPart(double d) {
    if(d > -Precision.SAFE_MIN && d < Precision.SAFE_MIN) {
      return d;
    }
    long xl = Double.doubleToRawLongBits(d);
    xl = xl & MASK_30BITS;
    return Double.longBitsToDouble(xl);
  }
  public static double exp(double x) {
    return exp(x, 0.0D, null);
  }
  private static double exp(double x, double extra, double[] hiPrec) {
    double intPartA;
    double intPartB;
    int intVal;
    if(x < 0.0D) {
      intVal = (int)-x;
      if(intVal > 746) {
        if(hiPrec != null) {
          hiPrec[0] = 0.0D;
          hiPrec[1] = 0.0D;
        }
        return 0.0D;
      }
      if(intVal > 709) {
        final double result = exp(x + 40.19140625D, extra, hiPrec) / 285040095144011776.0D;
        if(hiPrec != null) {
          hiPrec[0] /= 285040095144011776.0D;
          hiPrec[1] /= 285040095144011776.0D;
        }
        return result;
      }
      if(intVal == 709) {
        final double result = exp(x + 1.494140625D, extra, hiPrec) / 4.455505956692756620D;
        if(hiPrec != null) {
          hiPrec[0] /= 4.455505956692756620D;
          hiPrec[1] /= 4.455505956692756620D;
        }
        return result;
      }
      intVal++;
      intPartA = ExpIntTable.EXP_INT_TABLE_A[EXP_INT_TABLE_MAX_INDEX - intVal];
      intPartB = ExpIntTable.EXP_INT_TABLE_B[EXP_INT_TABLE_MAX_INDEX - intVal];
      intVal = -intVal;
    }
    else {
      intVal = (int)x;
      if(intVal > 709) {
        if(hiPrec != null) {
          hiPrec[0] = Double.POSITIVE_INFINITY;
          hiPrec[1] = 0.0D;
        }
        return Double.POSITIVE_INFINITY;
      }
      intPartA = ExpIntTable.EXP_INT_TABLE_A[EXP_INT_TABLE_MAX_INDEX + intVal];
      intPartB = ExpIntTable.EXP_INT_TABLE_B[EXP_INT_TABLE_MAX_INDEX + intVal];
    }
    final int intFrac = (int)((x - intVal) * 1024.0D);
    final double fracPartA = ExpFracTable.EXP_FRAC_TABLE_A[intFrac];
    final double fracPartB = ExpFracTable.EXP_FRAC_TABLE_B[intFrac];
    final double epsilon = x - (intVal + intFrac / 1024.0D);
    double z = 0.04168701738764507D;
    z = z * epsilon + 0.1666666505023083D;
    z = z * epsilon + 0.5000000000042687D;
    z = z * epsilon + 1.0D;
    z = z * epsilon + -3.940510424527919E-20D;
    double tempA = intPartA * fracPartA;
    double tempB = intPartA * fracPartB + intPartB * fracPartA + intPartB * fracPartB;
    final double tempC = tempB + tempA;
    final double result;
    if(extra != 0.0D) {
      result = tempC * extra * z + tempC * extra + tempC * z + tempB + tempA;
    }
    else {
      result = tempC * z + tempB + tempA;
    }
    if(hiPrec != null) {
      hiPrec[0] = tempA;
      hiPrec[1] = tempC * extra * z + tempC * extra + tempC * z + tempB;
    }
    return result;
  }
  public static double expm1(double x) {
    return expm1(x, null);
  }
  private static double expm1(double x, double[] hiPrecOut) {
    if(x != x || x == 0.0D) {
      return x;
    }
    if(x <= -1.0D || x >= 1.0D) {
      double[] hiPrec = new double[2];
      exp(x, 0.0D, hiPrec);
      if(x > 0.0D) {
        return -1.0D + hiPrec[0] + hiPrec[1];
      }
      else {
        final double ra = -1.0D + hiPrec[0];
        double rb = -(ra + 1.0D - hiPrec[0]);
        rb += hiPrec[1];
        return ra + rb;
      }
    }
    double baseA;
    double baseB;
    double epsilon;
    boolean negative = false;
    if(x < 0.0D) {
      x = -x;
      negative = true;
    }
    {
      int intFrac = (int)(x * 1024.0D);
      double tempA = ExpFracTable.EXP_FRAC_TABLE_A[intFrac] - 1.0D;
      double tempB = ExpFracTable.EXP_FRAC_TABLE_B[intFrac];
      double temp = tempA + tempB;
      tempB = -(temp - tempA - tempB);
      tempA = temp;
      temp = tempA * HEX_40000000;
      baseA = tempA + temp - temp;
      baseB = tempB + (tempA - baseA);
      epsilon = x - intFrac / 1024.0D;
    }
    double zb = 0.008336750013465571D;
    zb = zb * epsilon + 0.041666663879186654D;
    zb = zb * epsilon + 0.16666666666745392D;
    zb = zb * epsilon + 0.49999999999999994D;
    zb = zb * epsilon;
    zb = zb * epsilon;
    double za = epsilon;
    double temp = za + zb;
    zb = -(temp - za - zb);
    za = temp;
    temp = za * HEX_40000000;
    temp = za + temp - temp;
    zb += za - temp;
    za = temp;
    double ya = za * baseA;
    temp = ya + za * baseB;
    double yb = -(temp - ya - za * baseB);
    ya = temp;
    temp = ya + zb * baseA;
    yb += -(temp - ya - zb * baseA);
    ya = temp;
    temp = ya + zb * baseB;
    yb += -(temp - ya - zb * baseB);
    ya = temp;
    temp = ya + baseA;
    yb += -(temp - baseA - ya);
    ya = temp;
    temp = ya + za;
    yb += -(temp - ya - za);
    ya = temp;
    temp = ya + baseB;
    yb += -(temp - ya - baseB);
    ya = temp;
    temp = ya + zb;
    yb += -(temp - ya - zb);
    ya = temp;
    if(negative) {
      double denom = 1.0D + ya;
      double denomr = 1.0D / denom;
      double denomb = -(denom - 1.0D - ya) + yb;
      double ratio = ya * denomr;
      temp = ratio * HEX_40000000;
      final double ra = ratio + temp - temp;
      double rb = ratio - ra;
      temp = denom * HEX_40000000;
      za = denom + temp - temp;
      zb = denom - za;
      rb += (ya - za * ra - za * rb - zb * ra - zb * rb) * denomr;
      rb += yb * denomr;
      rb += -ya * denomb * denomr * denomr;
      ya = -ra;
      yb = -rb;
    }
    if(hiPrecOut != null) {
      hiPrecOut[0] = ya;
      hiPrecOut[1] = yb;
    }
    return ya + yb;
  }
  public static double floor(double x) {
    long y;
    if(x != x) {
      return x;
    }
    if(x >= TWO_POWER_52 || x <= -TWO_POWER_52) {
      return x;
    }
    y = (long)x;
    if(x < 0 && y != x) {
      y--;
    }
    if(y == 0) {
      return x * y;
    }
    return y;
  }
  public static double hypot(final double x, final double y) {
    if(Double.isInfinite(x) || Double.isInfinite(y)) {
      return Double.POSITIVE_INFINITY;
    }
    else 
      if(Double.isNaN(x) || Double.isNaN(y)) {
        return Double.NaN;
      }
      else {
        final int expX = getExponent(x);
        final int expY = getExponent(y);
        if(expX > expY + 27) {
          return abs(x);
        }
        else 
          if(expY > expX + 27) {
            return abs(y);
          }
          else {
            final int middleExp = (expX + expY) / 2;
            final double scaledX = scalb(x, -middleExp);
            final double scaledY = scalb(y, -middleExp);
            final double scaledH = sqrt(scaledX * scaledX + scaledY * scaledY);
            return scalb(scaledH, middleExp);
          }
      }
  }
  public static double log(final double x) {
    return log(x, null);
  }
  public static double log(double base, double x) {
    return log(x) / log(base);
  }
  private static double log(final double x, final double[] hiPrec) {
    if(x == 0) {
      return Double.NEGATIVE_INFINITY;
    }
    long bits = Double.doubleToRawLongBits(x);
    if(((bits & 0x8000000000000000L) != 0 || x != x) && x != 0.0D) {
      if(hiPrec != null) {
        hiPrec[0] = Double.NaN;
      }
      return Double.NaN;
    }
    if(x == Double.POSITIVE_INFINITY) {
      if(hiPrec != null) {
        hiPrec[0] = Double.POSITIVE_INFINITY;
      }
      return Double.POSITIVE_INFINITY;
    }
    int exp = (int)(bits >> 52) - 1023;
    if((bits & 0x7ff0000000000000L) == 0) {
      if(x == 0) {
        if(hiPrec != null) {
          hiPrec[0] = Double.NEGATIVE_INFINITY;
        }
        return Double.NEGATIVE_INFINITY;
      }
      bits <<= 1;
      while((bits & 0x0010000000000000L) == 0){
        --exp;
        bits <<= 1;
      }
    }
    if((exp == -1 || exp == 0) && x < 1.01D && x > 0.99D && hiPrec == null) {
      double xa = x - 1.0D;
      double xb = xa - x + 1.0D;
      double tmp = xa * HEX_40000000;
      double aa = xa + tmp - tmp;
      double ab = xa - aa;
      xa = aa;
      xb = ab;
      final double[] lnCoef_last = LN_QUICK_COEF[LN_QUICK_COEF.length - 1];
      double ya = lnCoef_last[0];
      double yb = lnCoef_last[1];
      for(int i = LN_QUICK_COEF.length - 2; i >= 0; i--) {
        aa = ya * xa;
        ab = ya * xb + yb * xa + yb * xb;
        tmp = aa * HEX_40000000;
        ya = aa + tmp - tmp;
        yb = aa - ya + ab;
        final double[] lnCoef_i = LN_QUICK_COEF[i];
        aa = ya + lnCoef_i[0];
        ab = yb + lnCoef_i[1];
        tmp = aa * HEX_40000000;
        ya = aa + tmp - tmp;
        yb = aa - ya + ab;
      }
      aa = ya * xa;
      ab = ya * xb + yb * xa + yb * xb;
      tmp = aa * HEX_40000000;
      ya = aa + tmp - tmp;
      yb = aa - ya + ab;
      return ya + yb;
    }
    final double[] lnm = lnMant.LN_MANT[(int)((bits & 0x000ffc0000000000L) >> 42)];
    final double epsilon = (bits & 0x3ffffffffffL) / (TWO_POWER_52 + (bits & 0x000ffc0000000000L));
    double lnza = 0.0D;
    double lnzb = 0.0D;
    if(hiPrec != null) {
      double tmp = epsilon * HEX_40000000;
      double aa = epsilon + tmp - tmp;
      double ab = epsilon - aa;
      double xa = aa;
      double xb = ab;
      final double numer = bits & 0x3ffffffffffL;
      final double denom = TWO_POWER_52 + (bits & 0x000ffc0000000000L);
      aa = numer - xa * denom - xb * denom;
      xb += aa / denom;
      final double[] lnCoef_last = LN_HI_PREC_COEF[LN_HI_PREC_COEF.length - 1];
      double ya = lnCoef_last[0];
      double yb = lnCoef_last[1];
      for(int i = LN_HI_PREC_COEF.length - 2; i >= 0; i--) {
        aa = ya * xa;
        ab = ya * xb + yb * xa + yb * xb;
        tmp = aa * HEX_40000000;
        ya = aa + tmp - tmp;
        yb = aa - ya + ab;
        final double[] lnCoef_i = LN_HI_PREC_COEF[i];
        aa = ya + lnCoef_i[0];
        ab = yb + lnCoef_i[1];
        tmp = aa * HEX_40000000;
        ya = aa + tmp - tmp;
        yb = aa - ya + ab;
      }
      aa = ya * xa;
      ab = ya * xb + yb * xa + yb * xb;
      lnza = aa + ab;
      lnzb = -(lnza - aa - ab);
    }
    else {
      lnza = -0.16624882440418567D;
      lnza = lnza * epsilon + 0.19999954120254515D;
      lnza = lnza * epsilon + -0.2499999997677497D;
      lnza = lnza * epsilon + 0.3333333333332802D;
      lnza = lnza * epsilon + -0.5D;
      lnza = lnza * epsilon + 1.0D;
      lnza = lnza * epsilon;
    }
    double a = LN_2_A * exp;
    double b = 0.0D;
    double c = a + lnm[0];
    double d = -(c - a - lnm[0]);
    a = c;
    b = b + d;
    c = a + lnza;
    d = -(c - a - lnza);
    a = c;
    b = b + d;
    c = a + LN_2_B * exp;
    d = -(c - a - LN_2_B * exp);
    a = c;
    b = b + d;
    c = a + lnm[1];
    d = -(c - a - lnm[1]);
    a = c;
    b = b + d;
    c = a + lnzb;
    d = -(c - a - lnzb);
    a = c;
    b = b + d;
    if(hiPrec != null) {
      hiPrec[0] = a;
      hiPrec[1] = b;
    }
    return a + b;
  }
  public static double log10(final double x) {
    final double[] hiPrec = new double[2];
    final double lores = log(x, hiPrec);
    if(Double.isInfinite(lores)) {
      return lores;
    }
    final double tmp = hiPrec[0] * HEX_40000000;
    final double lna = hiPrec[0] + tmp - tmp;
    final double lnb = hiPrec[0] - lna + hiPrec[1];
    final double rln10a = 0.4342944622039795D;
    final double rln10b = 1.9699272335463627E-8D;
    return rln10b * lnb + rln10b * lna + rln10a * lnb + rln10a * lna;
  }
  public static double log1p(final double x) {
    if(x == -1) {
      return Double.NEGATIVE_INFINITY;
    }
    if(x == Double.POSITIVE_INFINITY) {
      return Double.POSITIVE_INFINITY;
    }
    if(x > 1e-6D || x < -1e-6D) {
      final double xpa = 1 + x;
      final double xpb = -(xpa - 1 - x);
      final double[] hiPrec = new double[2];
      final double lores = log(xpa, hiPrec);
      if(Double.isInfinite(lores)) {
        return lores;
      }
      final double fx1 = xpb / xpa;
      final double epsilon = 0.5D * fx1 + 1;
      return epsilon * fx1 + hiPrec[1] + hiPrec[0];
    }
    else {
      final double y = (x * F_1_3 - F_1_2) * x + 1;
      return y * x;
    }
  }
  public static double max(final double a, final double b) {
    if(a > b) {
      return a;
    }
    if(a < b) {
      return b;
    }
    if(a != b) {
      return Double.NaN;
    }
    long bits = Double.doubleToRawLongBits(a);
    if(bits == 0x8000000000000000L) {
      return b;
    }
    return a;
  }
  public static double min(final double a, final double b) {
    if(a > b) {
      return b;
    }
    if(a < b) {
      return a;
    }
    if(a != b) {
      return Double.NaN;
    }
    long bits = Double.doubleToRawLongBits(a);
    if(bits == 0x8000000000000000L) {
      return a;
    }
    return b;
  }
  public static double nextAfter(double d, double direction) {
    if(Double.isNaN(d) || Double.isNaN(direction)) {
      return Double.NaN;
    }
    else 
      if(d == direction) {
        return direction;
      }
      else 
        if(Double.isInfinite(d)) {
          return (d < 0) ? -Double.MAX_VALUE : Double.MAX_VALUE;
        }
        else 
          if(d == 0) {
            return (direction < 0) ? -Double.MIN_VALUE : Double.MIN_VALUE;
          }
    final long bits = Double.doubleToRawLongBits(d);
    final long sign = bits & 0x8000000000000000L;
    if((direction < d) ^ (sign == 0L)) {
      return Double.longBitsToDouble(sign | ((bits & 0x7fffffffffffffffL) + 1));
    }
    else {
      return Double.longBitsToDouble(sign | ((bits & 0x7fffffffffffffffL) - 1));
    }
  }
  public static double nextUp(final double a) {
    return nextAfter(a, Double.POSITIVE_INFINITY);
  }
  private static double polyCosine(double x) {
    double x2 = x * x;
    double p = 2.479773539153719E-5D;
    p = p * x2 + -0.0013888888689039883D;
    p = p * x2 + 0.041666666666621166D;
    p = p * x2 + -0.49999999999999994D;
    p *= x2;
    return p;
  }
  private static double polySine(final double x) {
    double x2 = x * x;
    double p = 2.7553817452272217E-6D;
    p = p * x2 + -1.9841269659586505E-4D;
    p = p * x2 + 0.008333333333329196D;
    p = p * x2 + -0.16666666666666666D;
    p = p * x2 * x;
    return p;
  }
  public static double pow(double x, double y) {
    final double[] lns = new double[2];
    if(y == 0.0D) {
      return 1.0D;
    }
    if(x != x) {
      return x;
    }
    if(x == 0) {
      long bits = Double.doubleToRawLongBits(x);
      if((bits & 0x8000000000000000L) != 0) {
        long yi = (long)y;
        if(y < 0 && y == yi && (yi & 1) == 1) {
          return Double.NEGATIVE_INFINITY;
        }
        if(y > 0 && y == yi && (yi & 1) == 1) {
          return -0.0D;
        }
      }
      if(y < 0) {
        return Double.POSITIVE_INFINITY;
      }
      if(y > 0) {
        return 0.0D;
      }
      return Double.NaN;
    }
    if(x == Double.POSITIVE_INFINITY) {
      if(y != y) {
        return y;
      }
      if(y < 0.0D) {
        return 0.0D;
      }
      else {
        return Double.POSITIVE_INFINITY;
      }
    }
    if(y == Double.POSITIVE_INFINITY) {
      if(x * x == 1.0D) {
        return Double.NaN;
      }
      if(x * x > 1.0D) {
        return Double.POSITIVE_INFINITY;
      }
      else {
        return 0.0D;
      }
    }
    if(x == Double.NEGATIVE_INFINITY) {
      if(y != y) {
        return y;
      }
      if(y < 0) {
        long yi = (long)y;
        if(y == yi && (yi & 1) == 1) {
          return -0.0D;
        }
        return 0.0D;
      }
      if(y > 0) {
        long yi = (long)y;
        if(y == yi && (yi & 1) == 1) {
          return Double.NEGATIVE_INFINITY;
        }
        return Double.POSITIVE_INFINITY;
      }
    }
    if(y == Double.NEGATIVE_INFINITY) {
      if(x * x == 1.0D) {
        return Double.NaN;
      }
      if(x * x < 1.0D) {
        return Double.POSITIVE_INFINITY;
      }
      else {
        return 0.0D;
      }
    }
    if(x < 0) {
      if(y >= TWO_POWER_53 || y <= -TWO_POWER_53) {
        return pow(-x, y);
      }
      if(y == (long)y) {
        return ((long)y & 1) == 0 ? pow(-x, y) : -pow(-x, y);
      }
      else {
        return Double.NaN;
      }
    }
    double ya;
    double yb;
    if(y < 8e298D && y > -8e298D) {
      double tmp1 = y * HEX_40000000;
      ya = y + tmp1 - tmp1;
      yb = y - ya;
    }
    else {
      double tmp1 = y * 9.31322574615478515625E-10D;
      double tmp2 = tmp1 * 9.31322574615478515625E-10D;
      ya = (tmp1 + tmp2 - tmp1) * HEX_40000000 * HEX_40000000;
      yb = y - ya;
    }
    final double lores = log(x, lns);
    if(Double.isInfinite(lores)) {
      return lores;
    }
    double lna = lns[0];
    double lnb = lns[1];
    double tmp1 = lna * HEX_40000000;
    double tmp2 = lna + tmp1 - tmp1;
    lnb += lna - tmp2;
    lna = tmp2;
    final double aa = lna * ya;
    final double ab = lna * yb + lnb * ya + lnb * yb;
    lna = aa + ab;
    lnb = -(lna - aa - ab);
    double z = 1.0D / 120.0D;
    z = z * lnb + (1.0D / 24.0D);
    z = z * lnb + (1.0D / 6.0D);
    z = z * lnb + 0.5D;
    z = z * lnb + 1.0D;
    z = z * lnb;
    final double result = exp(lna, z, null);
    return result;
  }
  public static double pow(double d, int e) {
    if(e == 0) {
      return 1.0D;
    }
    else 
      if(e < 0) {
        e = -e;
        d = 1.0D / d;
      }
    final int splitFactor = 0x8000001;
    final double cd = splitFactor * d;
    final double d1High = cd - (cd - d);
    final double d1Low = d - d1High;
    double resultHigh = 1;
    double resultLow = 0;
    double d2p = d;
    double d2pHigh = d1High;
    double d2pLow = d1Low;
    while(e != 0){
      if((e & 0x1) != 0) {
        final double tmpHigh = resultHigh * d2p;
        final double cRH = splitFactor * resultHigh;
        final double rHH = cRH - (cRH - resultHigh);
        final double rHL = resultHigh - rHH;
        final double tmpLow = rHL * d2pLow - (((tmpHigh - rHH * d2pHigh) - rHL * d2pHigh) - rHH * d2pLow);
        resultHigh = tmpHigh;
        resultLow = resultLow * d2p + tmpLow;
      }
      final double tmpHigh = d2pHigh * d2p;
      final double cD2pH = splitFactor * d2pHigh;
      final double d2pHH = cD2pH - (cD2pH - d2pHigh);
      final double d2pHL = d2pHigh - d2pHH;
      final double tmpLow = d2pHL * d2pLow - (((tmpHigh - d2pHH * d2pHigh) - d2pHL * d2pHigh) - d2pHH * d2pLow);
      final double cTmpH = splitFactor * tmpHigh;
      d2pHigh = cTmpH - (cTmpH - tmpHigh);
      d2pLow = d2pLow * d2p + tmpLow + (tmpHigh - d2pHigh);
      d2p = d2pHigh + d2pLow;
      e = e >> 1;
    }
    return resultHigh + resultLow;
  }
  public static double random() {
    return Math.random();
  }
  public static double rint(double x) {
    double y = floor(x);
    double d = x - y;
    if(d > 0.5D) {
      if(y == -1.0D) {
        return -0.0D;
      }
      return y + 1.0D;
    }
    if(d < 0.5D) {
      return y;
    }
    long z = (long)y;
    return (z & 1) == 0 ? y : y + 1.0D;
  }
  public static double scalb(final double d, final int n) {
    if((n > -1023) && (n < 1024)) {
      return d * Double.longBitsToDouble(((long)(n + 1023)) << 52);
    }
    if(Double.isNaN(d) || Double.isInfinite(d) || (d == 0)) {
      return d;
    }
    if(n < -2098) {
      return (d > 0) ? 0.0D : -0.0D;
    }
    if(n > 2097) {
      return (d > 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
    }
    final long bits = Double.doubleToRawLongBits(d);
    final long sign = bits & 0x8000000000000000L;
    int exponent = ((int)(bits >>> 52)) & 0x7ff;
    long mantissa = bits & 0x000fffffffffffffL;
    int scaledExponent = exponent + n;
    if(n < 0) {
      if(scaledExponent > 0) {
        return Double.longBitsToDouble(sign | (((long)scaledExponent) << 52) | mantissa);
      }
      else 
        if(scaledExponent > -53) {
          mantissa = mantissa | (1L << 52);
          final long mostSignificantLostBit = mantissa & (1L << (-scaledExponent));
          mantissa = mantissa >>> (1 - scaledExponent);
          if(mostSignificantLostBit != 0) {
            mantissa++;
          }
          return Double.longBitsToDouble(sign | mantissa);
        }
        else {
          return (sign == 0L) ? 0.0D : -0.0D;
        }
    }
    else {
      if(exponent == 0) {
        while((mantissa >>> 52) != 1){
          mantissa = mantissa << 1;
          --scaledExponent;
        }
        ++scaledExponent;
        mantissa = mantissa & 0x000fffffffffffffL;
        if(scaledExponent < 2047) {
          return Double.longBitsToDouble(sign | (((long)scaledExponent) << 52) | mantissa);
        }
        else {
          return (sign == 0L) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
      }
      else 
        if(scaledExponent < 2047) {
          return Double.longBitsToDouble(sign | (((long)scaledExponent) << 52) | mantissa);
        }
        else {
          return (sign == 0L) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
    }
  }
  public static double signum(final double a) {
    return (a < 0.0D) ? -1.0D : ((a > 0.0D) ? 1.0D : a);
  }
  public static double sin(double x) {
    boolean negative = false;
    int quadrant = 0;
    double xa;
    double xb = 0.0D;
    xa = x;
    if(x < 0) {
      negative = true;
      xa = -xa;
    }
    if(xa == 0.0D) {
      long bits = Double.doubleToRawLongBits(x);
      if(bits < 0) {
        return -0.0D;
      }
      return 0.0D;
    }
    if(xa != xa || xa == Double.POSITIVE_INFINITY) {
      return Double.NaN;
    }
    if(xa > 3294198.0D) {
      double[] reduceResults = new double[3];
      reducePayneHanek(xa, reduceResults);
      quadrant = ((int)reduceResults[0]) & 3;
      xa = reduceResults[1];
      xb = reduceResults[2];
    }
    else 
      if(xa > 1.5707963267948966D) {
        final CodyWaite cw = new CodyWaite(xa);
        quadrant = cw.getK() & 3;
        xa = cw.getRemA();
        xb = cw.getRemB();
      }
    if(negative) {
      quadrant ^= 2;
    }
    switch (quadrant){
      case 0:
      return sinQ(xa, xb);
      case 1:
      double var_4328 = cosQ(xa, xb);
      return var_4328;
      case 2:
      return -sinQ(xa, xb);
      case 3:
      return -cosQ(xa, xb);
      default:
      return Double.NaN;
    }
  }
  private static double sinQ(double xa, double xb) {
    int idx = (int)((xa * 8.0D) + 0.5D);
    final double epsilon = xa - EIGHTHS[idx];
    final double sintA = SINE_TABLE_A[idx];
    final double sintB = SINE_TABLE_B[idx];
    final double costA = COSINE_TABLE_A[idx];
    final double costB = COSINE_TABLE_B[idx];
    double sinEpsA = epsilon;
    double sinEpsB = polySine(epsilon);
    final double cosEpsA = 1.0D;
    final double cosEpsB = polyCosine(epsilon);
    final double temp = sinEpsA * HEX_40000000;
    double temp2 = (sinEpsA + temp) - temp;
    sinEpsB += sinEpsA - temp2;
    sinEpsA = temp2;
    double result;
    double a = 0;
    double b = 0;
    double t = sintA;
    double c = a + t;
    double d = -(c - a - t);
    a = c;
    b = b + d;
    t = costA * sinEpsA;
    c = a + t;
    d = -(c - a - t);
    a = c;
    b = b + d;
    b = b + sintA * cosEpsB + costA * sinEpsB;
    b = b + sintB + costB * sinEpsA + sintB * cosEpsB + costB * sinEpsB;
    if(xb != 0.0D) {
      t = ((costA + costB) * (cosEpsA + cosEpsB) - (sintA + sintB) * (sinEpsA + sinEpsB)) * xb;
      c = a + t;
      d = -(c - a - t);
      a = c;
      b = b + d;
    }
    result = a + b;
    return result;
  }
  public static double sinh(double x) {
    boolean negate = false;
    if(x != x) {
      return x;
    }
    if(x > 20) {
      if(x >= LOG_MAX_VALUE) {
        final double t = exp(0.5D * x);
        return (0.5D * t) * t;
      }
      else {
        return 0.5D * exp(x);
      }
    }
    else 
      if(x < -20) {
        if(x <= -LOG_MAX_VALUE) {
          final double t = exp(-0.5D * x);
          return (-0.5D * t) * t;
        }
        else {
          return -0.5D * exp(-x);
        }
      }
    if(x == 0) {
      return x;
    }
    if(x < 0.0D) {
      x = -x;
      negate = true;
    }
    double result;
    if(x > 0.25D) {
      double[] hiPrec = new double[2];
      exp(x, 0.0D, hiPrec);
      double ya = hiPrec[0] + hiPrec[1];
      double yb = -(ya - hiPrec[0] - hiPrec[1]);
      double temp = ya * HEX_40000000;
      double yaa = ya + temp - temp;
      double yab = ya - yaa;
      double recip = 1.0D / ya;
      temp = recip * HEX_40000000;
      double recipa = recip + temp - temp;
      double recipb = recip - recipa;
      recipb += (1.0D - yaa * recipa - yaa * recipb - yab * recipa - yab * recipb) * recip;
      recipb += -yb * recip * recip;
      recipa = -recipa;
      recipb = -recipb;
      temp = ya + recipa;
      yb += -(temp - ya - recipa);
      ya = temp;
      temp = ya + recipb;
      yb += -(temp - ya - recipb);
      ya = temp;
      result = ya + yb;
      result *= 0.5D;
    }
    else {
      double[] hiPrec = new double[2];
      expm1(x, hiPrec);
      double ya = hiPrec[0] + hiPrec[1];
      double yb = -(ya - hiPrec[0] - hiPrec[1]);
      double denom = 1.0D + ya;
      double denomr = 1.0D / denom;
      double denomb = -(denom - 1.0D - ya) + yb;
      double ratio = ya * denomr;
      double temp = ratio * HEX_40000000;
      double ra = ratio + temp - temp;
      double rb = ratio - ra;
      temp = denom * HEX_40000000;
      double za = denom + temp - temp;
      double zb = denom - za;
      rb += (ya - za * ra - za * rb - zb * ra - zb * rb) * denomr;
      rb += yb * denomr;
      rb += -ya * denomb * denomr * denomr;
      temp = ya + ra;
      yb += -(temp - ya - ra);
      ya = temp;
      temp = ya + rb;
      yb += -(temp - ya - rb);
      ya = temp;
      result = ya + yb;
      result *= 0.5D;
    }
    if(negate) {
      result = -result;
    }
    return result;
  }
  public static double sqrt(final double a) {
    return Math.sqrt(a);
  }
  public static double tan(double x) {
    boolean negative = false;
    int quadrant = 0;
    double xa = x;
    if(x < 0) {
      negative = true;
      xa = -xa;
    }
    if(xa == 0.0D) {
      long bits = Double.doubleToRawLongBits(x);
      if(bits < 0) {
        return -0.0D;
      }
      return 0.0D;
    }
    if(xa != xa || xa == Double.POSITIVE_INFINITY) {
      return Double.NaN;
    }
    double xb = 0;
    if(xa > 3294198.0D) {
      double[] reduceResults = new double[3];
      reducePayneHanek(xa, reduceResults);
      quadrant = ((int)reduceResults[0]) & 3;
      xa = reduceResults[1];
      xb = reduceResults[2];
    }
    else 
      if(xa > 1.5707963267948966D) {
        final CodyWaite cw = new CodyWaite(xa);
        quadrant = cw.getK() & 3;
        xa = cw.getRemA();
        xb = cw.getRemB();
      }
    if(xa > 1.5D) {
      final double pi2a = 1.5707963267948966D;
      final double pi2b = 6.123233995736766E-17D;
      final double a = pi2a - xa;
      double b = -(a - pi2a + xa);
      b += pi2b - xb;
      xa = a + b;
      xb = -(xa - a - b);
      quadrant ^= 1;
      negative ^= true;
    }
    double result;
    if((quadrant & 1) == 0) {
      result = tanQ(xa, xb, false);
    }
    else {
      result = -tanQ(xa, xb, true);
    }
    if(negative) {
      result = -result;
    }
    return result;
  }
  private static double tanQ(double xa, double xb, boolean cotanFlag) {
    int idx = (int)((xa * 8.0D) + 0.5D);
    final double epsilon = xa - EIGHTHS[idx];
    final double sintA = SINE_TABLE_A[idx];
    final double sintB = SINE_TABLE_B[idx];
    final double costA = COSINE_TABLE_A[idx];
    final double costB = COSINE_TABLE_B[idx];
    double sinEpsA = epsilon;
    double sinEpsB = polySine(epsilon);
    final double cosEpsA = 1.0D;
    final double cosEpsB = polyCosine(epsilon);
    double temp = sinEpsA * HEX_40000000;
    double temp2 = (sinEpsA + temp) - temp;
    sinEpsB += sinEpsA - temp2;
    sinEpsA = temp2;
    double a = 0;
    double b = 0;
    double t = sintA;
    double c = a + t;
    double d = -(c - a - t);
    a = c;
    b = b + d;
    t = costA * sinEpsA;
    c = a + t;
    d = -(c - a - t);
    a = c;
    b = b + d;
    b = b + sintA * cosEpsB + costA * sinEpsB;
    b = b + sintB + costB * sinEpsA + sintB * cosEpsB + costB * sinEpsB;
    double sina = a + b;
    double sinb = -(sina - a - b);
    a = b = c = d = 0.0D;
    t = costA * cosEpsA;
    c = a + t;
    d = -(c - a - t);
    a = c;
    b = b + d;
    t = -sintA * sinEpsA;
    c = a + t;
    d = -(c - a - t);
    a = c;
    b = b + d;
    b = b + costB * cosEpsA + costA * cosEpsB + costB * cosEpsB;
    b = b - (sintB * sinEpsA + sintA * sinEpsB + sintB * sinEpsB);
    double cosa = a + b;
    double cosb = -(cosa - a - b);
    if(cotanFlag) {
      double tmp;
      tmp = cosa;
      cosa = sina;
      sina = tmp;
      tmp = cosb;
      cosb = sinb;
      sinb = tmp;
    }
    double est = sina / cosa;
    temp = est * HEX_40000000;
    double esta = (est + temp) - temp;
    double estb = est - esta;
    temp = cosa * HEX_40000000;
    double cosaa = (cosa + temp) - temp;
    double cosab = cosa - cosaa;
    double err = (sina - esta * cosaa - esta * cosab - estb * cosaa - estb * cosab) / cosa;
    err += sinb / cosa;
    err += -sina * cosb / cosa / cosa;
    if(xb != 0.0D) {
      double xbadj = xb + est * est * xb;
      if(cotanFlag) {
        xbadj = -xbadj;
      }
      err += xbadj;
    }
    return est + err;
  }
  public static double tanh(double x) {
    boolean negate = false;
    if(x != x) {
      return x;
    }
    if(x > 20.0D) {
      return 1.0D;
    }
    if(x < -20) {
      return -1.0D;
    }
    if(x == 0) {
      return x;
    }
    if(x < 0.0D) {
      x = -x;
      negate = true;
    }
    double result;
    if(x >= 0.5D) {
      double[] hiPrec = new double[2];
      exp(x * 2.0D, 0.0D, hiPrec);
      double ya = hiPrec[0] + hiPrec[1];
      double yb = -(ya - hiPrec[0] - hiPrec[1]);
      double na = -1.0D + ya;
      double nb = -(na + 1.0D - ya);
      double temp = na + yb;
      nb += -(temp - na - yb);
      na = temp;
      double da = 1.0D + ya;
      double db = -(da - 1.0D - ya);
      temp = da + yb;
      db += -(temp - da - yb);
      da = temp;
      temp = da * HEX_40000000;
      double daa = da + temp - temp;
      double dab = da - daa;
      double ratio = na / da;
      temp = ratio * HEX_40000000;
      double ratioa = ratio + temp - temp;
      double ratiob = ratio - ratioa;
      ratiob += (na - daa * ratioa - daa * ratiob - dab * ratioa - dab * ratiob) / da;
      ratiob += nb / da;
      ratiob += -db * na / da / da;
      result = ratioa + ratiob;
    }
    else {
      double[] hiPrec = new double[2];
      expm1(x * 2.0D, hiPrec);
      double ya = hiPrec[0] + hiPrec[1];
      double yb = -(ya - hiPrec[0] - hiPrec[1]);
      double na = ya;
      double nb = yb;
      double da = 2.0D + ya;
      double db = -(da - 2.0D - ya);
      double temp = da + yb;
      db += -(temp - da - yb);
      da = temp;
      temp = da * HEX_40000000;
      double daa = da + temp - temp;
      double dab = da - daa;
      double ratio = na / da;
      temp = ratio * HEX_40000000;
      double ratioa = ratio + temp - temp;
      double ratiob = ratio - ratioa;
      ratiob += (na - daa * ratioa - daa * ratiob - dab * ratioa - dab * ratiob) / da;
      ratiob += nb / da;
      ratiob += -db * na / da / da;
      result = ratioa + ratiob;
    }
    if(negate) {
      result = -result;
    }
    return result;
  }
  public static double toDegrees(double x) {
    if(Double.isInfinite(x) || x == 0.0D) {
      return x;
    }
    final double facta = 57.2957763671875D;
    final double factb = 3.145894820876798E-6D;
    double xa = doubleHighPart(x);
    double xb = x - xa;
    return xb * factb + xb * facta + xa * factb + xa * facta;
  }
  public static double toRadians(double x) {
    if(Double.isInfinite(x) || x == 0.0D) {
      return x;
    }
    final double facta = 0.01745329052209854D;
    final double factb = 1.997844754509471E-9D;
    double xa = doubleHighPart(x);
    double xb = x - xa;
    double result = xb * factb + xb * facta + xa * factb + xa * facta;
    if(result == 0) {
      result = result * x;
    }
    return result;
  }
  public static double ulp(double x) {
    if(Double.isInfinite(x)) {
      return Double.POSITIVE_INFINITY;
    }
    return abs(x - Double.longBitsToDouble(Double.doubleToRawLongBits(x) ^ 1));
  }
  public static float abs(final float x) {
    return Float.intBitsToFloat(MASK_NON_SIGN_INT & Float.floatToRawIntBits(x));
  }
  public static float copySign(float magnitude, float sign) {
    final int m = Float.floatToRawIntBits(magnitude);
    final int s = Float.floatToRawIntBits(sign);
    if((m ^ s) >= 0) {
      return magnitude;
    }
    return -magnitude;
  }
  public static float max(final float a, final float b) {
    if(a > b) {
      return a;
    }
    if(a < b) {
      return b;
    }
    if(a != b) {
      return Float.NaN;
    }
    int bits = Float.floatToRawIntBits(a);
    if(bits == 0x80000000) {
      return b;
    }
    return a;
  }
  public static float min(final float a, final float b) {
    if(a > b) {
      return b;
    }
    if(a < b) {
      return a;
    }
    if(a != b) {
      return Float.NaN;
    }
    int bits = Float.floatToRawIntBits(a);
    if(bits == 0x80000000) {
      return a;
    }
    return b;
  }
  public static float nextAfter(final float f, final double direction) {
    if(Double.isNaN(f) || Double.isNaN(direction)) {
      return Float.NaN;
    }
    else 
      if(f == direction) {
        return (float)direction;
      }
      else 
        if(Float.isInfinite(f)) {
          return (f < 0F) ? -Float.MAX_VALUE : Float.MAX_VALUE;
        }
        else 
          if(f == 0F) {
            return (direction < 0) ? -Float.MIN_VALUE : Float.MIN_VALUE;
          }
    final int bits = Float.floatToIntBits(f);
    final int sign = bits & 0x80000000;
    if((direction < f) ^ (sign == 0)) {
      return Float.intBitsToFloat(sign | ((bits & 0x7fffffff) + 1));
    }
    else {
      return Float.intBitsToFloat(sign | ((bits & 0x7fffffff) - 1));
    }
  }
  public static float nextUp(final float a) {
    return nextAfter(a, Float.POSITIVE_INFINITY);
  }
  public static float scalb(final float f, final int n) {
    if((n > -127) && (n < 128)) {
      return f * Float.intBitsToFloat((n + 127) << 23);
    }
    if(Float.isNaN(f) || Float.isInfinite(f) || (f == 0F)) {
      return f;
    }
    if(n < -277) {
      return (f > 0) ? 0.0F : -0.0F;
    }
    if(n > 276) {
      return (f > 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
    }
    final int bits = Float.floatToIntBits(f);
    final int sign = bits & 0x80000000;
    int exponent = (bits >>> 23) & 0xff;
    int mantissa = bits & 0x007fffff;
    int scaledExponent = exponent + n;
    if(n < 0) {
      if(scaledExponent > 0) {
        return Float.intBitsToFloat(sign | (scaledExponent << 23) | mantissa);
      }
      else 
        if(scaledExponent > -24) {
          mantissa = mantissa | (1 << 23);
          final int mostSignificantLostBit = mantissa & (1 << (-scaledExponent));
          mantissa = mantissa >>> (1 - scaledExponent);
          if(mostSignificantLostBit != 0) {
            mantissa++;
          }
          return Float.intBitsToFloat(sign | mantissa);
        }
        else {
          return (sign == 0) ? 0.0F : -0.0F;
        }
    }
    else {
      if(exponent == 0) {
        while((mantissa >>> 23) != 1){
          mantissa = mantissa << 1;
          --scaledExponent;
        }
        ++scaledExponent;
        mantissa = mantissa & 0x007fffff;
        if(scaledExponent < 255) {
          return Float.intBitsToFloat(sign | (scaledExponent << 23) | mantissa);
        }
        else {
          return (sign == 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }
      }
      else 
        if(scaledExponent < 255) {
          return Float.intBitsToFloat(sign | (scaledExponent << 23) | mantissa);
        }
        else {
          return (sign == 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }
    }
  }
  public static float signum(final float a) {
    return (a < 0.0F) ? -1.0F : ((a > 0.0F) ? 1.0F : a);
  }
  public static float ulp(float x) {
    if(Float.isInfinite(x)) {
      return Float.POSITIVE_INFINITY;
    }
    return abs(x - Float.intBitsToFloat(Float.floatToIntBits(x) ^ 1));
  }
  public static int abs(final int x) {
    final int i = x >>> 31;
    return (x ^ (~i + 1)) + i;
  }
  public static int getExponent(final double d) {
    return (int)((Double.doubleToRawLongBits(d) >>> 52) & 0x7ff) - 1023;
  }
  public static int getExponent(final float f) {
    return ((Float.floatToRawIntBits(f) >>> 23) & 0xff) - 127;
  }
  public static int max(final int a, final int b) {
    return (a <= b) ? b : a;
  }
  public static int min(final int a, final int b) {
    return (a <= b) ? a : b;
  }
  public static int round(final float x) {
    return (int)floor(x + 0.5F);
  }
  public static long abs(final long x) {
    final long l = x >>> 63;
    return (x ^ (~l + 1)) + l;
  }
  public static long max(final long a, final long b) {
    return (a <= b) ? b : a;
  }
  public static long min(final long a, final long b) {
    return (a <= b) ? a : b;
  }
  public static long round(double x) {
    return (long)floor(x + 0.5D);
  }
  public static void main(String[] a) {
    PrintStream out = System.out;
    FastMathCalc.printarray(out, "EXP_INT_TABLE_A", EXP_INT_TABLE_LEN, ExpIntTable.EXP_INT_TABLE_A);
    FastMathCalc.printarray(out, "EXP_INT_TABLE_B", EXP_INT_TABLE_LEN, ExpIntTable.EXP_INT_TABLE_B);
    FastMathCalc.printarray(out, "EXP_FRAC_TABLE_A", EXP_FRAC_TABLE_LEN, ExpFracTable.EXP_FRAC_TABLE_A);
    FastMathCalc.printarray(out, "EXP_FRAC_TABLE_B", EXP_FRAC_TABLE_LEN, ExpFracTable.EXP_FRAC_TABLE_B);
    FastMathCalc.printarray(out, "LN_MANT", LN_MANT_LEN, lnMant.LN_MANT);
    FastMathCalc.printarray(out, "SINE_TABLE_A", SINE_TABLE_LEN, SINE_TABLE_A);
    FastMathCalc.printarray(out, "SINE_TABLE_B", SINE_TABLE_LEN, SINE_TABLE_B);
    FastMathCalc.printarray(out, "COSINE_TABLE_A", SINE_TABLE_LEN, COSINE_TABLE_A);
    FastMathCalc.printarray(out, "COSINE_TABLE_B", SINE_TABLE_LEN, COSINE_TABLE_B);
    FastMathCalc.printarray(out, "TANGENT_TABLE_A", SINE_TABLE_LEN, TANGENT_TABLE_A);
    FastMathCalc.printarray(out, "TANGENT_TABLE_B", SINE_TABLE_LEN, TANGENT_TABLE_B);
  }
  private static void reducePayneHanek(double x, double[] result) {
    long inbits = Double.doubleToRawLongBits(x);
    int exponent = (int)((inbits >> 52) & 0x7ff) - 1023;
    inbits &= 0x000fffffffffffffL;
    inbits |= 0x0010000000000000L;
    exponent++;
    inbits <<= 11;
    long shpi0;
    long shpiA;
    long shpiB;
    int idx = exponent >> 6;
    int shift = exponent - (idx << 6);
    if(shift != 0) {
      shpi0 = (idx == 0) ? 0 : (RECIP_2PI[idx - 1] << shift);
      shpi0 |= RECIP_2PI[idx] >>> (64 - shift);
      shpiA = (RECIP_2PI[idx] << shift) | (RECIP_2PI[idx + 1] >>> (64 - shift));
      shpiB = (RECIP_2PI[idx + 1] << shift) | (RECIP_2PI[idx + 2] >>> (64 - shift));
    }
    else {
      shpi0 = (idx == 0) ? 0 : RECIP_2PI[idx - 1];
      shpiA = RECIP_2PI[idx];
      shpiB = RECIP_2PI[idx + 1];
    }
    long a = inbits >>> 32;
    long b = inbits & 0xffffffffL;
    long c = shpiA >>> 32;
    long d = shpiA & 0xffffffffL;
    long ac = a * c;
    long bd = b * d;
    long bc = b * c;
    long ad = a * d;
    long prodB = bd + (ad << 32);
    long prodA = ac + (ad >>> 32);
    boolean bita = (bd & 0x8000000000000000L) != 0;
    boolean bitb = (ad & 0x80000000L) != 0;
    boolean bitsum = (prodB & 0x8000000000000000L) != 0;
    if((bita && bitb) || ((bita || bitb) && !bitsum)) {
      prodA++;
    }
    bita = (prodB & 0x8000000000000000L) != 0;
    bitb = (bc & 0x80000000L) != 0;
    prodB = prodB + (bc << 32);
    prodA = prodA + (bc >>> 32);
    bitsum = (prodB & 0x8000000000000000L) != 0;
    if((bita && bitb) || ((bita || bitb) && !bitsum)) {
      prodA++;
    }
    c = shpiB >>> 32;
    d = shpiB & 0xffffffffL;
    ac = a * c;
    bc = b * c;
    ad = a * d;
    ac = ac + ((bc + ad) >>> 32);
    bita = (prodB & 0x8000000000000000L) != 0;
    bitb = (ac & 0x8000000000000000L) != 0;
    prodB += ac;
    bitsum = (prodB & 0x8000000000000000L) != 0;
    if((bita && bitb) || ((bita || bitb) && !bitsum)) {
      prodA++;
    }
    c = shpi0 >>> 32;
    d = shpi0 & 0xffffffffL;
    bd = b * d;
    bc = b * c;
    ad = a * d;
    prodA += bd + ((bc + ad) << 32);
    int intPart = (int)(prodA >>> 62);
    prodA <<= 2;
    prodA |= prodB >>> 62;
    prodB <<= 2;
    a = prodA >>> 32;
    b = prodA & 0xffffffffL;
    c = PI_O_4_BITS[0] >>> 32;
    d = PI_O_4_BITS[0] & 0xffffffffL;
    ac = a * c;
    bd = b * d;
    bc = b * c;
    ad = a * d;
    long prod2B = bd + (ad << 32);
    long prod2A = ac + (ad >>> 32);
    bita = (bd & 0x8000000000000000L) != 0;
    bitb = (ad & 0x80000000L) != 0;
    bitsum = (prod2B & 0x8000000000000000L) != 0;
    if((bita && bitb) || ((bita || bitb) && !bitsum)) {
      prod2A++;
    }
    bita = (prod2B & 0x8000000000000000L) != 0;
    bitb = (bc & 0x80000000L) != 0;
    prod2B = prod2B + (bc << 32);
    prod2A = prod2A + (bc >>> 32);
    bitsum = (prod2B & 0x8000000000000000L) != 0;
    if((bita && bitb) || ((bita || bitb) && !bitsum)) {
      prod2A++;
    }
    c = PI_O_4_BITS[1] >>> 32;
    d = PI_O_4_BITS[1] & 0xffffffffL;
    ac = a * c;
    bc = b * c;
    ad = a * d;
    ac = ac + ((bc + ad) >>> 32);
    bita = (prod2B & 0x8000000000000000L) != 0;
    bitb = (ac & 0x8000000000000000L) != 0;
    prod2B += ac;
    bitsum = (prod2B & 0x8000000000000000L) != 0;
    if((bita && bitb) || ((bita || bitb) && !bitsum)) {
      prod2A++;
    }
    a = prodB >>> 32;
    b = prodB & 0xffffffffL;
    c = PI_O_4_BITS[0] >>> 32;
    d = PI_O_4_BITS[0] & 0xffffffffL;
    ac = a * c;
    bc = b * c;
    ad = a * d;
    ac = ac + ((bc + ad) >>> 32);
    bita = (prod2B & 0x8000000000000000L) != 0;
    bitb = (ac & 0x8000000000000000L) != 0;
    prod2B += ac;
    bitsum = (prod2B & 0x8000000000000000L) != 0;
    if((bita && bitb) || ((bita || bitb) && !bitsum)) {
      prod2A++;
    }
    double tmpA = (prod2A >>> 12) / TWO_POWER_52;
    double tmpB = (((prod2A & 0xfffL) << 40) + (prod2B >>> 24)) / TWO_POWER_52 / TWO_POWER_52;
    double sumA = tmpA + tmpB;
    double sumB = -(sumA - tmpA - tmpB);
    result[0] = intPart;
    result[1] = sumA * 2.0D;
    result[2] = sumB * 2.0D;
  }
  
  private static class CodyWaite  {
    final private int finalK;
    final private double finalRemA;
    final private double finalRemB;
    CodyWaite(double xa) {
      super();
      int k = (int)(xa * 0.6366197723675814D);
      double remA;
      double remB;
      while(true){
        double a = -k * 1.570796251296997D;
        remA = xa + a;
        remB = -(remA - xa - a);
        a = -k * 7.549789948768648E-8D;
        double b = remA;
        remA = a + b;
        remB += -(remA - b - a);
        a = -k * 6.123233995736766E-17D;
        b = remA;
        remA = a + b;
        remB += -(remA - b - a);
        if(remA > 0) {
          break ;
        }
        --k;
      }
      this.finalK = k;
      this.finalRemA = remA;
      this.finalRemB = remB;
    }
    double getRemA() {
      return finalRemA;
    }
    double getRemB() {
      return finalRemB;
    }
    int getK() {
      return finalK;
    }
  }
  
  private static class ExpFracTable  {
    final private static double[] EXP_FRAC_TABLE_A;
    final private static double[] EXP_FRAC_TABLE_B;
    static {
      if(RECOMPUTE_TABLES_AT_RUNTIME) {
        EXP_FRAC_TABLE_A = new double[FastMath.EXP_FRAC_TABLE_LEN];
        EXP_FRAC_TABLE_B = new double[FastMath.EXP_FRAC_TABLE_LEN];
        final double[] tmp = new double[2];
        final double factor = 1D / (EXP_FRAC_TABLE_LEN - 1);
        for(int i = 0; i < EXP_FRAC_TABLE_A.length; i++) {
          FastMathCalc.slowexp(i * factor, tmp);
          EXP_FRAC_TABLE_A[i] = tmp[0];
          EXP_FRAC_TABLE_B[i] = tmp[1];
        }
      }
      else {
        EXP_FRAC_TABLE_A = FastMathLiteralArrays.loadExpFracA();
        EXP_FRAC_TABLE_B = FastMathLiteralArrays.loadExpFracB();
      }
    }
  }
  
  private static class ExpIntTable  {
    final private static double[] EXP_INT_TABLE_A;
    final private static double[] EXP_INT_TABLE_B;
    static {
      if(RECOMPUTE_TABLES_AT_RUNTIME) {
        EXP_INT_TABLE_A = new double[FastMath.EXP_INT_TABLE_LEN];
        EXP_INT_TABLE_B = new double[FastMath.EXP_INT_TABLE_LEN];
        final double[] tmp = new double[2];
        final double[] recip = new double[2];
        for(int i = 0; i < FastMath.EXP_INT_TABLE_MAX_INDEX; i++) {
          FastMathCalc.expint(i, tmp);
          EXP_INT_TABLE_A[i + FastMath.EXP_INT_TABLE_MAX_INDEX] = tmp[0];
          EXP_INT_TABLE_B[i + FastMath.EXP_INT_TABLE_MAX_INDEX] = tmp[1];
          if(i != 0) {
            FastMathCalc.splitReciprocal(tmp, recip);
            EXP_INT_TABLE_A[FastMath.EXP_INT_TABLE_MAX_INDEX - i] = recip[0];
            EXP_INT_TABLE_B[FastMath.EXP_INT_TABLE_MAX_INDEX - i] = recip[1];
          }
        }
      }
      else {
        EXP_INT_TABLE_A = FastMathLiteralArrays.loadExpIntA();
        EXP_INT_TABLE_B = FastMathLiteralArrays.loadExpIntB();
      }
    }
  }
  
  private static class lnMant  {
    final private static double[][] LN_MANT;
    static {
      if(RECOMPUTE_TABLES_AT_RUNTIME) {
        LN_MANT = new double[FastMath.LN_MANT_LEN][];
        for(int i = 0; i < LN_MANT.length; i++) {
          final double d = Double.longBitsToDouble((((long)i) << 42) | 0x3ff0000000000000L);
          LN_MANT[i] = FastMathCalc.slowLog(d);
        }
      }
      else {
        LN_MANT = FastMathLiteralArrays.loadLnMant();
      }
    }
  }
}