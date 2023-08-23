package org.apache.commons.math3.ml.clustering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathUtils;
public class FuzzyKMeansClusterer<T extends org.apache.commons.math3.ml.clustering.Clusterable> extends Clusterer<T>  {
  final private static double DEFAULT_EPSILON = 1e-3D;
  final private int k;
  final private int maxIterations;
  final private double fuzziness;
  final private double epsilon;
  final private RandomGenerator random;
  private double[][] membershipMatrix;
  private List<T> points;
  private List<CentroidCluster<T>> clusters;
  public FuzzyKMeansClusterer(final int k, final double fuzziness) throws NumberIsTooSmallException {
    this(k, fuzziness, -1, new EuclideanDistance());
  }
  public FuzzyKMeansClusterer(final int k, final double fuzziness, final int maxIterations, final DistanceMeasure measure) throws NumberIsTooSmallException {
    this(k, fuzziness, maxIterations, measure, DEFAULT_EPSILON, new JDKRandomGenerator());
  }
  public FuzzyKMeansClusterer(final int k, final double fuzziness, final int maxIterations, final DistanceMeasure measure, final double epsilon, final RandomGenerator random) throws NumberIsTooSmallException {
    super(measure);
    if(fuzziness <= 1.0D) {
      throw new NumberIsTooSmallException(fuzziness, 1.0D, false);
    }
    this.k = k;
    this.fuzziness = fuzziness;
    this.maxIterations = maxIterations;
    this.epsilon = epsilon;
    this.random = random;
    this.membershipMatrix = null;
    this.points = null;
    this.clusters = null;
  }
  @Override() public List<CentroidCluster<T>> cluster(final Collection<T> dataPoints) throws MathIllegalArgumentException {
    MathUtils.checkNotNull(dataPoints);
    final int size = dataPoints.size();
    if(size < k) {
      throw new NumberIsTooSmallException(size, k, false);
    }
    points = Collections.unmodifiableList(new ArrayList<T>(dataPoints));
    clusters = new ArrayList<CentroidCluster<T>>();
    membershipMatrix = new double[size][k];
    final double[][] oldMatrix = new double[size][k];
    if(size == 0) {
      return clusters;
    }
    initializeMembershipMatrix();
    final int pointDimension = points.get(0).getPoint().length;
    for(int i = 0; i < k; i++) {
      clusters.add(new CentroidCluster<T>(new DoublePoint(new double[pointDimension])));
    }
    int iteration = 0;
    final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
    double difference = 0.0D;
    do {
      saveMembershipMatrix(oldMatrix);
      updateClusterCenters();
      updateMembershipMatrix();
      difference = calculateMaxMembershipChange(oldMatrix);
    }while(difference > epsilon && ++iteration < max);
    return clusters;
  }
  public List<CentroidCluster<T>> getClusters() {
    return clusters;
  }
  public List<T> getDataPoints() {
    return points;
  }
  public RandomGenerator getRandomGenerator() {
    return random;
  }
  public RealMatrix getMembershipMatrix() {
    if(membershipMatrix == null) {
      throw new MathIllegalStateException();
    }
    return MatrixUtils.createRealMatrix(membershipMatrix);
  }
  private double calculateMaxMembershipChange(final double[][] matrix) {
    double maxMembership = 0.0D;
    for(int i = 0; i < points.size(); i++) {
      for(int j = 0; j < clusters.size(); j++) {
        double v = FastMath.abs(membershipMatrix[i][j] - matrix[i][j]);
        maxMembership = FastMath.max(v, maxMembership);
      }
    }
    return maxMembership;
  }
  public double getEpsilon() {
    return epsilon;
  }
  public double getFuzziness() {
    return fuzziness;
  }
  public double getObjectiveFunctionValue() {
    if(points == null || clusters == null) {
      throw new MathIllegalStateException();
    }
    int i = 0;
    double objFunction = 0.0D;
    for (final T point : points) {
      int j = 0;
      for (final CentroidCluster<T> cluster : clusters) {
        final double dist = distance(point, cluster.getCenter());
        objFunction += (dist * dist) * FastMath.pow(membershipMatrix[i][j], fuzziness);
        j++;
      }
      i++;
    }
    return objFunction;
  }
  public int getK() {
    return k;
  }
  public int getMaxIterations() {
    return maxIterations;
  }
  private void initializeMembershipMatrix() {
    for(int i = 0; i < points.size(); i++) {
      for(int j = 0; j < k; j++) {
        membershipMatrix[i][j] = random.nextDouble();
      }
      membershipMatrix[i] = MathArrays.normalizeArray(membershipMatrix[i], 1.0D);
    }
  }
  private void saveMembershipMatrix(final double[][] matrix) {
    for(int i = 0; i < points.size(); i++) {
      System.arraycopy(membershipMatrix[i], 0, matrix[i], 0, clusters.size());
    }
  }
  private void updateClusterCenters() {
    int j = 0;
    final List<CentroidCluster<T>> newClusters = new ArrayList<CentroidCluster<T>>(k);
    for (final CentroidCluster<T> cluster : clusters) {
      final Clusterable center = cluster.getCenter();
      int i = 0;
      double[] arr = new double[center.getPoint().length];
      double sum = 0.0D;
      for (final T point : points) {
        final double u = FastMath.pow(membershipMatrix[i][j], fuzziness);
        final double[] pointArr = point.getPoint();
        for(int idx = 0; idx < arr.length; idx++) {
          arr[idx] += u * pointArr[idx];
        }
        sum += u;
        i++;
      }
      MathArrays.scaleInPlace(1.0D / sum, arr);
      newClusters.add(new CentroidCluster<T>(new DoublePoint(arr)));
      j++;
    }
    clusters.clear();
    clusters = newClusters;
  }
  private void updateMembershipMatrix() {
    for(int i = 0; i < points.size(); i++) {
      final T point = points.get(i);
      double maxMembership = 0.0D;
      int newCluster = -1;
      for(int j = 0; j < clusters.size(); j++) {
        double sum = 0.0D;
        final double distA = FastMath.abs(distance(point, clusters.get(j).getCenter()));
        for (final CentroidCluster<T> c : clusters) {
          final double distB = FastMath.abs(distance(point, c.getCenter()));
          sum += FastMath.pow(distA / distB, 2.0D / (fuzziness - 1.0D));
        }
        membershipMatrix[i][j] = 1.0D / sum;
        double var_2529 = membershipMatrix[i][j];
        if(var_2529 > maxMembership) {
          maxMembership = membershipMatrix[i][j];
          newCluster = j;
        }
      }
      clusters.get(newCluster).addPoint(point);
    }
  }
}