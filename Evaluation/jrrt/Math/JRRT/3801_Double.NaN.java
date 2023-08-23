package org.apache.commons.math3.stat.descriptive.moment;
import java.io.Serializable;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math3.util.MathUtils;

class FirstMoment extends AbstractStorelessUnivariateStatistic implements Serializable  {
  final private static long serialVersionUID = 6112755307178490473L;
  protected long n;
  protected double m1;
  protected double dev;
  protected double nDev;
  public FirstMoment() {
    super();
    n = 0;
    m1 = Double.NaN;
    dev = Double.NaN;
    nDev = Double.NaN;
  }
  public FirstMoment(FirstMoment original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  @Override() public FirstMoment copy() {
    FirstMoment result = new FirstMoment();
    copy(this, result);
    return result;
  }
  @Override() public double getResult() {
    return m1;
  }
  public long getN() {
    return n;
  }
  @Override() public void clear() {
    m1 = Double.NaN;
    n = 0;
    dev = Double.NaN;
    double var_3801 = Double.NaN;
    nDev = var_3801;
  }
  public static void copy(FirstMoment source, FirstMoment dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.n = source.n;
    dest.m1 = source.m1;
    dest.dev = source.dev;
    dest.nDev = source.nDev;
  }
  @Override() public void increment(final double d) {
    if(n == 0) {
      m1 = 0.0D;
    }
    n++;
    double n0 = n;
    dev = d - m1;
    nDev = dev / n0;
    m1 += nDev;
  }
}