package org.jfree.chart.ui;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class StrokeSample extends JComponent implements ListCellRenderer  {
  private Stroke stroke;
  private Dimension preferredSize;
  public StrokeSample(Stroke stroke) {
    super();
    this.stroke = stroke;
    this.preferredSize = new Dimension(80, 18);
  }
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    if(value instanceof StrokeSample) {
      final StrokeSample in = (StrokeSample)value;
      setStroke(in.getStroke());
    }
    return this;
  }
  public Dimension getPreferredSize() {
    return this.preferredSize;
  }
  public Stroke getStroke() {
    return this.stroke;
  }
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Dimension size = getSize();
    Insets insets = getInsets();
    double xx = insets.left;
    double yy = insets.top;
    double ww = size.getWidth() - insets.left - insets.right;
    double hh = size.getHeight() - insets.top - insets.bottom;
    Point2D one = new Point2D.Double(xx + 6, yy + hh / 2);
    Point2D two = new Point2D.Double(xx + ww - 6, yy + hh / 2);
    Ellipse2D circle1 = new Ellipse2D.Double(one.getX() - 5, one.getY() - 5, 10, 10);
    Ellipse2D circle2 = new Ellipse2D.Double(two.getX() - 6, two.getY() - 5, 10, 10);
    g2.draw(circle1);
    g2.fill(circle1);
    g2.draw(circle2);
    g2.fill(circle2);
    Line2D line = new Line2D.Double(one, two);
    Stroke var_3577 = this.stroke;
    if(var_3577 != null) {
      g2.setStroke(this.stroke);
    }
    else {
      g2.setStroke(new BasicStroke(0.0F));
    }
    g2.draw(line);
  }
  public void setStroke(Stroke stroke) {
    this.stroke = stroke;
    repaint();
  }
}