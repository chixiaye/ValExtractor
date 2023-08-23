package org.jfree.chart.renderer.category;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.DataUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

public class StackedBarRenderer extends BarRenderer implements Cloneable, PublicCloneable, Serializable  {
  final static long serialVersionUID = 6402943811500067531L;
  private boolean renderAsPercentages;
  public StackedBarRenderer() {
    this(false);
  }
  public StackedBarRenderer(boolean renderAsPercentages) {
    super();
    this.renderAsPercentages = renderAsPercentages;
    ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
    setBasePositiveItemLabelPosition(p);
    setBaseNegativeItemLabelPosition(p);
    setPositiveItemLabelPositionFallback(null);
    setNegativeItemLabelPositionFallback(null);
  }
  public Range findRangeBounds(CategoryDataset dataset) {
    if(dataset == null) {
      return null;
    }
    if(this.renderAsPercentages) {
      return new Range(0.0D, 1.0D);
    }
    else {
      return DatasetUtilities.findStackedRangeBounds(dataset, getBase());
    }
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof StackedBarRenderer)) {
      return false;
    }
    StackedBarRenderer that = (StackedBarRenderer)obj;
    if(this.renderAsPercentages != that.renderAsPercentages) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getRenderAsPercentages() {
    return this.renderAsPercentages;
  }
  public int getPassCount() {
    return 3;
  }
  protected void calculateBarWidth(CategoryPlot plot, Rectangle2D dataArea, int rendererIndex, CategoryItemRendererState state) {
    CategoryAxis xAxis = plot.getDomainAxisForDataset(rendererIndex);
    CategoryDataset data = plot.getDataset(rendererIndex);
    if(data != null) {
      PlotOrientation orientation = plot.getOrientation();
      double space = 0.0D;
      if(orientation == PlotOrientation.HORIZONTAL) {
        space = dataArea.getHeight();
      }
      else 
        if(orientation == PlotOrientation.VERTICAL) {
          space = dataArea.getWidth();
        }
      double maxWidth = space * getMaximumBarWidth();
      int columns = data.getColumnCount();
      double categoryMargin = 0.0D;
      if(columns > 1) {
        categoryMargin = xAxis.getCategoryMargin();
      }
      double used = space * (1 - xAxis.getLowerMargin() - xAxis.getUpperMargin() - categoryMargin);
      if(columns > 0) {
        state.setBarWidth(Math.min(used / columns, maxWidth));
      }
      else {
        state.setBarWidth(Math.min(used, maxWidth));
      }
    }
  }
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, int pass) {
    if(!isSeriesVisible(row)) {
      return ;
    }
    Number dataValue = dataset.getValue(row, column);
    if(dataValue == null) {
      return ;
    }
    double value = dataValue.doubleValue();
    double total = 0.0D;
    if(this.renderAsPercentages) {
      total = DataUtilities.calculateColumnTotal(dataset, column, state.getVisibleSeriesArray());
      value = value / total;
    }
    PlotOrientation orientation = plot.getOrientation();
    double barW0 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0D;
    double positiveBase = getBase();
    double negativeBase = positiveBase;
    for(int i = 0; i < row; i++) {
      Number v = dataset.getValue(i, column);
      if(v != null && isSeriesVisible(i)) {
        double d = v.doubleValue();
        if(this.renderAsPercentages) {
          d = d / total;
        }
        if(d > 0) {
          positiveBase = positiveBase + d;
        }
        else {
          negativeBase = negativeBase + d;
        }
      }
    }
    double translatedBase;
    double translatedValue;
    boolean positive = (value > 0.0D);
    boolean inverted = rangeAxis.isInverted();
    RectangleEdge barBase;
    if(orientation == PlotOrientation.HORIZONTAL) {
      if(positive && inverted || !positive && !inverted) {
        barBase = RectangleEdge.RIGHT;
      }
      else {
        barBase = RectangleEdge.LEFT;
      }
    }
    else {
      if(positive && !inverted || !positive && inverted) {
        barBase = RectangleEdge.BOTTOM;
      }
      else {
        barBase = RectangleEdge.TOP;
      }
    }
    RectangleEdge location = plot.getRangeAxisEdge();
    if(positive) {
      translatedBase = rangeAxis.valueToJava2D(positiveBase, dataArea, location);
      translatedValue = rangeAxis.valueToJava2D(positiveBase + value, dataArea, location);
    }
    else {
      translatedBase = rangeAxis.valueToJava2D(negativeBase, dataArea, location);
      translatedValue = rangeAxis.valueToJava2D(negativeBase + value, dataArea, location);
    }
    double barL0 = Math.min(translatedBase, translatedValue);
    double barLength = Math.max(Math.abs(translatedValue - translatedBase), getMinimumBarLength());
    Rectangle2D bar = null;
    if(orientation == PlotOrientation.HORIZONTAL) {
      double var_2688 = state.getBarWidth();
      bar = new Rectangle2D.Double(barL0, barW0, barLength, var_2688);
    }
    else {
      bar = new Rectangle2D.Double(barW0, barL0, state.getBarWidth(), barLength);
    }
    if(pass == 0) {
      if(getShadowsVisible()) {
        boolean pegToBase = (positive && (positiveBase == getBase())) || (!positive && (negativeBase == getBase()));
        getBarPainter().paintBarShadow(g2, this, row, column, selected, bar, barBase, pegToBase);
      }
    }
    else 
      if(pass == 1) {
        getBarPainter().paintBar(g2, this, row, column, selected, bar, barBase);
        EntityCollection entities = state.getEntityCollection();
        if(entities != null) {
          addEntity(entities, bar, dataset, row, column, selected);
        }
      }
      else 
        if(pass == 2) {
          CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column, selected);
          if(generator != null && isItemLabelVisible(row, column, selected)) {
            drawItemLabelForBar(g2, plot, dataset, row, column, selected, generator, bar, (value < 0.0D));
          }
        }
  }
  public void setRenderAsPercentages(boolean asPercentages) {
    this.renderAsPercentages = asPercentages;
    fireChangeEvent();
  }
}