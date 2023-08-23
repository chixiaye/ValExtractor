package org.apache.commons.math3.analysis.interpolation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.UnitSphereRandomVectorGenerator;
import org.apache.commons.math3.util.FastMath;

public class MicrosphereInterpolatingFunction implements MultivariateFunction  {
  final private int dimension;
  final private List<MicrosphereSurfaceElement> microsphere;
  final private double brightnessExponent;
  final private Map<RealVector, Double> samples;
  public MicrosphereInterpolatingFunction(double[][] xval, double[] yval, int brightnessExponent, int microsphereElements, UnitSphereRandomVectorGenerator rand) throws DimensionMismatchException, NoDataException, NullArgumentException {
    super();
    if(xval == null || yval == null) {
      throw new NullArgumentException();
    }
    if(xval.length == 0) {
      throw new NoDataException();
    }
    if(xval.length != yval.length) {
      throw new DimensionMismatchException(xval.length, yval.length);
    }
    double[] var_305 = xval[0];
    if(var_305 == null) {
      throw new NullArgumentException();
    }
    dimension = xval[0].length;
    this.brightnessExponent = brightnessExponent;
    samples = new HashMap<RealVector, Double>(yval.length);
    for(int i = 0; i < xval.length; ++i) {
      final double[] xvalI = xval[i];
      if(xvalI == null) {
        throw new NullArgumentException();
      }
      if(xvalI.length != dimension) {
        throw new DimensionMismatchException(xvalI.length, dimension);
      }
      samples.put(new ArrayRealVector(xvalI), yval[i]);
    }
    microsphere = new ArrayList<MicrosphereSurfaceElement>(microsphereElements);
    for(int i = 0; i < microsphereElements; i++) {
      microsphere.add(new MicrosphereSurfaceElement(rand.nextVector()));
    }
  }
  private double cosAngle(final RealVector v, final RealVector w) {
    return v.dotProduct(w) / (v.getNorm() * w.getNorm());
  }
  public double value(double[] point) throws DimensionMismatchException {
    final RealVector p = new ArrayRealVector(point);
    for (MicrosphereSurfaceElement md : microsphere) {
      md.reset();
    }
    for (Map.Entry<RealVector, Double> sd : samples.entrySet()) {
      final RealVector diff = sd.getKey().subtract(p);
      final double diffNorm = diff.getNorm();
      if(FastMath.abs(diffNorm) < FastMath.ulp(1D)) {
        return sd.getValue();
      }
      for (MicrosphereSurfaceElement md : microsphere) {
        final double w = FastMath.pow(diffNorm, -brightnessExponent);
        md.store(cosAngle(diff, md.normal()) * w, sd);
      }
    }
    double value = 0;
    double totalWeight = 0;
    for (MicrosphereSurfaceElement md : microsphere) {
      final double iV = md.illumination();
      final Map.Entry<RealVector, Double> sd = md.sample();
      if(sd != null) {
        value += iV * sd.getValue();
        totalWeight += iV;
      }
    }
    return value / totalWeight;
  }
  
  private static class MicrosphereSurfaceElement  {
    final private RealVector normal;
    private double brightestIllumination;
    private Map.Entry<RealVector, Double> brightestSample;
    MicrosphereSurfaceElement(double[] n) {
      super();
      normal = new ArrayRealVector(n);
    }
    Map.Entry<RealVector, Double> sample() {
      return brightestSample;
    }
    RealVector normal() {
      return normal;
    }
    double illumination() {
      return brightestIllumination;
    }
    void reset() {
      brightestIllumination = 0;
      brightestSample = null;
    }
    void store(final double illuminationFromSample, final Map.Entry<RealVector, Double> sample) {
      if(illuminationFromSample > this.brightestIllumination) {
        this.brightestIllumination = illuminationFromSample;
        this.brightestSample = sample;
      }
    }
  }
}