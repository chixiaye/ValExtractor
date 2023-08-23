package org.apache.commons.math3.stat.descriptive.moment;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class Kurtosis extends AbstractStorelessUnivariateStatistic implements Serializable  {
  final private static long serialVersionUID = 2784465764798260919L;
  protected FourthMoment moment;
  protected boolean incMoment;
  public Kurtosis() {
    super();
    incMoment = true;
    moment = new FourthMoment();
  }
  public Kurtosis(Kurtosis original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  public Kurtosis(final FourthMoment m4) {
    super();
    incMoment = false;
    this.moment = m4;
  }
  @Override() public Kurtosis copy() {
    Kurtosis result = new Kurtosis();
    copy(this, result);
    return result;
  }
  @Override() public double evaluate(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    double kurt = Double.NaN;
    if(test(values, begin, length) && length > 3) {
      Variance variance = new Variance();
      variance.incrementAll(values, begin, length);
      double mean = variance.moment.m1;
      double stdDev = FastMath.sqrt(variance.getResult());
      double accum3 = 0.0D;
      for(int i = begin; i < begin + length; i++) {
        accum3 += FastMath.pow(values[i] - mean, 4.0D);
      }
      accum3 /= FastMath.pow(stdDev, 4.0D);
      double n0 = length;
      double coefficientOne = (n0 * (n0 + 1)) / ((n0 - 1) * (n0 - 2) * (n0 - 3));
      double termTwo = (3 * FastMath.pow(n0 - 1, 2.0D)) / ((n0 - 2) * (n0 - 3));
      kurt = (coefficientOne * accum3) - termTwo;
    }
    return kurt;
  }
  @Override() public double getResult() {
    double kurtosis = Double.NaN;
    if(moment.getN() > 3) {
      double variance = moment.m2 / (moment.n - 1);
      long var_3802 = moment.n;
      if(var_3802 <= 3 || variance < 10E-20D) {
        kurtosis = 0.0D;
      }
      else {
        double n = moment.n;
        kurtosis = (n * (n + 1) * moment.getResult() - 3 * moment.m2 * moment.m2 * (n - 1)) / ((n - 1) * (n - 2) * (n - 3) * variance * variance);
      }
    }
    return kurtosis;
  }
  public long getN() {
    return moment.getN();
  }
  @Override() public void clear() {
    if(incMoment) {
      moment.clear();
    }
  }
  public static void copy(Kurtosis source, Kurtosis dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.moment = source.moment.copy();
    dest.incMoment = source.incMoment;
  }
  @Override() public void increment(final double d) {
    if(incMoment) {
      moment.increment(d);
    }
  }
}