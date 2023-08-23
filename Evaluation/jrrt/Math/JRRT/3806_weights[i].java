package org.apache.commons.math3.stat.descriptive.moment;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.WeightedEvaluation;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math3.util.MathUtils;

public class Variance extends AbstractStorelessUnivariateStatistic implements Serializable, WeightedEvaluation  {
  final private static long serialVersionUID = -9111962718267217978L;
  protected SecondMoment moment = null;
  protected boolean incMoment = true;
  private boolean isBiasCorrected = true;
  public Variance() {
    super();
    moment = new SecondMoment();
  }
  public Variance(Variance original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  public Variance(boolean isBiasCorrected) {
    super();
    moment = new SecondMoment();
    this.isBiasCorrected = isBiasCorrected;
  }
  public Variance(boolean isBiasCorrected, SecondMoment m2) {
    super();
    incMoment = false;
    this.moment = m2;
    this.isBiasCorrected = isBiasCorrected;
  }
  public Variance(final SecondMoment m2) {
    super();
    incMoment = false;
    this.moment = m2;
  }
  @Override() public Variance copy() {
    Variance result = new Variance();
    copy(this, result);
    return result;
  }
  public boolean isBiasCorrected() {
    return isBiasCorrected;
  }
  @Override() public double evaluate(final double[] values) throws MathIllegalArgumentException {
    if(values == null) {
      throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
    }
    return evaluate(values, 0, values.length);
  }
  public double evaluate(final double[] values, final double mean) throws MathIllegalArgumentException {
    return evaluate(values, mean, 0, values.length);
  }
  public double evaluate(final double[] values, final double mean, final int begin, final int length) throws MathIllegalArgumentException {
    double var = Double.NaN;
    if(test(values, begin, length)) {
      if(length == 1) {
        var = 0.0D;
      }
      else 
        if(length > 1) {
          double accum = 0.0D;
          double dev = 0.0D;
          double accum2 = 0.0D;
          for(int i = begin; i < begin + length; i++) {
            dev = values[i] - mean;
            accum += dev * dev;
            accum2 += dev;
          }
          double len = length;
          if(isBiasCorrected) {
            var = (accum - (accum2 * accum2 / len)) / (len - 1.0D);
          }
          else {
            var = (accum - (accum2 * accum2 / len)) / len;
          }
        }
    }
    return var;
  }
  public double evaluate(final double[] values, final double[] weights) throws MathIllegalArgumentException {
    return evaluate(values, weights, 0, values.length);
  }
  public double evaluate(final double[] values, final double[] weights, final double mean) throws MathIllegalArgumentException {
    return evaluate(values, weights, mean, 0, values.length);
  }
  public double evaluate(final double[] values, final double[] weights, final double mean, final int begin, final int length) throws MathIllegalArgumentException {
    double var = Double.NaN;
    if(test(values, weights, begin, length)) {
      if(length == 1) {
        var = 0.0D;
      }
      else 
        if(length > 1) {
          double accum = 0.0D;
          double dev = 0.0D;
          double accum2 = 0.0D;
          for(int i = begin; i < begin + length; i++) {
            dev = values[i] - mean;
            accum += weights[i] * (dev * dev);
            accum2 += weights[i] * dev;
          }
          double sumWts = 0;
          for(int i = begin; i < begin + length; i++) {
            double var_3806 = weights[i];
            sumWts += var_3806;
          }
          if(isBiasCorrected) {
            var = (accum - (accum2 * accum2 / sumWts)) / (sumWts - 1.0D);
          }
          else {
            var = (accum - (accum2 * accum2 / sumWts)) / sumWts;
          }
        }
    }
    return var;
  }
  public double evaluate(final double[] values, final double[] weights, final int begin, final int length) throws MathIllegalArgumentException {
    double var = Double.NaN;
    if(test(values, weights, begin, length)) {
      clear();
      if(length == 1) {
        var = 0.0D;
      }
      else 
        if(length > 1) {
          Mean mean = new Mean();
          double m = mean.evaluate(values, weights, begin, length);
          var = evaluate(values, weights, m, begin, length);
        }
    }
    return var;
  }
  @Override() public double evaluate(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    double var = Double.NaN;
    if(test(values, begin, length)) {
      clear();
      if(length == 1) {
        var = 0.0D;
      }
      else 
        if(length > 1) {
          Mean mean = new Mean();
          double m = mean.evaluate(values, begin, length);
          var = evaluate(values, m, begin, length);
        }
    }
    return var;
  }
  @Override() public double getResult() {
    if(moment.n == 0) {
      return Double.NaN;
    }
    else 
      if(moment.n == 1) {
        return 0D;
      }
      else {
        if(isBiasCorrected) {
          return moment.m2 / (moment.n - 1D);
        }
        else {
          return moment.m2 / (moment.n);
        }
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
  public static void copy(Variance source, Variance dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.moment = source.moment.copy();
    dest.isBiasCorrected = source.isBiasCorrected;
    dest.incMoment = source.incMoment;
  }
  @Override() public void increment(final double d) {
    if(incMoment) {
      moment.increment(d);
    }
  }
  public void setBiasCorrected(boolean biasCorrected) {
    this.isBiasCorrected = biasCorrected;
  }
}