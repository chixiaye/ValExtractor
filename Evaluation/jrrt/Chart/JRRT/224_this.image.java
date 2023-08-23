package org.jfree.chart.annotations;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleAnchor;
import org.jfree.chart.util.RectangleEdge;

public class XYImageAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -4364694501921559958L;
  private double x;
  private double y;
  private transient Image image;
  private RectangleAnchor anchor;
  public XYImageAnnotation(double x, double y, Image image) {
    this(x, y, image, RectangleAnchor.CENTER);
  }
  public XYImageAnnotation(double x, double y, Image image, RectangleAnchor anchor) {
    super();
    if(image == null) {
      throw new IllegalArgumentException("Null \'image\' argument.");
    }
    if(anchor == null) {
      throw new IllegalArgumentException("Null \'anchor\' argument.");
    }
    this.x = x;
    this.y = y;
    this.image = image;
    this.anchor = anchor;
  }
  public Image getImage() {
    return this.image;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public RectangleAnchor getImageAnchor() {
    return this.anchor;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!super.equals(obj)) {
      return false;
    }
    if(!(obj instanceof XYImageAnnotation)) {
      return false;
    }
    XYImageAnnotation that = (XYImageAnnotation)obj;
    if(this.x != that.x) {
      return false;
    }
    if(this.y != that.y) {
      return false;
    }
    if(!ObjectUtilities.equal(this.image, that.image)) {
      return false;
    }
    if(!this.anchor.equals(that.anchor)) {
      return false;
    }
    return true;
  }
  public double getX() {
    return this.x;
  }
  public double getY() {
    return this.y;
  }
  public int hashCode() {
    return this.image.hashCode();
  }
  public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
    PlotOrientation orientation = plot.getOrientation();
    AxisLocation domainAxisLocation = plot.getDomainAxisLocation();
    AxisLocation rangeAxisLocation = plot.getRangeAxisLocation();
    RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(domainAxisLocation, orientation);
    RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(rangeAxisLocation, orientation);
    float j2DX = (float)domainAxis.valueToJava2D(this.x, dataArea, domainEdge);
    float j2DY = (float)rangeAxis.valueToJava2D(this.y, dataArea, rangeEdge);
    float xx = 0.0F;
    float yy = 0.0F;
    if(orientation == PlotOrientation.HORIZONTAL) {
      xx = j2DY;
      yy = j2DX;
    }
    else 
      if(orientation == PlotOrientation.VERTICAL) {
        xx = j2DX;
        yy = j2DY;
      }
    int w = this.image.getWidth(null);
    Image var_224 = this.image;
    int h = var_224.getHeight(null);
    Rectangle2D imageRect = new Rectangle2D.Double(0, 0, w, h);
    Point2D anchorPoint = RectangleAnchor.coordinates(imageRect, this.anchor);
    xx = xx - (float)anchorPoint.getX();
    yy = yy - (float)anchorPoint.getY();
    g2.drawImage(this.image, (int)xx, (int)yy, null);
    String toolTip = getToolTipText();
    String url = getURL();
    if(toolTip != null || url != null) {
      addEntity(info, new Rectangle2D.Float(xx, yy, w, h), rendererIndex, toolTip, url);
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
  }
}