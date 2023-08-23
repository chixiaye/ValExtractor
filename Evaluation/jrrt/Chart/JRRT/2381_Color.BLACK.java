package org.jfree.chart.renderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.ObjectList;
import org.jfree.chart.util.PaintList;
import org.jfree.chart.util.ShapeList;
import org.jfree.chart.util.StrokeList;

public class RenderAttributes implements Cloneable, Serializable  {
  private boolean allowNull;
  private PaintList paintList;
  private Paint defaultPaint;
  private StrokeList strokeList;
  private Stroke defaultStroke;
  private ShapeList shapeList;
  private Shape defaultShape;
  private PaintList fillPaintList;
  private Paint defaultFillPaint;
  private PaintList outlinePaintList;
  private Paint defaultOutlinePaint;
  private StrokeList outlineStrokeList;
  private Stroke defaultOutlineStroke;
  private BooleanList labelsVisibleList;
  private Boolean defaultLabelVisible;
  private ObjectList labelFontList;
  private Font defaultLabelFont;
  private PaintList labelPaintList;
  private Paint defaultLabelPaint;
  private ObjectList positionItemLabelPositionList;
  private ItemLabelPosition defaultPositiveItemLabelPosition;
  private ObjectList negativeItemLabelPositionList;
  private ItemLabelPosition defaultNegativeItemLabelPosition;
  private BooleanList createEntityList;
  private Boolean defaultCreateEntity;
  public RenderAttributes() {
    this(true);
  }
  public RenderAttributes(boolean allowNull) {
    super();
    this.paintList = new PaintList();
    Color var_2381 = Color.BLACK;
    this.defaultPaint = allowNull ? null : var_2381;
    this.strokeList = new StrokeList();
    this.defaultStroke = allowNull ? null : new BasicStroke(1.0F);
    this.fillPaintList = new PaintList();
    this.defaultFillPaint = allowNull ? null : Color.BLACK;
    this.outlinePaintList = new PaintList();
    this.defaultOutlinePaint = allowNull ? null : Color.BLACK;
    this.shapeList = new ShapeList();
  }
  public Boolean getCreateEntity(int series, int item) {
    return lookupSeriesCreateEntity(series);
  }
  public Boolean getDefaultCreateEntity() {
    return this.defaultCreateEntity;
  }
  public Boolean getDefaultLabelVisible() {
    return this.defaultLabelVisible;
  }
  public Boolean getSeriesCreateEntity(int series) {
    return this.createEntityList.getBoolean(series);
  }
  public Boolean getSeriesLabelVisible(int series) {
    return this.labelsVisibleList.getBoolean(series);
  }
  public Boolean isLabelVisible(int series, int item) {
    return lookupSeriesLabelVisible(series);
  }
  protected Boolean lookupSeriesCreateEntity(int series) {
    Boolean result = this.createEntityList.getBoolean(series);
    if(result == null) {
      result = this.defaultCreateEntity;
    }
    return result;
  }
  protected Boolean lookupSeriesLabelVisible(int series) {
    Boolean result = this.labelsVisibleList.getBoolean(series);
    if(result == null) {
      result = this.defaultLabelVisible;
    }
    return result;
  }
  public Font getDefaultLabelFont() {
    return this.defaultLabelFont;
  }
  public Font getItemLabelFont(int series, int item) {
    return lookupSeriesLabelFont(series);
  }
  public Font getSeriesLabelFont(int series) {
    return (Font)this.labelFontList.get(series);
  }
  protected Font lookupSeriesLabelFont(int series) {
    Font result = (Font)this.labelFontList.get(series);
    if(result == null) {
      result = this.defaultLabelFont;
    }
    return result;
  }
  public Paint getDefaultFillPaint() {
    return this.defaultFillPaint;
  }
  public Paint getDefaultLabelPaint() {
    return this.defaultLabelPaint;
  }
  public Paint getDefaultOutlinePaint() {
    return this.defaultOutlinePaint;
  }
  public Paint getDefaultPaint() {
    return this.defaultPaint;
  }
  public Paint getItemFillPaint(int series, int item) {
    return lookupSeriesFillPaint(series);
  }
  public Paint getItemLabelPaint(int series, int item) {
    return lookupSeriesLabelPaint(series);
  }
  public Paint getItemOutlinePaint(int series, int item) {
    return lookupSeriesOutlinePaint(series);
  }
  public Paint getItemPaint(int series, int item) {
    return lookupSeriesPaint(series);
  }
  public Paint getSeriesFillPaint(int series) {
    return this.fillPaintList.getPaint(series);
  }
  public Paint getSeriesLabelPaint(int series) {
    return this.labelPaintList.getPaint(series);
  }
  public Paint getSeriesOutlinePaint(int series) {
    return this.outlinePaintList.getPaint(series);
  }
  public Paint getSeriesPaint(int series) {
    return this.paintList.getPaint(series);
  }
  protected Paint lookupSeriesFillPaint(int series) {
    Paint result = this.fillPaintList.getPaint(series);
    if(result == null) {
      result = this.defaultFillPaint;
    }
    return result;
  }
  protected Paint lookupSeriesLabelPaint(int series) {
    Paint result = this.labelPaintList.getPaint(series);
    if(result == null) {
      result = this.defaultLabelPaint;
    }
    return result;
  }
  protected Paint lookupSeriesOutlinePaint(int series) {
    Paint result = this.outlinePaintList.getPaint(series);
    if(result == null) {
      result = this.defaultOutlinePaint;
    }
    return result;
  }
  protected Paint lookupSeriesPaint(int series) {
    Paint result = this.paintList.getPaint(series);
    if(result == null) {
      result = this.defaultPaint;
    }
    return result;
  }
  public Shape getDefaultShape() {
    return this.defaultShape;
  }
  public Shape getItemShape(int series, int item) {
    return lookupSeriesShape(series);
  }
  public Shape getSeriesShape(int series) {
    return this.shapeList.getShape(series);
  }
  protected Shape lookupSeriesShape(int series) {
    Shape result = this.shapeList.getShape(series);
    if(result == null) {
      result = this.defaultShape;
    }
    return result;
  }
  public Stroke getDefaultOutlineStroke() {
    return this.defaultOutlineStroke;
  }
  public Stroke getDefaultStroke() {
    return this.defaultStroke;
  }
  public Stroke getItemOutlineStroke(int series, int item) {
    return lookupSeriesOutlineStroke(series);
  }
  public Stroke getItemStroke(int series, int item) {
    return lookupSeriesStroke(series);
  }
  public Stroke getSeriesOutlineStroke(int series) {
    return this.outlineStrokeList.getStroke(series);
  }
  public Stroke getSeriesStroke(int series) {
    return this.strokeList.getStroke(series);
  }
  protected Stroke lookupSeriesOutlineStroke(int series) {
    Stroke result = this.outlineStrokeList.getStroke(series);
    if(result == null) {
      result = this.defaultOutlineStroke;
    }
    return result;
  }
  protected Stroke lookupSeriesStroke(int series) {
    Stroke result = this.strokeList.getStroke(series);
    if(result == null) {
      result = this.defaultStroke;
    }
    return result;
  }
  public boolean getAllowNull() {
    return this.allowNull;
  }
  public void setDefaultCreateEntity(Boolean create) {
    if(create == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'create\' argument.");
    }
    this.defaultCreateEntity = create;
  }
  public void setDefaultFillPaint(Paint paint) {
    if(paint == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.defaultFillPaint = paint;
  }
  public void setDefaultLabelFont(Font font) {
    if(font == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'font\' argument.");
    }
    this.defaultLabelFont = font;
  }
  public void setDefaultLabelPaint(Paint paint) {
    if(paint == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.defaultLabelPaint = paint;
  }
  public void setDefaultLabelVisible(Boolean visible) {
    if(visible == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'visible\' argument.");
    }
    this.defaultLabelVisible = visible;
  }
  public void setDefaultOutlinePaint(Paint paint) {
    if(paint == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.defaultOutlinePaint = paint;
  }
  public void setDefaultOutlineStroke(Stroke stroke) {
    if(stroke == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'stroke\' argument.");
    }
    this.defaultOutlineStroke = stroke;
  }
  public void setDefaultPaint(Paint paint) {
    if(paint == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.defaultPaint = paint;
  }
  public void setDefaultShape(Shape shape) {
    if(shape == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'shape\' argument.");
    }
    this.defaultShape = shape;
  }
  public void setDefaultStroke(Stroke stroke) {
    if(stroke == null && !this.allowNull) {
      throw new IllegalArgumentException("Null \'stroke\' argument.");
    }
    this.defaultStroke = stroke;
  }
  public void setSeriesCreateEntity(int series, Boolean visible) {
    this.createEntityList.setBoolean(series, visible);
  }
  public void setSeriesFillPaint(int series, Paint paint) {
    this.fillPaintList.setPaint(series, paint);
  }
  public void setSeriesLabelFont(int series, Font font) {
    this.labelFontList.set(series, font);
  }
  public void setSeriesLabelPaint(int series, Paint paint) {
    this.labelPaintList.setPaint(series, paint);
  }
  public void setSeriesLabelVisible(int series, Boolean visible) {
    this.labelsVisibleList.setBoolean(series, visible);
  }
  public void setSeriesOutlinePaint(int series, Paint paint) {
    this.outlinePaintList.setPaint(series, paint);
  }
  public void setSeriesOutlineStroke(int series, Stroke stroke) {
    this.outlineStrokeList.setStroke(series, stroke);
  }
  public void setSeriesPaint(int series, Paint paint) {
    this.paintList.setPaint(series, paint);
  }
  public void setSeriesShape(int series, Shape shape) {
    this.shapeList.setShape(series, shape);
  }
  public void setSeriesStroke(int series, Stroke stroke) {
    this.strokeList.setStroke(series, stroke);
  }
}