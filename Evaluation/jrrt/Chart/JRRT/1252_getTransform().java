package org.jfree.chart.needle;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class LineNeedle extends MeterNeedle implements Cloneable, Serializable  {
  final private static long serialVersionUID = 6215321387896748945L;
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof LineNeedle)) {
      return false;
    }
    return super.equals(obj);
  }
  public int hashCode() {
    return super.hashCode();
  }
  protected void drawNeedle(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
    Line2D shape = new Line2D.Double();
    double x = plotArea.getMinX() + (plotArea.getWidth() / 2);
    shape.setLine(x, plotArea.getMinY(), x, plotArea.getMaxY());
    Shape s = shape;
    if((rotate != null) && (angle != 0)) {
      getTransform().setToRotation(angle, rotate.getX(), rotate.getY());
      java.awt.geom.AffineTransform var_1252 = getTransform();
      s = var_1252.createTransformedShape(s);
    }
    defaultDisplay(g2, s);
  }
}