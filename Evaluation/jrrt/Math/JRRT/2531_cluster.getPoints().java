package org.apache.commons.math3.ml.clustering;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
public class MultiKMeansPlusPlusClusterer<T extends org.apache.commons.math3.ml.clustering.Clusterable> extends Clusterer<T>  {
  final private KMeansPlusPlusClusterer<T> clusterer;
  final private int numTrials;
  public MultiKMeansPlusPlusClusterer(final KMeansPlusPlusClusterer<T> clusterer, final int numTrials) {
    super(clusterer.getDistanceMeasure());
    this.clusterer = clusterer;
    this.numTrials = numTrials;
  }
  public KMeansPlusPlusClusterer<T> getClusterer() {
    return clusterer;
  }
  @Override() public List<CentroidCluster<T>> cluster(final Collection<T> points) throws MathIllegalArgumentException, ConvergenceException {
    List<CentroidCluster<T>> best = null;
    double bestVarianceSum = Double.POSITIVE_INFINITY;
    for(int i = 0; i < numTrials; ++i) {
      List<CentroidCluster<T>> clusters = clusterer.cluster(points);
      double varianceSum = 0.0D;
      for (final CentroidCluster<T> cluster : clusters) {
        if(!cluster.getPoints().isEmpty()) {
          final Clusterable center = cluster.getCenter();
          final Variance stat = new Variance();
          List<T> var_2531 = cluster.getPoints();
          for (final T point : var_2531) {
            stat.increment(distance(point, center));
          }
          varianceSum += stat.getResult();
        }
      }
      if(varianceSum <= bestVarianceSum) {
        best = clusters;
        bestVarianceSum = varianceSum;
      }
    }
    return best;
  }
  public int getNumTrials() {
    return numTrials;
  }
}