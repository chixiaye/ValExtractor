package org.apache.commons.math3.stat.descriptive.rank;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.AbstractUnivariateStatistic;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

public class Percentile extends AbstractUnivariateStatistic implements Serializable  {
  final private static long serialVersionUID = -8091216485095130416L;
  final private static int MIN_SELECT_SIZE = 15;
  final private static int MAX_CACHED_LEVELS = 10;
  private double quantile = 0.0D;
  private int[] cachedPivots;
  public Percentile() {
    this(50.0D);
  }
  public Percentile(Percentile original) throws NullArgumentException {
    super();
    copy(original, this);
  }
  public Percentile(final double p) throws MathIllegalArgumentException {
    super();
    setQuantile(p);
    cachedPivots = null;
  }
  @Override() public Percentile copy() {
    Percentile result = new Percentile();
    copy(this, result);
    return result;
  }
  public double evaluate(final double p) throws MathIllegalArgumentException {
    return evaluate(getDataRef(), p);
  }
  public double evaluate(final double[] values, final double p) throws MathIllegalArgumentException {
    test(values, 0, 0);
    return evaluate(values, 0, values.length, p);
  }
  @Override() public double evaluate(final double[] values, final int start, final int length) throws MathIllegalArgumentException {
    return evaluate(values, start, length, quantile);
  }
  public double evaluate(final double[] values, final int begin, final int length, final double p) throws MathIllegalArgumentException {
    test(values, begin, length);
    if((p > 100) || (p <= 0)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_BOUNDS_QUANTILE_VALUE, p, 0, 100);
    }
    if(length == 0) {
      return Double.NaN;
    }
    if(length == 1) {
      return values[begin];
    }
    double n = length;
    double pos = p * (n + 1) / 100;
    double fpos = FastMath.floor(pos);
    int intPos = (int)fpos;
    double dif = pos - fpos;
    double[] work;
    int[] pivotsHeap;
    if(values == getDataRef()) {
      work = getDataRef();
      pivotsHeap = cachedPivots;
    }
    else {
      work = new double[length];
      System.arraycopy(values, begin, work, 0, length);
      pivotsHeap = new int[(0x1 << MAX_CACHED_LEVELS) - 1];
      Arrays.fill(pivotsHeap, -1);
    }
    if(pos < 1) {
      return select(work, pivotsHeap, 0);
    }
    if(pos >= n) {
      return select(work, pivotsHeap, length - 1);
    }
    double lower = select(work, pivotsHeap, intPos - 1);
    double upper = select(work, pivotsHeap, intPos);
    return lower + dif * (upper - lower);
  }
  public double getQuantile() {
    return quantile;
  }
  private double select(final double[] work, final int[] pivotsHeap, final int k) {
    int begin = 0;
    int end = work.length;
    int node = 0;
    while(end - begin > MIN_SELECT_SIZE){
      final int pivot;
      if((node < pivotsHeap.length) && (pivotsHeap[node] >= 0)) {
        pivot = pivotsHeap[node];
      }
      else {
        pivot = partition(work, begin, end, medianOf3(work, begin, end));
        if(node < pivotsHeap.length) {
          pivotsHeap[node] = pivot;
        }
      }
      if(k == pivot) {
        return work[k];
      }
      else 
        if(k < pivot) {
          end = pivot;
          node = FastMath.min(2 * node + 1, pivotsHeap.length);
        }
        else {
          begin = pivot + 1;
          node = FastMath.min(2 * node + 2, pivotsHeap.length);
        }
    }
    insertionSort(work, begin, end);
    return work[k];
  }
  int medianOf3(final double[] work, final int begin, final int end) {
    final int inclusiveEnd = end - 1;
    final int middle = begin + (inclusiveEnd - begin) / 2;
    final double wBegin = work[begin];
    final double wMiddle = work[middle];
    final double wEnd = work[inclusiveEnd];
    if(wBegin < wMiddle) {
      if(wMiddle < wEnd) {
        return middle;
      }
      else {
        return (wBegin < wEnd) ? inclusiveEnd : begin;
      }
    }
    else {
      if(wBegin < wEnd) {
        return begin;
      }
      else {
        return (wMiddle < wEnd) ? inclusiveEnd : middle;
      }
    }
  }
  private int partition(final double[] work, final int begin, final int end, final int pivot) {
    final double value = work[pivot];
    work[pivot] = work[begin];
    int i = begin + 1;
    int j = end - 1;
    while(i < j){
      while((i < j) && (work[j] > value)){
        --j;
      }
      while((i < j) && (work[i] < value)){
        ++i;
      }
      if(i < j) {
        final double tmp = work[i];
        work[i++] = work[j];
        work[j--] = tmp;
      }
    }
    if((i >= end) || (work[i] > value)) {
      --i;
    }
    work[begin] = work[i];
    work[i] = value;
    return i;
  }
  public static void copy(Percentile source, Percentile dest) throws NullArgumentException {
    MathUtils.checkNotNull(source);
    MathUtils.checkNotNull(dest);
    dest.setData(source.getDataRef());
    if(source.cachedPivots != null) {
      int[] var_3819 = source.cachedPivots;
      System.arraycopy(source.cachedPivots, 0, dest.cachedPivots, 0, var_3819.length);
    }
    dest.quantile = source.quantile;
  }
  private void insertionSort(final double[] work, final int begin, final int end) {
    for(int j = begin + 1; j < end; j++) {
      final double saved = work[j];
      int i = j - 1;
      while((i >= begin) && (saved < work[i])){
        work[i + 1] = work[i];
        i--;
      }
      work[i + 1] = saved;
    }
  }
  @Override() public void setData(final double[] values) {
    if(values == null) {
      cachedPivots = null;
    }
    else {
      cachedPivots = new int[(0x1 << MAX_CACHED_LEVELS) - 1];
      Arrays.fill(cachedPivots, -1);
    }
    super.setData(values);
  }
  @Override() public void setData(final double[] values, final int begin, final int length) throws MathIllegalArgumentException {
    if(values == null) {
      cachedPivots = null;
    }
    else {
      cachedPivots = new int[(0x1 << MAX_CACHED_LEVELS) - 1];
      Arrays.fill(cachedPivots, -1);
    }
    super.setData(values, begin, length);
  }
  public void setQuantile(final double p) throws MathIllegalArgumentException {
    if(p <= 0 || p > 100) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_BOUNDS_QUANTILE_VALUE, p, 0, 100);
    }
    quantile = p;
  }
}