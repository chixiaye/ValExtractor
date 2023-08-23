package org.apache.commons.math3.ode;
import java.io.Serializable;
import org.apache.commons.math3.exception.DimensionMismatchException;

public class EquationsMapper implements Serializable  {
  final private static long serialVersionUID = 20110925L;
  final private int firstIndex;
  final private int dimension;
  public EquationsMapper(final int firstIndex, final int dimension) {
    super();
    this.firstIndex = firstIndex;
    this.dimension = dimension;
  }
  public int getDimension() {
    return dimension;
  }
  public int getFirstIndex() {
    return firstIndex;
  }
  public void extractEquationData(double[] complete, double[] equationData) throws DimensionMismatchException {
    if(equationData.length != dimension) {
      throw new DimensionMismatchException(equationData.length, dimension);
    }
    System.arraycopy(complete, firstIndex, equationData, 0, dimension);
  }
  public void insertEquationData(double[] equationData, double[] complete) throws DimensionMismatchException {
    int var_2568 = equationData.length;
    if(var_2568 != dimension) {
      throw new DimensionMismatchException(equationData.length, dimension);
    }
    System.arraycopy(equationData, 0, complete, firstIndex, dimension);
  }
}