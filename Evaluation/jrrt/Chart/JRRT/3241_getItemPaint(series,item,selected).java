package org.jfree.chart.renderer.xy;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

public class XYErrorRenderer extends XYLineAndShapeRenderer  {
  final static long serialVersionUID = 5162283570955172424L;
  private boolean drawXError;
  private boolean drawYError;
  private double capLength;
  private transient Paint errorPaint;
  private transient Stroke errorStroke;
  public XYErrorRenderer() {
    super(false, true);
    this.drawXError = true;
    this.drawYError = true;
    this.errorPaint = null;
    this.errorStroke = null;
    this.capLength = 4.0D;
  }
  public Paint getErrorPaint() {
    return this.errorPaint;
  }
  public Range findDomainBounds(XYDataset dataset) {
    if(dataset != null) {
      return DatasetUtilities.findDomainBounds(dataset, true);
    }
    else {
      return null;
    }
  }
  public Range findRangeBounds(XYDataset dataset) {
    if(dataset != null) {
      return DatasetUtilities.findRangeBounds(dataset, true);
    }
    else {
      return null;
    }
  }
  public Stroke getErrorStroke() {
    return this.errorStroke;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYErrorRenderer)) {
      return false;
    }
    XYErrorRenderer that = (XYErrorRenderer)obj;
    if(this.drawXError != that.drawXError) {
      return false;
    }
    if(this.drawYError != that.drawYError) {
      return false;
    }
    if(this.capLength != that.capLength) {
      return false;
    }
    if(!PaintUtilities.equal(this.errorPaint, that.errorPaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.errorStroke, that.errorStroke)) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getDrawXError() {
    return this.drawXError;
  }
  public boolean getDrawYError() {
    return this.drawYError;
  }
  public double getCapLength() {
    return this.capLength;
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    if(pass == 0 && dataset instanceof IntervalXYDataset && getItemVisible(series, item)) {
      IntervalXYDataset ixyd = (IntervalXYDataset)dataset;
      PlotOrientation orientation = plot.getOrientation();
      if(this.drawXError) {
        double x0 = ixyd.getStartXValue(series, item);
        double x1 = ixyd.getEndXValue(series, item);
        double y = ixyd.getYValue(series, item);
        RectangleEdge edge = plot.getDomainAxisEdge();
        double xx0 = domainAxis.valueToJava2D(x0, dataArea, edge);
        double xx1 = domainAxis.valueToJava2D(x1, dataArea, edge);
        double yy = rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
        Line2D line;
        Line2D cap1 = null;
        Line2D cap2 = null;
        double adj = this.capLength / 2.0D;
        if(orientation == PlotOrientation.VERTICAL) {
          line = new Line2D.Double(xx0, yy, xx1, yy);
          cap1 = new Line2D.Double(xx0, yy - adj, xx0, yy + adj);
          cap2 = new Line2D.Double(xx1, yy - adj, xx1, yy + adj);
        }
        else {
          line = new Line2D.Double(yy, xx0, yy, xx1);
          cap1 = new Line2D.Double(yy - adj, xx0, yy + adj, xx0);
          cap2 = new Line2D.Double(yy - adj, xx1, yy + adj, xx1);
        }
        if(this.errorPaint != null) {
          g2.setPaint(this.errorPaint);
        }
        else {
          Paint var_3241 = getItemPaint(series, item, selected);
          g2.setPaint(var_3241);
        }
        if(this.errorStroke != null) {
          g2.setStroke(this.errorStroke);
        }
        else {
          g2.setStroke(getItemStroke(series, item, selected));
        }
        g2.draw(line);
        g2.draw(cap1);
        g2.draw(cap2);
      }
      if(this.drawYError) {
        double y0 = ixyd.getStartYValue(series, item);
        double y1 = ixyd.getEndYValue(series, item);
        double x = ixyd.getXValue(series, item);
        RectangleEdge edge = plot.getRangeAxisEdge();
        double yy0 = rangeAxis.valueToJava2D(y0, dataArea, edge);
        double yy1 = rangeAxis.valueToJava2D(y1, dataArea, edge);
        double xx = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
        Line2D line;
        Line2D cap1 = null;
        Line2D cap2 = null;
        double adj = this.capLength / 2.0D;
        if(orientation == PlotOrientation.VERTICAL) {
          line = new Line2D.Double(xx, yy0, xx, yy1);
          cap1 = new Line2D.Double(xx - adj, yy0, xx + adj, yy0);
          cap2 = new Line2D.Double(xx - adj, yy1, xx + adj, yy1);
        }
        else {
          line = new Line2D.Double(yy0, xx, yy1, xx);
          cap1 = new Line2D.Double(yy0, xx - adj, yy0, xx + adj);
          cap2 = new Line2D.Double(yy1, xx - adj, yy1, xx + adj);
        }
        if(this.errorPaint != null) {
          g2.setPaint(this.errorPaint);
        }
        else {
          g2.setPaint(getItemPaint(series, item, selected));
        }
        if(this.errorStroke != null) {
          g2.setStroke(this.errorStroke);
        }
        else {
          g2.setStroke(getItemStroke(series, item, selected));
        }
        g2.draw(line);
        g2.draw(cap1);
        g2.draw(cap2);
      }
    }
    super.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, series, item, selected, pass);
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.errorPaint = SerialUtilities.readPaint(stream);
    this.errorStroke = SerialUtilities.readStroke(stream);
  }
  public void setCapLength(double length) {
    this.capLength = length;
    fireChangeEvent();
  }
  public void setDrawXError(boolean draw) {
    if(this.drawXError != draw) {
      this.drawXError = draw;
      fireChangeEvent();
    }
  }
  public void setDrawYError(boolean draw) {
    if(this.drawYError != draw) {
      this.drawYError = draw;
      fireChangeEvent();
    }
  }
  public void setErrorPaint(Paint paint) {
    this.errorPaint = paint;
    fireChangeEvent();
  }
  public void setErrorStroke(Stroke stroke) {
    this.errorStroke = stroke;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.errorPaint, stream);
    SerialUtilities.writeStroke(this.errorStroke, stream);
  }
}