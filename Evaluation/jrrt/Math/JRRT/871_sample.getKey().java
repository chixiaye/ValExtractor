package org.apache.commons.math3.distribution;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.exception.NotFiniteNumberException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;

public class EnumeratedRealDistribution extends AbstractRealDistribution  {
  final private static long serialVersionUID = 20130308L;
  final protected EnumeratedDistribution<Double> innerDistribution;
  public EnumeratedRealDistribution(final RandomGenerator rng, final double[] singletons, final double[] probabilities) throws DimensionMismatchException, NotPositiveException, MathArithmeticException, NotFiniteNumberException, NotANumberException {
    super(rng);
    if(singletons.length != probabilities.length) {
      throw new DimensionMismatchException(probabilities.length, singletons.length);
    }
    List<Pair<Double, Double>> samples = new ArrayList<Pair<Double, Double>>(singletons.length);
    for(int i = 0; i < singletons.length; i++) {
      samples.add(new Pair<Double, Double>(singletons[i], probabilities[i]));
    }
    innerDistribution = new EnumeratedDistribution<Double>(rng, samples);
  }
  public EnumeratedRealDistribution(final double[] singletons, final double[] probabilities) throws DimensionMismatchException, NotPositiveException, MathArithmeticException, NotFiniteNumberException, NotANumberException {
    this(new Well19937c(), singletons, probabilities);
  }
  public boolean isSupportConnected() {
    return true;
  }
  public boolean isSupportLowerBoundInclusive() {
    return true;
  }
  public boolean isSupportUpperBoundInclusive() {
    return true;
  }
  public double cumulativeProbability(final double x) {
    double probability = 0;
    for (final Pair<Double, Double> sample : innerDistribution.getPmf()) {
      if(sample.getKey() <= x) {
        probability += sample.getValue();
      }
    }
    return probability;
  }
  public double density(final double x) {
    return probability(x);
  }
  public double getNumericalMean() {
    double mean = 0;
    for (final Pair<Double, Double> sample : innerDistribution.getPmf()) {
      mean += sample.getValue() * sample.getKey();
    }
    return mean;
  }
  public double getNumericalVariance() {
    double mean = 0;
    double meanOfSquares = 0;
    for (final Pair<Double, Double> sample : innerDistribution.getPmf()) {
      mean += sample.getValue() * sample.getKey();
      meanOfSquares += sample.getValue() * sample.getKey() * sample.getKey();
    }
    return meanOfSquares - mean * mean;
  }
  public double getSupportLowerBound() {
    double min = Double.POSITIVE_INFINITY;
    for (final Pair<Double, Double> sample : innerDistribution.getPmf()) {
      if(sample.getKey() < min && sample.getValue() > 0) {
        min = sample.getKey();
      }
    }
    return min;
  }
  public double getSupportUpperBound() {
    double max = Double.NEGATIVE_INFINITY;
    for (final Pair<Double, Double> sample : innerDistribution.getPmf()) {
      Double var_871 = sample.getKey();
      if(var_871 > max && sample.getValue() > 0) {
        max = sample.getKey();
      }
    }
    return max;
  }
  @Override() public double probability(final double x) {
    return innerDistribution.probability(x);
  }
  @Override() public double sample() {
    return innerDistribution.sample();
  }
}