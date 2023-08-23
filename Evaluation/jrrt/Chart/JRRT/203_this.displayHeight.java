package org.jfree.chart.annotations;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.Drawable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;

public class XYDrawableAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -6540812859722691020L;
  private double drawScaleFactor;
  private double x;
  private double y;
  private double displayWidth;
  private double displayHeight;
  private Drawable drawable;
  public XYDrawableAnnotation(double x, double y, double displayWidth, double displayHeight, double drawScaleFactor, Drawable drawable) {
    super();
    if(drawable == null) {
      throw new IllegalArgumentException("Null \'drawable\' argument.");
    }
    this.x = x;
    this.y = y;
    this.displayWidth = displayWidth;
    this.displayHeight = displayHeight;
    this.drawScaleFactor = drawScaleFactor;
    this.drawable = drawable;
  }
  public XYDrawableAnnotation(double x, double y, double width, double height, Drawable drawable) {
    this(x, y, width, height, 1.0D, drawable);
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!super.equals(obj)) {
      return false;
    }
    if(!(obj instanceof XYDrawableAnnotation)) {
      return false;
    }
    XYDrawableAnnotation that = (XYDrawableAnnotation)obj;
    if(this.x != that.x) {
      return false;
    }
    if(this.y != that.y) {
      return false;
    }
    if(this.displayWidth != that.displayWidth) {
      return false;
    }
    if(this.displayHeight != that.displayHeight) {
      return false;
    }
    if(this.drawScaleFactor != that.drawScaleFactor) {
      return false;
    }
    if(!ObjectUtilities.equal(this.drawable, that.drawable)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(this.x);
    result = (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.y);
    result = 29 * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.displayWidth);
    result = 29 * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.displayHeight);
    result = 29 * result + (int)(temp ^ (temp >>> 32));
    return result;
  }
  public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
    PlotOrientation orientation = plot.getOrientation();
    RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
    RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
    float j2DX = (float)domainAxis.valueToJava2D(this.x, dataArea, domainEdge);
    float j2DY = (float)rangeAxis.valueToJava2D(this.y, dataArea, rangeEdge);
    double var_203 = this.displayHeight;
    Rectangle2D displayArea = new Rectangle2D.Double(j2DX - this.displayWidth / 2.0D, j2DY - this.displayHeight / 2.0D, this.displayWidth, var_203);
    AffineTransform savedTransform = g2.getTransform();
    Rectangle2D drawArea = new Rectangle2D.Double(0.0D, 0.0D, this.displayWidth * this.drawScaleFactor, this.displayHeight * this.drawScaleFactor);
    g2.scale(1 / this.drawScaleFactor, 1 / this.drawScaleFactor);
    g2.translate((j2DX - this.displayWidth / 2.0D) * this.drawScaleFactor, (j2DY - this.displayHeight / 2.0D) * this.drawScaleFactor);
    this.drawable.draw(g2, drawArea);
    g2.setTransform(savedTransform);
    String toolTip = getToolTipText();
    String url = getURL();
    if(toolTip != null || url != null) {
      addEntity(info, displayArea, rendererIndex, toolTip, url);
    }
  }
}