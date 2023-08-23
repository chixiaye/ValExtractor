package org.apache.commons.math3.genetics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
abstract public class RandomKey<T extends java.lang.Object> extends AbstractListChromosome<Double> implements PermutationChromosome<T>  {
  final private List<Double> sortedRepresentation;
  final private List<Integer> baseSeqPermutation;
  public RandomKey(final Double[] representation) throws InvalidRepresentationException {
    this(Arrays.asList(representation));
  }
  public RandomKey(final List<Double> representation) throws InvalidRepresentationException {
    super(representation);
    List<Double> sortedRepr = new ArrayList<Double>(getRepresentation());
    Collections.sort(sortedRepr);
    sortedRepresentation = Collections.unmodifiableList(sortedRepr);
    baseSeqPermutation = Collections.unmodifiableList(decodeGeneric(baseSequence(getLength()), getRepresentation(), sortedRepresentation));
  }
  public static  <S extends java.lang.Object> List<Double> comparatorPermutation(final List<S> data, final Comparator<S> comparator) {
    List<S> sortedData = new ArrayList<S>(data);
    Collections.sort(sortedData, comparator);
    return inducedPermutation(data, sortedData);
  }
  final public static List<Double> identityPermutation(final int l) {
    List<Double> repr = new ArrayList<Double>(l);
    for(int i = 0; i < l; i++) {
      repr.add((double)i / l);
    }
    return repr;
  }
  public static  <S extends java.lang.Object> List<Double> inducedPermutation(final List<S> originalData, final List<S> permutedData) throws DimensionMismatchException, MathIllegalArgumentException {
    if(originalData.size() != permutedData.size()) {
      throw new DimensionMismatchException(permutedData.size(), originalData.size());
    }
    int l = originalData.size();
    List<S> origDataCopy = new ArrayList<S>(originalData);
    Double[] res = new Double[l];
    for(int i = 0; i < l; i++) {
      int index = origDataCopy.indexOf(permutedData.get(i));
      if(index == -1) {
        throw new MathIllegalArgumentException(LocalizedFormats.DIFFERENT_ORIG_AND_PERMUTED_DATA);
      }
      res[index] = (double)i / l;
      origDataCopy.set(index, null);
    }
    return Arrays.asList(res);
  }
  final public static List<Double> randomPermutation(final int l) {
    List<Double> repr = new ArrayList<Double>(l);
    for(int i = 0; i < l; i++) {
      repr.add(GeneticAlgorithm.getRandomGenerator().nextDouble());
    }
    return repr;
  }
  private static List<Integer> baseSequence(final int l) {
    List<Integer> baseSequence = new ArrayList<Integer>(l);
    for(int i = 0; i < l; i++) {
      baseSequence.add(i);
    }
    return baseSequence;
  }
  private static  <S extends java.lang.Object> List<S> decodeGeneric(final List<S> sequence, List<Double> representation, final List<Double> sortedRepr) throws DimensionMismatchException {
    int l = sequence.size();
    if(representation.size() != l) {
      throw new DimensionMismatchException(representation.size(), l);
    }
    if(sortedRepr.size() != l) {
      int var_1092 = sortedRepr.size();
      throw new DimensionMismatchException(var_1092, l);
    }
    List<Double> reprCopy = new ArrayList<Double>(representation);
    List<S> res = new ArrayList<S>(l);
    for(int i = 0; i < l; i++) {
      int index = reprCopy.indexOf(sortedRepr.get(i));
      res.add(sequence.get(index));
      reprCopy.set(index, null);
    }
    return res;
  }
  public List<T> decode(final List<T> sequence) {
    return decodeGeneric(sequence, getRepresentation(), sortedRepresentation);
  }
  @Override() public String toString() {
    return String.format("(f=%s pi=(%s))", getFitness(), baseSeqPermutation);
  }
  @Override() protected boolean isSame(final Chromosome another) {
    if(!(another instanceof RandomKey<?>)) {
      return false;
    }
    RandomKey<?> anotherRk = (RandomKey<?>)another;
    if(getLength() != anotherRk.getLength()) {
      return false;
    }
    List<Integer> thisPerm = this.baseSeqPermutation;
    List<Integer> anotherPerm = anotherRk.baseSeqPermutation;
    for(int i = 0; i < getLength(); i++) {
      if(thisPerm.get(i) != anotherPerm.get(i)) {
        return false;
      }
    }
    return true;
  }
  @Override() protected void checkValidity(final List<Double> chromosomeRepresentation) throws InvalidRepresentationException {
    for (double val : chromosomeRepresentation) {
      if(val < 0 || val > 1) {
        throw new InvalidRepresentationException(LocalizedFormats.OUT_OF_RANGE_SIMPLE, val, 0, 1);
      }
    }
  }
}