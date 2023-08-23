package org.apache.commons.math3.util;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class ResizableDoubleArray implements DoubleArray, Serializable  {
  @Deprecated() final public static int ADDITIVE_MODE = 1;
  @Deprecated() final public static int MULTIPLICATIVE_MODE = 0;
  final private static long serialVersionUID = -3485529955529426875L;
  final private static int DEFAULT_INITIAL_CAPACITY = 16;
  final private static double DEFAULT_EXPANSION_FACTOR = 2.0D;
  final private static double DEFAULT_CONTRACTION_DELTA = 0.5D;
  private double contractionCriterion = 2.5D;
  private double expansionFactor = 2.0D;
  private ExpansionMode expansionMode = ExpansionMode.MULTIPLICATIVE;
  private double[] internalArray;
  private int numElements = 0;
  private int startIndex = 0;
  public ResizableDoubleArray() {
    this(DEFAULT_INITIAL_CAPACITY);
  }
  public ResizableDoubleArray(ResizableDoubleArray original) throws NullArgumentException {
    super();
    MathUtils.checkNotNull(original);
    copy(original, this);
  }
  public ResizableDoubleArray(double[] initialArray) {
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_EXPANSION_FACTOR, DEFAULT_CONTRACTION_DELTA + DEFAULT_EXPANSION_FACTOR, ExpansionMode.MULTIPLICATIVE, initialArray);
  }
  public ResizableDoubleArray(int initialCapacity) throws MathIllegalArgumentException {
    this(initialCapacity, DEFAULT_EXPANSION_FACTOR);
  }
  public ResizableDoubleArray(int initialCapacity, double expansionFactor) throws MathIllegalArgumentException {
    this(initialCapacity, expansionFactor, DEFAULT_CONTRACTION_DELTA + expansionFactor);
  }
  public ResizableDoubleArray(int initialCapacity, double expansionFactor, double contractionCriterion) throws MathIllegalArgumentException {
    this(initialCapacity, expansionFactor, contractionCriterion, ExpansionMode.MULTIPLICATIVE, null);
  }
  public ResizableDoubleArray(int initialCapacity, double expansionFactor, double contractionCriterion, ExpansionMode expansionMode, double ... data) throws MathIllegalArgumentException {
    super();
    if(initialCapacity <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.INITIAL_CAPACITY_NOT_POSITIVE, initialCapacity);
    }
    checkContractExpand(contractionCriterion, expansionFactor);
    this.expansionFactor = expansionFactor;
    this.contractionCriterion = contractionCriterion;
    this.expansionMode = expansionMode;
    internalArray = new double[initialCapacity];
    numElements = 0;
    startIndex = 0;
    if(data != null) {
      addElements(data);
    }
  }
  @Deprecated() public ResizableDoubleArray(int initialCapacity, float expansionFactor) throws MathIllegalArgumentException {
    this(initialCapacity, (double)expansionFactor);
  }
  @Deprecated() public ResizableDoubleArray(int initialCapacity, float expansionFactor, float contractionCriteria) throws MathIllegalArgumentException {
    this(initialCapacity, (double)expansionFactor, (double)contractionCriteria);
  }
  @Deprecated() public ResizableDoubleArray(int initialCapacity, float expansionFactor, float contractionCriteria, int expansionMode) throws MathIllegalArgumentException {
    this(initialCapacity, expansionFactor, contractionCriteria, expansionMode == ADDITIVE_MODE ? ExpansionMode.ADDITIVE : ExpansionMode.MULTIPLICATIVE, null);
    setExpansionMode(expansionMode);
  }
  public synchronized ResizableDoubleArray copy() {
    final ResizableDoubleArray result = new ResizableDoubleArray();
    copy(this, result);
    return result;
  }
  @Override() public boolean equals(Object object) {
    if(object == this) {
      return true;
    }
    if(object instanceof ResizableDoubleArray == false) {
      return false;
    }
    synchronized(this) {
      synchronized(object) {
        boolean result = true;
        final ResizableDoubleArray other = (ResizableDoubleArray)object;
        result = result && (other.contractionCriterion == contractionCriterion);
        result = result && (other.expansionFactor == expansionFactor);
        result = result && (other.expansionMode == expansionMode);
        result = result && (other.numElements == numElements);
        result = result && (other.startIndex == startIndex);
        if(!result) {
          return false;
        }
        else {
          return Arrays.equals(internalArray, other.internalArray);
        }
      }
    }
  }
  private synchronized boolean shouldContract() {
    if(expansionMode == ExpansionMode.MULTIPLICATIVE) {
      return (internalArray.length / ((float)numElements)) > contractionCriterion;
    }
    else {
      return (internalArray.length - numElements) > contractionCriterion;
    }
  }
  public synchronized double addElementRolling(double value) {
    double discarded = internalArray[startIndex];
    if((startIndex + (numElements + 1)) > internalArray.length) {
      expand();
    }
    startIndex += 1;
    internalArray[startIndex + (numElements - 1)] = value;
    if(shouldContract()) {
      contract();
    }
    return discarded;
  }
  public double compute(MathArrays.Function f) {
    final double[] array;
    final int start;
    final int num;
    synchronized(this) {
      array = internalArray;
      start = startIndex;
      num = numElements;
    }
    return f.evaluate(array, start, num);
  }
  public double getContractionCriterion() {
    return contractionCriterion;
  }
  public synchronized double getElement(int index) {
    if(index >= numElements) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    else 
      if(index >= 0) {
        return internalArray[startIndex + index];
      }
      else {
        throw new ArrayIndexOutOfBoundsException(index);
      }
  }
  public synchronized double substituteMostRecentElement(double value) throws MathIllegalStateException {
    if(numElements < 1) {
      throw new MathIllegalStateException(LocalizedFormats.CANNOT_SUBSTITUTE_ELEMENT_FROM_EMPTY_ARRAY);
    }
    final int substIndex = startIndex + (numElements - 1);
    final double discarded = internalArray[substIndex];
    internalArray[substIndex] = value;
    return discarded;
  }
  protected double[] getArrayRef() {
    return internalArray;
  }
  public synchronized double[] getElements() {
    final double[] elementArray = new double[numElements];
    System.arraycopy(internalArray, startIndex, elementArray, 0, numElements);
    return elementArray;
  }
  @Deprecated() public synchronized double[] getInternalValues() {
    return internalArray;
  }
  @Deprecated() public float getContractionCriteria() {
    return (float)getContractionCriterion();
  }
  @Deprecated() public float getExpansionFactor() {
    return (float)expansionFactor;
  }
  public int getCapacity() {
    return internalArray.length;
  }
  @Deprecated() public int getExpansionMode() {
    switch (expansionMode){
      case MULTIPLICATIVE:
      return MULTIPLICATIVE_MODE;
      case ADDITIVE:
      return ADDITIVE_MODE;
      default:
      throw new MathInternalError();
    }
  }
  @Deprecated() synchronized int getInternalLength() {
    return internalArray.length;
  }
  public synchronized int getNumElements() {
    return numElements;
  }
  protected int getStartIndex() {
    return startIndex;
  }
  @Override() public synchronized int hashCode() {
    final int[] hashData = new int[6];
    hashData[0] = Double.valueOf(expansionFactor).hashCode();
    hashData[1] = Double.valueOf(contractionCriterion).hashCode();
    hashData[2] = expansionMode.hashCode();
    hashData[3] = Arrays.hashCode(internalArray);
    hashData[4] = numElements;
    hashData[5] = startIndex;
    return Arrays.hashCode(hashData);
  }
  @Deprecated() public synchronized int start() {
    return startIndex;
  }
  public synchronized void addElement(double value) {
    if(internalArray.length <= startIndex + numElements) {
      expand();
    }
    internalArray[startIndex + numElements++] = value;
  }
  public synchronized void addElements(double[] values) {
    final double[] tempArray = new double[numElements + values.length + 1];
    System.arraycopy(internalArray, startIndex, tempArray, 0, numElements);
    System.arraycopy(values, 0, tempArray, numElements, values.length);
    internalArray = tempArray;
    startIndex = 0;
    numElements += values.length;
  }
  protected void checkContractExpand(double contraction, double expansion) throws NumberIsTooSmallException {
    if(contraction < expansion) {
      final NumberIsTooSmallException e = new NumberIsTooSmallException(contraction, 1, true);
      e.getContext().addMessage(LocalizedFormats.CONTRACTION_CRITERIA_SMALLER_THAN_EXPANSION_FACTOR, contraction, expansion);
      throw e;
    }
    if(contraction <= 1) {
      final NumberIsTooSmallException e = new NumberIsTooSmallException(contraction, 1, false);
      e.getContext().addMessage(LocalizedFormats.CONTRACTION_CRITERIA_SMALLER_THAN_ONE, contraction);
      throw e;
    }
    if(expansion <= 1) {
      final NumberIsTooSmallException e = new NumberIsTooSmallException(contraction, 1, false);
      e.getContext().addMessage(LocalizedFormats.EXPANSION_FACTOR_SMALLER_THAN_ONE, expansion);
      throw e;
    }
  }
  @Deprecated() protected void checkContractExpand(float contraction, float expansion) throws MathIllegalArgumentException {
    checkContractExpand((double)contraction, (double)expansion);
  }
  public synchronized void clear() {
    numElements = 0;
    startIndex = 0;
  }
  public synchronized void contract() {
    final double[] tempArray = new double[numElements + 1];
    System.arraycopy(internalArray, startIndex, tempArray, 0, numElements);
    internalArray = tempArray;
    startIndex = 0;
  }
  public static void copy(ResizableDoubleArray source, ResizableDoubleArray dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    synchronized(source) {
      synchronized(dest) {
        dest.contractionCriterion = source.contractionCriterion;
        dest.expansionFactor = source.expansionFactor;
        dest.expansionMode = source.expansionMode;
        dest.internalArray = new double[source.internalArray.length];
        double[] var_4109 = dest.internalArray;
        System.arraycopy(source.internalArray, 0, dest.internalArray, 0, var_4109.length);
        dest.numElements = source.numElements;
        dest.startIndex = source.startIndex;
      }
    }
  }
  private synchronized void discardExtremeElements(int i, boolean front) throws MathIllegalArgumentException {
    if(i > numElements) {
      throw new MathIllegalArgumentException(LocalizedFormats.TOO_MANY_ELEMENTS_TO_DISCARD_FROM_ARRAY, i, numElements);
    }
    else 
      if(i < 0) {
        throw new MathIllegalArgumentException(LocalizedFormats.CANNOT_DISCARD_NEGATIVE_NUMBER_OF_ELEMENTS, i);
      }
      else {
        numElements -= i;
        if(front) {
          startIndex += i;
        }
      }
    if(shouldContract()) {
      contract();
    }
  }
  public synchronized void discardFrontElements(int i) throws MathIllegalArgumentException {
    discardExtremeElements(i, true);
  }
  public synchronized void discardMostRecentElements(int i) throws MathIllegalArgumentException {
    discardExtremeElements(i, false);
  }
  protected synchronized void expand() {
    int newSize = 0;
    if(expansionMode == ExpansionMode.MULTIPLICATIVE) {
      newSize = (int)FastMath.ceil(internalArray.length * expansionFactor);
    }
    else {
      newSize = (int)(internalArray.length + FastMath.round(expansionFactor));
    }
    final double[] tempArray = new double[newSize];
    System.arraycopy(internalArray, 0, tempArray, 0, internalArray.length);
    internalArray = tempArray;
  }
  private synchronized void expandTo(int size) {
    final double[] tempArray = new double[size];
    System.arraycopy(internalArray, 0, tempArray, 0, internalArray.length);
    internalArray = tempArray;
  }
  @Deprecated() public void setContractionCriteria(float contractionCriteria) throws MathIllegalArgumentException {
    checkContractExpand(contractionCriteria, getExpansionFactor());
    synchronized(this) {
      this.contractionCriterion = contractionCriteria;
    }
  }
  public synchronized void setElement(int index, double value) {
    if(index < 0) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    if(index + 1 > numElements) {
      numElements = index + 1;
    }
    if((startIndex + index) >= internalArray.length) {
      expandTo(startIndex + (index + 1));
    }
    internalArray[startIndex + index] = value;
  }
  @Deprecated() public void setExpansionFactor(float expansionFactor) throws MathIllegalArgumentException {
    checkContractExpand(getContractionCriterion(), expansionFactor);
    synchronized(this) {
      this.expansionFactor = expansionFactor;
    }
  }
  @Deprecated() public void setExpansionMode(int expansionMode) throws MathIllegalArgumentException {
    if(expansionMode != MULTIPLICATIVE_MODE && expansionMode != ADDITIVE_MODE) {
      throw new MathIllegalArgumentException(LocalizedFormats.UNSUPPORTED_EXPANSION_MODE, expansionMode, MULTIPLICATIVE_MODE, "MULTIPLICATIVE_MODE", ADDITIVE_MODE, "ADDITIVE_MODE");
    }
    synchronized(this) {
      if(expansionMode == MULTIPLICATIVE_MODE) {
        setExpansionMode(ExpansionMode.MULTIPLICATIVE);
      }
      else 
        if(expansionMode == ADDITIVE_MODE) {
          setExpansionMode(ExpansionMode.ADDITIVE);
        }
    }
  }
  @Deprecated() public void setExpansionMode(ExpansionMode expansionMode) {
    this.expansionMode = expansionMode;
  }
  @Deprecated() protected void setInitialCapacity(int initialCapacity) throws MathIllegalArgumentException {
  }
  public synchronized void setNumElements(int i) throws MathIllegalArgumentException {
    if(i < 0) {
      throw new MathIllegalArgumentException(LocalizedFormats.INDEX_NOT_POSITIVE, i);
    }
    final int newSize = startIndex + i;
    if(newSize > internalArray.length) {
      expandTo(newSize);
    }
    numElements = i;
  }
  public static enum ExpansionMode {
    MULTIPLICATIVE(),

    ADDITIVE(),

  ;
  private ExpansionMode() {
  }
  }
}