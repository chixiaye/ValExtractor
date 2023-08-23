package org.apache.commons.math3.ode.events;
import java.util.Arrays;

public class EventFilter implements EventHandler  {
  final private static int HISTORY_SIZE = 100;
  final private EventHandler rawHandler;
  final private FilterType filter;
  final private Transformer[] transformers;
  final private double[] updates;
  private boolean forward;
  private double extremeT;
  public EventFilter(final EventHandler rawHandler, final FilterType filter) {
    super();
    this.rawHandler = rawHandler;
    this.filter = filter;
    this.transformers = new Transformer[HISTORY_SIZE];
    this.updates = new double[HISTORY_SIZE];
  }
  public Action eventOccurred(double t, double[] y, boolean increasing) {
    return rawHandler.eventOccurred(t, y, filter.getTriggeredIncreasing());
  }
  public double g(double t, double[] y) {
    final double rawG = rawHandler.g(t, y);
    if(forward) {
      final int last = transformers.length - 1;
      if(extremeT < t) {
        Transformer var_2598 = transformers[last];
        final Transformer previous = var_2598;
        final Transformer next = filter.selectTransformer(previous, rawG, forward);
        if(next != previous) {
          System.arraycopy(updates, 1, updates, 0, last);
          System.arraycopy(transformers, 1, transformers, 0, last);
          updates[last] = extremeT;
          transformers[last] = next;
        }
        extremeT = t;
        return next.transformed(rawG);
      }
      else {
        for(int i = last; i > 0; --i) {
          if(updates[i] <= t) {
            return transformers[i].transformed(rawG);
          }
        }
        return transformers[0].transformed(rawG);
      }
    }
    else {
      if(t < extremeT) {
        final Transformer previous = transformers[0];
        final Transformer next = filter.selectTransformer(previous, rawG, forward);
        if(next != previous) {
          System.arraycopy(updates, 0, updates, 1, updates.length - 1);
          System.arraycopy(transformers, 0, transformers, 1, transformers.length - 1);
          updates[0] = extremeT;
          transformers[0] = next;
        }
        extremeT = t;
        return next.transformed(rawG);
      }
      else {
        for(int i = 0; i < updates.length - 1; ++i) {
          if(t <= updates[i]) {
            return transformers[i].transformed(rawG);
          }
        }
        return transformers[updates.length - 1].transformed(rawG);
      }
    }
  }
  public void init(double t0, double[] y0, double t) {
    rawHandler.init(t0, y0, t);
    forward = t >= t0;
    extremeT = forward ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    Arrays.fill(transformers, Transformer.UNINITIALIZED);
    Arrays.fill(updates, extremeT);
  }
  public void resetState(double t, double[] y) {
    rawHandler.resetState(t, y);
  }
}