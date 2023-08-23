package org.apache.commons.math3.stat.descriptive.moment;
import java.io.Serializable;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.MathUtils;

class ThirdMoment extends SecondMoment implements Serializable  {
  final private static long serialVersionUID = -7818711964045118679L;
  protected double m3;
  protected double nDevSq;
  public ThirdMoment() {
    super();
    double var_3787 = Double.NaN;
    m3 = var_3787;
    nDevSq = Double.NaN;
  }
  public ThirdMoment(ThirdMoment original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  @Override() public ThirdMoment copy() {
    ThirdMoment result = new ThirdMoment();
    copy(this, result);
    return result;
  }
  @Override() public double getResult() {
    return m3;
  }
  @Override() public void clear() {
    super.clear();
    m3 = Double.NaN;
    nDevSq = Double.NaN;
  }
  public static void copy(ThirdMoment source, ThirdMoment dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    SecondMoment.copy(source, dest);
    dest.m3 = source.m3;
    dest.nDevSq = source.nDevSq;
  }
  @Override() public void increment(final double d) {
    if(n < 1) {
      m3 = m2 = m1 = 0.0D;
    }
    double prevM2 = m2;
    super.increment(d);
    nDevSq = nDev * nDev;
    double n0 = n;
    m3 = m3 - 3.0D * nDev * prevM2 + (n0 - 1) * (n0 - 2) * nDevSq * dev;
  }
}