package org.apache.commons.math3.optim;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.random.RandomVectorGenerator;
abstract public class BaseMultiStartMultivariateOptimizer<PAIR extends java.lang.Object> extends BaseMultivariateOptimizer<PAIR>  {
  final private BaseMultivariateOptimizer<PAIR> optimizer;
  private int totalEvaluations;
  private int starts;
  private RandomVectorGenerator generator;
  private OptimizationData[] optimData;
  private int maxEvalIndex = -1;
  private int initialGuessIndex = -1;
  public BaseMultiStartMultivariateOptimizer(final BaseMultivariateOptimizer<PAIR> optimizer, final int starts, final RandomVectorGenerator generator) {
    super(optimizer.getConvergenceChecker());
    if(starts < 1) {
      throw new NotStrictlyPositiveException(starts);
    }
    this.optimizer = optimizer;
    this.starts = starts;
    this.generator = generator;
  }
  @Override() protected PAIR doOptimize() {
    for(int i = 0; i < optimData.length; i++) {
      if(optimData[i] instanceof MaxEval) {
        optimData[i] = null;
        maxEvalIndex = i;
      }
      if(optimData[i] instanceof InitialGuess) {
        optimData[i] = null;
        initialGuessIndex = i;
        continue ;
      }
    }
    if(maxEvalIndex == -1) {
      throw new MathIllegalStateException();
    }
    if(initialGuessIndex == -1) {
      throw new MathIllegalStateException();
    }
    RuntimeException lastException = null;
    totalEvaluations = 0;
    clear();
    final int maxEval = getMaxEvaluations();
    final double[] min = getLowerBound();
    final double[] max = getUpperBound();
    final double[] startPoint = getStartPoint();
    for(int i = 0; i < starts; i++) {
      try {
        optimData[maxEvalIndex] = new MaxEval(maxEval - totalEvaluations);
        double[] s = null;
        if(i == 0) {
          s = startPoint;
        }
        else {
          int attempts = 0;
          while(s == null){
            if(attempts++ >= getMaxEvaluations()) {
              throw new TooManyEvaluationsException(getMaxEvaluations());
            }
            s = generator.nextVector();
            for(int k = 0; s != null && k < s.length; ++k) {
              double var_2922 = s[k];
              if((min != null && s[k] < min[k]) || (max != null && var_2922 > max[k])) {
                s = null;
              }
            }
          }
        }
        optimData[initialGuessIndex] = new InitialGuess(s);
        final PAIR result = optimizer.optimize(optimData);
        store(result);
      }
      catch (RuntimeException mue) {
        lastException = mue;
      }
      totalEvaluations += optimizer.getEvaluations();
    }
    final PAIR[] optima = getOptima();
    if(optima.length == 0) {
      throw lastException;
    }
    return optima[0];
  }
  @Override() public PAIR optimize(OptimizationData ... optData) {
    optimData = optData;
    return super.optimize(optData);
  }
  abstract public PAIR[] getOptima();
  @Override() public int getEvaluations() {
    return totalEvaluations;
  }
  abstract protected void clear();
  abstract protected void store(PAIR optimum);
}