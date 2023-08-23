package org.apache.commons.math3.optimization.direct;
import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optimization.BaseMultivariateOptimizer;
import org.apache.commons.math3.optimization.OptimizationData;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.InitialGuess;
import org.apache.commons.math3.optimization.SimpleBounds;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
abstract @Deprecated() public class BaseAbstractMultivariateOptimizer<FUNC extends org.apache.commons.math3.analysis.MultivariateFunction> implements BaseMultivariateOptimizer<FUNC>  {
  final protected Incrementor evaluations = new Incrementor();
  private ConvergenceChecker<PointValuePair> checker;
  private GoalType goal;
  private double[] start;
  private double[] lowerBound;
  private double[] upperBound;
  private MultivariateFunction function;
  @Deprecated() protected BaseAbstractMultivariateOptimizer() {
    this(new SimpleValueChecker());
  }
  protected BaseAbstractMultivariateOptimizer(ConvergenceChecker<PointValuePair> checker) {
    super();
    this.checker = checker;
  }
  public ConvergenceChecker<PointValuePair> getConvergenceChecker() {
    return checker;
  }
  public GoalType getGoalType() {
    return goal;
  }
  abstract protected PointValuePair doOptimize();
  @Deprecated() public PointValuePair optimize(int maxEval, FUNC f, GoalType goalType, double[] startPoint) {
    return optimizeInternal(maxEval, f, goalType, new InitialGuess(startPoint));
  }
  public PointValuePair optimize(int maxEval, FUNC f, GoalType goalType, OptimizationData ... optData) {
    return optimizeInternal(maxEval, f, goalType, optData);
  }
  @Deprecated() protected PointValuePair optimizeInternal(int maxEval, FUNC f, GoalType goalType, double[] startPoint) {
    return optimizeInternal(maxEval, f, goalType, new InitialGuess(startPoint));
  }
  protected PointValuePair optimizeInternal(int maxEval, FUNC f, GoalType goalType, OptimizationData ... optData) throws TooManyEvaluationsException {
    evaluations.setMaximalCount(maxEval);
    evaluations.resetCount();
    function = f;
    goal = goalType;
    parseOptimizationData(optData);
    checkParameters();
    return doOptimize();
  }
  protected double computeObjectiveValue(double[] point) {
    try {
      evaluations.incrementCount();
    }
    catch (MaxCountExceededException e) {
      throw new TooManyEvaluationsException(e.getMax());
    }
    return function.value(point);
  }
  public double[] getLowerBound() {
    return lowerBound == null ? null : lowerBound.clone();
  }
  public double[] getStartPoint() {
    return start == null ? null : start.clone();
  }
  public double[] getUpperBound() {
    return upperBound == null ? null : upperBound.clone();
  }
  public int getEvaluations() {
    return evaluations.getCount();
  }
  public int getMaxEvaluations() {
    return evaluations.getMaximalCount();
  }
  private void checkParameters() {
    if(start != null) {
      final int dim = start.length;
      if(lowerBound != null) {
        if(lowerBound.length != dim) {
          throw new DimensionMismatchException(lowerBound.length, dim);
        }
        for(int i = 0; i < dim; i++) {
          final double v = start[i];
          final double lo = lowerBound[i];
          if(v < lo) {
            throw new NumberIsTooSmallException(v, lo, true);
          }
        }
      }
      if(upperBound != null) {
        if(upperBound.length != dim) {
          int var_3455 = upperBound.length;
          throw new DimensionMismatchException(var_3455, dim);
        }
        for(int i = 0; i < dim; i++) {
          final double v = start[i];
          final double hi = upperBound[i];
          if(v > hi) {
            throw new NumberIsTooLargeException(v, hi, true);
          }
        }
      }
      if(lowerBound == null) {
        lowerBound = new double[dim];
        for(int i = 0; i < dim; i++) {
          lowerBound[i] = Double.NEGATIVE_INFINITY;
        }
      }
      if(upperBound == null) {
        upperBound = new double[dim];
        for(int i = 0; i < dim; i++) {
          upperBound[i] = Double.POSITIVE_INFINITY;
        }
      }
    }
  }
  private void parseOptimizationData(OptimizationData ... optData) {
    for (OptimizationData data : optData) {
      if(data instanceof InitialGuess) {
        start = ((InitialGuess)data).getInitialGuess();
        continue ;
      }
      if(data instanceof SimpleBounds) {
        final SimpleBounds bounds = (SimpleBounds)data;
        lowerBound = bounds.getLower();
        upperBound = bounds.getUpper();
        continue ;
      }
    }
  }
}