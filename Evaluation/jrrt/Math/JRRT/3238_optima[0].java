package org.apache.commons.math3.optimization;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomVectorGenerator;
@Deprecated() public class BaseMultivariateMultiStartOptimizer<FUNC extends org.apache.commons.math3.analysis.MultivariateFunction> implements BaseMultivariateOptimizer<FUNC>  {
  final private BaseMultivariateOptimizer<FUNC> optimizer;
  private int maxEvaluations;
  private int totalEvaluations;
  private int starts;
  private RandomVectorGenerator generator;
  private PointValuePair[] optima;
  protected BaseMultivariateMultiStartOptimizer(final BaseMultivariateOptimizer<FUNC> optimizer, final int starts, final RandomVectorGenerator generator) {
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
  public ConvergenceChecker<PointValuePair> getConvergenceChecker() {
    return optimizer.getConvergenceChecker();
  }
  public PointValuePair optimize(int maxEval, final FUNC f, final GoalType goal, double[] startPoint) {
    maxEvaluations = maxEval;
    RuntimeException lastException = null;
    optima = new PointValuePair[starts];
    totalEvaluations = 0;
    for(int i = 0; i < starts; ++i) {
      try {
        optima[i] = optimizer.optimize(maxEval - totalEvaluations, f, goal, i == 0 ? startPoint : generator.nextVector());
      }
      catch (RuntimeException mue) {
        lastException = mue;
        optima[i] = null;
      }
      totalEvaluations += optimizer.getEvaluations();
    }
    sortPairs(goal);
    if(optima[0] == null) {
      throw lastException;
    }
    PointValuePair var_3238 = optima[0];
    return var_3238;
  }
  public PointValuePair[] getOptima() {
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
    Arrays.sort(optima, new Comparator<PointValuePair>() {
        public int compare(final PointValuePair o1, final PointValuePair o2) {
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