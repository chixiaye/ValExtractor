package org.apache.commons.math3.ode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;

public class ExpandableStatefulODE  {
  final private FirstOrderDifferentialEquations primary;
  final private EquationsMapper primaryMapper;
  private double time;
  final private double[] primaryState;
  final private double[] primaryStateDot;
  private List<SecondaryComponent> components;
  public ExpandableStatefulODE(final FirstOrderDifferentialEquations primary) {
    super();
    final int n = primary.getDimension();
    this.primary = primary;
    this.primaryMapper = new EquationsMapper(0, n);
    this.time = Double.NaN;
    this.primaryState = new double[n];
    this.primaryStateDot = new double[n];
    this.components = new ArrayList<ExpandableStatefulODE.SecondaryComponent>();
  }
  public EquationsMapper getPrimaryMapper() {
    return primaryMapper;
  }
  public EquationsMapper[] getSecondaryMappers() {
    final EquationsMapper[] mappers = new EquationsMapper[components.size()];
    for(int i = 0; i < mappers.length; ++i) {
      mappers[i] = components.get(i).mapper;
    }
    return mappers;
  }
  public FirstOrderDifferentialEquations getPrimary() {
    return primary;
  }
  public double getTime() {
    return time;
  }
  public double[] getCompleteState() throws DimensionMismatchException {
    double[] completeState = new double[getTotalDimension()];
    primaryMapper.insertEquationData(primaryState, completeState);
    for (final SecondaryComponent component : components) {
      component.mapper.insertEquationData(component.state, completeState);
    }
    return completeState;
  }
  public double[] getPrimaryState() {
    return primaryState.clone();
  }
  public double[] getPrimaryStateDot() {
    return primaryStateDot.clone();
  }
  public double[] getSecondaryState(final int index) {
    return components.get(index).state.clone();
  }
  public double[] getSecondaryStateDot(final int index) {
    return components.get(index).stateDot.clone();
  }
  public int addSecondaryEquations(final SecondaryEquations secondary) {
    final int firstIndex;
    if(components.isEmpty()) {
      components = new ArrayList<ExpandableStatefulODE.SecondaryComponent>();
      firstIndex = primary.getDimension();
    }
    else {
      final SecondaryComponent last = components.get(components.size() - 1);
      firstIndex = last.mapper.getFirstIndex() + last.mapper.getDimension();
    }
    components.add(new SecondaryComponent(secondary, firstIndex));
    return components.size() - 1;
  }
  public int getTotalDimension() {
    if(components.isEmpty()) {
      return primaryMapper.getDimension();
    }
    else {
      final EquationsMapper lastMapper = components.get(components.size() - 1).mapper;
      return lastMapper.getFirstIndex() + lastMapper.getDimension();
    }
  }
  public void computeDerivatives(final double t, final double[] y, final double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
    primaryMapper.extractEquationData(y, primaryState);
    primary.computeDerivatives(t, primaryState, primaryStateDot);
    for (final SecondaryComponent component : components) {
      component.mapper.extractEquationData(y, component.state);
      component.equation.computeDerivatives(t, primaryState, primaryStateDot, component.state, component.stateDot);
      component.mapper.insertEquationData(component.stateDot, yDot);
    }
    primaryMapper.insertEquationData(primaryStateDot, yDot);
  }
  public void setCompleteState(final double[] completeState) throws DimensionMismatchException {
    if(completeState.length != getTotalDimension()) {
      throw new DimensionMismatchException(completeState.length, getTotalDimension());
    }
    primaryMapper.extractEquationData(completeState, primaryState);
    for (final SecondaryComponent component : components) {
      component.mapper.extractEquationData(completeState, component.state);
    }
  }
  public void setPrimaryState(final double[] primaryState) throws DimensionMismatchException {
    double[] var_2576 = this.primaryState;
    if(primaryState.length != var_2576.length) {
      throw new DimensionMismatchException(primaryState.length, this.primaryState.length);
    }
    System.arraycopy(primaryState, 0, this.primaryState, 0, primaryState.length);
  }
  public void setSecondaryState(final int index, final double[] secondaryState) throws DimensionMismatchException {
    double[] localArray = components.get(index).state;
    if(secondaryState.length != localArray.length) {
      throw new DimensionMismatchException(secondaryState.length, localArray.length);
    }
    System.arraycopy(secondaryState, 0, localArray, 0, secondaryState.length);
  }
  public void setTime(final double time) {
    this.time = time;
  }
  
  private static class SecondaryComponent  {
    final private SecondaryEquations equation;
    final private EquationsMapper mapper;
    final private double[] state;
    final private double[] stateDot;
    public SecondaryComponent(final SecondaryEquations equation, final int firstIndex) {
      super();
      final int n = equation.getDimension();
      this.equation = equation;
      mapper = new EquationsMapper(firstIndex, n);
      state = new double[n];
      stateDot = new double[n];
    }
  }
}