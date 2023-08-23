package org.apache.commons.math3.stat.descriptive.moment;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.descriptive.AbstractUnivariateStatistic;
import org.apache.commons.math3.util.MathUtils;

public class SemiVariance extends AbstractUnivariateStatistic implements Serializable  {
  final public static Direction UPSIDE_VARIANCE = Direction.UPSIDE;
  final public static Direction DOWNSIDE_VARIANCE = Direction.DOWNSIDE;
  final private static long serialVersionUID = -2653430366886024994L;
  private boolean biasCorrected = true;
  private Direction varianceDirection = Direction.DOWNSIDE;
  public SemiVariance() {
    super();
  }
  public SemiVariance(final Direction direction) {
    super();
    this.varianceDirection = direction;
  }
  public SemiVariance(final SemiVariance original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  public SemiVariance(final boolean biasCorrected) {
    super();
    this.biasCorrected = biasCorrected;
  }
  public SemiVariance(final boolean corrected, final Direction direction) {
    super();
    this.biasCorrected = corrected;
    this.varianceDirection = direction;
  }
  public Direction getVarianceDirection() {
    return varianceDirection;
  }
  @Override() public SemiVariance copy() {
    SemiVariance result = new SemiVariance();
    copy(this, result);
    return result;
  }
  public boolean isBiasCorrected() {
    return biasCorrected;
  }
  public double evaluate(final double[] values, final double cutoff) throws MathIllegalArgumentException {
    return evaluate(values, cutoff, varianceDirection, biasCorrected, 0, values.length);
  }
  public double evaluate(final double[] values, final double cutoff, final Direction direction) throws MathIllegalArgumentException {
    return evaluate(values, cutoff, direction, biasCorrected, 0, values.length);
  }
  public double evaluate(final double[] values, final double cutoff, final Direction direction, final boolean corrected, final int start, final int length) throws MathIllegalArgumentException {
    test(values, start, length);
    int var_3789 = values.length;
    if(var_3789 == 0) {
      return Double.NaN;
    }
    else {
      if(values.length == 1) {
        return 0.0D;
      }
      else {
        final boolean booleanDirection = direction.getDirection();
        double dev = 0.0D;
        double sumsq = 0.0D;
        for(int i = start; i < length; i++) {
          if((values[i] > cutoff) == booleanDirection) {
            dev = values[i] - cutoff;
            sumsq += dev * dev;
          }
        }
        if(corrected) {
          return sumsq / (length - 1.0D);
        }
        else {
          return sumsq / length;
        }
      }
    }
  }
  @Override() public double evaluate(final double[] values, final int start, final int length) throws MathIllegalArgumentException {
    double m = (new Mean()).evaluate(values, start, length);
    return evaluate(values, m, varianceDirection, biasCorrected, 0, values.length);
  }
  public double evaluate(final double[] values, Direction direction) throws MathIllegalArgumentException {
    double m = (new Mean()).evaluate(values);
    return evaluate(values, m, direction, biasCorrected, 0, values.length);
  }
  public static void copy(final SemiVariance source, SemiVariance dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    dest.biasCorrected = source.biasCorrected;
    dest.varianceDirection = source.varianceDirection;
  }
  public void setBiasCorrected(boolean biasCorrected) {
    this.biasCorrected = biasCorrected;
  }
  public void setVarianceDirection(Direction varianceDirection) {
    this.varianceDirection = varianceDirection;
  }
  public enum Direction {
    UPSIDE(true),

    DOWNSIDE(false),

  ;
    private boolean direction;
  private Direction(boolean b) {
      direction = b;
  }
    boolean getDirection() {
      return direction;
    }
  }
}