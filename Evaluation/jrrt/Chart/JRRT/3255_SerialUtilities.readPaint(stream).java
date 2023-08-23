package org.jfree.chart.renderer.xy;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

public class HighLowRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -8135673815876552516L;
  private boolean drawOpenTicks;
  private boolean drawCloseTicks;
  private transient Paint openTickPaint;
  private transient Paint closeTickPaint;
  private double tickLength;
  public HighLowRenderer() {
    super();
    this.drawOpenTicks = true;
    this.drawCloseTicks = true;
    this.tickLength = 2.0D;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Paint getCloseTickPaint() {
    return this.closeTickPaint;
  }
  public Paint getOpenTickPaint() {
    return this.openTickPaint;
  }
  public Range findRangeBounds(XYDataset dataset) {
    if(dataset != null) {
      return DatasetUtilities.findRangeBounds(dataset, true);
    }
    else {
      return null;
    }
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof HighLowRenderer)) {
      return false;
    }
    HighLowRenderer that = (HighLowRenderer)obj;
    if(this.drawOpenTicks != that.drawOpenTicks) {
      return false;
    }
    if(this.drawCloseTicks != that.drawCloseTicks) {
      return false;
    }
    if(!PaintUtilities.equal(this.openTickPaint, that.openTickPaint)) {
      return false;
    }
    if(!PaintUtilities.equal(this.closeTickPaint, that.closeTickPaint)) {
      return false;
    }
    if(this.tickLength != that.tickLength) {
      return false;
    }
    if(!super.equals(obj)) {
      return false;
    }
    return true;
  }
  public boolean getDrawCloseTicks() {
    return this.drawCloseTicks;
  }
  public boolean getDrawOpenTicks() {
    return this.drawOpenTicks;
  }
  public double getTickLength() {
    return this.tickLength;
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    double x = dataset.getXValue(series, item);
    if(!domainAxis.getRange().contains(x)) {
      return ;
    }
    double xx = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
    Shape entityArea = null;
    EntityCollection entities = null;
    if(state.getInfo() != null) {
      entities = state.getInfo().getOwner().getEntityCollection();
    }
    PlotOrientation orientation = plot.getOrientation();
    RectangleEdge location = plot.getRangeAxisEdge();
    Paint itemPaint = getItemPaint(series, item, selected);
    Stroke itemStroke = getItemStroke(series, item, selected);
    g2.setPaint(itemPaint);
    g2.setStroke(itemStroke);
    if(dataset instanceof OHLCDataset) {
      OHLCDataset hld = (OHLCDataset)dataset;
      double yHigh = hld.getHighValue(series, item);
      double yLow = hld.getLowValue(series, item);
      if(!Double.isNaN(yHigh) && !Double.isNaN(yLow)) {
        double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea, location);
        double yyLow = rangeAxis.valueToJava2D(yLow, dataArea, location);
        if(orientation == PlotOrientation.HORIZONTAL) {
          g2.draw(new Line2D.Double(yyLow, xx, yyHigh, xx));
          entityArea = new Rectangle2D.Double(Math.min(yyLow, yyHigh), xx - 1.0D, Math.abs(yyHigh - yyLow), 2.0D);
        }
        else 
          if(orientation == PlotOrientation.VERTICAL) {
            g2.draw(new Line2D.Double(xx, yyLow, xx, yyHigh));
            entityArea = new Rectangle2D.Double(xx - 1.0D, Math.min(yyLow, yyHigh), 2.0D, Math.abs(yyHigh - yyLow));
          }
      }
      double delta = getTickLength();
      if(domainAxis.isInverted()) {
        delta = -delta;
      }
      if(getDrawOpenTicks()) {
        double yOpen = hld.getOpenValue(series, item);
        if(!Double.isNaN(yOpen)) {
          double yyOpen = rangeAxis.valueToJava2D(yOpen, dataArea, location);
          if(this.openTickPaint != null) {
            g2.setPaint(this.openTickPaint);
          }
          else {
            g2.setPaint(itemPaint);
          }
          if(orientation == PlotOrientation.HORIZONTAL) {
            g2.draw(new Line2D.Double(yyOpen, xx + delta, yyOpen, xx));
          }
          else 
            if(orientation == PlotOrientation.VERTICAL) {
              g2.draw(new Line2D.Double(xx - delta, yyOpen, xx, yyOpen));
            }
        }
      }
      if(getDrawCloseTicks()) {
        double yClose = hld.getCloseValue(series, item);
        if(!Double.isNaN(yClose)) {
          double yyClose = rangeAxis.valueToJava2D(yClose, dataArea, location);
          if(this.closeTickPaint != null) {
            g2.setPaint(this.closeTickPaint);
          }
          else {
            g2.setPaint(itemPaint);
          }
          if(orientation == PlotOrientation.HORIZONTAL) {
            g2.draw(new Line2D.Double(yyClose, xx, yyClose, xx - delta));
          }
          else 
            if(orientation == PlotOrientation.VERTICAL) {
              g2.draw(new Line2D.Double(xx, yyClose, xx + delta, yyClose));
            }
        }
      }
    }
    else {
      if(item > 0) {
        double x0 = dataset.getXValue(series, item - 1);
        double y0 = dataset.getYValue(series, item - 1);
        double y = dataset.getYValue(series, item);
        if(Double.isNaN(x0) || Double.isNaN(y0) || Double.isNaN(y)) {
          return ;
        }
        double xx0 = domainAxis.valueToJava2D(x0, dataArea, plot.getDomainAxisEdge());
        double yy0 = rangeAxis.valueToJava2D(y0, dataArea, location);
        double yy = rangeAxis.valueToJava2D(y, dataArea, location);
        if(orientation == PlotOrientation.HORIZONTAL) {
          g2.draw(new Line2D.Double(yy0, xx0, yy, xx));
        }
        else 
          if(orientation == PlotOrientation.VERTICAL) {
            g2.draw(new Line2D.Double(xx0, yy0, xx, yy));
          }
      }
    }
    if(entities != null) {
      addEntity(entities, entityArea, dataset, series, item, selected, 0.0D, 0.0D);
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    Paint var_3255 = SerialUtilities.readPaint(stream);
    this.openTickPaint = var_3255;
    this.closeTickPaint = SerialUtilities.readPaint(stream);
  }
  public void setCloseTickPaint(Paint paint) {
    this.closeTickPaint = paint;
    fireChangeEvent();
  }
  public void setDrawCloseTicks(boolean draw) {
    this.drawCloseTicks = draw;
    fireChangeEvent();
  }
  public void setDrawOpenTicks(boolean draw) {
    this.drawOpenTicks = draw;
    fireChangeEvent();
  }
  public void setOpenTickPaint(Paint paint) {
    this.openTickPaint = paint;
    fireChangeEvent();
  }
  public void setTickLength(double length) {
    this.tickLength = length;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.openTickPaint, stream);
    SerialUtilities.writePaint(this.closeTickPaint, stream);
  }
}