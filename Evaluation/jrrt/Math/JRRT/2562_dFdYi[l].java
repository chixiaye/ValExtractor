package org.apache.commons.math3.ode;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class JacobianMatrices  {
  private ExpandableStatefulODE efode;
  private int index;
  private MainStateJacobianProvider jode;
  private ParameterizedODE pode;
  private int stateDim;
  private ParameterConfiguration[] selectedParameters;
  private List<ParameterJacobianProvider> jacobianProviders;
  private int paramDim;
  private boolean dirtyParameter;
  private double[] matricesData;
  public JacobianMatrices(final FirstOrderDifferentialEquations fode, final double[] hY, final String ... parameters) throws DimensionMismatchException {
    this(new MainStateJacobianWrapper(fode, hY), parameters);
  }
  public JacobianMatrices(final MainStateJacobianProvider jode, final String ... parameters) {
    super();
    this.efode = null;
    this.index = -1;
    this.jode = jode;
    this.pode = null;
    this.stateDim = jode.getDimension();
    if(parameters == null) {
      selectedParameters = null;
      paramDim = 0;
    }
    else {
      this.selectedParameters = new ParameterConfiguration[parameters.length];
      for(int i = 0; i < parameters.length; ++i) {
        selectedParameters[i] = new ParameterConfiguration(parameters[i], Double.NaN);
      }
      paramDim = parameters.length;
    }
    this.dirtyParameter = false;
    this.jacobianProviders = new ArrayList<ParameterJacobianProvider>();
    matricesData = new double[(stateDim + paramDim) * stateDim];
    for(int i = 0; i < stateDim; ++i) {
      matricesData[i * (stateDim + 1)] = 1.0D;
    }
  }
  public void addParameterJacobianProvider(final ParameterJacobianProvider provider) {
    jacobianProviders.add(provider);
  }
  private void checkDimension(final int expected, final Object array) throws DimensionMismatchException {
    int arrayDimension = (array == null) ? 0 : Array.getLength(array);
    if(arrayDimension != expected) {
      throw new DimensionMismatchException(arrayDimension, expected);
    }
  }
  public void getCurrentMainSetJacobian(final double[][] dYdY0) {
    double[] p = efode.getSecondaryState(index);
    int j = 0;
    for(int i = 0; i < stateDim; i++) {
      System.arraycopy(p, j, dYdY0[i], 0, stateDim);
      j += stateDim;
    }
  }
  public void getCurrentParameterJacobian(String pName, final double[] dYdP) {
    double[] p = efode.getSecondaryState(index);
    int i = stateDim * stateDim;
    for (ParameterConfiguration param : selectedParameters) {
      if(param.getParameterName().equals(pName)) {
        System.arraycopy(p, i, dYdP, 0, stateDim);
        return ;
      }
      i += stateDim;
    }
  }
  public void registerVariationalEquations(final ExpandableStatefulODE expandable) throws DimensionMismatchException, MismatchedEquations {
    final FirstOrderDifferentialEquations ode = (jode instanceof MainStateJacobianWrapper) ? ((MainStateJacobianWrapper)jode).ode : jode;
    if(expandable.getPrimary() != ode) {
      throw new MismatchedEquations();
    }
    efode = expandable;
    index = efode.addSecondaryEquations(new JacobiansSecondaryEquations());
    efode.setSecondaryState(index, matricesData);
  }
  public void setInitialMainStateJacobian(final double[][] dYdY0) throws DimensionMismatchException {
    checkDimension(stateDim, dYdY0);
    checkDimension(stateDim, dYdY0[0]);
    int i = 0;
    for (final double[] row : dYdY0) {
      System.arraycopy(row, 0, matricesData, i, stateDim);
      i += stateDim;
    }
    if(efode != null) {
      efode.setSecondaryState(index, matricesData);
    }
  }
  public void setInitialParameterJacobian(final String pName, final double[] dYdP) throws UnknownParameterException, DimensionMismatchException {
    checkDimension(stateDim, dYdP);
    int i = stateDim * stateDim;
    for (ParameterConfiguration param : selectedParameters) {
      if(pName.equals(param.getParameterName())) {
        System.arraycopy(dYdP, 0, matricesData, i, stateDim);
        if(efode != null) {
          efode.setSecondaryState(index, matricesData);
        }
        return ;
      }
      i += stateDim;
    }
    throw new UnknownParameterException(pName);
  }
  public void setParameterStep(final String parameter, final double hP) throws UnknownParameterException {
    for (ParameterConfiguration param : selectedParameters) {
      if(parameter.equals(param.getParameterName())) {
        param.setHP(hP);
        dirtyParameter = true;
        return ;
      }
    }
    throw new UnknownParameterException(parameter);
  }
  public void setParameterizedODE(final ParameterizedODE parameterizedOde) {
    this.pode = parameterizedOde;
    dirtyParameter = true;
  }
  
  private class JacobiansSecondaryEquations implements SecondaryEquations  {
    public int getDimension() {
      return stateDim * (stateDim + paramDim);
    }
    public void computeDerivatives(final double t, final double[] y, final double[] yDot, final double[] z, final double[] zDot) throws MaxCountExceededException, DimensionMismatchException {
      if(dirtyParameter && (paramDim != 0)) {
        jacobianProviders.add(new ParameterJacobianWrapper(jode, pode, selectedParameters));
        dirtyParameter = false;
      }
      double[][] dFdY = new double[stateDim][stateDim];
      jode.computeMainStateJacobian(t, y, yDot, dFdY);
      for(int i = 0; i < stateDim; ++i) {
        final double[] dFdYi = dFdY[i];
        for(int j = 0; j < stateDim; ++j) {
          double s = 0;
          final int startIndex = j;
          int zIndex = startIndex;
          for(int l = 0; l < stateDim; ++l) {
            double var_2562 = dFdYi[l];
            s += var_2562 * z[zIndex];
            zIndex += stateDim;
          }
          zDot[startIndex + i * stateDim] = s;
        }
      }
      if(paramDim != 0) {
        double[] dFdP = new double[stateDim];
        int startIndex = stateDim * stateDim;
        for (ParameterConfiguration param : selectedParameters) {
          boolean found = false;
          for(int k = 0; (!found) && (k < jacobianProviders.size()); ++k) {
            final ParameterJacobianProvider provider = jacobianProviders.get(k);
            if(provider.isSupported(param.getParameterName())) {
              provider.computeParameterJacobian(t, y, yDot, param.getParameterName(), dFdP);
              for(int i = 0; i < stateDim; ++i) {
                final double[] dFdYi = dFdY[i];
                int zIndex = startIndex;
                double s = dFdP[i];
                for(int l = 0; l < stateDim; ++l) {
                  s += dFdYi[l] * z[zIndex];
                  zIndex++;
                }
                zDot[startIndex + i] = s;
              }
              found = true;
            }
          }
          if(!found) {
            Arrays.fill(zDot, startIndex, startIndex + stateDim, 0.0D);
          }
          startIndex += stateDim;
        }
      }
    }
  }
  
  private static class MainStateJacobianWrapper implements MainStateJacobianProvider  {
    final private FirstOrderDifferentialEquations ode;
    final private double[] hY;
    public MainStateJacobianWrapper(final FirstOrderDifferentialEquations ode, final double[] hY) throws DimensionMismatchException {
      super();
      this.ode = ode;
      this.hY = hY.clone();
      if(hY.length != ode.getDimension()) {
        throw new DimensionMismatchException(ode.getDimension(), hY.length);
      }
    }
    public int getDimension() {
      return ode.getDimension();
    }
    public void computeDerivatives(double t, double[] y, double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
      ode.computeDerivatives(t, y, yDot);
    }
    public void computeMainStateJacobian(double t, double[] y, double[] yDot, double[][] dFdY) throws MaxCountExceededException, DimensionMismatchException {
      final int n = ode.getDimension();
      final double[] tmpDot = new double[n];
      for(int j = 0; j < n; ++j) {
        final double savedYj = y[j];
        y[j] += hY[j];
        ode.computeDerivatives(t, y, tmpDot);
        for(int i = 0; i < n; ++i) {
          dFdY[i][j] = (tmpDot[i] - yDot[i]) / hY[j];
        }
        y[j] = savedYj;
      }
    }
  }
  
  public static class MismatchedEquations extends MathIllegalArgumentException  {
    final private static long serialVersionUID = 20120902L;
    public MismatchedEquations() {
      super(LocalizedFormats.UNMATCHED_ODE_IN_EXPANDED_SET);
    }
  }
}