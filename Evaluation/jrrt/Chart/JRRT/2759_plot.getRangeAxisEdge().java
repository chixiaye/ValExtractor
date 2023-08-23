package org.jfree.chart.renderer.category;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.DataUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

public class StackedBarRenderer3D extends BarRenderer3D implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = -5832945916493247123L;
  private boolean renderAsPercentages;
  private boolean ignoreZeroValues;
  public StackedBarRenderer3D() {
    this(false);
  }
  public StackedBarRenderer3D(boolean renderAsPercentages) {
    super();
    this.renderAsPercentages = renderAsPercentages;
  }
  public StackedBarRenderer3D(double xOffset, double yOffset) {
    super(xOffset, yOffset);
  }
  public StackedBarRenderer3D(double xOffset, double yOffset, boolean renderAsPercentages) {
    super(xOffset, yOffset);
    this.renderAsPercentages = renderAsPercentages;
  }
  protected List createStackedValueList(CategoryDataset dataset, Comparable category, int[] includedRows, double base, boolean asPercentages) {
    List result = new ArrayList();
    double posBase = base;
    double negBase = base;
    double total = 0.0D;
    if(asPercentages) {
      total = DataUtilities.calculateColumnTotal(dataset, dataset.getColumnIndex(category), includedRows);
    }
    int baseIndex = -1;
    int rowCount = includedRows.length;
    for(int i = 0; i < rowCount; i++) {
      int r = includedRows[i];
      Number n = dataset.getValue(dataset.getRowKey(r), category);
      if(n == null) {
        continue ;
      }
      double v = n.doubleValue();
      if(asPercentages) {
        v = v / total;
      }
      if((v > 0.0D) || (!this.ignoreZeroValues && v >= 0.0D)) {
        if(baseIndex < 0) {
          result.add(new Object[]{ null, new Double(base) } );
          baseIndex = 0;
        }
        posBase = posBase + v;
        result.add(new Object[]{ new Integer(r), new Double(posBase) } );
      }
      else 
        if(v < 0.0D) {
          if(baseIndex < 0) {
            result.add(new Object[]{ null, new Double(base) } );
            baseIndex = 0;
          }
          negBase = negBase + v;
          result.add(0, new Object[]{ new Integer(-r - 1), new Double(negBase) } );
          baseIndex++;
        }
    }
    return result;
  }
  public Range findRangeBounds(CategoryDataset dataset) {
    if(dataset == null) {
      return null;
    }
    if(this.renderAsPercentages) {
      return new Range(0.0D, 1.0D);
    }
    else {
      return DatasetUtilities.findStackedRangeBounds(dataset);
    }
  }
  private Shape[] createHorizontalBlock(double x0, double width, double y0, double y1, boolean inverted) {
    Shape[] result = new Shape[6];
    Point2D p00 = new Point2D.Double(y0, x0);
    Point2D p01 = new Point2D.Double(y0, x0 + width);
    Point2D p02 = new Point2D.Double(p01.getX() + getXOffset(), p01.getY() - getYOffset());
    Point2D p03 = new Point2D.Double(p00.getX() + getXOffset(), p00.getY() - getYOffset());
    Point2D p0 = new Point2D.Double(y1, x0);
    Point2D p1 = new Point2D.Double(y1, x0 + width);
    Point2D p2 = new Point2D.Double(p1.getX() + getXOffset(), p1.getY() - getYOffset());
    Point2D p3 = new Point2D.Double(p0.getX() + getXOffset(), p0.getY() - getYOffset());
    GeneralPath bottom = new GeneralPath();
    bottom.moveTo((float)p1.getX(), (float)p1.getY());
    bottom.lineTo((float)p01.getX(), (float)p01.getY());
    bottom.lineTo((float)p02.getX(), (float)p02.getY());
    bottom.lineTo((float)p2.getX(), (float)p2.getY());
    bottom.closePath();
    GeneralPath top = new GeneralPath();
    top.moveTo((float)p0.getX(), (float)p0.getY());
    top.lineTo((float)p00.getX(), (float)p00.getY());
    top.lineTo((float)p03.getX(), (float)p03.getY());
    top.lineTo((float)p3.getX(), (float)p3.getY());
    top.closePath();
    GeneralPath back = new GeneralPath();
    back.moveTo((float)p2.getX(), (float)p2.getY());
    back.lineTo((float)p02.getX(), (float)p02.getY());
    back.lineTo((float)p03.getX(), (float)p03.getY());
    back.lineTo((float)p3.getX(), (float)p3.getY());
    back.closePath();
    GeneralPath front = new GeneralPath();
    front.moveTo((float)p0.getX(), (float)p0.getY());
    front.lineTo((float)p1.getX(), (float)p1.getY());
    front.lineTo((float)p01.getX(), (float)p01.getY());
    front.lineTo((float)p00.getX(), (float)p00.getY());
    front.closePath();
    GeneralPath left = new GeneralPath();
    left.moveTo((float)p0.getX(), (float)p0.getY());
    left.lineTo((float)p1.getX(), (float)p1.getY());
    left.lineTo((float)p2.getX(), (float)p2.getY());
    left.lineTo((float)p3.getX(), (float)p3.getY());
    left.closePath();
    GeneralPath right = new GeneralPath();
    right.moveTo((float)p00.getX(), (float)p00.getY());
    right.lineTo((float)p01.getX(), (float)p01.getY());
    right.lineTo((float)p02.getX(), (float)p02.getY());
    right.lineTo((float)p03.getX(), (float)p03.getY());
    right.closePath();
    result[0] = bottom;
    result[1] = back;
    if(inverted) {
      result[2] = right;
      result[3] = left;
    }
    else {
      result[2] = left;
      result[3] = right;
    }
    result[4] = top;
    result[5] = front;
    return result;
  }
  private Shape[] createVerticalBlock(double x0, double width, double y0, double y1, boolean inverted) {
    Shape[] result = new Shape[6];
    Point2D p00 = new Point2D.Double(x0, y0);
    Point2D p01 = new Point2D.Double(x0 + width, y0);
    Point2D p02 = new Point2D.Double(p01.getX() + getXOffset(), p01.getY() - getYOffset());
    Point2D p03 = new Point2D.Double(p00.getX() + getXOffset(), p00.getY() - getYOffset());
    Point2D p0 = new Point2D.Double(x0, y1);
    Point2D p1 = new Point2D.Double(x0 + width, y1);
    Point2D p2 = new Point2D.Double(p1.getX() + getXOffset(), p1.getY() - getYOffset());
    Point2D p3 = new Point2D.Double(p0.getX() + getXOffset(), p0.getY() - getYOffset());
    GeneralPath right = new GeneralPath();
    right.moveTo((float)p1.getX(), (float)p1.getY());
    right.lineTo((float)p01.getX(), (float)p01.getY());
    right.lineTo((float)p02.getX(), (float)p02.getY());
    right.lineTo((float)p2.getX(), (float)p2.getY());
    right.closePath();
    GeneralPath left = new GeneralPath();
    left.moveTo((float)p0.getX(), (float)p0.getY());
    left.lineTo((float)p00.getX(), (float)p00.getY());
    left.lineTo((float)p03.getX(), (float)p03.getY());
    left.lineTo((float)p3.getX(), (float)p3.getY());
    left.closePath();
    GeneralPath back = new GeneralPath();
    back.moveTo((float)p2.getX(), (float)p2.getY());
    back.lineTo((float)p02.getX(), (float)p02.getY());
    back.lineTo((float)p03.getX(), (float)p03.getY());
    back.lineTo((float)p3.getX(), (float)p3.getY());
    back.closePath();
    GeneralPath front = new GeneralPath();
    front.moveTo((float)p0.getX(), (float)p0.getY());
    front.lineTo((float)p1.getX(), (float)p1.getY());
    front.lineTo((float)p01.getX(), (float)p01.getY());
    front.lineTo((float)p00.getX(), (float)p00.getY());
    front.closePath();
    GeneralPath top = new GeneralPath();
    top.moveTo((float)p0.getX(), (float)p0.getY());
    top.lineTo((float)p1.getX(), (float)p1.getY());
    top.lineTo((float)p2.getX(), (float)p2.getY());
    top.lineTo((float)p3.getX(), (float)p3.getY());
    top.closePath();
    GeneralPath bottom = new GeneralPath();
    bottom.moveTo((float)p00.getX(), (float)p00.getY());
    bottom.lineTo((float)p01.getX(), (float)p01.getY());
    bottom.lineTo((float)p02.getX(), (float)p02.getY());
    bottom.lineTo((float)p03.getX(), (float)p03.getY());
    bottom.closePath();
    result[0] = bottom;
    result[1] = back;
    result[2] = left;
    result[3] = right;
    result[4] = top;
    result[5] = front;
    if(inverted) {
      result[0] = top;
      result[4] = bottom;
    }
    return result;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof StackedBarRenderer3D)) {
      return false;
    }
    StackedBarRenderer3D that = (StackedBarRenderer3D)obj;
    if(this.renderAsPercentages != that.getRenderAsPercentages()) {
      return false;
    }
    if(this.ignoreZeroValues != that.ignoreZeroValues) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getIgnoreZeroValues() {
    return this.ignoreZeroValues;
  }
  public boolean getRenderAsPercentages() {
    return this.renderAsPercentages;
  }
  public int hashCode() {
    int hash = super.hashCode();
    hash = HashUtilities.hashCode(hash, this.renderAsPercentages);
    hash = HashUtilities.hashCode(hash, this.ignoreZeroValues);
    return hash;
  }
  protected void calculateBarWidth(CategoryPlot plot, Rectangle2D dataArea, CategoryDataset dataset, CategoryItemRendererState state) {
    CategoryAxis domainAxis = getDomainAxis(plot, dataset);
    if(dataset != null) {
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
      int columns = dataset.getColumnCount();
      double categoryMargin = 0.0D;
      if(columns > 1) {
        categoryMargin = domainAxis.getCategoryMargin();
      }
      double used = space * (1 - domainAxis.getLowerMargin() - domainAxis.getUpperMargin() - categoryMargin);
      if(columns > 0) {
        state.setBarWidth(Math.min(used / columns, maxWidth));
      }
      else {
        state.setBarWidth(Math.min(used, maxWidth));
      }
    }
  }
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, int pass) {
    if(row < dataset.getRowCount() - 1) {
      return ;
    }
    Comparable category = dataset.getColumnKey(column);
    List values = createStackedValueList(dataset, dataset.getColumnKey(column), state.getVisibleSeriesArray(), getBase(), this.renderAsPercentages);
    Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX(), dataArea.getY() + getYOffset(), dataArea.getWidth() - getXOffset(), dataArea.getHeight() - getYOffset());
    PlotOrientation orientation = plot.getOrientation();
    if(orientation == PlotOrientation.HORIZONTAL) {
      drawStackHorizontal(values, category, g2, state, adjusted, plot, domainAxis, rangeAxis, dataset);
    }
    else {
      drawStackVertical(values, category, g2, state, adjusted, plot, domainAxis, rangeAxis, dataset);
    }
  }
  protected void drawStackHorizontal(List values, Comparable category, Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset) {
    int column = dataset.getColumnIndex(category);
    double barX0 = domainAxis.getCategoryMiddle(column, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0D;
    double barW = state.getBarWidth();
    List itemLabelList = new ArrayList();
    boolean inverted = rangeAxis.isInverted();
    int blockCount = values.size() - 1;
    for(int k = 0; k < blockCount; k++) {
      int index = (inverted ? blockCount - k - 1 : k);
      Object[] prev = (Object[])values.get(index);
      Object[] curr = (Object[])values.get(index + 1);
      int series = 0;
      if(curr[0] == null) {
        series = -((Integer)prev[0]).intValue() - 1;
      }
      else {
        series = ((Integer)curr[0]).intValue();
        if(series < 0) {
          series = -((Integer)prev[0]).intValue() - 1;
        }
      }
      double v0 = ((Double)prev[1]).doubleValue();
      org.jfree.chart.util.RectangleEdge var_2759 = plot.getRangeAxisEdge();
      double vv0 = rangeAxis.valueToJava2D(v0, dataArea, var_2759);
      double v1 = ((Double)curr[1]).doubleValue();
      double vv1 = rangeAxis.valueToJava2D(v1, dataArea, plot.getRangeAxisEdge());
      Shape[] faces = createHorizontalBlock(barX0, barW, vv0, vv1, inverted);
      Paint fillPaint = getItemPaint(series, column, false);
      Paint fillPaintDark = fillPaint;
      if(fillPaintDark instanceof Color) {
        fillPaintDark = ((Color)fillPaint).darker();
      }
      boolean drawOutlines = isDrawBarOutline();
      Paint outlinePaint = fillPaint;
      if(drawOutlines) {
        outlinePaint = getItemOutlinePaint(series, column, false);
        g2.setStroke(getItemOutlineStroke(series, column, false));
      }
      for(int f = 0; f < 6; f++) {
        if(f == 5) {
          g2.setPaint(fillPaint);
        }
        else {
          g2.setPaint(fillPaintDark);
        }
        g2.fill(faces[f]);
        if(drawOutlines) {
          g2.setPaint(outlinePaint);
          g2.draw(faces[f]);
        }
      }
      itemLabelList.add(new Object[]{ new Integer(series), faces[5].getBounds2D(), Boolean.valueOf(v0 < getBase()) } );
      EntityCollection entities = state.getEntityCollection();
      if(entities != null) {
        addEntity(entities, faces[5], dataset, series, column, false);
      }
    }
    for(int i = 0; i < itemLabelList.size(); i++) {
      Object[] record = (Object[])itemLabelList.get(i);
      int series = ((Integer)record[0]).intValue();
      Rectangle2D bar = (Rectangle2D)record[1];
      boolean neg = ((Boolean)record[2]).booleanValue();
      CategoryItemLabelGenerator generator = getItemLabelGenerator(series, column, false);
      if(generator != null && isItemLabelVisible(series, column, false)) {
        drawItemLabelForBar(g2, plot, dataset, series, column, false, generator, bar, neg);
      }
    }
  }
  protected void drawStackVertical(List values, Comparable category, Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset) {
    int column = dataset.getColumnIndex(category);
    double barX0 = domainAxis.getCategoryMiddle(column, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge()) - state.getBarWidth() / 2.0D;
    double barW = state.getBarWidth();
    List itemLabelList = new ArrayList();
    boolean inverted = rangeAxis.isInverted();
    int blockCount = values.size() - 1;
    for(int k = 0; k < blockCount; k++) {
      int index = (inverted ? blockCount - k - 1 : k);
      Object[] prev = (Object[])values.get(index);
      Object[] curr = (Object[])values.get(index + 1);
      int series = 0;
      if(curr[0] == null) {
        series = -((Integer)prev[0]).intValue() - 1;
      }
      else {
        series = ((Integer)curr[0]).intValue();
        if(series < 0) {
          series = -((Integer)prev[0]).intValue() - 1;
        }
      }
      double v0 = ((Double)prev[1]).doubleValue();
      double vv0 = rangeAxis.valueToJava2D(v0, dataArea, plot.getRangeAxisEdge());
      double v1 = ((Double)curr[1]).doubleValue();
      double vv1 = rangeAxis.valueToJava2D(v1, dataArea, plot.getRangeAxisEdge());
      Shape[] faces = createVerticalBlock(barX0, barW, vv0, vv1, inverted);
      Paint fillPaint = getItemPaint(series, column, false);
      Paint fillPaintDark = fillPaint;
      if(fillPaintDark instanceof Color) {
        fillPaintDark = ((Color)fillPaint).darker();
      }
      boolean drawOutlines = isDrawBarOutline();
      Paint outlinePaint = fillPaint;
      if(drawOutlines) {
        outlinePaint = getItemOutlinePaint(series, column, false);
        g2.setStroke(getItemOutlineStroke(series, column, false));
      }
      for(int f = 0; f < 6; f++) {
        if(f == 5) {
          g2.setPaint(fillPaint);
        }
        else {
          g2.setPaint(fillPaintDark);
        }
        g2.fill(faces[f]);
        if(drawOutlines) {
          g2.setPaint(outlinePaint);
          g2.draw(faces[f]);
        }
      }
      itemLabelList.add(new Object[]{ new Integer(series), faces[5].getBounds2D(), Boolean.valueOf(v0 < getBase()) } );
      EntityCollection entities = state.getEntityCollection();
      if(entities != null) {
        addEntity(entities, faces[5], dataset, series, column, false);
      }
    }
    for(int i = 0; i < itemLabelList.size(); i++) {
      Object[] record = (Object[])itemLabelList.get(i);
      int series = ((Integer)record[0]).intValue();
      Rectangle2D bar = (Rectangle2D)record[1];
      boolean neg = ((Boolean)record[2]).booleanValue();
      CategoryItemLabelGenerator generator = getItemLabelGenerator(series, column, false);
      if(generator != null && isItemLabelVisible(series, column, false)) {
        drawItemLabelForBar(g2, plot, dataset, series, column, false, generator, bar, neg);
      }
    }
  }
  public void setIgnoreZeroValues(boolean ignore) {
    this.ignoreZeroValues = ignore;
    notifyListeners(new RendererChangeEvent(this));
  }
  public void setRenderAsPercentages(boolean asPercentages) {
    this.renderAsPercentages = asPercentages;
    fireChangeEvent();
  }
}