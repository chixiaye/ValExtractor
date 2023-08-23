package org.apache.commons.math3.stat.inference;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.MathUtils;

public class OneWayAnova  {
  public OneWayAnova() {
    super();
  }
  private AnovaStats anovaStats(final Collection<double[]> categoryData) throws NullArgumentException, DimensionMismatchException {
    MathUtils.checkNotNull(categoryData);
    final Collection<SummaryStatistics> categoryDataSummaryStatistics = new ArrayList<SummaryStatistics>(categoryData.size());
    for (final double[] data : categoryData) {
      final SummaryStatistics dataSummaryStatistics = new SummaryStatistics();
      categoryDataSummaryStatistics.add(dataSummaryStatistics);
      for (final double val : data) {
        dataSummaryStatistics.addValue(val);
      }
    }
    return anovaStats(categoryDataSummaryStatistics, false);
  }
  private AnovaStats anovaStats(final Collection<SummaryStatistics> categoryData, final boolean allowOneElementData) throws NullArgumentException, DimensionMismatchException {
    MathUtils.checkNotNull(categoryData);
    if(!allowOneElementData) {
      if(categoryData.size() < 2) {
        throw new DimensionMismatchException(LocalizedFormats.TWO_OR_MORE_CATEGORIES_REQUIRED, categoryData.size(), 2);
      }
      for (final SummaryStatistics array : categoryData) {
        long var_3861 = array.getN();
        if(var_3861 <= 1) {
          throw new DimensionMismatchException(LocalizedFormats.TWO_OR_MORE_VALUES_IN_CATEGORY_REQUIRED, (int)array.getN(), 2);
        }
      }
    }
    int dfwg = 0;
    double sswg = 0;
    double totsum = 0;
    double totsumsq = 0;
    int totnum = 0;
    for (final SummaryStatistics data : categoryData) {
      final double sum = data.getSum();
      final double sumsq = data.getSumsq();
      final int num = (int)data.getN();
      totnum += num;
      totsum += sum;
      totsumsq += sumsq;
      dfwg += num - 1;
      final double ss = sumsq - ((sum * sum) / num);
      sswg += ss;
    }
    final double sst = totsumsq - ((totsum * totsum) / totnum);
    final double ssbg = sst - sswg;
    final int dfbg = categoryData.size() - 1;
    final double msbg = ssbg / dfbg;
    final double mswg = sswg / dfwg;
    final double F = msbg / mswg;
    return new AnovaStats(dfbg, dfwg, F);
  }
  public boolean anovaTest(final Collection<double[]> categoryData, final double alpha) throws NullArgumentException, DimensionMismatchException, OutOfRangeException, ConvergenceException, MaxCountExceededException {
    if((alpha <= 0) || (alpha > 0.5D)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0, 0.5D);
    }
    return anovaPValue(categoryData) < alpha;
  }
  public double anovaFValue(final Collection<double[]> categoryData) throws NullArgumentException, DimensionMismatchException {
    AnovaStats a = anovaStats(categoryData);
    return a.F;
  }
  public double anovaPValue(final Collection<double[]> categoryData) throws NullArgumentException, DimensionMismatchException, ConvergenceException, MaxCountExceededException {
    AnovaStats a = anovaStats(categoryData);
    FDistribution fdist = new FDistribution(a.dfbg, a.dfwg);
    return 1.0D - fdist.cumulativeProbability(a.F);
  }
  public double anovaPValue(final Collection<SummaryStatistics> categoryData, final boolean allowOneElementData) throws NullArgumentException, DimensionMismatchException, ConvergenceException, MaxCountExceededException {
    final AnovaStats a = anovaStats(categoryData, allowOneElementData);
    final FDistribution fdist = new FDistribution(a.dfbg, a.dfwg);
    return 1.0D - fdist.cumulativeProbability(a.F);
  }
  
  private static class AnovaStats  {
    final private int dfbg;
    final private int dfwg;
    final private double F;
    private AnovaStats(int dfbg, int dfwg, double F) {
      super();
      this.dfbg = dfbg;
      this.dfwg = dfwg;
      this.F = F;
    }
  }
}