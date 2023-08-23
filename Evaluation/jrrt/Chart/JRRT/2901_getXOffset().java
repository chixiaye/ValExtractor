package org.jfree.chart.renderer.xy;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.Effect3D;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.SerialUtilities;

public class XYLine3DRenderer extends XYLineAndShapeRenderer implements Effect3D, Serializable  {
  final private static long serialVersionUID = 588933208243446087L;
  final public static double DEFAULT_X_OFFSET = 12.0D;
  final public static double DEFAULT_Y_OFFSET = 8.0D;
  final public static Paint DEFAULT_WALL_PAINT = new Color(0xDD, 0xDD, 0xDD);
  private double xOffset;
  private double yOffset;
  private transient Paint wallPaint;
  public XYLine3DRenderer() {
    super();
    this.wallPaint = DEFAULT_WALL_PAINT;
    this.xOffset = DEFAULT_X_OFFSET;
    this.yOffset = DEFAULT_Y_OFFSET;
  }
  public Paint getWallPaint() {
    return this.wallPaint;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYLine3DRenderer)) {
      return false;
    }
    XYLine3DRenderer that = (XYLine3DRenderer)obj;
    if(this.xOffset != that.xOffset) {
      return false;
    }
    if(this.yOffset != that.yOffset) {
      return false;
    }
    if(!PaintUtilities.equal(this.wallPaint, that.wallPaint)) {
      return false;
    }
    return super.equals(obj);
  }
  protected boolean isItemPass(int pass) {
    return pass == 2;
  }
  protected boolean isLinePass(int pass) {
    return pass == 0 || pass == 1;
  }
  protected boolean isShadowPass(int pass) {
    return pass == 0;
  }
  public double getXOffset() {
    return this.xOffset;
  }
  public double getYOffset() {
    return this.yOffset;
  }
  public int getPassCount() {
    return 3;
  }
  protected void drawShape1(Graphics2D g2, int pass, int series, int item, boolean selected, Shape shape) {
    if(isShadowPass(pass)) {
      if(getWallPaint() != null) {
        g2.setStroke(getItemStroke(series, item, selected));
        g2.setPaint(getWallPaint());
        double var_2901 = getXOffset();
        g2.translate(var_2901, getYOffset());
        g2.draw(shape);
        g2.translate(-getXOffset(), -getYOffset());
      }
    }
    else {
      super.drawShape1(g2, pass, series, item, selected, shape);
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.wallPaint = SerialUtilities.readPaint(stream);
  }
  public void setWallPaint(Paint paint) {
    this.wallPaint = paint;
    fireChangeEvent();
  }
  public void setXOffset(double xOffset) {
    this.xOffset = xOffset;
    fireChangeEvent();
  }
  public void setYOffset(double yOffset) {
    this.yOffset = yOffset;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.wallPaint, stream);
  }
}