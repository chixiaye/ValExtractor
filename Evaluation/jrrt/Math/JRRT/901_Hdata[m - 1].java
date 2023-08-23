package org.apache.commons.math3.distribution;
import java.io.Serializable;
import java.math.BigDecimal;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.BigFractionField;
import org.apache.commons.math3.fraction.FractionConversionException;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public class KolmogorovSmirnovDistribution implements Serializable  {
  final private static long serialVersionUID = -4670676796862967187L;
  private int n;
  public KolmogorovSmirnovDistribution(int n) throws NotStrictlyPositiveException {
    super();
    if(n <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.NOT_POSITIVE_NUMBER_OF_SAMPLES, n);
    }
    this.n = n;
  }
  private FieldMatrix<BigFraction> createH(double d) throws NumberIsTooLargeException, FractionConversionException {
    int k = (int)Math.ceil(n * d);
    int m = 2 * k - 1;
    double hDouble = k - n * d;
    if(hDouble >= 1) {
      throw new NumberIsTooLargeException(hDouble, 1.0D, false);
    }
    BigFraction h = null;
    try {
      h = new BigFraction(hDouble, 1.0e-20D, 10000);
    }
    catch (FractionConversionException e1) {
      try {
        h = new BigFraction(hDouble, 1.0e-10D, 10000);
      }
      catch (FractionConversionException e2) {
        h = new BigFraction(hDouble, 1.0e-5D, 10000);
      }
    }
    final BigFraction[][] Hdata = new BigFraction[m][m];
    for(int i = 0; i < m; ++i) {
      for(int j = 0; j < m; ++j) {
        if(i - j + 1 < 0) {
          Hdata[i][j] = BigFraction.ZERO;
        }
        else {
          Hdata[i][j] = BigFraction.ONE;
        }
      }
    }
    final BigFraction[] hPowers = new BigFraction[m];
    hPowers[0] = h;
    for(int i = 1; i < m; ++i) {
      hPowers[i] = h.multiply(hPowers[i - 1]);
    }
    for(int i = 0; i < m; ++i) {
      Hdata[i][0] = Hdata[i][0].subtract(hPowers[i]);
      Hdata[m - 1][i] = Hdata[m - 1][i].subtract(hPowers[m - i - 1]);
    }
    if(h.compareTo(BigFraction.ONE_HALF) == 1) {
      BigFraction[] var_901 = Hdata[m - 1];
      Hdata[m - 1][0] = var_901[0].add(h.multiply(2).subtract(1).pow(m));
    }
    for(int i = 0; i < m; ++i) {
      for(int j = 0; j < i + 1; ++j) {
        if(i - j + 1 > 0) {
          for(int g = 2; g <= i - j + 1; ++g) {
            Hdata[i][j] = Hdata[i][j].divide(g);
          }
        }
      }
    }
    return new Array2DRowFieldMatrix<BigFraction>(BigFractionField.getInstance(), Hdata);
  }
  public double cdf(double d) throws MathArithmeticException {
    return this.cdf(d, false);
  }
  public double cdf(double d, boolean exact) throws MathArithmeticException {
    final double ninv = 1 / ((double)n);
    final double ninvhalf = 0.5D * ninv;
    if(d <= ninvhalf) {
      return 0;
    }
    else 
      if(ninvhalf < d && d <= ninv) {
        double res = 1;
        double f = 2 * d - ninv;
        for(int i = 1; i <= n; ++i) {
          res *= i * f;
        }
        return res;
      }
      else 
        if(1 - ninv <= d && d < 1) {
          return 1 - 2 * Math.pow(1 - d, n);
        }
        else 
          if(1 <= d) {
            return 1;
          }
    return exact ? exactK(d) : roundedK(d);
  }
  public double cdfExact(double d) throws MathArithmeticException {
    return this.cdf(d, true);
  }
  private double exactK(double d) throws MathArithmeticException {
    final int k = (int)Math.ceil(n * d);
    final FieldMatrix<BigFraction> H = this.createH(d);
    final FieldMatrix<BigFraction> Hpower = H.power(n);
    BigFraction pFrac = Hpower.getEntry(k - 1, k - 1);
    for(int i = 1; i <= n; ++i) {
      pFrac = pFrac.multiply(i).divide(n);
    }
    return pFrac.bigDecimalValue(20, BigDecimal.ROUND_HALF_UP).doubleValue();
  }
  private double roundedK(double d) throws MathArithmeticException {
    final int k = (int)Math.ceil(n * d);
    final FieldMatrix<BigFraction> HBigFraction = this.createH(d);
    final int m = HBigFraction.getRowDimension();
    final RealMatrix H = new Array2DRowRealMatrix(m, m);
    for(int i = 0; i < m; ++i) {
      for(int j = 0; j < m; ++j) {
        H.setEntry(i, j, HBigFraction.getEntry(i, j).doubleValue());
      }
    }
    final RealMatrix Hpower = H.power(n);
    double pFrac = Hpower.getEntry(k - 1, k - 1);
    for(int i = 1; i <= n; ++i) {
      pFrac *= (double)i / (double)n;
    }
    return pFrac;
  }
}