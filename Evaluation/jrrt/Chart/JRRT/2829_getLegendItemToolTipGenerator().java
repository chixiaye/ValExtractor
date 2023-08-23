package org.jfree.chart.renderer.xy;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYCrosshairState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.data.xy.XYDataset;

public class XYAreaRenderer2 extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable  {
  final private static long serialVersionUID = -7378069681579984133L;
  private boolean showOutline;
  private transient Shape legendArea;
  public XYAreaRenderer2() {
    this(null, null);
  }
  public XYAreaRenderer2(XYToolTipGenerator labelGenerator, XYURLGenerator urlGenerator) {
    super();
    this.showOutline = false;
    setBaseToolTipGenerator(labelGenerator);
    setBaseURLGenerator(urlGenerator);
    GeneralPath area = new GeneralPath();
    area.moveTo(0.0F, -4.0F);
    area.lineTo(3.0F, -2.0F);
    area.lineTo(4.0F, 4.0F);
    area.lineTo(-4.0F, 4.0F);
    area.lineTo(-3.0F, -2.0F);
    area.closePath();
    this.legendArea = area;
  }
  public LegendItem getLegendItem(int datasetIndex, int series) {
    LegendItem result = null;
    XYPlot xyplot = getPlot();
    if(xyplot != null) {
      XYDataset dataset = xyplot.getDataset(datasetIndex);
      if(dataset != null) {
        XYSeriesLabelGenerator lg = getLegendItemLabelGenerator();
        String label = lg.generateLabel(dataset, series);
        String description = label;
        String toolTipText = null;
        XYSeriesLabelGenerator var_2829 = getLegendItemToolTipGenerator();
        if(var_2829 != null) {
          toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset, series);
        }
        String urlText = null;
        if(getLegendItemURLGenerator() != null) {
          urlText = getLegendItemURLGenerator().generateLabel(dataset, series);
        }
        Paint paint = lookupSeriesPaint(series);
        result = new LegendItem(label, description, toolTipText, urlText, this.legendArea, paint);
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
    XYAreaRenderer2 clone = (XYAreaRenderer2)super.clone();
    clone.legendArea = ShapeUtilities.clone(this.legendArea);
    return clone;
  }
  public Shape getLegendArea() {
    return this.legendArea;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYAreaRenderer2)) {
      return false;
    }
    XYAreaRenderer2 that = (XYAreaRenderer2)obj;
    if(this.showOutline != that.showOutline) {
      return false;
    }
    if(!ShapeUtilities.equal(this.legendArea, that.legendArea)) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean isOutline() {
    return this.showOutline;
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    if(!getItemVisible(series, item)) {
      return ;
    }
    double x1 = dataset.getXValue(series, item);
    double y1 = dataset.getYValue(series, item);
    if(Double.isNaN(y1)) {
      y1 = 0.0D;
    }
    double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
    double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());
    double x0 = dataset.getXValue(series, Math.max(item - 1, 0));
    double y0 = dataset.getYValue(series, Math.max(item - 1, 0));
    if(Double.isNaN(y0)) {
      y0 = 0.0D;
    }
    double transX0 = domainAxis.valueToJava2D(x0, dataArea, plot.getDomainAxisEdge());
    double transY0 = rangeAxis.valueToJava2D(y0, dataArea, plot.getRangeAxisEdge());
    int itemCount = dataset.getItemCount(series);
    double x2 = dataset.getXValue(series, Math.min(item + 1, itemCount - 1));
    double y2 = dataset.getYValue(series, Math.min(item + 1, itemCount - 1));
    if(Double.isNaN(y2)) {
      y2 = 0.0D;
    }
    double transX2 = domainAxis.valueToJava2D(x2, dataArea, plot.getDomainAxisEdge());
    double transY2 = rangeAxis.valueToJava2D(y2, dataArea, plot.getRangeAxisEdge());
    double transZero = rangeAxis.valueToJava2D(0.0D, dataArea, plot.getRangeAxisEdge());
    Polygon hotspot = null;
    if(plot.getOrientation() == PlotOrientation.HORIZONTAL) {
      hotspot = new Polygon();
      hotspot.addPoint((int)transZero, (int)((transX0 + transX1) / 2.0D));
      hotspot.addPoint((int)((transY0 + transY1) / 2.0D), (int)((transX0 + transX1) / 2.0D));
      hotspot.addPoint((int)transY1, (int)transX1);
      hotspot.addPoint((int)((transY1 + transY2) / 2.0D), (int)((transX1 + transX2) / 2.0D));
      hotspot.addPoint((int)transZero, (int)((transX1 + transX2) / 2.0D));
    }
    else {
      hotspot = new Polygon();
      hotspot.addPoint((int)((transX0 + transX1) / 2.0D), (int)transZero);
      hotspot.addPoint((int)((transX0 + transX1) / 2.0D), (int)((transY0 + transY1) / 2.0D));
      hotspot.addPoint((int)transX1, (int)transY1);
      hotspot.addPoint((int)((transX1 + transX2) / 2.0D), (int)((transY1 + transY2) / 2.0D));
      hotspot.addPoint((int)((transX1 + transX2) / 2.0D), (int)transZero);
    }
    PlotOrientation orientation = plot.getOrientation();
    Paint paint = getItemPaint(series, item, selected);
    Stroke stroke = getItemStroke(series, item, selected);
    g2.setPaint(paint);
    g2.setStroke(stroke);
    g2.fill(hotspot);
    if(isOutline()) {
      g2.setStroke(lookupSeriesOutlineStroke(series));
      g2.setPaint(lookupSeriesOutlinePaint(series));
      g2.draw(hotspot);
    }
    int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
    int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
    XYCrosshairState crosshairState = state.getCrosshairState();
    updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation);
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, hotspot, dataset, series, item, selected, 0.0D, 0.0D);
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.legendArea = SerialUtilities.readShape(stream);
  }
  public void setLegendArea(Shape area) {
    if(area == null) {
      throw new IllegalArgumentException("Null \'area\' argument.");
    }
    this.legendArea = area;
    fireChangeEvent();
  }
  public void setOutline(boolean show) {
    this.showOutline = show;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writeShape(this.legendArea, stream);
  }
}