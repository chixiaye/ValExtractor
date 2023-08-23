package org.apache.commons.math3.optim.linear;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

class SimplexTableau implements Serializable  {
  final private static String NEGATIVE_VAR_COLUMN_LABEL = "x-";
  final private static long serialVersionUID = -1369660067587938365L;
  final private LinearObjectiveFunction f;
  final private List<LinearConstraint> constraints;
  final private boolean restrictToNonNegative;
  final private List<String> columnLabels = new ArrayList<String>();
  private transient RealMatrix tableau;
  final private int numDecisionVariables;
  final private int numSlackVariables;
  private int numArtificialVariables;
  final private double epsilon;
  final private int maxUlps;
  final private double cutOff;
  SimplexTableau(final LinearObjectiveFunction f, final Collection<LinearConstraint> constraints, final GoalType goalType, final boolean restrictToNonNegative, final double epsilon) {
    this(f, constraints, goalType, restrictToNonNegative, epsilon, SimplexSolver.DEFAULT_ULPS, SimplexSolver.DEFAULT_CUT_OFF);
  }
  SimplexTableau(final LinearObjectiveFunction f, final Collection<LinearConstraint> constraints, final GoalType goalType, final boolean restrictToNonNegative, final double epsilon, final int maxUlps) {
    this(f, constraints, goalType, restrictToNonNegative, epsilon, maxUlps, SimplexSolver.DEFAULT_CUT_OFF);
  }
  SimplexTableau(final LinearObjectiveFunction f, final Collection<LinearConstraint> constraints, final GoalType goalType, final boolean restrictToNonNegative, final double epsilon, final int maxUlps, final double cutOff) {
    super();
    this.f = f;
    this.constraints = normalizeConstraints(constraints);
    this.restrictToNonNegative = restrictToNonNegative;
    this.epsilon = epsilon;
    this.maxUlps = maxUlps;
    this.cutOff = cutOff;
    this.numDecisionVariables = f.getCoefficients().getDimension() + (restrictToNonNegative ? 0 : 1);
    this.numSlackVariables = getConstraintTypeCounts(Relationship.LEQ) + getConstraintTypeCounts(Relationship.GEQ);
    this.numArtificialVariables = getConstraintTypeCounts(Relationship.EQ) + getConstraintTypeCounts(Relationship.GEQ);
    this.tableau = createTableau(goalType == GoalType.MAXIMIZE);
    initializeColumnLabels();
  }
  protected Integer getBasicRow(final int col) {
    Integer row = null;
    for(int i = 0; i < getHeight(); i++) {
      final double entry = getEntry(i, col);
      if(Precision.equals(entry, 1D, maxUlps) && (row == null)) {
        row = i;
      }
      else 
        if(!Precision.equals(entry, 0D, maxUlps)) {
          return null;
        }
    }
    return row;
  }
  private LinearConstraint normalize(final LinearConstraint constraint) {
    if(constraint.getValue() < 0) {
      return new LinearConstraint(constraint.getCoefficients().mapMultiply(-1), constraint.getRelationship().oppositeRelationship(), -1 * constraint.getValue());
    }
    return new LinearConstraint(constraint.getCoefficients(), constraint.getRelationship(), constraint.getValue());
  }
  public List<LinearConstraint> normalizeConstraints(Collection<LinearConstraint> originalConstraints) {
    List<LinearConstraint> normalized = new ArrayList<LinearConstraint>(originalConstraints.size());
    for (LinearConstraint constraint : originalConstraints) {
      normalized.add(normalize(constraint));
    }
    return normalized;
  }
  protected PointValuePair getSolution() {
    int negativeVarColumn = columnLabels.indexOf(NEGATIVE_VAR_COLUMN_LABEL);
    Integer negativeVarBasicRow = negativeVarColumn > 0 ? getBasicRow(negativeVarColumn) : null;
    double mostNegative = negativeVarBasicRow == null ? 0 : getEntry(negativeVarBasicRow, getRhsOffset());
    Set<Integer> basicRows = new HashSet<Integer>();
    double[] coefficients = new double[getOriginalNumDecisionVariables()];
    for(int i = 0; i < coefficients.length; i++) {
      int colIndex = columnLabels.indexOf("x" + i);
      if(colIndex < 0) {
        coefficients[i] = 0;
        continue ;
      }
      Integer basicRow = getBasicRow(colIndex);
      if(basicRow != null && basicRow == 0) {
        coefficients[i] = 0;
      }
      else 
        if(basicRows.contains(basicRow)) {
          coefficients[i] = 0 - (restrictToNonNegative ? 0 : mostNegative);
        }
        else {
          basicRows.add(basicRow);
          coefficients[i] = (basicRow == null ? 0 : getEntry(basicRow, getRhsOffset())) - (restrictToNonNegative ? 0 : mostNegative);
        }
    }
    return new PointValuePair(coefficients, f.value(coefficients));
  }
  protected RealMatrix createTableau(final boolean maximize) {
    int width = numDecisionVariables + numSlackVariables + numArtificialVariables + getNumObjectiveFunctions() + 1;
    int height = constraints.size() + getNumObjectiveFunctions();
    Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(height, width);
    if(getNumObjectiveFunctions() == 2) {
      matrix.setEntry(0, 0, -1);
    }
    int zIndex = (getNumObjectiveFunctions() == 1) ? 0 : 1;
    matrix.setEntry(zIndex, zIndex, maximize ? 1 : -1);
    RealVector objectiveCoefficients = maximize ? f.getCoefficients().mapMultiply(-1) : f.getCoefficients();
    copyArray(objectiveCoefficients.toArray(), matrix.getDataRef()[zIndex]);
    matrix.setEntry(zIndex, width - 1, maximize ? f.getConstantTerm() : -1 * f.getConstantTerm());
    if(!restrictToNonNegative) {
      matrix.setEntry(zIndex, getSlackVariableOffset() - 1, getInvertedCoefficientSum(objectiveCoefficients));
    }
    int slackVar = 0;
    int artificialVar = 0;
    for(int i = 0; i < constraints.size(); i++) {
      LinearConstraint constraint = constraints.get(i);
      int row = getNumObjectiveFunctions() + i;
      RealVector var_2935 = constraint.getCoefficients();
      copyArray(var_2935.toArray(), matrix.getDataRef()[row]);
      if(!restrictToNonNegative) {
        matrix.setEntry(row, getSlackVariableOffset() - 1, getInvertedCoefficientSum(constraint.getCoefficients()));
      }
      matrix.setEntry(row, width - 1, constraint.getValue());
      if(constraint.getRelationship() == Relationship.LEQ) {
        matrix.setEntry(row, getSlackVariableOffset() + slackVar++, 1);
      }
      else 
        if(constraint.getRelationship() == Relationship.GEQ) {
          matrix.setEntry(row, getSlackVariableOffset() + slackVar++, -1);
        }
      if((constraint.getRelationship() == Relationship.EQ) || (constraint.getRelationship() == Relationship.GEQ)) {
        matrix.setEntry(0, getArtificialVariableOffset() + artificialVar, 1);
        matrix.setEntry(row, getArtificialVariableOffset() + artificialVar++, 1);
        matrix.setRowVector(0, matrix.getRowVector(0).subtract(matrix.getRowVector(row)));
      }
    }
    return matrix;
  }
  @Override() public boolean equals(Object other) {
    if(this == other) {
      return true;
    }
    if(other instanceof SimplexTableau) {
      SimplexTableau rhs = (SimplexTableau)other;
      return (restrictToNonNegative == rhs.restrictToNonNegative) && (numDecisionVariables == rhs.numDecisionVariables) && (numSlackVariables == rhs.numSlackVariables) && (numArtificialVariables == rhs.numArtificialVariables) && (epsilon == rhs.epsilon) && (maxUlps == rhs.maxUlps) && f.equals(rhs.f) && constraints.equals(rhs.constraints) && tableau.equals(rhs.tableau);
    }
    return false;
  }
  boolean isOptimal() {
    for(int i = getNumObjectiveFunctions(); i < getWidth() - 1; i++) {
      final double entry = tableau.getEntry(0, i);
      if(Precision.compareTo(entry, 0D, epsilon) < 0) {
        return false;
      }
    }
    return true;
  }
  final protected double getEntry(final int row, final int column) {
    return tableau.getEntry(row, column);
  }
  protected static double getInvertedCoefficientSum(final RealVector coefficients) {
    double sum = 0;
    for (double coefficient : coefficients.toArray()) {
      sum -= coefficient;
    }
    return sum;
  }
  final protected double[][] getData() {
    return tableau.getData();
  }
  final protected int getArtificialVariableOffset() {
    return getNumObjectiveFunctions() + numDecisionVariables + numSlackVariables;
  }
  private int getConstraintTypeCounts(final Relationship relationship) {
    int count = 0;
    for (final LinearConstraint constraint : constraints) {
      if(constraint.getRelationship() == relationship) {
        ++count;
      }
    }
    return count;
  }
  final protected int getHeight() {
    return tableau.getRowDimension();
  }
  final protected int getNumArtificialVariables() {
    return numArtificialVariables;
  }
  final protected int getNumDecisionVariables() {
    return numDecisionVariables;
  }
  final protected int getNumObjectiveFunctions() {
    return this.numArtificialVariables > 0 ? 2 : 1;
  }
  final protected int getNumSlackVariables() {
    return numSlackVariables;
  }
  final protected int getOriginalNumDecisionVariables() {
    return f.getCoefficients().getDimension();
  }
  final protected int getRhsOffset() {
    return getWidth() - 1;
  }
  final protected int getSlackVariableOffset() {
    return getNumObjectiveFunctions() + numDecisionVariables;
  }
  final protected int getWidth() {
    return tableau.getColumnDimension();
  }
  @Override() public int hashCode() {
    return Boolean.valueOf(restrictToNonNegative).hashCode() ^ numDecisionVariables ^ numSlackVariables ^ numArtificialVariables ^ Double.valueOf(epsilon).hashCode() ^ maxUlps ^ f.hashCode() ^ constraints.hashCode() ^ tableau.hashCode();
  }
  private void copyArray(final double[] src, final double[] dest) {
    System.arraycopy(src, 0, dest, getNumObjectiveFunctions(), src.length);
  }
  protected void divideRow(final int dividendRow, final double divisor) {
    for(int j = 0; j < getWidth(); j++) {
      tableau.setEntry(dividendRow, j, tableau.getEntry(dividendRow, j) / divisor);
    }
  }
  protected void dropPhase1Objective() {
    if(getNumObjectiveFunctions() == 1) {
      return ;
    }
    Set<Integer> columnsToDrop = new TreeSet<Integer>();
    columnsToDrop.add(0);
    for(int i = getNumObjectiveFunctions(); i < getArtificialVariableOffset(); i++) {
      final double entry = tableau.getEntry(0, i);
      if(Precision.compareTo(entry, 0D, epsilon) > 0) {
        columnsToDrop.add(i);
      }
    }
    for(int i = 0; i < getNumArtificialVariables(); i++) {
      int col = i + getArtificialVariableOffset();
      if(getBasicRow(col) == null) {
        columnsToDrop.add(col);
      }
    }
    double[][] matrix = new double[getHeight() - 1][getWidth() - columnsToDrop.size()];
    for(int i = 1; i < getHeight(); i++) {
      int col = 0;
      for(int j = 0; j < getWidth(); j++) {
        if(!columnsToDrop.contains(j)) {
          matrix[i - 1][col++] = tableau.getEntry(i, j);
        }
      }
    }
    Integer[] drop = columnsToDrop.toArray(new Integer[columnsToDrop.size()]);
    for(int i = drop.length - 1; i >= 0; i--) {
      columnLabels.remove((int)drop[i]);
    }
    this.tableau = new Array2DRowRealMatrix(matrix);
    this.numArtificialVariables = 0;
  }
  protected void initializeColumnLabels() {
    if(getNumObjectiveFunctions() == 2) {
      columnLabels.add("W");
    }
    columnLabels.add("Z");
    for(int i = 0; i < getOriginalNumDecisionVariables(); i++) {
      columnLabels.add("x" + i);
    }
    if(!restrictToNonNegative) {
      columnLabels.add(NEGATIVE_VAR_COLUMN_LABEL);
    }
    for(int i = 0; i < getNumSlackVariables(); i++) {
      columnLabels.add("s" + i);
    }
    for(int i = 0; i < getNumArtificialVariables(); i++) {
      columnLabels.add("a" + i);
    }
    columnLabels.add("RHS");
  }
  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    MatrixUtils.deserializeRealMatrix(this, "tableau", ois);
  }
  final protected void setEntry(final int row, final int column, final double value) {
    tableau.setEntry(row, column, value);
  }
  protected void subtractRow(final int minuendRow, final int subtrahendRow, final double multiple) {
    for(int i = 0; i < getWidth(); i++) {
      double result = tableau.getEntry(minuendRow, i) - tableau.getEntry(subtrahendRow, i) * multiple;
      if(FastMath.abs(result) < cutOff) {
        result = 0.0D;
      }
      tableau.setEntry(minuendRow, i, result);
    }
  }
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    MatrixUtils.serializeRealMatrix(tableau, oos);
  }
}