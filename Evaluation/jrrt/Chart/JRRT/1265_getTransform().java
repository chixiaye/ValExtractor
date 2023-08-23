package org.jfree.chart.needle;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class MiddlePinNeedle extends MeterNeedle implements Cloneable, Serializable  {
  final private static long serialVersionUID = 6237073996403125310L;
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public boolean equals(Object object) {
    if(object == null) {
      return false;
    }
    if(object == this) {
      return true;
    }
    if(super.equals(object) && object instanceof MiddlePinNeedle) {
      return true;
    }
    return false;
  }
  public int hashCode() {
    return super.hashCode();
  }
  protected void drawNeedle(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
    Area shape;
    GeneralPath pointer = new GeneralPath();
    int minY = (int)(plotArea.getMinY());
    int maxY = (int)(plotArea.getMaxY());
    int midY = ((maxY - minY) / 2) + minY;
    int midX = (int)(plotArea.getMinX() + (plotArea.getWidth() / 2));
    int lenX = (int)(plotArea.getWidth() / 10);
    if(lenX < 2) {
      lenX = 2;
    }
    pointer.moveTo(midX - lenX, midY - lenX);
    pointer.lineTo(midX + lenX, midY - lenX);
    pointer.lineTo(midX, minY);
    pointer.closePath();
    lenX = 4 * lenX;
    Ellipse2D circle = new Ellipse2D.Double(midX - lenX / 2, midY - lenX, lenX, lenX);
    shape = new Area(circle);
    shape.add(new Area(pointer));
    if((rotate != null) && (angle != 0)) {
      java.awt.geom.AffineTransform var_1265 = getTransform();
      var_1265.setToRotation(angle, rotate.getX(), rotate.getY());
      shape.transform(getTransform());
    }
    defaultDisplay(g2, shape);
  }
}