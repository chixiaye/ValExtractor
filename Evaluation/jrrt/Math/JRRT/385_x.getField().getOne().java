package org.apache.commons.math3.analysis.interpolation;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathUtils;
public class FieldHermiteInterpolator<T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolator@T>>  {
  final private List<T> abscissae;
  final private List<T[]> topDiagonal;
  final private List<T[]> bottomDiagonal;
  public FieldHermiteInterpolator() {
    super();
    this.abscissae = new ArrayList<T>();
    this.topDiagonal = new ArrayList<T[]>();
    this.bottomDiagonal = new ArrayList<T[]>();
  }
  public T[] value(T x) throws NoDataException, NullArgumentException {
    MathUtils.checkNotNull(x);
    if(abscissae.isEmpty()) {
      throw new NoDataException(LocalizedFormats.EMPTY_INTERPOLATION_SAMPLE);
    }
    final T[] value = MathArrays.buildArray(x.getField(), topDiagonal.get(0).length);
    T valueCoeff = x.getField().getOne();
    for(int i = 0; i < topDiagonal.size(); ++i) {
      T[] dividedDifference = topDiagonal.get(i);
      for(int k = 0; k < value.length; ++k) {
        value[k] = value[k].add(dividedDifference[k].multiply(valueCoeff));
      }
      final T deltaX = x.subtract(abscissae.get(i));
      valueCoeff = valueCoeff.multiply(deltaX);
    }
    return value;
  }
  public T[][] derivatives(T x, int order) throws NoDataException, NullArgumentException {
    MathUtils.checkNotNull(x);
    if(abscissae.isEmpty()) {
      throw new NoDataException(LocalizedFormats.EMPTY_INTERPOLATION_SAMPLE);
    }
    final T zero = x.getField().getZero();
    T var_385 = x.getField().getOne();
    final T one = var_385;
    final T[] tj = MathArrays.buildArray(x.getField(), order + 1);
    tj[0] = zero;
    for(int i = 0; i < order; ++i) {
      tj[i + 1] = tj[i].add(one);
    }
    final T[][] derivatives = MathArrays.buildArray(x.getField(), order + 1, topDiagonal.get(0).length);
    final T[] valueCoeff = MathArrays.buildArray(x.getField(), order + 1);
    valueCoeff[0] = x.getField().getOne();
    for(int i = 0; i < topDiagonal.size(); ++i) {
      T[] dividedDifference = topDiagonal.get(i);
      final T deltaX = x.subtract(abscissae.get(i));
      for(int j = order; j >= 0; --j) {
        for(int k = 0; k < derivatives[j].length; ++k) {
          derivatives[j][k] = derivatives[j][k].add(dividedDifference[k].multiply(valueCoeff[j]));
        }
        valueCoeff[j] = valueCoeff[j].multiply(deltaX);
        if(j > 0) {
          valueCoeff[j] = valueCoeff[j].add(tj[j].multiply(valueCoeff[j - 1]));
        }
      }
    }
    return derivatives;
  }
  public void addSamplePoint(final T x, final T[] ... value) throws ZeroException, MathArithmeticException, DimensionMismatchException, NullArgumentException {
    MathUtils.checkNotNull(x);
    T factorial = x.getField().getOne();
    for(int i = 0; i < value.length; ++i) {
      final T[] y = value[i].clone();
      if(i > 1) {
        factorial = factorial.multiply(i);
        final T inv = factorial.reciprocal();
        for(int j = 0; j < y.length; ++j) {
          y[j] = y[j].multiply(inv);
        }
      }
      final int n = abscissae.size();
      bottomDiagonal.add(n - i, y);
      T[] bottom0 = y;
      for(int j = i; j < n; ++j) {
        final T[] bottom1 = bottomDiagonal.get(n - (j + 1));
        if(x.equals(abscissae.get(n - (j + 1)))) {
          throw new ZeroException(LocalizedFormats.DUPLICATED_ABSCISSA_DIVISION_BY_ZERO, x);
        }
        final T inv = x.subtract(abscissae.get(n - (j + 1))).reciprocal();
        for(int k = 0; k < y.length; ++k) {
          bottom1[k] = inv.multiply(bottom0[k].subtract(bottom1[k]));
        }
        bottom0 = bottom1;
      }
      topDiagonal.add(bottom0.clone());
      abscissae.add(x);
    }
  }
}