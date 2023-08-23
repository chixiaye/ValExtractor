package org.jfree.chart.renderer.category;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;

public class StatisticalBarRenderer extends BarRenderer implements CategoryItemRenderer, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -4986038395414039117L;
  private transient Paint errorIndicatorPaint;
  private transient Stroke errorIndicatorStroke;
  public StatisticalBarRenderer() {
    super();
    this.errorIndicatorPaint = Color.gray;
    this.errorIndicatorStroke = new BasicStroke(0.5F);
  }
  public Paint getErrorIndicatorPaint() {
    return this.errorIndicatorPaint;
  }
  public Range findRangeBounds(CategoryDataset dataset) {
    return findRangeBounds(dataset, true);
  }
  public Stroke getErrorIndicatorStroke() {
    return this.errorIndicatorStroke;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof StatisticalBarRenderer)) {
      return false;
    }
    StatisticalBarRenderer that = (StatisticalBarRenderer)obj;
    if(!PaintUtilities.equal(this.errorIndicatorPaint, that.errorIndicatorPaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.errorIndicatorStroke, that.errorIndicatorStroke)) {
      return false;
    }
    return super.equals(obj);
  }
  protected void drawHorizontalItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, StatisticalCategoryDataset dataset, int visibleRow, int row, int column, boolean selected) {
    RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
    double rectY = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, xAxisLocation);
    int seriesCount = state.getVisibleSeriesCount() >= 0 ? state.getVisibleSeriesCount() : getRowCount();
    int categoryCount = getColumnCount();
    if(seriesCount > 1) {
      double seriesGap = dataArea.getHeight() * getItemMargin() / (categoryCount * (seriesCount - 1));
      rectY = rectY + visibleRow * (state.getBarWidth() + seriesGap);
    }
    else {
      rectY = rectY + visibleRow * state.getBarWidth();
    }
    Number meanValue = dataset.getMeanValue(row, column);
    if(meanValue == null) {
      return ;
    }
    double value = meanValue.doubleValue();
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
    RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
    double transY1 = rangeAxis.valueToJava2D(base, dataArea, yAxisLocation);
    double transY2 = rangeAxis.valueToJava2D(value, dataArea, yAxisLocation);
    double rectX = Math.min(transY2, transY1);
    double rectHeight = state.getBarWidth();
    double rectWidth = Math.abs(transY2 - transY1);
    Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);
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
    Number n = dataset.getStdDevValue(row, column);
    if(n != null) {
      double valueDelta = n.doubleValue();
      double highVal = rangeAxis.valueToJava2D(meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation);
      double lowVal = rangeAxis.valueToJava2D(meanValue.doubleValue() - valueDelta, dataArea, yAxisLocation);
      if(this.errorIndicatorPaint != null) {
        g2.setPaint(this.errorIndicatorPaint);
      }
      else {
        g2.setPaint(getItemOutlinePaint(row, column, selected));
      }
      if(this.errorIndicatorStroke != null) {
        g2.setStroke(this.errorIndicatorStroke);
      }
      else {
        g2.setStroke(getItemOutlineStroke(row, column, selected));
      }
      Line2D line = null;
      line = new Line2D.Double(lowVal, rectY + rectHeight / 2.0D, highVal, rectY + rectHeight / 2.0D);
      g2.draw(line);
      line = new Line2D.Double(highVal, rectY + rectHeight * 0.25D, highVal, rectY + rectHeight * 0.75D);
      g2.draw(line);
      line = new Line2D.Double(lowVal, rectY + rectHeight * 0.25D, lowVal, rectY + rectHeight * 0.75D);
      g2.draw(line);
    }
    CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column, selected);
    if(generator != null && isItemLabelVisible(row, column, selected)) {
      drawItemLabelForBar(g2, plot, dataset, row, column, selected, generator, bar, (value < 0.0D));
    }
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, bar, dataset, row, column, selected);
    }
  }
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, int pass) {
    int visibleRow = state.getVisibleSeriesIndex(row);
    if(visibleRow < 0) {
      return ;
    }
    if(!(dataset instanceof StatisticalCategoryDataset)) {
      throw new IllegalArgumentException("Requires StatisticalCategoryDataset.");
    }
    StatisticalCategoryDataset statDataset = (StatisticalCategoryDataset)dataset;
    PlotOrientation orientation = plot.getOrientation();
    if(orientation == PlotOrientation.HORIZONTAL) {
      drawHorizontalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, statDataset, visibleRow, row, column, selected);
    }
    else 
      if(orientation == PlotOrientation.VERTICAL) {
        drawVerticalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, statDataset, visibleRow, row, column, selected);
      }
  }
  protected void drawVerticalItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, StatisticalCategoryDataset dataset, int visibleRow, int row, int column, boolean selected) {
    RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
    double rectX = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, xAxisLocation);
    int seriesCount = state.getVisibleSeriesCount() >= 0 ? state.getVisibleSeriesCount() : getRowCount();
    int categoryCount = getColumnCount();
    if(seriesCount > 1) {
      double seriesGap = dataArea.getWidth() * getItemMargin() / (categoryCount * (seriesCount - 1));
      rectX = rectX + visibleRow * (state.getBarWidth() + seriesGap);
    }
    else {
      rectX = rectX + visibleRow * state.getBarWidth();
    }
    Number meanValue = dataset.getMeanValue(row, column);
    if(meanValue == null) {
      return ;
    }
    double value = meanValue.doubleValue();
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
    RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
    double transY1 = rangeAxis.valueToJava2D(base, dataArea, yAxisLocation);
    double transY2 = rangeAxis.valueToJava2D(value, dataArea, yAxisLocation);
    double rectY = Math.min(transY2, transY1);
    double rectWidth = state.getBarWidth();
    double rectHeight = Math.abs(transY2 - transY1);
    Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);
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
    Number n = dataset.getStdDevValue(row, column);
    if(n != null) {
      double valueDelta = n.doubleValue();
      double highVal = rangeAxis.valueToJava2D(meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation);
      double lowVal = rangeAxis.valueToJava2D(meanValue.doubleValue() - valueDelta, dataArea, yAxisLocation);
      if(this.errorIndicatorPaint != null) {
        g2.setPaint(this.errorIndicatorPaint);
      }
      else {
        g2.setPaint(getItemOutlinePaint(row, column, selected));
      }
      Stroke var_2701 = this.errorIndicatorStroke;
      if(var_2701 != null) {
        g2.setStroke(this.errorIndicatorStroke);
      }
      else {
        g2.setStroke(getItemOutlineStroke(row, column, selected));
      }
      Line2D line = null;
      line = new Line2D.Double(rectX + rectWidth / 2.0D, lowVal, rectX + rectWidth / 2.0D, highVal);
      g2.draw(line);
      line = new Line2D.Double(rectX + rectWidth / 2.0D - 5.0D, highVal, rectX + rectWidth / 2.0D + 5.0D, highVal);
      g2.draw(line);
      line = new Line2D.Double(rectX + rectWidth / 2.0D - 5.0D, lowVal, rectX + rectWidth / 2.0D + 5.0D, lowVal);
      g2.draw(line);
    }
    CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column, selected);
    if(generator != null && isItemLabelVisible(row, column, selected)) {
      drawItemLabelForBar(g2, plot, dataset, row, column, selected, generator, bar, (value < 0.0D));
    }
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, bar, dataset, row, column, selected);
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.errorIndicatorPaint = SerialUtilities.readPaint(stream);
    this.errorIndicatorStroke = SerialUtilities.readStroke(stream);
  }
  public void setErrorIndicatorPaint(Paint paint) {
    this.errorIndicatorPaint = paint;
    fireChangeEvent();
  }
  public void setErrorIndicatorStroke(Stroke stroke) {
    this.errorIndicatorStroke = stroke;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.errorIndicatorPaint, stream);
    SerialUtilities.writeStroke(this.errorIndicatorStroke, stream);
  }
}