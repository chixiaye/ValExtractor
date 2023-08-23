package org.apache.commons.math3.transform;
import java.io.Serializable;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.FastMath;

public class FastSineTransformer implements RealTransformer, Serializable  {
  final static long serialVersionUID = 20120211L;
  final private DstNormalization normalization;
  public FastSineTransformer(final DstNormalization normalization) {
    super();
    this.normalization = normalization;
  }
  protected double[] fst(double[] f) throws MathIllegalArgumentException {
    final double[] transformed = new double[f.length];
    if(!ArithmeticUtils.isPowerOfTwo(f.length)) {
      throw new MathIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO_CONSIDER_PADDING, Integer.valueOf(f.length));
    }
    if(f[0] != 0.0D) {
      throw new MathIllegalArgumentException(LocalizedFormats.FIRST_ELEMENT_NOT_ZERO, Double.valueOf(f[0]));
    }
    final int n = f.length;
    if(n == 1) {
      transformed[0] = 0.0D;
      return transformed;
    }
    final double[] x = new double[n];
    x[0] = 0.0D;
    x[n >> 1] = 2.0D * f[n >> 1];
    for(int i = 1; i < (n >> 1); i++) {
      final double a = FastMath.sin(i * FastMath.PI / n) * (f[i] + f[n - i]);
      double var_4039 = f[n - i];
      final double b = 0.5D * (f[i] - var_4039);
      x[i] = a + b;
      x[n - i] = a - b;
    }
    FastFourierTransformer transformer;
    transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    Complex[] y = transformer.transform(x, TransformType.FORWARD);
    transformed[0] = 0.0D;
    transformed[1] = 0.5D * y[0].getReal();
    for(int i = 1; i < (n >> 1); i++) {
      transformed[2 * i] = -y[i].getImaginary();
      transformed[2 * i + 1] = y[i].getReal() + transformed[2 * i - 1];
    }
    return transformed;
  }
  public double[] transform(final double[] f, final TransformType type) {
    if(normalization == DstNormalization.ORTHOGONAL_DST_I) {
      final double s = FastMath.sqrt(2.0D / f.length);
      return TransformUtils.scaleArray(fst(f), s);
    }
    if(type == TransformType.FORWARD) {
      return fst(f);
    }
    final double s = 2.0D / f.length;
    return TransformUtils.scaleArray(fst(f), s);
  }
  public double[] transform(final UnivariateFunction f, final double min, final double max, final int n, final TransformType type) {
    final double[] data = FunctionUtils.sample(f, min, max, n);
    data[0] = 0.0D;
    return transform(data, type);
  }
}