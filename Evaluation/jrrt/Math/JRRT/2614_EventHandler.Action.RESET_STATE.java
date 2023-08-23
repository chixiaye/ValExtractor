package org.apache.commons.math3.ode.events;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.AllowedSolution;
import org.apache.commons.math3.analysis.solvers.BracketedUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.PegasusSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.ode.EquationsMapper;
import org.apache.commons.math3.ode.ExpandableStatefulODE;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.apache.commons.math3.util.FastMath;

public class EventState  {
  final private EventHandler handler;
  final private double maxCheckInterval;
  final private double convergence;
  final private int maxIterationCount;
  private ExpandableStatefulODE expandable;
  private double t0;
  private double g0;
  private boolean g0Positive;
  private boolean pendingEvent;
  private double pendingEventTime;
  private double previousEventTime;
  private boolean forward;
  private boolean increasing;
  private EventHandler.Action nextAction;
  final private UnivariateSolver solver;
  public EventState(final EventHandler handler, final double maxCheckInterval, final double convergence, final int maxIterationCount, final UnivariateSolver solver) {
    super();
    this.handler = handler;
    this.maxCheckInterval = maxCheckInterval;
    this.convergence = FastMath.abs(convergence);
    this.maxIterationCount = maxIterationCount;
    this.solver = solver;
    expandable = null;
    t0 = Double.NaN;
    g0 = Double.NaN;
    g0Positive = true;
    pendingEvent = false;
    pendingEventTime = Double.NaN;
    previousEventTime = Double.NaN;
    increasing = true;
    nextAction = EventHandler.Action.CONTINUE;
  }
  public EventHandler getEventHandler() {
    return handler;
  }
  public boolean evaluateStep(final StepInterpolator interpolator) throws MaxCountExceededException, NoBracketingException {
    try {
      forward = interpolator.isForward();
      final double t1 = interpolator.getCurrentTime();
      final double dt = t1 - t0;
      if(FastMath.abs(dt) < convergence) {
        return false;
      }
      final int n = FastMath.max(1, (int)FastMath.ceil(FastMath.abs(dt) / maxCheckInterval));
      final double h = dt / n;
      final UnivariateFunction f = new UnivariateFunction() {
          public double value(final double t) throws LocalMaxCountExceededException {
            try {
              interpolator.setInterpolatedTime(t);
              return handler.g(t, getCompleteState(interpolator));
            }
            catch (MaxCountExceededException mcee) {
              throw new LocalMaxCountExceededException(mcee);
            }
          }
      };
      double ta = t0;
      double ga = g0;
      for(int i = 0; i < n; ++i) {
        final double tb = t0 + (i + 1) * h;
        interpolator.setInterpolatedTime(tb);
        final double gb = handler.g(tb, getCompleteState(interpolator));
        if(g0Positive ^ (gb >= 0)) {
          increasing = gb >= ga;
          final double root;
          if(solver instanceof BracketedUnivariateSolver<?>) {
            @SuppressWarnings(value = {"unchecked", }) BracketedUnivariateSolver<UnivariateFunction> bracketing = (BracketedUnivariateSolver<UnivariateFunction>)solver;
            root = forward ? bracketing.solve(maxIterationCount, f, ta, tb, AllowedSolution.RIGHT_SIDE) : bracketing.solve(maxIterationCount, f, tb, ta, AllowedSolution.LEFT_SIDE);
          }
          else {
            final double baseRoot = forward ? solver.solve(maxIterationCount, f, ta, tb) : solver.solve(maxIterationCount, f, tb, ta);
            final int remainingEval = maxIterationCount - solver.getEvaluations();
            BracketedUnivariateSolver<UnivariateFunction> bracketing = new PegasusSolver(solver.getRelativeAccuracy(), solver.getAbsoluteAccuracy());
            root = forward ? UnivariateSolverUtils.forceSide(remainingEval, f, bracketing, baseRoot, ta, tb, AllowedSolution.RIGHT_SIDE) : UnivariateSolverUtils.forceSide(remainingEval, f, bracketing, baseRoot, tb, ta, AllowedSolution.LEFT_SIDE);
          }
          if((!Double.isNaN(previousEventTime)) && (FastMath.abs(root - ta) <= convergence) && (FastMath.abs(root - previousEventTime) <= convergence)) {
            ta = forward ? ta + convergence : ta - convergence;
            ga = f.value(ta);
            --i;
          }
          else 
            if(Double.isNaN(previousEventTime) || (FastMath.abs(previousEventTime - root) > convergence)) {
              pendingEventTime = root;
              pendingEvent = true;
              return true;
            }
            else {
              ta = tb;
              ga = gb;
            }
        }
        else {
          ta = tb;
          ga = gb;
        }
      }
      pendingEvent = false;
      pendingEventTime = Double.NaN;
      return false;
    }
    catch (LocalMaxCountExceededException lmcee) {
      throw lmcee.getException();
    }
  }
  public boolean reset(final double t, final double[] y) {
    if(!(pendingEvent && (FastMath.abs(pendingEventTime - t) <= convergence))) {
      return false;
    }
    if(nextAction == EventHandler.Action.RESET_STATE) {
      handler.resetState(t, y);
    }
    pendingEvent = false;
    pendingEventTime = Double.NaN;
    EventHandler.Action var_2614 = EventHandler.Action.RESET_STATE;
    return (nextAction == var_2614) || (nextAction == EventHandler.Action.RESET_DERIVATIVES);
  }
  public boolean stop() {
    return nextAction == EventHandler.Action.STOP;
  }
  public double getConvergence() {
    return convergence;
  }
  public double getEventTime() {
    return pendingEvent ? pendingEventTime : (forward ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
  }
  public double getMaxCheckInterval() {
    return maxCheckInterval;
  }
  private double[] getCompleteState(final StepInterpolator interpolator) {
    final double[] complete = new double[expandable.getTotalDimension()];
    expandable.getPrimaryMapper().insertEquationData(interpolator.getInterpolatedState(), complete);
    int index = 0;
    for (EquationsMapper secondary : expandable.getSecondaryMappers()) {
      secondary.insertEquationData(interpolator.getInterpolatedSecondaryState(index++), complete);
    }
    return complete;
  }
  public int getMaxIterationCount() {
    return maxIterationCount;
  }
  public void reinitializeBegin(final StepInterpolator interpolator) throws MaxCountExceededException {
    t0 = interpolator.getPreviousTime();
    interpolator.setInterpolatedTime(t0);
    g0 = handler.g(t0, getCompleteState(interpolator));
    if(g0 == 0) {
      final double epsilon = FastMath.max(solver.getAbsoluteAccuracy(), FastMath.abs(solver.getRelativeAccuracy() * t0));
      final double tStart = t0 + 0.5D * epsilon;
      interpolator.setInterpolatedTime(tStart);
      g0 = handler.g(tStart, getCompleteState(interpolator));
    }
    g0Positive = g0 >= 0;
  }
  public void setExpandable(final ExpandableStatefulODE expandable) {
    this.expandable = expandable;
  }
  public void stepAccepted(final double t, final double[] y) {
    t0 = t;
    g0 = handler.g(t, y);
    if(pendingEvent && (FastMath.abs(pendingEventTime - t) <= convergence)) {
      previousEventTime = t;
      g0Positive = increasing;
      nextAction = handler.eventOccurred(t, y, !(increasing ^ forward));
    }
    else {
      g0Positive = g0 >= 0;
      nextAction = EventHandler.Action.CONTINUE;
    }
  }
  
  private static class LocalMaxCountExceededException extends RuntimeException  {
    final private static long serialVersionUID = 20120901L;
    final private MaxCountExceededException wrapped;
    public LocalMaxCountExceededException(final MaxCountExceededException exception) {
      super();
      wrapped = exception;
    }
    public MaxCountExceededException getException() {
      return wrapped;
    }
  }
}