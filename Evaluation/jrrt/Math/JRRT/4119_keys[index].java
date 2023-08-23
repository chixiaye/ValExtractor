package org.apache.commons.math3.util;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public class OpenIntToDoubleHashMap implements Serializable  {
  final protected static byte FREE = 0;
  final protected static byte FULL = 1;
  final protected static byte REMOVED = 2;
  final private static long serialVersionUID = -3646337053166149105L;
  final private static float LOAD_FACTOR = 0.5F;
  final private static int DEFAULT_EXPECTED_SIZE = 16;
  final private static int RESIZE_MULTIPLIER = 2;
  final private static int PERTURB_SHIFT = 5;
  private int[] keys;
  private double[] values;
  private byte[] states;
  final private double missingEntries;
  private int size;
  private int mask;
  private transient int count;
  public OpenIntToDoubleHashMap() {
    this(DEFAULT_EXPECTED_SIZE, Double.NaN);
  }
  public OpenIntToDoubleHashMap(final OpenIntToDoubleHashMap source) {
    super();
    final int length = source.keys.length;
    keys = new int[length];
    System.arraycopy(source.keys, 0, keys, 0, length);
    values = new double[length];
    System.arraycopy(source.values, 0, values, 0, length);
    states = new byte[length];
    System.arraycopy(source.states, 0, states, 0, length);
    missingEntries = source.missingEntries;
    size = source.size;
    mask = source.mask;
    count = source.count;
  }
  public OpenIntToDoubleHashMap(final double missingEntries) {
    this(DEFAULT_EXPECTED_SIZE, missingEntries);
  }
  public OpenIntToDoubleHashMap(final int expectedSize) {
    this(expectedSize, Double.NaN);
  }
  public OpenIntToDoubleHashMap(final int expectedSize, final double missingEntries) {
    super();
    final int capacity = computeCapacity(expectedSize);
    keys = new int[capacity];
    values = new double[capacity];
    states = new byte[capacity];
    this.missingEntries = missingEntries;
    mask = capacity - 1;
  }
  public Iterator iterator() {
    return new Iterator();
  }
  public boolean containsKey(final int key) {
    final int hash = hashOf(key);
    int index = hash & mask;
    if(containsKey(key, index)) {
      return true;
    }
    if(states[index] == FREE) {
      return false;
    }
    int j = index;
    for(int perturb = perturb(hash); states[index] != FREE; perturb >>= PERTURB_SHIFT) {
      j = probe(perturb, j);
      index = j & mask;
      if(containsKey(key, index)) {
        return true;
      }
    }
    return false;
  }
  private boolean containsKey(final int key, final int index) {
    return (key != 0 || states[index] == FULL) && keys[index] == key;
  }
  private boolean shouldGrowTable() {
    return size > (mask + 1) * LOAD_FACTOR;
  }
  private double doRemove(int index) {
    keys[index] = 0;
    states[index] = REMOVED;
    final double previous = values[index];
    values[index] = missingEntries;
    --size;
    ++count;
    return previous;
  }
  public double get(final int key) {
    final int hash = hashOf(key);
    int index = hash & mask;
    if(containsKey(key, index)) {
      return values[index];
    }
    if(states[index] == FREE) {
      return missingEntries;
    }
    int j = index;
    for(int perturb = perturb(hash); states[index] != FREE; perturb >>= PERTURB_SHIFT) {
      j = probe(perturb, j);
      index = j & mask;
      if(containsKey(key, index)) {
        return values[index];
      }
    }
    return missingEntries;
  }
  public double put(final int key, final double value) {
    int index = findInsertionIndex(key);
    double previous = missingEntries;
    boolean newMapping = true;
    if(index < 0) {
      index = changeIndexSign(index);
      previous = values[index];
      newMapping = false;
    }
    keys[index] = key;
    states[index] = FULL;
    values[index] = value;
    if(newMapping) {
      ++size;
      if(shouldGrowTable()) {
        growTable();
      }
      ++count;
    }
    return previous;
  }
  public double remove(final int key) {
    final int hash = hashOf(key);
    int index = hash & mask;
    if(containsKey(key, index)) {
      return doRemove(index);
    }
    if(states[index] == FREE) {
      return missingEntries;
    }
    int j = index;
    for(int perturb = perturb(hash); states[index] != FREE; perturb >>= PERTURB_SHIFT) {
      j = probe(perturb, j);
      index = j & mask;
      if(containsKey(key, index)) {
        return doRemove(index);
      }
    }
    return missingEntries;
  }
  private static int changeIndexSign(final int index) {
    return -index - 1;
  }
  private static int computeCapacity(final int expectedSize) {
    if(expectedSize == 0) {
      return 1;
    }
    final int capacity = (int)FastMath.ceil(expectedSize / LOAD_FACTOR);
    final int powerOfTwo = Integer.highestOneBit(capacity);
    if(powerOfTwo == capacity) {
      return capacity;
    }
    return nextPowerOfTwo(capacity);
  }
  private int findInsertionIndex(final int key) {
    return findInsertionIndex(keys, states, key, mask);
  }
  private static int findInsertionIndex(final int[] keys, final byte[] states, final int key, final int mask) {
    final int hash = hashOf(key);
    int index = hash & mask;
    if(states[index] == FREE) {
      return index;
    }
    else 
      if(states[index] == FULL && keys[index] == key) {
        return changeIndexSign(index);
      }
    int perturb = perturb(hash);
    int j = index;
    if(states[index] == FULL) {
      while(true){
        j = probe(perturb, j);
        index = j & mask;
        perturb >>= PERTURB_SHIFT;
        if(states[index] != FULL || keys[index] == key) {
          break ;
        }
      }
    }
    if(states[index] == FREE) {
      return index;
    }
    else 
      if(states[index] == FULL) {
        return changeIndexSign(index);
      }
    final int firstRemoved = index;
    while(true){
      j = probe(perturb, j);
      index = j & mask;
      if(states[index] == FREE) {
        return firstRemoved;
      }
      else {
        int var_4119 = keys[index];
        if(states[index] == FULL && var_4119 == key) {
          return changeIndexSign(index);
        }
      }
      perturb >>= PERTURB_SHIFT;
    }
  }
  private static int hashOf(final int key) {
    final int h = key ^ ((key >>> 20) ^ (key >>> 12));
    return h ^ (h >>> 7) ^ (h >>> 4);
  }
  private static int nextPowerOfTwo(final int i) {
    return Integer.highestOneBit(i) << 1;
  }
  private static int perturb(final int hash) {
    return hash & 0x7fffffff;
  }
  private static int probe(final int perturb, final int j) {
    return (j << 2) + j + perturb + 1;
  }
  public int size() {
    return size;
  }
  private void growTable() {
    final int oldLength = states.length;
    final int[] oldKeys = keys;
    final double[] oldValues = values;
    final byte[] oldStates = states;
    final int newLength = RESIZE_MULTIPLIER * oldLength;
    final int[] newKeys = new int[newLength];
    final double[] newValues = new double[newLength];
    final byte[] newStates = new byte[newLength];
    final int newMask = newLength - 1;
    for(int i = 0; i < oldLength; ++i) {
      if(oldStates[i] == FULL) {
        final int key = oldKeys[i];
        final int index = findInsertionIndex(newKeys, newStates, key, newMask);
        newKeys[index] = key;
        newValues[index] = oldValues[i];
        newStates[index] = FULL;
      }
    }
    mask = newMask;
    keys = newKeys;
    values = newValues;
    states = newStates;
  }
  private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    count = 0;
  }
  
  public class Iterator  {
    final private int referenceCount;
    private int current;
    private int next;
    private Iterator() {
      super();
      referenceCount = count;
      next = -1;
      try {
        advance();
      }
      catch (NoSuchElementException nsee) {
      }
    }
    public boolean hasNext() {
      return next >= 0;
    }
    public double value() throws ConcurrentModificationException, NoSuchElementException {
      if(referenceCount != count) {
        throw new ConcurrentModificationException();
      }
      if(current < 0) {
        throw new NoSuchElementException();
      }
      return values[current];
    }
    public int key() throws ConcurrentModificationException, NoSuchElementException {
      if(referenceCount != count) {
        throw new ConcurrentModificationException();
      }
      if(current < 0) {
        throw new NoSuchElementException();
      }
      return keys[current];
    }
    public void advance() throws ConcurrentModificationException, NoSuchElementException {
      if(referenceCount != count) {
        throw new ConcurrentModificationException();
      }
      current = next;
      try {
        while(states[++next] != FULL){
        }
      }
      catch (ArrayIndexOutOfBoundsException e) {
        next = -2;
        if(current < 0) {
          throw new NoSuchElementException();
        }
      }
    }
  }
}