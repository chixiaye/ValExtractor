package org.apache.commons.math3.ode.sampling;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.ode.EquationsMapper;
import org.apache.commons.math3.util.FastMath;

public class NordsieckStepInterpolator extends AbstractStepInterpolator  {
  final private static long serialVersionUID = -7179861704951334960L;
  protected double[] stateVariation;
  private double scalingH;
  private double referenceTime;
  private double[] scaled;
  private Array2DRowRealMatrix nordsieck;
  public NordsieckStepInterpolator() {
    super();
  }
  public NordsieckStepInterpolator(final NordsieckStepInterpolator interpolator) {
    super(interpolator);
    scalingH = interpolator.scalingH;
    referenceTime = interpolator.referenceTime;
    if(interpolator.scaled != null) {
      scaled = interpolator.scaled.clone();
    }
    if(interpolator.nordsieck != null) {
      nordsieck = new Array2DRowRealMatrix(interpolator.nordsieck.getDataRef(), true);
    }
    double[] var_2892 = interpolator.stateVariation;
    if(var_2892 != null) {
      stateVariation = interpolator.stateVariation.clone();
    }
  }
  @Override() protected StepInterpolator doCopy() {
    return new NordsieckStepInterpolator(this);
  }
  public double[] getInterpolatedStateVariation() throws MaxCountExceededException {
    getInterpolatedState();
    return stateVariation;
  }
  @Override() protected void computeInterpolatedStateAndDerivatives(final double theta, final double oneMinusThetaH) {
    final double x = interpolatedTime - referenceTime;
    final double normalizedAbscissa = x / scalingH;
    Arrays.fill(stateVariation, 0.0D);
    Arrays.fill(interpolatedDerivatives, 0.0D);
    final double[][] nData = nordsieck.getDataRef();
    for(int i = nData.length - 1; i >= 0; --i) {
      final int order = i + 2;
      final double[] nDataI = nData[i];
      final double power = FastMath.pow(normalizedAbscissa, order);
      for(int j = 0; j < nDataI.length; ++j) {
        final double d = nDataI[j] * power;
        stateVariation[j] += d;
        interpolatedDerivatives[j] += order * d;
      }
    }
    for(int j = 0; j < currentState.length; ++j) {
      stateVariation[j] += scaled[j] * normalizedAbscissa;
      interpolatedState[j] = currentState[j] + stateVariation[j];
      interpolatedDerivatives[j] = (interpolatedDerivatives[j] + scaled[j] * normalizedAbscissa) / x;
    }
  }
  @Override() public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    final double t = readBaseExternal(in);
    scalingH = in.readDouble();
    referenceTime = in.readDouble();
    final int n = (currentState == null) ? -1 : currentState.length;
    final boolean hasScaled = in.readBoolean();
    if(hasScaled) {
      scaled = new double[n];
      for(int j = 0; j < n; ++j) {
        scaled[j] = in.readDouble();
      }
    }
    else {
      scaled = null;
    }
    final boolean hasNordsieck = in.readBoolean();
    if(hasNordsieck) {
      nordsieck = (Array2DRowRealMatrix)in.readObject();
    }
    else {
      nordsieck = null;
    }
    if(hasScaled && hasNordsieck) {
      stateVariation = new double[n];
      setInterpolatedTime(t);
    }
    else {
      stateVariation = null;
    }
  }
  public void reinitialize(final double time, final double stepSize, final double[] scaledDerivative, final Array2DRowRealMatrix nordsieckVector) {
    this.referenceTime = time;
    this.scalingH = stepSize;
    this.scaled = scaledDerivative;
    this.nordsieck = nordsieckVector;
    setInterpolatedTime(getInterpolatedTime());
  }
  @Override() public void reinitialize(final double[] y, final boolean forward, final EquationsMapper primaryMapper, final EquationsMapper[] secondaryMappers) {
    super.reinitialize(y, forward, primaryMapper, secondaryMappers);
    stateVariation = new double[y.length];
  }
  public void rescale(final double stepSize) {
    final double ratio = stepSize / scalingH;
    for(int i = 0; i < scaled.length; ++i) {
      scaled[i] *= ratio;
    }
    final double[][] nData = nordsieck.getDataRef();
    double power = ratio;
    for(int i = 0; i < nData.length; ++i) {
      power *= ratio;
      final double[] nDataI = nData[i];
      for(int j = 0; j < nDataI.length; ++j) {
        nDataI[j] *= power;
      }
    }
    scalingH = stepSize;
  }
  @Override() public void writeExternal(final ObjectOutput out) throws IOException {
    writeBaseExternal(out);
    out.writeDouble(scalingH);
    out.writeDouble(referenceTime);
    final int n = (currentState == null) ? -1 : currentState.length;
    if(scaled == null) {
      out.writeBoolean(false);
    }
    else {
      out.writeBoolean(true);
      for(int j = 0; j < n; ++j) {
        out.writeDouble(scaled[j]);
      }
    }
    if(nordsieck == null) {
      out.writeBoolean(false);
    }
    else {
      out.writeBoolean(true);
      out.writeObject(nordsieck);
    }
  }
}