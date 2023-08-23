package org.apache.commons.math3.ode.events;
import org.apache.commons.math3.exception.MathInternalError;
public enum FilterType {
  TRIGGER_ONLY_DECREASING_EVENTS() {
      @Override() protected boolean getTriggeredIncreasing() {
        return false;
      }
      @Override() protected Transformer selectTransformer(final Transformer previous, final double g, final boolean forward) {
        if(forward) {
          switch (previous){
            case UNINITIALIZED:
            if(g > 0) {
              return Transformer.MAX;
            }
            else 
              if(g < 0) {
                return Transformer.PLUS;
              }
              else {
                return Transformer.UNINITIALIZED;
              }
            case PLUS:
            if(g >= 0) {
              return Transformer.MIN;
            }
            else {
              return previous;
            }
            case MINUS:
            if(g >= 0) {
              return Transformer.MAX;
            }
            else {
              return previous;
            }
            case MIN:
            if(g <= 0) {
              return Transformer.MINUS;
            }
            else {
              return previous;
            }
            case MAX:
            if(g <= 0) {
              return Transformer.PLUS;
            }
            else {
              return previous;
            }
            default:
            throw new MathInternalError();
          }
        }
        else {
          switch (previous){
            case UNINITIALIZED:
            if(g > 0) {
              return Transformer.MINUS;
            }
            else 
              if(g < 0) {
                return Transformer.MIN;
              }
              else {
                return Transformer.UNINITIALIZED;
              }
            case PLUS:
            if(g <= 0) {
              return Transformer.MAX;
            }
            else {
              return previous;
            }
            case MINUS:
            if(g <= 0) {
              return Transformer.MIN;
            }
            else {
              return previous;
            }
            case MIN:
            if(g >= 0) {
              return Transformer.PLUS;
            }
            else {
              return previous;
            }
            case MAX:
            if(g >= 0) {
              return Transformer.MINUS;
            }
            else {
              return previous;
            }
            default:
            throw new MathInternalError();
          }
        }
      }
  },

  TRIGGER_ONLY_INCREASING_EVENTS() {
      @Override() protected boolean getTriggeredIncreasing() {
        return true;
      }
      @Override() protected Transformer selectTransformer(final Transformer previous, final double g, final boolean forward) {
        if(forward) {
          switch (previous){
            case UNINITIALIZED:
            if(g > 0) {
              return Transformer.PLUS;
            }
            else 
              if(g < 0) {
                return Transformer.MIN;
              }
              else {
                return Transformer.UNINITIALIZED;
              }
            case PLUS:
            if(g <= 0) {
              return Transformer.MAX;
            }
            else {
              return previous;
            }
            case MINUS:
            if(g <= 0) {
              return Transformer.MIN;
            }
            else {
              return previous;
            }
            case MIN:
            if(g >= 0) {
              return Transformer.PLUS;
            }
            else {
              return previous;
            }
            case MAX:
            if(g >= 0) {
              return Transformer.MINUS;
            }
            else {
              return previous;
            }
            default:
            throw new MathInternalError();
          }
        }
        else {
          switch (previous){
            case UNINITIALIZED:
            if(g > 0) {
              return Transformer.MAX;
            }
            else 
              if(g < 0) {
                Transformer var_2623 = Transformer.MINUS;
                return var_2623;
              }
              else {
                return Transformer.UNINITIALIZED;
              }
            case PLUS:
            if(g >= 0) {
              return Transformer.MIN;
            }
            else {
              return previous;
            }
            case MINUS:
            if(g >= 0) {
              return Transformer.MAX;
            }
            else {
              return previous;
            }
            case MIN:
            if(g <= 0) {
              return Transformer.MINUS;
            }
            else {
              return previous;
            }
            case MAX:
            if(g <= 0) {
              return Transformer.PLUS;
            }
            else {
              return previous;
            }
            default:
            throw new MathInternalError();
          }
        }
      }
  },

;
  abstract protected boolean getTriggeredIncreasing();
  abstract protected Transformer selectTransformer(Transformer previous, double g, boolean forward);
private FilterType() {
}
}