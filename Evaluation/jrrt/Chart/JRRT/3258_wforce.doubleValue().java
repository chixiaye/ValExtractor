package org.jfree.chart.renderer.xy;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.data.xy.WindDataset;
import org.jfree.data.xy.XYDataset;

public class WindItemRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable  {
  final private static long serialVersionUID = 8078914101916976844L;
  public WindItemRenderer() {
    super();
  }
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D plotArea, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, boolean selected, int pass) {
    WindDataset windData = (WindDataset)dataset;
    Paint seriesPaint = getItemPaint(series, item, selected);
    Stroke seriesStroke = getItemStroke(series, item, selected);
    g2.setPaint(seriesPaint);
    g2.setStroke(seriesStroke);
    Number x = windData.getX(series, item);
    Number windDir = windData.getWindDirection(series, item);
    Number wforce = windData.getWindForce(series, item);
    double windForce = wforce.doubleValue();
    double wdirt = Math.toRadians(windDir.doubleValue() * (-30.0D) - 90.0D);
    double ax1;
    double ax2;
    double ay1;
    double ay2;
    double rax2;
    double ray2;
    RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
    RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
    ax1 = domainAxis.valueToJava2D(x.doubleValue(), plotArea, domainAxisLocation);
    ay1 = rangeAxis.valueToJava2D(0.0D, plotArea, rangeAxisLocation);
    rax2 = x.doubleValue() + (windForce * Math.cos(wdirt) * 8000000.0D);
    ray2 = windForce * Math.sin(wdirt);
    ax2 = domainAxis.valueToJava2D(rax2, plotArea, domainAxisLocation);
    ay2 = rangeAxis.valueToJava2D(ray2, plotArea, rangeAxisLocation);
    int diri = windDir.intValue();
    int forcei = wforce.intValue();
    String dirforce = diri + "-" + forcei;
    Line2D line = new Line2D.Double(ax1, ay1, ax2, ay2);
    g2.draw(line);
    g2.setPaint(Color.blue);
    g2.setFont(new Font("Tahoma", 1, 9));
    g2.drawString(dirforce, (float)ax1, (float)ay1);
    g2.setPaint(seriesPaint);
    g2.setStroke(seriesStroke);
    double alx2;
    double aly2;
    double arx2;
    double ary2;
    double ralx2;
    double raly2;
    double rarx2;
    double rary2;
    double aldir = Math.toRadians(windDir.doubleValue() * (-30.0D) - 90.0D - 5.0D);
    ralx2 = wforce.doubleValue() * Math.cos(aldir) * 8000000 * 0.8D + x.doubleValue();
    raly2 = wforce.doubleValue() * Math.sin(aldir) * 0.8D;
    alx2 = domainAxis.valueToJava2D(ralx2, plotArea, domainAxisLocation);
    aly2 = rangeAxis.valueToJava2D(raly2, plotArea, rangeAxisLocation);
    line = new Line2D.Double(alx2, aly2, ax2, ay2);
    g2.draw(line);
    double ardir = Math.toRadians(windDir.doubleValue() * (-30.0D) - 90.0D + 5.0D);
    double var_3258 = wforce.doubleValue();
    rarx2 = var_3258 * Math.cos(ardir) * 8000000 * 0.8D + x.doubleValue();
    rary2 = wforce.doubleValue() * Math.sin(ardir) * 0.8D;
    arx2 = domainAxis.valueToJava2D(rarx2, plotArea, domainAxisLocation);
    ary2 = rangeAxis.valueToJava2D(rary2, plotArea, rangeAxisLocation);
    line = new Line2D.Double(arx2, ary2, ax2, ay2);
    g2.draw(line);
  }
}