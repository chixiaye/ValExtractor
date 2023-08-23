package org.jfree.chart.renderer.xy;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYCrosshairState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

public class XYBubbleRenderer extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable  {
  final public static long serialVersionUID = -5221991598674249125L;
  final public static int SCALE_ON_BOTH_AXES = 0;
  final public static int SCALE_ON_DOMAIN_AXIS = 1;
  final public static int SCALE_ON_RANGE_AXIS = 2;
  private int scaleType;
  public XYBubbleRenderer() {
    this(SCALE_ON_BOTH_AXES);
  }
  public XYBubbleRenderer(int scaleType) {
    super();
    if(scaleType < 0 || scaleType > 2) {
      throw new IllegalArgumentException("Invalid \'scaleType\'.");
    }
    this.scaleType = scaleType;
    setBaseLegendShape(new Ellipse2D.Double(-4.0D, -4.0D, 8.0D, 8.0D));
  }
  public LegendItem getLegendItem(int datasetIndex, int series) {
    LegendItem result = null;
    XYPlot plot = getPlot();
    if(plot == null) {
      return null;
    }
    XYDataset dataset = plot.getDataset(datasetIndex);
    if(dataset != null) {
      if(getItemVisible(series, 0)) {
        String label = getLegendItemLabelGenerator().generateLabel(dataset, series);
        String description = label;
        String toolTipText = null;
        if(getLegendItemToolTipGenerator() != null) {
          toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset, series);
        }
        String urlText = null;
        if(getLegendItemURLGenerator() != null) {
          urlText = getLegendItemURLGenerator().generateLabel(dataset, series);
        }
        Shape shape = lookupLegendShape(series);
        Paint paint = lookupSeriesPaint(series);
        Paint outlinePaint = lookupSeriesOutlinePaint(series);
        Stroke outlineStroke = lookupSeriesOutlineStroke(series);
        result = new LegendItem(label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint);
        result.setLabelFont(lookupLegendTextFont(series));
        Paint labelPaint = lookupLegendTextPaint(series);
        if(labelPaint != null) {
          result.setLabelPaint(labelPaint);
        }
        result.setDataset(dataset);
        result.setDatasetIndex(datasetIndex);
        result.setSeriesKey(dataset.getSeriesKey(series));
        result.setSeriesIndex(series);
      }
    }
    return result;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYBubbleRenderer)) {
      return false;
    }
    XYBubbleRenderer that = (XYBubbleRenderer)obj;
    if(this.scaleType != that.scaleType) {
      return false;
    }
    return super.equals(obj);
  }
  public int getScaleType() {
    return this.scaleType;
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    if(!getItemVisible(series, item)) {
      return ;
    }
    PlotOrientation orientation = plot.getOrientation();
    double x = dataset.getXValue(series, item);
    double y = dataset.getYValue(series, item);
    double z = Double.NaN;
    if(dataset instanceof XYZDataset) {
      XYZDataset xyzData = (XYZDataset)dataset;
      z = xyzData.getZValue(series, item);
    }
    if(!Double.isNaN(z)) {
      RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
      RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
      double transX = domainAxis.valueToJava2D(x, dataArea, domainAxisLocation);
      double transY = rangeAxis.valueToJava2D(y, dataArea, rangeAxisLocation);
      double transDomain = 0.0D;
      double transRange = 0.0D;
      double zero;
      switch (getScaleType()){
        case SCALE_ON_DOMAIN_AXIS:
        zero = domainAxis.valueToJava2D(0.0D, dataArea, domainAxisLocation);
        transDomain = domainAxis.valueToJava2D(z, dataArea, domainAxisLocation) - zero;
        transRange = transDomain;
        break ;
        case SCALE_ON_RANGE_AXIS:
        double var_2894 = rangeAxis.valueToJava2D(0.0D, dataArea, rangeAxisLocation);
        zero = var_2894;
        transRange = zero - rangeAxis.valueToJava2D(z, dataArea, rangeAxisLocation);
        transDomain = transRange;
        break ;
        default:
        double zero1 = domainAxis.valueToJava2D(0.0D, dataArea, domainAxisLocation);
        double zero2 = rangeAxis.valueToJava2D(0.0D, dataArea, rangeAxisLocation);
        transDomain = domainAxis.valueToJava2D(z, dataArea, domainAxisLocation) - zero1;
        transRange = zero2 - rangeAxis.valueToJava2D(z, dataArea, rangeAxisLocation);
      }
      transDomain = Math.abs(transDomain);
      transRange = Math.abs(transRange);
      Ellipse2D circle = null;
      if(orientation == PlotOrientation.VERTICAL) {
        circle = new Ellipse2D.Double(transX - transDomain / 2.0D, transY - transRange / 2.0D, transDomain, transRange);
      }
      else 
        if(orientation == PlotOrientation.HORIZONTAL) {
          circle = new Ellipse2D.Double(transY - transRange / 2.0D, transX - transDomain / 2.0D, transRange, transDomain);
        }
      g2.setPaint(getItemPaint(series, item, selected));
      g2.fill(circle);
      g2.setStroke(getItemOutlineStroke(series, item, selected));
      g2.setPaint(getItemOutlinePaint(series, item, selected));
      g2.draw(circle);
      if(isItemLabelVisible(series, item, selected)) {
        if(orientation == PlotOrientation.VERTICAL) {
          drawItemLabel(g2, orientation, dataset, series, item, selected, transX, transY, false);
        }
        else 
          if(orientation == PlotOrientation.HORIZONTAL) {
            drawItemLabel(g2, orientation, dataset, series, item, selected, transY, transX, false);
          }
      }
      EntityCollection entities = null;
      if(state.getInfo() != null) {
        entities = state.getInfo().getOwner().getEntityCollection();
        if(entities != null && circle.intersects(dataArea)) {
          addEntity(entities, circle, dataset, series, item, selected, circle.getCenterX(), circle.getCenterY());
        }
      }
      int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
      int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
      XYCrosshairState crosshairState = state.getCrosshairState();
      updateCrosshairValues(crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY, orientation);
    }
  }
}