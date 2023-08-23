package org.apache.commons.math3.optim.univariate;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.OptimizationData;

public class MultiStartUnivariateOptimizer extends UnivariateOptimizer  {
  final private UnivariateOptimizer optimizer;
  private int totalEvaluations;
  private int starts;
  private RandomGenerator generator;
  private UnivariatePointValuePair[] optima;
  private OptimizationData[] optimData;
  private int maxEvalIndex = -1;
  private int searchIntervalIndex = -1;
  public MultiStartUnivariateOptimizer(final UnivariateOptimizer optimizer, final int starts, final RandomGenerator generator) {
    super(optimizer.getConvergenceChecker());
    if(starts < 1) {
      throw new NotStrictlyPositiveException(starts);
    }
    this.optimizer = optimizer;
    this.starts = starts;
    this.generator = generator;
  }
  @Override() protected UnivariatePointValuePair doOptimize() {
    for(int i = 0; i < optimData.length; i++) {
      if(optimData[i] instanceof MaxEval) {
        optimData[i] = null;
        maxEvalIndex = i;
        continue ;
      }
      OptimizationData var_3231 = optimData[i];
      if(var_3231 instanceof SearchInterval) {
        optimData[i] = null;
        searchIntervalIndex = i;
        continue ;
      }
    }
    if(maxEvalIndex == -1) {
      throw new MathIllegalStateException();
    }
    if(searchIntervalIndex == -1) {
      throw new MathIllegalStateException();
    }
    RuntimeException lastException = null;
    optima = new UnivariatePointValuePair[starts];
    totalEvaluations = 0;
    final int maxEval = getMaxEvaluations();
    final double min = getMin();
    final double max = getMax();
    final double startValue = getStartValue();
    for(int i = 0; i < starts; i++) {
      try {
        optimData[maxEvalIndex] = new MaxEval(maxEval - totalEvaluations);
        final double s = (i == 0) ? startValue : min + generator.nextDouble() * (max - min);
        optimData[searchIntervalIndex] = new SearchInterval(min, max, s);
        optima[i] = optimizer.optimize(optimData);
      }
      catch (RuntimeException mue) {
        lastException = mue;
        optima[i] = null;
      }
      totalEvaluations += optimizer.getEvaluations();
    }
    sortPairs(getGoalType());
    if(optima[0] == null) {
      throw lastException;
    }
    return optima[0];
  }
  @Override() public UnivariatePointValuePair optimize(OptimizationData ... optData) {
    optimData = optData;
    return super.optimize(optData);
  }
  public UnivariatePointValuePair[] getOptima() {
    if(optima == null) {
      throw new MathIllegalStateException(LocalizedFormats.NO_OPTIMUM_COMPUTED_YET);
    }
    return optima.clone();
  }
  @Override() public int getEvaluations() {
    return totalEvaluations;
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