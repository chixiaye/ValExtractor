package org.jfree.chart.plot;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.RenderingSource;
import org.jfree.chart.util.PaintMap;
import org.jfree.chart.util.StrokeMap;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.text.G2TextMeasurer;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextBox;
import org.jfree.chart.text.TextUtilities;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.DefaultShadowGenerator;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleAnchor;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.util.Rotation;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.chart.util.UnitType;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.pie.PieDataset;
import org.jfree.data.pie.PieDatasetChangeInfo;
import org.jfree.data.pie.PieDatasetChangeType;
import org.jfree.data.pie.PieDatasetSelectionState;
import org.jfree.data.pie.SelectablePieDataset;

public class PiePlot extends Plot implements Selectable, Cloneable, Serializable  {
  final private static long serialVersionUID = -795612466005590431L;
  final public static double DEFAULT_INTERIOR_GAP = 0.08D;
  final public static double MAX_INTERIOR_GAP = 0.40D;
  final public static double DEFAULT_START_ANGLE = 90.0D;
  final public static Font DEFAULT_LABEL_FONT = new Font("Tahoma", Font.PLAIN, 10);
  final public static Paint DEFAULT_LABEL_PAINT = Color.black;
  final public static Paint DEFAULT_LABEL_BACKGROUND_PAINT = new Color(255, 255, 192);
  final public static Paint DEFAULT_LABEL_OUTLINE_PAINT = Color.black;
  final public static Stroke DEFAULT_LABEL_OUTLINE_STROKE = new BasicStroke(0.5F);
  final public static Paint DEFAULT_LABEL_SHADOW_PAINT = new Color(151, 151, 151, 128);
  final public static double DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW = 0.00001D;
  private PieDataset dataset;
  private int pieIndex;
  private double interiorGap;
  private boolean circular;
  private double startAngle;
  private Rotation direction;
  private PaintMap sectionPaintMap;
  private transient Paint baseSectionPaint;
  private boolean autoPopulateSectionPaint;
  private boolean sectionOutlinesVisible;
  private PaintMap sectionOutlinePaintMap;
  private transient Paint baseSectionOutlinePaint;
  private boolean autoPopulateSectionOutlinePaint;
  private StrokeMap sectionOutlineStrokeMap;
  private transient Stroke baseSectionOutlineStroke;
  private boolean autoPopulateSectionOutlineStroke;
  private transient Paint shadowPaint = Color.gray;
  private double shadowXOffset = 4.0F;
  private double shadowYOffset = 4.0F;
  private Map explodePercentages;
  private PieSectionLabelGenerator labelGenerator;
  private Font labelFont;
  private transient Paint labelPaint;
  private transient Paint labelBackgroundPaint;
  private transient Paint labelOutlinePaint;
  private transient Stroke labelOutlineStroke;
  private transient Paint labelShadowPaint;
  private boolean simpleLabels = true;
  private RectangleInsets labelPadding;
  private RectangleInsets simpleLabelOffset;
  private double maximumLabelWidth = 0.14D;
  private double labelGap = 0.025D;
  private boolean labelLinksVisible;
  private PieLabelLinkStyle labelLinkStyle = PieLabelLinkStyle.STANDARD;
  private double labelLinkMargin = 0.025D;
  private transient Paint labelLinkPaint = Color.black;
  private transient Stroke labelLinkStroke = new BasicStroke(0.5F);
  private AbstractPieLabelDistributor labelDistributor;
  private PieToolTipGenerator toolTipGenerator;
  private PieURLGenerator urlGenerator;
  private PieSectionLabelGenerator legendLabelGenerator;
  private PieSectionLabelGenerator legendLabelToolTipGenerator;
  private PieURLGenerator legendLabelURLGenerator;
  private boolean ignoreNullValues;
  private boolean ignoreZeroValues;
  private transient Shape legendItemShape;
  private double minimumArcAngleToDraw;
  private ShadowGenerator shadowGenerator;
  protected static ResourceBundle localizationResources = ResourceBundleWrapper.getBundle("org.jfree.chart.plot.LocalizationBundle");
  private PieSelectionAttributes selectedItemAttributes;
  final static boolean DEBUG_DRAW_INTERIOR = false;
  final static boolean DEBUG_DRAW_LINK_AREA = false;
  final static boolean DEBUG_DRAW_PIE_AREA = false;
  public PiePlot() {
    this(null);
  }
  public PiePlot(PieDataset dataset) {
    super();
    this.dataset = dataset;
    if(dataset != null) {
      dataset.addChangeListener(this);
    }
    this.pieIndex = 0;
    this.interiorGap = DEFAULT_INTERIOR_GAP;
    this.circular = true;
    this.startAngle = DEFAULT_START_ANGLE;
    this.direction = Rotation.CLOCKWISE;
    this.minimumArcAngleToDraw = DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW;
    this.sectionPaintMap = new PaintMap();
    this.baseSectionPaint = Color.gray;
    this.autoPopulateSectionPaint = true;
    this.sectionOutlinesVisible = true;
    this.sectionOutlinePaintMap = new PaintMap();
    this.baseSectionOutlinePaint = DEFAULT_OUTLINE_PAINT;
    this.autoPopulateSectionOutlinePaint = false;
    this.sectionOutlineStrokeMap = new StrokeMap();
    this.baseSectionOutlineStroke = DEFAULT_OUTLINE_STROKE;
    this.autoPopulateSectionOutlineStroke = false;
    this.explodePercentages = new TreeMap();
    this.labelGenerator = new StandardPieSectionLabelGenerator();
    this.labelFont = DEFAULT_LABEL_FONT;
    this.labelPaint = DEFAULT_LABEL_PAINT;
    this.labelBackgroundPaint = DEFAULT_LABEL_BACKGROUND_PAINT;
    this.labelOutlinePaint = DEFAULT_LABEL_OUTLINE_PAINT;
    this.labelOutlineStroke = DEFAULT_LABEL_OUTLINE_STROKE;
    this.labelShadowPaint = DEFAULT_LABEL_SHADOW_PAINT;
    this.labelLinksVisible = true;
    this.labelDistributor = new PieLabelDistributor(0);
    this.simpleLabels = false;
    this.simpleLabelOffset = new RectangleInsets(UnitType.RELATIVE, 0.18D, 0.18D, 0.18D, 0.18D);
    this.labelPadding = new RectangleInsets(2, 2, 2, 2);
    this.toolTipGenerator = null;
    this.urlGenerator = null;
    this.legendLabelGenerator = new StandardPieSectionLabelGenerator();
    this.legendLabelToolTipGenerator = null;
    this.legendLabelURLGenerator = null;
    this.legendItemShape = Plot.DEFAULT_LEGEND_ITEM_CIRCLE;
    this.ignoreNullValues = false;
    this.ignoreZeroValues = false;
    this.selectedItemAttributes = new PieSelectionAttributes();
    this.shadowGenerator = new DefaultShadowGenerator();
  }
  public AbstractPieLabelDistributor getLabelDistributor() {
    return this.labelDistributor;
  }
  protected Comparable getSectionKey(int section) {
    Comparable key = null;
    if(this.dataset != null) {
      if(section >= 0 && section < this.dataset.getItemCount()) {
        key = this.dataset.getKey(section);
      }
    }
    if(key == null) {
      key = new Integer(section);
    }
    return key;
  }
  public Font getLabelFont() {
    return this.labelFont;
  }
  public LegendItemCollection getLegendItems() {
    LegendItemCollection result = new LegendItemCollection();
    if(this.dataset == null) {
      return result;
    }
    List keys = this.dataset.getKeys();
    int section = 0;
    Shape shape = getLegendItemShape();
    Iterator iterator = keys.iterator();
    while(iterator.hasNext()){
      Comparable key = (Comparable)iterator.next();
      Number n = this.dataset.getValue(key);
      boolean include = true;
      if(n == null) {
        include = !this.ignoreNullValues;
      }
      else {
        double v = n.doubleValue();
        if(v == 0.0D) {
          include = !this.ignoreZeroValues;
        }
        else {
          include = v > 0.0D;
        }
      }
      if(include) {
        String label = this.legendLabelGenerator.generateSectionLabel(this.dataset, key);
        if(label != null) {
          String description = label;
          String toolTipText = null;
          if(this.legendLabelToolTipGenerator != null) {
            toolTipText = this.legendLabelToolTipGenerator.generateSectionLabel(this.dataset, key);
          }
          String urlText = null;
          if(this.legendLabelURLGenerator != null) {
            urlText = this.legendLabelURLGenerator.generateURL(this.dataset, key, this.pieIndex);
          }
          Paint paint = lookupSectionPaint(key, false);
          Paint outlinePaint = lookupSectionOutlinePaint(key, false);
          Stroke outlineStroke = lookupSectionOutlineStroke(key, false);
          LegendItem item = new LegendItem(label, description, toolTipText, urlText, true, shape, true, paint, true, outlinePaint, outlineStroke, false, new Line2D.Float(), new BasicStroke(), Color.black);
          item.setDataset(getDataset());
          item.setSeriesIndex(this.dataset.getIndex(key));
          item.setSeriesKey(key);
          result.add(item);
        }
        section++;
      }
      else {
        section++;
      }
    }
    return result;
  }
  public Object clone() throws CloneNotSupportedException {
    PiePlot clone = (PiePlot)super.clone();
    if(clone.dataset != null) {
      clone.dataset.addChangeListener(clone);
    }
    if(this.urlGenerator instanceof PublicCloneable) {
      clone.urlGenerator = (PieURLGenerator)ObjectUtilities.clone(this.urlGenerator);
    }
    clone.legendItemShape = ShapeUtilities.clone(this.legendItemShape);
    if(this.legendLabelGenerator != null) {
      clone.legendLabelGenerator = (PieSectionLabelGenerator)ObjectUtilities.clone(this.legendLabelGenerator);
    }
    if(this.legendLabelToolTipGenerator != null) {
      clone.legendLabelToolTipGenerator = (PieSectionLabelGenerator)ObjectUtilities.clone(this.legendLabelToolTipGenerator);
    }
    if(this.legendLabelURLGenerator instanceof PublicCloneable) {
      clone.legendLabelURLGenerator = (PieURLGenerator)ObjectUtilities.clone(this.legendLabelURLGenerator);
    }
    return clone;
  }
  public Paint getBaseSectionOutlinePaint() {
    return this.baseSectionOutlinePaint;
  }
  public Paint getBaseSectionPaint() {
    return this.baseSectionPaint;
  }
  public Paint getLabelBackgroundPaint() {
    return this.labelBackgroundPaint;
  }
  public Paint getLabelLinkPaint() {
    return this.labelLinkPaint;
  }
  public Paint getLabelOutlinePaint() {
    return this.labelOutlinePaint;
  }
  public Paint getLabelPaint() {
    return this.labelPaint;
  }
  public Paint getLabelShadowPaint() {
    return this.labelShadowPaint;
  }
  public Paint getSectionOutlinePaint(Comparable key) {
    return this.sectionOutlinePaintMap.getPaint(key);
  }
  public Paint getSectionPaint(Comparable key, boolean selected) {
    if(selected) {
      return Color.white;
    }
    return this.sectionPaintMap.getPaint(key);
  }
  public Paint getShadowPaint() {
    return this.shadowPaint;
  }
  protected Paint lookupSectionOutlinePaint(Comparable key, boolean selected) {
    Paint result = null;
    if(selected) {
      result = this.selectedItemAttributes.lookupSectionOutlinePaint(key);
    }
    if(result == null) {
      result = lookupSectionOutlinePaint(key, selected, getAutoPopulateSectionOutlinePaint());
    }
    return result;
  }
  protected Paint lookupSectionOutlinePaint(Comparable key, boolean selected, boolean autoPopulate) {
    Paint result = null;
    if(selected) {
      return Color.WHITE;
    }
    result = this.sectionOutlinePaintMap.getPaint(key);
    if(result != null) {
      return result;
    }
    if(autoPopulate) {
      DrawingSupplier ds = getDrawingSupplier();
      if(ds != null) {
        result = ds.getNextOutlinePaint();
        this.sectionOutlinePaintMap.put(key, result);
      }
      else {
        result = this.baseSectionOutlinePaint;
      }
    }
    else {
      result = this.baseSectionOutlinePaint;
    }
    return result;
  }
  protected Paint lookupSectionPaint(Comparable key, boolean selected) {
    Paint result = null;
    if(selected) {
      result = this.selectedItemAttributes.lookupSectionPaint(key);
    }
    if(result == null) {
      result = lookupSectionPaint(key, selected, getAutoPopulateSectionPaint());
    }
    return result;
  }
  protected Paint lookupSectionPaint(Comparable key, boolean selected, boolean autoPopulate) {
    if(selected) {
    }
    Paint result = null;
    result = this.sectionPaintMap.getPaint(key);
    if(result != null) {
      return result;
    }
    if(autoPopulate) {
      DrawingSupplier ds = getDrawingSupplier();
      if(ds != null) {
        result = ds.getNextPaint();
        this.sectionPaintMap.put(key, result);
      }
      else {
        result = this.baseSectionPaint;
      }
    }
    else {
      result = this.baseSectionPaint;
    }
    return result;
  }
  public PieDataset getDataset() {
    return this.dataset;
  }
  private PieDatasetSelectionState findSelectionStateForDataset(PieDataset dataset, Object source) {
    if(dataset instanceof SelectablePieDataset) {
      SelectablePieDataset sd = (SelectablePieDataset)dataset;
      PieDatasetSelectionState s = sd.getSelectionState();
      return s;
    }
    throw new RuntimeException();
  }
  public PieLabelLinkStyle getLabelLinkStyle() {
    return this.labelLinkStyle;
  }
  public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea, PiePlot plot, Integer index, PlotRenderingInfo info) {
    PiePlotState state = new PiePlotState(info);
    state.setPassesRequired(2);
    if(this.dataset != null) {
      state.setTotal(DatasetUtilities.calculatePieDatasetTotal(plot.getDataset()));
    }
    state.setLatestAngle(plot.getStartAngle());
    return state;
  }
  public PieSectionLabelGenerator getLabelGenerator() {
    return this.labelGenerator;
  }
  public PieSectionLabelGenerator getLegendLabelGenerator() {
    return this.legendLabelGenerator;
  }
  public PieSectionLabelGenerator getLegendLabelToolTipGenerator() {
    return this.legendLabelToolTipGenerator;
  }
  public PieSelectionAttributes getSelectedItemAttributes() {
    return this.selectedItemAttributes;
  }
  public PieToolTipGenerator getToolTipGenerator() {
    return this.toolTipGenerator;
  }
  public PieURLGenerator getLegendLabelURLGenerator() {
    return this.legendLabelURLGenerator;
  }
  public PieURLGenerator getURLGenerator() {
    return this.urlGenerator;
  }
  protected Rectangle2D getArcBounds(Rectangle2D unexploded, Rectangle2D exploded, double angle, double extent, double explodePercent) {
    if(explodePercent == 0.0D) {
      return unexploded;
    }
    else {
      Arc2D arc1 = new Arc2D.Double(unexploded, angle, extent / 2, Arc2D.OPEN);
      Point2D point1 = arc1.getEndPoint();
      Arc2D.Double arc2 = new Arc2D.Double(exploded, angle, extent / 2, Arc2D.OPEN);
      Point2D point2 = arc2.getEndPoint();
      double deltaX = (point1.getX() - point2.getX()) * explodePercent;
      double deltaY = (point1.getY() - point2.getY()) * explodePercent;
      return new Rectangle2D.Double(unexploded.getX() - deltaX, unexploded.getY() - deltaY, unexploded.getWidth(), unexploded.getHeight());
    }
  }
  private Rectangle2D[] calculateLinkAndExplodeAreas(Graphics2D g2, Rectangle2D plotArea) {
    Rectangle2D[] result = new Rectangle2D[2];
    double labelReserve = 0.0D;
    if(this.labelGenerator != null && !this.simpleLabels) {
      labelReserve = this.labelGap + this.maximumLabelWidth;
    }
    double gapHorizontal = plotArea.getWidth() * (this.interiorGap + labelReserve) * 2.0D;
    double gapVertical = plotArea.getHeight() * this.interiorGap * 2.0D;
    if(DEBUG_DRAW_INTERIOR) {
      double hGap = plotArea.getWidth() * this.interiorGap;
      double vGap = plotArea.getHeight() * this.interiorGap;
      double igx1 = plotArea.getX() + hGap;
      double igx2 = plotArea.getMaxX() - hGap;
      double igy1 = plotArea.getY() + vGap;
      double igy2 = plotArea.getMaxY() - vGap;
      g2.setPaint(Color.gray);
      g2.draw(new Rectangle2D.Double(igx1, igy1, igx2 - igx1, igy2 - igy1));
    }
    double linkX = plotArea.getX() + gapHorizontal / 2;
    double linkY = plotArea.getY() + gapVertical / 2;
    double linkW = plotArea.getWidth() - gapHorizontal;
    double linkH = plotArea.getHeight() - gapVertical;
    if(this.circular) {
      double min = Math.min(linkW, linkH) / 2;
      linkX = (linkX + linkX + linkW) / 2 - min;
      linkY = (linkY + linkY + linkH) / 2 - min;
      linkW = 2 * min;
      linkH = 2 * min;
    }
    Rectangle2D linkArea = new Rectangle2D.Double(linkX, linkY, linkW, linkH);
    result[0] = linkArea;
    if(DEBUG_DRAW_LINK_AREA) {
      g2.setPaint(Color.blue);
      g2.draw(linkArea);
      g2.setPaint(Color.yellow);
      g2.draw(new Ellipse2D.Double(linkArea.getX(), linkArea.getY(), linkArea.getWidth(), linkArea.getHeight()));
    }
    double lm = 0.0D;
    if(!this.simpleLabels) {
      lm = this.labelLinkMargin;
    }
    double hh = linkArea.getWidth() * lm * 2.0D;
    double vv = linkArea.getHeight() * lm * 2.0D;
    Rectangle2D explodeArea = new Rectangle2D.Double(linkX + hh / 2.0D, linkY + vv / 2.0D, linkW - hh, linkH - vv);
    result[1] = explodeArea;
    return result;
  }
  public RectangleInsets getLabelPadding() {
    return this.labelPadding;
  }
  public RectangleInsets getSimpleLabelOffset() {
    return this.simpleLabelOffset;
  }
  public Rotation getDirection() {
    return this.direction;
  }
  public ShadowGenerator getShadowGenerator() {
    return this.shadowGenerator;
  }
  public Shape createHotSpotShape(Graphics2D g2, Rectangle2D dataArea, int section, boolean selected) {
    Number n = this.dataset.getValue(section);
    if(n == null) {
      return null;
    }
    double value = n.doubleValue();
    double angle1 = 0.0D;
    double angle2 = 0.0D;
    double total = DatasetUtilities.calculatePieDatasetTotal(this.dataset);
    double lead = 0.0D;
    if(this.direction == Rotation.CLOCKWISE) {
      for(int i = 0; i < section; i++) {
        n = this.dataset.getValue(i);
        if(n != null) {
          value = n.doubleValue();
          if(value >= 0.0D) {
            lead = lead + value;
          }
        }
      }
      angle1 = getStartAngle() - lead / total * 360.0D;
      angle2 = angle1 - value / total * 360.0D;
    }
    else 
      if(this.direction == Rotation.ANTICLOCKWISE) {
        angle1 = getStartAngle() + lead / total * 360.0D;
        angle2 = angle1 + value / total * 360.0D;
      }
      else {
        throw new IllegalStateException("Rotation type not recognised.");
      }
    double angle = (angle2 - angle1);
    if(Math.abs(angle) > getMinimumArcAngleToDraw()) {
      double ep = 0.0D;
      double mep = getMaximumExplodePercent();
      if(mep > 0.0D) {
        ep = getExplodePercent(getSectionKey(section)) / mep;
      }
      Rectangle2D arcBounds = getArcBounds(dataArea, dataArea, angle1, angle, ep);
      Arc2D.Double arc = new Arc2D.Double(arcBounds, angle1, angle, Arc2D.PIE);
      return arc;
    }
    return null;
  }
  public Shape getLegendItemShape() {
    return this.legendItemShape;
  }
  public String getPlotType() {
    return localizationResources.getString("Pie_Plot");
  }
  public Stroke getBaseSectionOutlineStroke() {
    return this.baseSectionOutlineStroke;
  }
  public Stroke getLabelLinkStroke() {
    return this.labelLinkStroke;
  }
  public Stroke getLabelOutlineStroke() {
    return this.labelOutlineStroke;
  }
  public Stroke getSectionOutlineStroke(Comparable key) {
    return this.sectionOutlineStrokeMap.getStroke(key);
  }
  protected Stroke lookupSectionOutlineStroke(Comparable key, boolean selected) {
    Stroke s = null;
    if(selected) {
      s = this.selectedItemAttributes.lookupSectionOutlineStroke(key);
    }
    if(s == null) {
      s = lookupSectionOutlineStroke(key, selected, getAutoPopulateSectionOutlineStroke());
    }
    return s;
  }
  protected Stroke lookupSectionOutlineStroke(Comparable key, boolean selected, boolean autoPopulate) {
    Stroke result = null;
    result = this.sectionOutlineStrokeMap.getStroke(key);
    if(result != null) {
      return result;
    }
    if(autoPopulate) {
      DrawingSupplier ds = getDrawingSupplier();
      if(ds != null) {
        result = ds.getNextOutlineStroke();
        this.sectionOutlineStrokeMap.put(key, result);
      }
      else {
        result = this.baseSectionOutlineStroke;
      }
    }
    else {
      result = this.baseSectionOutlineStroke;
    }
    return result;
  }
  public boolean canSelectByPoint() {
    return true;
  }
  public boolean canSelectByRegion() {
    return false;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof PiePlot)) {
      return false;
    }
    if(!super.equals(obj)) {
      return false;
    }
    PiePlot that = (PiePlot)obj;
    if(this.pieIndex != that.pieIndex) {
      return false;
    }
    if(this.interiorGap != that.interiorGap) {
      return false;
    }
    if(this.circular != that.circular) {
      return false;
    }
    if(this.startAngle != that.startAngle) {
      return false;
    }
    if(this.direction != that.direction) {
      return false;
    }
    if(this.ignoreZeroValues != that.ignoreZeroValues) {
      return false;
    }
    if(this.ignoreNullValues != that.ignoreNullValues) {
      return false;
    }
    if(!ObjectUtilities.equal(this.sectionPaintMap, that.sectionPaintMap)) {
      return false;
    }
    if(!PaintUtilities.equal(this.baseSectionPaint, that.baseSectionPaint)) {
      return false;
    }
    if(this.sectionOutlinesVisible != that.sectionOutlinesVisible) {
      return false;
    }
    if(!ObjectUtilities.equal(this.sectionOutlinePaintMap, that.sectionOutlinePaintMap)) {
      return false;
    }
    if(!PaintUtilities.equal(this.baseSectionOutlinePaint, that.baseSectionOutlinePaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.sectionOutlineStrokeMap, that.sectionOutlineStrokeMap)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.baseSectionOutlineStroke, that.baseSectionOutlineStroke)) {
      return false;
    }
    if(!PaintUtilities.equal(this.shadowPaint, that.shadowPaint)) {
      return false;
    }
    if(!(this.shadowXOffset == that.shadowXOffset)) {
      return false;
    }
    if(!(this.shadowYOffset == that.shadowYOffset)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.explodePercentages, that.explodePercentages)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.labelGenerator, that.labelGenerator)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.labelFont, that.labelFont)) {
      return false;
    }
    if(!PaintUtilities.equal(this.labelPaint, that.labelPaint)) {
      return false;
    }
    if(!PaintUtilities.equal(this.labelBackgroundPaint, that.labelBackgroundPaint)) {
      return false;
    }
    if(!PaintUtilities.equal(this.labelOutlinePaint, that.labelOutlinePaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.labelOutlineStroke, that.labelOutlineStroke)) {
      return false;
    }
    if(!PaintUtilities.equal(this.labelShadowPaint, that.labelShadowPaint)) {
      return false;
    }
    if(this.simpleLabels != that.simpleLabels) {
      return false;
    }
    if(!this.simpleLabelOffset.equals(that.simpleLabelOffset)) {
      return false;
    }
    if(!this.labelPadding.equals(that.labelPadding)) {
      return false;
    }
    if(!(this.maximumLabelWidth == that.maximumLabelWidth)) {
      return false;
    }
    if(!(this.labelGap == that.labelGap)) {
      return false;
    }
    if(!(this.labelLinkMargin == that.labelLinkMargin)) {
      return false;
    }
    if(this.labelLinksVisible != that.labelLinksVisible) {
      return false;
    }
    if(!this.labelLinkStyle.equals(that.labelLinkStyle)) {
      return false;
    }
    if(!PaintUtilities.equal(this.labelLinkPaint, that.labelLinkPaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.labelLinkStroke, that.labelLinkStroke)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.toolTipGenerator, that.toolTipGenerator)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.urlGenerator, that.urlGenerator)) {
      return false;
    }
    if(!(this.minimumArcAngleToDraw == that.minimumArcAngleToDraw)) {
      return false;
    }
    if(!ShapeUtilities.equal(this.legendItemShape, that.legendItemShape)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.legendLabelGenerator, that.legendLabelGenerator)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.legendLabelToolTipGenerator, that.legendLabelToolTipGenerator)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.legendLabelURLGenerator, that.legendLabelURLGenerator)) {
      return false;
    }
    if(this.autoPopulateSectionPaint != that.autoPopulateSectionPaint) {
      return false;
    }
    if(this.autoPopulateSectionOutlinePaint != that.autoPopulateSectionOutlinePaint) {
      return false;
    }
    if(this.autoPopulateSectionOutlineStroke != that.autoPopulateSectionOutlineStroke) {
      return false;
    }
    if(!ObjectUtilities.equal(this.shadowGenerator, that.shadowGenerator)) {
      return false;
    }
    return true;
  }
  public boolean getAutoPopulateSectionOutlinePaint() {
    return this.autoPopulateSectionOutlinePaint;
  }
  public boolean getAutoPopulateSectionOutlineStroke() {
    return this.autoPopulateSectionOutlineStroke;
  }
  public boolean getAutoPopulateSectionPaint() {
    return this.autoPopulateSectionPaint;
  }
  public boolean getIgnoreNullValues() {
    return this.ignoreNullValues;
  }
  public boolean getIgnoreZeroValues() {
    return this.ignoreZeroValues;
  }
  public boolean getLabelLinksVisible() {
    return this.labelLinksVisible;
  }
  public boolean getSectionOutlinesVisible() {
    return this.sectionOutlinesVisible;
  }
  public boolean getSimpleLabels() {
    return this.simpleLabels;
  }
  public boolean isCircular() {
    return this.circular;
  }
  private double calculateAngleForValue(double value, double total) {
    if(this.direction == Rotation.CLOCKWISE) {
      return this.startAngle - (value / total * 360.0D);
    }
    else 
      if(this.direction == Rotation.ANTICLOCKWISE) {
        return this.startAngle + (value / total * 360.0D);
      }
    throw new RuntimeException("Unrecognised Rotation type.");
  }
  public double getExplodePercent(Comparable key) {
    double result = 0.0D;
    Map var_2017 = this.explodePercentages;
    if(var_2017 != null) {
      Number percent = (Number)this.explodePercentages.get(key);
      if(percent != null) {
        result = percent.doubleValue();
      }
    }
    return result;
  }
  public double getInteriorGap() {
    return this.interiorGap;
  }
  public double getLabelGap() {
    return this.labelGap;
  }
  protected double getLabelLinkDepth() {
    return 0.1D;
  }
  public double getLabelLinkMargin() {
    return this.labelLinkMargin;
  }
  public double getMaximumExplodePercent() {
    if(this.dataset == null) {
      return 0.0D;
    }
    double result = 0.0D;
    Iterator iterator = this.dataset.getKeys().iterator();
    while(iterator.hasNext()){
      Comparable key = (Comparable)iterator.next();
      Number explode = (Number)this.explodePercentages.get(key);
      if(explode != null) {
        result = Math.max(result, explode.doubleValue());
      }
    }
    return result;
  }
  public double getMaximumLabelWidth() {
    return this.maximumLabelWidth;
  }
  public double getMinimumArcAngleToDraw() {
    return this.minimumArcAngleToDraw;
  }
  public double getShadowXOffset() {
    return this.shadowXOffset;
  }
  public double getShadowYOffset() {
    return this.shadowYOffset;
  }
  public double getStartAngle() {
    return this.startAngle;
  }
  public int getPieIndex() {
    return this.pieIndex;
  }
  public void clearSectionOutlinePaints(boolean notify) {
    this.sectionOutlinePaintMap.clear();
    if(notify) {
      fireChangeEvent();
    }
  }
  public void clearSectionOutlineStrokes(boolean notify) {
    this.sectionOutlineStrokeMap.clear();
    if(notify) {
      fireChangeEvent();
    }
  }
  public void clearSectionPaints(boolean notify) {
    this.sectionPaintMap.clear();
    if(notify) {
      fireChangeEvent();
    }
  }
  public void clearSelection() {
    System.out.println("Clear selection.");
  }
  public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
    RectangleInsets insets = getInsets();
    insets.trim(area);
    if(info != null) {
      info.setPlotArea(area);
      info.setDataArea(area);
    }
    drawBackground(g2, area);
    drawOutline(g2, area);
    Shape savedClip = g2.getClip();
    g2.clip(area);
    Composite originalComposite = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getForegroundAlpha()));
    if(!DatasetUtilities.isEmptyOrNull(this.dataset)) {
      Graphics2D savedG2 = g2;
      Rectangle2D savedDataArea = area;
      BufferedImage dataImage = null;
      if(this.shadowGenerator != null) {
        dataImage = new BufferedImage((int)area.getWidth(), (int)area.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g2 = dataImage.createGraphics();
        g2.setRenderingHints(savedG2.getRenderingHints());
        area = new Rectangle(0, 0, dataImage.getWidth(), dataImage.getHeight());
      }
      drawPie(g2, area, info);
      if(this.shadowGenerator != null) {
        BufferedImage shadowImage = this.shadowGenerator.createDropShadow(dataImage);
        g2 = savedG2;
        area = savedDataArea;
        g2.drawImage(shadowImage, (int)savedDataArea.getX() + this.shadowGenerator.calculateOffsetX(), (int)savedDataArea.getY() + this.shadowGenerator.calculateOffsetY(), null);
        g2.drawImage(dataImage, (int)savedDataArea.getX(), (int)savedDataArea.getY(), null);
      }
    }
    else {
      drawNoDataMessage(g2, area);
    }
    g2.setClip(savedClip);
    g2.setComposite(originalComposite);
    drawOutline(g2, area);
  }
  protected void drawItem(Graphics2D g2, int section, boolean selected, Rectangle2D dataArea, PiePlotState state, int currentPass) {
    Number n = this.dataset.getValue(section);
    if(n == null) {
      return ;
    }
    double value = n.doubleValue();
    double angle1 = 0.0D;
    double angle2 = 0.0D;
    if(this.direction == Rotation.CLOCKWISE) {
      angle1 = state.getLatestAngle();
      angle2 = angle1 - value / state.getTotal() * 360.0D;
    }
    else 
      if(this.direction == Rotation.ANTICLOCKWISE) {
        angle1 = state.getLatestAngle();
        angle2 = angle1 + value / state.getTotal() * 360.0D;
      }
      else {
        throw new IllegalStateException("Rotation type not recognised.");
      }
    double angle = (angle2 - angle1);
    if(Math.abs(angle) > getMinimumArcAngleToDraw()) {
      double ep = 0.0D;
      double mep = getMaximumExplodePercent();
      if(mep > 0.0D) {
        ep = getExplodePercent(getSectionKey(section)) / mep;
      }
      Rectangle2D arcBounds = getArcBounds(state.getPieArea(), state.getExplodedPieArea(), angle1, angle, ep);
      Arc2D.Double arc = new Arc2D.Double(arcBounds, angle1, angle, Arc2D.PIE);
      if(currentPass == 0) {
        if(this.shadowPaint != null) {
          Shape shadowArc = ShapeUtilities.createTranslatedShape(arc, (float)this.shadowXOffset, (float)this.shadowYOffset);
          g2.setPaint(this.shadowPaint);
          g2.fill(shadowArc);
        }
      }
      else 
        if(currentPass == 1) {
          Comparable key = getSectionKey(section);
          Paint paint = lookupSectionPaint(key, selected);
          Shape savedClip = g2.getClip();
          g2.clip(arc);
          g2.setPaint(paint);
          g2.fill(arc);
          Paint outlinePaint = lookupSectionOutlinePaint(key, selected);
          Stroke outlineStroke = lookupSectionOutlineStroke(key, selected);
          if(this.sectionOutlinesVisible) {
            g2.setPaint(outlinePaint);
            g2.setStroke(outlineStroke);
            g2.draw(arc);
          }
          g2.setClip(savedClip);
          if(state.getInfo() != null) {
            EntityCollection entities = state.getEntityCollection();
            if(entities != null) {
              String tip = null;
              if(this.toolTipGenerator != null) {
                tip = this.toolTipGenerator.generateToolTip(this.dataset, key);
              }
              String url = null;
              if(this.urlGenerator != null) {
                url = this.urlGenerator.generateURL(this.dataset, key, this.pieIndex);
              }
              PieSectionEntity entity = new PieSectionEntity(arc, this.dataset, this.pieIndex, section, key, tip, url);
              entities.add(entity);
            }
          }
        }
    }
    state.setLatestAngle(angle2);
  }
  protected void drawLabels(Graphics2D g2, List keys, double totalValue, Rectangle2D plotArea, Rectangle2D linkArea, PiePlotState state) {
    Composite originalComposite = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
    DefaultKeyedValues leftKeys = new DefaultKeyedValues();
    DefaultKeyedValues rightKeys = new DefaultKeyedValues();
    double runningTotal = 0.0D;
    Iterator iterator = keys.iterator();
    while(iterator.hasNext()){
      Comparable key = (Comparable)iterator.next();
      boolean include = true;
      double v = 0.0D;
      Number n = this.dataset.getValue(key);
      if(n == null) {
        include = !this.ignoreNullValues;
      }
      else {
        v = n.doubleValue();
        include = this.ignoreZeroValues ? v > 0.0D : v >= 0.0D;
      }
      if(include) {
        runningTotal = runningTotal + v;
        double mid = this.startAngle + (this.direction.getFactor() * ((runningTotal - v / 2.0D) * 360) / totalValue);
        if(Math.cos(Math.toRadians(mid)) < 0.0D) {
          leftKeys.addValue(key, new Double(mid));
        }
        else {
          rightKeys.addValue(key, new Double(mid));
        }
      }
    }
    g2.setFont(getLabelFont());
    double marginX = plotArea.getX() + this.interiorGap * plotArea.getWidth();
    double gap = plotArea.getWidth() * this.labelGap;
    double ww = linkArea.getX() - gap - marginX;
    float labelWidth = (float)this.labelPadding.trimWidth(ww);
    if(this.labelGenerator != null) {
      drawLeftLabels(leftKeys, g2, plotArea, linkArea, labelWidth, state);
      drawRightLabels(rightKeys, g2, plotArea, linkArea, labelWidth, state);
    }
    g2.setComposite(originalComposite);
  }
  protected void drawLeftLabel(Graphics2D g2, PiePlotState state, PieLabelRecord record) {
    double anchorX = state.getLinkArea().getMinX();
    double targetX = anchorX - record.getGap();
    double targetY = record.getAllocatedY();
    if(this.labelLinksVisible) {
      double theta = record.getAngle();
      double linkX = state.getPieCenterX() + Math.cos(theta) * state.getPieWRadius() * record.getLinkPercent();
      double linkY = state.getPieCenterY() - Math.sin(theta) * state.getPieHRadius() * record.getLinkPercent();
      double elbowX = state.getPieCenterX() + Math.cos(theta) * state.getLinkArea().getWidth() / 2.0D;
      double elbowY = state.getPieCenterY() - Math.sin(theta) * state.getLinkArea().getHeight() / 2.0D;
      double anchorY = elbowY;
      g2.setPaint(this.labelLinkPaint);
      g2.setStroke(this.labelLinkStroke);
      PieLabelLinkStyle style = getLabelLinkStyle();
      if(style.equals(PieLabelLinkStyle.STANDARD)) {
        g2.draw(new Line2D.Double(linkX, linkY, elbowX, elbowY));
        g2.draw(new Line2D.Double(anchorX, anchorY, elbowX, elbowY));
        g2.draw(new Line2D.Double(anchorX, anchorY, targetX, targetY));
      }
      else 
        if(style.equals(PieLabelLinkStyle.QUAD_CURVE)) {
          QuadCurve2D q = new QuadCurve2D.Float();
          q.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY);
          g2.draw(q);
          g2.draw(new Line2D.Double(elbowX, elbowY, linkX, linkY));
        }
        else 
          if(style.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
            CubicCurve2D c = new CubicCurve2D.Float();
            c.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY, linkX, linkY);
            g2.draw(c);
          }
    }
    TextBox tb = record.getLabel();
    tb.draw(g2, (float)targetX, (float)targetY, RectangleAnchor.RIGHT);
  }
  protected void drawLeftLabels(KeyedValues leftKeys, Graphics2D g2, Rectangle2D plotArea, Rectangle2D linkArea, float maxLabelWidth, PiePlotState state) {
    this.labelDistributor.clear();
    double lGap = plotArea.getWidth() * this.labelGap;
    double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0D;
    for(int i = 0; i < leftKeys.getItemCount(); i++) {
      String label = this.labelGenerator.generateSectionLabel(this.dataset, leftKeys.getKey(i));
      if(label != null) {
        TextBlock block = TextUtilities.createTextBlock(label, this.labelFont, this.labelPaint, maxLabelWidth, new G2TextMeasurer(g2));
        TextBox labelBox = new TextBox(block);
        labelBox.setBackgroundPaint(this.labelBackgroundPaint);
        labelBox.setOutlinePaint(this.labelOutlinePaint);
        labelBox.setOutlineStroke(this.labelOutlineStroke);
        if(this.shadowGenerator == null) {
          labelBox.setShadowPaint(this.labelShadowPaint);
        }
        else {
          labelBox.setShadowPaint(null);
        }
        labelBox.setInteriorGap(this.labelPadding);
        double theta = Math.toRadians(leftKeys.getValue(i).doubleValue());
        double baseY = state.getPieCenterY() - Math.sin(theta) * verticalLinkRadius;
        double hh = labelBox.getHeight(g2);
        this.labelDistributor.addPieLabelRecord(new PieLabelRecord(leftKeys.getKey(i), theta, baseY, labelBox, hh, lGap / 2.0D + lGap / 2.0D * -Math.cos(theta), 1.0D - getLabelLinkDepth() + getExplodePercent(leftKeys.getKey(i))));
      }
    }
    double hh = plotArea.getHeight();
    double gap = hh * getInteriorGap();
    this.labelDistributor.distributeLabels(plotArea.getMinY() + gap, hh - 2 * gap);
    for(int i = 0; i < this.labelDistributor.getItemCount(); i++) {
      drawLeftLabel(g2, state, this.labelDistributor.getPieLabelRecord(i));
    }
  }
  protected void drawPie(Graphics2D g2, Rectangle2D plotArea, PlotRenderingInfo info) {
    PiePlotState state = initialise(g2, plotArea, this, null, info);
    Rectangle2D[] areas = calculateLinkAndExplodeAreas(g2, plotArea);
    Rectangle2D linkArea = areas[0];
    Rectangle2D explodeArea = areas[1];
    state.setLinkArea(linkArea);
    state.setExplodedPieArea(explodeArea);
    double maximumExplodePercent = getMaximumExplodePercent();
    double percent = maximumExplodePercent / (1.0D + maximumExplodePercent);
    double h1 = explodeArea.getWidth() * percent;
    double v1 = explodeArea.getHeight() * percent;
    Rectangle2D pieArea = new Rectangle2D.Double(explodeArea.getX() + h1 / 2.0D, explodeArea.getY() + v1 / 2.0D, explodeArea.getWidth() - h1, explodeArea.getHeight() - v1);
    if(DEBUG_DRAW_PIE_AREA) {
      g2.setPaint(Color.green);
      g2.draw(pieArea);
    }
    state.setPieArea(pieArea);
    state.setPieCenterX(pieArea.getCenterX());
    state.setPieCenterY(pieArea.getCenterY());
    state.setPieWRadius(pieArea.getWidth() / 2.0D);
    state.setPieHRadius(pieArea.getHeight() / 2.0D);
    if((this.dataset != null) && (this.dataset.getKeys().size() > 0)) {
      PieDatasetSelectionState ss = findSelectionStateForDataset(this.dataset, state);
      List keys = this.dataset.getKeys();
      double totalValue = DatasetUtilities.calculatePieDatasetTotal(this.dataset);
      int passesRequired = state.getPassesRequired();
      for(int pass = 0; pass < passesRequired; pass++) {
        double runningTotal = 0.0D;
        for(int section = 0; section < keys.size(); section++) {
          Number n = this.dataset.getValue(section);
          if(n != null) {
            double value = n.doubleValue();
            if(value > 0.0D) {
              runningTotal += value;
              boolean selected = false;
              if(ss != null) {
                selected = ss.isSelected(this.dataset.getKey(section));
              }
              drawItem(g2, section, selected, explodeArea, state, pass);
            }
          }
        }
      }
      if(this.simpleLabels) {
        drawSimpleLabels(g2, keys, totalValue, plotArea, linkArea, state);
      }
      else {
        drawLabels(g2, keys, totalValue, plotArea, linkArea, state);
      }
    }
    else {
      drawNoDataMessage(g2, plotArea);
    }
  }
  protected void drawRightLabel(Graphics2D g2, PiePlotState state, PieLabelRecord record) {
    double anchorX = state.getLinkArea().getMaxX();
    double targetX = anchorX + record.getGap();
    double targetY = record.getAllocatedY();
    if(this.labelLinksVisible) {
      double theta = record.getAngle();
      double linkX = state.getPieCenterX() + Math.cos(theta) * state.getPieWRadius() * record.getLinkPercent();
      double linkY = state.getPieCenterY() - Math.sin(theta) * state.getPieHRadius() * record.getLinkPercent();
      double elbowX = state.getPieCenterX() + Math.cos(theta) * state.getLinkArea().getWidth() / 2.0D;
      double elbowY = state.getPieCenterY() - Math.sin(theta) * state.getLinkArea().getHeight() / 2.0D;
      double anchorY = elbowY;
      g2.setPaint(this.labelLinkPaint);
      g2.setStroke(this.labelLinkStroke);
      PieLabelLinkStyle style = getLabelLinkStyle();
      if(style.equals(PieLabelLinkStyle.STANDARD)) {
        g2.draw(new Line2D.Double(linkX, linkY, elbowX, elbowY));
        g2.draw(new Line2D.Double(anchorX, anchorY, elbowX, elbowY));
        g2.draw(new Line2D.Double(anchorX, anchorY, targetX, targetY));
      }
      else 
        if(style.equals(PieLabelLinkStyle.QUAD_CURVE)) {
          QuadCurve2D q = new QuadCurve2D.Float();
          q.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY);
          g2.draw(q);
          g2.draw(new Line2D.Double(elbowX, elbowY, linkX, linkY));
        }
        else 
          if(style.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
            CubicCurve2D c = new CubicCurve2D.Float();
            c.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY, linkX, linkY);
            g2.draw(c);
          }
    }
    TextBox tb = record.getLabel();
    tb.draw(g2, (float)targetX, (float)targetY, RectangleAnchor.LEFT);
  }
  protected void drawRightLabels(KeyedValues keys, Graphics2D g2, Rectangle2D plotArea, Rectangle2D linkArea, float maxLabelWidth, PiePlotState state) {
    this.labelDistributor.clear();
    double lGap = plotArea.getWidth() * this.labelGap;
    double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0D;
    for(int i = 0; i < keys.getItemCount(); i++) {
      String label = this.labelGenerator.generateSectionLabel(this.dataset, keys.getKey(i));
      if(label != null) {
        TextBlock block = TextUtilities.createTextBlock(label, this.labelFont, this.labelPaint, maxLabelWidth, new G2TextMeasurer(g2));
        TextBox labelBox = new TextBox(block);
        labelBox.setBackgroundPaint(this.labelBackgroundPaint);
        labelBox.setOutlinePaint(this.labelOutlinePaint);
        labelBox.setOutlineStroke(this.labelOutlineStroke);
        if(this.shadowGenerator == null) {
          labelBox.setShadowPaint(this.labelShadowPaint);
        }
        else {
          labelBox.setShadowPaint(null);
        }
        labelBox.setInteriorGap(this.labelPadding);
        double theta = Math.toRadians(keys.getValue(i).doubleValue());
        double baseY = state.getPieCenterY() - Math.sin(theta) * verticalLinkRadius;
        double hh = labelBox.getHeight(g2);
        this.labelDistributor.addPieLabelRecord(new PieLabelRecord(keys.getKey(i), theta, baseY, labelBox, hh, lGap / 2.0D + lGap / 2.0D * Math.cos(theta), 1.0D - getLabelLinkDepth() + getExplodePercent(keys.getKey(i))));
      }
    }
    double hh = plotArea.getHeight();
    double gap = hh * getInteriorGap();
    this.labelDistributor.distributeLabels(plotArea.getMinY() + gap, hh - 2 * gap);
    for(int i = 0; i < this.labelDistributor.getItemCount(); i++) {
      drawRightLabel(g2, state, this.labelDistributor.getPieLabelRecord(i));
    }
  }
  protected void drawSimpleLabels(Graphics2D g2, List keys, double totalValue, Rectangle2D plotArea, Rectangle2D pieArea, PiePlotState state) {
    Composite originalComposite = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
    Rectangle2D labelsArea = this.simpleLabelOffset.createInsetRectangle(pieArea);
    double runningTotal = 0.0D;
    Iterator iterator = keys.iterator();
    while(iterator.hasNext()){
      Comparable key = (Comparable)iterator.next();
      boolean include = true;
      double v = 0.0D;
      Number n = getDataset().getValue(key);
      if(n == null) {
        include = !getIgnoreNullValues();
      }
      else {
        v = n.doubleValue();
        include = getIgnoreZeroValues() ? v > 0.0D : v >= 0.0D;
      }
      if(include) {
        runningTotal = runningTotal + v;
        double mid = getStartAngle() + (getDirection().getFactor() * ((runningTotal - v / 2.0D) * 360) / totalValue);
        Arc2D arc = new Arc2D.Double(labelsArea, getStartAngle(), mid - getStartAngle(), Arc2D.OPEN);
        int x = (int)arc.getEndPoint().getX();
        int y = (int)arc.getEndPoint().getY();
        PieSectionLabelGenerator labelGenerator = getLabelGenerator();
        if(labelGenerator == null) {
          continue ;
        }
        String label = labelGenerator.generateSectionLabel(this.dataset, key);
        if(label == null) {
          continue ;
        }
        g2.setFont(this.labelFont);
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D bounds = TextUtilities.getTextBounds(label, g2, fm);
        Rectangle2D out = this.labelPadding.createOutsetRectangle(bounds);
        Shape bg = ShapeUtilities.createTranslatedShape(out, x - bounds.getCenterX(), y - bounds.getCenterY());
        if(this.labelShadowPaint != null && this.shadowGenerator == null) {
          Shape shadow = ShapeUtilities.createTranslatedShape(bg, this.shadowXOffset, this.shadowYOffset);
          g2.setPaint(this.labelShadowPaint);
          g2.fill(shadow);
        }
        if(this.labelBackgroundPaint != null) {
          g2.setPaint(this.labelBackgroundPaint);
          g2.fill(bg);
        }
        if(this.labelOutlinePaint != null && this.labelOutlineStroke != null) {
          g2.setPaint(this.labelOutlinePaint);
          g2.setStroke(this.labelOutlineStroke);
          g2.draw(bg);
        }
        g2.setPaint(this.labelPaint);
        g2.setFont(this.labelFont);
        TextUtilities.drawAlignedString(getLabelGenerator().generateSectionLabel(getDataset(), key), g2, x, y, TextAnchor.CENTER);
      }
    }
    g2.setComposite(originalComposite);
  }
  public void handleMouseWheelRotation(int rotateClicks) {
    setStartAngle(this.startAngle + rotateClicks * 4.0D);
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.baseSectionPaint = SerialUtilities.readPaint(stream);
    this.baseSectionOutlinePaint = SerialUtilities.readPaint(stream);
    this.baseSectionOutlineStroke = SerialUtilities.readStroke(stream);
    this.shadowPaint = SerialUtilities.readPaint(stream);
    this.labelPaint = SerialUtilities.readPaint(stream);
    this.labelBackgroundPaint = SerialUtilities.readPaint(stream);
    this.labelOutlinePaint = SerialUtilities.readPaint(stream);
    this.labelOutlineStroke = SerialUtilities.readStroke(stream);
    this.labelShadowPaint = SerialUtilities.readPaint(stream);
    this.labelLinkPaint = SerialUtilities.readPaint(stream);
    this.labelLinkStroke = SerialUtilities.readStroke(stream);
    this.legendItemShape = SerialUtilities.readShape(stream);
  }
  public void select(double x, double y, Rectangle2D dataArea, RenderingSource source) {
    System.out.println("select " + x + ", " + y);
    PieDatasetSelectionState state = findSelectionStateForDataset(dataset, source);
    if(state == null) {
      return ;
    }
    Rectangle2D[] areas = calculateLinkAndExplodeAreas(null, dataArea);
    Rectangle2D linkArea = areas[0];
    Rectangle2D explodeArea = areas[1];
    double maximumExplodePercent = getMaximumExplodePercent();
    double percent = maximumExplodePercent / (1.0D + maximumExplodePercent);
    double h1 = explodeArea.getWidth() * percent;
    double v1 = explodeArea.getHeight() * percent;
    Rectangle2D pieArea = new Rectangle2D.Double(explodeArea.getX() + h1 / 2.0D, explodeArea.getY() + v1 / 2.0D, explodeArea.getWidth() - h1, explodeArea.getHeight() - v1);
    if((this.dataset != null) && (this.dataset.getKeys().size() > 0)) {
      List keys = this.dataset.getKeys();
      double total = DatasetUtilities.calculatePieDatasetTotal(this.dataset);
      double runningTotal = 0.0D;
      for(int section = 0; section < keys.size(); section++) {
        Number n = this.dataset.getValue(section);
        if(n == null) {
          continue ;
        }
        double value = n.doubleValue();
        if(value > 0.0D) {
          double angle0 = calculateAngleForValue(runningTotal, total);
          double angle1 = calculateAngleForValue(runningTotal + value, total);
          runningTotal += value;
          System.out.println(this.dataset.getValue(section));
          System.out.println(angle0);
          System.out.println(angle1);
          double angle = (angle1 - angle0);
          if(Math.abs(angle) > getMinimumArcAngleToDraw()) {
            double ep = 0.0D;
            double mep = getMaximumExplodePercent();
            if(mep > 0.0D) {
              ep = getExplodePercent(getSectionKey(section)) / mep;
            }
            Rectangle2D arcBounds = getArcBounds(pieArea, explodeArea, angle0, angle, ep);
            Arc2D.Double arc = new Arc2D.Double(arcBounds, angle0, angle, Arc2D.PIE);
            if(arc.contains(x, y)) {
              Comparable key = this.dataset.getKey(section);
              state.setSelected(key, !state.isSelected(key));
              System.out.println(key + " is " + state.isSelected(key));
            }
          }
        }
      }
    }
  }
  public void select(GeneralPath region, Rectangle2D dataArea, RenderingSource source) {
  }
  public void setAutoPopulateSectionOutlinePaint(boolean auto) {
    this.autoPopulateSectionOutlinePaint = auto;
    fireChangeEvent();
  }
  public void setAutoPopulateSectionOutlineStroke(boolean auto) {
    this.autoPopulateSectionOutlineStroke = auto;
    fireChangeEvent();
  }
  public void setAutoPopulateSectionPaint(boolean auto) {
    this.autoPopulateSectionPaint = auto;
    fireChangeEvent();
  }
  public void setBaseSectionOutlinePaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.baseSectionOutlinePaint = paint;
    fireChangeEvent();
  }
  public void setBaseSectionOutlineStroke(Stroke stroke) {
    if(stroke == null) {
      throw new IllegalArgumentException("Null \'stroke\' argument.");
    }
    this.baseSectionOutlineStroke = stroke;
    fireChangeEvent();
  }
  public void setBaseSectionPaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.baseSectionPaint = paint;
    fireChangeEvent();
  }
  public void setCircular(boolean flag) {
    setCircular(flag, true);
  }
  public void setCircular(boolean circular, boolean notify) {
    this.circular = circular;
    if(notify) {
      fireChangeEvent();
    }
  }
  public void setDataset(PieDataset dataset) {
    PieDataset existing = this.dataset;
    if(existing != null) {
      existing.removeChangeListener(this);
    }
    this.dataset = dataset;
    if(dataset != null) {
      setDatasetGroup(dataset.getGroup());
      dataset.addChangeListener(this);
    }
    DatasetChangeEvent event = new DatasetChangeEvent(this, dataset, new PieDatasetChangeInfo(PieDatasetChangeType.UPDATE, -1, -1));
    datasetChanged(event);
  }
  public void setDirection(Rotation direction) {
    if(direction == null) {
      throw new IllegalArgumentException("Null \'direction\' argument.");
    }
    this.direction = direction;
    fireChangeEvent();
  }
  public void setExplodePercent(Comparable key, double percent) {
    if(key == null) {
      throw new IllegalArgumentException("Null \'key\' argument.");
    }
    if(this.explodePercentages == null) {
      this.explodePercentages = new TreeMap();
    }
    this.explodePercentages.put(key, new Double(percent));
    fireChangeEvent();
  }
  public void setIgnoreNullValues(boolean flag) {
    this.ignoreNullValues = flag;
    fireChangeEvent();
  }
  public void setIgnoreZeroValues(boolean flag) {
    this.ignoreZeroValues = flag;
    fireChangeEvent();
  }
  public void setInteriorGap(double percent) {
    if((percent < 0.0D) || (percent > MAX_INTERIOR_GAP)) {
      throw new IllegalArgumentException("Invalid \'percent\' (" + percent + ") argument.");
    }
    if(this.interiorGap != percent) {
      this.interiorGap = percent;
      fireChangeEvent();
    }
  }
  public void setLabelBackgroundPaint(Paint paint) {
    this.labelBackgroundPaint = paint;
    fireChangeEvent();
  }
  public void setLabelDistributor(AbstractPieLabelDistributor distributor) {
    if(distributor == null) {
      throw new IllegalArgumentException("Null \'distributor\' argument.");
    }
    this.labelDistributor = distributor;
    fireChangeEvent();
  }
  public void setLabelFont(Font font) {
    if(font == null) {
      throw new IllegalArgumentException("Null \'font\' argument.");
    }
    this.labelFont = font;
    fireChangeEvent();
  }
  public void setLabelGap(double gap) {
    this.labelGap = gap;
    fireChangeEvent();
  }
  public void setLabelGenerator(PieSectionLabelGenerator generator) {
    this.labelGenerator = generator;
    fireChangeEvent();
  }
  public void setLabelLinkMargin(double margin) {
    this.labelLinkMargin = margin;
    fireChangeEvent();
  }
  public void setLabelLinkPaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.labelLinkPaint = paint;
    fireChangeEvent();
  }
  public void setLabelLinkStroke(Stroke stroke) {
    if(stroke == null) {
      throw new IllegalArgumentException("Null \'stroke\' argument.");
    }
    this.labelLinkStroke = stroke;
    fireChangeEvent();
  }
  public void setLabelLinkStyle(PieLabelLinkStyle style) {
    if(style == null) {
      throw new IllegalArgumentException("Null \'style\' argument.");
    }
    this.labelLinkStyle = style;
    fireChangeEvent();
  }
  public void setLabelLinksVisible(boolean visible) {
    this.labelLinksVisible = visible;
    fireChangeEvent();
  }
  public void setLabelOutlinePaint(Paint paint) {
    this.labelOutlinePaint = paint;
    fireChangeEvent();
  }
  public void setLabelOutlineStroke(Stroke stroke) {
    this.labelOutlineStroke = stroke;
    fireChangeEvent();
  }
  public void setLabelPadding(RectangleInsets padding) {
    if(padding == null) {
      throw new IllegalArgumentException("Null \'padding\' argument.");
    }
    this.labelPadding = padding;
    fireChangeEvent();
  }
  public void setLabelPaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.labelPaint = paint;
    fireChangeEvent();
  }
  public void setLabelShadowPaint(Paint paint) {
    this.labelShadowPaint = paint;
    fireChangeEvent();
  }
  public void setLegendItemShape(Shape shape) {
    if(shape == null) {
      throw new IllegalArgumentException("Null \'shape\' argument.");
    }
    this.legendItemShape = shape;
    fireChangeEvent();
  }
  public void setLegendLabelGenerator(PieSectionLabelGenerator generator) {
    if(generator == null) {
      throw new IllegalArgumentException("Null \'generator\' argument.");
    }
    this.legendLabelGenerator = generator;
    fireChangeEvent();
  }
  public void setLegendLabelToolTipGenerator(PieSectionLabelGenerator generator) {
    this.legendLabelToolTipGenerator = generator;
    fireChangeEvent();
  }
  public void setLegendLabelURLGenerator(PieURLGenerator generator) {
    this.legendLabelURLGenerator = generator;
    fireChangeEvent();
  }
  public void setMaximumLabelWidth(double width) {
    this.maximumLabelWidth = width;
    fireChangeEvent();
  }
  public void setMinimumArcAngleToDraw(double angle) {
    this.minimumArcAngleToDraw = angle;
  }
  public void setPieIndex(int index) {
    this.pieIndex = index;
  }
  public void setSectionOutlinePaint(Comparable key, Paint paint) {
    this.sectionOutlinePaintMap.put(key, paint);
    fireChangeEvent();
  }
  public void setSectionOutlineStroke(Comparable key, Stroke stroke) {
    this.sectionOutlineStrokeMap.put(key, stroke);
    fireChangeEvent();
  }
  public void setSectionOutlinesVisible(boolean visible) {
    this.sectionOutlinesVisible = visible;
    fireChangeEvent();
  }
  public void setSectionPaint(Comparable key, Paint paint) {
    this.sectionPaintMap.put(key, paint);
    fireChangeEvent();
  }
  public void setShadowGenerator(ShadowGenerator generator) {
    this.shadowGenerator = generator;
    fireChangeEvent();
  }
  public void setShadowPaint(Paint paint) {
    this.shadowPaint = paint;
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
  public void setSimpleLabelOffset(RectangleInsets offset) {
    if(offset == null) {
      throw new IllegalArgumentException("Null \'offset\' argument.");
    }
    this.simpleLabelOffset = offset;
    fireChangeEvent();
  }
  public void setSimpleLabels(boolean simple) {
    this.simpleLabels = simple;
    fireChangeEvent();
  }
  public void setStartAngle(double angle) {
    this.startAngle = angle;
    fireChangeEvent();
  }
  public void setToolTipGenerator(PieToolTipGenerator generator) {
    this.toolTipGenerator = generator;
    fireChangeEvent();
  }
  public void setURLGenerator(PieURLGenerator generator) {
    this.urlGenerator = generator;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.baseSectionPaint, stream);
    SerialUtilities.writePaint(this.baseSectionOutlinePaint, stream);
    SerialUtilities.writeStroke(this.baseSectionOutlineStroke, stream);
    SerialUtilities.writePaint(this.shadowPaint, stream);
    SerialUtilities.writePaint(this.labelPaint, stream);
    SerialUtilities.writePaint(this.labelBackgroundPaint, stream);
    SerialUtilities.writePaint(this.labelOutlinePaint, stream);
    SerialUtilities.writeStroke(this.labelOutlineStroke, stream);
    SerialUtilities.writePaint(this.labelShadowPaint, stream);
    SerialUtilities.writePaint(this.labelLinkPaint, stream);
    SerialUtilities.writeStroke(this.labelLinkStroke, stream);
    SerialUtilities.writeShape(this.legendItemShape, stream);
  }
}