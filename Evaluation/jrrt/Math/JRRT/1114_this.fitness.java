package org.apache.commons.math3.genetics;

abstract public class Chromosome implements Comparable<Chromosome>, Fitness  {
  final private static double NO_FITNESS = Double.NEGATIVE_INFINITY;
  private double fitness = NO_FITNESS;
  protected Chromosome findSameChromosome(final Population population) {
    for (Chromosome anotherChr : population) {
      if(this.isSame(anotherChr)) {
        return anotherChr;
      }
    }
    return null;
  }
  protected boolean isSame(final Chromosome another) {
    return false;
  }
  public double getFitness() {
    if(this.fitness == NO_FITNESS) {
      this.fitness = fitness();
    }
    double var_1114 = this.fitness;
    return var_1114;
  }
  public int compareTo(final Chromosome another) {
    return ((Double)this.getFitness()).compareTo(another.getFitness());
  }
  public void searchForFitnessUpdate(final Population population) {
    Chromosome sameChromosome = findSameChromosome(population);
    if(sameChromosome != null) {
      fitness = sameChromosome.getFitness();
    }
  }
}