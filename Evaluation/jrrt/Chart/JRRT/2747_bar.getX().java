package org.jfree.chart.renderer.category;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.TextUtilities;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.chart.util.StandardGradientPaintTransformer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;

public class BarRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 6000649414965887481L;
  final public static double DEFAULT_ITEM_MARGIN = 0.20D;
  final public static double BAR_OUTLINE_WIDTH_THRESHOLD = 3.0D;
  private static BarPainter defaultBarPainter = new GradientBarPainter();
  private static boolean defaultShadowsVisible = true;
  private double itemMargin;
  private boolean drawBarOutline;
  private double maximumBarWidth;
  private double minimumBarLength;
  private GradientPaintTransformer gradientPaintTransformer;
  private ItemLabelPosition positiveItemLabelPositionFallback;
  private ItemLabelPosition negativeItemLabelPositionFallback;
  private double base;
  private boolean includeBaseInRange;
  private BarPainter barPainter;
  private boolean shadowsVisible;
  private transient Paint shadowPaint;
  private double shadowXOffset;
  private double shadowYOffset;
  public BarRenderer() {
    super();
    this.base = 0.0D;
    this.includeBaseInRange = true;
    this.itemMargin = DEFAULT_ITEM_MARGIN;
    this.drawBarOutline = false;
    this.maximumBarWidth = 1.0D;
    this.positiveItemLabelPositionFallback = null;
    this.negativeItemLabelPositionFallback = null;
    this.gradientPaintTransformer = new StandardGradientPaintTransformer();
    this.minimumBarLength = 0.0D;
    setBaseLegendShape(new Rectangle2D.Double(-4.0D, -4.0D, 8.0D, 8.0D));
    this.barPainter = getDefaultBarPainter();
    this.shadowsVisible = getDefaultShadowsVisible();
    this.shadowPaint = Color.gray;
    this.shadowXOffset = 4.0D;
    this.shadowYOffset = 4.0D;
  }
  public BarPainter getBarPainter() {
    return this.barPainter;
  }
  public static BarPainter getDefaultBarPainter() {
    return BarRenderer.defaultBarPainter;
  }
  public CategoryItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, CategoryPlot plot, CategoryDataset dataset, PlotRenderingInfo info) {
    CategoryItemRendererState state = super.initialise(g2, dataArea, plot, dataset, info);
    calculateBarWidth(plot, dataArea, dataset, state);
    return state;
  }
  public GradientPaintTransformer getGradientPaintTransformer() {
    return this.gradientPaintTransformer;
  }
  public ItemLabelPosition getNegativeItemLabelPositionFallback() {
    return this.negativeItemLabelPositionFallback;
  }
  public ItemLabelPosition getPositiveItemLabelPositionFallback() {
    return this.positiveItemLabelPositionFallback;
  }
  public LegendItem getLegendItem(int datasetIndex, int series) {
    CategoryPlot cp = getPlot();
    if(cp == null) {
      return null;
    }
    if(!isSeriesVisible(series) || !isSeriesVisibleInLegend(series)) {
      return null;
    }
    CategoryDataset dataset = cp.getDataset(datasetIndex);
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
    LegendItem result = new LegendItem(label, description, toolTipText, urlText, true, shape, true, paint, isDrawBarOutline(), outlinePaint, outlineStroke, false, new Line2D.Float(), new BasicStroke(1.0F), Color.black);
    result.setLabelFont(lookupLegendTextFont(series));
    Paint labelPaint = lookupLegendTextPaint(series);
    if(labelPaint != null) {
      result.setLabelPaint(labelPaint);
    }
    result.setDataset(dataset);
    result.setDatasetIndex(datasetIndex);
    result.setSeriesKey(dataset.getRowKey(series));
    result.setSeriesIndex(series);
    if(this.gradientPaintTransformer != null) {
      result.setFillPaintTransformer(this.gradientPaintTransformer);
    }
    return result;
  }
  public Paint getShadowPaint() {
    return this.shadowPaint;
  }
  private Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor, Rectangle2D bar, PlotOrientation orientation) {
    Point2D result = null;
    double offset = getItemLabelAnchorOffset();
    double x0 = bar.getX() - offset;
    double var_2747 = bar.getX();
    double x1 = var_2747;
    double x2 = bar.getX() + offset;
    double x3 = bar.getCenterX();
    double x4 = bar.getMaxX() - offset;
    double x5 = bar.getMaxX();
    double x6 = bar.getMaxX() + offset;
    double y0 = bar.getMaxY() + offset;
    double y1 = bar.getMaxY();
    double y2 = bar.getMaxY() - offset;
    double y3 = bar.getCenterY();
    double y4 = bar.getMinY() + offset;
    double y5 = bar.getMinY();
    double y6 = bar.getMinY() - offset;
    if(anchor == ItemLabelAnchor.CENTER) {
      result = new Point2D.Double(x3, y3);
    }
    else 
      if(anchor == ItemLabelAnchor.INSIDE1) {
        result = new Point2D.Double(x4, y4);
      }
      else 
        if(anchor == ItemLabelAnchor.INSIDE2) {
          result = new Point2D.Double(x4, y4);
        }
        else 
          if(anchor == ItemLabelAnchor.INSIDE3) {
            result = new Point2D.Double(x4, y3);
          }
          else 
            if(anchor == ItemLabelAnchor.INSIDE4) {
              result = new Point2D.Double(x4, y2);
            }
            else 
              if(anchor == ItemLabelAnchor.INSIDE5) {
                result = new Point2D.Double(x4, y2);
              }
              else 
                if(anchor == ItemLabelAnchor.INSIDE6) {
                  result = new Point2D.Double(x3, y2);
                }
                else 
                  if(anchor == ItemLabelAnchor.INSIDE7) {
                    result = new Point2D.Double(x2, y2);
                  }
                  else 
                    if(anchor == ItemLabelAnchor.INSIDE8) {
                      result = new Point2D.Double(x2, y2);
                    }
                    else 
                      if(anchor == ItemLabelAnchor.INSIDE9) {
                        result = new Point2D.Double(x2, y3);
                      }
                      else 
                        if(anchor == ItemLabelAnchor.INSIDE10) {
                          result = new Point2D.Double(x2, y4);
                        }
                        else 
                          if(anchor == ItemLabelAnchor.INSIDE11) {
                            result = new Point2D.Double(x2, y4);
                          }
                          else 
                            if(anchor == ItemLabelAnchor.INSIDE12) {
                              result = new Point2D.Double(x3, y4);
                            }
                            else 
                              if(anchor == ItemLabelAnchor.OUTSIDE1) {
                                result = new Point2D.Double(x5, y6);
                              }
                              else 
                                if(anchor == ItemLabelAnchor.OUTSIDE2) {
                                  result = new Point2D.Double(x6, y5);
                                }
                                else 
                                  if(anchor == ItemLabelAnchor.OUTSIDE3) {
                                    result = new Point2D.Double(x6, y3);
                                  }
                                  else 
                                    if(anchor == ItemLabelAnchor.OUTSIDE4) {
                                      result = new Point2D.Double(x6, y1);
                                    }
                                    else 
                                      if(anchor == ItemLabelAnchor.OUTSIDE5) {
                                        result = new Point2D.Double(x5, y0);
                                      }
                                      else 
                                        if(anchor == ItemLabelAnchor.OUTSIDE6) {
                                          result = new Point2D.Double(x3, y0);
                                        }
                                        else 
                                          if(anchor == ItemLabelAnchor.OUTSIDE7) {
                                            result = new Point2D.Double(x1, y0);
                                          }
                                          else 
                                            if(anchor == ItemLabelAnchor.OUTSIDE8) {
                                              result = new Point2D.Double(x0, y1);
                                            }
                                            else 
                                              if(anchor == ItemLabelAnchor.OUTSIDE9) {
                                                result = new Point2D.Double(x0, y3);
                                              }
                                              else 
                                                if(anchor == ItemLabelAnchor.OUTSIDE10) {
                                                  result = new Point2D.Double(x0, y5);
                                                }
                                                else 
                                                  if(anchor == ItemLabelAnchor.OUTSIDE11) {
                                                    result = new Point2D.Double(x1, y6);
                                                  }
                                                  else 
                                                    if(anchor == ItemLabelAnchor.OUTSIDE12) {
                                                      result = new Point2D.Double(x3, y6);
                                                    }
    return result;
  }
  public Range findRangeBounds(CategoryDataset dataset, boolean includeInterval) {
    if(dataset == null) {
      return null;
    }
    Range result = super.findRangeBounds(dataset, includeInterval);
    if(result != null) {
      if(this.includeBaseInRange) {
        result = Range.expandToInclude(result, this.base);
      }
    }
    return result;
  }
  public Rectangle2D createHotSpotBounds(Graphics2D g2, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean selected, CategoryItemRendererState state, Rectangle2D result) {
    int visibleRow = state.getVisibleSeriesIndex(row);
    if(visibleRow < 0) {
      return null;
    }
    if(!this.getItemVisible(row, column)) {
      return null;
    }
    Number dataValue = dataset.getValue(row, column);
    if(dataValue == null) {
      return null;
    }
    final double value = dataValue.doubleValue();
    PlotOrientation orientation = plot.getOrientation();
    double barW0 = calculateBarW0(plot, orientation, dataArea, domainAxis, state, visibleRow, column);
    double[] barL0L1 = calculateBarL0L1(value, rangeAxis.getLowerBound(), rangeAxis.getUpperBound());
    if(barL0L1 == null) {
      return null;
    }
    RectangleEdge edge = plot.getRangeAxisEdge();
    double transL0 = rangeAxis.valueToJava2D(barL0L1[0], dataArea, edge);
    double transL1 = rangeAxis.valueToJava2D(barL0L1[1], dataArea, edge);
    boolean positive = (value >= this.base);
    boolean inverted = rangeAxis.isInverted();
    double barL0 = Math.min(transL0, transL1);
    double barLength = Math.abs(transL1 - transL0);
    double barLengthAdj = 0.0D;
    if(barLength > 0.0D && barLength < getMinimumBarLength()) {
      barLengthAdj = getMinimumBarLength() - barLength;
    }
    double barL0Adj = 0.0D;
    RectangleEdge barBase;
    if(orientation == PlotOrientation.HORIZONTAL) {
      if(positive && inverted || !positive && !inverted) {
        barL0Adj = barLengthAdj;
        barBase = RectangleEdge.RIGHT;
      }
      else {
        barBase = RectangleEdge.LEFT;
      }
    }
    else {
      if(positive && !inverted || !positive && inverted) {
        barL0Adj = barLengthAdj;
        barBase = RectangleEdge.BOTTOM;
      }
      else {
        barBase = RectangleEdge.TOP;
      }
    }
    Rectangle2D bar = null;
    if(orientation == PlotOrientation.HORIZONTAL) {
      bar = new Rectangle2D.Double(barL0 - barL0Adj, barW0, barLength + barLengthAdj, state.getBarWidth());
    }
    else {
      bar = new Rectangle2D.Double(barW0, barL0 - barL0Adj, state.getBarWidth(), barLength + barLengthAdj);
    }
    return bar;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof BarRenderer)) {
      return false;
    }
    BarRenderer that = (BarRenderer)obj;
    if(this.base != that.base) {
      return false;
    }
    if(this.itemMargin != that.itemMargin) {
      return false;
    }
    if(this.drawBarOutline != that.drawBarOutline) {
      return false;
    }
    if(this.maximumBarWidth != that.maximumBarWidth) {
      return false;
    }
    if(this.minimumBarLength != that.minimumBarLength) {
      return false;
    }
    if(!ObjectUtilities.equal(this.gradientPaintTransformer, that.gradientPaintTransformer)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.positiveItemLabelPositionFallback, that.positiveItemLabelPositionFallback)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.negativeItemLabelPositionFallback, that.negativeItemLabelPositionFallback)) {
      return false;
    }
    if(!this.barPainter.equals(that.barPainter)) {
      return false;
    }
    if(this.shadowsVisible != that.shadowsVisible) {
      return false;
    }
    if(!PaintUtilities.equal(this.shadowPaint, that.shadowPaint)) {
      return false;
    }
    if(this.shadowXOffset != that.shadowXOffset) {
      return false;
    }
    if(this.shadowYOffset != that.shadowYOffset) {
      return false;
    }
    return super.equals(obj);
  }
  public static boolean getDefaultShadowsVisible() {
    return BarRenderer.defaultShadowsVisible;
  }
  public boolean getIncludeBaseInRange() {
    return this.includeBaseInRange;
  }
  public boolean getShadowsVisible() {
    return this.shadowsVisible;
  }
  public boolean isDrawBarOutline() {
    return this.drawBarOutline;
  }
  private boolean isInternalAnchor(ItemLabelAnchor anchor) {
    return anchor == ItemLabelAnchor.CENTER || anchor == ItemLabelAnchor.INSIDE1 || anchor == ItemLabelAnchor.INSIDE2 || anchor == ItemLabelAnchor.INSIDE3 || anchor == ItemLabelAnchor.INSIDE4 || anchor == ItemLabelAnchor.INSIDE5 || anchor == ItemLabelAnchor.INSIDE6 || anchor == ItemLabelAnchor.INSIDE7 || anchor == ItemLabelAnchor.INSIDE8 || anchor == ItemLabelAnchor.INSIDE9 || anchor == ItemLabelAnchor.INSIDE10 || anchor == ItemLabelAnchor.INSIDE11 || anchor == ItemLabelAnchor.INSIDE12;
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
    int seriesCount = state.getVisibleSeriesCount() >= 0 ? state.getVisibleSeriesCount() : getRowCount();
    int categoryCount = getColumnCount();
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
  public double getBase() {
    return this.base;
  }
  public double getItemMargin() {
    return this.itemMargin;
  }
  public double getMaximumBarWidth() {
    return this.maximumBarWidth;
  }
  public double getMinimumBarLength() {
    return this.minimumBarLength;
  }
  public double getShadowXOffset() {
    return this.shadowXOffset;
  }
  public double getShadowYOffset() {
    return this.shadowYOffset;
  }
  protected double[] calculateBarL0L1(double value, double min, double max) {
    double barLow = Math.min(this.base, value);
    double barHigh = Math.max(this.base, value);
    if(barHigh < min) {
      return null;
    }
    if(barLow > max) {
      return null;
    }
    barLow = Math.max(barLow, min);
    barHigh = Math.min(barHigh, max);
    return new double[]{ barLow, barHigh } ;
  }
  protected void calculateBarWidth(CategoryPlot plot, Rectangle2D dataArea, CategoryDataset dataset, CategoryItemRendererState state) {
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
      double maxWidth = space * getMaximumBarWidth();
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
    final double value = dataValue.doubleValue();
    PlotOrientation orientation = plot.getOrientation();
    double barW0 = calculateBarW0(plot, orientation, dataArea, domainAxis, state, visibleRow, column);
    double[] barL0L1 = calculateBarL0L1(value, rangeAxis.getLowerBound(), rangeAxis.getUpperBound());
    if(barL0L1 == null) {
      return ;
    }
    RectangleEdge edge = plot.getRangeAxisEdge();
    double transL0 = rangeAxis.valueToJava2D(barL0L1[0], dataArea, edge);
    double transL1 = rangeAxis.valueToJava2D(barL0L1[1], dataArea, edge);
    boolean positive = (value >= this.base);
    boolean inverted = rangeAxis.isInverted();
    double barL0 = Math.min(transL0, transL1);
    double barLength = Math.abs(transL1 - transL0);
    double barLengthAdj = 0.0D;
    if(barLength > 0.0D && barLength < getMinimumBarLength()) {
      barLengthAdj = getMinimumBarLength() - barLength;
    }
    double barL0Adj = 0.0D;
    RectangleEdge barBase;
    if(orientation == PlotOrientation.HORIZONTAL) {
      if(positive && inverted || !positive && !inverted) {
        barL0Adj = barLengthAdj;
        barBase = RectangleEdge.RIGHT;
      }
      else {
        barBase = RectangleEdge.LEFT;
      }
    }
    else {
      if(positive && !inverted || !positive && inverted) {
        barL0Adj = barLengthAdj;
        barBase = RectangleEdge.BOTTOM;
      }
      else {
        barBase = RectangleEdge.TOP;
      }
    }
    Rectangle2D bar = null;
    if(orientation == PlotOrientation.HORIZONTAL) {
      bar = new Rectangle2D.Double(barL0 - barL0Adj, barW0, barLength + barLengthAdj, state.getBarWidth());
    }
    else {
      bar = new Rectangle2D.Double(barW0, barL0 - barL0Adj, state.getBarWidth(), barLength + barLengthAdj);
    }
    if(getShadowsVisible()) {
      this.barPainter.paintBarShadow(g2, this, row, column, selected, bar, barBase, true);
    }
    this.barPainter.paintBar(g2, this, row, column, selected, bar, barBase);
    CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column, selected);
    if(generator != null && isItemLabelVisible(row, column, selected)) {
      drawItemLabelForBar(g2, plot, dataset, row, column, selected, generator, bar, (value < 0.0D));
    }
    int datasetIndex = plot.indexOf(dataset);
    updateCrosshairValues(state.getCrosshairState(), dataset.getRowKey(row), dataset.getColumnKey(column), value, datasetIndex, barW0, barL0, orientation);
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, bar, dataset, row, column, selected);
    }
  }
  protected void drawItemLabelForBar(Graphics2D g2, CategoryPlot plot, CategoryDataset dataset, int row, int column, boolean selected, CategoryItemLabelGenerator generator, Rectangle2D bar, boolean negative) {
    String label = generator.generateLabel(dataset, row, column);
    if(label == null) {
      return ;
    }
    Font labelFont = getItemLabelFont(row, column, selected);
    g2.setFont(labelFont);
    Paint paint = getItemLabelPaint(row, column, selected);
    g2.setPaint(paint);
    ItemLabelPosition position = null;
    if(!negative) {
      position = getPositiveItemLabelPosition(row, column, selected);
    }
    else {
      position = getNegativeItemLabelPosition(row, column, selected);
    }
    Point2D anchorPoint = calculateLabelAnchorPoint(position.getItemLabelAnchor(), bar, plot.getOrientation());
    if(isInternalAnchor(position.getItemLabelAnchor())) {
      Shape bounds = TextUtilities.calculateRotatedStringBounds(label, g2, (float)anchorPoint.getX(), (float)anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor());
      if(bounds != null) {
        if(!bar.contains(bounds.getBounds2D())) {
          if(!negative) {
            position = getPositiveItemLabelPositionFallback();
          }
          else {
            position = getNegativeItemLabelPositionFallback();
          }
          if(position != null) {
            anchorPoint = calculateLabelAnchorPoint(position.getItemLabelAnchor(), bar, plot.getOrientation());
          }
        }
      }
    }
    if(position != null) {
      TextUtilities.drawRotatedString(label, g2, (float)anchorPoint.getX(), (float)anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor());
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.shadowPaint = SerialUtilities.readPaint(stream);
  }
  public void setBarPainter(BarPainter painter) {
    if(painter == null) {
      throw new IllegalArgumentException("Null \'painter\' argument.");
    }
    this.barPainter = painter;
    fireChangeEvent();
  }
  public void setBase(double base) {
    this.base = base;
    fireChangeEvent();
  }
  public static void setDefaultBarPainter(BarPainter painter) {
    if(painter == null) {
      throw new IllegalArgumentException("Null \'painter\' argument.");
    }
    BarRenderer.defaultBarPainter = painter;
  }
  public static void setDefaultShadowsVisible(boolean visible) {
    BarRenderer.defaultShadowsVisible = visible;
  }
  public void setDrawBarOutline(boolean draw) {
    this.drawBarOutline = draw;
    fireChangeEvent();
  }
  public void setGradientPaintTransformer(GradientPaintTransformer transformer) {
    this.gradientPaintTransformer = transformer;
    fireChangeEvent();
  }
  public void setIncludeBaseInRange(boolean include) {
    if(this.includeBaseInRange != include) {
      this.includeBaseInRange = include;
      fireChangeEvent();
    }
  }
  public void setItemMargin(double percent) {
    this.itemMargin = percent;
    fireChangeEvent();
  }
  public void setMaximumBarWidth(double percent) {
    this.maximumBarWidth = percent;
    fireChangeEvent();
  }
  public void setMinimumBarLength(double min) {
    if(min < 0.0D) {
      throw new IllegalArgumentException("Requires \'min\' >= 0.0");
    }
    this.minimumBarLength = min;
    fireChangeEvent();
  }
  public void setNegativeItemLabelPositionFallback(ItemLabelPosition position) {
    this.negativeItemLabelPositionFallback = position;
    fireChangeEvent();
  }
  public void setPositiveItemLabelPositionFallback(ItemLabelPosition position) {
    this.positiveItemLabelPositionFallback = position;
    fireChangeEvent();
  }
  public void setShadowPaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.shadowPaint = paint;
    fireChangeEvent();
  }
  public void setShadowVisible(boolean visible) {
    this.shadowsVisible = visible;
    fireChangeEvent();
  }
  public void setShadowXOffset(double offset) {
    this.shadowXOffset = offset;
    fireChangeEvent();
  }
  public void setShadowYOffset(double offset) {
    this.shadowYOffset = offset;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.shadowPaint, stream);
  }
}