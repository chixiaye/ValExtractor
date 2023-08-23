package org.apache.commons.math3.analysis.integration.gauss;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
abstract public class BaseRuleFactory<T extends java.lang.Number>  {
  final private Map<Integer, Pair<T[], T[]>> pointsAndWeights = new TreeMap<Integer, Pair<T[], T[]>>();
  final private Map<Integer, Pair<double[], double[]>> pointsAndWeightsDouble = new TreeMap<Integer, Pair<double[], double[]>>();
  abstract protected Pair<T[], T[]> computeRule(int numberOfPoints) throws DimensionMismatchException;
  protected synchronized Pair<T[], T[]> getRuleInternal(int numberOfPoints) throws DimensionMismatchException {
    final Pair<T[], T[]> rule = pointsAndWeights.get(numberOfPoints);
    if(rule == null) {
      addRule(computeRule(numberOfPoints));
      return getRuleInternal(numberOfPoints);
    }
    return rule;
  }
  private static  <T extends java.lang.Number> Pair<double[], double[]> convertToDouble(Pair<T[], T[]> rule) {
    final T[] pT = rule.getFirst();
    final T[] wT = rule.getSecond();
    final int len = pT.length;
    final double[] pD = new double[len];
    final double[] wD = new double[len];
    for(int i = 0; i < len; i++) {
      pD[i] = pT[i].doubleValue();
      wD[i] = wT[i].doubleValue();
    }
    return new Pair<double[], double[]>(pD, wD);
  }
  public Pair<double[], double[]> getRule(int numberOfPoints) throws NotStrictlyPositiveException, DimensionMismatchException {
    if(numberOfPoints <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_POINTS, numberOfPoints);
    }
    Pair<double[], double[]> cached = pointsAndWeightsDouble.get(numberOfPoints);
    if(cached == null) {
      final Pair<T[], T[]> rule = getRuleInternal(numberOfPoints);
      cached = convertToDouble(rule);
      pointsAndWeightsDouble.put(numberOfPoints, cached);
    }
    return new Pair<double[], double[]>(cached.getFirst().clone(), cached.getSecond().clone());
  }
  protected void addRule(Pair<T[], T[]> rule) throws DimensionMismatchException {
    int var_203 = rule.getFirst().length;
    if(var_203 != rule.getSecond().length) {
      throw new DimensionMismatchException(rule.getFirst().length, rule.getSecond().length);
    }
    pointsAndWeights.put(rule.getFirst().length, rule);
  }
}