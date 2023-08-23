package org.jfree.chart.renderer.xy;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.Serializable;
import org.jfree.chart.util.HashUtilities;
import org.jfree.chart.util.RectangleEdge;

public class GradientXYBarPainter implements XYBarPainter, Serializable  {
  private double g1;
  private double g2;
  private double g3;
  public GradientXYBarPainter() {
    this(0.10D, 0.20D, 0.80D);
  }
  public GradientXYBarPainter(double g1, double g2, double g3) {
    super();
    this.g1 = g1;
    this.g2 = g2;
    this.g3 = g3;
  }
  private Rectangle2D createShadow(RectangularShape bar, double xOffset, double yOffset, RectangleEdge base, boolean pegShadow) {
    double x0 = bar.getMinX();
    double x1 = bar.getMaxX();
    double y0 = bar.getMinY();
    double y1 = bar.getMaxY();
    if(base == RectangleEdge.TOP) {
      x0 += xOffset;
      x1 += xOffset;
      if(!pegShadow) {
        y0 += yOffset;
      }
      y1 += yOffset;
    }
    else 
      if(base == RectangleEdge.BOTTOM) {
        x0 += xOffset;
        x1 += xOffset;
        y0 += yOffset;
        if(!pegShadow) {
          y1 += yOffset;
        }
      }
      else 
        if(base == RectangleEdge.LEFT) {
          if(!pegShadow) {
            x0 += xOffset;
          }
          x1 += xOffset;
          y0 += yOffset;
          y1 += yOffset;
        }
        else 
          if(base == RectangleEdge.RIGHT) {
            x0 += xOffset;
            if(!pegShadow) {
              x1 += xOffset;
            }
            y0 += yOffset;
            y1 += yOffset;
          }
    return new Rectangle2D.Double(x0, y0, (x1 - x0), (y1 - y0));
  }
  private Rectangle2D[] splitHorizontalBar(RectangularShape bar, double a, double b, double c) {
    Rectangle2D[] result = new Rectangle2D[4];
    double y0 = bar.getMinY();
    double y1 = Math.rint(y0 + (bar.getHeight() * a));
    double y2 = Math.rint(y0 + (bar.getHeight() * b));
    double y3 = Math.rint(y0 + (bar.getHeight() * c));
    result[0] = new Rectangle2D.Double(bar.getMinX(), bar.getMinY(), bar.getWidth(), y1 - y0);
    result[1] = new Rectangle2D.Double(bar.getMinX(), y1, bar.getWidth(), y2 - y1);
    result[2] = new Rectangle2D.Double(bar.getMinX(), y2, bar.getWidth(), y3 - y2);
    result[3] = new Rectangle2D.Double(bar.getMinX(), y3, bar.getWidth(), bar.getMaxY() - y3);
    return result;
  }
  private Rectangle2D[] splitVerticalBar(RectangularShape bar, double a, double b, double c) {
    Rectangle2D[] result = new Rectangle2D[4];
    double x0 = bar.getMinX();
    double x1 = Math.rint(x0 + (bar.getWidth() * a));
    double x2 = Math.rint(x0 + (bar.getWidth() * b));
    double x3 = Math.rint(x0 + (bar.getWidth() * c));
    result[0] = new Rectangle2D.Double(bar.getMinX(), bar.getMinY(), x1 - x0, bar.getHeight());
    result[1] = new Rectangle2D.Double(x1, bar.getMinY(), x2 - x1, bar.getHeight());
    result[2] = new Rectangle2D.Double(x2, bar.getMinY(), x3 - x2, bar.getHeight());
    result[3] = new Rectangle2D.Double(x3, bar.getMinY(), bar.getMaxX() - x3, bar.getHeight());
    return result;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof GradientXYBarPainter)) {
      return false;
    }
    GradientXYBarPainter that = (GradientXYBarPainter)obj;
    if(this.g1 != that.g1) {
      return false;
    }
    if(this.g2 != that.g2) {
      return false;
    }
    if(this.g3 != that.g3) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    int hash = 37;
    hash = HashUtilities.hashCode(hash, this.g1);
    hash = HashUtilities.hashCode(hash, this.g2);
    hash = HashUtilities.hashCode(hash, this.g3);
    return hash;
  }
  public void paintBar(Graphics2D g2, XYBarRenderer renderer, int row, int column, boolean selected, RectangularShape bar, RectangleEdge base) {
    Paint itemPaint = renderer.getItemPaint(row, column, selected);
    Color c0;
    Color c1;
    if(itemPaint instanceof Color) {
      c0 = (Color)itemPaint;
      c1 = c0.brighter();
    }
    else 
      if(itemPaint instanceof GradientPaint) {
        GradientPaint gp = (GradientPaint)itemPaint;
        c0 = gp.getColor1();
        c1 = gp.getColor2();
      }
      else {
        c0 = Color.blue;
        c1 = Color.blue.brighter();
      }
    if(c0.getAlpha() == 0) {
      return ;
    }
    if(base == RectangleEdge.TOP || base == RectangleEdge.BOTTOM) {
      double var_2956 = this.g2;
      Rectangle2D[] regions = splitVerticalBar(bar, this.g1, var_2956, this.g3);
      GradientPaint gp = new GradientPaint((float)regions[0].getMinX(), 0.0F, c0, (float)regions[0].getMaxX(), 0.0F, Color.white);
      g2.setPaint(gp);
      g2.fill(regions[0]);
      gp = new GradientPaint((float)regions[1].getMinX(), 0.0F, Color.white, (float)regions[1].getMaxX(), 0.0F, c0);
      g2.setPaint(gp);
      g2.fill(regions[1]);
      gp = new GradientPaint((float)regions[2].getMinX(), 0.0F, c0, (float)regions[2].getMaxX(), 0.0F, c1);
      g2.setPaint(gp);
      g2.fill(regions[2]);
      gp = new GradientPaint((float)regions[3].getMinX(), 0.0F, c1, (float)regions[3].getMaxX(), 0.0F, c0);
      g2.setPaint(gp);
      g2.fill(regions[3]);
    }
    else 
      if(base == RectangleEdge.LEFT || base == RectangleEdge.RIGHT) {
        Rectangle2D[] regions = splitHorizontalBar(bar, this.g1, this.g2, this.g3);
        GradientPaint gp = new GradientPaint(0.0F, (float)regions[0].getMinY(), c0, 0.0F, (float)regions[0].getMaxX(), Color.white);
        g2.setPaint(gp);
        g2.fill(regions[0]);
        gp = new GradientPaint(0.0F, (float)regions[1].getMinY(), Color.white, 0.0F, (float)regions[1].getMaxY(), c0);
        g2.setPaint(gp);
        g2.fill(regions[1]);
        gp = new GradientPaint(0.0F, (float)regions[2].getMinY(), c0, 0.0F, (float)regions[2].getMaxY(), c1);
        g2.setPaint(gp);
        g2.fill(regions[2]);
        gp = new GradientPaint(0.0F, (float)regions[3].getMinY(), c1, 0.0F, (float)regions[3].getMaxY(), c0);
        g2.setPaint(gp);
        g2.fill(regions[3]);
      }
    if(renderer.isDrawBarOutline()) {
      Stroke stroke = renderer.getItemOutlineStroke(row, column, selected);
      Paint paint = renderer.getItemOutlinePaint(row, column, selected);
      if(stroke != null && paint != null) {
        g2.setStroke(stroke);
        g2.setPaint(paint);
        g2.draw(bar);
      }
    }
  }
  public void paintBarShadow(Graphics2D g2, XYBarRenderer renderer, int row, int column, boolean selected, RectangularShape bar, RectangleEdge base, boolean pegShadow) {
    Paint itemPaint = renderer.getItemPaint(row, column, selected);
    if(itemPaint instanceof Color) {
      Color c = (Color)itemPaint;
      if(c.getAlpha() == 0) {
        return ;
      }
    }
    RectangularShape shadow = createShadow(bar, renderer.getShadowXOffset(), renderer.getShadowYOffset(), base, pegShadow);
    g2.setPaint(Color.gray);
    g2.fill(shadow);
  }
}