package org.jfree.chart.plot;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.event.EventListenerList;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.event.MarkerChangeListener;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.text.G2TextMeasurer;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.text.TextUtilities;
import org.jfree.chart.util.Align;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.event.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

abstract public class Plot implements AxisChangeListener, DatasetChangeListener, AnnotationChangeListener, MarkerChangeListener, LegendItemSource, PublicCloneable, Cloneable, Serializable  {
  final private static long serialVersionUID = -8831571430103671324L;
  final public static Number ZERO = new Integer(0);
  final public static RectangleInsets DEFAULT_INSETS = new RectangleInsets(4.0D, 8.0D, 4.0D, 8.0D);
  final public static Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(0.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  final public static Paint DEFAULT_OUTLINE_PAINT = Color.gray;
  final public static float DEFAULT_FOREGROUND_ALPHA = 1.0F;
  final public static float DEFAULT_BACKGROUND_ALPHA = 1.0F;
  final public static Paint DEFAULT_BACKGROUND_PAINT = Color.LIGHT_GRAY;
  final public static int MINIMUM_WIDTH_TO_DRAW = 10;
  final public static int MINIMUM_HEIGHT_TO_DRAW = 10;
  final public static Shape DEFAULT_LEGEND_ITEM_BOX = new Rectangle2D.Double(-4.0D, -4.0D, 8.0D, 8.0D);
  final public static Shape DEFAULT_LEGEND_ITEM_CIRCLE = new Ellipse2D.Double(-4.0D, -4.0D, 8.0D, 8.0D);
  private Plot parent;
  private DatasetGroup datasetGroup;
  private String noDataMessage;
  private Font noDataMessageFont;
  private transient Paint noDataMessagePaint;
  private RectangleInsets insets;
  private boolean outlineVisible;
  private transient Stroke outlineStroke;
  private transient Paint outlinePaint;
  private transient Paint backgroundPaint;
  private transient Image backgroundImage;
  private int backgroundImageAlignment = Align.FIT;
  private float backgroundImageAlpha = 0.5F;
  private float foregroundAlpha;
  private float backgroundAlpha;
  private DrawingSupplier drawingSupplier;
  private transient EventListenerList listenerList;
  private boolean notify;
  protected Plot() {
    super();
    this.parent = null;
    this.insets = DEFAULT_INSETS;
    this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;
    this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;
    this.backgroundImage = null;
    this.outlineVisible = true;
    this.outlineStroke = DEFAULT_OUTLINE_STROKE;
    this.outlinePaint = DEFAULT_OUTLINE_PAINT;
    this.foregroundAlpha = DEFAULT_FOREGROUND_ALPHA;
    this.noDataMessage = null;
    this.noDataMessageFont = new Font("Tahoma", Font.PLAIN, 12);
    this.noDataMessagePaint = Color.black;
    this.drawingSupplier = new DefaultDrawingSupplier();
    this.notify = true;
    this.listenerList = new EventListenerList();
  }
  public DatasetGroup getDatasetGroup() {
    return this.datasetGroup;
  }
  public DrawingSupplier getDrawingSupplier() {
    DrawingSupplier result = null;
    Plot p = getParent();
    if(p != null) {
      result = p.getDrawingSupplier();
    }
    else {
      result = this.drawingSupplier;
    }
    return result;
  }
  public Font getNoDataMessageFont() {
    return this.noDataMessageFont;
  }
  public Image getBackgroundImage() {
    return this.backgroundImage;
  }
  public LegendItemCollection getLegendItems() {
    return null;
  }
  public Object clone() throws CloneNotSupportedException {
    Plot clone = (Plot)super.clone();
    if(this.datasetGroup != null) {
      clone.datasetGroup = (DatasetGroup)ObjectUtilities.clone(this.datasetGroup);
    }
    clone.drawingSupplier = (DrawingSupplier)ObjectUtilities.clone(this.drawingSupplier);
    clone.listenerList = new EventListenerList();
    return clone;
  }
  public Paint getBackgroundPaint() {
    return this.backgroundPaint;
  }
  public Paint getNoDataMessagePaint() {
    return this.noDataMessagePaint;
  }
  public Paint getOutlinePaint() {
    return this.outlinePaint;
  }
  public Plot getParent() {
    return this.parent;
  }
  public Plot getRootPlot() {
    Plot p = getParent();
    if(p == null) {
      return this;
    }
    else {
      return p.getRootPlot();
    }
  }
  public static RectangleEdge resolveDomainAxisLocation(AxisLocation location, PlotOrientation orientation) {
    if(location == null) {
      throw new IllegalArgumentException("Null \'location\' argument.");
    }
    if(orientation == null) {
      throw new IllegalArgumentException("Null \'orientation\' argument.");
    }
    RectangleEdge result = null;
    if(location == AxisLocation.TOP_OR_RIGHT) {
      if(orientation == PlotOrientation.HORIZONTAL) {
        result = RectangleEdge.RIGHT;
      }
      else 
        if(orientation == PlotOrientation.VERTICAL) {
          result = RectangleEdge.TOP;
        }
    }
    else 
      if(location == AxisLocation.TOP_OR_LEFT) {
        if(orientation == PlotOrientation.HORIZONTAL) {
          result = RectangleEdge.LEFT;
        }
        else 
          if(orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.TOP;
          }
      }
      else 
        if(location == AxisLocation.BOTTOM_OR_RIGHT) {
          if(orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.RIGHT;
          }
          else 
            if(orientation == PlotOrientation.VERTICAL) {
              result = RectangleEdge.BOTTOM;
            }
        }
        else 
          if(location == AxisLocation.BOTTOM_OR_LEFT) {
            if(orientation == PlotOrientation.HORIZONTAL) {
              result = RectangleEdge.LEFT;
            }
            else 
              if(orientation == PlotOrientation.VERTICAL) {
                result = RectangleEdge.BOTTOM;
              }
          }
    if(result == null) {
      throw new IllegalStateException("resolveDomainAxisLocation()");
    }
    return result;
  }
  public static RectangleEdge resolveRangeAxisLocation(AxisLocation location, PlotOrientation orientation) {
    if(location == null) {
      throw new IllegalArgumentException("Null \'location\' argument.");
    }
    if(orientation == null) {
      throw new IllegalArgumentException("Null \'orientation\' argument.");
    }
    RectangleEdge result = null;
    if(location == AxisLocation.TOP_OR_RIGHT) {
      if(orientation == PlotOrientation.HORIZONTAL) {
        RectangleEdge var_2124 = RectangleEdge.TOP;
        result = var_2124;
      }
      else 
        if(orientation == PlotOrientation.VERTICAL) {
          result = RectangleEdge.RIGHT;
        }
    }
    else 
      if(location == AxisLocation.TOP_OR_LEFT) {
        if(orientation == PlotOrientation.HORIZONTAL) {
          result = RectangleEdge.TOP;
        }
        else 
          if(orientation == PlotOrientation.VERTICAL) {
            result = RectangleEdge.LEFT;
          }
      }
      else 
        if(location == AxisLocation.BOTTOM_OR_RIGHT) {
          if(orientation == PlotOrientation.HORIZONTAL) {
            result = RectangleEdge.BOTTOM;
          }
          else 
            if(orientation == PlotOrientation.VERTICAL) {
              result = RectangleEdge.RIGHT;
            }
        }
        else 
          if(location == AxisLocation.BOTTOM_OR_LEFT) {
            if(orientation == PlotOrientation.HORIZONTAL) {
              result = RectangleEdge.BOTTOM;
            }
            else 
              if(orientation == PlotOrientation.VERTICAL) {
                result = RectangleEdge.LEFT;
              }
          }
    if(result == null) {
      throw new IllegalStateException("resolveRangeAxisLocation()");
    }
    return result;
  }
  public RectangleInsets getInsets() {
    return this.insets;
  }
  public String getNoDataMessage() {
    return this.noDataMessage;
  }
  abstract public String getPlotType();
  public Stroke getOutlineStroke() {
    return this.outlineStroke;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof Plot)) {
      return false;
    }
    Plot that = (Plot)obj;
    if(!ObjectUtilities.equal(this.noDataMessage, that.noDataMessage)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.noDataMessageFont, that.noDataMessageFont)) {
      return false;
    }
    if(!PaintUtilities.equal(this.noDataMessagePaint, that.noDataMessagePaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.insets, that.insets)) {
      return false;
    }
    if(this.outlineVisible != that.outlineVisible) {
      return false;
    }
    if(!ObjectUtilities.equal(this.outlineStroke, that.outlineStroke)) {
      return false;
    }
    if(!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
      return false;
    }
    if(!PaintUtilities.equal(this.backgroundPaint, that.backgroundPaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.backgroundImage, that.backgroundImage)) {
      return false;
    }
    if(this.backgroundImageAlignment != that.backgroundImageAlignment) {
      return false;
    }
    if(this.backgroundImageAlpha != that.backgroundImageAlpha) {
      return false;
    }
    if(this.foregroundAlpha != that.foregroundAlpha) {
      return false;
    }
    if(this.backgroundAlpha != that.backgroundAlpha) {
      return false;
    }
    if(!this.drawingSupplier.equals(that.drawingSupplier)) {
      return false;
    }
    if(this.notify != that.notify) {
      return false;
    }
    return true;
  }
  public boolean isNotify() {
    return this.notify;
  }
  public boolean isOutlineVisible() {
    return this.outlineVisible;
  }
  public boolean isSubplot() {
    return (getParent() != null);
  }
  protected double getRectX(double x, double w1, double w2, RectangleEdge edge) {
    double result = x;
    if(edge == RectangleEdge.LEFT) {
      result = result + w1;
    }
    else 
      if(edge == RectangleEdge.RIGHT) {
        result = result + w2;
      }
    return result;
  }
  protected double getRectY(double y, double h1, double h2, RectangleEdge edge) {
    double result = y;
    if(edge == RectangleEdge.TOP) {
      result = result + h1;
    }
    else 
      if(edge == RectangleEdge.BOTTOM) {
        result = result + h2;
      }
    return result;
  }
  public float getBackgroundAlpha() {
    return this.backgroundAlpha;
  }
  public float getBackgroundImageAlpha() {
    return this.backgroundImageAlpha;
  }
  public float getForegroundAlpha() {
    return this.foregroundAlpha;
  }
  public int getBackgroundImageAlignment() {
    return this.backgroundImageAlignment;
  }
  public void addChangeListener(PlotChangeListener listener) {
    this.listenerList.add(PlotChangeListener.class, listener);
  }
  public void annotationChanged(AnnotationChangeEvent event) {
    fireChangeEvent();
  }
  public void axisChanged(AxisChangeEvent event) {
    fireChangeEvent();
  }
  protected void createAndAddEntity(Rectangle2D dataArea, PlotRenderingInfo plotState, String toolTip, String urlText) {
    if(plotState != null && plotState.getOwner() != null) {
      EntityCollection e = plotState.getOwner().getEntityCollection();
      if(e != null) {
        e.add(new PlotEntity(dataArea, this, toolTip, urlText));
      }
    }
  }
  public void datasetChanged(DatasetChangeEvent event) {
    PlotChangeEvent newEvent = new PlotChangeEvent(this);
    newEvent.setType(ChartChangeEventType.DATASET_UPDATED);
    notifyListeners(newEvent);
  }
  abstract public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info);
  public void drawBackground(Graphics2D g2, Rectangle2D area) {
    fillBackground(g2, area);
    drawBackgroundImage(g2, area);
  }
  public void drawBackgroundImage(Graphics2D g2, Rectangle2D area) {
    if(this.backgroundImage == null) {
      return ;
    }
    Composite savedComposite = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.backgroundImageAlpha));
    Rectangle2D dest = new Rectangle2D.Double(0.0D, 0.0D, this.backgroundImage.getWidth(null), this.backgroundImage.getHeight(null));
    Align.align(dest, area, this.backgroundImageAlignment);
    Shape savedClip = g2.getClip();
    g2.clip(area);
    g2.drawImage(this.backgroundImage, (int)dest.getX(), (int)dest.getY(), (int)dest.getWidth() + 1, (int)dest.getHeight() + 1, null);
    g2.setClip(savedClip);
    g2.setComposite(savedComposite);
  }
  protected void drawNoDataMessage(Graphics2D g2, Rectangle2D area) {
    Shape savedClip = g2.getClip();
    g2.clip(area);
    String message = this.noDataMessage;
    if(message != null) {
      g2.setFont(this.noDataMessageFont);
      g2.setPaint(this.noDataMessagePaint);
      TextBlock block = TextUtilities.createTextBlock(this.noDataMessage, this.noDataMessageFont, this.noDataMessagePaint, 0.9F * (float)area.getWidth(), new G2TextMeasurer(g2));
      block.draw(g2, (float)area.getCenterX(), (float)area.getCenterY(), TextBlockAnchor.CENTER);
    }
    g2.setClip(savedClip);
  }
  public void drawOutline(Graphics2D g2, Rectangle2D area) {
    if(!this.outlineVisible) {
      return ;
    }
    if((this.outlineStroke != null) && (this.outlinePaint != null)) {
      g2.setStroke(this.outlineStroke);
      g2.setPaint(this.outlinePaint);
      g2.draw(area);
    }
  }
  protected void fillBackground(Graphics2D g2, Rectangle2D area) {
    fillBackground(g2, area, PlotOrientation.VERTICAL);
  }
  protected void fillBackground(Graphics2D g2, Rectangle2D area, PlotOrientation orientation) {
    if(orientation == null) {
      throw new IllegalArgumentException("Null \'orientation\' argument.");
    }
    if(this.backgroundPaint == null) {
      return ;
    }
    Paint p = this.backgroundPaint;
    if(p instanceof GradientPaint) {
      GradientPaint gp = (GradientPaint)p;
      if(orientation == PlotOrientation.VERTICAL) {
        p = new GradientPaint((float)area.getCenterX(), (float)area.getMaxY(), gp.getColor1(), (float)area.getCenterX(), (float)area.getMinY(), gp.getColor2());
      }
      else 
        if(orientation == PlotOrientation.HORIZONTAL) {
          p = new GradientPaint((float)area.getMinX(), (float)area.getCenterY(), gp.getColor1(), (float)area.getMaxX(), (float)area.getCenterY(), gp.getColor2());
        }
    }
    Composite originalComposite = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.backgroundAlpha));
    g2.setPaint(p);
    g2.fill(area);
    g2.setComposite(originalComposite);
  }
  protected void fireChangeEvent() {
    notifyListeners(new PlotChangeEvent(this));
  }
  public void handleClick(int x, int y, PlotRenderingInfo info) {
  }
  public void markerChanged(MarkerChangeEvent event) {
    fireChangeEvent();
  }
  public void notifyListeners(PlotChangeEvent event) {
    if(!this.notify) {
      return ;
    }
    Object[] listeners = this.listenerList.getListenerList();
    for(int i = listeners.length - 2; i >= 0; i -= 2) {
      if(listeners[i] == PlotChangeListener.class) {
        ((PlotChangeListener)listeners[i + 1]).plotChanged(event);
      }
    }
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.noDataMessagePaint = SerialUtilities.readPaint(stream);
    this.outlineStroke = SerialUtilities.readStroke(stream);
    this.outlinePaint = SerialUtilities.readPaint(stream);
    this.backgroundPaint = SerialUtilities.readPaint(stream);
    this.listenerList = new EventListenerList();
  }
  public void removeChangeListener(PlotChangeListener listener) {
    this.listenerList.remove(PlotChangeListener.class, listener);
  }
  public void setBackgroundAlpha(float alpha) {
    if(this.backgroundAlpha != alpha) {
      this.backgroundAlpha = alpha;
      fireChangeEvent();
    }
  }
  public void setBackgroundImage(Image image) {
    this.backgroundImage = image;
    fireChangeEvent();
  }
  public void setBackgroundImageAlignment(int alignment) {
    if(this.backgroundImageAlignment != alignment) {
      this.backgroundImageAlignment = alignment;
      fireChangeEvent();
    }
  }
  public void setBackgroundImageAlpha(float alpha) {
    if(alpha < 0.0F || alpha > 1.0F) 
      throw new IllegalArgumentException("The \'alpha\' value must be in the range 0.0f to 1.0f.");
    if(this.backgroundImageAlpha != alpha) {
      this.backgroundImageAlpha = alpha;
      fireChangeEvent();
    }
  }
  public void setBackgroundPaint(Paint paint) {
    if(paint == null) {
      if(this.backgroundPaint != null) {
        this.backgroundPaint = null;
        fireChangeEvent();
      }
    }
    else {
      if(this.backgroundPaint != null) {
        if(this.backgroundPaint.equals(paint)) {
          return ;
        }
      }
      this.backgroundPaint = paint;
      fireChangeEvent();
    }
  }
  protected void setDatasetGroup(DatasetGroup group) {
    this.datasetGroup = group;
  }
  public void setDrawingSupplier(DrawingSupplier supplier) {
    this.drawingSupplier = supplier;
    fireChangeEvent();
  }
  public void setDrawingSupplier(DrawingSupplier supplier, boolean notify) {
    this.drawingSupplier = supplier;
    if(notify) {
      fireChangeEvent();
    }
  }
  public void setForegroundAlpha(float alpha) {
    if(this.foregroundAlpha != alpha) {
      this.foregroundAlpha = alpha;
      fireChangeEvent();
    }
  }
  public void setInsets(RectangleInsets insets) {
    setInsets(insets, true);
  }
  public void setInsets(RectangleInsets insets, boolean notify) {
    if(insets == null) {
      throw new IllegalArgumentException("Null \'insets\' argument.");
    }
    if(!this.insets.equals(insets)) {
      this.insets = insets;
      if(notify) {
        fireChangeEvent();
      }
    }
  }
  public void setNoDataMessage(String message) {
    this.noDataMessage = message;
    fireChangeEvent();
  }
  public void setNoDataMessageFont(Font font) {
    if(font == null) {
      throw new IllegalArgumentException("Null \'font\' argument.");
    }
    this.noDataMessageFont = font;
    fireChangeEvent();
  }
  public void setNoDataMessagePaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.noDataMessagePaint = paint;
    fireChangeEvent();
  }
  public void setNotify(boolean notify) {
    this.notify = notify;
    if(notify) {
      notifyListeners(new PlotChangeEvent(this));
    }
  }
  public void setOutlinePaint(Paint paint) {
    if(paint == null) {
      if(this.outlinePaint != null) {
        this.outlinePaint = null;
        fireChangeEvent();
      }
    }
    else {
      if(this.outlinePaint != null) {
        if(this.outlinePaint.equals(paint)) {
          return ;
        }
      }
      this.outlinePaint = paint;
      fireChangeEvent();
    }
  }
  public void setOutlineStroke(Stroke stroke) {
    if(stroke == null) {
      if(this.outlineStroke != null) {
        this.outlineStroke = null;
        fireChangeEvent();
      }
    }
    else {
      if(this.outlineStroke != null) {
        if(this.outlineStroke.equals(stroke)) {
          return ;
        }
      }
      this.outlineStroke = stroke;
      fireChangeEvent();
    }
  }
  public void setOutlineVisible(boolean visible) {
    this.outlineVisible = visible;
    fireChangeEvent();
  }
  public void setParent(Plot parent) {
    this.parent = parent;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.noDataMessagePaint, stream);
    SerialUtilities.writeStroke(this.outlineStroke, stream);
    SerialUtilities.writePaint(this.outlinePaint, stream);
    SerialUtilities.writePaint(this.backgroundPaint, stream);
  }
  public void zoom(double percent) {
  }
}