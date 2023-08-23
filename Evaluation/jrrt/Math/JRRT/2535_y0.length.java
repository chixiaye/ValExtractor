package org.apache.commons.math3.ode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.events.EventState;
import org.apache.commons.math3.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.util.Precision;

abstract public class AbstractIntegrator implements FirstOrderIntegrator  {
  protected Collection<StepHandler> stepHandlers;
  protected double stepStart;
  protected double stepSize;
  protected boolean isLastStep;
  protected boolean resetOccurred;
  private Collection<EventState> eventsStates;
  private boolean statesInitialized;
  final private String name;
  private Incrementor evaluations;
  private transient ExpandableStatefulODE expandable;
  protected AbstractIntegrator() {
    this(null);
  }
  public AbstractIntegrator(final String name) {
    super();
    this.name = name;
    stepHandlers = new ArrayList<StepHandler>();
    stepStart = Double.NaN;
    stepSize = Double.NaN;
    eventsStates = new ArrayList<EventState>();
    statesInitialized = false;
    evaluations = new Incrementor();
    setMaxEvaluations(-1);
    evaluations.resetCount();
  }
  public Collection<EventHandler> getEventHandlers() {
    final List<EventHandler> list = new ArrayList<EventHandler>(eventsStates.size());
    for (EventState state : eventsStates) {
      list.add(state.getEventHandler());
    }
    return Collections.unmodifiableCollection(list);
  }
  public Collection<StepHandler> getStepHandlers() {
    return Collections.unmodifiableCollection(stepHandlers);
  }
  protected ExpandableStatefulODE getExpandable() {
    return expandable;
  }
  protected Incrementor getEvaluationsCounter() {
    return evaluations;
  }
  public String getName() {
    return name;
  }
  protected double acceptStep(final AbstractStepInterpolator interpolator, final double[] y, final double[] yDot, final double tEnd) throws MaxCountExceededException, DimensionMismatchException, NoBracketingException {
    double previousT = interpolator.getGlobalPreviousTime();
    final double currentT = interpolator.getGlobalCurrentTime();
    if(!statesInitialized) {
      for (EventState state : eventsStates) {
        state.reinitializeBegin(interpolator);
      }
      statesInitialized = true;
    }
    final int orderingSign = interpolator.isForward() ? +1 : -1;
    SortedSet<EventState> occurringEvents = new TreeSet<EventState>(new Comparator<EventState>() {
        public int compare(EventState es0, EventState es1) {
          return orderingSign * Double.compare(es0.getEventTime(), es1.getEventTime());
        }
    });
    for (final EventState state : eventsStates) {
      if(state.evaluateStep(interpolator)) {
        occurringEvents.add(state);
      }
    }
    while(!occurringEvents.isEmpty()){
      final Iterator<EventState> iterator = occurringEvents.iterator();
      final EventState currentEvent = iterator.next();
      iterator.remove();
      final double eventT = currentEvent.getEventTime();
      interpolator.setSoftPreviousTime(previousT);
      interpolator.setSoftCurrentTime(eventT);
      interpolator.setInterpolatedTime(eventT);
      final double[] eventYComplete = new double[y.length];
      expandable.getPrimaryMapper().insertEquationData(interpolator.getInterpolatedState(), eventYComplete);
      int index = 0;
      for (EquationsMapper secondary : expandable.getSecondaryMappers()) {
        secondary.insertEquationData(interpolator.getInterpolatedSecondaryState(index++), eventYComplete);
      }
      for (final EventState state : eventsStates) {
        state.stepAccepted(eventT, eventYComplete);
        isLastStep = isLastStep || state.stop();
      }
      for (final StepHandler handler : stepHandlers) {
        handler.handleStep(interpolator, isLastStep);
      }
      if(isLastStep) {
        System.arraycopy(eventYComplete, 0, y, 0, y.length);
        return eventT;
      }
      boolean needReset = false;
      for (final EventState state : eventsStates) {
        needReset = needReset || state.reset(eventT, eventYComplete);
      }
      if(needReset) {
        interpolator.setInterpolatedTime(eventT);
        System.arraycopy(eventYComplete, 0, y, 0, y.length);
        computeDerivatives(eventT, y, yDot);
        resetOccurred = true;
        return eventT;
      }
      previousT = eventT;
      interpolator.setSoftPreviousTime(eventT);
      interpolator.setSoftCurrentTime(currentT);
      if(currentEvent.evaluateStep(interpolator)) {
        occurringEvents.add(currentEvent);
      }
    }
    interpolator.setInterpolatedTime(currentT);
    final double[] currentY = new double[y.length];
    expandable.getPrimaryMapper().insertEquationData(interpolator.getInterpolatedState(), currentY);
    int index = 0;
    for (EquationsMapper secondary : expandable.getSecondaryMappers()) {
      secondary.insertEquationData(interpolator.getInterpolatedSecondaryState(index++), currentY);
    }
    for (final EventState state : eventsStates) {
      state.stepAccepted(currentT, currentY);
      isLastStep = isLastStep || state.stop();
    }
    isLastStep = isLastStep || Precision.equals(currentT, tEnd, 1);
    for (StepHandler handler : stepHandlers) {
      handler.handleStep(interpolator, isLastStep);
    }
    return currentT;
  }
  public double getCurrentSignedStepsize() {
    return stepSize;
  }
  public double getCurrentStepStart() {
    return stepStart;
  }
  public double integrate(final FirstOrderDifferentialEquations equations, final double t0, final double[] y0, final double t, final double[] y) throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
    if(y0.length != equations.getDimension()) {
      int var_2535 = y0.length;
      throw new DimensionMismatchException(var_2535, equations.getDimension());
    }
    if(y.length != equations.getDimension()) {
      throw new DimensionMismatchException(y.length, equations.getDimension());
    }
    final ExpandableStatefulODE expandableODE = new ExpandableStatefulODE(equations);
    expandableODE.setTime(t0);
    expandableODE.setPrimaryState(y0);
    integrate(expandableODE, t);
    System.arraycopy(expandableODE.getPrimaryState(), 0, y, 0, y.length);
    return expandableODE.getTime();
  }
  public int getEvaluations() {
    return evaluations.getCount();
  }
  public int getMaxEvaluations() {
    return evaluations.getMaximalCount();
  }
  public void addEventHandler(final EventHandler handler, final double maxCheckInterval, final double convergence, final int maxIterationCount) {
    addEventHandler(handler, maxCheckInterval, convergence, maxIterationCount, new BracketingNthOrderBrentSolver(convergence, 5));
  }
  public void addEventHandler(final EventHandler handler, final double maxCheckInterval, final double convergence, final int maxIterationCount, final UnivariateSolver solver) {
    eventsStates.add(new EventState(handler, maxCheckInterval, convergence, maxIterationCount, solver));
  }
  public void addStepHandler(final StepHandler handler) {
    stepHandlers.add(handler);
  }
  public void clearEventHandlers() {
    eventsStates.clear();
  }
  public void clearStepHandlers() {
    stepHandlers.clear();
  }
  public void computeDerivatives(final double t, final double[] y, final double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
    evaluations.incrementCount();
    expandable.computeDerivatives(t, y, yDot);
  }
  protected void initIntegration(final double t0, final double[] y0, final double t) {
    evaluations.resetCount();
    for (final EventState state : eventsStates) {
      state.setExpandable(expandable);
      state.getEventHandler().init(t0, y0, t);
    }
    for (StepHandler handler : stepHandlers) {
      handler.init(t0, y0, t);
    }
    setStateInitialized(false);
  }
  abstract public void integrate(ExpandableStatefulODE equations, double t) throws NumberIsTooSmallException, DimensionMismatchException, MaxCountExceededException, NoBracketingException;
  protected void sanityChecks(final ExpandableStatefulODE equations, final double t) throws NumberIsTooSmallException, DimensionMismatchException {
    final double threshold = 1000 * FastMath.ulp(FastMath.max(FastMath.abs(equations.getTime()), FastMath.abs(t)));
    final double dt = FastMath.abs(equations.getTime() - t);
    if(dt <= threshold) {
      throw new NumberIsTooSmallException(LocalizedFormats.TOO_SMALL_INTEGRATION_INTERVAL, dt, threshold, false);
    }
  }
  protected void setEquations(final ExpandableStatefulODE equations) {
    this.expandable = equations;
  }
  public void setMaxEvaluations(int maxEvaluations) {
    evaluations.setMaximalCount((maxEvaluations < 0) ? Integer.MAX_VALUE : maxEvaluations);
  }
  protected void setStateInitialized(final boolean stateInitialized) {
    this.statesInitialized = stateInitialized;
  }
}