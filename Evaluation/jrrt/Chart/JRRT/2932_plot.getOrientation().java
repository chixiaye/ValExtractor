package org.jfree.chart.renderer.xy;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYCrosshairState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.data.xy.XYDataset;

public class XYStepAreaRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -7311560779702649635L;
  final public static int SHAPES = 1;
  final public static int AREA = 2;
  final public static int AREA_AND_SHAPES = 3;
  private boolean shapesVisible;
  private boolean shapesFilled;
  private boolean plotArea;
  private boolean showOutline;
  protected transient Polygon pArea = null;
  private double rangeBase;
  public XYStepAreaRenderer() {
    this(AREA);
  }
  public XYStepAreaRenderer(int type) {
    this(type, null, null);
  }
  public XYStepAreaRenderer(int type, XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {
    super();
    setBaseToolTipGenerator(toolTipGenerator);
    setBaseURLGenerator(urlGenerator);
    if(type == AREA) {
      this.plotArea = true;
    }
    else 
      if(type == SHAPES) {
        this.shapesVisible = true;
      }
      else 
        if(type == AREA_AND_SHAPES) {
          this.plotArea = true;
          this.shapesVisible = true;
        }
    this.showOutline = false;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
    XYItemRendererState state = super.initialise(g2, dataArea, plot, data, info);
    state.setProcessVisibleItemsOnly(false);
    return state;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYStepAreaRenderer)) {
      return false;
    }
    XYStepAreaRenderer that = (XYStepAreaRenderer)obj;
    if(this.showOutline != that.showOutline) {
      return false;
    }
    if(this.shapesVisible != that.shapesVisible) {
      return false;
    }
    if(this.shapesFilled != that.shapesFilled) {
      return false;
    }
    if(this.plotArea != that.plotArea) {
      return false;
    }
    if(this.rangeBase != that.rangeBase) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getPlotArea() {
    return this.plotArea;
  }
  public boolean getShapesVisible() {
    return this.shapesVisible;
  }
  public boolean isOutline() {
    return this.showOutline;
  }
  public boolean isShapesFilled() {
    return this.shapesFilled;
  }
  public double getRangeBase() {
    return this.rangeBase;
  }
  protected static double restrictValueToDataArea(double value, XYPlot plot, Rectangle2D dataArea) {
    double min = 0;
    double max = 0;
    PlotOrientation var_2932 = plot.getOrientation();
    if(var_2932 == PlotOrientation.VERTICAL) {
      min = dataArea.getMinY();
      max = dataArea.getMaxY();
    }
    else 
      if(plot.getOrientation() == PlotOrientation.HORIZONTAL) {
        min = dataArea.getMinX();
        max = dataArea.getMaxX();
      }
    if(value < min) {
      value = min;
    }
    else 
      if(value > max) {
        value = max;
      }
    return value;
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    PlotOrientation orientation = plot.getOrientation();
    int itemCount = dataset.getItemCount(series);
    Paint paint = getItemPaint(series, item, selected);
    Stroke seriesStroke = getItemStroke(series, item, selected);
    g2.setPaint(paint);
    g2.setStroke(seriesStroke);
    double x1 = dataset.getXValue(series, item);
    double y1 = dataset.getYValue(series, item);
    double x = x1;
    double y = Double.isNaN(y1) ? getRangeBase() : y1;
    double transX1 = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
    double transY1 = rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
    transY1 = restrictValueToDataArea(transY1, plot, dataArea);
    if(this.pArea == null && !Double.isNaN(y1)) {
      this.pArea = new Polygon();
      double transY2 = rangeAxis.valueToJava2D(getRangeBase(), dataArea, plot.getRangeAxisEdge());
      transY2 = restrictValueToDataArea(transY2, plot, dataArea);
      if(orientation == PlotOrientation.VERTICAL) {
        this.pArea.addPoint((int)transX1, (int)transY2);
      }
      else 
        if(orientation == PlotOrientation.HORIZONTAL) {
          this.pArea.addPoint((int)transY2, (int)transX1);
        }
    }
    double transX0 = 0;
    double transY0 = restrictValueToDataArea(getRangeBase(), plot, dataArea);
    double x0;
    double y0;
    if(item > 0) {
      x0 = dataset.getXValue(series, item - 1);
      y0 = Double.isNaN(y1) ? y1 : dataset.getYValue(series, item - 1);
      x = x0;
      y = Double.isNaN(y0) ? getRangeBase() : y0;
      transX0 = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
      transY0 = rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
      transY0 = restrictValueToDataArea(transY0, plot, dataArea);
      if(Double.isNaN(y1)) {
        transX1 = transX0;
        transY0 = transY1;
      }
      if(transY0 != transY1) {
        if(orientation == PlotOrientation.VERTICAL) {
          this.pArea.addPoint((int)transX1, (int)transY0);
        }
        else 
          if(orientation == PlotOrientation.HORIZONTAL) {
            this.pArea.addPoint((int)transY0, (int)transX1);
          }
      }
    }
    Shape shape = null;
    if(!Double.isNaN(y1)) {
      if(orientation == PlotOrientation.VERTICAL) {
        this.pArea.addPoint((int)transX1, (int)transY1);
      }
      else 
        if(orientation == PlotOrientation.HORIZONTAL) {
          this.pArea.addPoint((int)transY1, (int)transX1);
        }
      if(getShapesVisible()) {
        shape = getItemShape(series, item, selected);
        if(orientation == PlotOrientation.VERTICAL) {
          shape = ShapeUtilities.createTranslatedShape(shape, transX1, transY1);
        }
        else 
          if(orientation == PlotOrientation.HORIZONTAL) {
            shape = ShapeUtilities.createTranslatedShape(shape, transY1, transX1);
          }
        if(isShapesFilled()) {
          g2.fill(shape);
        }
        else {
          g2.draw(shape);
        }
      }
      else {
        if(orientation == PlotOrientation.VERTICAL) {
          shape = new Rectangle2D.Double(transX1 - 2, transY1 - 2, 4.0D, 4.0D);
        }
        else 
          if(orientation == PlotOrientation.HORIZONTAL) {
            shape = new Rectangle2D.Double(transY1 - 2, transX1 - 2, 4.0D, 4.0D);
          }
      }
    }
    if(getPlotArea() && item > 0 && this.pArea != null && (item == (itemCount - 1) || Double.isNaN(y1))) {
      double transY2 = rangeAxis.valueToJava2D(getRangeBase(), dataArea, plot.getRangeAxisEdge());
      transY2 = restrictValueToDataArea(transY2, plot, dataArea);
      if(orientation == PlotOrientation.VERTICAL) {
        this.pArea.addPoint((int)transX1, (int)transY2);
      }
      else 
        if(orientation == PlotOrientation.HORIZONTAL) {
          this.pArea.addPoint((int)transY2, (int)transX1);
        }
      g2.fill(this.pArea);
      if(isOutline()) {
        g2.setStroke(plot.getOutlineStroke());
        g2.setPaint(plot.getOutlinePaint());
        g2.draw(this.pArea);
      }
      this.pArea = null;
    }
    if(!Double.isNaN(y1)) {
      int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
      int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
      XYCrosshairState crosshairState = state.getCrosshairState();
      updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation);
    }
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, shape, dataset, series, item, selected, transX1, transY1);
    }
  }
  public void setOutline(boolean show) {
    this.showOutline = show;
    fireChangeEvent();
  }
  public void setPlotArea(boolean flag) {
    this.plotArea = flag;
    fireChangeEvent();
  }
  public void setRangeBase(double val) {
    this.rangeBase = val;
    fireChangeEvent();
  }
  public void setShapesFilled(boolean filled) {
    this.shapesFilled = filled;
    fireChangeEvent();
  }
  public void setShapesVisible(boolean flag) {
    this.shapesVisible = flag;
    fireChangeEvent();
  }
}