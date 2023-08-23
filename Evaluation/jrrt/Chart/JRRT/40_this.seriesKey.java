package org.jfree.chart;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.AttributedString;
import java.text.CharacterIterator;
import org.jfree.chart.util.AttributedStringUtilities;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.chart.util.StandardGradientPaintTransformer;
import org.jfree.data.general.Dataset;

public class LegendItem implements Cloneable, Serializable  {
  final private static long serialVersionUID = -797214582948827144L;
  private Dataset dataset;
  private Comparable seriesKey;
  private int datasetIndex;
  private int series;
  private String label;
  private Font labelFont;
  private transient Paint labelPaint;
  private transient AttributedString attributedLabel;
  private String description;
  private String toolTipText;
  private String urlText;
  private boolean shapeVisible;
  private transient Shape shape;
  private boolean shapeFilled;
  private transient Paint fillPaint;
  private GradientPaintTransformer fillPaintTransformer;
  private boolean shapeOutlineVisible;
  private transient Paint outlinePaint;
  private transient Stroke outlineStroke;
  private boolean lineVisible;
  private transient Shape line;
  private transient Stroke lineStroke;
  private transient Paint linePaint;
  final private static Shape UNUSED_SHAPE = new Line2D.Float();
  final private static Stroke UNUSED_STROKE = new BasicStroke(0.0F);
  public LegendItem(AttributedString label, String description, String toolTipText, String urlText, Shape line, Stroke lineStroke, Paint linePaint) {
    this(label, description, toolTipText, urlText, false, UNUSED_SHAPE, false, Color.black, false, Color.black, UNUSED_STROKE, true, line, lineStroke, linePaint);
  }
  public LegendItem(AttributedString label, String description, String toolTipText, String urlText, Shape shape, Paint fillPaint) {
    this(label, description, toolTipText, urlText, true, shape, true, fillPaint, false, Color.black, UNUSED_STROKE, false, UNUSED_SHAPE, UNUSED_STROKE, Color.black);
  }
  public LegendItem(AttributedString label, String description, String toolTipText, String urlText, Shape shape, Paint fillPaint, Stroke outlineStroke, Paint outlinePaint) {
    this(label, description, toolTipText, urlText, true, shape, true, fillPaint, true, outlinePaint, outlineStroke, false, UNUSED_SHAPE, UNUSED_STROKE, Color.black);
  }
  public LegendItem(AttributedString label, String description, String toolTipText, String urlText, boolean shapeVisible, Shape shape, boolean shapeFilled, Paint fillPaint, boolean shapeOutlineVisible, Paint outlinePaint, Stroke outlineStroke, boolean lineVisible, Shape line, Stroke lineStroke, Paint linePaint) {
    super();
    if(label == null) {
      throw new IllegalArgumentException("Null \'label\' argument.");
    }
    if(fillPaint == null) {
      throw new IllegalArgumentException("Null \'fillPaint\' argument.");
    }
    if(lineStroke == null) {
      throw new IllegalArgumentException("Null \'lineStroke\' argument.");
    }
    if(line == null) {
      throw new IllegalArgumentException("Null \'line\' argument.");
    }
    if(linePaint == null) {
      throw new IllegalArgumentException("Null \'linePaint\' argument.");
    }
    if(outlinePaint == null) {
      throw new IllegalArgumentException("Null \'outlinePaint\' argument.");
    }
    if(outlineStroke == null) {
      throw new IllegalArgumentException("Null \'outlineStroke\' argument.");
    }
    this.label = characterIteratorToString(label.getIterator());
    this.attributedLabel = label;
    this.description = description;
    this.shapeVisible = shapeVisible;
    this.shape = shape;
    this.shapeFilled = shapeFilled;
    this.fillPaint = fillPaint;
    this.fillPaintTransformer = new StandardGradientPaintTransformer();
    this.shapeOutlineVisible = shapeOutlineVisible;
    this.outlinePaint = outlinePaint;
    this.outlineStroke = outlineStroke;
    this.lineVisible = lineVisible;
    this.line = line;
    this.lineStroke = lineStroke;
    this.linePaint = linePaint;
    this.toolTipText = toolTipText;
    this.urlText = urlText;
  }
  public LegendItem(String label) {
    this(label, Color.black);
  }
  public LegendItem(String label, Paint paint) {
    this(label, null, null, null, new Rectangle2D.Double(-4.0D, -4.0D, 8.0D, 8.0D), paint);
  }
  public LegendItem(String label, String description, String toolTipText, String urlText, Shape line, Stroke lineStroke, Paint linePaint) {
    this(label, description, toolTipText, urlText, false, UNUSED_SHAPE, false, Color.black, false, Color.black, UNUSED_STROKE, true, line, lineStroke, linePaint);
  }
  public LegendItem(String label, String description, String toolTipText, String urlText, Shape shape, Paint fillPaint) {
    this(label, description, toolTipText, urlText, true, shape, true, fillPaint, false, Color.black, UNUSED_STROKE, false, UNUSED_SHAPE, UNUSED_STROKE, Color.black);
  }
  public LegendItem(String label, String description, String toolTipText, String urlText, Shape shape, Paint fillPaint, Stroke outlineStroke, Paint outlinePaint) {
    this(label, description, toolTipText, urlText, true, shape, true, fillPaint, true, outlinePaint, outlineStroke, false, UNUSED_SHAPE, UNUSED_STROKE, Color.black);
  }
  public LegendItem(String label, String description, String toolTipText, String urlText, boolean shapeVisible, Shape shape, boolean shapeFilled, Paint fillPaint, boolean shapeOutlineVisible, Paint outlinePaint, Stroke outlineStroke, boolean lineVisible, Shape line, Stroke lineStroke, Paint linePaint) {
    super();
    if(label == null) {
      throw new IllegalArgumentException("Null \'label\' argument.");
    }
    if(fillPaint == null) {
      throw new IllegalArgumentException("Null \'fillPaint\' argument.");
    }
    if(lineStroke == null) {
      throw new IllegalArgumentException("Null \'lineStroke\' argument.");
    }
    if(outlinePaint == null) {
      throw new IllegalArgumentException("Null \'outlinePaint\' argument.");
    }
    if(outlineStroke == null) {
      throw new IllegalArgumentException("Null \'outlineStroke\' argument.");
    }
    this.label = label;
    this.labelPaint = null;
    this.attributedLabel = null;
    this.description = description;
    this.shapeVisible = shapeVisible;
    this.shape = shape;
    this.shapeFilled = shapeFilled;
    this.fillPaint = fillPaint;
    this.fillPaintTransformer = new StandardGradientPaintTransformer();
    this.shapeOutlineVisible = shapeOutlineVisible;
    this.outlinePaint = outlinePaint;
    this.outlineStroke = outlineStroke;
    this.lineVisible = lineVisible;
    this.line = line;
    this.lineStroke = lineStroke;
    this.linePaint = linePaint;
    this.toolTipText = toolTipText;
    this.urlText = urlText;
  }
  public AttributedString getAttributedLabel() {
    return this.attributedLabel;
  }
  public Comparable getSeriesKey() {
    return this.seriesKey;
  }
  public Dataset getDataset() {
    return this.dataset;
  }
  public Font getLabelFont() {
    return this.labelFont;
  }
  public GradientPaintTransformer getFillPaintTransformer() {
    return this.fillPaintTransformer;
  }
  public Object clone() throws CloneNotSupportedException {
    LegendItem clone = (LegendItem)super.clone();
    Comparable var_40 = this.seriesKey;
    if(var_40 instanceof PublicCloneable) {
      PublicCloneable pc = (PublicCloneable)this.seriesKey;
      clone.seriesKey = (Comparable)pc.clone();
    }
    clone.shape = ShapeUtilities.clone(this.shape);
    if(this.fillPaintTransformer instanceof PublicCloneable) {
      PublicCloneable pc = (PublicCloneable)this.fillPaintTransformer;
      clone.fillPaintTransformer = (GradientPaintTransformer)pc.clone();
    }
    clone.line = ShapeUtilities.clone(this.line);
    return clone;
  }
  public Paint getFillPaint() {
    return this.fillPaint;
  }
  public Paint getLabelPaint() {
    return this.labelPaint;
  }
  public Paint getLinePaint() {
    return this.linePaint;
  }
  public Paint getOutlinePaint() {
    return this.outlinePaint;
  }
  public Shape getLine() {
    return this.line;
  }
  public Shape getShape() {
    return this.shape;
  }
  private String characterIteratorToString(CharacterIterator iterator) {
    int endIndex = iterator.getEndIndex();
    int beginIndex = iterator.getBeginIndex();
    int count = endIndex - beginIndex;
    if(count <= 0) {
      return "";
    }
    char[] chars = new char[count];
    int i = 0;
    char c = iterator.first();
    while(c != CharacterIterator.DONE){
      chars[i] = c;
      i++;
      c = iterator.next();
    }
    return new String(chars);
  }
  public String getDescription() {
    return this.description;
  }
  public String getLabel() {
    return this.label;
  }
  public String getToolTipText() {
    return this.toolTipText;
  }
  public String getURLText() {
    return this.urlText;
  }
  public Stroke getLineStroke() {
    return this.lineStroke;
  }
  public Stroke getOutlineStroke() {
    return this.outlineStroke;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof LegendItem)) {
      return false;
    }
    LegendItem that = (LegendItem)obj;
    if(this.datasetIndex != that.datasetIndex) {
      return false;
    }
    if(this.series != that.series) {
      return false;
    }
    if(!this.label.equals(that.label)) {
      return false;
    }
    if(!AttributedStringUtilities.equal(this.attributedLabel, that.attributedLabel)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.description, that.description)) {
      return false;
    }
    if(this.shapeVisible != that.shapeVisible) {
      return false;
    }
    if(!ShapeUtilities.equal(this.shape, that.shape)) {
      return false;
    }
    if(this.shapeFilled != that.shapeFilled) {
      return false;
    }
    if(!PaintUtilities.equal(this.fillPaint, that.fillPaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.fillPaintTransformer, that.fillPaintTransformer)) {
      return false;
    }
    if(this.shapeOutlineVisible != that.shapeOutlineVisible) {
      return false;
    }
    if(!this.outlineStroke.equals(that.outlineStroke)) {
      return false;
    }
    if(!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
      return false;
    }
    if(!this.lineVisible == that.lineVisible) {
      return false;
    }
    if(!ShapeUtilities.equal(this.line, that.line)) {
      return false;
    }
    if(!this.lineStroke.equals(that.lineStroke)) {
      return false;
    }
    if(!PaintUtilities.equal(this.linePaint, that.linePaint)) {
      return false;
    }
    if(!ObjectUtilities.equal(this.labelFont, that.labelFont)) {
      return false;
    }
    if(!PaintUtilities.equal(this.labelPaint, that.labelPaint)) {
      return false;
    }
    return true;
  }
  public boolean isLineVisible() {
    return this.lineVisible;
  }
  public boolean isShapeFilled() {
    return this.shapeFilled;
  }
  public boolean isShapeOutlineVisible() {
    return this.shapeOutlineVisible;
  }
  public boolean isShapeVisible() {
    return this.shapeVisible;
  }
  public int getDatasetIndex() {
    return this.datasetIndex;
  }
  public int getSeriesIndex() {
    return this.series;
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.attributedLabel = SerialUtilities.readAttributedString(stream);
    this.shape = SerialUtilities.readShape(stream);
    this.fillPaint = SerialUtilities.readPaint(stream);
    this.outlineStroke = SerialUtilities.readStroke(stream);
    this.outlinePaint = SerialUtilities.readPaint(stream);
    this.line = SerialUtilities.readShape(stream);
    this.lineStroke = SerialUtilities.readStroke(stream);
    this.linePaint = SerialUtilities.readPaint(stream);
    this.labelPaint = SerialUtilities.readPaint(stream);
  }
  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }
  public void setDatasetIndex(int index) {
    this.datasetIndex = index;
  }
  public void setDescription(String text) {
    this.description = text;
  }
  public void setFillPaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.fillPaint = paint;
  }
  public void setFillPaintTransformer(GradientPaintTransformer transformer) {
    if(transformer == null) {
      throw new IllegalArgumentException("Null \'transformer\' attribute.");
    }
    this.fillPaintTransformer = transformer;
  }
  public void setLabelFont(Font font) {
    this.labelFont = font;
  }
  public void setLabelPaint(Paint paint) {
    this.labelPaint = paint;
  }
  public void setLine(Shape line) {
    if(line == null) {
      throw new IllegalArgumentException("Null \'line\' argument.");
    }
    this.line = line;
  }
  public void setLinePaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.linePaint = paint;
  }
  public void setLineVisible(boolean visible) {
    this.lineVisible = visible;
  }
  public void setOutlinePaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.outlinePaint = paint;
  }
  public void setOutlineStroke(Stroke stroke) {
    this.outlineStroke = stroke;
  }
  public void setSeriesIndex(int index) {
    this.series = index;
  }
  public void setSeriesKey(Comparable key) {
    this.seriesKey = key;
  }
  public void setShape(Shape shape) {
    if(shape == null) {
      throw new IllegalArgumentException("Null \'shape\' argument.");
    }
    this.shape = shape;
  }
  public void setShapeVisible(boolean visible) {
    this.shapeVisible = visible;
  }
  public void setToolTipText(String text) {
    this.toolTipText = text;
  }
  public void setURLText(String text) {
    this.urlText = text;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writeAttributedString(this.attributedLabel, stream);
    SerialUtilities.writeShape(this.shape, stream);
    SerialUtilities.writePaint(this.fillPaint, stream);
    SerialUtilities.writeStroke(this.outlineStroke, stream);
    SerialUtilities.writePaint(this.outlinePaint, stream);
    SerialUtilities.writeShape(this.line, stream);
    SerialUtilities.writeStroke(this.lineStroke, stream);
    SerialUtilities.writePaint(this.linePaint, stream);
    SerialUtilities.writePaint(this.labelPaint, stream);
  }
}