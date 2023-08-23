package org.jfree.chart.plot;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.chart.util.PaintUtilities;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.Rotation;
import org.jfree.chart.util.SerialUtilities;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.chart.util.UnitType;
import org.jfree.data.pie.PieDataset;

public class RingPlot extends PiePlot implements Cloneable, Serializable  {
  final private static long serialVersionUID = 1556064784129676620L;
  private boolean separatorsVisible;
  private transient Stroke separatorStroke;
  private transient Paint separatorPaint;
  private double innerSeparatorExtension;
  private double outerSeparatorExtension;
  private double sectionDepth;
  public RingPlot() {
    this(null);
  }
  public RingPlot(PieDataset dataset) {
    super(dataset);
    this.separatorsVisible = true;
    this.separatorStroke = new BasicStroke(0.5F);
    this.separatorPaint = Color.gray;
    this.innerSeparatorExtension = 0.20D;
    this.outerSeparatorExtension = 0.20D;
    this.sectionDepth = 0.20D;
  }
  private Line2D extendLine(Line2D line, double startPercent, double endPercent) {
    if(line == null) {
      throw new IllegalArgumentException("Null \'line\' argument.");
    }
    double x1 = line.getX1();
    double x2 = line.getX2();
    double deltaX = x2 - x1;
    double y1 = line.getY1();
    double y2 = line.getY2();
    double deltaY = y2 - y1;
    x1 = x1 - (startPercent * deltaX);
    y1 = y1 - (startPercent * deltaY);
    x2 = x2 + (endPercent * deltaX);
    y2 = y2 + (endPercent * deltaY);
    return new Line2D.Double(x1, y1, x2, y2);
  }
  public Paint getSeparatorPaint() {
    return this.separatorPaint;
  }
  public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea, PiePlot plot, Integer index, PlotRenderingInfo info) {
    PiePlotState state = super.initialise(g2, plotArea, plot, index, info);
    state.setPassesRequired(3);
    return state;
  }
  public Stroke getSeparatorStroke() {
    return this.separatorStroke;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof RingPlot)) {
      return false;
    }
    RingPlot that = (RingPlot)obj;
    if(this.separatorsVisible != that.separatorsVisible) {
      return false;
    }
    if(!ObjectUtilities.equal(this.separatorStroke, that.separatorStroke)) {
      return false;
    }
    if(!PaintUtilities.equal(this.separatorPaint, that.separatorPaint)) {
      return false;
    }
    if(this.innerSeparatorExtension != that.innerSeparatorExtension) {
      return false;
    }
    if(this.outerSeparatorExtension != that.outerSeparatorExtension) {
      return false;
    }
    if(this.sectionDepth != that.sectionDepth) {
      return false;
    }
    return super.equals(obj);
  }
  public boolean getSeparatorsVisible() {
    return this.separatorsVisible;
  }
  public double getInnerSeparatorExtension() {
    return this.innerSeparatorExtension;
  }
  protected double getLabelLinkDepth() {
    return Math.min(super.getLabelLinkDepth(), getSectionDepth() / 2);
  }
  public double getOuterSeparatorExtension() {
    return this.outerSeparatorExtension;
  }
  public double getSectionDepth() {
    return this.sectionDepth;
  }
  protected void drawItem(Graphics2D g2, int section, Rectangle2D dataArea, PiePlotState state, int currentPass) {
    PieDataset dataset = getDataset();
    Number n = dataset.getValue(section);
    if(n == null) {
      return ;
    }
    double value = n.doubleValue();
    double angle1 = 0.0D;
    double angle2 = 0.0D;
    Rotation direction = getDirection();
    if(direction == Rotation.CLOCKWISE) {
      angle1 = state.getLatestAngle();
      angle2 = angle1 - value / state.getTotal() * 360.0D;
    }
    else 
      if(direction == Rotation.ANTICLOCKWISE) {
        angle1 = state.getLatestAngle();
        angle2 = angle1 + value / state.getTotal() * 360.0D;
      }
      else {
        throw new IllegalStateException("Rotation type not recognised.");
      }
    double angle = (angle2 - angle1);
    if(Math.abs(angle) > getMinimumArcAngleToDraw()) {
      Comparable key = getSectionKey(section);
      double ep = 0.0D;
      double mep = getMaximumExplodePercent();
      if(mep > 0.0D) {
        ep = getExplodePercent(key) / mep;
      }
      Rectangle2D arcBounds = getArcBounds(state.getPieArea(), state.getExplodedPieArea(), angle1, angle, ep);
      Arc2D.Double arc = new Arc2D.Double(arcBounds, angle1, angle, Arc2D.OPEN);
      double depth = this.sectionDepth / 2.0D;
      RectangleInsets s = new RectangleInsets(UnitType.RELATIVE, depth, depth, depth, depth);
      Rectangle2D innerArcBounds = new Rectangle2D.Double();
      innerArcBounds.setRect(arcBounds);
      s.trim(innerArcBounds);
      Arc2D.Double arc2 = new Arc2D.Double(innerArcBounds, angle1 + angle, -angle, Arc2D.OPEN);
      GeneralPath path = new GeneralPath();
      java.awt.geom.Point2D var_2241 = arc.getStartPoint();
      path.moveTo((float)var_2241.getX(), (float)arc.getStartPoint().getY());
      path.append(arc.getPathIterator(null), false);
      path.append(arc2.getPathIterator(null), true);
      path.closePath();
      Line2D separator = new Line2D.Double(arc2.getEndPoint(), arc.getStartPoint());
      if(currentPass == 0) {
        Paint shadowPaint = getShadowPaint();
        double shadowXOffset = getShadowXOffset();
        double shadowYOffset = getShadowYOffset();
        if(shadowPaint != null) {
          Shape shadowArc = ShapeUtilities.createTranslatedShape(path, (float)shadowXOffset, (float)shadowYOffset);
          g2.setPaint(shadowPaint);
          g2.fill(shadowArc);
        }
      }
      else 
        if(currentPass == 1) {
          Paint paint = lookupSectionPaint(key, false);
          g2.setPaint(paint);
          g2.fill(path);
          Paint outlinePaint = lookupSectionOutlinePaint(key, false);
          Stroke outlineStroke = lookupSectionOutlineStroke(key, false);
          if(outlinePaint != null && outlineStroke != null) {
            g2.setPaint(outlinePaint);
            g2.setStroke(outlineStroke);
            g2.draw(path);
          }
          if(state.getInfo() != null) {
            EntityCollection entities = state.getEntityCollection();
            if(entities != null) {
              String tip = null;
              PieToolTipGenerator toolTipGenerator = getToolTipGenerator();
              if(toolTipGenerator != null) {
                tip = toolTipGenerator.generateToolTip(dataset, key);
              }
              String url = null;
              PieURLGenerator urlGenerator = getURLGenerator();
              if(urlGenerator != null) {
                url = urlGenerator.generateURL(dataset, key, getPieIndex());
              }
              PieSectionEntity entity = new PieSectionEntity(path, dataset, getPieIndex(), section, key, tip, url);
              entities.add(entity);
            }
          }
        }
        else 
          if(currentPass == 2) {
            if(this.separatorsVisible) {
              Line2D extendedSeparator = extendLine(separator, this.innerSeparatorExtension, this.outerSeparatorExtension);
              g2.setStroke(this.separatorStroke);
              g2.setPaint(this.separatorPaint);
              g2.draw(extendedSeparator);
            }
          }
    }
    state.setLatestAngle(angle2);
  }
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.separatorStroke = SerialUtilities.readStroke(stream);
    this.separatorPaint = SerialUtilities.readPaint(stream);
  }
  public void setInnerSeparatorExtension(double percent) {
    this.innerSeparatorExtension = percent;
    fireChangeEvent();
  }
  public void setOuterSeparatorExtension(double percent) {
    this.outerSeparatorExtension = percent;
    fireChangeEvent();
  }
  public void setSectionDepth(double sectionDepth) {
    this.sectionDepth = sectionDepth;
    fireChangeEvent();
  }
  public void setSeparatorPaint(Paint paint) {
    if(paint == null) {
      throw new IllegalArgumentException("Null \'paint\' argument.");
    }
    this.separatorPaint = paint;
    fireChangeEvent();
  }
  public void setSeparatorStroke(Stroke stroke) {
    if(stroke == null) {
      throw new IllegalArgumentException("Null \'stroke\' argument.");
    }
    this.separatorStroke = stroke;
    fireChangeEvent();
  }
  public void setSeparatorsVisible(boolean visible) {
    this.separatorsVisible = visible;
    fireChangeEvent();
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerialUtilities.writeStroke(this.separatorStroke, stream);
    SerialUtilities.writePaint(this.separatorPaint, stream);
  }
}