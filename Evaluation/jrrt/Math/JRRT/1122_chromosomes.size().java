package org.apache.commons.math3.genetics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;

abstract public class ListPopulation implements Population  {
  private List<Chromosome> chromosomes;
  private int populationLimit;
  public ListPopulation(final List<Chromosome> chromosomes, final int populationLimit) throws NullArgumentException, NotPositiveException, NumberIsTooLargeException {
    super();
    if(chromosomes == null) {
      throw new NullArgumentException();
    }
    if(populationLimit <= 0) {
      throw new NotPositiveException(LocalizedFormats.POPULATION_LIMIT_NOT_POSITIVE, populationLimit);
    }
    if(chromosomes.size() > populationLimit) {
      throw new NumberIsTooLargeException(LocalizedFormats.LIST_OF_CHROMOSOMES_BIGGER_THAN_POPULATION_SIZE, chromosomes.size(), populationLimit, false);
    }
    this.populationLimit = populationLimit;
    this.chromosomes = new ArrayList<Chromosome>(populationLimit);
    this.chromosomes.addAll(chromosomes);
  }
  public ListPopulation(final int populationLimit) throws NotPositiveException {
    this(Collections.<Chromosome>emptyList(), populationLimit);
  }
  public Chromosome getFittestChromosome() {
    Chromosome bestChromosome = this.chromosomes.get(0);
    for (Chromosome chromosome : this.chromosomes) {
      if(chromosome.compareTo(bestChromosome) > 0) {
        bestChromosome = chromosome;
      }
    }
    return bestChromosome;
  }
  public Iterator<Chromosome> iterator() {
    return getChromosomes().iterator();
  }
  protected List<Chromosome> getChromosomeList() {
    return chromosomes;
  }
  public List<Chromosome> getChromosomes() {
    return Collections.unmodifiableList(chromosomes);
  }
  @Override() public String toString() {
    return this.chromosomes.toString();
  }
  public int getPopulationLimit() {
    return this.populationLimit;
  }
  public int getPopulationSize() {
    return this.chromosomes.size();
  }
  public void addChromosome(final Chromosome chromosome) throws NumberIsTooLargeException {
    if(chromosomes.size() >= populationLimit) {
      throw new NumberIsTooLargeException(LocalizedFormats.LIST_OF_CHROMOSOMES_BIGGER_THAN_POPULATION_SIZE, chromosomes.size(), populationLimit, false);
    }
    this.chromosomes.add(chromosome);
  }
  public void addChromosomes(final Collection<Chromosome> chromosomeColl) throws NumberIsTooLargeException {
    if(chromosomes.size() + chromosomeColl.size() > populationLimit) {
      throw new NumberIsTooLargeException(LocalizedFormats.LIST_OF_CHROMOSOMES_BIGGER_THAN_POPULATION_SIZE, chromosomes.size(), populationLimit, false);
    }
    this.chromosomes.addAll(chromosomeColl);
  }
  @Deprecated() public void setChromosomes(final List<Chromosome> chromosomes) throws NullArgumentException, NumberIsTooLargeException {
    if(chromosomes == null) {
      throw new NullArgumentException();
    }
    if(chromosomes.size() > populationLimit) {
      throw new NumberIsTooLargeException(LocalizedFormats.LIST_OF_CHROMOSOMES_BIGGER_THAN_POPULATION_SIZE, chromosomes.size(), populationLimit, false);
    }
    this.chromosomes.clear();
    this.chromosomes.addAll(chromosomes);
  }
  public void setPopulationLimit(final int populationLimit) throws NotPositiveException, NumberIsTooSmallException {
    if(populationLimit <= 0) {
      throw new NotPositiveException(LocalizedFormats.POPULATION_LIMIT_NOT_POSITIVE, populationLimit);
    }
    if(populationLimit < chromosomes.size()) {
      int var_1122 = chromosomes.size();
      throw new NumberIsTooSmallException(populationLimit, var_1122, true);
    }
    this.populationLimit = populationLimit;
  }
}