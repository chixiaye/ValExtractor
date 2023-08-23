package org.jfree.chart.axis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.data.time.RegularTimePeriod;

public class PeriodAxisLabelInfo implements Cloneable, Serializable  {
  final private static long serialVersionUID = 5710451740920277357L;
  final public static RectangleInsets DEFAULT_INSETS = new RectangleInsets(2, 2, 2, 2);
  final public static Font DEFAULT_FONT = new Font("Tahoma", Font.PLAIN, 10);
  final public static Paint DEFAULT_LABEL_PAINT = Color.black;
  final public static Stroke DEFAULT_DIVIDER_STROKE = new BasicStroke(0.5F);
  final public static Paint DEFAULT_DIVIDER_PAINT = Color.gray;
  private Class periodClass;
  private RectangleInsets padding;
  private DateFormat dateFormat;
  private Font labelFont;
  private transient Paint labelPaint;
  private boolean drawDividers;
  private transient Stroke dividerStroke;
  private transient Paint dividerPaint;
  public PeriodAxisLabelInfo(Class periodClass, DateFormat dateFormat) {
    this(periodClass, dateFormat, DEFAULT_INSETS, DEFAULT_FONT, DEFAULT_LABEL_PAINT, true, DEFAULT_DIVIDER_STROKE, DEFAULT_DIVIDER_PAINT);
  }
  public PeriodAxisLabelInfo(Class periodClass, DateFormat dateFormat, RectangleInsets padding, Font labelFont, Paint labelPaint, boolean drawDividers, Stroke dividerStroke, Paint dividerPaint) {
    super();
    if(periodClass == null) {
      throw new IllegalArgumentException("Null \'periodClass\' argument.");
    }
    if(dateFormat == null) {
      throw new IllegalArgumentException("Null \'dateFormat\' argument.");
    }
    if(padding == null) {
      throw new IllegalArgumentException("Null \'padding\' argument.");
    }
    if(labelFont == null) {
      throw new IllegalArgumentException("Null \'labelFont\' argument.");
    }
    if(labelPaint == null) {
      throw new IllegalArgumentException("Null \'labelPaint\' argument.");
    }
    if(dividerStroke == null) {
      throw new IllegalArgumentException("Null \'dividerStroke\' argument.");
    }
    if(dividerPaint == null) {
      throw new IllegalArgumentException("Null \'dividerPaint\' argument.");
    }
    this.periodClass = periodClass;
    this.dateFormat = dateFormat;
    this.padding = padding;
    this.labelFont = labelFont;
    this.labelPaint = labelPaint;
    this.drawDividers = drawDividers;
    this.dividerStroke = dividerStroke;
    this.dividerPaint = dividerPaint;
  }
  public Class getPeriodClass() {
    return this.periodClass;
  }
  public DateFormat getDateFormat() {
    return this.dateFormat;
  }
  public Font getLabelFont() {
    return this.labelFont;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Paint getDividerPaint() {
    return this.dividerPaint;
  }
  public Paint getLabelPaint() {
    return this.labelPaint;
  }
  public RectangleInsets getPadding() {
    return this.padding;
  }
  public RegularTimePeriod createInstance(Date millisecond, TimeZone zone, Locale locale) {
    RegularTimePeriod result = null;
    try {
      Constructor c = this.periodClass.getDeclaredConstructor(new Class[]{ Date.class, TimeZone.class, Locale.class } );
      result = (RegularTimePeriod)c.newInstance(new Object[]{ millisecond, zone, locale } );
    }
    catch (Exception e) {
    }
    return result;
  }
  public Stroke getDividerStroke() {
    return this.dividerStroke;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(obj instanceof PeriodAxisLabelInfo) {
      PeriodAxisLabelInfo info = (PeriodAxisLabelInfo)obj;
      if(!info.periodClass.equals(this.periodClass)) {
        return false;
      }
      if(!info.dateFormat.equals(this.dateFormat)) {
        return false;
      }
      if(!info.padding.equals(this.padding)) {
        return false;
      }
      if(!info.labelFont.equals(this.labelFont)) {
        return false;
      }
      if(!info.labelPaint.equals(this.labelPaint)) {
        return false;
      }
      if(info.drawDividers != this.drawDividers) {
        return false;
      }
      if(!info.dividerStroke.equals(this.dividerStroke)) {
        return false;
      }
      if(!info.dividerPaint.equals(this.dividerPaint)) {
        return false;
      }
      return true;
    }
    return false;
  }
  public boolean getDrawDividers() {
    return this.drawDividers;
  }
  public int hashCode() {
    int result = 41;
    result = 37 * this.periodClass.hashCode();
    result = 37 * this.dateFormat.hashCode();
    return result;
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    Paint var_336 = SerialUtilities.readPaint(stream);
    this.labelPaint = var_336;
    this.dividerStroke = SerialUtilities.readStroke(stream);
    this.dividerPaint = SerialUtilities.readPaint(stream);
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writePaint(this.labelPaint, stream);
    SerialUtilities.writeStroke(this.dividerStroke, stream);
    SerialUtilities.writePaint(this.dividerPaint, stream);
  }
}