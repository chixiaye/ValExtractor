package org.apache.commons.math3.optimization;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomVectorGenerator;
@Deprecated() public class BaseMultivariateVectorMultiStartOptimizer<FUNC extends org.apache.commons.math3.analysis.MultivariateVectorFunction> implements BaseMultivariateVectorOptimizer<FUNC>  {
  final private BaseMultivariateVectorOptimizer<FUNC> optimizer;
  private int maxEvaluations;
  private int totalEvaluations;
  private int starts;
  private RandomVectorGenerator generator;
  private PointVectorValuePair[] optima;
  protected BaseMultivariateVectorMultiStartOptimizer(final BaseMultivariateVectorOptimizer<FUNC> optimizer, final int starts, final RandomVectorGenerator generator) {
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
  public ConvergenceChecker<PointVectorValuePair> getConvergenceChecker() {
    return optimizer.getConvergenceChecker();
  }
  public PointVectorValuePair optimize(int maxEval, final FUNC f, double[] target, double[] weights, double[] startPoint) {
    maxEvaluations = maxEval;
    RuntimeException lastException = null;
    optima = new PointVectorValuePair[starts];
    totalEvaluations = 0;
    for(int i = 0; i < starts; ++i) {
      try {
        optima[i] = optimizer.optimize(maxEval - totalEvaluations, f, target, weights, i == 0 ? startPoint : generator.nextVector());
      }
      catch (ConvergenceException oe) {
        optima[i] = null;
      }
      catch (RuntimeException mue) {
        lastException = mue;
        optima[i] = null;
      }
      totalEvaluations += optimizer.getEvaluations();
    }
    sortPairs(target, weights);
    PointVectorValuePair var_3249 = optima[0];
    if(var_3249 == null) {
      throw lastException;
    }
    return optima[0];
  }
  public PointVectorValuePair[] getOptima() {
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
  private void sortPairs(final double[] target, final double[] weights) {
    Arrays.sort(optima, new Comparator<PointVectorValuePair>() {
        public int compare(final PointVectorValuePair o1, final PointVectorValuePair o2) {
          if(o1 == null) {
            return (o2 == null) ? 0 : 1;
          }
          else 
            if(o2 == null) {
              return -1;
            }
          return Double.compare(weightedResidual(o1), weightedResidual(o2));
        }
        private double weightedResidual(final PointVectorValuePair pv) {
          final double[] value = pv.getValueRef();
          double sum = 0;
          for(int i = 0; i < value.length; ++i) {
            final double ri = value[i] - target[i];
            sum += weights[i] * ri * ri;
          }
          return sum;
        }
    });
  }
}