package org.jfree.chart.renderer.xy;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Stack;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYCrosshairState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;

public class StackedXYAreaRenderer extends XYAreaRenderer implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 5217394318178570889L;
  private transient Paint shapePaint = null;
  private transient Stroke shapeStroke = null;
  public StackedXYAreaRenderer() {
    this(AREA);
  }
  public StackedXYAreaRenderer(int type) {
    this(type, null, null);
  }
  public StackedXYAreaRenderer(int type, XYToolTipGenerator labelGenerator, XYURLGenerator urlGenerator) {
    super(type, labelGenerator, urlGenerator);
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Paint getShapePaint() {
    return this.shapePaint;
  }
  public Range findRangeBounds(XYDataset dataset) {
    if(dataset != null) {
      return DatasetUtilities.findStackedRangeBounds((TableXYDataset)dataset);
    }
    else {
      return null;
    }
  }
  public Stroke getShapeStroke() {
    return this.shapeStroke;
  }
  public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
    XYItemRendererState state = new StackedXYAreaRendererState(info);
    state.setProcessVisibleItemsOnly(false);
    return state;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof StackedXYAreaRenderer) || !super.equals(obj)) {
      return false;
    }
    StackedXYAreaRenderer that = (StackedXYAreaRenderer)obj;
    if(!PaintUtilities.equal(this.shapePaint, that.shapePaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.shapeStroke, that.shapeStroke)) {
      return false;
    }
    return true;
  }
  protected double getPreviousHeight(TableXYDataset dataset, int series, int index) {
    double result = 0.0D;
    for(int i = 0; i < series; i++) {
      double value = dataset.getYValue(i, index);
      if(!Double.isNaN(value)) {
        result += value;
      }
    }
    return result;
  }
  public int getPassCount() {
    return 2;
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    PlotOrientation orientation = plot.getOrientation();
    StackedXYAreaRendererState areaState = (StackedXYAreaRendererState)state;
    TableXYDataset tdataset = (TableXYDataset)dataset;
    int itemCount = tdataset.getItemCount();
    double x1 = dataset.getXValue(series, item);
    double y1 = dataset.getYValue(series, item);
    boolean nullPoint = false;
    if(Double.isNaN(y1)) {
      y1 = 0.0D;
      nullPoint = true;
    }
    double ph1 = getPreviousHeight(tdataset, series, item);
    double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
    org.jfree.chart.util.RectangleEdge var_3013 = plot.getRangeAxisEdge();
    double transY1 = rangeAxis.valueToJava2D(y1 + ph1, dataArea, var_3013);
    Paint seriesPaint = getItemPaint(series, item, selected);
    Stroke seriesStroke = getItemStroke(series, item, selected);
    if(pass == 0) {
      if(item == 0) {
        areaState.setSeriesArea(new Polygon());
        areaState.setLastSeriesPoints(areaState.getCurrentSeriesPoints());
        areaState.setCurrentSeriesPoints(new Stack());
        double transY2 = rangeAxis.valueToJava2D(ph1, dataArea, plot.getRangeAxisEdge());
        if(orientation == PlotOrientation.VERTICAL) {
          areaState.getSeriesArea().addPoint((int)transX1, (int)transY2);
        }
        else 
          if(orientation == PlotOrientation.HORIZONTAL) {
            areaState.getSeriesArea().addPoint((int)transY2, (int)transX1);
          }
      }
      if(orientation == PlotOrientation.VERTICAL) {
        Point point = new Point((int)transX1, (int)transY1);
        areaState.getSeriesArea().addPoint((int)point.getX(), (int)point.getY());
        areaState.getCurrentSeriesPoints().push(point);
      }
      else 
        if(orientation == PlotOrientation.HORIZONTAL) {
          areaState.getSeriesArea().addPoint((int)transY1, (int)transX1);
        }
      if(getPlotLines()) {
        if(item > 0) {
          double x0 = dataset.getXValue(series, item - 1);
          double y0 = dataset.getYValue(series, item - 1);
          double ph0 = getPreviousHeight(tdataset, series, item - 1);
          double transX0 = domainAxis.valueToJava2D(x0, dataArea, plot.getDomainAxisEdge());
          double transY0 = rangeAxis.valueToJava2D(y0 + ph0, dataArea, plot.getRangeAxisEdge());
          if(orientation == PlotOrientation.VERTICAL) {
            areaState.getLine().setLine(transX0, transY0, transX1, transY1);
          }
          else 
            if(orientation == PlotOrientation.HORIZONTAL) {
              areaState.getLine().setLine(transY0, transX0, transY1, transX1);
            }
          g2.draw(areaState.getLine());
        }
      }
      if(getPlotArea() && item > 0 && item == (itemCount - 1)) {
        double transY2 = rangeAxis.valueToJava2D(ph1, dataArea, plot.getRangeAxisEdge());
        if(orientation == PlotOrientation.VERTICAL) {
          areaState.getSeriesArea().addPoint((int)transX1, (int)transY2);
        }
        else 
          if(orientation == PlotOrientation.HORIZONTAL) {
            areaState.getSeriesArea().addPoint((int)transY2, (int)transX1);
          }
        if(series != 0) {
          Stack points = areaState.getLastSeriesPoints();
          while(!points.empty()){
            Point point = (Point)points.pop();
            areaState.getSeriesArea().addPoint((int)point.getX(), (int)point.getY());
          }
        }
        g2.setPaint(seriesPaint);
        g2.setStroke(seriesStroke);
        g2.fill(areaState.getSeriesArea());
        if(isOutline()) {
          g2.setStroke(lookupSeriesOutlineStroke(series));
          g2.setPaint(lookupSeriesOutlinePaint(series));
          g2.draw(areaState.getSeriesArea());
        }
      }
      int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
      int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
      XYCrosshairState crosshairState = state.getCrosshairState();
      updateCrosshairValues(crosshairState, x1, ph1 + y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation);
    }
    else 
      if(pass == 1) {
        Shape shape = null;
        if(getPlotShapes()) {
          shape = getItemShape(series, item, selected);
          if(plot.getOrientation() == PlotOrientation.VERTICAL) {
            shape = ShapeUtilities.createTranslatedShape(shape, transX1, transY1);
          }
          else 
            if(plot.getOrientation() == PlotOrientation.HORIZONTAL) {
              shape = ShapeUtilities.createTranslatedShape(shape, transY1, transX1);
            }
          if(!nullPoint) {
            if(getShapePaint() != null) {
              g2.setPaint(getShapePaint());
            }
            else {
              g2.setPaint(seriesPaint);
            }
            if(getShapeStroke() != null) {
              g2.setStroke(getShapeStroke());
            }
            else {
              g2.setStroke(seriesStroke);
            }
            g2.draw(shape);
          }
        }
        else {
          if(plot.getOrientation() == PlotOrientation.VERTICAL) {
            shape = new Rectangle2D.Double(transX1 - 3, transY1 - 3, 6.0D, 6.0D);
          }
          else 
            if(plot.getOrientation() == PlotOrientation.HORIZONTAL) {
              shape = new Rectangle2D.Double(transY1 - 3, transX1 - 3, 6.0D, 6.0D);
            }
        }
        EntityCollection entities = state.getEntityCollection();
        if(entities != null) {
          addEntity(entities, shape, dataset, series, item, selected, 0.0D, 0.0D);
        }
      }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.shapePaint = SerialUtilities.readPaint(stream);
    this.shapeStroke = SerialUtilities.readStroke(stream);
  }
  public void setShapePaint(Paint shapePaint) {
    this.shapePaint = shapePaint;
    fireChangeEvent();
  }
  public void setShapeStroke(Stroke shapeStroke) {
    this.shapeStroke = shapeStroke;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.shapePaint, stream);
    SerialUtilities.writeStroke(this.shapeStroke, stream);
  }
  
  static class StackedXYAreaRendererState extends XYItemRendererState  {
    private Polygon seriesArea;
    private Line2D line;
    private Stack lastSeriesPoints;
    private Stack currentSeriesPoints;
    public StackedXYAreaRendererState(PlotRenderingInfo info) {
      super(info);
      this.seriesArea = null;
      this.line = new Line2D.Double();
      this.lastSeriesPoints = new Stack();
      this.currentSeriesPoints = new Stack();
    }
    public Line2D getLine() {
      return this.line;
    }
    public Polygon getSeriesArea() {
      return this.seriesArea;
    }
    public Stack getCurrentSeriesPoints() {
      return this.currentSeriesPoints;
    }
    public Stack getLastSeriesPoints() {
      return this.lastSeriesPoints;
    }
    public void setCurrentSeriesPoints(Stack points) {
      this.currentSeriesPoints = points;
    }
    public void setLastSeriesPoints(Stack points) {
      this.lastSeriesPoints = points;
    }
    public void setSeriesArea(Polygon area) {
      this.seriesArea = area;
    }
  }
}