package org.jfree.chart.renderer.xy;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYCrosshairState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtilities;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.chart.util.StandardGradientPaintTransformer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

public class XYBarRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 770559577251370036L;
  private static XYBarPainter defaultBarPainter = new GradientXYBarPainter();
  private static boolean defaultShadowsVisible = true;
  private double base;
  private boolean useYInterval;
  private double margin;
  private boolean drawBarOutline;
  private GradientPaintTransformer gradientPaintTransformer;
  private transient Shape legendBar;
  private ItemLabelPosition positiveItemLabelPositionFallback;
  private ItemLabelPosition negativeItemLabelPositionFallback;
  private XYBarPainter barPainter;
  private boolean shadowsVisible;
  private double shadowXOffset;
  private double shadowYOffset;
  private double barAlignmentFactor;
  public XYBarRenderer() {
    this(0.0D);
  }
  public XYBarRenderer(double margin) {
    super();
    this.margin = margin;
    this.base = 0.0D;
    this.useYInterval = false;
    this.gradientPaintTransformer = new StandardGradientPaintTransformer();
    this.drawBarOutline = false;
    this.legendBar = new Rectangle2D.Double(-3.0D, -5.0D, 6.0D, 10.0D);
    this.barPainter = getDefaultBarPainter();
    this.shadowsVisible = getDefaultShadowsVisible();
    this.shadowXOffset = 4.0D;
    this.shadowYOffset = 4.0D;
    this.barAlignmentFactor = -1.0D;
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
    LegendItem result = null;
    XYPlot xyplot = getPlot();
    if(xyplot != null) {
      XYDataset dataset = xyplot.getDataset(datasetIndex);
      if(dataset != null) {
        XYSeriesLabelGenerator lg = getLegendItemLabelGenerator();
        String label = lg.generateLabel(dataset, series);
        String description = label;
        String toolTipText = null;
        if(getLegendItemToolTipGenerator() != null) {
          toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset, series);
        }
        String urlText = null;
        if(getLegendItemURLGenerator() != null) {
          urlText = getLegendItemURLGenerator().generateLabel(dataset, series);
        }
        Shape shape = this.legendBar;
        Paint paint = lookupSeriesPaint(series);
        Paint outlinePaint = lookupSeriesOutlinePaint(series);
        Stroke outlineStroke = lookupSeriesOutlineStroke(series);
        if(this.drawBarOutline) {
          result = new LegendItem(label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint);
        }
        else {
          result = new LegendItem(label, description, toolTipText, urlText, shape, paint);
        }
        result.setLabelFont(lookupLegendTextFont(series));
        Paint labelPaint = lookupLegendTextPaint(series);
        if(labelPaint != null) {
          result.setLabelPaint(labelPaint);
        }
        result.setDataset(dataset);
        result.setDatasetIndex(datasetIndex);
        result.setSeriesKey(dataset.getSeriesKey(series));
        result.setSeriesIndex(series);
        if(getGradientPaintTransformer() != null) {
          result.setFillPaintTransformer(getGradientPaintTransformer());
        }
      }
    }
    return result;
  }
  public Object clone() throws CloneNotSupportedException {
    XYBarRenderer result = (XYBarRenderer)super.clone();
    if(this.gradientPaintTransformer != null) {
      result.gradientPaintTransformer = (GradientPaintTransformer)ObjectUtilities.clone(this.gradientPaintTransformer);
    }
    result.legendBar = ShapeUtilities.clone(this.legendBar);
    return result;
  }
  private Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor, Rectangle2D bar, PlotOrientation orientation) {
    Point2D result = null;
    double offset = getItemLabelAnchorOffset();
    double x0 = bar.getX() - offset;
    double var_2885 = bar.getX();
    double x1 = var_2885;
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
  public Range findDomainBounds(XYDataset dataset) {
    if(dataset != null) {
      return DatasetUtilities.findDomainBounds(dataset, true);
    }
    else {
      return null;
    }
  }
  public Range findRangeBounds(XYDataset dataset) {
    if(dataset != null) {
      return DatasetUtilities.findRangeBounds(dataset, this.useYInterval);
    }
    else {
      return null;
    }
  }
  protected Rectangle2D createBar(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected) {
    if(!getItemVisible(series, item)) {
      return null;
    }
    IntervalXYDataset ixyd = (IntervalXYDataset)dataset;
    double value0;
    double value1;
    if(this.useYInterval) {
      value0 = ixyd.getStartYValue(series, item);
      value1 = ixyd.getEndYValue(series, item);
    }
    else {
      value0 = this.base;
      value1 = ixyd.getYValue(series, item);
    }
    if(Double.isNaN(value0) || Double.isNaN(value1)) {
      return null;
    }
    if(value0 <= value1) {
      if(!rangeAxis.getRange().intersects(value0, value1)) {
        return null;
      }
    }
    else {
      if(!rangeAxis.getRange().intersects(value1, value0)) {
        return null;
      }
    }
    double translatedValue0 = rangeAxis.valueToJava2D(value0, dataArea, plot.getRangeAxisEdge());
    double translatedValue1 = rangeAxis.valueToJava2D(value1, dataArea, plot.getRangeAxisEdge());
    double bottom = Math.min(translatedValue0, translatedValue1);
    double top = Math.max(translatedValue0, translatedValue1);
    double startX = ixyd.getStartXValue(series, item);
    if(Double.isNaN(startX)) {
      return null;
    }
    double endX = ixyd.getEndXValue(series, item);
    if(Double.isNaN(endX)) {
      return null;
    }
    if(startX <= endX) {
      if(!domainAxis.getRange().intersects(startX, endX)) {
        return null;
      }
    }
    else {
      if(!domainAxis.getRange().intersects(endX, startX)) {
        return null;
      }
    }
    if(this.barAlignmentFactor >= 0.0D && this.barAlignmentFactor <= 1.0D) {
      double x = ixyd.getXValue(series, item);
      double interval = endX - startX;
      startX = x - interval * this.barAlignmentFactor;
      endX = startX + interval;
    }
    RectangleEdge location = plot.getDomainAxisEdge();
    double translatedStartX = domainAxis.valueToJava2D(startX, dataArea, location);
    double translatedEndX = domainAxis.valueToJava2D(endX, dataArea, location);
    double translatedWidth = Math.max(1, Math.abs(translatedEndX - translatedStartX));
    double left = Math.min(translatedStartX, translatedEndX);
    if(getMargin() > 0.0D) {
      double cut = translatedWidth * getMargin();
      translatedWidth = translatedWidth - cut;
      left = left + cut / 2;
    }
    Rectangle2D bar = null;
    PlotOrientation orientation = plot.getOrientation();
    if(orientation == PlotOrientation.HORIZONTAL) {
      bottom = Math.max(bottom, dataArea.getMinX());
      top = Math.min(top, dataArea.getMaxX());
      bar = new Rectangle2D.Double(bottom, left, top - bottom, translatedWidth);
    }
    else 
      if(orientation == PlotOrientation.VERTICAL) {
        bottom = Math.max(bottom, dataArea.getMinY());
        top = Math.min(top, dataArea.getMaxY());
        bar = new Rectangle2D.Double(left, bottom, translatedWidth, top - bottom);
      }
    return bar;
  }
  public Rectangle2D createHotSpotBounds(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, Rectangle2D result) {
    return createBar(g2, dataArea, plot, domainAxis, rangeAxis, dataset, series, item, selected);
  }
  public Shape getLegendBar() {
    return this.legendBar;
  }
  public XYBarPainter getBarPainter() {
    return this.barPainter;
  }
  public static XYBarPainter getDefaultBarPainter() {
    return XYBarRenderer.defaultBarPainter;
  }
  protected XYItemRendererState createState(PlotRenderingInfo info) {
    return new XYBarRendererState(info);
  }
  public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset dataset, PlotRenderingInfo info) {
    XYBarRendererState state = (XYBarRendererState)super.initialise(g2, dataArea, plot, dataset, info);
    ValueAxis rangeAxis = plot.getRangeAxisForDataset(plot.indexOf(dataset));
    state.setG2Base(rangeAxis.valueToJava2D(this.base, dataArea, plot.getRangeAxisEdge()));
    return state;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof XYBarRenderer)) {
      return false;
    }
    XYBarRenderer that = (XYBarRenderer)obj;
    if(this.base != that.base) {
      return false;
    }
    if(this.drawBarOutline != that.drawBarOutline) {
      return false;
    }
    if(this.margin != that.margin) {
      return false;
    }
    if(this.useYInterval != that.useYInterval) {
      return false;
    }
    if(!ObjectUtilities.equal(this.gradientPaintTransformer, that.gradientPaintTransformer)) {
      return false;
    }
    if(!ShapeUtilities.equal(this.legendBar, that.legendBar)) {
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
    if(this.shadowXOffset != that.shadowXOffset) {
      return false;
    }
    if(this.shadowYOffset != that.shadowYOffset) {
      return false;
    }
    if(this.barAlignmentFactor != that.barAlignmentFactor) {
      return false;
    }
    return super.equals(obj);
  }
  public static boolean getDefaultShadowsVisible() {
    return XYBarRenderer.defaultShadowsVisible;
  }
  public boolean getShadowsVisible() {
    return this.shadowsVisible;
  }
  public boolean getUseYInterval() {
    return this.useYInterval;
  }
  public boolean isDrawBarOutline() {
    return this.drawBarOutline;
  }
  private boolean isInternalAnchor(ItemLabelAnchor anchor) {
    return anchor == ItemLabelAnchor.CENTER || anchor == ItemLabelAnchor.INSIDE1 || anchor == ItemLabelAnchor.INSIDE2 || anchor == ItemLabelAnchor.INSIDE3 || anchor == ItemLabelAnchor.INSIDE4 || anchor == ItemLabelAnchor.INSIDE5 || anchor == ItemLabelAnchor.INSIDE6 || anchor == ItemLabelAnchor.INSIDE7 || anchor == ItemLabelAnchor.INSIDE8 || anchor == ItemLabelAnchor.INSIDE9 || anchor == ItemLabelAnchor.INSIDE10 || anchor == ItemLabelAnchor.INSIDE11 || anchor == ItemLabelAnchor.INSIDE12;
  }
  public double getBarAlignmentFactor() {
    return this.barAlignmentFactor;
  }
  public double getBase() {
    return this.base;
  }
  public double getMargin() {
    return this.margin;
  }
  public double getShadowXOffset() {
    return this.shadowXOffset;
  }
  public double getShadowYOffset() {
    return this.shadowYOffset;
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    Rectangle2D bar = createBar(g2, dataArea, plot, domainAxis, rangeAxis, dataset, series, item, selected);
    if(bar == null) {
      return ;
    }
    boolean positive = true;
    if(this.useYInterval) {
      positive = dataset.getYValue(series, item) >= 0.0D;
    }
    else {
      positive = dataset.getYValue(series, item) >= 0.0D;
    }
    boolean inverted = rangeAxis.isInverted();
    RectangleEdge barBase;
    if(plot.getOrientation() == PlotOrientation.HORIZONTAL) {
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
    if(getShadowsVisible()) {
      this.barPainter.paintBarShadow(g2, this, series, item, selected, bar, barBase, !this.useYInterval);
    }
    this.barPainter.paintBar(g2, this, series, item, selected, bar, barBase);
    if(isItemLabelVisible(series, item, selected)) {
      XYItemLabelGenerator generator = getItemLabelGenerator(series, item, selected);
      drawItemLabelForBar(g2, plot, dataset, series, item, selected, generator, bar, !positive);
    }
    double x1 = dataset.getXValue(series, item);
    double y1 = dataset.getYValue(series, item);
    double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
    double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());
    int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
    int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
    XYCrosshairState crosshairState = state.getCrosshairState();
    updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, plot.getOrientation());
    EntityCollection entities = state.getEntityCollection();
    if(entities != null) {
      addEntity(entities, bar, dataset, series, item, selected, 0.0D, 0.0D);
    }
  }
  protected void drawItemLabelForBar(Graphics2D g2, XYPlot plot, XYDataset dataset, int series, int item, boolean selected, XYItemLabelGenerator generator, Rectangle2D bar, boolean negative) {
    if(generator == null) {
      return ;
    }
    String label = generator.generateLabel(dataset, series, item);
    if(label == null) {
      return ;
    }
    Font labelFont = getItemLabelFont(series, item, selected);
    g2.setFont(labelFont);
    Paint paint = getItemLabelPaint(series, item, selected);
    g2.setPaint(paint);
    ItemLabelPosition position = null;
    if(!negative) {
      position = getPositiveItemLabelPosition(series, item, selected);
    }
    else {
      position = getNegativeItemLabelPosition(series, item, selected);
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
    this.legendBar = SerialUtilities.readShape(stream);
  }
  public void setBarAlignmentFactor(double factor) {
    this.barAlignmentFactor = factor;
    fireChangeEvent();
  }
  public void setBarPainter(XYBarPainter painter) {
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
  public static void setDefaultBarPainter(XYBarPainter painter) {
    if(painter == null) {
      throw new IllegalArgumentException("Null \'painter\' argument.");
    }
    XYBarRenderer.defaultBarPainter = painter;
  }
  public static void setDefaultShadowsVisible(boolean visible) {
    XYBarRenderer.defaultShadowsVisible = visible;
  }
  public void setDrawBarOutline(boolean draw) {
    this.drawBarOutline = draw;
    fireChangeEvent();
  }
  public void setGradientPaintTransformer(GradientPaintTransformer transformer) {
    this.gradientPaintTransformer = transformer;
    fireChangeEvent();
  }
  public void setLegendBar(Shape bar) {
    if(bar == null) {
      throw new IllegalArgumentException("Null \'bar\' argument.");
    }
    this.legendBar = bar;
    fireChangeEvent();
  }
  public void setMargin(double margin) {
    this.margin = margin;
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
  public void setUseYInterval(boolean use) {
    if(this.useYInterval != use) {
      this.useYInterval = use;
      fireChangeEvent();
    }
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writeShape(this.legendBar, stream);
  }
  
  protected class XYBarRendererState extends XYItemRendererState  {
    private double g2Base;
    public XYBarRendererState(PlotRenderingInfo info) {
      super(info);
    }
    public double getG2Base() {
      return this.g2Base;
    }
    public void setG2Base(double value) {
      this.g2Base = value;
    }
  }
}