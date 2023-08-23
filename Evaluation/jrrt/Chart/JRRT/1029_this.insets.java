package org.jfree.chart.block;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SerialUtilities;

public class LineBorder implements BlockFrame, Serializable  {
  final static long serialVersionUID = 4630356736707233924L;
  private transient Paint paint;
  private transient Stroke stroke;
  private RectangleInsets insets;
  public LineBorder() {
    this(Color.black, new BasicStroke(1.0F), new RectangleInsets(1.0D, 1.0D, 1.0D, 1.0D));
  }
  public LineBorder(Paint paint, Stroke stroke, RectangleInsets insets) {
    super();
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    if(stroke == null) {
      throw new IllegalArgumentException("Null \'stroke\' argument.");
    }
    if(insets == null) {
      throw new IllegalArgumentException("Null \'insets\' argument.");
    }
    this.paint = paint;
    this.stroke = stroke;
    this.insets = insets;
  }
  public Paint getPaint() {
    return this.paint;
  }
  public RectangleInsets getInsets() {
    return this.insets;
  }
  public Stroke getStroke() {
    return this.stroke;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof LineBorder)) {
      return false;
    }
    LineBorder that = (LineBorder)obj;
    if(!PaintUtilities.equal(this.paint, that.paint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.stroke, that.stroke)) {
      return false;
    }
    if(!this.insets.equals(that.insets)) {
      return false;
    }
    return true;
  }
  public void draw(Graphics2D g2, Rectangle2D area) {
    double w = area.getWidth();
    double h = area.getHeight();
    if(w <= 0.0D || h <= 0.0D) {
      return ;
    }
    double t = this.insets.calculateTopInset(h);
    double b = this.insets.calculateBottomInset(h);
    RectangleInsets var_1029 = this.insets;
    double l = var_1029.calculateLeftInset(w);
    double r = this.insets.calculateRightInset(w);
    double x = area.getX();
    double y = area.getY();
    double x0 = x + l / 2.0D;
    double x1 = x + w - r / 2.0D;
    double y0 = y + h - b / 2.0D;
    double y1 = y + t / 2.0D;
    g2.setPaint(getPaint());
    g2.setStroke(getStroke());
    Line2D line = new Line2D.Double();
    if(t > 0.0D) {
      line.setLine(x0, y1, x1, y1);
      g2.draw(line);
    }
    if(b > 0.0D) {
      line.setLine(x0, y0, x1, y0);
      g2.draw(line);
    }
    if(l > 0.0D) {
      line.setLine(x0, y0, x0, y1);
      g2.draw(line);
    }
    if(r > 0.0D) {
      line.setLine(x1, y0, x1, y1);
      g2.draw(line);
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.paint = SerialUtilities.readPaint(stream);
    this.stroke = SerialUtilities.readStroke(stream);
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.paint, stream);
    SerialUtilities.writeStroke(this.stroke, stream);
  }
}