package org.apache.commons.math3.filter;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.MathUtils;

public class KalmanFilter  {
  final private ProcessModel processModel;
  final private MeasurementModel measurementModel;
  private RealMatrix transitionMatrix;
  private RealMatrix transitionMatrixT;
  private RealMatrix controlMatrix;
  private RealMatrix measurementMatrix;
  private RealMatrix measurementMatrixT;
  private RealVector stateEstimation;
  private RealMatrix errorCovariance;
  public KalmanFilter(final ProcessModel process, final MeasurementModel measurement) throws NullArgumentException, NonSquareMatrixException, DimensionMismatchException, MatrixDimensionMismatchException {
    super();
    MathUtils.checkNotNull(process);
    MathUtils.checkNotNull(measurement);
    this.processModel = process;
    this.measurementModel = measurement;
    transitionMatrix = processModel.getStateTransitionMatrix();
    MathUtils.checkNotNull(transitionMatrix);
    transitionMatrixT = transitionMatrix.transpose();
    if(processModel.getControlMatrix() == null) {
      controlMatrix = new Array2DRowRealMatrix();
    }
    else {
      controlMatrix = processModel.getControlMatrix();
    }
    measurementMatrix = measurementModel.getMeasurementMatrix();
    MathUtils.checkNotNull(measurementMatrix);
    measurementMatrixT = measurementMatrix.transpose();
    RealMatrix processNoise = processModel.getProcessNoise();
    MathUtils.checkNotNull(processNoise);
    RealMatrix measNoise = measurementModel.getMeasurementNoise();
    MathUtils.checkNotNull(measNoise);
    if(processModel.getInitialStateEstimate() == null) {
      stateEstimation = new ArrayRealVector(transitionMatrix.getColumnDimension());
    }
    else {
      stateEstimation = processModel.getInitialStateEstimate();
    }
    if(transitionMatrix.getColumnDimension() != stateEstimation.getDimension()) {
      throw new DimensionMismatchException(transitionMatrix.getColumnDimension(), stateEstimation.getDimension());
    }
    if(processModel.getInitialErrorCovariance() == null) {
      errorCovariance = processNoise.copy();
    }
    else {
      errorCovariance = processModel.getInitialErrorCovariance();
    }
    if(!transitionMatrix.isSquare()) {
      throw new NonSquareMatrixException(transitionMatrix.getRowDimension(), transitionMatrix.getColumnDimension());
    }
    if(controlMatrix != null && controlMatrix.getRowDimension() > 0 && controlMatrix.getColumnDimension() > 0 && (controlMatrix.getRowDimension() != transitionMatrix.getRowDimension() || controlMatrix.getColumnDimension() != 1)) {
      throw new MatrixDimensionMismatchException(controlMatrix.getRowDimension(), controlMatrix.getColumnDimension(), transitionMatrix.getRowDimension(), 1);
    }
    MatrixUtils.checkAdditionCompatible(transitionMatrix, processNoise);
    if(measurementMatrix.getColumnDimension() != transitionMatrix.getRowDimension()) {
      throw new MatrixDimensionMismatchException(measurementMatrix.getRowDimension(), measurementMatrix.getColumnDimension(), measurementMatrix.getRowDimension(), transitionMatrix.getRowDimension());
    }
    if(measNoise.getRowDimension() != measurementMatrix.getRowDimension() || measNoise.getColumnDimension() != 1) {
      throw new MatrixDimensionMismatchException(measNoise.getRowDimension(), measNoise.getColumnDimension(), measurementMatrix.getRowDimension(), 1);
    }
  }
  public RealMatrix getErrorCovarianceMatrix() {
    return errorCovariance.copy();
  }
  public RealVector getStateEstimationVector() {
    return stateEstimation.copy();
  }
  public double[] getStateEstimation() {
    return stateEstimation.toArray();
  }
  public double[][] getErrorCovariance() {
    return errorCovariance.getData();
  }
  public int getMeasurementDimension() {
    return measurementMatrix.getRowDimension();
  }
  public int getStateDimension() {
    return stateEstimation.getDimension();
  }
  public void correct(final double[] z) throws NullArgumentException, DimensionMismatchException, SingularMatrixException {
    correct(new ArrayRealVector(z));
  }
  public void correct(final RealVector z) throws NullArgumentException, DimensionMismatchException, SingularMatrixException {
    MathUtils.checkNotNull(z);
    if(z.getDimension() != measurementMatrix.getRowDimension()) {
      throw new DimensionMismatchException(z.getDimension(), measurementMatrix.getRowDimension());
    }
    RealMatrix s = measurementMatrix.multiply(errorCovariance).multiply(measurementMatrixT).add(measurementModel.getMeasurementNoise());
    DecompositionSolver solver = new CholeskyDecomposition(s).getSolver();
    RealMatrix invertedS = solver.getInverse();
    RealVector innovation = z.subtract(measurementMatrix.operate(stateEstimation));
    RealMatrix kalmanGain = errorCovariance.multiply(measurementMatrixT).multiply(invertedS);
    stateEstimation = stateEstimation.add(kalmanGain.operate(innovation));
    RealMatrix identity = MatrixUtils.createRealIdentityMatrix(kalmanGain.getRowDimension());
    errorCovariance = identity.subtract(kalmanGain.multiply(measurementMatrix)).multiply(errorCovariance);
  }
  public void predict() {
    predict((RealVector)null);
  }
  public void predict(final double[] u) throws DimensionMismatchException {
    predict(new ArrayRealVector(u));
  }
  public void predict(final RealVector u) throws DimensionMismatchException {
    if(u != null && u.getDimension() != controlMatrix.getColumnDimension()) {
      int var_948 = u.getDimension();
      throw new DimensionMismatchException(var_948, controlMatrix.getColumnDimension());
    }
    stateEstimation = transitionMatrix.operate(stateEstimation);
    if(u != null) {
      stateEstimation = stateEstimation.add(controlMatrix.operate(u));
    }
    errorCovariance = transitionMatrix.multiply(errorCovariance).multiply(transitionMatrixT).add(processModel.getProcessNoise());
  }
}