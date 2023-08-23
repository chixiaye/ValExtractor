package org.jfree.chart.renderer.category;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.category.CategoryDataset;

public class CategoryStepRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -5121079703118261470L;
  final public static int STAGGER_WIDTH = 5;
  private boolean stagger = false;
  public CategoryStepRenderer() {
    this(false);
  }
  public CategoryStepRenderer(boolean stagger) {
    super();
    this.stagger = stagger;
    setBaseLegendShape(new Rectangle2D.Double(-4.0D, -3.0D, 8.0D, 6.0D));
  }
  protected CategoryItemRendererState createState(PlotRenderingInfo info) {
    return new State(info);
  }
  public LegendItem getLegendItem(int datasetIndex, int series) {
    CategoryPlot p = getPlot();
    if(p == null) {
      return null;
    }
    if(!isSeriesVisible(series) || !isSeriesVisibleInLegend(series)) {
      return null;
    }
    CategoryDataset dataset = p.getDataset(datasetIndex);
    String label = getLegendItemLabelGenerator().generateLabel(dataset, series);
    String description = label;
    String toolTipText = null;
    if(getLegendItemToolTipGenerator() != null) {
      toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset, series);
    }
    String urlText = null;
    org.jfree.chart.labels.CategorySeriesLabelGenerator var_2519 = getLegendItemURLGenerator();
    if(var_2519 != null) {
      urlText = getLegendItemURLGenerator().generateLabel(dataset, series);
    }
    Shape shape = lookupLegendShape(series);
    Paint paint = lookupSeriesPaint(series);
    LegendItem item = new LegendItem(label, description, toolTipText, urlText, shape, paint);
    item.setLabelFont(lookupLegendTextFont(series));
    Paint labelPaint = lookupLegendTextPaint(series);
    if(labelPaint != null) {
      item.setLabelPaint(labelPaint);
    }
    item.setSeriesKey(dataset.getRowKey(series));
    item.setSeriesIndex(series);
    item.setDataset(dataset);
    item.setDatasetIndex(datasetIndex);
    return item;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof CategoryStepRenderer)) {
      return false;
    }
    CategoryStepRenderer that = (CategoryStepRenderer)obj;
    if(this.stagger != that.stagger) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getStagger() {
    return this.stagger;
  }
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, int pass) {
    if(!getItemVisible(row, column)) {
      return ;
    }
    Number value = dataset.getValue(row, column);
    if(value == null) {
      return ;
    }
    PlotOrientation orientation = plot.getOrientation();
    double x1s = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
    double x1 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
    double x1e = 2 * x1 - x1s;
    double y1 = rangeAxis.valueToJava2D(value.doubleValue(), dataArea, plot.getRangeAxisEdge());
    g2.setPaint(getItemPaint(row, column, selected));
    g2.setStroke(getItemStroke(row, column, selected));
    if(column != 0) {
      Number previousValue = dataset.getValue(row, column - 1);
      if(previousValue != null) {
        double previous = previousValue.doubleValue();
        double x0s = domainAxis.getCategoryStart(column - 1, getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double x0 = domainAxis.getCategoryMiddle(column - 1, getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double x0e = 2 * x0 - x0s;
        double y0 = rangeAxis.valueToJava2D(previous, dataArea, plot.getRangeAxisEdge());
        if(getStagger()) {
          int xStagger = row * STAGGER_WIDTH;
          if(xStagger > (x1s - x0e)) {
            xStagger = (int)(x1s - x0e);
          }
          x1s = x0e + xStagger;
        }
        drawLine(g2, (State)state, orientation, x0e, y0, x1s, y0);
        drawLine(g2, (State)state, orientation, x1s, y0, x1s, y1);
      }
    }
    drawLine(g2, (State)state, orientation, x1s, y1, x1e, y1);
    if(isItemLabelVisible(row, column, selected)) {
      drawItemLabel(g2, orientation, dataset, row, column, selected, x1, y1, (value.doubleValue() < 0.0D));
    }
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      Rectangle2D hotspot = new Rectangle2D.Double();
      if(orientation == PlotOrientation.VERTICAL) {
        hotspot.setRect(x1s, y1, x1e - x1s, 4.0D);
      }
      else {
        hotspot.setRect(y1 - 2.0D, x1s, 4.0D, x1e - x1s);
      }
      addEntity(entities, hotspot, dataset, row, column, selected);
    }
  }
  protected void drawLine(Graphics2D g2, State state, PlotOrientation orientation, double x0, double y0, double x1, double y1) {
    if(orientation == PlotOrientation.VERTICAL) {
      state.line.setLine(x0, y0, x1, y1);
      g2.draw(state.line);
    }
    else 
      if(orientation == PlotOrientation.HORIZONTAL) {
        state.line.setLine(y0, x0, y1, x1);
        g2.draw(state.line);
      }
  }
  public void setStagger(boolean shouldStagger) {
    this.stagger = shouldStagger;
    fireChangeEvent();
  }
  
  protected static class State extends CategoryItemRendererState  {
    public Line2D line;
    public State(PlotRenderingInfo info) {
      super(info);
      this.line = new Line2D.Double();
    }
  }
}