package org.apache.commons.math3.ml.clustering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.MathUtils;
public class KMeansPlusPlusClusterer<T extends org.apache.commons.math3.ml.clustering.Clusterable> extends Clusterer<T>  {
  final private int k;
  final private int maxIterations;
  final private RandomGenerator random;
  final private EmptyClusterStrategy emptyStrategy;
  public KMeansPlusPlusClusterer(final int k) {
    this(k, -1);
  }
  public KMeansPlusPlusClusterer(final int k, final int maxIterations) {
    this(k, maxIterations, new EuclideanDistance());
  }
  public KMeansPlusPlusClusterer(final int k, final int maxIterations, final DistanceMeasure measure) {
    this(k, maxIterations, measure, new JDKRandomGenerator());
  }
  public KMeansPlusPlusClusterer(final int k, final int maxIterations, final DistanceMeasure measure, final RandomGenerator random) {
    this(k, maxIterations, measure, random, EmptyClusterStrategy.LARGEST_VARIANCE);
  }
  public KMeansPlusPlusClusterer(final int k, final int maxIterations, final DistanceMeasure measure, final RandomGenerator random, final EmptyClusterStrategy emptyStrategy) {
    super(measure);
    this.k = k;
    this.maxIterations = maxIterations;
    this.random = random;
    this.emptyStrategy = emptyStrategy;
  }
  private Clusterable centroidOf(final Collection<T> points, final int dimension) {
    final double[] centroid = new double[dimension];
    for (final T p : points) {
      final double[] point = p.getPoint();
      for(int i = 0; i < centroid.length; i++) {
        centroid[i] += point[i];
      }
    }
    for(int i = 0; i < centroid.length; i++) {
      centroid[i] /= points.size();
    }
    return new DoublePoint(centroid);
  }
  public EmptyClusterStrategy getEmptyClusterStrategy() {
    return emptyStrategy;
  }
  private List<CentroidCluster<T>> chooseInitialCenters(final Collection<T> points) {
    final List<T> pointList = Collections.unmodifiableList(new ArrayList<T>(points));
    final int numPoints = pointList.size();
    final boolean[] taken = new boolean[numPoints];
    final List<CentroidCluster<T>> resultSet = new ArrayList<CentroidCluster<T>>();
    final int firstPointIndex = random.nextInt(numPoints);
    final T firstPoint = pointList.get(firstPointIndex);
    resultSet.add(new CentroidCluster<T>(firstPoint));
    taken[firstPointIndex] = true;
    final double[] minDistSquared = new double[numPoints];
    for(int i = 0; i < numPoints; i++) {
      if(i != firstPointIndex) {
        double d = distance(firstPoint, pointList.get(i));
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
          double var_2519 = minDistSquared[i];
          sum += var_2519;
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
        resultSet.add(new CentroidCluster<T>(p));
        taken[nextPointIndex] = true;
        if(resultSet.size() < k) {
          for(int j = 0; j < numPoints; j++) {
            if(!taken[j]) {
              double d = distance(p, pointList.get(j));
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
  @Override() public List<CentroidCluster<T>> cluster(final Collection<T> points) throws MathIllegalArgumentException, ConvergenceException {
    MathUtils.checkNotNull(points);
    if(points.size() < k) {
      throw new NumberIsTooSmallException(points.size(), k, false);
    }
    List<CentroidCluster<T>> clusters = chooseInitialCenters(points);
    int[] assignments = new int[points.size()];
    assignPointsToClusters(clusters, points, assignments);
    final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
    for(int count = 0; count < max; count++) {
      boolean emptyCluster = false;
      List<CentroidCluster<T>> newClusters = new ArrayList<CentroidCluster<T>>();
      for (final CentroidCluster<T> cluster : clusters) {
        final Clusterable newCenter;
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
          newCenter = centroidOf(cluster.getPoints(), cluster.getCenter().getPoint().length);
        }
        newClusters.add(new CentroidCluster<T>(newCenter));
      }
      int changes = assignPointsToClusters(newClusters, points, assignments);
      clusters = newClusters;
      if(changes == 0 && !emptyCluster) {
        return clusters;
      }
    }
    return clusters;
  }
  public RandomGenerator getRandomGenerator() {
    return random;
  }
  private T getFarthestPoint(final Collection<CentroidCluster<T>> clusters) throws ConvergenceException {
    double maxDistance = Double.NEGATIVE_INFINITY;
    Cluster<T> selectedCluster = null;
    int selectedPoint = -1;
    for (final CentroidCluster<T> cluster : clusters) {
      final Clusterable center = cluster.getCenter();
      final List<T> points = cluster.getPoints();
      for(int i = 0; i < points.size(); ++i) {
        final double distance = distance(points.get(i), center);
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
  private T getPointFromLargestNumberCluster(final Collection<? extends Cluster<T>> clusters) throws ConvergenceException {
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
  private T getPointFromLargestVarianceCluster(final Collection<CentroidCluster<T>> clusters) throws ConvergenceException {
    double maxVariance = Double.NEGATIVE_INFINITY;
    Cluster<T> selected = null;
    for (final CentroidCluster<T> cluster : clusters) {
      if(!cluster.getPoints().isEmpty()) {
        final Clusterable center = cluster.getCenter();
        final Variance stat = new Variance();
        for (final T point : cluster.getPoints()) {
          stat.increment(distance(point, center));
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
  private int assignPointsToClusters(final List<CentroidCluster<T>> clusters, final Collection<T> points, final int[] assignments) {
    int assignedDifferently = 0;
    int pointIndex = 0;
    for (final T p : points) {
      int clusterIndex = getNearestCluster(clusters, p);
      if(clusterIndex != assignments[pointIndex]) {
        assignedDifferently++;
      }
      CentroidCluster<T> cluster = clusters.get(clusterIndex);
      cluster.addPoint(p);
      assignments[pointIndex++] = clusterIndex;
    }
    return assignedDifferently;
  }
  public int getK() {
    return k;
  }
  public int getMaxIterations() {
    return maxIterations;
  }
  private int getNearestCluster(final Collection<CentroidCluster<T>> clusters, final T point) {
    double minDistance = Double.MAX_VALUE;
    int clusterIndex = 0;
    int minCluster = 0;
    for (final CentroidCluster<T> c : clusters) {
      final double distance = distance(point, c.getCenter());
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