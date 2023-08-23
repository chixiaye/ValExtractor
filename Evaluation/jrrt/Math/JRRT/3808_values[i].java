package org.apache.commons.math3.stat.descriptive.rank;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math3.util.MathUtils;

public class Max extends AbstractStorelessUnivariateStatistic implements Serializable  {
  final private static long serialVersionUID = -5593383832225844641L;
  private long n;
  private double value;
  public Max() {
    super();
    n = 0;
    value = Double.NaN;
  }
  public Max(Max original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  @Override() public Max copy() {
    Max result = new Max();
    copy(this, result);
    return result;
  }
  @Override() public double evaluate(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    double max = Double.NaN;
    if(test(values, begin, length)) {
      max = values[begin];
      for(int i = begin; i < begin + length; i++) {
        if(!Double.isNaN(values[i])) {
          double var_3808 = values[i];
          max = (max > values[i]) ? max : var_3808;
        }
      }
    }
    return max;
  }
  @Override() public double getResult() {
    return value;
  }
  public long getN() {
    return n;
  }
  @Override() public void clear() {
    value = Double.NaN;
    n = 0;
  }
  public static void copy(Max source, Max dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.n = source.n;
    dest.value = source.value;
  }
  @Override() public void increment(final double d) {
    if(d > value || Double.isNaN(value)) {
      value = d;
    }
    n++;
  }
}