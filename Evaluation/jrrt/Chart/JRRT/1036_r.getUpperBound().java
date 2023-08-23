package org.jfree.chart.block;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.chart.util.Size2D;
import org.jfree.data.Range;

public class AbstractBlock implements Cloneable, Serializable  {
  final private static long serialVersionUID = 7689852412141274563L;
  private String id;
  private RectangleInsets margin;
  private BlockFrame frame;
  private RectangleInsets padding;
  private double width;
  private double height;
  private transient Rectangle2D bounds;
  protected AbstractBlock() {
    super();
    this.id = null;
    this.width = 0.0D;
    this.height = 0.0D;
    this.bounds = new Rectangle2D.Float();
    this.margin = RectangleInsets.ZERO_INSETS;
    this.frame = BlockBorder.NONE;
    this.padding = RectangleInsets.ZERO_INSETS;
  }
  public BlockFrame getFrame() {
    return this.frame;
  }
  public Object clone() throws CloneNotSupportedException {
    AbstractBlock clone = (AbstractBlock)super.clone();
    clone.bounds = (Rectangle2D)ShapeUtilities.clone(this.bounds);
    if(this.frame instanceof PublicCloneable) {
      PublicCloneable pc = (PublicCloneable)this.frame;
      clone.frame = (BlockFrame)pc.clone();
    }
    return clone;
  }
  private Range trimToContentHeight(Range r) {
    if(r == null) {
      return null;
    }
    double lowerBound = 0.0D;
    double upperBound = Double.POSITIVE_INFINITY;
    if(r.getLowerBound() > 0.0D) {
      lowerBound = trimToContentHeight(r.getLowerBound());
    }
    double var_1036 = r.getUpperBound();
    if(var_1036 < Double.POSITIVE_INFINITY) {
      upperBound = trimToContentHeight(r.getUpperBound());
    }
    return new Range(lowerBound, upperBound);
  }
  private Range trimToContentWidth(Range r) {
    if(r == null) {
      return null;
    }
    double lowerBound = 0.0D;
    double upperBound = Double.POSITIVE_INFINITY;
    if(r.getLowerBound() > 0.0D) {
      lowerBound = trimToContentWidth(r.getLowerBound());
    }
    if(r.getUpperBound() < Double.POSITIVE_INFINITY) {
      upperBound = trimToContentWidth(r.getUpperBound());
    }
    return new Range(lowerBound, upperBound);
  }
  public Rectangle2D getBounds() {
    return this.bounds;
  }
  protected Rectangle2D trimBorder(Rectangle2D area) {
    this.frame.getInsets().trim(area);
    return area;
  }
  protected Rectangle2D trimMargin(Rectangle2D area) {
    this.margin.trim(area);
    return area;
  }
  protected Rectangle2D trimPadding(Rectangle2D area) {
    this.padding.trim(area);
    return area;
  }
  protected RectangleConstraint toContentConstraint(RectangleConstraint c) {
    if(c == null) {
      throw new IllegalArgumentException("Null \'c\' argument.");
    }
    if(c.equals(RectangleConstraint.NONE)) {
      return c;
    }
    double w = c.getWidth();
    Range wr = c.getWidthRange();
    double h = c.getHeight();
    Range hr = c.getHeightRange();
    double ww = trimToContentWidth(w);
    double hh = trimToContentHeight(h);
    Range wwr = trimToContentWidth(wr);
    Range hhr = trimToContentHeight(hr);
    return new RectangleConstraint(ww, wwr, c.getWidthConstraintType(), hh, hhr, c.getHeightConstraintType());
  }
  public RectangleInsets getMargin() {
    return this.margin;
  }
  public RectangleInsets getPadding() {
    return this.padding;
  }
  public Size2D arrange(Graphics2D g2) {
    return arrange(g2, RectangleConstraint.NONE);
  }
  public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
    Size2D base = new Size2D(getWidth(), getHeight());
    return constraint.calculateConstrainedSize(base);
  }
  public String getID() {
    return this.id;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof AbstractBlock)) {
      return false;
    }
    AbstractBlock that = (AbstractBlock)obj;
    if(!ObjectUtilities.equal(this.id, that.id)) {
      return false;
    }
    if(!this.frame.equals(that.frame)) {
      return false;
    }
    if(!this.bounds.equals(that.bounds)) {
      return false;
    }
    if(!this.margin.equals(that.margin)) {
      return false;
    }
    if(!this.padding.equals(that.padding)) {
      return false;
    }
    if(this.height != that.height) {
      return false;
    }
    if(this.width != that.width) {
      return false;
    }
    return true;
  }
  protected double calculateTotalHeight(double contentHeight) {
    double result = contentHeight;
    result = this.padding.extendHeight(result);
    result = this.frame.getInsets().extendHeight(result);
    result = this.margin.extendHeight(result);
    return result;
  }
  protected double calculateTotalWidth(double contentWidth) {
    double result = contentWidth;
    result = this.padding.extendWidth(result);
    result = this.frame.getInsets().extendWidth(result);
    result = this.margin.extendWidth(result);
    return result;
  }
  public double getContentXOffset() {
    return this.margin.getLeft() + this.frame.getInsets().getLeft() + this.padding.getLeft();
  }
  public double getContentYOffset() {
    return this.margin.getTop() + this.frame.getInsets().getTop() + this.padding.getTop();
  }
  public double getHeight() {
    return this.height;
  }
  public double getWidth() {
    return this.width;
  }
  protected double trimToContentHeight(double fixedHeight) {
    double result = this.margin.trimHeight(fixedHeight);
    result = this.frame.getInsets().trimHeight(result);
    result = this.padding.trimHeight(result);
    return Math.max(result, 0.0D);
  }
  protected double trimToContentWidth(double fixedWidth) {
    double result = this.margin.trimWidth(fixedWidth);
    result = this.frame.getInsets().trimWidth(result);
    result = this.padding.trimWidth(result);
    return Math.max(result, 0.0D);
  }
  protected void drawBorder(Graphics2D g2, Rectangle2D area) {
    this.frame.draw(g2, area);
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.bounds = (Rectangle2D)SerialUtilities.readShape(stream);
  }
  public void setBounds(Rectangle2D bounds) {
    if(bounds == null) {
      throw new IllegalArgumentException("Null \'bounds\' argument.");
    }
    this.bounds = bounds;
  }
  public void setFrame(BlockFrame frame) {
    if(frame == null) {
      throw new IllegalArgumentException("Null \'frame\' argument.");
    }
    this.frame = frame;
  }
  public void setHeight(double height) {
    this.height = height;
  }
  public void setID(String id) {
    this.id = id;
  }
  public void setMargin(double top, double left, double bottom, double right) {
    setMargin(new RectangleInsets(top, left, bottom, right));
  }
  public void setMargin(RectangleInsets margin) {
    if(margin == null) {
      throw new IllegalArgumentException("Null \'margin\' argument.");
    }
    this.margin = margin;
  }
  public void setPadding(double top, double left, double bottom, double right) {
    setPadding(new RectangleInsets(top, left, bottom, right));
  }
  public void setPadding(RectangleInsets padding) {
    if(padding == null) {
      throw new IllegalArgumentException("Null \'padding\' argument.");
    }
    this.padding = padding;
  }
  public void setWidth(double width) {
    this.width = width;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writeShape(this.bounds, stream);
  }
}