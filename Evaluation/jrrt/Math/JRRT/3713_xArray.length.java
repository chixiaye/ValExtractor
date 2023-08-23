package org.apache.commons.math3.stat.correlation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.RankingAlgorithm;

public class SpearmansCorrelation  {
  final private RealMatrix data;
  final private RankingAlgorithm rankingAlgorithm;
  final private PearsonsCorrelation rankCorrelation;
  public SpearmansCorrelation() {
    this(new NaturalRanking());
  }
  public SpearmansCorrelation(final RankingAlgorithm rankingAlgorithm) {
    super();
    data = null;
    this.rankingAlgorithm = rankingAlgorithm;
    rankCorrelation = null;
  }
  public SpearmansCorrelation(final RealMatrix dataMatrix) {
    this(dataMatrix, new NaturalRanking());
  }
  public SpearmansCorrelation(final RealMatrix dataMatrix, final RankingAlgorithm rankingAlgorithm) {
    super();
    this.rankingAlgorithm = rankingAlgorithm;
    this.data = rankTransform(dataMatrix);
    rankCorrelation = new PearsonsCorrelation(data);
  }
  private List<Integer> getNaNPositions(final double[] input) {
    final List<Integer> positions = new ArrayList<Integer>();
    for(int i = 0; i < input.length; i++) {
      if(Double.isNaN(input[i])) {
        positions.add(i);
      }
    }
    return positions;
  }
  public PearsonsCorrelation getRankCorrelation() {
    return rankCorrelation;
  }
  public RealMatrix computeCorrelationMatrix(final double[][] matrix) {
    return computeCorrelationMatrix(new BlockRealMatrix(matrix));
  }
  public RealMatrix computeCorrelationMatrix(final RealMatrix matrix) {
    final RealMatrix matrixCopy = rankTransform(matrix);
    return new PearsonsCorrelation().computeCorrelationMatrix(matrixCopy);
  }
  public RealMatrix getCorrelationMatrix() {
    return rankCorrelation.getCorrelationMatrix();
  }
  private RealMatrix rankTransform(final RealMatrix matrix) {
    RealMatrix transformed = null;
    if(rankingAlgorithm instanceof NaturalRanking && ((NaturalRanking)rankingAlgorithm).getNanStrategy() == NaNStrategy.REMOVED) {
      final Set<Integer> nanPositions = new HashSet<Integer>();
      for(int i = 0; i < matrix.getColumnDimension(); i++) {
        nanPositions.addAll(getNaNPositions(matrix.getColumn(i)));
      }
      if(!nanPositions.isEmpty()) {
        transformed = new BlockRealMatrix(matrix.getRowDimension() - nanPositions.size(), matrix.getColumnDimension());
        for(int i = 0; i < transformed.getColumnDimension(); i++) {
          transformed.setColumn(i, removeValues(matrix.getColumn(i), nanPositions));
        }
      }
    }
    if(transformed == null) {
      transformed = matrix.copy();
    }
    for(int i = 0; i < transformed.getColumnDimension(); i++) {
      transformed.setColumn(i, rankingAlgorithm.rank(transformed.getColumn(i)));
    }
    return transformed;
  }
  public double correlation(final double[] xArray, final double[] yArray) {
    if(xArray.length != yArray.length) {
      int var_3713 = xArray.length;
      throw new DimensionMismatchException(var_3713, yArray.length);
    }
    else 
      if(xArray.length < 2) {
        throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, xArray.length, 2);
      }
      else {
        double[] x = xArray;
        double[] y = yArray;
        if(rankingAlgorithm instanceof NaturalRanking && NaNStrategy.REMOVED == ((NaturalRanking)rankingAlgorithm).getNanStrategy()) {
          final Set<Integer> nanPositions = new HashSet<Integer>();
          nanPositions.addAll(getNaNPositions(xArray));
          nanPositions.addAll(getNaNPositions(yArray));
          x = removeValues(xArray, nanPositions);
          y = removeValues(yArray, nanPositions);
        }
        return new PearsonsCorrelation().correlation(rankingAlgorithm.rank(x), rankingAlgorithm.rank(y));
      }
  }
  private double[] removeValues(final double[] input, final Set<Integer> indices) {
    if(indices.isEmpty()) {
      return input;
    }
    final double[] result = new double[input.length - indices.size()];
    for(int i = 0, j = 0; i < input.length; i++) {
      if(!indices.contains(i)) {
        result[j++] = input[i];
      }
    }
    return result;
  }
}