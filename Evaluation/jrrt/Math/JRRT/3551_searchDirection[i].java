package org.apache.commons.math3.optimization.general;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.util.FastMath;

@Deprecated() public class NonLinearConjugateGradientOptimizer extends AbstractScalarDifferentiableOptimizer  {
  final private ConjugateGradientFormula updateFormula;
  final private Preconditioner preconditioner;
  final private UnivariateSolver solver;
  private double initialStep;
  private double[] point;
  @Deprecated() public NonLinearConjugateGradientOptimizer(final ConjugateGradientFormula updateFormula) {
    this(updateFormula, new SimpleValueChecker());
  }
  public NonLinearConjugateGradientOptimizer(final ConjugateGradientFormula updateFormula, ConvergenceChecker<PointValuePair> checker) {
    this(updateFormula, checker, new BrentSolver(), new IdentityPreconditioner());
  }
  public NonLinearConjugateGradientOptimizer(final ConjugateGradientFormula updateFormula, ConvergenceChecker<PointValuePair> checker, final UnivariateSolver lineSearchSolver) {
    this(updateFormula, checker, lineSearchSolver, new IdentityPreconditioner());
  }
  public NonLinearConjugateGradientOptimizer(final ConjugateGradientFormula updateFormula, ConvergenceChecker<PointValuePair> checker, final UnivariateSolver lineSearchSolver, final Preconditioner preconditioner) {
    super(checker);
    this.updateFormula = updateFormula;
    solver = lineSearchSolver;
    this.preconditioner = preconditioner;
    initialStep = 1.0D;
  }
  @Override() protected PointValuePair doOptimize() {
    final ConvergenceChecker<PointValuePair> checker = getConvergenceChecker();
    point = getStartPoint();
    final GoalType goal = getGoalType();
    final int n = point.length;
    double[] r = computeObjectiveGradient(point);
    if(goal == GoalType.MINIMIZE) {
      for(int i = 0; i < n; ++i) {
        r[i] = -r[i];
      }
    }
    double[] steepestDescent = preconditioner.precondition(point, r);
    double[] searchDirection = steepestDescent.clone();
    double delta = 0;
    for(int i = 0; i < n; ++i) {
      delta += r[i] * searchDirection[i];
    }
    PointValuePair current = null;
    int iter = 0;
    int maxEval = getMaxEvaluations();
    while(true){
      ++iter;
      final double objective = computeObjectiveValue(point);
      PointValuePair previous = current;
      current = new PointValuePair(point, objective);
      if(previous != null && checker.converged(iter, previous, current)) {
        return current;
      }
      final UnivariateFunction lsf = new LineSearchFunction(searchDirection);
      final double uB = findUpperBound(lsf, 0, initialStep);
      final double step = solver.solve(maxEval, lsf, 0, uB, 1e-15D);
      maxEval -= solver.getEvaluations();
      for(int i = 0; i < point.length; ++i) {
        double var_3551 = searchDirection[i];
        point[i] += step * var_3551;
      }
      r = computeObjectiveGradient(point);
      if(goal == GoalType.MINIMIZE) {
        for(int i = 0; i < n; ++i) {
          r[i] = -r[i];
        }
      }
      final double deltaOld = delta;
      final double[] newSteepestDescent = preconditioner.precondition(point, r);
      delta = 0;
      for(int i = 0; i < n; ++i) {
        delta += r[i] * newSteepestDescent[i];
      }
      final double beta;
      if(updateFormula == ConjugateGradientFormula.FLETCHER_REEVES) {
        beta = delta / deltaOld;
      }
      else {
        double deltaMid = 0;
        for(int i = 0; i < r.length; ++i) {
          deltaMid += r[i] * steepestDescent[i];
        }
        beta = (delta - deltaMid) / deltaOld;
      }
      steepestDescent = newSteepestDescent;
      if(iter % n == 0 || beta < 0) {
        searchDirection = steepestDescent.clone();
      }
      else {
        for(int i = 0; i < n; ++i) {
          searchDirection[i] = steepestDescent[i] + beta * searchDirection[i];
        }
      }
    }
  }
  private double findUpperBound(final UnivariateFunction f, final double a, final double h) {
    final double yA = f.value(a);
    double yB = yA;
    for(double step = h; step < Double.MAX_VALUE; step *= FastMath.max(2, yA / yB)) {
      final double b = a + step;
      yB = f.value(b);
      if(yA * yB <= 0) {
        return b;
      }
    }
    throw new MathIllegalStateException(LocalizedFormats.UNABLE_TO_BRACKET_OPTIMUM_IN_LINE_SEARCH);
  }
  public void setInitialStep(final double initialStep) {
    if(initialStep <= 0) {
      this.initialStep = 1.0D;
    }
    else {
      this.initialStep = initialStep;
    }
  }
  
  public static class IdentityPreconditioner implements Preconditioner  {
    public double[] precondition(double[] variables, double[] r) {
      return r.clone();
    }
  }
  
  private class LineSearchFunction implements UnivariateFunction  {
    final private double[] searchDirection;
    public LineSearchFunction(final double[] searchDirection) {
      super();
      this.searchDirection = searchDirection;
    }
    public double value(double x) {
      final double[] shiftedPoint = point.clone();
      for(int i = 0; i < shiftedPoint.length; ++i) {
        shiftedPoint[i] += x * searchDirection[i];
      }
      final double[] gradient = computeObjectiveGradient(shiftedPoint);
      double dotProduct = 0;
      for(int i = 0; i < gradient.length; ++i) {
        dotProduct += gradient[i] * searchDirection[i];
      }
      return dotProduct;
    }
  }
}