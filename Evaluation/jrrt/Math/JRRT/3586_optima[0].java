package org.apache.commons.math3.optimization.univariate;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.ConvergenceChecker;
@Deprecated() public class UnivariateMultiStartOptimizer<FUNC extends org.apache.commons.math3.analysis.UnivariateFunction> implements BaseUnivariateOptimizer<FUNC>  {
  final private BaseUnivariateOptimizer<FUNC> optimizer;
  private int maxEvaluations;
  private int totalEvaluations;
  private int starts;
  private RandomGenerator generator;
  private UnivariatePointValuePair[] optima;
  public UnivariateMultiStartOptimizer(final BaseUnivariateOptimizer<FUNC> optimizer, final int starts, final RandomGenerator generator) {
    super();
    if(optimizer == null || generator == null) {
      throw new NullArgumentException();
    }
    if(starts < 1) {
      throw new NotStrictlyPositiveException(starts);
    }
    this.optimizer = optimizer;
    this.starts = starts;
    this.generator = generator;
  }
  public ConvergenceChecker<UnivariatePointValuePair> getConvergenceChecker() {
    return optimizer.getConvergenceChecker();
  }
  public UnivariatePointValuePair optimize(int maxEval, final FUNC f, final GoalType goal, final double min, final double max) {
    return optimize(maxEval, f, goal, min, max, min + 0.5D * (max - min));
  }
  public UnivariatePointValuePair optimize(int maxEval, final FUNC f, final GoalType goal, final double min, final double max, final double startValue) {
    RuntimeException lastException = null;
    optima = new UnivariatePointValuePair[starts];
    totalEvaluations = 0;
    for(int i = 0; i < starts; ++i) {
      try {
        final double s = (i == 0) ? startValue : min + generator.nextDouble() * (max - min);
        optima[i] = optimizer.optimize(maxEval - totalEvaluations, f, goal, min, max, s);
      }
      catch (RuntimeException mue) {
        lastException = mue;
        optima[i] = null;
      }
      totalEvaluations += optimizer.getEvaluations();
    }
    sortPairs(goal);
    UnivariatePointValuePair var_3586 = optima[0];
    if(var_3586 == null) {
      throw lastException;
    }
    return optima[0];
  }
  public UnivariatePointValuePair[] getOptima() {
    if(optima == null) {
      throw new MathIllegalStateException(LocalizedFormats.NO_OPTIMUM_COMPUTED_YET);
    }
    return optima.clone();
  }
  public int getEvaluations() {
    return totalEvaluations;
  }
  public int getMaxEvaluations() {
    return maxEvaluations;
  }
  private void sortPairs(final GoalType goal) {
    Arrays.sort(optima, new Comparator<UnivariatePointValuePair>() {
        public int compare(final UnivariatePointValuePair o1, final UnivariatePointValuePair o2) {
          if(o1 == null) {
            return (o2 == null) ? 0 : 1;
          }
          else 
            if(o2 == null) {
              return -1;
            }
          final double v1 = o1.getValue();
          final double v2 = o2.getValue();
          return (goal == GoalType.MINIMIZE) ? Double.compare(v1, v2) : Double.compare(v2, v1);
        }
    });
  }
}