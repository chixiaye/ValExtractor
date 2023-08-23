package org.jfree.chart.renderer.category;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.MultiValueCategoryDataset;

public class ScatterRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable  {
  private BooleanList seriesShapesFilled;
  private boolean baseShapesFilled;
  private boolean useFillPaint;
  private boolean drawOutlines;
  private boolean useOutlinePaint;
  private boolean useSeriesOffset;
  private double itemMargin;
  public ScatterRenderer() {
    super();
    this.seriesShapesFilled = new BooleanList();
    this.baseShapesFilled = true;
    this.useFillPaint = false;
    this.drawOutlines = false;
    this.useOutlinePaint = false;
    this.useSeriesOffset = true;
    this.itemMargin = 0.20D;
  }
  public LegendItem getLegendItem(int datasetIndex, int series) {
    CategoryPlot cp = getPlot();
    if(cp == null) {
      return null;
    }
    if(isSeriesVisible(series) && isSeriesVisibleInLegend(series)) {
      CategoryDataset dataset = cp.getDataset(datasetIndex);
      String label = getLegendItemLabelGenerator().generateLabel(dataset, series);
      String description = label;
      String toolTipText = null;
      if(getLegendItemToolTipGenerator() != null) {
        toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset, series);
      }
      String urlText = null;
      org.jfree.chart.labels.CategorySeriesLabelGenerator var_2584 = getLegendItemURLGenerator();
      if(var_2584 != null) {
        urlText = getLegendItemURLGenerator().generateLabel(dataset, series);
      }
      Shape shape = lookupLegendShape(series);
      Paint paint = lookupSeriesPaint(series);
      Paint fillPaint = (this.useFillPaint ? getItemFillPaint(series, 0, false) : paint);
      boolean shapeOutlineVisible = this.drawOutlines;
      Paint outlinePaint = (this.useOutlinePaint ? getItemOutlinePaint(series, 0, false) : paint);
      Stroke outlineStroke = lookupSeriesOutlineStroke(series);
      LegendItem result = new LegendItem(label, description, toolTipText, urlText, true, shape, getItemShapeFilled(series, 0), fillPaint, shapeOutlineVisible, outlinePaint, outlineStroke, false, new Line2D.Double(-7.0D, 0.0D, 7.0D, 0.0D), getItemStroke(series, 0, false), getItemPaint(series, 0, false));
      result.setLabelFont(lookupLegendTextFont(series));
      Paint labelPaint = lookupLegendTextPaint(series);
      if(labelPaint != null) {
        result.setLabelPaint(labelPaint);
      }
      result.setDataset(dataset);
      result.setDatasetIndex(datasetIndex);
      result.setSeriesKey(dataset.getRowKey(series));
      result.setSeriesIndex(series);
      return result;
    }
    return null;
  }
  public Object clone() throws CloneNotSupportedException {
    ScatterRenderer clone = (ScatterRenderer)super.clone();
    clone.seriesShapesFilled = (BooleanList)this.seriesShapesFilled.clone();
    return clone;
  }
  public Range findRangeBounds(CategoryDataset dataset) {
    return findRangeBounds(dataset, true);
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof ScatterRenderer)) {
      return false;
    }
    ScatterRenderer that = (ScatterRenderer)obj;
    if(!ObjectUtilities.equal(this.seriesShapesFilled, that.seriesShapesFilled)) {
      return false;
    }
    if(this.baseShapesFilled != that.baseShapesFilled) {
      return false;
    }
    if(this.useFillPaint != that.useFillPaint) {
      return false;
    }
    if(this.drawOutlines != that.drawOutlines) {
      return false;
    }
    if(this.useOutlinePaint != that.useOutlinePaint) {
      return false;
    }
    if(this.useSeriesOffset != that.useSeriesOffset) {
      return false;
    }
    if(this.itemMargin != that.itemMargin) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getBaseShapesFilled() {
    return this.baseShapesFilled;
  }
  public boolean getDrawOutlines() {
    return this.drawOutlines;
  }
  public boolean getItemShapeFilled(int series, int item) {
    return getSeriesShapesFilled(series);
  }
  public boolean getSeriesShapesFilled(int series) {
    Boolean flag = this.seriesShapesFilled.getBoolean(series);
    if(flag != null) {
      return flag.booleanValue();
    }
    else {
      return this.baseShapesFilled;
    }
  }
  public boolean getUseFillPaint() {
    return this.useFillPaint;
  }
  public boolean getUseOutlinePaint() {
    return this.useOutlinePaint;
  }
  public boolean getUseSeriesOffset() {
    return this.useSeriesOffset;
  }
  public double getItemMargin() {
    return this.itemMargin;
  }
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, int pass) {
    if(!getItemVisible(row, column)) {
      return ;
    }
    int visibleRow = state.getVisibleSeriesIndex(row);
    if(visibleRow < 0) {
      return ;
    }
    int visibleRowCount = state.getVisibleSeriesCount();
    PlotOrientation orientation = plot.getOrientation();
    MultiValueCategoryDataset d = (MultiValueCategoryDataset)dataset;
    List values = d.getValues(row, column);
    if(values == null) {
      return ;
    }
    int valueCount = values.size();
    for(int i = 0; i < valueCount; i++) {
      double x1;
      if(this.useSeriesOffset) {
        x1 = domainAxis.getCategorySeriesMiddle(column, dataset.getColumnCount(), visibleRow, visibleRowCount, this.itemMargin, dataArea, plot.getDomainAxisEdge());
      }
      else {
        x1 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
      }
      Number n = (Number)values.get(i);
      double value = n.doubleValue();
      double y1 = rangeAxis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
      Shape shape = getItemShape(row, column, selected);
      if(orientation == PlotOrientation.HORIZONTAL) {
        shape = ShapeUtilities.createTranslatedShape(shape, y1, x1);
      }
      else 
        if(orientation == PlotOrientation.VERTICAL) {
          shape = ShapeUtilities.createTranslatedShape(shape, x1, y1);
        }
      if(getItemShapeFilled(row, column)) {
        if(this.useFillPaint) {
          g2.setPaint(getItemFillPaint(row, column, selected));
        }
        else {
          g2.setPaint(getItemPaint(row, column, selected));
        }
        g2.fill(shape);
      }
      if(this.drawOutlines) {
        if(this.useOutlinePaint) {
          g2.setPaint(getItemOutlinePaint(row, column, selected));
        }
        else {
          g2.setPaint(getItemPaint(row, column, selected));
        }
        g2.setStroke(getItemOutlineStroke(row, column, selected));
        g2.draw(shape);
      }
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
  }
  public void setBaseShapesFilled(boolean flag) {
    this.baseShapesFilled = flag;
    fireChangeEvent();
  }
  public void setDrawOutlines(boolean flag) {
    this.drawOutlines = flag;
    fireChangeEvent();
  }
  public void setItemMargin(double margin) {
    if(margin < 0.0D || margin >= 1.0D) {
      throw new IllegalArgumentException("Requires 0.0 <= margin < 1.0.");
    }
    this.itemMargin = margin;
    fireChangeEvent();
  }
  public void setSeriesShapesFilled(int series, boolean filled) {
    this.seriesShapesFilled.setBoolean(series, Boolean.valueOf(filled));
    fireChangeEvent();
  }
  public void setSeriesShapesFilled(int series, Boolean filled) {
    this.seriesShapesFilled.setBoolean(series, filled);
    fireChangeEvent();
  }
  public void setUseFillPaint(boolean flag) {
    this.useFillPaint = flag;
    fireChangeEvent();
  }
  public void setUseOutlinePaint(boolean use) {
    this.useOutlinePaint = use;
    fireChangeEvent();
  }
  public void setUseSeriesOffset(boolean offset) {
    this.useSeriesOffset = offset;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
  }
}