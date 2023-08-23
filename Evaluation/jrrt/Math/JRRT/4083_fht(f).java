package org.apache.commons.math3.transform;
import java.io.Serializable;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.ArithmeticUtils;

public class FastHadamardTransformer implements RealTransformer, Serializable  {
  final static long serialVersionUID = 20120211L;
  protected double[] fht(double[] x) throws MathIllegalArgumentException {
    final int n = x.length;
    final int halfN = n / 2;
    if(!ArithmeticUtils.isPowerOfTwo(n)) {
      throw new MathIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO, Integer.valueOf(n));
    }
    double[] yPrevious = new double[n];
    double[] yCurrent = x.clone();
    for(int j = 1; j < n; j <<= 1) {
      final double[] yTmp = yCurrent;
      yCurrent = yPrevious;
      yPrevious = yTmp;
      for(int i = 0; i < halfN; ++i) {
        final int twoI = 2 * i;
        yCurrent[i] = yPrevious[twoI] + yPrevious[twoI + 1];
      }
      for(int i = halfN; i < n; ++i) {
        final int twoI = 2 * i;
        yCurrent[i] = yPrevious[twoI - n] - yPrevious[twoI - n + 1];
      }
    }
    return yCurrent;
  }
  public double[] transform(final double[] f, final TransformType type) {
    if(type == TransformType.FORWARD) {
      double[] var_4083 = fht(f);
      return var_4083;
    }
    return TransformUtils.scaleArray(fht(f), 1.0D / f.length);
  }
  public double[] transform(final UnivariateFunction f, final double min, final double max, final int n, final TransformType type) {
    return transform(FunctionUtils.sample(f, min, max, n), type);
  }
  protected int[] fht(int[] x) throws MathIllegalArgumentException {
    final int n = x.length;
    final int halfN = n / 2;
    if(!ArithmeticUtils.isPowerOfTwo(n)) {
      throw new MathIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO, Integer.valueOf(n));
    }
    int[] yPrevious = new int[n];
    int[] yCurrent = x.clone();
    for(int j = 1; j < n; j <<= 1) {
      final int[] yTmp = yCurrent;
      yCurrent = yPrevious;
      yPrevious = yTmp;
      for(int i = 0; i < halfN; ++i) {
        final int twoI = 2 * i;
        yCurrent[i] = yPrevious[twoI] + yPrevious[twoI + 1];
      }
      for(int i = halfN; i < n; ++i) {
        final int twoI = 2 * i;
        yCurrent[i] = yPrevious[twoI - n] - yPrevious[twoI - n + 1];
      }
    }
    return yCurrent;
  }
  public int[] transform(final int[] f) {
    return fht(f);
  }
}