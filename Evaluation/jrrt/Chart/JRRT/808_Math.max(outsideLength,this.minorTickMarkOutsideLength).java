package org.jfree.chart.axis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.text.TextUtilities;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Year;

public class PeriodAxis extends ValueAxis implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 8353295532075872069L;
  private RegularTimePeriod first;
  private RegularTimePeriod last;
  private TimeZone timeZone;
  private Locale locale;
  private Calendar calendar;
  private Class autoRangeTimePeriodClass;
  private Class majorTickTimePeriodClass;
  private boolean minorTickMarksVisible;
  private Class minorTickTimePeriodClass;
  private float minorTickMarkInsideLength = 0.0F;
  private float minorTickMarkOutsideLength = 2.0F;
  private transient Stroke minorTickMarkStroke = new BasicStroke(0.5F);
  private transient Paint minorTickMarkPaint = Color.black;
  private PeriodAxisLabelInfo[] labelInfo;
  public PeriodAxis(String label) {
    this(label, new Day(), new Day());
  }
  public PeriodAxis(String label, RegularTimePeriod first, RegularTimePeriod last) {
    this(label, first, last, TimeZone.getDefault(), Locale.getDefault());
  }
  public PeriodAxis(String label, RegularTimePeriod first, RegularTimePeriod last, TimeZone timeZone, Locale locale) {
    super(label, null);
    if(timeZone == null) {
      throw new IllegalArgumentException("Null \'timeZone\' argument.");
    }
    if(locale == null) {
      throw new IllegalArgumentException("Null \'locale\' argument.");
    }
    this.first = first;
    this.last = last;
    this.timeZone = timeZone;
    this.locale = locale;
    this.calendar = Calendar.getInstance(timeZone, locale);
    this.first.peg(this.calendar);
    this.last.peg(this.calendar);
    this.autoRangeTimePeriodClass = first.getClass();
    this.majorTickTimePeriodClass = first.getClass();
    this.minorTickMarksVisible = false;
    this.minorTickTimePeriodClass = RegularTimePeriod.downsize(this.majorTickTimePeriodClass);
    setAutoRange(true);
    this.labelInfo = new PeriodAxisLabelInfo[2];
    this.labelInfo[0] = new PeriodAxisLabelInfo(Month.class, new SimpleDateFormat("MMM", locale));
    this.labelInfo[1] = new PeriodAxisLabelInfo(Year.class, new SimpleDateFormat("yyyy", locale));
  }
  public AxisSpace reserveSpace(Graphics2D g2, Plot plot, Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {
    if(space == null) {
      space = new AxisSpace();
    }
    if(!isVisible()) {
      return space;
    }
    double dimension = getFixedDimension();
    if(dimension > 0.0D) {
      space.ensureAtLeast(dimension, edge);
    }
    Rectangle2D labelEnclosure = getLabelEnclosure(g2, edge);
    double labelHeight = 0.0D;
    double labelWidth = 0.0D;
    double tickLabelBandsDimension = 0.0D;
    for(int i = 0; i < this.labelInfo.length; i++) {
      PeriodAxisLabelInfo info = this.labelInfo[i];
      FontMetrics fm = g2.getFontMetrics(info.getLabelFont());
      tickLabelBandsDimension += info.getPadding().extendHeight(fm.getHeight());
    }
    if(RectangleEdge.isTopOrBottom(edge)) {
      labelHeight = labelEnclosure.getHeight();
      space.add(labelHeight + tickLabelBandsDimension, edge);
    }
    else 
      if(RectangleEdge.isLeftOrRight(edge)) {
        labelWidth = labelEnclosure.getWidth();
        space.add(labelWidth + tickLabelBandsDimension, edge);
      }
    double tickMarkSpace = 0.0D;
    if(isTickMarksVisible()) {
      tickMarkSpace = getTickMarkOutsideLength();
    }
    if(this.minorTickMarksVisible) {
      tickMarkSpace = Math.max(tickMarkSpace, this.minorTickMarkOutsideLength);
    }
    space.add(tickMarkSpace, edge);
    return space;
  }
  public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
    AxisState axisState = new AxisState(cursor);
    if(isAxisLineVisible()) {
      drawAxisLine(g2, cursor, dataArea, edge);
    }
    if(isTickMarksVisible()) {
      drawTickMarks(g2, axisState, dataArea, edge);
    }
    if(isTickLabelsVisible()) {
      for(int band = 0; band < this.labelInfo.length; band++) {
        axisState = drawTickLabels(band, g2, axisState, dataArea, edge);
      }
    }
    axisState = drawLabel(getLabel(), g2, plotArea, dataArea, edge, axisState, plotState);
    return axisState;
  }
  protected AxisState drawTickLabels(int band, Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
    double delta1 = 0.0D;
    FontMetrics fm = g2.getFontMetrics(this.labelInfo[band].getLabelFont());
    if(edge == RectangleEdge.BOTTOM) {
      delta1 = this.labelInfo[band].getPadding().calculateTopOutset(fm.getHeight());
    }
    else 
      if(edge == RectangleEdge.TOP) {
        delta1 = this.labelInfo[band].getPadding().calculateBottomOutset(fm.getHeight());
      }
    state.moveCursor(delta1, edge);
    long axisMin = this.first.getFirstMillisecond();
    long axisMax = this.last.getLastMillisecond();
    g2.setFont(this.labelInfo[band].getLabelFont());
    g2.setPaint(this.labelInfo[band].getLabelPaint());
    RegularTimePeriod p1 = this.labelInfo[band].createInstance(new Date(axisMin), this.timeZone, this.locale);
    RegularTimePeriod p2 = this.labelInfo[band].createInstance(new Date(axisMax), this.timeZone, this.locale);
    String label1 = this.labelInfo[band].getDateFormat().format(new Date(p1.getMiddleMillisecond()));
    String label2 = this.labelInfo[band].getDateFormat().format(new Date(p2.getMiddleMillisecond()));
    Rectangle2D b1 = TextUtilities.getTextBounds(label1, g2, g2.getFontMetrics());
    Rectangle2D b2 = TextUtilities.getTextBounds(label2, g2, g2.getFontMetrics());
    double w = Math.max(b1.getWidth(), b2.getWidth());
    long ww = Math.round(java2DToValue(dataArea.getX() + w + 5.0D, dataArea, edge));
    if(isInverted()) {
      ww = axisMax - ww;
    }
    else {
      ww = ww - axisMin;
    }
    long length = p1.getLastMillisecond() - p1.getFirstMillisecond();
    int periods = (int)(ww / length) + 1;
    RegularTimePeriod p = this.labelInfo[band].createInstance(new Date(axisMin), this.timeZone, this.locale);
    Rectangle2D b = null;
    long lastXX = 0L;
    float y = (float)(state.getCursor());
    TextAnchor anchor = TextAnchor.TOP_CENTER;
    float yDelta = (float)b1.getHeight();
    if(edge == RectangleEdge.TOP) {
      anchor = TextAnchor.BOTTOM_CENTER;
      yDelta = -yDelta;
    }
    while(p.getFirstMillisecond() <= axisMax){
      float x = (float)valueToJava2D(p.getMiddleMillisecond(), dataArea, edge);
      DateFormat df = this.labelInfo[band].getDateFormat();
      String label = df.format(new Date(p.getMiddleMillisecond()));
      long first = p.getFirstMillisecond();
      long last = p.getLastMillisecond();
      if(last > axisMax) {
        Rectangle2D bb = TextUtilities.getTextBounds(label, g2, g2.getFontMetrics());
        if((x + bb.getWidth() / 2) > dataArea.getMaxX()) {
          float xstart = (float)valueToJava2D(Math.max(first, axisMin), dataArea, edge);
          if(bb.getWidth() < (dataArea.getMaxX() - xstart)) {
            x = ((float)dataArea.getMaxX() + xstart) / 2.0F;
          }
          else {
            label = null;
          }
        }
      }
      if(first < axisMin) {
        Rectangle2D bb = TextUtilities.getTextBounds(label, g2, g2.getFontMetrics());
        if((x - bb.getWidth() / 2) < dataArea.getX()) {
          float xlast = (float)valueToJava2D(Math.min(last, axisMax), dataArea, edge);
          if(bb.getWidth() < (xlast - dataArea.getX())) {
            x = (xlast + (float)dataArea.getX()) / 2.0F;
          }
          else {
            label = null;
          }
        }
      }
      if(label != null) {
        g2.setPaint(this.labelInfo[band].getLabelPaint());
        b = TextUtilities.drawAlignedString(label, g2, x, y, anchor);
      }
      if(lastXX > 0L) {
        if(this.labelInfo[band].getDrawDividers()) {
          long nextXX = p.getFirstMillisecond();
          long mid = (lastXX + nextXX) / 2;
          float mid2d = (float)valueToJava2D(mid, dataArea, edge);
          g2.setStroke(this.labelInfo[band].getDividerStroke());
          g2.setPaint(this.labelInfo[band].getDividerPaint());
          g2.draw(new Line2D.Float(mid2d, y, mid2d, y + yDelta));
        }
      }
      lastXX = last;
      for(int i = 0; i < periods; i++) {
        p = p.next();
      }
      p.peg(this.calendar);
    }
    double used = 0.0D;
    if(b != null) {
      used = b.getHeight();
      if(edge == RectangleEdge.BOTTOM) {
        used += this.labelInfo[band].getPadding().calculateBottomOutset(fm.getHeight());
      }
      else 
        if(edge == RectangleEdge.TOP) {
          used += this.labelInfo[band].getPadding().calculateTopOutset(fm.getHeight());
        }
    }
    state.moveCursor(used, edge);
    return state;
  }
  public Class getAutoRangeTimePeriodClass() {
    return this.autoRangeTimePeriodClass;
  }
  public Class getMajorTickTimePeriodClass() {
    return this.majorTickTimePeriodClass;
  }
  public Class getMinorTickTimePeriodClass() {
    return this.minorTickTimePeriodClass;
  }
  public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
    return Collections.EMPTY_LIST;
  }
  public Locale getLocale() {
    return this.locale;
  }
  public Object clone() throws CloneNotSupportedException {
    PeriodAxis clone = (PeriodAxis)super.clone();
    clone.timeZone = (TimeZone)this.timeZone.clone();
    clone.labelInfo = new PeriodAxisLabelInfo[this.labelInfo.length];
    for(int i = 0; i < this.labelInfo.length; i++) {
      clone.labelInfo[i] = this.labelInfo[i];
    }
    return clone;
  }
  public Paint getMinorTickMarkPaint() {
    return this.minorTickMarkPaint;
  }
  public PeriodAxisLabelInfo[] getLabelInfo() {
    return this.labelInfo;
  }
  public Range getRange() {
    return new Range(this.first.getFirstMillisecond(this.calendar), this.last.getLastMillisecond(this.calendar));
  }
  private RegularTimePeriod createInstance(Class periodClass, Date millisecond, TimeZone zone, Locale locale) {
    RegularTimePeriod result = null;
    try {
      Constructor c = periodClass.getDeclaredConstructor(new Class[]{ Date.class, TimeZone.class, Locale.class } );
      result = (RegularTimePeriod)c.newInstance(new Object[]{ millisecond, zone, locale } );
    }
    catch (Exception e) {
      try {
        Constructor c = periodClass.getDeclaredConstructor(new Class[]{ Date.class } );
        result = (RegularTimePeriod)c.newInstance(new Object[]{ millisecond } );
      }
      catch (Exception e2) {
      }
    }
    return result;
  }
  public RegularTimePeriod getFirst() {
    return this.first;
  }
  public RegularTimePeriod getLast() {
    return this.last;
  }
  public Stroke getMinorTickMarkStroke() {
    return this.minorTickMarkStroke;
  }
  public TimeZone getTimeZone() {
    return this.timeZone;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof PeriodAxis)) {
      return false;
    }
    PeriodAxis that = (PeriodAxis)obj;
    if(!this.first.equals(that.first)) {
      return false;
    }
    if(!this.last.equals(that.last)) {
      return false;
    }
    if(!this.timeZone.equals(that.timeZone)) {
      return false;
    }
    if(!this.locale.equals(that.locale)) {
      return false;
    }
    if(!this.autoRangeTimePeriodClass.equals(that.autoRangeTimePeriodClass)) {
      return false;
    }
    if(!(isMinorTickMarksVisible() == that.isMinorTickMarksVisible())) {
      return false;
    }
    if(!this.majorTickTimePeriodClass.equals(that.majorTickTimePeriodClass)) {
      return false;
    }
    if(!this.minorTickTimePeriodClass.equals(that.minorTickTimePeriodClass)) {
      return false;
    }
    if(!this.minorTickMarkPaint.equals(that.minorTickMarkPaint)) {
      return false;
    }
    if(!this.minorTickMarkStroke.equals(that.minorTickMarkStroke)) {
      return false;
    }
    if(!Arrays.equals(this.labelInfo, that.labelInfo)) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean isMinorTickMarksVisible() {
    return this.minorTickMarksVisible;
  }
  public double java2DToValue(double java2DValue, Rectangle2D area, RectangleEdge edge) {
    double result = Double.NaN;
    double min = 0.0D;
    double max = 0.0D;
    double axisMin = this.first.getFirstMillisecond();
    double axisMax = this.last.getLastMillisecond();
    if(RectangleEdge.isTopOrBottom(edge)) {
      min = area.getX();
      max = area.getMaxX();
    }
    else 
      if(RectangleEdge.isLeftOrRight(edge)) {
        min = area.getMaxY();
        max = area.getY();
      }
    if(isInverted()) {
      result = axisMax - ((java2DValue - min) / (max - min) * (axisMax - axisMin));
    }
    else {
      result = axisMin + ((java2DValue - min) / (max - min) * (axisMax - axisMin));
    }
    return result;
  }
  public double valueToJava2D(double value, Rectangle2D area, RectangleEdge edge) {
    double result = Double.NaN;
    double axisMin = this.first.getFirstMillisecond();
    double axisMax = this.last.getLastMillisecond();
    if(RectangleEdge.isTopOrBottom(edge)) {
      double minX = area.getX();
      double maxX = area.getMaxX();
      if(isInverted()) {
        result = maxX + ((value - axisMin) / (axisMax - axisMin)) * (minX - maxX);
      }
      else {
        result = minX + ((value - axisMin) / (axisMax - axisMin)) * (maxX - minX);
      }
    }
    else 
      if(RectangleEdge.isLeftOrRight(edge)) {
        double minY = area.getMinY();
        double maxY = area.getMaxY();
        if(isInverted()) {
          result = minY + (((value - axisMin) / (axisMax - axisMin)) * (maxY - minY));
        }
        else {
          result = maxY - (((value - axisMin) / (axisMax - axisMin)) * (maxY - minY));
        }
      }
    return result;
  }
  public float getMinorTickMarkInsideLength() {
    return this.minorTickMarkInsideLength;
  }
  public float getMinorTickMarkOutsideLength() {
    return this.minorTickMarkOutsideLength;
  }
  public int hashCode() {
    if(getLabel() != null) {
      return getLabel().hashCode();
    }
    else {
      return 0;
    }
  }
  protected void autoAdjustRange() {
    Plot plot = getPlot();
    if(plot == null) {
      return ;
    }
    if(plot instanceof ValueAxisPlot) {
      ValueAxisPlot vap = (ValueAxisPlot)plot;
      Range r = vap.getDataRange(this);
      if(r == null) {
        r = getDefaultAutoRange();
      }
      long upper = Math.round(r.getUpperBound());
      long lower = Math.round(r.getLowerBound());
      this.first = createInstance(this.autoRangeTimePeriodClass, new Date(lower), this.timeZone, this.locale);
      this.last = createInstance(this.autoRangeTimePeriodClass, new Date(upper), this.timeZone, this.locale);
      setRange(r, false, false);
    }
  }
  public void configure() {
    if(this.isAutoRange()) {
      autoAdjustRange();
    }
  }
  protected void drawTickMarks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
    if(RectangleEdge.isTopOrBottom(edge)) {
      drawTickMarksHorizontal(g2, state, dataArea, edge);
    }
    else 
      if(RectangleEdge.isLeftOrRight(edge)) {
        drawTickMarksVertical(g2, state, dataArea, edge);
      }
  }
  protected void drawTickMarksHorizontal(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
    List ticks = new ArrayList();
    double x0 = dataArea.getX();
    double y0 = state.getCursor();
    double insideLength = getTickMarkInsideLength();
    double outsideLength = getTickMarkOutsideLength();
    RegularTimePeriod t = createInstance(this.majorTickTimePeriodClass, this.first.getStart(), getTimeZone(), this.locale);
    long t0 = t.getFirstMillisecond();
    Line2D inside = null;
    Line2D outside = null;
    long firstOnAxis = getFirst().getFirstMillisecond();
    long lastOnAxis = getLast().getLastMillisecond() + 1;
    while(t0 <= lastOnAxis){
      ticks.add(new NumberTick(new Double(t0), "", TextAnchor.CENTER, TextAnchor.CENTER, 0.0D));
      x0 = valueToJava2D(t0, dataArea, edge);
      if(edge == RectangleEdge.TOP) {
        inside = new Line2D.Double(x0, y0, x0, y0 + insideLength);
        outside = new Line2D.Double(x0, y0, x0, y0 - outsideLength);
      }
      else 
        if(edge == RectangleEdge.BOTTOM) {
          inside = new Line2D.Double(x0, y0, x0, y0 - insideLength);
          outside = new Line2D.Double(x0, y0, x0, y0 + outsideLength);
        }
      if(t0 >= firstOnAxis) {
        g2.setPaint(getTickMarkPaint());
        g2.setStroke(getTickMarkStroke());
        g2.draw(inside);
        g2.draw(outside);
      }
      if(this.minorTickMarksVisible) {
        RegularTimePeriod tminor = createInstance(this.minorTickTimePeriodClass, new Date(t0), getTimeZone(), this.locale);
        long tt0 = tminor.getFirstMillisecond();
        while(tt0 < t.getLastMillisecond() && tt0 < lastOnAxis){
          double xx0 = valueToJava2D(tt0, dataArea, edge);
          if(edge == RectangleEdge.TOP) {
            inside = new Line2D.Double(xx0, y0, xx0, y0 + this.minorTickMarkInsideLength);
            outside = new Line2D.Double(xx0, y0, xx0, y0 - this.minorTickMarkOutsideLength);
          }
          else 
            if(edge == RectangleEdge.BOTTOM) {
              inside = new Line2D.Double(xx0, y0, xx0, y0 - this.minorTickMarkInsideLength);
              outside = new Line2D.Double(xx0, y0, xx0, y0 + this.minorTickMarkOutsideLength);
            }
          if(tt0 >= firstOnAxis) {
            g2.setPaint(this.minorTickMarkPaint);
            g2.setStroke(this.minorTickMarkStroke);
            g2.draw(inside);
            g2.draw(outside);
          }
          tminor = tminor.next();
          tminor.peg(this.calendar);
          tt0 = tminor.getFirstMillisecond();
        }
      }
      t = t.next();
      t.peg(this.calendar);
      t0 = t.getFirstMillisecond();
    }
    if(edge == RectangleEdge.TOP) {
      double var_808 = Math.max(outsideLength, this.minorTickMarkOutsideLength);
      state.cursorUp(var_808);
    }
    else 
      if(edge == RectangleEdge.BOTTOM) {
        state.cursorDown(Math.max(outsideLength, this.minorTickMarkOutsideLength));
      }
    state.setTicks(ticks);
  }
  protected void drawTickMarksVertical(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.minorTickMarkStroke = SerialUtilities.readStroke(stream);
    this.minorTickMarkPaint = SerialUtilities.readPaint(stream);
  }
  public void setAutoRangeTimePeriodClass(Class c) {
    if(c == null) {
      throw new IllegalArgumentException("Null \'c\' argument.");
    }
    this.autoRangeTimePeriodClass = c;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setFirst(RegularTimePeriod first) {
    if(first == null) {
      throw new IllegalArgumentException("Null \'first\' argument.");
    }
    this.first = first;
    this.first.peg(this.calendar);
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setLabelInfo(PeriodAxisLabelInfo[] info) {
    this.labelInfo = info;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setLast(RegularTimePeriod last) {
    if(last == null) {
      throw new IllegalArgumentException("Null \'last\' argument.");
    }
    this.last = last;
    this.last.peg(this.calendar);
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setMajorTickTimePeriodClass(Class c) {
    if(c == null) {
      throw new IllegalArgumentException("Null \'c\' argument.");
    }
    this.majorTickTimePeriodClass = c;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setMinorTickMarkInsideLength(float length) {
    this.minorTickMarkInsideLength = length;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setMinorTickMarkOutsideLength(float length) {
    this.minorTickMarkOutsideLength = length;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setMinorTickMarkPaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.minorTickMarkPaint = paint;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setMinorTickMarkStroke(Stroke stroke) {
    if(stroke == null) {
      throw new IllegalArgumentException("Null \'stroke\' argument.");
    }
    this.minorTickMarkStroke = stroke;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setMinorTickMarksVisible(boolean visible) {
    this.minorTickMarksVisible = visible;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setMinorTickTimePeriodClass(Class c) {
    if(c == null) {
      throw new IllegalArgumentException("Null \'c\' argument.");
    }
    this.minorTickTimePeriodClass = c;
    notifyListeners(new AxisChangeEvent(this));
  }
  public void setRange(Range range, boolean turnOffAutoRange, boolean notify) {
    long upper = Math.round(range.getUpperBound());
    long lower = Math.round(range.getLowerBound());
    this.first = createInstance(this.autoRangeTimePeriodClass, new Date(lower), this.timeZone, this.locale);
    this.last = createInstance(this.autoRangeTimePeriodClass, new Date(upper), this.timeZone, this.locale);
    super.setRange(new Range(this.first.getFirstMillisecond(), this.last.getLastMillisecond() + 1.0D), turnOffAutoRange, notify);
  }
  public void setTimeZone(TimeZone zone) {
    if(zone == null) {
      throw new IllegalArgumentException("Null \'zone\' argument.");
    }
    this.timeZone = zone;
    this.calendar = Calendar.getInstance(zone, this.locale);
    this.first.peg(this.calendar);
    this.last.peg(this.calendar);
    notifyListeners(new AxisChangeEvent(this));
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writeStroke(this.minorTickMarkStroke, stream);
    SerialUtilities.writePaint(this.minorTickMarkPaint, stream);
  }
}