package org.apache.commons.math3.distribution.fitting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.MixtureMultivariateNormalDistribution;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.Pair;

public class MultivariateNormalMixtureExpectationMaximization  {
  final private static int DEFAULT_MAX_ITERATIONS = 1000;
  final private static double DEFAULT_THRESHOLD = 1E-5D;
  final private double[][] data;
  private MixtureMultivariateNormalDistribution fittedModel;
  private double logLikelihood = 0D;
  public MultivariateNormalMixtureExpectationMaximization(double[][] data) throws NotStrictlyPositiveException, DimensionMismatchException, NumberIsTooSmallException {
    super();
    if(data.length < 1) {
      throw new NotStrictlyPositiveException(data.length);
    }
    this.data = new double[data.length][data[0].length];
    for(int i = 0; i < data.length; i++) {
      if(data[i].length != data[0].length) {
        throw new DimensionMismatchException(data[i].length, data[0].length);
      }
      if(data[i].length < 2) {
        throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_TOO_SMALL, data[i].length, 2, true);
      }
      this.data[i] = MathArrays.copyOf(data[i], data[i].length);
    }
  }
  public static MixtureMultivariateNormalDistribution estimate(final double[][] data, final int numComponents) throws NotStrictlyPositiveException, DimensionMismatchException {
    if(data.length < 2) {
      throw new NotStrictlyPositiveException(data.length);
    }
    if(numComponents < 2) {
      throw new NumberIsTooSmallException(numComponents, 2, true);
    }
    if(numComponents > data.length) {
      throw new NumberIsTooLargeException(numComponents, data.length, true);
    }
    final int numRows = data.length;
    final int numCols = data[0].length;
    final DataRow[] sortedData = new DataRow[numRows];
    for(int i = 0; i < numRows; i++) {
      sortedData[i] = new DataRow(data[i]);
    }
    Arrays.sort(sortedData);
    final double weight = 1D / numComponents;
    final List<Pair<Double, MultivariateNormalDistribution>> components = new ArrayList<Pair<Double, MultivariateNormalDistribution>>(numComponents);
    for(int binIndex = 0; binIndex < numComponents; binIndex++) {
      final int minIndex = (binIndex * numRows) / numComponents;
      final int maxIndex = ((binIndex + 1) * numRows) / numComponents;
      final int numBinRows = maxIndex - minIndex;
      final double[][] binData = new double[numBinRows][numCols];
      final double[] columnMeans = new double[numCols];
      for(int i = minIndex, iBin = 0; i < maxIndex; i++, iBin++) {
        for(int j = 0; j < numCols; j++) {
          final double val = sortedData[i].getRow()[j];
          columnMeans[j] += val;
          binData[iBin][j] = val;
        }
      }
      MathArrays.scaleInPlace(1D / numBinRows, columnMeans);
      final double[][] covMat = new Covariance(binData).getCovarianceMatrix().getData();
      final MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(columnMeans, covMat);
      components.add(new Pair<Double, MultivariateNormalDistribution>(weight, mvn));
    }
    return new MixtureMultivariateNormalDistribution(components);
  }
  public MixtureMultivariateNormalDistribution getFittedModel() {
    return new MixtureMultivariateNormalDistribution(fittedModel.getComponents());
  }
  public double getLogLikelihood() {
    return logLikelihood;
  }
  public void fit(MixtureMultivariateNormalDistribution initialMixture) throws SingularMatrixException, NotStrictlyPositiveException {
    fit(initialMixture, DEFAULT_MAX_ITERATIONS, DEFAULT_THRESHOLD);
  }
  public void fit(final MixtureMultivariateNormalDistribution initialMixture, final int maxIterations, final double threshold) throws SingularMatrixException, NotStrictlyPositiveException, DimensionMismatchException {
    if(maxIterations < 1) {
      throw new NotStrictlyPositiveException(maxIterations);
    }
    if(threshold < Double.MIN_VALUE) {
      throw new NotStrictlyPositiveException(threshold);
    }
    final int n = data.length;
    final int numCols = data[0].length;
    final int k = initialMixture.getComponents().size();
    final int numMeanColumns = initialMixture.getComponents().get(0).getSecond().getMeans().length;
    if(numMeanColumns != numCols) {
      throw new DimensionMismatchException(numMeanColumns, numCols);
    }
    int numIterations = 0;
    double previousLogLikelihood = 0D;
    logLikelihood = Double.NEGATIVE_INFINITY;
    fittedModel = new MixtureMultivariateNormalDistribution(initialMixture.getComponents());
    while(numIterations++ <= maxIterations && Math.abs(previousLogLikelihood - logLikelihood) > threshold){
      previousLogLikelihood = logLikelihood;
      double sumLogLikelihood = 0D;
      final List<Pair<Double, MultivariateNormalDistribution>> components = fittedModel.getComponents();
      final double[] weights = new double[k];
      final MultivariateNormalDistribution[] mvns = new MultivariateNormalDistribution[k];
      for(int j = 0; j < k; j++) {
        Pair<Double, MultivariateNormalDistribution> var_919 = components.get(j);
        weights[j] = var_919.getFirst();
        mvns[j] = components.get(j).getSecond();
      }
      final double[][] gamma = new double[n][k];
      final double[] gammaSums = new double[k];
      final double[][] gammaDataProdSums = new double[k][numCols];
      for(int i = 0; i < n; i++) {
        final double rowDensity = fittedModel.density(data[i]);
        sumLogLikelihood += Math.log(rowDensity);
        for(int j = 0; j < k; j++) {
          gamma[i][j] = weights[j] * mvns[j].density(data[i]) / rowDensity;
          gammaSums[j] += gamma[i][j];
          for(int col = 0; col < numCols; col++) {
            gammaDataProdSums[j][col] += gamma[i][j] * data[i][col];
          }
        }
      }
      logLikelihood = sumLogLikelihood / n;
      final double[] newWeights = new double[k];
      final double[][] newMeans = new double[k][numCols];
      for(int j = 0; j < k; j++) {
        newWeights[j] = gammaSums[j] / n;
        for(int col = 0; col < numCols; col++) {
          newMeans[j][col] = gammaDataProdSums[j][col] / gammaSums[j];
        }
      }
      final RealMatrix[] newCovMats = new RealMatrix[k];
      for(int j = 0; j < k; j++) {
        newCovMats[j] = new Array2DRowRealMatrix(numCols, numCols);
      }
      for(int i = 0; i < n; i++) {
        for(int j = 0; j < k; j++) {
          final RealMatrix vec = new Array2DRowRealMatrix(MathArrays.ebeSubtract(data[i], newMeans[j]));
          final RealMatrix dataCov = vec.multiply(vec.transpose()).scalarMultiply(gamma[i][j]);
          newCovMats[j] = newCovMats[j].add(dataCov);
        }
      }
      final double[][][] newCovMatArrays = new double[k][numCols][numCols];
      for(int j = 0; j < k; j++) {
        newCovMats[j] = newCovMats[j].scalarMultiply(1D / gammaSums[j]);
        newCovMatArrays[j] = newCovMats[j].getData();
      }
      fittedModel = new MixtureMultivariateNormalDistribution(newWeights, newMeans, newCovMatArrays);
    }
    if(Math.abs(previousLogLikelihood - logLikelihood) > threshold) {
      throw new ConvergenceException();
    }
  }
  
  private static class DataRow implements Comparable<DataRow>  {
    final private double[] row;
    private Double mean;
    DataRow(final double[] data) {
      super();
      row = data;
      mean = 0D;
      for(int i = 0; i < data.length; i++) {
        mean += data[i];
      }
      mean /= data.length;
    }
    @Override() public boolean equals(Object other) {
      if(this == other) {
        return true;
      }
      if(other instanceof DataRow) {
        return MathArrays.equals(row, ((DataRow)other).row);
      }
      return false;
    }
    public double[] getRow() {
      return row;
    }
    public int compareTo(final DataRow other) {
      return mean.compareTo(other.mean);
    }
    @Override() public int hashCode() {
      return Arrays.hashCode(row);
    }
  }
}