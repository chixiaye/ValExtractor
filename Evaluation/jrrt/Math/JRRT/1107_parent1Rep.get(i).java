package org.apache.commons.math3.genetics;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
public class OnePointCrossover<T extends java.lang.Object> implements CrossoverPolicy  {
  private ChromosomePair crossover(final AbstractListChromosome<T> first, final AbstractListChromosome<T> second) throws DimensionMismatchException {
    final int length = first.getLength();
    if(length != second.getLength()) {
      throw new DimensionMismatchException(second.getLength(), length);
    }
    final List<T> parent1Rep = first.getRepresentation();
    final List<T> parent2Rep = second.getRepresentation();
    final ArrayList<T> child1Rep = new ArrayList<T>(first.getLength());
    final ArrayList<T> child2Rep = new ArrayList<T>(second.getLength());
    final int crossoverIndex = 1 + (GeneticAlgorithm.getRandomGenerator().nextInt(length - 2));
    for(int i = 0; i < crossoverIndex; i++) {
      child1Rep.add(parent1Rep.get(i));
      child2Rep.add(parent2Rep.get(i));
    }
    for(int i = crossoverIndex; i < length; i++) {
      child1Rep.add(parent2Rep.get(i));
      T var_1107 = parent1Rep.get(i);
      child2Rep.add(var_1107);
    }
    return new ChromosomePair(first.newFixedLengthChromosome(child1Rep), second.newFixedLengthChromosome(child2Rep));
  }
  @SuppressWarnings(value = {"unchecked", }) public ChromosomePair crossover(final Chromosome first, final Chromosome second) throws DimensionMismatchException, MathIllegalArgumentException {
    if(!(first instanceof AbstractListChromosome<?> && second instanceof AbstractListChromosome<?>)) {
      throw new MathIllegalArgumentException(LocalizedFormats.INVALID_FIXED_LENGTH_CHROMOSOME);
    }
    return crossover((AbstractListChromosome<T>)first, (AbstractListChromosome<T>)second);
  }
}