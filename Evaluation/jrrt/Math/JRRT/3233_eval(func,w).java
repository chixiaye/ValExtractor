package org.apache.commons.math3.optim.univariate;
import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

public class BracketFinder  {
  final private static double EPS_MIN = 1e-21D;
  final private static double GOLD = 1.618034D;
  final private double growLimit;
  final private Incrementor evaluations = new Incrementor();
  private double lo;
  private double hi;
  private double mid;
  private double fLo;
  private double fHi;
  private double fMid;
  public BracketFinder() {
    this(100, 50);
  }
  public BracketFinder(double growLimit, int maxEvaluations) {
    super();
    if(growLimit <= 0) {
      throw new NotStrictlyPositiveException(growLimit);
    }
    if(maxEvaluations <= 0) {
      throw new NotStrictlyPositiveException(maxEvaluations);
    }
    this.growLimit = growLimit;
    evaluations.setMaximalCount(maxEvaluations);
  }
  private double eval(UnivariateFunction f, double x) {
    try {
      evaluations.incrementCount();
    }
    catch (MaxCountExceededException e) {
      throw new TooManyEvaluationsException(e.getMax());
    }
    return f.value(x);
  }
  public double getFHi() {
    return fHi;
  }
  public double getFLo() {
    return fLo;
  }
  public double getFMid() {
    return fMid;
  }
  public double getHi() {
    return hi;
  }
  public double getLo() {
    return lo;
  }
  public double getMid() {
    return mid;
  }
  public int getEvaluations() {
    return evaluations.getCount();
  }
  public int getMaxEvaluations() {
    return evaluations.getMaximalCount();
  }
  public void search(UnivariateFunction func, GoalType goal, double xA, double xB) {
    evaluations.resetCount();
    final boolean isMinim = goal == GoalType.MINIMIZE;
    double fA = eval(func, xA);
    double fB = eval(func, xB);
    if(isMinim ? fA < fB : fA > fB) {
      double tmp = xA;
      xA = xB;
      xB = tmp;
      tmp = fA;
      fA = fB;
      fB = tmp;
    }
    double xC = xB + GOLD * (xB - xA);
    double fC = eval(func, xC);
    while(isMinim ? fC < fB : fC > fB){
      double tmp1 = (xB - xA) * (fB - fC);
      double tmp2 = (xB - xC) * (fB - fA);
      double val = tmp2 - tmp1;
      double denom = Math.abs(val) < EPS_MIN ? 2 * EPS_MIN : 2 * val;
      double w = xB - ((xB - xC) * tmp2 - (xB - xA) * tmp1) / denom;
      double wLim = xB + growLimit * (xC - xB);
      double fW;
      if((w - xC) * (xB - w) > 0) {
        fW = eval(func, w);
        if(isMinim ? fW < fC : fW > fC) {
          xA = xB;
          xB = w;
          fA = fB;
          fB = fW;
          break ;
        }
        else 
          if(isMinim ? fW > fB : fW < fB) {
            xC = w;
            fC = fW;
            break ;
          }
        w = xC + GOLD * (xC - xB);
        fW = eval(func, w);
      }
      else 
        if((w - wLim) * (wLim - xC) >= 0) {
          w = wLim;
          fW = eval(func, w);
        }
        else 
          if((w - wLim) * (xC - w) > 0) {
            fW = eval(func, w);
            if(isMinim ? fW < fC : fW > fC) {
              xB = xC;
              xC = w;
              w = xC + GOLD * (xC - xB);
              fB = fC;
              fC = fW;
              double var_3233 = eval(func, w);
              fW = var_3233;
            }
          }
          else {
            w = xC + GOLD * (xC - xB);
            fW = eval(func, w);
          }
      xA = xB;
      fA = fB;
      xB = xC;
      fB = fC;
      xC = w;
      fC = fW;
    }
    lo = xA;
    fLo = fA;
    mid = xB;
    fMid = fB;
    hi = xC;
    fHi = fC;
    if(lo > hi) {
      double tmp = lo;
      lo = hi;
      hi = tmp;
      tmp = fLo;
      fLo = fHi;
      fHi = tmp;
    }
  }
}