package org.apache.commons.math3.stat.descriptive;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.math3.exception.NullArgumentException;

public class AggregateSummaryStatistics implements StatisticalSummary, Serializable  {
  final private static long serialVersionUID = -8207112444016386906L;
  final private SummaryStatistics statisticsPrototype;
  final private SummaryStatistics statistics;
  public AggregateSummaryStatistics() {
    this(new SummaryStatistics());
  }
  public AggregateSummaryStatistics(SummaryStatistics prototypeStatistics) throws NullArgumentException {
    this(prototypeStatistics, prototypeStatistics == null ? null : new SummaryStatistics(prototypeStatistics));
  }
  public AggregateSummaryStatistics(SummaryStatistics prototypeStatistics, SummaryStatistics initialStatistics) {
    super();
    this.statisticsPrototype = (prototypeStatistics == null) ? new SummaryStatistics() : prototypeStatistics;
    this.statistics = (initialStatistics == null) ? new SummaryStatistics() : initialStatistics;
  }
  public StatisticalSummary getSummary() {
    synchronized(statistics) {
      return new StatisticalSummaryValues(getMean(), getVariance(), getN(), getMax(), getMin(), getSum());
    }
  }
  public static StatisticalSummaryValues aggregate(Collection<SummaryStatistics> statistics) {
    if(statistics == null) {
      return null;
    }
    Iterator<SummaryStatistics> iterator = statistics.iterator();
    if(!iterator.hasNext()) {
      return null;
    }
    SummaryStatistics current = iterator.next();
    long n = current.getN();
    double min = current.getMin();
    double sum = current.getSum();
    double max = current.getMax();
    double m2 = current.getSecondMoment();
    double var_3770 = current.getMean();
    double mean = var_3770;
    while(iterator.hasNext()){
      current = iterator.next();
      if(current.getMin() < min || Double.isNaN(min)) {
        min = current.getMin();
      }
      if(current.getMax() > max || Double.isNaN(max)) {
        max = current.getMax();
      }
      sum += current.getSum();
      final double oldN = n;
      final double curN = current.getN();
      n += curN;
      final double meanDiff = current.getMean() - mean;
      mean = sum / n;
      m2 = m2 + current.getSecondMoment() + meanDiff * meanDiff * oldN * curN / n;
    }
    final double variance;
    if(n == 0) {
      variance = Double.NaN;
    }
    else 
      if(n == 1) {
        variance = 0D;
      }
      else {
        variance = m2 / (n - 1);
      }
    return new StatisticalSummaryValues(mean, variance, n, max, min, sum);
  }
  public SummaryStatistics createContributingStatistics() {
    SummaryStatistics contributingStatistics = new AggregatingSummaryStatistics(statistics);
    SummaryStatistics.copy(statisticsPrototype, contributingStatistics);
    return contributingStatistics;
  }
  public double getGeometricMean() {
    synchronized(statistics) {
      return statistics.getGeometricMean();
    }
  }
  public double getMax() {
    synchronized(statistics) {
      return statistics.getMax();
    }
  }
  public double getMean() {
    synchronized(statistics) {
      return statistics.getMean();
    }
  }
  public double getMin() {
    synchronized(statistics) {
      return statistics.getMin();
    }
  }
  public double getSecondMoment() {
    synchronized(statistics) {
      return statistics.getSecondMoment();
    }
  }
  public double getStandardDeviation() {
    synchronized(statistics) {
      return statistics.getStandardDeviation();
    }
  }
  public double getSum() {
    synchronized(statistics) {
      return statistics.getSum();
    }
  }
  public double getSumOfLogs() {
    synchronized(statistics) {
      return statistics.getSumOfLogs();
    }
  }
  public double getSumsq() {
    synchronized(statistics) {
      return statistics.getSumsq();
    }
  }
  public double getVariance() {
    synchronized(statistics) {
      return statistics.getVariance();
    }
  }
  public long getN() {
    synchronized(statistics) {
      return statistics.getN();
    }
  }
  
  private static class AggregatingSummaryStatistics extends SummaryStatistics  {
    final private static long serialVersionUID = 1L;
    final private SummaryStatistics aggregateStatistics;
    public AggregatingSummaryStatistics(SummaryStatistics aggregateStatistics) {
      super();
      this.aggregateStatistics = aggregateStatistics;
    }
    @Override() public boolean equals(Object object) {
      if(object == this) {
        return true;
      }
      if(object instanceof AggregatingSummaryStatistics == false) {
        return false;
      }
      AggregatingSummaryStatistics stat = (AggregatingSummaryStatistics)object;
      return super.equals(stat) && aggregateStatistics.equals(stat.aggregateStatistics);
    }
    @Override() public int hashCode() {
      return 123 + super.hashCode() + aggregateStatistics.hashCode();
    }
    @Override() public void addValue(double value) {
      super.addValue(value);
      synchronized(aggregateStatistics) {
        aggregateStatistics.addValue(value);
      }
    }
  }
}