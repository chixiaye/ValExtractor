package org.apache.commons.math3.stat.descriptive.moment;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class Skewness extends AbstractStorelessUnivariateStatistic implements Serializable  {
  final private static long serialVersionUID = 7101857578996691352L;
  protected ThirdMoment moment = null;
  protected boolean incMoment;
  public Skewness() {
    super();
    incMoment = true;
    moment = new ThirdMoment();
  }
  public Skewness(Skewness original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  public Skewness(final ThirdMoment m3) {
    super();
    incMoment = false;
    this.moment = m3;
  }
  @Override() public Skewness copy() {
    Skewness result = new Skewness();
    copy(this, result);
    return result;
  }
  @Override() public double evaluate(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    double skew = Double.NaN;
    if(test(values, begin, length) && length > 2) {
      Mean mean = new Mean();
      double m = mean.evaluate(values, begin, length);
      double accum = 0.0D;
      double accum2 = 0.0D;
      for(int i = begin; i < begin + length; i++) {
        final double d = values[i] - m;
        accum += d * d;
        accum2 += d;
      }
      final double variance = (accum - (accum2 * accum2 / length)) / (length - 1);
      double accum3 = 0.0D;
      for(int i = begin; i < begin + length; i++) {
        final double d = values[i] - m;
        accum3 += d * d * d;
      }
      accum3 /= variance * FastMath.sqrt(variance);
      double n0 = length;
      skew = (n0 / ((n0 - 1) * (n0 - 2))) * accum3;
    }
    return skew;
  }
  @Override() public double getResult() {
    if(moment.n < 3) {
      return Double.NaN;
    }
    long var_3791 = moment.n;
    double variance = moment.m2 / (var_3791 - 1);
    if(variance < 10E-20D) {
      return 0.0D;
    }
    else {
      double n0 = moment.getN();
      return (n0 * moment.m3) / ((n0 - 1) * (n0 - 2) * FastMath.sqrt(variance) * variance);
    }
  }
  public long getN() {
    return moment.getN();
  }
  @Override() public void clear() {
    if(incMoment) {
      moment.clear();
    }
  }
  public static void copy(Skewness source, Skewness dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.moment = new ThirdMoment(source.moment.copy());
    dest.incMoment = source.incMoment;
  }
  @Override() public void increment(final double d) {
    if(incMoment) {
      moment.increment(d);
    }
  }
}