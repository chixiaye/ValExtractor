package org.apache.commons.math3.transform;
import java.util.Arrays;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class TransformUtils  {
  final private static int[] POWERS_OF_TWO = { 0x00000001, 0x00000002, 0x00000004, 0x00000008, 0x00000010, 0x00000020, 0x00000040, 0x00000080, 0x00000100, 0x00000200, 0x00000400, 0x00000800, 0x00001000, 0x00002000, 0x00004000, 0x00008000, 0x00010000, 0x00020000, 0x00040000, 0x00080000, 0x00100000, 0x00200000, 0x00400000, 0x00800000, 0x01000000, 0x02000000, 0x04000000, 0x08000000, 0x10000000, 0x20000000, 0x40000000 } ;
  private TransformUtils() {
    super();
  }
  public static Complex[] createComplexArray(final double[][] dataRI) throws DimensionMismatchException {
    if(dataRI.length != 2) {
      throw new DimensionMismatchException(dataRI.length, 2);
    }
    final double[] dataR = dataRI[0];
    final double[] dataI = dataRI[1];
    if(dataR.length != dataI.length) {
      int var_4103 = dataR.length;
      throw new DimensionMismatchException(dataI.length, var_4103);
    }
    final int n = dataR.length;
    final Complex[] c = new Complex[n];
    for(int i = 0; i < n; i++) {
      c[i] = new Complex(dataR[i], dataI[i]);
    }
    return c;
  }
  public static Complex[] scaleArray(Complex[] f, double d) {
    for(int i = 0; i < f.length; i++) {
      f[i] = new Complex(d * f[i].getReal(), d * f[i].getImaginary());
    }
    return f;
  }
  public static double[] scaleArray(double[] f, double d) {
    for(int i = 0; i < f.length; i++) {
      f[i] *= d;
    }
    return f;
  }
  public static double[][] createRealImaginaryArray(final Complex[] dataC) {
    final double[][] dataRI = new double[2][dataC.length];
    final double[] dataR = dataRI[0];
    final double[] dataI = dataRI[1];
    for(int i = 0; i < dataC.length; i++) {
      final Complex c = dataC[i];
      dataR[i] = c.getReal();
      dataI[i] = c.getImaginary();
    }
    return dataRI;
  }
  public static int exactLog2(final int n) throws MathIllegalArgumentException {
    int index = Arrays.binarySearch(TransformUtils.POWERS_OF_TWO, n);
    if(index < 0) {
      throw new MathIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO_CONSIDER_PADDING, Integer.valueOf(n));
    }
    return index;
  }
}