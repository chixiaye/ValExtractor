package org.apache.commons.math3.stat.descriptive.rank;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math3.util.MathUtils;

public class Min extends AbstractStorelessUnivariateStatistic implements Serializable  {
  final private static long serialVersionUID = -2941995784909003131L;
  private long n;
  private double value;
  public Min() {
    super();
    n = 0;
    value = Double.NaN;
  }
  public Min(Min original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  @Override() public Min copy() {
    Min result = new Min();
    copy(this, result);
    return result;
  }
  @Override() public double evaluate(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    double min = Double.NaN;
    if(test(values, begin, length)) {
      min = values[begin];
      for(int i = begin; i < begin + length; i++) {
        double var_3807 = values[i];
        if(!Double.isNaN(var_3807)) {
          min = (min < values[i]) ? min : values[i];
        }
      }
    }
    return min;
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
  public static void copy(Min source, Min dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.n = source.n;
    dest.value = source.value;
  }
  @Override() public void increment(final double d) {
    if(d < value || Double.isNaN(value)) {
      value = d;
    }
    n++;
  }
}