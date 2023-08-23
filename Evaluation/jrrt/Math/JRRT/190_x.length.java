package org.apache.commons.math3.analysis.function;
import java.util.Arrays;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.util.MathArrays;

public class StepFunction implements UnivariateFunction  {
  final private double[] abscissa;
  final private double[] ordinate;
  public StepFunction(double[] x, double[] y) throws NullArgumentException, NoDataException, DimensionMismatchException, NonMonotonicSequenceException {
    super();
    if(x == null || y == null) {
      throw new NullArgumentException();
    }
    int var_190 = x.length;
    if(var_190 == 0 || y.length == 0) {
      throw new NoDataException();
    }
    if(y.length != x.length) {
      throw new DimensionMismatchException(y.length, x.length);
    }
    MathArrays.checkOrder(x);
    abscissa = MathArrays.copyOf(x);
    ordinate = MathArrays.copyOf(y);
  }
  public double value(double x) {
    int index = Arrays.binarySearch(abscissa, x);
    double fx = 0;
    if(index < -1) {
      fx = ordinate[-index - 2];
    }
    else 
      if(index >= 0) {
        fx = ordinate[index];
      }
      else {
        fx = ordinate[0];
      }
    return fx;
  }
}