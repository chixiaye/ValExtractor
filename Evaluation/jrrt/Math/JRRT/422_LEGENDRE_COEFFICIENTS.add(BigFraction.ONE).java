package org.apache.commons.math3.analysis.polynomials;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

public class PolynomialsUtils  {
  final private static List<BigFraction> CHEBYSHEV_COEFFICIENTS;
  final private static List<BigFraction> HERMITE_COEFFICIENTS;
  final private static List<BigFraction> LAGUERRE_COEFFICIENTS;
  final private static List<BigFraction> LEGENDRE_COEFFICIENTS;
  final private static Map<JacobiKey, List<BigFraction>> JACOBI_COEFFICIENTS;
  static {
    CHEBYSHEV_COEFFICIENTS = new ArrayList<BigFraction>();
    CHEBYSHEV_COEFFICIENTS.add(BigFraction.ONE);
    CHEBYSHEV_COEFFICIENTS.add(BigFraction.ZERO);
    CHEBYSHEV_COEFFICIENTS.add(BigFraction.ONE);
    HERMITE_COEFFICIENTS = new ArrayList<BigFraction>();
    HERMITE_COEFFICIENTS.add(BigFraction.ONE);
    HERMITE_COEFFICIENTS.add(BigFraction.ZERO);
    HERMITE_COEFFICIENTS.add(BigFraction.TWO);
    LAGUERRE_COEFFICIENTS = new ArrayList<BigFraction>();
    LAGUERRE_COEFFICIENTS.add(BigFraction.ONE);
    LAGUERRE_COEFFICIENTS.add(BigFraction.ONE);
    LAGUERRE_COEFFICIENTS.add(BigFraction.MINUS_ONE);
    LEGENDRE_COEFFICIENTS = new ArrayList<BigFraction>();
    LEGENDRE_COEFFICIENTS.add(BigFraction.ONE);
    LEGENDRE_COEFFICIENTS.add(BigFraction.ZERO);
    boolean var_422 = LEGENDRE_COEFFICIENTS.add(BigFraction.ONE);
    JACOBI_COEFFICIENTS = new HashMap<JacobiKey, List<BigFraction>>();
  }
  private PolynomialsUtils() {
    super();
  }
  private static PolynomialFunction buildPolynomial(final int degree, final List<BigFraction> coefficients, final RecurrenceCoefficientsGenerator generator) {
    final int maxDegree = (int)FastMath.floor(FastMath.sqrt(2 * coefficients.size())) - 1;
    synchronized(PolynomialsUtils.class) {
      if(degree > maxDegree) {
        computeUpToDegree(degree, maxDegree, generator, coefficients);
      }
    }
    final int start = degree * (degree + 1) / 2;
    final double[] a = new double[degree + 1];
    for(int i = 0; i <= degree; ++i) {
      a[i] = coefficients.get(start + i).doubleValue();
    }
    return new PolynomialFunction(a);
  }
  public static PolynomialFunction createChebyshevPolynomial(final int degree) {
    return buildPolynomial(degree, CHEBYSHEV_COEFFICIENTS, new RecurrenceCoefficientsGenerator() {
        final private BigFraction[] coeffs = { BigFraction.ZERO, BigFraction.TWO, BigFraction.ONE } ;
        public BigFraction[] generate(int k) {
          return coeffs;
        }
    });
  }
  public static PolynomialFunction createHermitePolynomial(final int degree) {
    return buildPolynomial(degree, HERMITE_COEFFICIENTS, new RecurrenceCoefficientsGenerator() {
        public BigFraction[] generate(int k) {
          return new BigFraction[]{ BigFraction.ZERO, BigFraction.TWO, new BigFraction(2 * k) } ;
        }
    });
  }
  public static PolynomialFunction createJacobiPolynomial(final int degree, final int v, final int w) {
    final JacobiKey key = new JacobiKey(v, w);
    if(!JACOBI_COEFFICIENTS.containsKey(key)) {
      final List<BigFraction> list = new ArrayList<BigFraction>();
      JACOBI_COEFFICIENTS.put(key, list);
      list.add(BigFraction.ONE);
      list.add(new BigFraction(v - w, 2));
      list.add(new BigFraction(2 + v + w, 2));
    }
    return buildPolynomial(degree, JACOBI_COEFFICIENTS.get(key), new RecurrenceCoefficientsGenerator() {
        public BigFraction[] generate(int k) {
          k++;
          final int kvw = k + v + w;
          final int twoKvw = kvw + k;
          final int twoKvwM1 = twoKvw - 1;
          final int twoKvwM2 = twoKvw - 2;
          final int den = 2 * k * kvw * twoKvwM2;
          return new BigFraction[]{ new BigFraction(twoKvwM1 * (v * v - w * w), den), new BigFraction(twoKvwM1 * twoKvw * twoKvwM2, den), new BigFraction(2 * (k + v - 1) * (k + w - 1) * twoKvw, den) } ;
        }
    });
  }
  public static PolynomialFunction createLaguerrePolynomial(final int degree) {
    return buildPolynomial(degree, LAGUERRE_COEFFICIENTS, new RecurrenceCoefficientsGenerator() {
        public BigFraction[] generate(int k) {
          final int kP1 = k + 1;
          return new BigFraction[]{ new BigFraction(2 * k + 1, kP1), new BigFraction(-1, kP1), new BigFraction(k, kP1) } ;
        }
    });
  }
  public static PolynomialFunction createLegendrePolynomial(final int degree) {
    return buildPolynomial(degree, LEGENDRE_COEFFICIENTS, new RecurrenceCoefficientsGenerator() {
        public BigFraction[] generate(int k) {
          final int kP1 = k + 1;
          return new BigFraction[]{ BigFraction.ZERO, new BigFraction(k + kP1, kP1), new BigFraction(k, kP1) } ;
        }
    });
  }
  public static double[] shift(final double[] coefficients, final double shift) {
    final int dp1 = coefficients.length;
    final double[] newCoefficients = new double[dp1];
    final int[][] coeff = new int[dp1][dp1];
    for(int i = 0; i < dp1; i++) {
      for(int j = 0; j <= i; j++) {
        coeff[i][j] = (int)CombinatoricsUtils.binomialCoefficient(i, j);
      }
    }
    for(int i = 0; i < dp1; i++) {
      newCoefficients[0] += coefficients[i] * FastMath.pow(shift, i);
    }
    final int d = dp1 - 1;
    for(int i = 0; i < d; i++) {
      for(int j = i; j < d; j++) {
        newCoefficients[i + 1] += coeff[j + 1][j - i] * coefficients[j + 1] * FastMath.pow(shift, j - i);
      }
    }
    return newCoefficients;
  }
  private static void computeUpToDegree(final int degree, final int maxDegree, final RecurrenceCoefficientsGenerator generator, final List<BigFraction> coefficients) {
    int startK = (maxDegree - 1) * maxDegree / 2;
    for(int k = maxDegree; k < degree; ++k) {
      int startKm1 = startK;
      startK += k;
      BigFraction[] ai = generator.generate(k);
      BigFraction ck = coefficients.get(startK);
      BigFraction ckm1 = coefficients.get(startKm1);
      coefficients.add(ck.multiply(ai[0]).subtract(ckm1.multiply(ai[2])));
      for(int i = 1; i < k; ++i) {
        final BigFraction ckPrev = ck;
        ck = coefficients.get(startK + i);
        ckm1 = coefficients.get(startKm1 + i);
        coefficients.add(ck.multiply(ai[0]).add(ckPrev.multiply(ai[1])).subtract(ckm1.multiply(ai[2])));
      }
      final BigFraction ckPrev = ck;
      ck = coefficients.get(startK + k);
      coefficients.add(ck.multiply(ai[0]).add(ckPrev.multiply(ai[1])));
      coefficients.add(ck.multiply(ai[1]));
    }
  }
  
  private static class JacobiKey  {
    final private int v;
    final private int w;
    public JacobiKey(final int v, final int w) {
      super();
      this.v = v;
      this.w = w;
    }
    @Override() public boolean equals(final Object key) {
      if((key == null) || !(key instanceof JacobiKey)) {
        return false;
      }
      final JacobiKey otherK = (JacobiKey)key;
      return (v == otherK.v) && (w == otherK.w);
    }
    @Override() public int hashCode() {
      return (v << 16) ^ w;
    }
  }
  
  private interface RecurrenceCoefficientsGenerator  {
    BigFraction[] generate(int k);
  }
}