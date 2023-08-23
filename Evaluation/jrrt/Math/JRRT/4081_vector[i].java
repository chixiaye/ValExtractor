package org.apache.commons.math3.transform;
import java.io.Serializable;
import java.lang.reflect.Array;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;

public class FastFourierTransformer implements Serializable  {
  final static long serialVersionUID = 20120210L;
  final private static double[] W_SUB_N_R = { 0x1.0p0D, -0x1.0p0D, 0x1.1a62633145c07p-54D, 0x1.6a09e667f3bcdp-1D, 0x1.d906bcf328d46p-1D, 0x1.f6297cff75cbp-1D, 0x1.fd88da3d12526p-1D, 0x1.ff621e3796d7ep-1D, 0x1.ffd886084cd0dp-1D, 0x1.fff62169b92dbp-1D, 0x1.fffd8858e8a92p-1D, 0x1.ffff621621d02p-1D, 0x1.ffffd88586ee6p-1D, 0x1.fffff62161a34p-1D, 0x1.fffffd8858675p-1D, 0x1.ffffff621619cp-1D, 0x1.ffffffd885867p-1D, 0x1.fffffff62161ap-1D, 0x1.fffffffd88586p-1D, 0x1.ffffffff62162p-1D, 0x1.ffffffffd8858p-1D, 0x1.fffffffff6216p-1D, 0x1.fffffffffd886p-1D, 0x1.ffffffffff621p-1D, 0x1.ffffffffffd88p-1D, 0x1.fffffffffff62p-1D, 0x1.fffffffffffd9p-1D, 0x1.ffffffffffff6p-1D, 0x1.ffffffffffffep-1D, 0x1.fffffffffffffp-1D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D, 0x1.0p0D } ;
  final private static double[] W_SUB_N_I = { 0x1.1a62633145c07p-52D, -0x1.1a62633145c07p-53D, -0x1.0p0D, -0x1.6a09e667f3bccp-1D, -0x1.87de2a6aea963p-2D, -0x1.8f8b83c69a60ap-3D, -0x1.917a6bc29b42cp-4D, -0x1.91f65f10dd814p-5D, -0x1.92155f7a3667ep-6D, -0x1.921d1fcdec784p-7D, -0x1.921f0fe670071p-8D, -0x1.921f8becca4bap-9D, -0x1.921faaee6472dp-10D, -0x1.921fb2aecb36p-11D, -0x1.921fb49ee4ea6p-12D, -0x1.921fb51aeb57bp-13D, -0x1.921fb539ecf31p-14D, -0x1.921fb541ad59ep-15D, -0x1.921fb5439d73ap-16D, -0x1.921fb544197ap-17D, -0x1.921fb544387bap-18D, -0x1.921fb544403c1p-19D, -0x1.921fb544422c2p-20D, -0x1.921fb54442a83p-21D, -0x1.921fb54442c73p-22D, -0x1.921fb54442cefp-23D, -0x1.921fb54442d0ep-24D, -0x1.921fb54442d15p-25D, -0x1.921fb54442d17p-26D, -0x1.921fb54442d18p-27D, -0x1.921fb54442d18p-28D, -0x1.921fb54442d18p-29D, -0x1.921fb54442d18p-30D, -0x1.921fb54442d18p-31D, -0x1.921fb54442d18p-32D, -0x1.921fb54442d18p-33D, -0x1.921fb54442d18p-34D, -0x1.921fb54442d18p-35D, -0x1.921fb54442d18p-36D, -0x1.921fb54442d18p-37D, -0x1.921fb54442d18p-38D, -0x1.921fb54442d18p-39D, -0x1.921fb54442d18p-40D, -0x1.921fb54442d18p-41D, -0x1.921fb54442d18p-42D, -0x1.921fb54442d18p-43D, -0x1.921fb54442d18p-44D, -0x1.921fb54442d18p-45D, -0x1.921fb54442d18p-46D, -0x1.921fb54442d18p-47D, -0x1.921fb54442d18p-48D, -0x1.921fb54442d18p-49D, -0x1.921fb54442d18p-50D, -0x1.921fb54442d18p-51D, -0x1.921fb54442d18p-52D, -0x1.921fb54442d18p-53D, -0x1.921fb54442d18p-54D, -0x1.921fb54442d18p-55D, -0x1.921fb54442d18p-56D, -0x1.921fb54442d18p-57D, -0x1.921fb54442d18p-58D, -0x1.921fb54442d18p-59D, -0x1.921fb54442d18p-60D } ;
  final private DftNormalization normalization;
  public FastFourierTransformer(final DftNormalization normalization) {
    super();
    this.normalization = normalization;
  }
  public Complex[] transform(final double[] f, final TransformType type) {
    final double[][] dataRI = new double[][]{ MathArrays.copyOf(f, f.length), new double[f.length] } ;
    transformInPlace(dataRI, normalization, type);
    return TransformUtils.createComplexArray(dataRI);
  }
  public Complex[] transform(final UnivariateFunction f, final double min, final double max, final int n, final TransformType type) {
    final double[] data = FunctionUtils.sample(f, min, max, n);
    return transform(data, type);
  }
  public Complex[] transform(final Complex[] f, final TransformType type) {
    final double[][] dataRI = TransformUtils.createRealImaginaryArray(f);
    transformInPlace(dataRI, normalization, type);
    return TransformUtils.createComplexArray(dataRI);
  }
  @Deprecated() public Object mdfft(Object mdca, TransformType type) {
    MultiDimensionalComplexMatrix mdcm = (MultiDimensionalComplexMatrix)new MultiDimensionalComplexMatrix(mdca).clone();
    int[] dimensionSize = mdcm.getDimensionSizes();
    for(int i = 0; i < dimensionSize.length; i++) {
      mdfft(mdcm, type, i, new int[0]);
    }
    return mdcm.getArray();
  }
  private static void bitReversalShuffle2(double[] a, double[] b) {
    final int n = a.length;
    assert b.length == n;
    final int halfOfN = n >> 1;
    int j = 0;
    for(int i = 0; i < n; i++) {
      if(i < j) {
        double temp = a[i];
        a[i] = a[j];
        a[j] = temp;
        temp = b[i];
        b[i] = b[j];
        b[j] = temp;
      }
      int k = halfOfN;
      while(k <= j && k > 0){
        j -= k;
        k >>= 1;
      }
      j += k;
    }
  }
  @Deprecated() private void mdfft(MultiDimensionalComplexMatrix mdcm, TransformType type, int d, int[] subVector) {
    int[] dimensionSize = mdcm.getDimensionSizes();
    if(subVector.length == dimensionSize.length) {
      Complex[] temp = new Complex[dimensionSize[d]];
      for(int i = 0; i < dimensionSize[d]; i++) {
        subVector[d] = i;
        temp[i] = mdcm.get(subVector);
      }
      temp = transform(temp, type);
      for(int i = 0; i < dimensionSize[d]; i++) {
        subVector[d] = i;
        mdcm.set(temp[i], subVector);
      }
    }
    else {
      int[] vector = new int[subVector.length + 1];
      System.arraycopy(subVector, 0, vector, 0, subVector.length);
      if(subVector.length == d) {
        vector[d] = 0;
        mdfft(mdcm, type, d, vector);
      }
      else {
        for(int i = 0; i < dimensionSize[subVector.length]; i++) {
          vector[subVector.length] = i;
          mdfft(mdcm, type, d, vector);
        }
      }
    }
  }
  private static void normalizeTransformedData(final double[][] dataRI, final DftNormalization normalization, final TransformType type) {
    final double[] dataR = dataRI[0];
    final double[] dataI = dataRI[1];
    final int n = dataR.length;
    assert dataI.length == n;
    switch (normalization){
      case STANDARD:
      if(type == TransformType.INVERSE) {
        final double scaleFactor = 1.0D / ((double)n);
        for(int i = 0; i < n; i++) {
          dataR[i] *= scaleFactor;
          dataI[i] *= scaleFactor;
        }
      }
      break ;
      case UNITARY:
      final double scaleFactor = 1.0D / FastMath.sqrt(n);
      for(int i = 0; i < n; i++) {
        dataR[i] *= scaleFactor;
        dataI[i] *= scaleFactor;
      }
      break ;
      default:
      throw new MathIllegalStateException();
    }
  }
  public static void transformInPlace(final double[][] dataRI, final DftNormalization normalization, final TransformType type) {
    if(dataRI.length != 2) {
      throw new DimensionMismatchException(dataRI.length, 2);
    }
    final double[] dataR = dataRI[0];
    final double[] dataI = dataRI[1];
    if(dataR.length != dataI.length) {
      throw new DimensionMismatchException(dataI.length, dataR.length);
    }
    final int n = dataR.length;
    if(!ArithmeticUtils.isPowerOfTwo(n)) {
      throw new MathIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO_CONSIDER_PADDING, Integer.valueOf(n));
    }
    if(n == 1) {
      return ;
    }
    else 
      if(n == 2) {
        final double srcR0 = dataR[0];
        final double srcI0 = dataI[0];
        final double srcR1 = dataR[1];
        final double srcI1 = dataI[1];
        dataR[0] = srcR0 + srcR1;
        dataI[0] = srcI0 + srcI1;
        dataR[1] = srcR0 - srcR1;
        dataI[1] = srcI0 - srcI1;
        normalizeTransformedData(dataRI, normalization, type);
        return ;
      }
    bitReversalShuffle2(dataR, dataI);
    if(type == TransformType.INVERSE) {
      for(int i0 = 0; i0 < n; i0 += 4) {
        final int i1 = i0 + 1;
        final int i2 = i0 + 2;
        final int i3 = i0 + 3;
        final double srcR0 = dataR[i0];
        final double srcI0 = dataI[i0];
        final double srcR1 = dataR[i2];
        final double srcI1 = dataI[i2];
        final double srcR2 = dataR[i1];
        final double srcI2 = dataI[i1];
        final double srcR3 = dataR[i3];
        final double srcI3 = dataI[i3];
        dataR[i0] = srcR0 + srcR1 + srcR2 + srcR3;
        dataI[i0] = srcI0 + srcI1 + srcI2 + srcI3;
        dataR[i1] = srcR0 - srcR2 + (srcI3 - srcI1);
        dataI[i1] = srcI0 - srcI2 + (srcR1 - srcR3);
        dataR[i2] = srcR0 - srcR1 + srcR2 - srcR3;
        dataI[i2] = srcI0 - srcI1 + srcI2 - srcI3;
        dataR[i3] = srcR0 - srcR2 + (srcI1 - srcI3);
        dataI[i3] = srcI0 - srcI2 + (srcR3 - srcR1);
      }
    }
    else {
      for(int i0 = 0; i0 < n; i0 += 4) {
        final int i1 = i0 + 1;
        final int i2 = i0 + 2;
        final int i3 = i0 + 3;
        final double srcR0 = dataR[i0];
        final double srcI0 = dataI[i0];
        final double srcR1 = dataR[i2];
        final double srcI1 = dataI[i2];
        final double srcR2 = dataR[i1];
        final double srcI2 = dataI[i1];
        final double srcR3 = dataR[i3];
        final double srcI3 = dataI[i3];
        dataR[i0] = srcR0 + srcR1 + srcR2 + srcR3;
        dataI[i0] = srcI0 + srcI1 + srcI2 + srcI3;
        dataR[i1] = srcR0 - srcR2 + (srcI1 - srcI3);
        dataI[i1] = srcI0 - srcI2 + (srcR3 - srcR1);
        dataR[i2] = srcR0 - srcR1 + srcR2 - srcR3;
        dataI[i2] = srcI0 - srcI1 + srcI2 - srcI3;
        dataR[i3] = srcR0 - srcR2 + (srcI3 - srcI1);
        dataI[i3] = srcI0 - srcI2 + (srcR1 - srcR3);
      }
    }
    int lastN0 = 4;
    int lastLogN0 = 2;
    while(lastN0 < n){
      int n0 = lastN0 << 1;
      int logN0 = lastLogN0 + 1;
      double wSubN0R = W_SUB_N_R[logN0];
      double wSubN0I = W_SUB_N_I[logN0];
      if(type == TransformType.INVERSE) {
        wSubN0I = -wSubN0I;
      }
      for(int destEvenStartIndex = 0; destEvenStartIndex < n; destEvenStartIndex += n0) {
        int destOddStartIndex = destEvenStartIndex + lastN0;
        double wSubN0ToRR = 1;
        double wSubN0ToRI = 0;
        for(int r = 0; r < lastN0; r++) {
          double grR = dataR[destEvenStartIndex + r];
          double grI = dataI[destEvenStartIndex + r];
          double hrR = dataR[destOddStartIndex + r];
          double hrI = dataI[destOddStartIndex + r];
          dataR[destEvenStartIndex + r] = grR + wSubN0ToRR * hrR - wSubN0ToRI * hrI;
          dataI[destEvenStartIndex + r] = grI + wSubN0ToRR * hrI + wSubN0ToRI * hrR;
          dataR[destOddStartIndex + r] = grR - (wSubN0ToRR * hrR - wSubN0ToRI * hrI);
          dataI[destOddStartIndex + r] = grI - (wSubN0ToRR * hrI + wSubN0ToRI * hrR);
          double nextWsubN0ToRR = wSubN0ToRR * wSubN0R - wSubN0ToRI * wSubN0I;
          double nextWsubN0ToRI = wSubN0ToRR * wSubN0I + wSubN0ToRI * wSubN0R;
          wSubN0ToRR = nextWsubN0ToRR;
          wSubN0ToRI = nextWsubN0ToRI;
        }
      }
      lastN0 = n0;
      lastLogN0 = logN0;
    }
    normalizeTransformedData(dataRI, normalization, type);
  }
  
  @Deprecated() private static class MultiDimensionalComplexMatrix implements Cloneable  {
    protected int[] dimensionSize;
    protected Object multiDimensionalComplexArray;
    public MultiDimensionalComplexMatrix(Object multiDimensionalComplexArray) {
      super();
      this.multiDimensionalComplexArray = multiDimensionalComplexArray;
      int numOfDimensions = 0;
      for(java.lang.Object lastDimension = multiDimensionalComplexArray; lastDimension instanceof Object[]; ) {
        final Object[] array = (Object[])lastDimension;
        numOfDimensions++;
        lastDimension = array[0];
      }
      dimensionSize = new int[numOfDimensions];
      numOfDimensions = 0;
      for(java.lang.Object lastDimension = multiDimensionalComplexArray; lastDimension instanceof Object[]; ) {
        final Object[] array = (Object[])lastDimension;
        dimensionSize[numOfDimensions++] = array.length;
        lastDimension = array[0];
      }
    }
    public Complex get(int ... vector) throws DimensionMismatchException {
      if(vector == null) {
        if(dimensionSize.length > 0) {
          throw new DimensionMismatchException(0, dimensionSize.length);
        }
        return null;
      }
      if(vector.length != dimensionSize.length) {
        throw new DimensionMismatchException(vector.length, dimensionSize.length);
      }
      Object lastDimension = multiDimensionalComplexArray;
      for(int i = 0; i < dimensionSize.length; i++) {
        lastDimension = ((Object[])lastDimension)[vector[i]];
      }
      return (Complex)lastDimension;
    }
    public Complex set(Complex magnitude, int ... vector) throws DimensionMismatchException {
      if(vector == null) {
        if(dimensionSize.length > 0) {
          throw new DimensionMismatchException(0, dimensionSize.length);
        }
        return null;
      }
      if(vector.length != dimensionSize.length) {
        throw new DimensionMismatchException(vector.length, dimensionSize.length);
      }
      Object[] lastDimension = (Object[])multiDimensionalComplexArray;
      for(int i = 0; i < dimensionSize.length - 1; i++) {
        lastDimension = (Object[])lastDimension[vector[i]];
      }
      Complex lastValue = (Complex)lastDimension[vector[dimensionSize.length - 1]];
      lastDimension[vector[dimensionSize.length - 1]] = magnitude;
      return lastValue;
    }
    @Override() public Object clone() {
      MultiDimensionalComplexMatrix mdcm = new MultiDimensionalComplexMatrix(Array.newInstance(Complex.class, dimensionSize));
      clone(mdcm);
      return mdcm;
    }
    public Object getArray() {
      return multiDimensionalComplexArray;
    }
    public int[] getDimensionSizes() {
      return dimensionSize.clone();
    }
    private void clone(MultiDimensionalComplexMatrix mdcm) {
      int[] vector = new int[dimensionSize.length];
      int size = 1;
      for(int i = 0; i < dimensionSize.length; i++) {
        size *= dimensionSize[i];
      }
      int[][] vectorList = new int[size][dimensionSize.length];
      for (int[] nextVector : vectorList) {
        System.arraycopy(vector, 0, nextVector, 0, dimensionSize.length);
        for(int i = 0; i < dimensionSize.length; i++) {
          vector[i]++;
          int var_4081 = vector[i];
          if(var_4081 < dimensionSize[i]) {
            break ;
          }
          else {
            vector[i] = 0;
          }
        }
      }
      for (int[] nextVector : vectorList) {
        mdcm.set(get(nextVector), nextVector);
      }
    }
  }
}