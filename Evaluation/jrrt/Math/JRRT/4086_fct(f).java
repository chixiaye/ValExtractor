package org.apache.commons.math3.transform;
import java.io.Serializable;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.FastMath;

public class FastCosineTransformer implements RealTransformer, Serializable  {
  final static long serialVersionUID = 20120212L;
  final private DctNormalization normalization;
  public FastCosineTransformer(final DctNormalization normalization) {
    super();
    this.normalization = normalization;
  }
  protected double[] fct(double[] f) throws MathIllegalArgumentException {
    final double[] transformed = new double[f.length];
    final int n = f.length - 1;
    if(!ArithmeticUtils.isPowerOfTwo(n)) {
      throw new MathIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO_PLUS_ONE, Integer.valueOf(f.length));
    }
    if(n == 1) {
      transformed[0] = 0.5D * (f[0] + f[1]);
      transformed[1] = 0.5D * (f[0] - f[1]);
      return transformed;
    }
    final double[] x = new double[n];
    x[0] = 0.5D * (f[0] + f[n]);
    x[n >> 1] = f[n >> 1];
    double t1 = 0.5D * (f[0] - f[n]);
    for(int i = 1; i < (n >> 1); i++) {
      final double a = 0.5D * (f[i] + f[n - i]);
      final double b = FastMath.sin(i * FastMath.PI / n) * (f[i] - f[n - i]);
      final double c = FastMath.cos(i * FastMath.PI / n) * (f[i] - f[n - i]);
      x[i] = a - b;
      x[n - i] = a + b;
      t1 += c;
    }
    FastFourierTransformer transformer;
    transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    Complex[] y = transformer.transform(x, TransformType.FORWARD);
    transformed[0] = y[0].getReal();
    transformed[1] = t1;
    for(int i = 1; i < (n >> 1); i++) {
      transformed[2 * i] = y[i].getReal();
      transformed[2 * i + 1] = transformed[2 * i - 1] - y[i].getImaginary();
    }
    transformed[n] = y[n >> 1].getReal();
    return transformed;
  }
  public double[] transform(final double[] f, final TransformType type) throws MathIllegalArgumentException {
    if(type == TransformType.FORWARD) {
      if(normalization == DctNormalization.ORTHOGONAL_DCT_I) {
        final double s = FastMath.sqrt(2.0D / (f.length - 1));
        double[] var_4086 = fct(f);
        return TransformUtils.scaleArray(var_4086, s);
      }
      return fct(f);
    }
    final double s2 = 2.0D / (f.length - 1);
    final double s1;
    if(normalization == DctNormalization.ORTHOGONAL_DCT_I) {
      s1 = FastMath.sqrt(s2);
    }
    else {
      s1 = s2;
    }
    return TransformUtils.scaleArray(fct(f), s1);
  }
  public double[] transform(final UnivariateFunction f, final double min, final double max, final int n, final TransformType type) throws MathIllegalArgumentException {
    final double[] data = FunctionUtils.sample(f, min, max, n);
    return transform(data, type);
  }
}