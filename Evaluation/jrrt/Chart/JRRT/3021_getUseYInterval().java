package org.jfree.chart.renderer.xy;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

public class ClusteredXYBarRenderer extends XYBarRenderer implements Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 5864462149177133147L;
  private boolean centerBarAtStartValue;
  public ClusteredXYBarRenderer() {
    this(0.0D, false);
  }
  public ClusteredXYBarRenderer(double margin, boolean centerBarAtStartValue) {
    super(margin);
    this.centerBarAtStartValue = centerBarAtStartValue;
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public Range findDomainBounds(XYDataset dataset) {
    if(dataset == null) {
      return null;
    }
    if(this.centerBarAtStartValue) {
      return findDomainBoundsWithOffset((IntervalXYDataset)dataset);
    }
    else {
      return super.findDomainBounds(dataset);
    }
  }
  protected Range findDomainBoundsWithOffset(IntervalXYDataset dataset) {
    if(dataset == null) {
      throw new IllegalArgumentException("Null \'dataset\' argument.");
    }
    double minimum = Double.POSITIVE_INFINITY;
    double maximum = Double.NEGATIVE_INFINITY;
    int seriesCount = dataset.getSeriesCount();
    double lvalue;
    double uvalue;
    for(int series = 0; series < seriesCount; series++) {
      int itemCount = dataset.getItemCount(series);
      for(int item = 0; item < itemCount; item++) {
        lvalue = dataset.getStartXValue(series, item);
        uvalue = dataset.getEndXValue(series, item);
        double offset = (uvalue - lvalue) / 2.0D;
        lvalue = lvalue - offset;
        uvalue = uvalue - offset;
        minimum = Math.min(minimum, lvalue);
        maximum = Math.max(maximum, uvalue);
      }
    }
    if(minimum > maximum) {
      return null;
    }
    else {
      return new Range(minimum, maximum);
    }
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof ClusteredXYBarRenderer)) {
      return false;
    }
    ClusteredXYBarRenderer that = (ClusteredXYBarRenderer)obj;
    if(this.centerBarAtStartValue != that.centerBarAtStartValue) {
      return false;
    }
    return super.equals(obj);
  }
  public int getPassCount() {
    return 2;
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    IntervalXYDataset intervalDataset = (IntervalXYDataset)dataset;
    double y0;
    double y1;
    boolean var_3021 = getUseYInterval();
    if(var_3021) {
      y0 = intervalDataset.getStartYValue(series, item);
      y1 = intervalDataset.getEndYValue(series, item);
    }
    else {
      y0 = getBase();
      y1 = intervalDataset.getYValue(series, item);
    }
    if(Double.isNaN(y0) || Double.isNaN(y1)) {
      return ;
    }
    double yy0 = rangeAxis.valueToJava2D(y0, dataArea, plot.getRangeAxisEdge());
    double yy1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());
    RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
    double x0 = intervalDataset.getStartXValue(series, item);
    double xx0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
    double x1 = intervalDataset.getEndXValue(series, item);
    double xx1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
    double intervalW = xx1 - xx0;
    double baseX = xx0;
    if(this.centerBarAtStartValue) {
      baseX = baseX - intervalW / 2.0D;
    }
    double m = getMargin();
    if(m > 0.0D) {
      double cut = intervalW * getMargin();
      intervalW = intervalW - cut;
      baseX = baseX + (cut / 2);
    }
    double intervalH = Math.abs(yy0 - yy1);
    PlotOrientation orientation = plot.getOrientation();
    int numSeries = dataset.getSeriesCount();
    double seriesBarWidth = intervalW / numSeries;
    Rectangle2D bar = null;
    if(orientation == PlotOrientation.HORIZONTAL) {
      double barY0 = baseX + (seriesBarWidth * series);
      double barY1 = barY0 + seriesBarWidth;
      double rx = Math.min(yy0, yy1);
      double rw = intervalH;
      double ry = Math.min(barY0, barY1);
      double rh = Math.abs(barY1 - barY0);
      bar = new Rectangle2D.Double(rx, ry, rw, rh);
    }
    else 
      if(orientation == PlotOrientation.VERTICAL) {
        double barX0 = baseX + (seriesBarWidth * series);
        double barX1 = barX0 + seriesBarWidth;
        double rx = Math.min(barX0, barX1);
        double rw = Math.abs(barX1 - barX0);
        double ry = Math.min(yy0, yy1);
        double rh = intervalH;
        bar = new Rectangle2D.Double(rx, ry, rw, rh);
      }
    boolean positive = (y1 > 0.0D);
    boolean inverted = rangeAxis.isInverted();
    RectangleEdge barBase;
    if(orientation == PlotOrientation.HORIZONTAL) {
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
    if(pass == 0 && getShadowsVisible()) {
      getBarPainter().paintBarShadow(g2, this, series, item, selected, bar, barBase, !getUseYInterval());
    }
    if(pass == 1) {
      getBarPainter().paintBar(g2, this, series, item, selected, bar, barBase);
      if(isItemLabelVisible(series, item, selected)) {
        XYItemLabelGenerator generator = getItemLabelGenerator(series, item, selected);
        drawItemLabelForBar(g2, plot, dataset, series, item, selected, generator, bar, y1 < 0.0D);
      }
      if(state.getInfo() != null) {
        EntityCollection entities = state.getInfo().getOwner().getEntityCollection();
        if(entities != null) {
          addEntity(entities, bar, dataset, series, item, selected, bar.getCenterX(), bar.getCenterY());
        }
      }
    }
  }
}