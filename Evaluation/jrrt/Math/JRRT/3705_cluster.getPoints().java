package org.apache.commons.math3.stat.clustering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.MathUtils;
@Deprecated() public class KMeansPlusPlusClusterer<T extends org.apache.commons.math3.stat.clustering.Clusterable<org.apache.commons.math3.stat.clustering.KMeansPlusPlusClusterer@T>>  {
  final private Random random;
  final private EmptyClusterStrategy emptyStrategy;
  public KMeansPlusPlusClusterer(final Random random) {
    this(random, EmptyClusterStrategy.LARGEST_VARIANCE);
  }
  public KMeansPlusPlusClusterer(final Random random, final EmptyClusterStrategy emptyStrategy) {
    super();
    this.random = random;
    this.emptyStrategy = emptyStrategy;
  }
  private static  <T extends org.apache.commons.math3.stat.clustering.Clusterable<org.apache.commons.math3.stat.clustering.T>> List<Cluster<T>> chooseInitialCenters(final Collection<T> points, final int k, final Random random) {
    final List<T> pointList = Collections.unmodifiableList(new ArrayList<T>(points));
    final int numPoints = pointList.size();
    final boolean[] taken = new boolean[numPoints];
    final List<Cluster<T>> resultSet = new ArrayList<Cluster<T>>();
    final int firstPointIndex = random.nextInt(numPoints);
    final T firstPoint = pointList.get(firstPointIndex);
    resultSet.add(new Cluster<T>(firstPoint));
    taken[firstPointIndex] = true;
    final double[] minDistSquared = new double[numPoints];
    for(int i = 0; i < numPoints; i++) {
      if(i != firstPointIndex) {
        double d = firstPoint.distanceFrom(pointList.get(i));
        minDistSquared[i] = d * d;
      }
    }
    while(resultSet.size() < k){
      double distSqSum = 0.0D;
      for(int i = 0; i < numPoints; i++) {
        if(!taken[i]) {
          distSqSum += minDistSquared[i];
        }
      }
      final double r = random.nextDouble() * distSqSum;
      int nextPointIndex = -1;
      double sum = 0.0D;
      for(int i = 0; i < numPoints; i++) {
        if(!taken[i]) {
          sum += minDistSquared[i];
          if(sum >= r) {
            nextPointIndex = i;
            break ;
          }
        }
      }
      if(nextPointIndex == -1) {
        for(int i = numPoints - 1; i >= 0; i--) {
          if(!taken[i]) {
            nextPointIndex = i;
            break ;
          }
        }
      }
      if(nextPointIndex >= 0) {
        final T p = pointList.get(nextPointIndex);
        resultSet.add(new Cluster<T>(p));
        taken[nextPointIndex] = true;
        if(resultSet.size() < k) {
          for(int j = 0; j < numPoints; j++) {
            if(!taken[j]) {
              double d = p.distanceFrom(pointList.get(j));
              double d2 = d * d;
              if(d2 < minDistSquared[j]) {
                minDistSquared[j] = d2;
              }
            }
          }
        }
      }
      else {
        break ;
      }
    }
    return resultSet;
  }
  public List<Cluster<T>> cluster(final Collection<T> points, final int k, final int maxIterations) throws MathIllegalArgumentException, ConvergenceException {
    MathUtils.checkNotNull(points);
    if(points.size() < k) {
      throw new NumberIsTooSmallException(points.size(), k, false);
    }
    List<Cluster<T>> clusters = chooseInitialCenters(points, k, random);
    int[] assignments = new int[points.size()];
    assignPointsToClusters(clusters, points, assignments);
    final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
    for(int count = 0; count < max; count++) {
      boolean emptyCluster = false;
      List<Cluster<T>> newClusters = new ArrayList<Cluster<T>>();
      for (final Cluster<T> cluster : clusters) {
        final T newCenter;
        if(cluster.getPoints().isEmpty()) {
          switch (emptyStrategy){
            case LARGEST_VARIANCE:
            newCenter = getPointFromLargestVarianceCluster(clusters);
            break ;
            case LARGEST_POINTS_NUMBER:
            newCenter = getPointFromLargestNumberCluster(clusters);
            break ;
            case FARTHEST_POINT:
            newCenter = getFarthestPoint(clusters);
            break ;
            default:
            throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
          }
          emptyCluster = true;
        }
        else {
          newCenter = cluster.getCenter().centroidOf(cluster.getPoints());
        }
        newClusters.add(new Cluster<T>(newCenter));
      }
      int changes = assignPointsToClusters(newClusters, points, assignments);
      clusters = newClusters;
      if(changes == 0 && !emptyCluster) {
        return clusters;
      }
    }
    return clusters;
  }
  public List<Cluster<T>> cluster(final Collection<T> points, final int k, int numTrials, int maxIterationsPerTrial) throws MathIllegalArgumentException, ConvergenceException {
    List<Cluster<T>> best = null;
    double bestVarianceSum = Double.POSITIVE_INFINITY;
    for(int i = 0; i < numTrials; ++i) {
      List<Cluster<T>> clusters = cluster(points, k, maxIterationsPerTrial);
      double varianceSum = 0.0D;
      for (final Cluster<T> cluster : clusters) {
        List<T> var_3705 = cluster.getPoints();
        if(!var_3705.isEmpty()) {
          final T center = cluster.getCenter();
          final Variance stat = new Variance();
          for (final T point : cluster.getPoints()) {
            stat.increment(point.distanceFrom(center));
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
  private T getFarthestPoint(final Collection<Cluster<T>> clusters) throws ConvergenceException {
    double maxDistance = Double.NEGATIVE_INFINITY;
    Cluster<T> selectedCluster = null;
    int selectedPoint = -1;
    for (final Cluster<T> cluster : clusters) {
      final T center = cluster.getCenter();
      final List<T> points = cluster.getPoints();
      for(int i = 0; i < points.size(); ++i) {
        final double distance = points.get(i).distanceFrom(center);
        if(distance > maxDistance) {
          maxDistance = distance;
          selectedCluster = cluster;
          selectedPoint = i;
        }
      }
    }
    if(selectedCluster == null) {
      throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
    }
    return selectedCluster.getPoints().remove(selectedPoint);
  }
  private T getPointFromLargestNumberCluster(final Collection<Cluster<T>> clusters) throws ConvergenceException {
    int maxNumber = 0;
    Cluster<T> selected = null;
    for (final Cluster<T> cluster : clusters) {
      final int number = cluster.getPoints().size();
      if(number > maxNumber) {
        maxNumber = number;
        selected = cluster;
      }
    }
    if(selected == null) {
      throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
    }
    final List<T> selectedPoints = selected.getPoints();
    return selectedPoints.remove(random.nextInt(selectedPoints.size()));
  }
  private T getPointFromLargestVarianceCluster(final Collection<Cluster<T>> clusters) throws ConvergenceException {
    double maxVariance = Double.NEGATIVE_INFINITY;
    Cluster<T> selected = null;
    for (final Cluster<T> cluster : clusters) {
      if(!cluster.getPoints().isEmpty()) {
        final T center = cluster.getCenter();
        final Variance stat = new Variance();
        for (final T point : cluster.getPoints()) {
          stat.increment(point.distanceFrom(center));
        }
        final double variance = stat.getResult();
        if(variance > maxVariance) {
          maxVariance = variance;
          selected = cluster;
        }
      }
    }
    if(selected == null) {
      throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
    }
    final List<T> selectedPoints = selected.getPoints();
    return selectedPoints.remove(random.nextInt(selectedPoints.size()));
  }
  private static  <T extends org.apache.commons.math3.stat.clustering.Clusterable<org.apache.commons.math3.stat.clustering.T>> int assignPointsToClusters(final List<Cluster<T>> clusters, final Collection<T> points, final int[] assignments) {
    int assignedDifferently = 0;
    int pointIndex = 0;
    for (final T p : points) {
      int clusterIndex = getNearestCluster(clusters, p);
      if(clusterIndex != assignments[pointIndex]) {
        assignedDifferently++;
      }
      Cluster<T> cluster = clusters.get(clusterIndex);
      cluster.addPoint(p);
      assignments[pointIndex++] = clusterIndex;
    }
    return assignedDifferently;
  }
  private static  <T extends org.apache.commons.math3.stat.clustering.Clusterable<org.apache.commons.math3.stat.clustering.T>> int getNearestCluster(final Collection<Cluster<T>> clusters, final T point) {
    double minDistance = Double.MAX_VALUE;
    int clusterIndex = 0;
    int minCluster = 0;
    for (final Cluster<T> c : clusters) {
      final double distance = point.distanceFrom(c.getCenter());
      if(distance < minDistance) {
        minDistance = distance;
        minCluster = clusterIndex;
      }
      clusterIndex++;
    }
    return minCluster;
  }
  public static enum EmptyClusterStrategy {
    LARGEST_VARIANCE(),

    LARGEST_POINTS_NUMBER(),

    FARTHEST_POINT(),

    ERROR(),

  ;
  private EmptyClusterStrategy() {
  }
  }
}