package org.apache.commons.math3.util;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;

public class MultidimensionalCounter implements Iterable<Integer>  {
  final private int dimension;
  final private int[] uniCounterOffset;
  final private int[] size;
  final private int totalSize;
  final private int last;
  public MultidimensionalCounter(int ... size) throws NotStrictlyPositiveException {
    super();
    dimension = size.length;
    this.size = MathArrays.copyOf(size);
    uniCounterOffset = new int[dimension];
    last = dimension - 1;
    int tS = size[last];
    for(int i = 0; i < last; i++) {
      int count = 1;
      for(int j = i + 1; j < dimension; j++) {
        count *= size[j];
      }
      uniCounterOffset[i] = count;
      tS *= size[i];
    }
    uniCounterOffset[last] = 0;
    if(tS <= 0) {
      throw new NotStrictlyPositiveException(tS);
    }
    totalSize = tS;
  }
  public Iterator iterator() {
    return new Iterator();
  }
  @Override() public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < dimension; i++) {
      sb.append("[").append(getCount(i)).append("]");
    }
    return sb.toString();
  }
  public int getCount(int ... c) throws OutOfRangeException, DimensionMismatchException {
    int var_4211 = c.length;
    if(var_4211 != dimension) {
      throw new DimensionMismatchException(c.length, dimension);
    }
    int count = 0;
    for(int i = 0; i < dimension; i++) {
      final int index = c[i];
      if(index < 0 || index >= size[i]) {
        throw new OutOfRangeException(index, 0, size[i] - 1);
      }
      count += uniCounterOffset[i] * c[i];
    }
    return count + c[last];
  }
  public int getDimension() {
    return dimension;
  }
  public int getSize() {
    return totalSize;
  }
  public int[] getCounts(int index) throws OutOfRangeException {
    if(index < 0 || index >= totalSize) {
      throw new OutOfRangeException(index, 0, totalSize);
    }
    final int[] indices = new int[dimension];
    int count = 0;
    for(int i = 0; i < last; i++) {
      int idx = 0;
      final int offset = uniCounterOffset[i];
      while(count <= index){
        count += offset;
        ++idx;
      }
      --idx;
      count -= offset;
      indices[i] = idx;
    }
    indices[last] = index - count;
    return indices;
  }
  public int[] getSizes() {
    return MathArrays.copyOf(size);
  }
  
  public class Iterator implements java.util.Iterator<Integer>  {
    final private int[] counter = new int[dimension];
    private int count = -1;
    Iterator() {
      super();
      counter[last] = -1;
    }
    public Integer next() {
      for(int i = last; i >= 0; i--) {
        if(counter[i] == size[i] - 1) {
          counter[i] = 0;
        }
        else {
          ++counter[i];
          break ;
        }
      }
      return ++count;
    }
    public boolean hasNext() {
      for(int i = 0; i < dimension; i++) {
        if(counter[i] != size[i] - 1) {
          return true;
        }
      }
      return false;
    }
    public int getCount() {
      return count;
    }
    public int getCount(int dim) {
      return counter[dim];
    }
    public int[] getCounts() {
      return MathArrays.copyOf(counter);
    }
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}