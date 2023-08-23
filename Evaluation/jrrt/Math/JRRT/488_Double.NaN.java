package org.apache.commons.math3.analysis.solvers;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.util.MathUtils;
abstract public class BaseAbstractUnivariateSolver<FUNC extends org.apache.commons.math3.analysis.UnivariateFunction> implements BaseUnivariateSolver<FUNC>  {
  final private static double DEFAULT_RELATIVE_ACCURACY = 1e-14D;
  final private static double DEFAULT_FUNCTION_VALUE_ACCURACY = 1e-15D;
  final private double functionValueAccuracy;
  final private double absoluteAccuracy;
  final private double relativeAccuracy;
  final private Incrementor evaluations = new Incrementor();
  private double searchMin;
  private double searchMax;
  private double searchStart;
  private FUNC function;
  protected BaseAbstractUnivariateSolver(final double absoluteAccuracy) {
    this(DEFAULT_RELATIVE_ACCURACY, absoluteAccuracy, DEFAULT_FUNCTION_VALUE_ACCURACY);
  }
  protected BaseAbstractUnivariateSolver(final double relativeAccuracy, final double absoluteAccuracy) {
    this(relativeAccuracy, absoluteAccuracy, DEFAULT_FUNCTION_VALUE_ACCURACY);
  }
  protected BaseAbstractUnivariateSolver(final double relativeAccuracy, final double absoluteAccuracy, final double functionValueAccuracy) {
    super();
    this.absoluteAccuracy = absoluteAccuracy;
    this.relativeAccuracy = relativeAccuracy;
    this.functionValueAccuracy = functionValueAccuracy;
  }
  protected boolean isBracketing(final double lower, final double upper) {
    return UnivariateSolverUtils.isBracketing(function, lower, upper);
  }
  protected boolean isSequence(final double start, final double mid, final double end) {
    return UnivariateSolverUtils.isSequence(start, mid, end);
  }
  protected double computeObjectiveValue(double point) throws TooManyEvaluationsException {
    incrementEvaluationCount();
    return function.value(point);
  }
  abstract protected double doSolve() throws TooManyEvaluationsException, NoBracketingException;
  public double getAbsoluteAccuracy() {
    return absoluteAccuracy;
  }
  public double getFunctionValueAccuracy() {
    return functionValueAccuracy;
  }
  public double getMax() {
    return searchMax;
  }
  public double getMin() {
    return searchMin;
  }
  public double getRelativeAccuracy() {
    return relativeAccuracy;
  }
  public double getStartValue() {
    return searchStart;
  }
  public double solve(int maxEval, FUNC f, double startValue) throws TooManyEvaluationsException, NoBracketingException {
    double var_488 = Double.NaN;
    return solve(maxEval, f, Double.NaN, var_488, startValue);
  }
  public double solve(int maxEval, FUNC f, double min, double max) {
    return solve(maxEval, f, min, max, min + 0.5D * (max - min));
  }
  public double solve(int maxEval, FUNC f, double min, double max, double startValue) throws TooManyEvaluationsException, NoBracketingException {
    setup(maxEval, f, min, max, startValue);
    return doSolve();
  }
  public int getEvaluations() {
    return evaluations.getCount();
  }
  public int getMaxEvaluations() {
    return evaluations.getMaximalCount();
  }
  protected void incrementEvaluationCount() throws TooManyEvaluationsException {
    try {
      evaluations.incrementCount();
    }
    catch (MaxCountExceededException e) {
      throw new TooManyEvaluationsException(e.getMax());
    }
  }
  protected void setup(int maxEval, FUNC f, double min, double max, double startValue) throws NullArgumentException {
    MathUtils.checkNotNull(f);
    searchMin = min;
    searchMax = max;
    searchStart = startValue;
    function = f;
    evaluations.setMaximalCount(maxEval);
    evaluations.resetCount();
  }
  protected void verifyBracketing(final double lower, final double upper) throws NullArgumentException, NoBracketingException {
    UnivariateSolverUtils.verifyBracketing(function, lower, upper);
  }
  protected void verifyInterval(final double lower, final double upper) throws NumberIsTooLargeException {
    UnivariateSolverUtils.verifyInterval(lower, upper);
  }
  protected void verifySequence(final double lower, final double initial, final double upper) throws NumberIsTooLargeException {
    UnivariateSolverUtils.verifySequence(lower, initial, upper);
  }
}