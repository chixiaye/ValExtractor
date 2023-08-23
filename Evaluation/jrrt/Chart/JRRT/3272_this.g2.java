package org.jfree.chart.text;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class G2TextMeasurer implements TextMeasurer  {
  private Graphics2D g2;
  public G2TextMeasurer(Graphics2D g2) {
    super();
    this.g2 = g2;
  }
  public float getStringWidth(String text, int start, int end) {
    Graphics2D var_3272 = this.g2;
    FontMetrics fm = var_3272.getFontMetrics();
    Rectangle2D bounds = TextUtilities.getTextBounds(text.substring(start, end), this.g2, fm);
    float result = (float)bounds.getWidth();
    return result;
  }
}