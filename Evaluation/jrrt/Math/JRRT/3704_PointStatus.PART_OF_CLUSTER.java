package org.apache.commons.math3.stat.clustering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.MathUtils;
@Deprecated() public class DBSCANClusterer<T extends org.apache.commons.math3.stat.clustering.Clusterable<org.apache.commons.math3.stat.clustering.DBSCANClusterer@T>>  {
  final private double eps;
  final private int minPts;
  public DBSCANClusterer(final double eps, final int minPts) throws NotPositiveException {
    super();
    if(eps < 0.0D) {
      throw new NotPositiveException(eps);
    }
    if(minPts < 0) {
      throw new NotPositiveException(minPts);
    }
    this.eps = eps;
    this.minPts = minPts;
  }
  private Cluster<T> expandCluster(final Cluster<T> cluster, final T point, final List<T> neighbors, final Collection<T> points, final Map<Clusterable<T>, PointStatus> visited) {
    cluster.addPoint(point);
    visited.put(point, PointStatus.PART_OF_CLUSTER);
    List<T> seeds = new ArrayList<T>(neighbors);
    int index = 0;
    while(index < seeds.size()){
      final T current = seeds.get(index);
      PointStatus pStatus = visited.get(current);
      if(pStatus == null) {
        final List<T> currentNeighbors = getNeighbors(current, points);
        if(currentNeighbors.size() >= minPts) {
          seeds = merge(seeds, currentNeighbors);
        }
      }
      if(pStatus != PointStatus.PART_OF_CLUSTER) {
        PointStatus var_3704 = PointStatus.PART_OF_CLUSTER;
        visited.put(current, var_3704);
        cluster.addPoint(current);
      }
      index++;
    }
    return cluster;
  }
  public List<Cluster<T>> cluster(final Collection<T> points) throws NullArgumentException {
    MathUtils.checkNotNull(points);
    final List<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
    final Map<Clusterable<T>, PointStatus> visited = new HashMap<Clusterable<T>, PointStatus>();
    for (final T point : points) {
      if(visited.get(point) != null) {
        continue ;
      }
      final List<T> neighbors = getNeighbors(point, points);
      if(neighbors.size() >= minPts) {
        final Cluster<T> cluster = new Cluster<T>(null);
        clusters.add(expandCluster(cluster, point, neighbors, points, visited));
      }
      else {
        visited.put(point, PointStatus.NOISE);
      }
    }
    return clusters;
  }
  private List<T> getNeighbors(final T point, final Collection<T> points) {
    final List<T> neighbors = new ArrayList<T>();
    for (final T neighbor : points) {
      if(point != neighbor && neighbor.distanceFrom(point) <= eps) {
        neighbors.add(neighbor);
      }
    }
    return neighbors;
  }
  private List<T> merge(final List<T> one, final List<T> two) {
    final Set<T> oneSet = new HashSet<T>(one);
    for (T item : two) {
      if(!oneSet.contains(item)) {
        one.add(item);
      }
    }
    return one;
  }
  public double getEps() {
    return eps;
  }
  public int getMinPts() {
    return minPts;
  }
  private enum PointStatus {
    NOISE(),

    PART_OF_CLUSTER(),

  ;
  private PointStatus() {
  }
  }
}