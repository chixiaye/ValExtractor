package org.jfree.chart.renderer.xy;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYCrosshairState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.xy.XYDataset;

public class XYStepRenderer extends XYLineAndShapeRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -8918141928884796108L;
  private double stepPoint = 1.0D;
  public XYStepRenderer() {
    this(null, null);
  }
  public XYStepRenderer(XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {
    super();
    setBaseToolTipGenerator(toolTipGenerator);
    setBaseURLGenerator(urlGenerator);
    setBaseShapesVisible(false);
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYLineAndShapeRenderer)) {
      return false;
    }
    XYStepRenderer that = (XYStepRenderer)obj;
    if(this.stepPoint != that.stepPoint) {
      return false;
    }
    return super.equals(obj);
  }
  public double getStepPoint() {
    return this.stepPoint;
  }
  public int hashCode() {
    return HashUtilities.hashCode(super.hashCode(), this.stepPoint);
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    if(!getItemVisible(series, item)) {
      return ;
    }
    PlotOrientation orientation = plot.getOrientation();
    Paint seriesPaint = getItemPaint(series, item, selected);
    Stroke seriesStroke = getItemStroke(series, item, selected);
    g2.setPaint(seriesPaint);
    g2.setStroke(seriesStroke);
    double x1 = dataset.getXValue(series, item);
    double y1 = dataset.getYValue(series, item);
    RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
    RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
    double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
    double transY1 = (Double.isNaN(y1) ? Double.NaN : rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation));
    if(pass == 0 && item > 0) {
      double x0 = dataset.getXValue(series, item - 1);
      double y0 = dataset.getYValue(series, item - 1);
      double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
      double transY0 = (Double.isNaN(y0) ? Double.NaN : rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation));
      PlotOrientation var_2863 = PlotOrientation.HORIZONTAL;
      if(orientation == var_2863) {
        if(transY0 == transY1) {
          drawLine(g2, state.workingLine, transY0, transX0, transY1, transX1);
        }
        else {
          double transXs = transX0 + (getStepPoint() * (transX1 - transX0));
          drawLine(g2, state.workingLine, transY0, transX0, transY0, transXs);
          drawLine(g2, state.workingLine, transY0, transXs, transY1, transXs);
          drawLine(g2, state.workingLine, transY1, transXs, transY1, transX1);
        }
      }
      else 
        if(orientation == PlotOrientation.VERTICAL) {
          if(transY0 == transY1) {
            drawLine(g2, state.workingLine, transX0, transY0, transX1, transY1);
          }
          else {
            double transXs = transX0 + (getStepPoint() * (transX1 - transX0));
            drawLine(g2, state.workingLine, transX0, transY0, transXs, transY0);
            drawLine(g2, state.workingLine, transXs, transY0, transXs, transY1);
            drawLine(g2, state.workingLine, transXs, transY1, transX1, transY1);
          }
        }
      int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
      int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
      XYCrosshairState crosshairState = state.getCrosshairState();
      updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation);
      EntityCollection entities = state.getEntityCollection();
      if(entities != null) {
        addEntity(entities, null, dataset, series, item, selected, transX1, transY1);
      }
    }
    if(pass == 1) {
      if(isItemLabelVisible(series, item, selected)) {
        double xx = transX1;
        double yy = transY1;
        if(orientation == PlotOrientation.HORIZONTAL) {
          xx = transY1;
          yy = transX1;
        }
        drawItemLabel(g2, orientation, dataset, series, item, selected, xx, yy, (y1 < 0.0D));
      }
    }
  }
  private void drawLine(Graphics2D g2, Line2D line, double x0, double y0, double x1, double y1) {
    if(Double.isNaN(x0) || Double.isNaN(x1) || Double.isNaN(y0) || Double.isNaN(y1)) {
      return ;
    }
    line.setLine(x0, y0, x1, y1);
    g2.draw(line);
  }
  public void setStepPoint(double stepPoint) {
    if(stepPoint < 0.0D || stepPoint > 1.0D) {
      throw new IllegalArgumentException("Requires stepPoint in [0.0;1.0]");
    }
    this.stepPoint = stepPoint;
    fireChangeEvent();
  }
}