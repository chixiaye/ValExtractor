package org.jfree.chart.needle;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class ShipNeedle extends MeterNeedle implements Cloneable, Serializable  {
  final private static long serialVersionUID = 149554868169435612L;
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
    if(super.equals(object) && object instanceof ShipNeedle) {
      return true;
    }
    return false;
  }
  public int hashCode() {
    return super.hashCode();
  }
  protected void drawNeedle(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {
    GeneralPath shape = new GeneralPath();
    shape.append(new Arc2D.Double(-9.0D, -7.0D, 10, 14, 0.0D, 25.5D, Arc2D.OPEN), true);
    shape.append(new Arc2D.Double(0.0D, -7.0D, 10, 14, 154.5D, 25.5D, Arc2D.OPEN), true);
    shape.closePath();
    getTransform().setToTranslation(plotArea.getMinX(), plotArea.getMaxY());
    java.awt.geom.AffineTransform var_1246 = getTransform();
    var_1246.scale(plotArea.getWidth(), plotArea.getHeight() / 3);
    shape.transform(getTransform());
    if((rotate != null) && (angle != 0)) {
      getTransform().setToRotation(angle, rotate.getX(), rotate.getY());
      shape.transform(getTransform());
    }
    defaultDisplay(g2, shape);
  }
}