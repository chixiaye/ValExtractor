package org.jfree.chart.renderer.category;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.category.CategoryDataset;

public class LevelRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -8204856624355025117L;
  final public static double DEFAULT_ITEM_MARGIN = 0.20D;
  private double itemMargin;
  private double maxItemWidth;
  public LevelRenderer() {
    super();
    this.itemMargin = DEFAULT_ITEM_MARGIN;
    this.maxItemWidth = 1.0D;
    setBaseLegendShape(new Rectangle2D.Float(-5.0F, -1.0F, 10.0F, 2.0F));
    setBaseOutlinePaint(new Color(0, 0, 0, 0));
  }
  public CategoryItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, CategoryPlot plot, CategoryDataset dataset, PlotRenderingInfo info) {
    CategoryItemRendererState state = super.initialise(g2, dataArea, plot, dataset, info);
    calculateItemWidth(plot, dataArea, dataset, state);
    return state;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof LevelRenderer)) {
      return false;
    }
    LevelRenderer that = (LevelRenderer)obj;
    if(this.itemMargin != that.itemMargin) {
      return false;
    }
    if(this.maxItemWidth != that.maxItemWidth) {
      return false;
    }
    return super.equals(obj);
  }
  protected double calculateBarW0(CategoryPlot plot, PlotOrientation orientation, Rectangle2D dataArea, CategoryAxis domainAxis, CategoryItemRendererState state, int row, int column) {
    double space = 0.0D;
    if(orientation == PlotOrientation.HORIZONTAL) {
      space = dataArea.getHeight();
    }
    else {
      space = dataArea.getWidth();
    }
    double barW0 = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
    int seriesCount = state.getVisibleSeriesCount();
    if(seriesCount < 0) {
      seriesCount = getRowCount();
    }
    int var_2441 = getColumnCount();
    int categoryCount = var_2441;
    if(seriesCount > 1) {
      double seriesGap = space * getItemMargin() / (categoryCount * (seriesCount - 1));
      double seriesW = calculateSeriesWidth(space, domainAxis, categoryCount, seriesCount);
      barW0 = barW0 + row * (seriesW + seriesGap) + (seriesW / 2.0D) - (state.getBarWidth() / 2.0D);
    }
    else {
      barW0 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0D;
    }
    return barW0;
  }
  protected double calculateSeriesWidth(double space, CategoryAxis axis, int categories, int series) {
    double factor = 1.0D - getItemMargin() - axis.getLowerMargin() - axis.getUpperMargin();
    if(categories > 1) {
      factor = factor - axis.getCategoryMargin();
    }
    return (space * factor) / (categories * series);
  }
  public double getItemMargin() {
    return this.itemMargin;
  }
  public double getItemMiddle(Comparable rowKey, Comparable columnKey, CategoryDataset dataset, CategoryAxis axis, Rectangle2D area, RectangleEdge edge) {
    return axis.getCategorySeriesMiddle(columnKey, rowKey, dataset, this.itemMargin, area, edge);
  }
  public double getMaximumItemWidth() {
    return this.maxItemWidth;
  }
  public int hashCode() {
    int hash = super.hashCode();
    hash = HashUtilities.hashCode(hash, this.itemMargin);
    hash = HashUtilities.hashCode(hash, this.maxItemWidth);
    return hash;
  }
  protected void calculateItemWidth(CategoryPlot plot, Rectangle2D dataArea, CategoryDataset dataset, CategoryItemRendererState state) {
    CategoryAxis domainAxis = getDomainAxis(plot, dataset);
    if(dataset != null) {
      int columns = dataset.getColumnCount();
      int rows = state.getVisibleSeriesCount() >= 0 ? state.getVisibleSeriesCount() : dataset.getRowCount();
      double space = 0.0D;
      PlotOrientation orientation = plot.getOrientation();
      if(orientation == PlotOrientation.HORIZONTAL) {
        space = dataArea.getHeight();
      }
      else 
        if(orientation == PlotOrientation.VERTICAL) {
          space = dataArea.getWidth();
        }
      double maxWidth = space * getMaximumItemWidth();
      double categoryMargin = 0.0D;
      double currentItemMargin = 0.0D;
      if(columns > 1) {
        categoryMargin = domainAxis.getCategoryMargin();
      }
      if(rows > 1) {
        currentItemMargin = getItemMargin();
      }
      double used = space * (1 - domainAxis.getLowerMargin() - domainAxis.getUpperMargin() - categoryMargin - currentItemMargin);
      if((rows * columns) > 0) {
        state.setBarWidth(Math.min(used / (rows * columns), maxWidth));
      }
      else {
        state.setBarWidth(Math.min(used, maxWidth));
      }
    }
  }
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, int pass) {
    int visibleRow = state.getVisibleSeriesIndex(row);
    if(visibleRow < 0) {
      return ;
    }
    Number dataValue = dataset.getValue(row, column);
    if(dataValue == null) {
      return ;
    }
    double value = dataValue.doubleValue();
    PlotOrientation orientation = plot.getOrientation();
    double barW0 = calculateBarW0(plot, orientation, dataArea, domainAxis, state, visibleRow, column);
    RectangleEdge edge = plot.getRangeAxisEdge();
    double barL = rangeAxis.valueToJava2D(value, dataArea, edge);
    Line2D line = null;
    double x = 0.0D;
    double y = 0.0D;
    if(orientation == PlotOrientation.HORIZONTAL) {
      x = barL;
      y = barW0 + state.getBarWidth() / 2.0D;
      line = new Line2D.Double(barL, barW0, barL, barW0 + state.getBarWidth());
    }
    else {
      x = barW0 + state.getBarWidth() / 2.0D;
      y = barL;
      line = new Line2D.Double(barW0, barL, barW0 + state.getBarWidth(), barL);
    }
    Stroke itemStroke = getItemStroke(row, column, selected);
    Paint itemPaint = getItemPaint(row, column, selected);
    g2.setStroke(itemStroke);
    g2.setPaint(itemPaint);
    g2.draw(line);
    CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column, selected);
    if(generator != null && isItemLabelVisible(row, column, selected)) {
      drawItemLabel(g2, orientation, dataset, row, column, selected, x, y, (value < 0.0D));
    }
    int datasetIndex = plot.indexOf(dataset);
    updateCrosshairValues(state.getCrosshairState(), dataset.getRowKey(row), dataset.getColumnKey(column), value, datasetIndex, barW0, barL, orientation);
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, line.getBounds(), dataset, row, column, selected);
    }
  }
  public void setItemMargin(double percent) {
    this.itemMargin = percent;
    fireChangeEvent();
  }
  public void setMaximumItemWidth(double percent) {
    this.maxItemWidth = percent;
    fireChangeEvent();
  }
}