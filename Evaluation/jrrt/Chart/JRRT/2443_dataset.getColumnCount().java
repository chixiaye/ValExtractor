package org.jfree.chart.renderer.category;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.util.ObjectList;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.category.CategoryDataset;

public class LayeredBarRenderer extends BarRenderer implements Serializable  {
  final private static long serialVersionUID = -8716572894780469487L;
  protected ObjectList seriesBarWidthList;
  public LayeredBarRenderer() {
    super();
    this.seriesBarWidthList = new ObjectList();
  }
  public double getSeriesBarWidth(int series) {
    double result = Double.NaN;
    Number n = (Number)this.seriesBarWidthList.get(series);
    if(n != null) {
      result = n.doubleValue();
    }
    return result;
  }
  protected void calculateBarWidth(CategoryPlot plot, Rectangle2D dataArea, CategoryDataset dataset, CategoryItemRendererState state) {
    CategoryAxis domainAxis = getDomainAxis(plot, dataset);
    if(dataset != null) {
      int var_2443 = dataset.getColumnCount();
      int columns = var_2443;
      int rows = dataset.getRowCount();
      double space = 0.0D;
      PlotOrientation orientation = plot.getOrientation();
      if(orientation == PlotOrientation.HORIZONTAL) {
        space = dataArea.getHeight();
      }
      else 
        if(orientation == PlotOrientation.VERTICAL) {
          space = dataArea.getWidth();
        }
      double maxWidth = space * getMaximumBarWidth();
      double categoryMargin = 0.0D;
      if(columns > 1) {
        categoryMargin = domainAxis.getCategoryMargin();
      }
      double used = space * (1 - domainAxis.getLowerMargin() - domainAxis.getUpperMargin() - categoryMargin);
      if((rows * columns) > 0) {
        state.setBarWidth(Math.min(used / (dataset.getColumnCount()), maxWidth));
      }
      else {
        state.setBarWidth(Math.min(used, maxWidth));
      }
    }
  }
  protected void drawHorizontalItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected) {
    Number dataValue = dataset.getValue(row, column);
    if(dataValue == null) {
      return ;
    }
    double value = dataValue.doubleValue();
    double base = 0.0D;
    double lclip = rangeAxis.getLowerBound();
    double uclip = rangeAxis.getUpperBound();
    if(uclip <= 0.0D) {
      if(value >= uclip) {
        return ;
      }
      base = uclip;
      if(value <= lclip) {
        value = lclip;
      }
    }
    else 
      if(lclip <= 0.0D) {
        if(value >= uclip) {
          value = uclip;
        }
        else {
          if(value <= lclip) {
            value = lclip;
          }
        }
      }
      else {
        if(value <= lclip) {
          return ;
        }
        base = lclip;
        if(value >= uclip) {
          value = uclip;
        }
      }
    RectangleEdge edge = plot.getRangeAxisEdge();
    double transX1 = rangeAxis.valueToJava2D(base, dataArea, edge);
    double transX2 = rangeAxis.valueToJava2D(value, dataArea, edge);
    double rectX = Math.min(transX1, transX2);
    double rectWidth = Math.abs(transX2 - transX1);
    double rectY = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0D;
    int seriesCount = getRowCount();
    double shift = 0.0D;
    double rectHeight = 0.0D;
    double widthFactor = 1.0D;
    double seriesBarWidth = getSeriesBarWidth(row);
    if(!Double.isNaN(seriesBarWidth)) {
      widthFactor = seriesBarWidth;
    }
    rectHeight = widthFactor * state.getBarWidth();
    rectY = rectY + (1 - widthFactor) * state.getBarWidth() / 2.0D;
    if(seriesCount > 1) {
      shift = rectHeight * 0.20D / (seriesCount - 1);
    }
    Rectangle2D bar = new Rectangle2D.Double(rectX, (rectY + ((seriesCount - 1 - row) * shift)), rectWidth, (rectHeight - (seriesCount - 1 - row) * shift * 2));
    Paint itemPaint = getItemPaint(row, column, selected);
    GradientPaintTransformer t = getGradientPaintTransformer();
    if(t != null && itemPaint instanceof GradientPaint) {
      itemPaint = t.transform((GradientPaint)itemPaint, bar);
    }
    g2.setPaint(itemPaint);
    g2.fill(bar);
    if(isDrawBarOutline() && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
      Stroke stroke = getItemOutlineStroke(row, column, selected);
      Paint paint = getItemOutlinePaint(row, column, selected);
      if(stroke != null && paint != null) {
        g2.setStroke(stroke);
        g2.setPaint(paint);
        g2.draw(bar);
      }
    }
    CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column, selected);
    if(generator != null && isItemLabelVisible(row, column, selected)) {
      drawItemLabelForBar(g2, plot, dataset, row, column, selected, generator, bar, (transX1 > transX2));
    }
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, bar, dataset, row, column, selected);
    }
  }
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, int pass) {
    PlotOrientation orientation = plot.getOrientation();
    if(orientation == PlotOrientation.HORIZONTAL) {
      drawHorizontalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, selected);
    }
    else 
      if(orientation == PlotOrientation.VERTICAL) {
        drawVerticalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, selected);
      }
  }
  protected void drawVerticalItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected) {
    Number dataValue = dataset.getValue(row, column);
    if(dataValue == null) {
      return ;
    }
    double rectX = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0D;
    int seriesCount = getRowCount();
    double value = dataValue.doubleValue();
    double base = 0.0D;
    double lclip = rangeAxis.getLowerBound();
    double uclip = rangeAxis.getUpperBound();
    if(uclip <= 0.0D) {
      if(value >= uclip) {
        return ;
      }
      base = uclip;
      if(value <= lclip) {
        value = lclip;
      }
    }
    else 
      if(lclip <= 0.0D) {
        if(value >= uclip) {
          value = uclip;
        }
        else {
          if(value <= lclip) {
            value = lclip;
          }
        }
      }
      else {
        if(value <= lclip) {
          return ;
        }
        base = rangeAxis.getLowerBound();
        if(value >= uclip) {
          value = uclip;
        }
      }
    RectangleEdge edge = plot.getRangeAxisEdge();
    double transY1 = rangeAxis.valueToJava2D(base, dataArea, edge);
    double transY2 = rangeAxis.valueToJava2D(value, dataArea, edge);
    double rectY = Math.min(transY2, transY1);
    double rectWidth = state.getBarWidth();
    double rectHeight = Math.abs(transY2 - transY1);
    double shift = 0.0D;
    rectWidth = 0.0D;
    double widthFactor = 1.0D;
    double seriesBarWidth = getSeriesBarWidth(row);
    if(!Double.isNaN(seriesBarWidth)) {
      widthFactor = seriesBarWidth;
    }
    rectWidth = widthFactor * state.getBarWidth();
    rectX = rectX + (1 - widthFactor) * state.getBarWidth() / 2.0D;
    if(seriesCount > 1) {
      shift = rectWidth * 0.20D / (seriesCount - 1);
    }
    Rectangle2D bar = new Rectangle2D.Double((rectX + ((seriesCount - 1 - row) * shift)), rectY, (rectWidth - (seriesCount - 1 - row) * shift * 2), rectHeight);
    Paint itemPaint = getItemPaint(row, column, selected);
    GradientPaintTransformer t = getGradientPaintTransformer();
    if(t != null && itemPaint instanceof GradientPaint) {
      itemPaint = t.transform((GradientPaint)itemPaint, bar);
    }
    g2.setPaint(itemPaint);
    g2.fill(bar);
    if(isDrawBarOutline() && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
      Stroke stroke = getItemOutlineStroke(row, column, selected);
      Paint paint = getItemOutlinePaint(row, column, selected);
      if(stroke != null && paint != null) {
        g2.setStroke(stroke);
        g2.setPaint(paint);
        g2.draw(bar);
      }
    }
    double transX1 = rangeAxis.valueToJava2D(base, dataArea, edge);
    double transX2 = rangeAxis.valueToJava2D(value, dataArea, edge);
    CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column, selected);
    if(generator != null && isItemLabelVisible(row, column, selected)) {
      drawItemLabelForBar(g2, plot, dataset, row, column, selected, generator, bar, (transX1 > transX2));
    }
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, bar, dataset, row, column, selected);
    }
  }
  public void setSeriesBarWidth(int series, double width) {
    this.seriesBarWidthList.set(series, new Double(width));
  }
}