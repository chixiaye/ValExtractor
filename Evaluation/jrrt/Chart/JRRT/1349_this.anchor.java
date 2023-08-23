package org.jfree.chart.plot;
import java.awt.geom.Point2D;

public class CrosshairState  {
  private boolean calculateDistanceInDataSpace = false;
  private double anchorX;
  private double anchorY;
  private Point2D anchor;
  private double crosshairX;
  private double crosshairY;
  private int datasetIndex;
  private int domainAxisIndex;
  private int rangeAxisIndex;
  private double distance;
  public CrosshairState() {
    this(false);
  }
  public CrosshairState(boolean calculateDistanceInDataSpace) {
    super();
    this.calculateDistanceInDataSpace = calculateDistanceInDataSpace;
  }
  public Point2D getAnchor() {
    return this.anchor;
  }
  public double getAnchorX() {
    return this.anchorX;
  }
  public double getAnchorY() {
    return this.anchorY;
  }
  public double getCrosshairDistance() {
    return this.distance;
  }
  public double getCrosshairX() {
    return this.crosshairX;
  }
  public double getCrosshairY() {
    return this.crosshairY;
  }
  public int getDatasetIndex() {
    return this.datasetIndex;
  }
  public int getDomainAxisIndex() {
    return this.domainAxisIndex;
  }
  public int getRangeAxisIndex() {
    return this.rangeAxisIndex;
  }
  public void setAnchor(Point2D anchor) {
    this.anchor = anchor;
  }
  public void setAnchorX(double x) {
    this.anchorX = x;
  }
  public void setAnchorY(double y) {
    this.anchorY = y;
  }
  public void setCrosshairDistance(double distance) {
    this.distance = distance;
  }
  public void setCrosshairX(double x) {
    this.crosshairX = x;
  }
  public void setCrosshairY(double y) {
    this.crosshairY = y;
  }
  public void setDatasetIndex(int index) {
    this.datasetIndex = index;
  }
  public void updateCrosshairPoint(double x, double y, double transX, double transY, PlotOrientation orientation) {
    updateCrosshairPoint(x, y, 0, 0, transX, transY, orientation);
  }
  public void updateCrosshairPoint(double x, double y, int domainAxisIndex, int rangeAxisIndex, double transX, double transY, PlotOrientation orientation) {
    Point2D var_1349 = this.anchor;
    if(var_1349 != null) {
      double d = 0.0D;
      if(this.calculateDistanceInDataSpace) {
        d = (x - this.anchorX) * (x - this.anchorX) + (y - this.anchorY) * (y - this.anchorY);
      }
      else {
        double xx = this.anchor.getX();
        double yy = this.anchor.getY();
        if(orientation == PlotOrientation.HORIZONTAL) {
          double temp = yy;
          yy = xx;
          xx = temp;
        }
        d = (transX - xx) * (transX - xx) + (transY - yy) * (transY - yy);
      }
      if(d < this.distance) {
        this.crosshairX = x;
        this.crosshairY = y;
        this.domainAxisIndex = domainAxisIndex;
        this.rangeAxisIndex = rangeAxisIndex;
        this.distance = d;
      }
    }
  }
  public void updateCrosshairX(double candidateX) {
    updateCrosshairX(candidateX, 0);
  }
  public void updateCrosshairX(double candidateX, int domainAxisIndex) {
    double d = Math.abs(candidateX - this.anchorX);
    if(d < this.distance) {
      this.crosshairX = candidateX;
      this.domainAxisIndex = domainAxisIndex;
      this.distance = d;
    }
  }
  public void updateCrosshairY(double candidateY) {
    updateCrosshairY(candidateY, 0);
  }
  public void updateCrosshairY(double candidateY, int rangeAxisIndex) {
    double d = Math.abs(candidateY - this.anchorY);
    if(d < this.distance) {
      this.crosshairY = candidateY;
      this.rangeAxisIndex = rangeAxisIndex;
      this.distance = d;
    }
  }
}