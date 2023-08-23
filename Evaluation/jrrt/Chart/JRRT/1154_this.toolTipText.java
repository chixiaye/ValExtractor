package org.jfree.chart.entity;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtilities;

public class ChartEntity implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -4445994133561919083L;
  private transient Shape area;
  private String toolTipText;
  private String urlText;
  public ChartEntity(Shape area) {
    this(area, null);
  }
  public ChartEntity(Shape area, String toolTipText) {
    this(area, toolTipText, null);
  }
  public ChartEntity(Shape area, String toolTipText, String urlText) {
    super();
    if(area == null) {
      throw new IllegalArgumentException("Null \'area\' argument.");
    }
    this.area = area;
    this.toolTipText = toolTipText;
    this.urlText = urlText;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Shape getArea() {
    return this.area;
  }
  public String getImageMapAreaTag(ToolTipTagFragmentGenerator toolTipTagFragmentGenerator, URLTagFragmentGenerator urlTagFragmentGenerator) {
    StringBuffer tag = new StringBuffer();
    boolean hasURL = (this.urlText == null ? false : !this.urlText.equals(""));
    String var_1154 = this.toolTipText;
    boolean hasToolTip = (var_1154 == null ? false : !this.toolTipText.equals(""));
    if(hasURL || hasToolTip) {
      tag.append("<area shape=\"" + getShapeType() + "\"" + " coords=\"" + getShapeCoords() + "\"");
      if(hasToolTip) {
        tag.append(toolTipTagFragmentGenerator.generateToolTipFragment(this.toolTipText));
      }
      if(hasURL) {
        tag.append(urlTagFragmentGenerator.generateURLFragment(this.urlText));
      }
      else {
        tag.append(" nohref=\"nohref\"");
      }
      if(!hasToolTip) {
        tag.append(" alt=\"\"");
      }
      tag.append("/>");
    }
    return tag.toString();
  }
  private String getPolyCoords(Shape shape) {
    if(shape == null) {
      throw new IllegalArgumentException("Null \'shape\' argument.");
    }
    StringBuffer result = new StringBuffer();
    boolean first = true;
    float[] coords = new float[6];
    PathIterator pi = shape.getPathIterator(null, 1.0D);
    while(!pi.isDone()){
      pi.currentSegment(coords);
      if(first) {
        first = false;
        result.append((int)coords[0]);
        result.append(",").append((int)coords[1]);
      }
      else {
        result.append(",");
        result.append((int)coords[0]);
        result.append(",");
        result.append((int)coords[1]);
      }
      pi.next();
    }
    return result.toString();
  }
  private String getRectCoords(Rectangle2D rectangle) {
    if(rectangle == null) {
      throw new IllegalArgumentException("Null \'rectangle\' argument.");
    }
    int x1 = (int)rectangle.getX();
    int y1 = (int)rectangle.getY();
    int x2 = x1 + (int)rectangle.getWidth();
    int y2 = y1 + (int)rectangle.getHeight();
    if(x2 == x1) {
      x2++;
    }
    if(y2 == y1) {
      y2++;
    }
    return x1 + "," + y1 + "," + x2 + "," + y2;
  }
  public String getShapeCoords() {
    if(this.area instanceof Rectangle2D) {
      return getRectCoords((Rectangle2D)this.area);
    }
    else {
      return getPolyCoords(this.area);
    }
  }
  public String getShapeType() {
    if(this.area instanceof Rectangle2D) {
      return "rect";
    }
    else {
      return "poly";
    }
  }
  public String getToolTipText() {
    return this.toolTipText;
  }
  public String getURLText() {
    return this.urlText;
  }
  public String toString() {
    StringBuffer buf = new StringBuffer("ChartEntity: ");
    buf.append("tooltip = ");
    buf.append(this.toolTipText);
    return buf.toString();
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof ChartEntity)) {
      return false;
    }
    ChartEntity that = (ChartEntity)obj;
    if(!this.area.equals(that.area)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.toolTipText, that.toolTipText)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.urlText, that.urlText)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    int result = 37;
    result = HashUtilities.hashCode(result, this.toolTipText);
    result = HashUtilities.hashCode(result, this.urlText);
    return result;
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.area = SerialUtilities.readShape(stream);
  }
  public void setArea(Shape area) {
    if(area == null) {
      throw new IllegalArgumentException("Null \'area\' argument.");
    }
    this.area = area;
  }
  public void setToolTipText(String text) {
    this.toolTipText = text;
  }
  public void setURLText(String text) {
    this.urlText = text;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writeShape(this.area, stream);
  }
}