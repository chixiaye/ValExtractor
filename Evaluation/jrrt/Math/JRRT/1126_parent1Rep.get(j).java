package org.apache.commons.math3.genetics;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
public class NPointCrossover<T extends java.lang.Object> implements CrossoverPolicy  {
  final private int crossoverPoints;
  public NPointCrossover(final int crossoverPoints) throws NotStrictlyPositiveException {
    super();
    if(crossoverPoints <= 0) {
      throw new NotStrictlyPositiveException(crossoverPoints);
    }
    this.crossoverPoints = crossoverPoints;
  }
  @SuppressWarnings(value = {"unchecked", }) public ChromosomePair crossover(final Chromosome first, final Chromosome second) throws DimensionMismatchException, MathIllegalArgumentException {
    if(!(first instanceof AbstractListChromosome<?> && second instanceof AbstractListChromosome<?>)) {
      throw new MathIllegalArgumentException(LocalizedFormats.INVALID_FIXED_LENGTH_CHROMOSOME);
    }
    return mate((AbstractListChromosome<T>)first, (AbstractListChromosome<T>)second);
  }
  private ChromosomePair mate(final AbstractListChromosome<T> first, final AbstractListChromosome<T> second) throws DimensionMismatchException, NumberIsTooLargeException {
    final int length = first.getLength();
    if(length != second.getLength()) {
      throw new DimensionMismatchException(second.getLength(), length);
    }
    if(crossoverPoints >= length) {
      throw new NumberIsTooLargeException(crossoverPoints, length, false);
    }
    final List<T> parent1Rep = first.getRepresentation();
    final List<T> parent2Rep = second.getRepresentation();
    final ArrayList<T> child1Rep = new ArrayList<T>(first.getLength());
    final ArrayList<T> child2Rep = new ArrayList<T>(second.getLength());
    final RandomGenerator random = GeneticAlgorithm.getRandomGenerator();
    ArrayList<T> c1 = child1Rep;
    ArrayList<T> c2 = child2Rep;
    int remainingPoints = crossoverPoints;
    int lastIndex = 0;
    for(int i = 0; i < crossoverPoints; i++, remainingPoints--) {
      final int crossoverIndex = 1 + lastIndex + random.nextInt(length - lastIndex - remainingPoints);
      for(int j = lastIndex; j < crossoverIndex; j++) {
        c1.add(parent1Rep.get(j));
        c2.add(parent2Rep.get(j));
      }
      ArrayList<T> tmp = c1;
      c1 = c2;
      c2 = tmp;
      lastIndex = crossoverIndex;
    }
    for(int j = lastIndex; j < length; j++) {
      T var_1126 = parent1Rep.get(j);
      c1.add(var_1126);
      c2.add(parent2Rep.get(j));
    }
    return new ChromosomePair(first.newFixedLengthChromosome(child1Rep), second.newFixedLengthChromosome(child2Rep));
  }
  public int getCrossoverPoints() {
    return crossoverPoints;
  }
}