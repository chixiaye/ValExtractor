package org.jfree.chart.renderer;
import java.awt.geom.Point2D;

public class Outlier implements Comparable  {
  private Point2D point;
  private double radius;
  public Outlier(double xCoord, double yCoord, double radius) {
    super();
    this.point = new Point2D.Double(xCoord - radius, yCoord - radius);
    this.radius = radius;
  }
  public Point2D getPoint() {
    return this.point;
  }
  public String toString() {
    return "{" + getX() + "," + getY() + "}";
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof Outlier)) {
      return false;
    }
    Outlier that = (Outlier)obj;
    if(!this.point.equals(that.point)) {
      return false;
    }
    if(this.radius != that.radius) {
      return false;
    }
    return true;
  }
  public boolean overlaps(Outlier other) {
    double var_2416 = other.getX();
    return ((var_2416 >= getX() - (this.radius * 1.1D)) && (other.getX() <= getX() + (this.radius * 1.1D)) && (other.getY() >= getY() - (this.radius * 1.1D)) && (other.getY() <= getY() + (this.radius * 1.1D)));
  }
  public double getRadius() {
    return this.radius;
  }
  public double getX() {
    return getPoint().getX();
  }
  public double getY() {
    return getPoint().getY();
  }
  public int compareTo(Object o) {
    Outlier outlier = (Outlier)o;
    Point2D p1 = getPoint();
    Point2D p2 = outlier.getPoint();
    if(p1.equals(p2)) {
      return 0;
    }
    else 
      if((p1.getX() < p2.getX()) || (p1.getY() < p2.getY())) {
        return -1;
      }
      else {
        return 1;
      }
  }
  public void setPoint(Point2D point) {
    this.point = point;
  }
  public void setRadius(double radius) {
    this.radius = radius;
  }
}