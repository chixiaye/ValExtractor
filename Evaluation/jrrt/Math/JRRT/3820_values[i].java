package org.apache.commons.math3.stat.descriptive.summary;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math3.util.MathUtils;

public class SumOfSquares extends AbstractStorelessUnivariateStatistic implements Serializable  {
  final private static long serialVersionUID = 1460986908574398008L;
  private long n;
  private double value;
  public SumOfSquares() {
    super();
    n = 0;
    value = 0;
  }
  public SumOfSquares(SumOfSquares original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  @Override() public SumOfSquares copy() {
    SumOfSquares result = new SumOfSquares();
    copy(this, result);
    return result;
  }
  @Override() public double evaluate(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    double sumSq = Double.NaN;
    if(test(values, begin, length, true)) {
      sumSq = 0.0D;
      for(int i = begin; i < begin + length; i++) {
        double var_3820 = values[i];
        sumSq += values[i] * var_3820;
      }
    }
    return sumSq;
  }
  @Override() public double getResult() {
    return value;
  }
  public long getN() {
    return n;
  }
  @Override() public void clear() {
    value = 0;
    n = 0;
  }
  public static void copy(SumOfSquares source, SumOfSquares dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.n = source.n;
    dest.value = source.value;
  }
  @Override() public void increment(final double d) {
    value += d * d;
    n++;
  }
}